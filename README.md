# lcduitest — J2ME "Lotus" triangle film

MIDP 2.0 / CLDC 1.1 MIDlet suite, one MIDlet (`LotusDemo`, suite name "Lotus"). It starts the film
right away, full screen 240×320:

- the video `docs/Phantasm Mineshaft Chase scene.mp4` is cropped to the screen aspect and fills it;
- every frame is a mosaic of flat-coloured triangles in two layers:
  - **environment**: a uniform grid of 48×64 cells (5×5 px), each cell cut into two triangles, each
    triangle with its own colour from a 16-colour palette (6144 triangles per frame);
  - **people**: small triangles (adaptive quadtree, down to 4 px, about 417 leaves on average, up to
    1159) drawn on top, following outlines and details, with a 64-colour palette; the regions are
    marked by hand on keyframes every 3 s (`tools/lotus/people_keyframes.json`);
- frames play at 5 fps, the film loops;
- `res/phantasm-8bit.mid` loops in the background (MMAPI `audio/midi`). Soft key "Music on/off" toggles it.

## Layout

| Path | What |
|------|------|
| `src/LotusDemo.java` | MIDlet: lifecycle, background MIDI, Exit and Music on/off commands |
| `src/LotusCanvas.java` | Canvas and playback thread: draws the environment grid, then the people triangles (`fillTriangle`) |
| `src/LotusFilm.java` | Streams `res/lotus.bin` and decodes each frame: environment colour nibbles and the people tree |
| `res/lotus.bin` | Format LOT7: 802 frames, each an environment grid and a people tree (generated, ~3.3 MB) |
| `tools/make_lotus_data.py` | Video + people markup → `res/lotus.bin` (environment grid, people quadtree, two palettes) |
| `tools/lotus/people_keyframes.json` | Manual people markup: ellipses `[cx, cy, rx, ry]` on keyframes 0–53 (every 3 s), in 240×320 |
| `tools/preview/LotusPreview.java` | Desktop PNG preview: environment grid plus people triangles |
| `libs/cldc11.jar`, `libs/midp21.jar` | CLDC 1.1 / MIDP 2.1 APIs (from `klaxons1/l2d_m3g`) |
| `build.sh` | Builds `deployed/raw/lcduitest.jar` and `.jad` |
| `tools/fetch_j2me_tools.sh` | Clones `klaxons1/j2me_cldc` and builds the preverifier (and, with `KVM=1`, the KVM) into `build/j2me/` |
| `tools/kvm/check_film.sh` | Runs `LotusFilm` on the CLDC KVM and on the desktop JVM, compares checksums |
| `releases/` | Committed build artifacts (see `releases/README.md`) |

## How a frame is modelled

1. **Environment.** The whole frame is graded (shared exposure, gamma, a little saturation). The
   screen is divided into a uniform grid of `--env-cols` × `--env-rows` cells (48×64 in the current build).
   Each cell is cut along its TL-BR diagonal into an upper-right and a lower-left triangle. Each
   triangle takes the mean colour of its pixels (people pixels excluded when possible), quantised to
   a 16-colour palette shared by the whole film (k-means). A triangle keeps its previous palette
   index while that colour is within `--hysteresis` (default 8) of the best one, so the picture
   flickers less and the file compresses better.
2. **People.** The ellipses from the markup for the frame's time are interpolated between
   keyframes (when both keyframes have the same number of people; otherwise the nearest keyframe is
   held). A quadtree covers the screen, but only cells that touch an ellipse are kept. A cell is
   split while it straddles an outline, has a colour spread above `--detail`, or is larger than
   `--max-cell`, down to `--min-cell`. Each leaf is cut into two triangles along the diagonal that
   fits the picture best, and each triangle is one flat colour (64-colour people palette, k-means,
   each triangle weighted by its area).

## Data format (LOT7)

Header: magic `LOT7`, version, fps, frame count, source size 240×320, minimum cell, max people leaves
per frame, grid columns and rows, environment palette size, people palette size, largest frame
payload, then the two palettes. Then per frame: a 16-bit length and the payload: a 16-bit tree bit
length, the bit-packed people tree, one byte per environment cell (high nibble = upper-right
triangle, low nibble = lower-left triangle), then two people palette bytes per leaf. The tree has, for
each node, a "present" bit (0 = no people in this node), a "split" bit where the node can split, and a
"diagonal" bit per leaf. The device streams the frames from the jar and draws each frame as it is
decoded.

## Regenerating the film data

```sh
pip install numpy pillow imageio-ffmpeg
python3 tools/make_lotus_data.py --video "docs/Phantasm Mineshaft Chase scene.mp4" \
    --out res/lotus.bin --fps 5 --people tools/lotus/people_keyframes.json \
    --env-cols 48 --env-rows 64 --env-colours 16 \
    --detail 15 --min-cell 4 --max-cell 32 --colours 64
```

Options: `--env-cols` / `--env-rows` (grid size; more cells = more triangles per frame),
`--env-colours` (environment palette, up to 16), `--people` (people markup), `--colours` (people
palette), `--quantiser kmeans|mediancut` (how the palettes are chosen; k-means is the default), `--hysteresis` (environment index stickiness, RGB distance; default 8, 0 = off), `--detail` (lower = more people triangles), `--min-cell` / `--max-cell` (smallest and
largest people triangle cell in pixels), `--fps`, `--fit crop|stretch` (crop keeps the aspect ratio
and fills the screen), `--start` / `--duration` to pick a part of the video, `--preview` for a PNG
sheet. With the settings above, the full clip (160 s) is 3.3 MB. The decoded frames are not stored
in git.

To edit the people markup, change the ellipses in `tools/lotus/people_keyframes.json` (keyframe `k`
is the source time `3k` s, an empty list means no people) and regenerate.

## Build

```sh
JAVA_HOME=/path/to/jdk8 ./build.sh   # or plain `./build.sh` if javac is on PATH
```

The build uses JDK 8 with `-source/-target 1.3` against the CLDC/MIDP bootclasspath, so only
CLDC/MIDP APIs can be used.

The classes are run through the CLDC preverifier before they go into the jar. `build.sh` builds it
on first use with `tools/fetch_j2me_tools.sh`, which clones `klaxons1/j2me_cldc` (depth 1) into
`build/j2me_cldc`, applies the portability fixes needed on a modern 64-bit gcc, and writes
`build/j2me/preverify`. Nothing from that repository is committed here. Needs `git`, `gcc`, `make`.

## Checking the decoder on the CLDC KVM

```sh
KVM=1 JAVA_HOME=/path/to/jdk8 tools/fetch_j2me_tools.sh   # preverifier + KVM (x86_64)
JAVA_HOME=/path/to/jdk8 tools/kvm/check_film.sh
```

`tools/kvm/check_film.sh` compiles `LotusFilm` and `tools/kvm/KvmCheck.java` for CLDC, preverifies them,
runs them on the KVM (`build/j2me/kvm`) and on the desktop JVM, and compares the checksums over a
fixed sequence of frame seeks. The current `res/lotus.bin` gives 802 frames, 48×64, on both (checksum
`3747855290951571346`).

## Desktop preview

```sh
javac -d /tmp/desk src/LotusFilm.java tools/preview/LotusPreview.java
java -cp /tmp/desk:res LotusPreview /tmp/preview 240 320 0 150 400
```
