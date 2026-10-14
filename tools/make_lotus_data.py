#!/usr/bin/env python3
"""
Turns a video into the film used by the J2ME "Lotus" demo (res/lotus.bin, format LOT6).

Each frame has two layers:

  * Background: the whole frame as a picture (a PNG, 240x320, palette of --bg-colours). It is
    not split into polygons. It changes every frame, so the backdrop plays as video.
  * People: polygons. An adaptive quadtree restricted to the people regions, which are marked by
    hand in tools/lotus/people_keyframes.json (ellipses on keyframes every 3 seconds). Between
    keyframes the ellipses are interpolated (or the nearest keyframe is held when the number of
    people changes). Each leaf is cut into two flat-coloured triangles along the diagonal that
    fits the picture best. Cells split while they straddle the people outline, while they hold
    detail, or while they are larger than --max-cell, down to --min-cell.

The people polygons are drawn over the background picture. Every cell that touches a people
region is emitted, so the polygons cover the regions completely.

Pipeline:
  1. Pass 1: ffmpeg decodes the chosen range (cropped to the screen aspect by default, or
     stretched); a global exposure is estimated from the luminance of all frames.
  2. Pass 2: each frame is graded; people ellipses are evaluated for its time; the background
     picture and the people tree are computed.
  3. The people colours are quantised to one palette (--colours) and everything is written as
     format "LOT6".

Format "LOT6" (big-endian):
  4 bytes  magic "LOT6"
  u8       version (6)
  u8       fps
  u16      frame count
  u16      source width, u16 source height (the coordinate space, 240 x 320)
  u8       minimum leaf size in source pixels
  u16      max people leaves in any frame (buffer sizing)
  u8       palette size
  u16      largest frame payload in bytes (buffer sizing)
  palette  size * (R, G, B)
  frames   frame count * [ u16 payload length, payload ]
  payload  u16 background PNG length, background PNG bytes (a complete PNG file),
           u16 tree bit bytes, tree bits, then two colour bytes per people leaf (leaf order)

Tree bits (preorder, MSB first). A node reads 1 "present" bit: 0 = no people in this node (no
leaf emitted), 1 = present. If present and the node can split (both sides >= 2*min-cell), it
reads 1 "split" bit: 1 = split into TL, TR, BL, BR at the integer midpoint. A leaf reads 1
"diagonal" bit: 0 = TL-BR, 1 = TR-BL. A leaf's two triangles take colour bytes 2k and 2k+1,
where k is the leaf index.

Only the derived binary is committed; decoded frames stay outside the repo.

Requirements: ffmpeg (or imageio-ffmpeg, which bundles one), numpy, Pillow.

Usage:
  python3 tools/make_lotus_data.py --video "docs/Phantasm Mineshaft Chase scene.mp4" \
      --out res/lotus.bin --fps 5 --people tools/lotus/people_keyframes.json
"""

import argparse
import io
import json
import math
import shutil
import struct
import subprocess
import sys

import numpy as np
from PIL import Image, ImageDraw

MAGIC = b"LOT6"
VERSION = 6
SRC_W = 240
SRC_H = 320
KEY_STEP = 3.0  # seconds between people keyframes


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
        samples.append(img.mean(axis=2)[::4, ::4].ravel())
    if not samples:
        sys.exit("no frames decoded; check --start/--duration")
    return max(float(np.percentile(np.concatenate(samples), 97.0)), 1.0)


def grade(img, peak):
    """Lifts dark footage with a shared exposure, gamma and a little saturation."""
    x = np.clip(img / peak, 0.0, 1.0) ** 0.45
    gray = x.mean(axis=2, keepdims=True)
    x = gray + (x - gray) * 1.35
    return np.clip(x, 0.0, 1.0) * 255.0


