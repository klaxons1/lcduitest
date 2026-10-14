#!/usr/bin/env python3
"""
Turns a video into the triangle film used by the J2ME "Lotus" demo (res/lotus.bin, format LOT7).

Each frame has two layers, both made of flat-coloured triangles:

  * Environment: a uniform grid of small cells (--env-cols x --env-rows over the 240x320 screen).
    Every cell is cut into two triangles along its TL-BR diagonal, and each triangle is one
    colour: the mean of the video pixels under it (people pixels excluded when possible). The
    colours come from a shared palette of 16 entries (4 bits per triangle, as in the quad film).
    A triangle keeps its previous colour index while that colour stays within --hysteresis of the
    best one, which means less flicker and a more compressible film.
  * People: an adaptive quadtree restricted to the people regions, which are marked by hand in
    tools/lotus/people_keyframes.json (ellipses on keyframes every 3 seconds). Between keyframes
    the ellipses are interpolated (or the nearest keyframe is held when the number of people
    changes). Each leaf is cut into two flat-coloured triangles along the diagonal that fits the
    picture best. Cells split while they straddle the people outline, while they hold detail, or
    while they are larger than --max-cell, down to --min-cell. The people colours use their own
    palette of --colours entries.

The people triangles are drawn over the environment. Every cell that touches a people region is
emitted, so the people triangles cover the regions completely.

Pipeline:
  1. Pass 1: ffmpeg decodes the chosen range (cropped to the screen aspect by default, or
     stretched); a global exposure is estimated from the luminance of all frames.
  2. Pass 2: each frame is graded; people ellipses are evaluated for its time; the environment
     colours and the people tree are computed.
  3. Colours are quantised to the two palettes and everything is written as format "LOT7".

Format "LOT7" (big-endian):
  4 bytes  magic "LOT7"
  u8       version (7)
  u8       fps
  u16      frame count
  u16      source width, u16 source height (the coordinate space, 240 x 320)
  u8       minimum leaf size in source pixels
  u16      max people leaves in any frame (buffer sizing)
  u8       environment grid columns, u8 environment grid rows
  u8       environment palette size (<= 16)
  u8       people palette size
  u16      largest frame payload in bytes (buffer sizing)
  env palette     size * (R, G, B)
  people palette  size * (R, G, B)
  frames   frame count * [ u16 payload length, payload ]
  payload  u16 tree bit bytes, tree bits, then one byte per environment cell (high nibble =
           colour of the cell's upper-right triangle, low nibble = lower-left triangle), then two
           people colour bytes per people leaf (leaf order)

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
import json
import math
import shutil
import struct
import subprocess
import sys

import numpy as np
from PIL import Image, ImageDraw

MAGIC = b"LOT7"
VERSION = 7
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


def environment_colours(img, people_px, cols, rows):
    """Mean colour of each environment triangle, shape (rows*cols*2, 3). Triangle 0 of a cell is
    the upper-right half (TL-BR diagonal), triangle 1 the lower-left half. Pixels under people
    are left out when the triangle has enough other pixels."""
    cw = SRC_W / float(cols)
    ch = SRC_H / float(rows)
    xs = np.arange(SRC_W) + 0.5
    ys = np.arange(SRC_H) + 0.5
    cx = np.minimum((xs / cw).astype(np.int64), cols - 1)
    cy = np.minimum((ys / ch).astype(np.int64), rows - 1)
    u = (xs[None, :] - cx[None, :] * cw) / cw
    v = (ys[:, None] - cy[:, None] * ch) / ch
    tri = (u < v).astype(np.int64)  # 1 = lower-left triangle
    index = ((cy[:, None] * cols + cx[None, :]) * 2 + tri).ravel()
    n = rows * cols * 2

    flat = img.reshape(-1, 3)
    outside = (~people_px).ravel()
    cnt_all = np.bincount(index, minlength=n).astype(np.float64)
    cnt_out = np.bincount(index[outside], minlength=n).astype(np.float64)
    out = np.zeros((n, 3), dtype=np.float64)
    use_out = cnt_out >= 4
    for ch_i in range(3):
        sum_all = np.bincount(index, weights=flat[:, ch_i], minlength=n)
        sum_out = np.bincount(index[outside], weights=flat[outside, ch_i], minlength=n)
        out[:, ch_i] = np.where(use_out, sum_out / np.maximum(cnt_out, 1.0),
                                sum_all / np.maximum(cnt_all, 1.0))
    return out


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


def kmeans(points, k, iters=25, seed=1, weights=None, init=None):
    """Lloyd k-means (squared Euclidean). points: (N, 3) float; weights: (N,) or None.

    init: (k, 3) starting centres (warm start), or None for k-means++ seeding. An empty cluster
    keeps its previous centre. Returns (k, 3) float centres.
    """
    rng = np.random.default_rng(seed)
    pts = np.asarray(points, dtype=np.float64)
    w = np.ones(len(pts)) if weights is None else np.asarray(weights, dtype=np.float64)
    if init is not None:
        cent = np.array(init, dtype=np.float64)
    else:
        cent = np.empty((k, 3))
        cent[0] = pts[rng.choice(len(pts), p=w / w.sum())]
        d2 = ((pts - cent[0]) ** 2).sum(1)
        for j in range(1, k):
            p = w * d2
            total = p.sum()
            idx = rng.choice(len(pts), p=p / total) if total > 0 else rng.integers(len(pts))
            cent[j] = pts[idx]
            d2 = np.minimum(d2, ((pts - cent[j]) ** 2).sum(1))
    for _ in range(iters):
        lab = nearest(pts, cent)
        wsum = np.bincount(lab, weights=w, minlength=k)
        new = cent.copy()
        for c in range(3):
            total = np.bincount(lab, weights=w * pts[:, c], minlength=k)
            ok = wsum > 0
            new[ok, c] = total[ok] / wsum[ok]
        moved = np.abs(new - cent).max()
        cent = new
        if moved < 1e-3:
            break
    return cent


def nearest(points, cent, chunk=100000):
    """Index of the nearest centre for each point (squared Euclidean distance)."""
    out = np.empty(len(points), dtype=np.int64)
    c2 = (cent ** 2).sum(1)
    for s in range(0, len(points), chunk):
        p = points[s:s + chunk]
        d = (p ** 2).sum(1)[:, None] - 2.0 * (p @ cent.T) + c2[None, :]
        out[s:s + chunk] = d.argmin(1)
    return out


def distances(points, cent):
    """Euclidean distance from every point to every centre: (N, k)."""
    diff = points[:, None, :] - cent[None, :, :]
    return np.sqrt((diff * diff).sum(2))


def quantise(colours, count, method="kmeans", weights=None):
    """Palette (count * 3 bytes) and indices for an (N, 3) array of colours.

    "kmeans": k-means (optionally weighted), entries rounded to bytes, each colour assigned to its
    nearest entry. "mediancut": Pillow's median cut, unweighted, as before.
    """
    arr = np.clip(np.asarray(colours, dtype=np.float64) + 0.5, 0, 255)
    if method == "mediancut":
        tall = Image.fromarray(arr.astype(np.uint8).reshape(-1, 1, 3), "RGB")
        pal_img = tall.quantize(colors=count, method=Image.MEDIANCUT, dither=Image.NONE)
        palette = pal_img.getpalette()[: count * 3]
        indices = np.array(pal_img, dtype=np.uint8).reshape(-1)
        return palette, indices
    cent = np.clip(np.round(kmeans(arr, count, weights=weights)), 0, 255)
    indices = nearest(arr, cent).astype(np.uint8)
    palette = cent.astype(np.uint8).reshape(-1).tolist()
    return palette, indices


def stable_indices(colours, palette, frame_count, tri_count, hysteresis):
    """Palette index of every triangle of every frame (frame-major, like `colours`).

    Each triangle takes its nearest palette entry, except that it keeps its previous frame's index
    while that entry is within `hysteresis` (RGB distance) of the best one. Less flicker, and the
    index stream repeats more, so the jar compresses better.
    """
    pal = np.array(palette, dtype=np.float64).reshape(-1, 3)
    arr = np.clip(np.asarray(colours, dtype=np.float64) + 0.5, 0, 255)
    out = np.empty(len(arr), dtype=np.uint8)
    rows_ = np.arange(tri_count)
    prev = None
    for f in range(frame_count):
        chunk = arr[f * tri_count:(f + 1) * tri_count]
        dist = distances(chunk, pal)
        best = dist.argmin(1)
        if prev is None or hysteresis <= 0:
            idx = best
        else:
            keep = dist[rows_, prev] <= dist.min(1) + hysteresis
            idx = np.where(keep, prev, best)
        out[f * tri_count:(f + 1) * tri_count] = idx
        prev = idx
    return out


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
    ap.add_argument("--env-cols", type=int, default=30, help="environment grid columns (1..120)")
    ap.add_argument("--env-rows", type=int, default=40, help="environment grid rows (1..160)")
    ap.add_argument("--env-colours", type=int, default=16, help="environment palette, 2..16")
    ap.add_argument("--detail", type=float, default=20.0,
                    help="split a people cell while its colour spread is above this")
    ap.add_argument("--min-cell", type=int, default=8, help="smallest people leaf in pixels (>=2)")
    ap.add_argument("--max-cell", type=int, default=96, help="largest people leaf in pixels")
    ap.add_argument("--colours", type=int, default=64, help="people palette size, 2..255")
    ap.add_argument("--hysteresis", type=float, default=8.0,
                    help="environment: a triangle keeps its previous colour index while that colour "
                         "is within this RGB distance of the best one (default 8; 0 = off)")
    ap.add_argument("--quantiser", choices=["kmeans", "mediancut"], default="kmeans",
                    help="how the two palettes are chosen: k-means (default) or Pillow median cut")
    ap.add_argument("--fit", choices=["crop", "stretch"], default="crop")
    ap.add_argument("--preview", help="optional PNG contact sheet of the result")
    args = ap.parse_args()

    if not 1 <= args.fps <= 30:
        sys.exit("--fps must be 1..30")
    if not 2 <= args.colours <= 255:
        sys.exit("--colours must be 2..255")
    if not 2 <= args.env_colours <= 16:
        sys.exit("--env-colours must be 2..16")
    if not (1 <= args.env_cols <= 120 and 1 <= args.env_rows <= 160):
        sys.exit("--env-cols must be 1..120 and --env-rows 1..160")
    if args.min_cell < 2 or args.min_cell > 60:
        sys.exit("--min-cell must be 2..60")
    if args.max_cell < args.min_cell:
        sys.exit("--max-cell must be >= --min-cell")
    if args.hysteresis < 0:
        sys.exit("--hysteresis must be >= 0")

    people = People(args.people)
    ffmpeg = find_ffmpeg()
    duration = args.duration if args.duration > 0 else probe_duration(ffmpeg, args.video) - args.start
    print("video: %s, %.2f s from %.2f s at %d fps, fit=%s" %
          (args.video, duration, args.start, args.fps, args.fit))

    peak = global_peak(ffmpeg, args, duration)
    print("exposure peak (97th percentile luminance): %.1f" % peak)

    cols, rows = args.env_cols, args.env_rows
    tri_count = cols * rows * 2
    print("environment: %d x %d cells, %d triangles per frame" % (cols, rows, tri_count))

    frames = []  # per frame: (env colours (tri_count, 3), bits, leaves)
    people_seen = 0
    for index, img in enumerate(frame_stream(ffmpeg, args.video, args.start, duration,
                                             args.fps, args.fit)):
        t = index / float(args.fps)
        ells = people.at(t)
        if ells:
            people_seen += 1
        graded = grade(img, peak)
        mask = people_mask(ells)
        env = environment_colours(graded, mask, cols, rows)
        bits, leaves = FrameModel(graded, ells, args.detail, args.min_cell, args.max_cell).run()
        frames.append((env, bits, leaves))
    if not frames:
        sys.exit("no frames decoded; check --start/--duration")
    if len(frames) > 65535:
        sys.exit("too many frames for the u16 counter; shorten --duration or lower --fps")

    leaf_counts = [len(leaves) for _, _, leaves in frames]
    max_leaves = max(leaf_counts)
    print("frames: %d (%d with people), people leaves per frame: avg %.0f, max %d" %
          (len(frames), people_seen, sum(leaf_counts) / float(len(frames)), max_leaves))

    # Environment palette over every environment triangle of the film.
    env_all = np.concatenate([env for env, _, _ in frames], axis=0)
    env_palette, _ = quantise(env_all, args.env_colours, args.quantiser)
    env_indices = stable_indices(env_all, env_palette, len(frames), tri_count, args.hysteresis)

    # People palette over the people leaf colours only. Each triangle is weighted by its area, so
    # big triangles decide the palette more than small ones.
    people_cols = []
    people_w = []
    for _, _, leaves in frames:
        for leaf in leaves:
            area = 0.5 * (leaf[2] - leaf[0]) * (leaf[3] - leaf[1])
            people_cols.append(leaf[5])
            people_cols.append(leaf[6])
            people_w.append(area)
            people_w.append(area)
    if people_cols:
        people_palette, people_indices = quantise(np.array(people_cols), args.colours,
                                                 args.quantiser, np.array(people_w))
    else:
        people_palette, people_indices = [0, 0, 0], np.zeros(0, dtype=np.uint8)

    payloads = []
    ei = 0
    pi = 0
    for _, bits, leaves in frames:
        env_bytes = bytearray()
        for c in range(tri_count // 2):
            hi = int(env_indices[ei])
            lo = int(env_indices[ei + 1])
            env_bytes.append((hi << 4) | lo)
            ei += 2
        colour_bytes = bytearray()
        for _ in leaves:
            colour_bytes.append(int(people_indices[pi]))
            colour_bytes.append(int(people_indices[pi + 1]))
            pi += 2
        bit_bytes = pack_bits(bits)
        if len(bit_bytes) > 65535:
            sys.exit("a frame's tree is too large for its u16 length")
        payloads.append(struct.pack(">H", len(bit_bytes)) + bit_bytes
                        + bytes(env_bytes) + bytes(colour_bytes))
    max_payload = max(len(p) for p in payloads)
    if max_payload > 65535:
        sys.exit("a frame payload is too large for the u16 length")

    env_count = len(env_palette) // 3
    people_count = len(people_palette) // 3
    with open(args.out, "wb") as f:
        f.write(MAGIC)
        f.write(struct.pack(">BBHHHBHBBBBH", VERSION, args.fps, len(payloads),
                            SRC_W, SRC_H, args.min_cell, max_leaves, cols, rows,
                            env_count, people_count, max_payload))
        f.write(bytes(env_palette[: env_count * 3]))
        f.write(bytes(people_palette[: people_count * 3]))
        for payload in payloads:
            f.write(struct.pack(">H", len(payload)))
            f.write(payload)

    header = 4 + 17 + env_count * 3 + people_count * 3
    total = header + sum(2 + len(p) for p in payloads)
    print("wrote %s: %d bytes (env palette %d, people palette %d), avg %.0f bytes per frame" %
          (args.out, total, len(env_palette) // 3, len(people_palette) // 3,
           total / float(len(payloads))))

    if args.preview:
        write_preview(args.preview, frames, env_palette, env_indices,
                      people_palette, people_indices, cols, rows)
        print("preview: %s" % args.preview)


def write_preview(path, frames, env_palette, env_indices, people_palette, people_indices,
                  cols, rows):
    """Renders a few frames the way the device draws them (environment, then people)."""
    env_pal = np.array(env_palette, dtype=np.uint8).reshape(-1, 3)
    people_pal = np.array(people_palette, dtype=np.uint8).reshape(-1, 3)
    n = len(frames)
    step = max(1, n // 6)
    picks = list(range(0, n, step))[:6]
    tri_count = cols * rows * 2
    env_off = []
    people_off = []
    e = 0
    p = 0
    for _, _, leaves in frames:
        env_off.append(e)
        people_off.append(p)
        e += tri_count
        p += 2 * len(leaves)

    cw = SRC_W / float(cols)
    ch = SRC_H / float(rows)
    sheet = Image.new("RGB", (len(picks) * (SRC_W + 4), SRC_H), (30, 30, 30))
    for i, f in enumerate(picks):
        _, _, leaves = frames[f]
        frame = Image.new("RGB", (SRC_W, SRC_H), (0, 0, 0))
        draw = ImageDraw.Draw(frame)
        for c in range(tri_count // 2):
            cy, cx = divmod(c, cols)
            x0, y0 = cx * cw, cy * ch
            x1, y1 = x0 + cw, y0 + ch
            a = tuple(int(v) for v in env_pal[env_indices[env_off[f] + 2 * c]])
            b = tuple(int(v) for v in env_pal[env_indices[env_off[f] + 2 * c + 1]])
            draw.polygon([(x0, y0), (x1, y0), (x1, y1)], fill=a)
            draw.polygon([(x0, y0), (x1, y1), (x0, y1)], fill=b)
        for j, leaf in enumerate(leaves):
            ca = tuple(int(v) for v in people_pal[people_indices[people_off[f] + 2 * j]])
            cb = tuple(int(v) for v in people_pal[people_indices[people_off[f] + 2 * j + 1]])
            t1, t2 = triangles_of(leaf)
            draw.polygon(t1, fill=ca)
            draw.polygon(t2, fill=cb)
        sheet.paste(frame, (i * (SRC_W + 4), 0))
    sheet.save(path)


if __name__ == "__main__":
    main()
