#!/usr/bin/env python3
"""
Turns a video into the triangle film used by the J2ME "Lotus" demo (res/lotus.bin).

The screen is covered by a jittered triangle mesh (irregular facets, like the low-poly
reference). The mesh is static and stored once in the file. For every sampled frame each
triangle gets ONE colour: the mean colour of the video pixels under that triangle. On the
device every triangle is drawn as a flat-coloured polygon, so the video plays as a facet film.

Pipeline:
  1. ffmpeg decodes the chosen time range at --fps, cropped (default) or stretched to
     SCREEN_W x SCREEN_H so the film fills the whole screen.
  2. Triangle ids are rasterised once into a label map of the screen.
  3. Per frame, the colour of each triangle is the mean of its pixels (numpy bincount).
  4. The colours are graded (exposure, gamma, a little saturation) and quantised to --colours.
  5. Written as format "LOT3" (see below).

Format "LOT3" (big-endian):
  4 bytes  magic "LOT3"
  u8       version (3)
  u8       bits per triangle colour (4 or 8)
  u8       palette size
  u8       fps
  u16      frame count
  u16      vertex count
  u16      triangle count
  vertices vertex count * (u16 x, u16 y), normalised to 0..1024 (scaled to the screen on device)
  tris     triangle count * (u16 a, u16 b, u16 c), indices into the vertex list
  palette  size * (R, G, B)
  frames   frame count * packed(triangle count colours), one frame after another

Only the derived binary is committed; decoded frames stay outside the repo.

Requirements: ffmpeg (or imageio-ffmpeg, which bundles one), numpy, Pillow.

Usage:
  python3 tools/make_lotus_data.py --video "docs/Phantasm Mineshaft Chase scene.mp4" \
      --out res/lotus.bin --fps 6 --cols 18 --rows 24 --colours 16 [--fit crop|stretch]
"""

import argparse
import random
import shutil
import struct
import subprocess
import sys

import numpy as np
from PIL import Image, ImageDraw

MAGIC = b"LOT3"
VERSION = 3
SCREEN_W = 240
SCREEN_H = 320
NORM = 1024


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


def build_mesh(cols, rows, jitter, seed):
    """Jittered triangle mesh over the screen. Returns (vertices, triangles).

    vertices: list of (x, y) in screen pixels (floats). Border vertices stay on the border so
    the mesh covers the screen exactly. Each cell is split along a random diagonal.
    """
    rng = random.Random(seed)
    cw = SCREEN_W / float(cols)
    ch = SCREEN_H / float(rows)
    verts = []
    for j in range(rows + 1):
        for i in range(cols + 1):
            x = i * cw
            y = j * ch
            if 0 < i < cols and 0 < j < rows:
                x += rng.uniform(-jitter, jitter) * cw
                y += rng.uniform(-jitter, jitter) * ch
            verts.append((x, y))
    tris = []
    stride = cols + 1
    for j in range(rows):
        for i in range(cols):
            a = j * stride + i
            b = a + 1
            c = a + stride
            d = c + 1
            if rng.random() < 0.5:
                tris.append((a, b, d))
                tris.append((a, d, c))
            else:
                tris.append((a, b, c))
                tris.append((b, d, c))
    return verts, tris


def label_map(verts, tris):
    """Screen-sized map: pixel -> triangle index + 1 (0 = outside, none expected)."""
    img = Image.new("I", (SCREEN_W, SCREEN_H), 0)
    draw = ImageDraw.Draw(img)
    for n, (a, b, c) in enumerate(tris):
        poly = [verts[a], verts[b], verts[c]]
        draw.polygon(poly, fill=n + 1)
    return np.array(img, dtype=np.int64)


