#!/usr/bin/env python3
"""
Turns a video into the film data used by the J2ME "Lotus" demo (res/lotus.bin).

Every sampled frame of the video is box-averaged to a COLS x ROWS grid. On the device each
grid cell is drawn as one single-colour polygon, so the frame plays back as a mosaic film.

Pipeline:
  1. ffmpeg decodes the chosen time range, sampled at --fps, scaled to COLS x ROWS (area filter).
  2. The whole sequence is graded (exposure, gamma, a little saturation) so dark footage reads.
  3. All cells are quantised to one shared palette of --colours entries.
  4. Frames are written in order as packed palette indices (4 bits per cell for <=16 colours,
     8 bits otherwise).

Format "LOT2" (big-endian):
  4 bytes  magic "LOT2"
  u8       version (2)
  u8       bits per cell (4 or 8)
  u8       cols
  u8       rows
  u8       palette size
  u8       fps (frames per second of playback)
  u16      frame count
  palette  size * (R, G, B)
  frames   frame_count * packed(cols*rows cells), row-major, one frame after another

Only the derived binary is committed; decoded frames stay outside the repo.

Requirements: ffmpeg (or imageio-ffmpeg, which bundles one), numpy, Pillow.

Usage:
  python3 tools/make_lotus_data.py --video "docs/Phantasm Mineshaft Chase scene.mp4" \
      --out res/lotus.bin --fps 6 --cols 40 --rows 22 --colours 16 [--start 0 --duration 0]
"""

import argparse
import shutil
import struct
import subprocess
import sys

import numpy as np
from PIL import Image

MAGIC = b"LOT2"
VERSION = 2


def find_ffmpeg():
    exe = shutil.which("ffmpeg")
    if exe:
        return exe
    try:
        import imageio_ffmpeg  # type: ignore
        return imageio_ffmpeg.get_ffmpeg_exe()
    except ImportError:
        sys.exit("ffmpeg not found: install ffmpeg or `pip install imageio-ffmpeg`")


