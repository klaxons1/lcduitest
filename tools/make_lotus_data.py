#!/usr/bin/env python3
"""
Turns a video into the adaptive triangle film used by the J2ME "Lotus" demo (res/lotus.bin).

Every frame is modelled with its own triangles. A quadtree is laid over the screen: a cell is
split while it still holds detail (colour variance above --detail) or is larger than --max-cell,
down to --min-cell. So flat areas get a few big triangles, while faces, edges and textures get
many small ones. Each leaf cell is cut into two triangles along the diagonal that fits the
frame best. Every triangle is one flat colour: the mean of the video pixels under it.

The cells tile the screen exactly and share their borders, so the triangles have no cracks.

Pipeline:
  1. Pass 1: ffmpeg decodes the chosen time range (cropped to the screen aspect by default,
     or stretched), and a global exposure is estimated from the luminance of all frames.
  2. Pass 2: each frame is graded, the quadtree and the diagonals are chosen, and the triangle
     colours are measured.
  3. The colours are quantised to one palette (--colours) and written as format "LOT4".

Format "LOT4" (big-endian):
  4 bytes  magic "LOT4"
  u8       version (4)
  u8       fps
  u16      frame count
  u16      source width, u16 source height (the coordinate space of the tree, 240 x 320)
  u8       minimum cell size in source pixels
  u16      max leaves in any frame (for buffer sizing)
  u8       palette size
  palette  size * (R, G, B)
  frames   frame count * [ u16 payload length, payload ]
  payload  u16 bit bytes, bit bytes, then 2 colour bytes (palette index) per leaf, leaf order

Tree bits (preorder, MSB first): at a node, if the node can split (both sides >= 2*min-cell)
read 1 bit: 1 = split into TL, TR, BL, BR (children split at the integer midpoint), 0 = leaf.
A leaf reads 1 more bit: its diagonal, 0 = TL-BR, 1 = TR-BL. The leaf's two triangles take
colour bytes 2k and 2k+1, where k is the leaf index.

Only the derived binary is committed; decoded frames stay outside the repo.

Requirements: ffmpeg (or imageio-ffmpeg, which bundles one), numpy, Pillow.

Usage:
  python3 tools/make_lotus_data.py --video "docs/Phantasm Mineshaft Chase scene.mp4" \
      --out res/lotus.bin --fps 5 --detail 12 --min-cell 6 --max-cell 96 --colours 64
"""

import argparse
import shutil
import struct
import subprocess
import sys

import numpy as np
from PIL import Image, ImageDraw

MAGIC = b"LOT4"
VERSION = 4
SRC_W = 240
SRC_H = 320


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


def frame_stream(ffmpeg, video, start, duration, fps, fit):
    """Yields source frames as float32 arrays (SRC_H, SRC_W, 3) in 0..255, one at a time."""
    if fit == "crop":
        vf = ("crop=trunc(ih*%d/%d/2)*2:ih:trunc((iw-ih*%d/%d)/4)*2:0,"
              "fps=%.6f,scale=%d:%d:flags=area") % (SRC_W, SRC_H, SRC_W, SRC_H,
                                                    fps, SRC_W, SRC_H)
    else:
        vf = "fps=%.6f,scale=%d:%d:flags=area" % (fps, SRC_W, SRC_H)
    cmd = [ffmpeg, "-hide_banner", "-loglevel", "error"]
    if start > 0:
        cmd += ["-ss", "%.3f" % start]
    cmd += ["-i", video]
    if duration > 0:
        cmd += ["-t", "%.3f" % duration]
    cmd += ["-vf", vf, "-f", "rawvideo", "-pix_fmt", "rgb24", "-"]

    frame_bytes = SRC_W * SRC_H * 3
    proc = subprocess.Popen(cmd, stdout=subprocess.PIPE)
    try:
        while True:
            raw = proc.stdout.read(frame_bytes)
            if len(raw) < frame_bytes:
                break
            yield np.frombuffer(raw, dtype=np.uint8).reshape(SRC_H, SRC_W, 3).astype(np.float32)
    finally:
        proc.stdout.close()
        proc.wait()


def global_peak(ffmpeg, args, duration):
    """97th percentile of luminance over all frames (subsampled), for a stable exposure."""
    samples = []
    for img in frame_stream(ffmpeg, args.video, args.start, duration, args.fps, args.fit):
        lum = img.mean(axis=2)
        samples.append(lum[::4, ::4].ravel())
    if not samples:
        sys.exit("no frames decoded; check --start/--duration")
    return max(float(np.percentile(np.concatenate(samples), 97.0)), 1.0)