def stream_triangle_colours(ffmpeg, video, start, duration, fps, fit, labels, tri_count):
    """Yields per-frame triangle colours (tri_count, 3) as float32, one frame at a time."""
    if fit == "crop":
        # Keep the screen aspect: centre crop of the source, then scale to the screen.
        vf = ("crop=trunc(ih*%d/%d/2)*2:ih:trunc((iw-ih*%d/%d)/4)*2:0,"
              "fps=%.6f,scale=%d:%d:flags=area") % (SCREEN_W, SCREEN_H, SCREEN_W, SCREEN_H,
                                                    fps, SCREEN_W, SCREEN_H)
    else:
        vf = "fps=%.6f,scale=%d:%d:flags=area" % (fps, SCREEN_W, SCREEN_H)
    cmd = [ffmpeg, "-hide_banner", "-loglevel", "error"]
    if start > 0:
        cmd += ["-ss", "%.3f" % start]
    cmd += ["-i", video]
    if duration > 0:
        cmd += ["-t", "%.3f" % duration]
    cmd += ["-vf", vf, "-f", "rawvideo", "-pix_fmt", "rgb24", "-"]

    frame_bytes = SCREEN_W * SCREEN_H * 3
    proc = subprocess.Popen(cmd, stdout=subprocess.PIPE)
    flat = labels.ravel()
    counts = np.bincount(flat, minlength=tri_count + 1)[1:].astype(np.float64)
    empty = counts == 0
    try:
        while True:
            raw = proc.stdout.read(frame_bytes)
            if len(raw) < frame_bytes:
                break
            img = np.frombuffer(raw, dtype=np.uint8).reshape(SCREEN_H, SCREEN_W, 3)
            out = np.zeros((tri_count, 3), dtype=np.float32)
            for ch in range(3):
                sums = np.bincount(flat, weights=img[:, :, ch].ravel().astype(np.float64),
                                   minlength=tri_count + 1)[1:]
                out[:, ch] = sums / np.maximum(counts, 1.0)
            yield out
    finally:
        proc.stdout.close()
        proc.wait()
    if empty.any():
        print("warning: %d triangles covered no pixels" % int(empty.sum()))


def grade(colours):
    """Lifts dark footage: exposure from a robust peak, gamma and a little saturation."""
    lum = colours.mean(axis=2)
    peak = max(np.percentile(lum, 97.0), 1.0)
    x = np.clip(colours / peak, 0.0, 1.0) ** 0.45
    gray = x.mean(axis=2, keepdims=True)
    x = gray + (x - gray) * 1.35
    return (np.clip(x, 0.0, 1.0) * 255.0 + 0.5).astype(np.uint8)


def quantise(graded, colours):
    n, tris, _ = graded.shape
    tall = Image.fromarray(graded, "RGB")  # width = triangles, height = frames
    pal_img = tall.quantize(colors=colours, method=Image.MEDIANCUT, dither=Image.NONE)
    palette = pal_img.getpalette()[: colours * 3]
    indices = np.array(pal_img, dtype=np.uint8).reshape(n, tris)
    return palette, indices


def pack(indices, bits):
    """4-bit: two triangles per byte, high nibble first."""
    if bits == 8:
        return indices.astype(np.uint8)
    frames, count = indices.shape
    if count % 2:
        indices = np.concatenate([indices, np.zeros((frames, 1), np.uint8)], axis=1)
    pairs = indices.reshape(frames, -1, 2)
    return ((pairs[:, :, 0] << 4) | pairs[:, :, 1]).astype(np.uint8)


def write_blob(path, verts, tris, palette, indices, bits, fps):
    frames = indices.shape[0]
    packed = pack(indices, bits)
    with open(path, "wb") as f:
        f.write(MAGIC)
        f.write(struct.pack(">BBBBHHH", VERSION, bits, len(palette) // 3, fps,
                            frames, len(verts), len(tris)))
        for x, y in verts:
            nx = int(round(min(max(x / SCREEN_W, 0.0), 1.0) * NORM))
            ny = int(round(min(max(y / SCREEN_H, 0.0), 1.0) * NORM))
            f.write(struct.pack(">HH", nx, ny))
        for a, b, c in tris:
            f.write(struct.pack(">HHH", a, b, c))
        f.write(bytes(palette))
        f.write(packed.tobytes())
    return packed.shape[1]