class People:
    """Ellipse markup on keyframes, interpolated to any time."""

    def __init__(self, path):
        with open(path, "r", encoding="utf-8") as f:
            data = json.load(f)
        self.keys = {int(k): [list(map(float, e)) for e in v]
                     for k, v in data["keyframes"].items()}
        if not self.keys:
            sys.exit("people markup has no keyframes")

    def at(self, t):
        k = t / KEY_STEP
        a = int(math.floor(k))
        alpha = k - a
        ka = self.keys.get(a)
        kb = self.keys.get(a + 1)
        if ka is not None and kb is not None:
            if len(ka) == 0 and len(kb) == 0:
                return []
            if len(ka) == len(kb):
                return [[p + (q - p) * alpha for p, q in zip(ea, eb)]
                        for ea, eb in zip(ka, kb)]
            return ka if alpha < 0.5 else kb
        # Outside the markup or next to a missing keyframe: hold the nearest one that exists.
        nearest = min(self.keys, key=lambda key: abs(key - k))
        return self.keys[nearest]


def point_in(px, py, ells):
    for cx, cy, rx, ry in ells:
        if ((px - cx) / rx) ** 2 + ((py - cy) / ry) ** 2 <= 1.0:
            return True
    return False


def rect_hits(x0, y0, x1, y1, ells):
    """True if the rectangle overlaps any ellipse (closest-point test)."""
    for cx, cy, rx, ry in ells:
        px = min(max(cx, x0), x1)
        py = min(max(cy, y0), y1)
        if ((px - cx) / rx) ** 2 + ((py - cy) / ry) ** 2 <= 1.0:
            return True
    return False


def rect_inside(x0, y0, x1, y1, ells):
    """True if all four corners lie inside the union of ellipses (approximation)."""
    for px, py in ((x0, y0), (x1, y0), (x0, y1), (x1, y1)):
        if not point_in(px, py, ells):
            return False
    return True


def people_mask(ells):
    """Boolean mask (SRC_H, SRC_W) of the people regions."""
    mask = np.zeros((SRC_H, SRC_W), dtype=bool)
    if not ells:
        return mask
    yy, xx = np.mgrid[0:SRC_H, 0:SRC_W].astype(np.float64)
    for cx, cy, rx, ry in ells:
        mask |= ((xx + 0.5 - cx) / rx) ** 2 + ((yy + 0.5 - cy) / ry) ** 2 <= 1.0
    return mask


class FrameModel:
    """Quadtree for the people layer of one frame: tree bits and leaves."""

    def __init__(self, img, ells, detail, min_cell, max_cell):
        self.img = img
        self.ells = ells
        self.detail = detail
        self.min_cell = min_cell
        self.max_cell = max_cell
        self.bits = []
        self.leaves = []  # (x0, y0, x1, y1, diag, colour_a, colour_b)

    def run(self):
        self.visit(0, 0, SRC_W, SRC_H)
        return self.bits, self.leaves

    def can_split(self, w, h):
        return w >= 2 * self.min_cell and h >= 2 * self.min_cell

    def visit(self, x0, y0, x1, y1):
        if not rect_hits(x0, y0, x1, y1, self.ells):
            self.bits.append(0)
            return
        self.bits.append(1)
        w = x1 - x0
        h = y1 - y0
        if self.can_split(w, h):
            region = self.img[y0:y1, x0:x1]
            spread = region.std(axis=(0, 1)).mean()
            straddles = not rect_inside(x0, y0, x1, y1, self.ells)
            split = straddles or spread > self.detail or max(w, h) > self.max_cell
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
        jj, ii = np.meshgrid(np.arange(w), np.arange(h))
        u = (jj + 0.5) / w
        v = (ii + 0.5) / h
        masks0 = (u >= v, u < v)              # diagonal TL-BR
        masks1 = (u + v < 1.0, u + v >= 1.0)  # diagonal TR-BL
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


def triangles_of(leaf):
    x0, y0, x1, y1, diag = leaf[0], leaf[1], leaf[2], leaf[3], leaf[4]
    if diag == 0:
        return ((x0, y0), (x1, y0), (x1, y1)), ((x0, y0), (x1, y1), (x0, y1))
    return ((x0, y0), (x1, y0), (x0, y1)), ((x1, y0), (x1, y1), (x0, y1))