def grade(img, peak):
    """Lifts dark footage with a shared exposure, gamma and a little saturation."""
    x = np.clip(img / peak, 0.0, 1.0) ** 0.45
    gray = x.mean(axis=2, keepdims=True)
    x = gray + (x - gray) * 1.35
    return np.clip(x, 0.0, 1.0) * 255.0


class FrameModel:
    """Quadtree + diagonal choice for one frame. Produces the bit list and leaf colours."""

    def __init__(self, img, detail, min_cell, max_cell):
        self.img = img
        self.detail = detail
        self.min_cell = min_cell
        self.max_cell = max_cell
        self.bits = []          # tree bits in traversal order
        self.leaves = []        # (x0, y0, x1, y1, diag, colour_a, colour_b) per leaf

    def can_split(self, w, h):
        return w >= 2 * self.min_cell and h >= 2 * self.min_cell

    def run(self):
        self.visit(0, 0, SRC_W, SRC_H)
        return self.bits, self.leaves

    def visit(self, x0, y0, x1, y1):
        w = x1 - x0
        h = y1 - y0
        if self.can_split(w, h):
            region = self.img[y0:y1, x0:x1]
            spread = region.std(axis=(0, 1)).mean()
            split = spread > self.detail or max(w, h) > self.max_cell
            self.bits.append(1 if split else 0)
            if split:
                mx = x0 + w // 2
                my = y0 + h // 2
                self.visit(x0, y0, mx, my)
                self.visit(mx, y0, x1, my)
                self.visit(x0, my, mx, y1)
                self.visit(mx, my, x1, y1)
                return
        self.leaf(x0, y0, x1, y1)

    def leaf(self, x0, y0, x1, y1):
        w = x1 - x0
        h = y1 - y0
        region = self.img[y0:y1, x0:x1]
        # Normalised pixel-centre coordinates inside the leaf.
        jj, ii = np.meshgrid(np.arange(w), np.arange(h))
        u = (jj + 0.5) / w
        v = (ii + 0.5) / h
        masks0 = (u >= v, u < v)                  # diagonal TL-BR
        masks1 = (u + v < 1.0, u + v >= 1.0)      # diagonal TR-BL
        best = None
        for diag, masks in ((0, masks0), (1, masks1)):
            err = 0.0
            cols = []
            for m in masks:
                px = region[m]
                if px.size == 0:
                    cols.append((0.0, 0.0, 0.0))
                    continue
                mean = px.mean(axis=0)
                err += float(((px - mean) ** 2).sum())
                cols.append(tuple(mean))
            if best is None or err < best[0]:
                best = (err, diag, cols)
        _, diag, cols = best
        self.bits.append(diag)
        self.leaves.append((x0, y0, x1, y1, diag, cols[0], cols[1]))