def probe_duration(ffmpeg, video):
    # ffmpeg prints "Duration: HH:MM:SS.xx" on stderr when given only -i.
    proc = subprocess.run([ffmpeg, "-hide_banner", "-i", video],
                          stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    text = proc.stderr.decode("utf-8", "replace")
    for line in text.splitlines():
        line = line.strip()
        if line.startswith("Duration:"):
            stamp = line.split(",")[0].split("Duration:")[1].strip()
            h, m, s = stamp.split(":")
            return int(h) * 3600 + int(m) * 60 + float(s)
    sys.exit("could not read video duration")


def extract_frames(ffmpeg, video, start, duration, fps, cols, rows):
    """Returns float array (frames, rows, cols, 3) of box-averaged colours in 0..255."""
    cmd = [ffmpeg, "-hide_banner", "-loglevel", "error"]
    if start > 0:
        cmd += ["-ss", "%.3f" % start]
    cmd += ["-i", video]
    if duration > 0:
        cmd += ["-t", "%.3f" % duration]
    cmd += [
        "-vf", "fps=%.6f,scale=%d:%d:flags=area" % (fps, cols, rows),
        "-f", "rawvideo", "-pix_fmt", "rgb24", "-",
    ]
    raw = subprocess.run(cmd, stdout=subprocess.PIPE, check=True).stdout
    frame_bytes = cols * rows * 3
    n = len(raw) // frame_bytes
    if n == 0:
        sys.exit("no frames decoded; check --start/--duration")
    frames = np.frombuffer(raw[: n * frame_bytes], dtype=np.uint8)
    return frames.reshape(n, rows, cols, 3).astype(np.float32)


def grade(cells):
    """Lifts dark footage: exposure from a robust peak, gamma and a little saturation."""
    lum = cells.mean(axis=3)
    peak = max(np.percentile(lum, 97.0), 1.0)
    x = np.clip(cells / peak, 0.0, 1.0) ** 0.45
    gray = x.mean(axis=3, keepdims=True)
    x = gray + (x - gray) * 1.35
    return (np.clip(x, 0.0, 1.0) * 255.0 + 0.5).astype(np.uint8)


def quantise(cells, colours):
    n, rows, cols, _ = cells.shape
    # One tall image so every frame shares the palette.
    tall = Image.fromarray(cells.reshape(n * rows, cols, 3), "RGB")
    pal_img = tall.quantize(colors=colours, method=Image.MEDIANCUT, dither=Image.NONE)
    palette = pal_img.getpalette()[: colours * 3]
    indices = np.array(pal_img, dtype=np.uint8).reshape(n, rows * cols)
    return palette, indices


def pack(indices, bits):
    """Packs (frames, cells) palette indices. 4-bit: two cells per byte, high nibble first."""
    if bits == 8:
        return indices.astype(np.uint8)
    frames, cells = indices.shape
    if cells % 2:
        indices = np.concatenate([indices, np.zeros((frames, 1), np.uint8)], axis=1)
    pairs = indices.reshape(frames, -1, 2)
    return ((pairs[:, :, 0] << 4) | pairs[:, :, 1]).astype(np.uint8)


def write_blob(path, palette, indices, bits, cols, rows, fps):
    frames = indices.shape[0]
    packed = pack(indices, bits)
    with open(path, "wb") as f:
        f.write(MAGIC)
        f.write(struct.pack(">BBBBBBH", VERSION, bits, cols, rows,
                            len(palette) // 3, fps, frames))
        f.write(bytes(palette))
        f.write(packed.tobytes())
    return packed.shape[1]


def write_preview(path, palette, indices, cols, rows, count):
    pal = np.array(palette, dtype=np.uint8).reshape(-1, 3)
    step = max(1, indices.shape[0] // count)
    picks = list(range(0, indices.shape[0], step))[:count]
    cols_sheet = 6
    scale = 4
    tile_h, tile_w = rows * scale, cols * scale
    rows_sheet = (len(picks) + cols_sheet - 1) // cols_sheet
    sheet = np.zeros((rows_sheet * (tile_h + 4), cols_sheet * (tile_w + 4), 3), dtype=np.uint8)
    for i, f in enumerate(picks):
        frame = pal[indices[f]].reshape(rows, cols, 3)
        big = np.repeat(np.repeat(frame, scale, axis=0), scale, axis=1)
        r, c = divmod(i, cols_sheet)
        sheet[r * (tile_h + 4):r * (tile_h + 4) + tile_h,
              c * (tile_w + 4):c * (tile_w + 4) + tile_w] = big
    Image.fromarray(sheet, "RGB").save(path)


def main():
    ap = argparse.ArgumentParser(description=__doc__,
                                 formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--video", required=True)
    ap.add_argument("--out", default="res/lotus.bin")
    ap.add_argument("--start", type=float, default=0.0, help="start time in seconds")
    ap.add_argument("--duration", type=float, default=0.0,
                    help="length in seconds (0 = until the end of the video)")
    ap.add_argument("--fps", type=int, default=6, help="playback frames per second (1..60)")
    ap.add_argument("--cols", type=int, default=40, help="grid columns (cells per frame)")
    ap.add_argument("--rows", type=int, default=22, help="grid rows (cells per frame)")
    ap.add_argument("--colours", type=int, default=16, help="palette size, 2..255")
    ap.add_argument("--preview", help="optional PNG contact sheet of the result")
    args = ap.parse_args()

    if not 1 <= args.fps <= 60:
        sys.exit("--fps must be 1..60")
    if not 2 <= args.colours <= 255:
        sys.exit("--colours must be 2..255")
    if not (1 <= args.cols <= 255 and 1 <= args.rows <= 255):
        sys.exit("--cols/--rows must be 1..255")
    bits = 4 if args.colours <= 16 else 8
    colours = min(args.colours, 16) if bits == 4 else args.colours

    ffmpeg = find_ffmpeg()
    if args.duration <= 0:
        duration = probe_duration(ffmpeg, args.video) - args.start
    else:
        duration = args.duration
    print("video: %s, range %.2f s .. %.2f s at %d fps" %
          (args.video, args.start, args.start + duration, args.fps))

    cells = extract_frames(ffmpeg, args.video, args.start, duration,
                           args.fps, args.cols, args.rows)
    if cells.shape[0] > 65535:
        sys.exit("too many frames for the u16 counter; shorten --duration or lower --fps")
    print("sampled %d frames of %dx%d cells" % (cells.shape[0], args.cols, args.rows))

    graded = grade(cells)
    palette, indices = quantise(graded, colours)
    frame_bytes = write_blob(args.out, palette, indices, bits, args.cols, args.rows, args.fps)
    total = 4 + 8 + len(palette) + cells.shape[0] * frame_bytes
    print("wrote %s: %d frames, %d bytes per frame, %d colours (%d-bit), %d bytes total" %
          (args.out, cells.shape[0], frame_bytes, len(palette) // 3, bits, total))
    if args.preview:
        write_preview(args.preview, palette, indices, args.cols, args.rows, 24)
        print("preview: %s" % args.preview)


if __name__ == "__main__":
    main()