def background_png(graded, mask, colours):
    """The frame as a palette PNG. Pixels under the people regions are set to the mean of the
    rest of the frame: the polygons cover them anyway, and flat areas compress much better."""
    bg = graded.copy()
    if mask.any():
        rest = ~mask
        if rest.any():
            bg[mask] = graded[rest].mean(axis=0)
    img = Image.fromarray(np.clip(bg + 0.5, 0, 255).astype(np.uint8))
    pal = img.quantize(colors=colours, method=Image.MEDIANCUT, dither=Image.NONE)
    buf = io.BytesIO()
    pal.save(buf, "PNG", optimize=True)
    return buf.getvalue()


def main():
    ap = argparse.ArgumentParser(description=__doc__,
                                 formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--video", required=True)
    ap.add_argument("--out", default="res/lotus.bin")
    ap.add_argument("--people", default="tools/lotus/people_keyframes.json",
                    help="people markup (ellipses on keyframes every 3 s)")
    ap.add_argument("--start", type=float, default=0.0, help="start time in seconds")
    ap.add_argument("--duration", type=float, default=0.0,
                    help="length in seconds (0 = until the end of the video)")
    ap.add_argument("--fps", type=int, default=5, help="playback frames per second (1..30)")
    ap.add_argument("--detail", type=float, default=20.0,
                    help="split a people cell while its colour spread is above this")
    ap.add_argument("--min-cell", type=int, default=8, help="smallest people leaf in pixels (>=2)")
    ap.add_argument("--max-cell", type=int, default=96, help="largest people leaf in pixels")
    ap.add_argument("--colours", type=int, default=64, help="people palette size, 2..255")
    ap.add_argument("--bg-colours", type=int, default=64, help="background PNG palette, 2..256")
    ap.add_argument("--fit", choices=["crop", "stretch"], default="crop")
    ap.add_argument("--preview", help="optional PNG contact sheet of the result")
    args = ap.parse_args()

    if not 1 <= args.fps <= 30:
        sys.exit("--fps must be 1..30")
    if not 2 <= args.colours <= 255:
        sys.exit("--colours must be 2..255")
    if not 2 <= args.bg_colours <= 256:
        sys.exit("--bg-colours must be 2..256")
    if args.min_cell < 2 or args.min_cell > 60:
        sys.exit("--min-cell must be 2..60")
    if args.max_cell < args.min_cell:
        sys.exit("--max-cell must be >= --min-cell")

    people = People(args.people)
    ffmpeg = find_ffmpeg()
    duration = args.duration if args.duration > 0 else probe_duration(ffmpeg, args.video) - args.start
    print("video: %s, %.2f s from %.2f s at %d fps, fit=%s" %
          (args.video, duration, args.start, args.fps, args.fit))

    peak = global_peak(ffmpeg, args, duration)
    print("exposure peak (97th percentile luminance): %.1f" % peak)

    frames = []  # per frame: (png, bits, leaves)
    people_seen = 0
    for index, img in enumerate(frame_stream(ffmpeg, args.video, args.start, duration,
                                             args.fps, args.fit)):
        t = index / float(args.fps)
        ells = people.at(t)
        if ells:
            people_seen += 1
        graded = grade(img, peak)
        mask = people_mask(ells)
        png = background_png(graded, mask, args.bg_colours)
        bits, leaves = FrameModel(graded, ells, args.detail, args.min_cell, args.max_cell).run()
        frames.append((png, bits, leaves))
    if not frames:
        sys.exit("no frames decoded; check --start/--duration")
    if len(frames) > 65535:
        sys.exit("too many frames for the u16 counter; shorten --duration or lower --fps")

    leaf_counts = [len(leaves) for _, _, leaves in frames]
    max_leaves = max(leaf_counts)
    png_sizes = [len(p) for p, _, _ in frames]
    print("frames: %d (%d with people), people leaves per frame: avg %.0f, max %d" %
          (len(frames), people_seen, sum(leaf_counts) / float(len(frames)), max_leaves))
    print("background PNG: avg %.0f bytes, max %d bytes" %
          (sum(png_sizes) / float(len(png_sizes)), max(png_sizes)))

    # Palette over the people leaf colours only.
    all_cols = []
    for _, _, leaves in frames:
        for leaf in leaves:
            all_cols.append(leaf[5])
            all_cols.append(leaf[6])
    if all_cols:
        colour_arr = np.clip(np.array(all_cols, dtype=np.float64) + 0.5, 0, 255).astype(np.uint8)
        tall = Image.fromarray(colour_arr.reshape(-1, 1, 3), "RGB")
        pal_img = tall.quantize(colors=args.colours, method=Image.MEDIANCUT, dither=Image.NONE)
        palette = pal_img.getpalette()[: args.colours * 3]
        indices = np.array(pal_img, dtype=np.uint8).reshape(-1)
    else:
        palette = [0, 0, 0]
        indices = np.zeros(0, dtype=np.uint8)

    payloads = []
    k = 0
    for png, bits, leaves in frames:
        colour_bytes = bytearray()
        for _ in leaves:
            colour_bytes.append(int(indices[k]))
            colour_bytes.append(int(indices[k + 1]))
            k += 2
        bit_bytes = pack_bits(bits)
        if len(png) > 65535 or len(bit_bytes) > 65535:
            sys.exit("a frame part is too large for its u16 length")
        payloads.append(struct.pack(">H", len(png)) + png
                        + struct.pack(">H", len(bit_bytes)) + bit_bytes + bytes(colour_bytes))
    max_payload = max(len(p) for p in payloads)
    if max_payload > 65535:
        sys.exit("a frame payload is too large for the u16 length")

    with open(args.out, "wb") as f:
        f.write(MAGIC)
        f.write(struct.pack(">BBHHHBHBH", VERSION, args.fps, len(payloads),
                            SRC_W, SRC_H, args.min_cell, max_leaves, len(palette) // 3,
                            max_payload))
        f.write(bytes(palette))
        for payload in payloads:
            f.write(struct.pack(">H", len(payload)))
            f.write(payload)

    total = 4 + 14 + len(palette) + sum(2 + len(p) for p in payloads)
    print("wrote %s: %d bytes (%d people colours), avg %.0f bytes per frame" %
          (args.out, total, len(palette) // 3, total / float(len(payloads))))

    if args.preview:
        write_preview(args.preview, frames, palette, indices)
        print("preview: %s" % args.preview)


def write_preview(path, frames, palette, indices):
    """Renders a few frames the way the device draws them (background picture, then people)."""
    pal = np.array(palette, dtype=np.uint8).reshape(-1, 3) if len(palette) else None
    n = len(frames)
    step = max(1, n // 6)
    picks = list(range(0, n, step))[:6]
    offsets = []
    k = 0
    for _, _, leaves in frames:
        offsets.append(k)
        k += 2 * len(leaves)
    cell_w, cell_h = SRC_W, SRC_H
    sheet = Image.new("RGB", (len(picks) * (cell_w + 4), cell_h), (30, 30, 30))
    for i, f in enumerate(picks):
        png, _, leaves = frames[f]
        base = offsets[f]
        frame = Image.open(io.BytesIO(png)).convert("RGB")
        draw = ImageDraw.Draw(frame)
        for j, leaf in enumerate(leaves):
            ca = tuple(int(v) for v in pal[indices[base + 2 * j]])
            cb = tuple(int(v) for v in pal[indices[base + 2 * j + 1]])
            t1, t2 = triangles_of(leaf)
            draw.polygon(t1, fill=ca)
            draw.polygon(t2, fill=cb)
        sheet.paste(frame, (i * (cell_w + 4), 0))
    sheet.save(path)


if __name__ == "__main__":
    main()