def pack_bits(bits):
    out = bytearray((len(bits) + 7) // 8)
    for i, b in enumerate(bits):
        if b:
            out[i >> 3] |= 0x80 >> (i & 7)
    return bytes(out)


def main():
    ap = argparse.ArgumentParser(description=__doc__,
                                 formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--video", required=True)
    ap.add_argument("--out", default="res/lotus.bin")
    ap.add_argument("--start", type=float, default=0.0, help="start time in seconds")
    ap.add_argument("--duration", type=float, default=0.0,
                    help="length in seconds (0 = until the end of the video)")
    ap.add_argument("--fps", type=int, default=5, help="playback frames per second (1..30)")
    ap.add_argument("--detail", type=float, default=12.0,
                    help="split a cell while its colour spread is above this (0..255)")
    ap.add_argument("--min-cell", type=int, default=6, help="smallest leaf size in pixels (>=2)")
    ap.add_argument("--max-cell", type=int, default=96, help="largest leaf size in pixels")
    ap.add_argument("--colours", type=int, default=64, help="palette size, 2..255")
    ap.add_argument("--fit", choices=["crop", "stretch"], default="crop",
                    help="crop: keep aspect, fill the screen; stretch: squeeze the whole frame")
    ap.add_argument("--preview", help="optional PNG contact sheet of the result")
    args = ap.parse_args()

    if not 1 <= args.fps <= 30:
        sys.exit("--fps must be 1..30")
    if not 2 <= args.colours <= 255:
        sys.exit("--colours must be 2..255")
    if args.min_cell < 2 or args.min_cell > 60:
        sys.exit("--min-cell must be 2..60")
    if args.max_cell < args.min_cell:
        sys.exit("--max-cell must be >= --min-cell")

    ffmpeg = find_ffmpeg()
    duration = args.duration if args.duration > 0 else probe_duration(ffmpeg, args.video) - args.start
    print("video: %s, %.2f s from %.2f s at %d fps, fit=%s" %
          (args.video, duration, args.start, args.fps, args.fit))

    peak = global_peak(ffmpeg, args, duration)
    print("exposure peak (97th percentile luminance): %.1f" % peak)

    frames = []                  # per frame: (bits, leaves)
    for img in frame_stream(ffmpeg, args.video, args.start, duration, args.fps, args.fit):
        graded = grade(img, peak)
        frames.append(FrameModel(graded, args.detail, args.min_cell, args.max_cell).run())
    if not frames:
        sys.exit("no frames decoded; check --start/--duration")
    if len(frames) > 65535:
        sys.exit("too many frames for the u16 counter; shorten --duration or lower --fps")

    leaf_counts = [len(leaves) for _, leaves in frames]
    max_leaves = max(leaf_counts)
    if max_leaves * 2 > 65535 - 16:
        sys.exit("a frame has too many leaves for the u16 payload length; raise --detail")
    print("frames: %d, leaves per frame: avg %.0f, max %d (%d triangles max)" %
          (len(frames), sum(leaf_counts) / float(len(frames)), max_leaves, 2 * max_leaves))

    # One shared palette, quantised over every triangle colour in the film.
    all_cols = []
    for _, leaves in frames:
        for leaf in leaves:
            all_cols.append(leaf[5])
            all_cols.append(leaf[6])
    colour_arr = np.clip(np.array(all_cols, dtype=np.float64) + 0.5, 0, 255).astype(np.uint8)
    tall = Image.fromarray(colour_arr.reshape(-1, 1, 3), "RGB")
    pal_img = tall.quantize(colors=args.colours, method=Image.MEDIANCUT, dither=Image.NONE)
    palette = pal_img.getpalette()[: args.colours * 3]
    indices = np.array(pal_img, dtype=np.uint8).reshape(-1)

    payloads = []
    k = 0
    for bits, leaves in frames:
        colour_bytes = bytearray()
        for _ in leaves:
            colour_bytes.append(int(indices[k]))
            colour_bytes.append(int(indices[k + 1]))
            k += 2
        bit_bytes = pack_bits(bits)
        payload = struct.pack(">H", len(bit_bytes)) + bit_bytes + bytes(colour_bytes)
        payloads.append(payload)

    with open(args.out, "wb") as f:
        f.write(MAGIC)
        f.write(struct.pack(">BBHHHBHB", VERSION, args.fps, len(payloads),
                            SRC_W, SRC_H, args.min_cell, max_leaves, len(palette) // 3))
        f.write(bytes(palette))
        for payload in payloads:
            f.write(struct.pack(">H", len(payload)))
            f.write(payload)

    total = 4 + 12 + len(palette) + sum(2 + len(p) for p in payloads)
    print("wrote %s: %d bytes (%d colours), avg %.0f bytes per frame" %
          (args.out, total, len(palette) // 3, total / float(len(payloads))))

    if args.preview:
        write_preview(args.preview, frames, palette, indices)
        print("preview: %s" % args.preview)


def triangles_of(leaf):
    x0, y0, x1, y1, diag = leaf[0], leaf[1], leaf[2], leaf[3], leaf[4]
    if diag == 0:
        return ((x0, y0), (x1, y0), (x1, y1)), ((x0, y0), (x1, y1), (x0, y1))
    return ((x0, y0), (x1, y0), (x0, y1)), ((x1, y0), (x1, y1), (x0, y1))


def write_preview(path, frames, palette, indices):
    """Renders a few frames exactly as the device will (flat triangles on black)."""
    pal = np.array(palette, dtype=np.uint8).reshape(-1, 3)
    n = len(frames)
    step = max(1, n // 6)
    picks = list(range(0, n, step))[:6]
    cell_w, cell_h = SRC_W // 2, SRC_H // 2
    sheet = Image.new("RGB", (len(picks) * (cell_w + 4), cell_h), (30, 30, 30))
    offsets = []
    k = 0
    for _, leaves in frames:
        offsets.append(k)
        k += 2 * len(leaves)
    for i, f in enumerate(picks):
        frame = Image.new("RGB", (SRC_W, SRC_H), (0, 0, 0))
        draw = ImageDraw.Draw(frame)
        base = offsets[f]
        for j, leaf in enumerate(frames[f][1]):
            ca = tuple(int(v) for v in pal[indices[base + 2 * j]])
            cb = tuple(int(v) for v in pal[indices[base + 2 * j + 1]])
            t1, t2 = triangles_of(leaf)
            draw.polygon(t1, fill=ca)
            draw.polygon(t2, fill=cb)
        sheet.paste(frame.resize((cell_w, cell_h), Image.BILINEAR), (i * (cell_w + 4), 0))
    sheet.save(path)


if __name__ == "__main__":
    main()
