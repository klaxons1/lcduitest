#!/usr/bin/env python3
"""
Extracts frames from a video and turns them into the compact data file used by
the J2ME "Lotus" demo (res/lotus.bin).

Pipeline:
  1. ffmpeg samples N frames evenly over the whole clip (decoded as raw RGB).
  2. Every frame is box-averaged to a SECTORS x BANDS grid of cells. Each cell is
     one flat colour; on the device every cell becomes one single-colour polygon.
  3. All cells are quantised to a small shared palette (PALETTE colours).
  4. The result is written as a binary blob: header, palette, cell indices.

Only the derived binary is committed; the decoded frames stay outside the repo.

Requirements: ffmpeg (or imageio-ffmpeg, which bundles one), numpy, Pillow.

Usage:
  python3 tools/make_lotus_data.py --video "docs/Phantasm Mineshaft Chase scene.mp4" \
      --out res/lotus.bin [--frames 96] [--preview /tmp/lotus_preview.png]
"""

import argparse
import shutil
import struct
import subprocess
import sys

import numpy as np
from PIL import Image

MAGIC = b"LOT1"
VERSION = 1

# Source grid resolution per frame. Frame is first scaled to SRC_W x SRC_H with
# ffmpeg (area filter), then block-averaged down to SECTORS x BANDS.
SECTORS = 32          # cells around the tunnel (one column of a frame)
BANDS = 8             # cell rows per frame (depth slices of the tunnel)
SRC_W = SECTORS * 10  # 320
SRC_H = BANDS * 18    # 144 (keeps the 20:9 film aspect as a 3:1 grid of cells)


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


def extract_frames(ffmpeg, video, count, duration):
    """Returns a float array of shape (count, BANDS, SECTORS, 3) in 0..255."""
    rate = count / duration
    cmd = [
        ffmpeg, "-hide_banner", "-loglevel", "error", "-i", video,
        "-vf", "fps=%.6f,scale=%d:%d:flags=area" % (rate, SRC_W, SRC_H),
        "-frames:v", str(count), "-f", "rawvideo", "-pix_fmt", "rgb24", "-",
    ]
    raw = subprocess.run(cmd, stdout=subprocess.PIPE, check=True).stdout
    frame_bytes = SRC_W * SRC_H * 3
    n = len(raw) // frame_bytes
    if n < count:
        print("warning: decoded %d of %d requested frames" % (n, count))
    frames = np.frombuffer(raw[: n * frame_bytes], dtype=np.uint8)
    frames = frames.reshape(n, SRC_H, SRC_W, 3).astype(np.float32)
    # Block-average SRC_H x SRC_W down to BANDS x SECTORS.
    bh, bw = SRC_H // BANDS, SRC_W // SECTORS
    cells = frames.reshape(n, BANDS, bh, SECTORS, bw, 3).mean(axis=(2, 4))
    return cells


def grade(cells):
    """Lifts the (very dark) film footage: exposure from a robust peak + gamma +
    a little saturation, so the flat polygons read clearly on a small LCD."""
    lum = cells.mean(axis=3)
    peak = np.percentile(lum, 97.0)
    peak = max(peak, 1.0)
    x = np.clip(cells / peak, 0.0, 1.0) ** 0.45
    gray = x.mean(axis=3, keepdims=True)
    x = gray + (x - gray) * 1.35  # saturation
    return (np.clip(x, 0.0, 1.0) * 255.0 + 0.5).astype(np.uint8)


def quantise(cells):
    n, bands, sectors, _ = cells.shape
    # Stack everything into one tall image so the palette is shared by all frames.
    tall = cells.reshape(n * bands, sectors, 3)
    img = Image.fromarray(tall, "RGB")
    pal_img = img.quantize(colors=PALETTE, method=Image.MEDIANCUT, dither=Image.NONE)
    palette = pal_img.getpalette()[: PALETTE * 3]
    indices = np.array(pal_img, dtype=np.uint8).reshape(n, bands, sectors)
    return palette, indices


def write_blob(path, palette, indices):
    frames, bands, sectors = indices.shape
    colours = len(palette) // 3
    with open(path, "wb") as f:
        f.write(MAGIC)
        f.write(struct.pack(">BBBBH", VERSION, colours, bands, sectors, frames))
        f.write(bytes(palette))
        f.write(indices.tobytes())


def write_preview(path, cells, palette, indices):
    pal = np.array(palette, dtype=np.uint8).reshape(-1, 3)
    shown = pal[indices]  # quantised look, as seen on the device
    n = shown.shape[0]
    cols = 8
    rows = (n + cols - 1) // cols
    sheet = np.zeros((rows * BANDS, cols * SECTORS, 3), dtype=np.uint8)
    for i in range(n):
        r, c = divmod(i, cols)
        sheet[r * BANDS:(r + 1) * BANDS, c * SECTORS:(c + 1) * SECTORS] = shown[i]
    Image.fromarray(sheet, "RGB").resize((cols * SECTORS * 4, rows * BANDS * 4),
                                         Image.NEAREST).save(path)


def main():
    global PALETTE
    ap = argparse.ArgumentParser(description=__doc__,
                                 formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--video", required=True)
    ap.add_argument("--out", default="res/lotus.bin")
    ap.add_argument("--frames", type=int, default=96)
    ap.add_argument("--palette", type=int, default=64, help="2..255 colours")
    ap.add_argument("--preview", help="optional PNG contact sheet of the result")
    args = ap.parse_args()
    PALETTE = max(2, min(255, args.palette))

    ffmpeg = find_ffmpeg()
    duration = probe_duration(ffmpeg, args.video)
    print("video: %s (%.2f s)" % (args.video, duration))

    cells = extract_frames(ffmpeg, args.video, args.frames, duration)
    print("sampled %d frames -> %d bands x %d sectors each" % (cells.shape[0], BANDS, SECTORS))

    graded = grade(cells)
    palette, indices = quantise(graded)
    write_blob(args.out, palette, indices)
    size = 4 + 6 + len(palette) + indices.size
    print("wrote %s (%d bytes, %d colours)" % (args.out, size, len(palette) // 3))
    if args.preview:
        write_preview(args.preview, graded, palette, indices)
        print("preview: %s" % args.preview)


if __name__ == "__main__":
    main()