def write_preview(path, verts, tris, palette, indices, count):
    pal = np.array(palette, dtype=np.uint8).reshape(-1, 3)
    n = indices.shape[0]
    step = max(1, n // count)
    picks = list(range(0, n, step))[:count]
    cols_sheet = 6
    scale = 1
    tw, th = SCREEN_W // 2, SCREEN_H // 2
    rows_sheet = (len(picks) + cols_sheet - 1) // cols_sheet
    sheet = Image.new("RGB", (cols_sheet * (tw + 4), rows_sheet * (th + 4)), (30, 30, 30))
    for i, f in enumerate(picks):
        frame = Image.new("RGB", (SCREEN_W, SCREEN_H), (0, 0, 0))
        draw = ImageDraw.Draw(frame)
        for t, (a, b, c) in enumerate(tris):
            colour = tuple(int(v) for v in pal[indices[f, t]])
            draw.polygon([verts[a], verts[b], verts[c]], fill=colour)
        frame = frame.resize((tw, th), Image.BILINEAR)
        r, c = divmod(i, cols_sheet)
        sheet.paste(frame, (c * (tw + 4), r * (th + 4)))
    sheet.save(path)


def main():
    ap = argparse.ArgumentParser(description=__doc__,
                                 formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--video", required=True)
    ap.add_argument("--out", default="res/lotus.bin")
    ap.add_argument("--start", type=float, default=0.0, help="start time in seconds")
    ap.add_argument("--duration", type=float, default=0.0,
                    help="length in seconds (0 = until the end of the video)")
    ap.add_argument("--fps", type=int, default=6, help="playback frames per second (1..60)")
    ap.add_argument("--cols", type=int, default=18, help="mesh cells across the screen")
    ap.add_argument("--rows", type=int, default=24, help="mesh cells down the screen")
    ap.add_argument("--jitter", type=float, default=0.32,
                    help="vertex jitter as a fraction of a cell (0 = regular grid)")
    ap.add_argument("--seed", type=int, default=7, help="mesh seed (changes facet shapes)")
    ap.add_argument("--colours", type=int, default=16, help="palette size, 2..255")
    ap.add_argument("--fit", choices=["crop", "stretch"], default="crop",
                    help="crop: keep aspect, fill the screen; stretch: squeeze the whole frame")
    ap.add_argument("--preview", help="optional PNG contact sheet of the result")
    args = ap.parse_args()

    if not 1 <= args.fps <= 60:
        sys.exit("--fps must be 1..60")
    if not 2 <= args.colours <= 255:
        sys.exit("--colours must be 2..255")
    if not (1 <= args.cols <= 60 and 1 <= args.rows <= 60):
        sys.exit("--cols/--rows must be 1..60")
    if not 0.0 <= args.jitter <= 0.45:
        sys.exit("--jitter must be 0..0.45 (larger values can fold triangles)")
    bits = 4 if args.colours <= 16 else 8
    colours = min(args.colours, 16) if bits == 4 else args.colours

    ffmpeg = find_ffmpeg()
    duration = args.duration if args.duration > 0 else probe_duration(ffmpeg, args.video) - args.start
    print("video: %s, %.2f s from %.2f s at %d fps, fit=%s" %
          (args.video, duration, args.start, args.fps, args.fit))

    verts, tris = build_mesh(args.cols, args.rows, args.jitter, args.seed)
    labels = label_map(verts, tris)
    print("mesh: %d vertices, %d triangles" % (len(verts), len(tris)))

    per_frame = list(stream_triangle_colours(ffmpeg, args.video, args.start, duration,
                                             args.fps, args.fit, labels, len(tris)))
    if not per_frame:
        sys.exit("no frames decoded; check --start/--duration")
    if len(per_frame) > 65535:
        sys.exit("too many frames for the u16 counter; shorten --duration or lower --fps")
    colour_array = np.stack(per_frame)  # (frames, triangles, 3)
    print("sampled %d frames" % colour_array.shape[0])

    graded = grade(colour_array)
    palette, indices = quantise(graded, colours)
    frame_bytes = write_blob(args.out, verts, tris, palette, indices, bits, args.fps)
    total = 4 + 1 + 1 + 1 + 1 + 2 + 2 + 2 + 4 * len(verts) + 6 * len(tris) + len(palette) \
        + indices.shape[0] * frame_bytes
    print("wrote %s: %d frames, %d triangles, %d bytes per frame, %d colours (%d-bit), %d bytes" %
          (args.out, indices.shape[0], len(tris), frame_bytes, len(palette) // 3, bits, total))
    if args.preview:
        write_preview(args.preview, verts, tris, palette, indices, 6)
        print("preview: %s" % args.preview)


if __name__ == "__main__":
    main()
