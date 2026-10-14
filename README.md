# lcduitest — J2ME "Lotus" triangle film

MIDP 2.0 / CLDC 1.1 MIDlet suite, one MIDlet (`LotusDemo`, suite name "Lotus"). It starts the film
right away, full screen 240×320:

- the video `docs/Phantasm Mineshaft Chase scene.mp4` is cropped to the screen aspect and fills it;
- every frame is drawn with flat-coloured triangles in two layers:
  - **environment**: a static, coarse mesh of big triangles (about 60) covers the whole screen,
    and each triangle takes the mean colour of the non-people pixels under it;
  - **people**: small triangles (adaptive quadtree, about 66 leaves on average, up to 162) cover
    the people regions drawn on top, following outlines and details;
- the people regions are marked by hand on keyframes every 3 s (`tools/lotus/people_keyframes.json`);
- frames play at 5 fps, the film loops;
- `res/phantasm-8bit.mid` loops in the background (MMAPI `audio/midi`). Soft key "Music on/off" toggles it.

## Layout

| Path | What |
|------|------|
| `src/LotusDemo.java` | MIDlet: lifecycle, background MIDI, Exit and Music on/off commands |
| `src/LotusCanvas.java` | Canvas and playback thread; redraws only when the film moves to a new frame |
| `src/LotusFilm.java` | Streams `res/lotus.bin`, decodes each frame (environment colours, people tree) and draws it |
| `res/lotus.bin` | Format LOT5: 802 frames, environment mesh in the header, per-frame colours and people tree (generated) |
| `tools/make_lotus_data.py` | Video + people markup → `res/lotus.bin` (environment, people quadtree, palette) |
| `tools/lotus/people_keyframes.json` | Manual people markup: ellipses `[cx, cy, rx, ry]` on keyframes 0–53 (every 3 s), in 240×320 |
| `tools/preview/LotusPreview.java` | Desktop PNG preview of the frame renderer |
| `libs/cldc11.jar`, `libs/midp21.jar` | CLDC 1.1 / MIDP 2.1 APIs (from `klaxons1/l2d_m3g`) |
| `build.sh` | Builds `deployed/raw/lcduitest.jar` and `.jad` |
| `releases/` | Committed build artifacts (see `releases/README.md`) |

## How a frame is modelled

1. **Environment.** A jittered mesh of 5×6 cells (60 triangles) covers the screen. The mesh and
   its triangles are fixed for the whole film. Each triangle gets a flat colour per frame, the mean
   of the non-people pixels under it.
2. **People.** The ellipses from the markup for the frame's time are interpolated between
   keyframes (when both keyframes have the same number of people; otherwise the nearest keyframe is
   held). A quadtree covers the screen, but only cells that touch an ellipse are kept. A cell is
   split while it straddles an outline, has a colour spread above `--detail`, or is larger than
   `--max-cell`, down to `--min-cell`. Each leaf is cut into two triangles along the diagonal that
   fits the picture best, and each triangle is one flat colour.

## Data format (LOT5)

Header: magic `LOT5`, version, fps, frame count, source size 240×320, minimum cell, max people
leaves per frame, palette size, then the environment mesh (vertices and triangles) and the palette.
Then per frame: a length, a bit-packed people tree, one palette byte per environment triangle, and
two palette bytes per people leaf. The tree has, for each node, a "present" bit (0 = no people in
this node), a "split" bit where the node can split, and a "diagonal" bit per leaf. The device
streams the frames from the jar and draws each frame as it is decoded.

## Regenerating the film data

```sh
pip install numpy pillow imageio-ffmpeg
python3 tools/make_lotus_data.py --video "docs/Phantasm Mineshaft Chase scene.mp4" \
    --out res/lotus.bin --fps 5 --people tools/lotus/people_keyframes.json \
    --detail 20 --min-cell 8 --max-cell 96 --colours 64
```

Options: `--people` (people markup), `--env-cols` / `--env-rows` / `--env-jitter` (environment mesh),
`--detail` (lower = more people triangles), `--min-cell` / `--max-cell` (smallest and largest people
triangle cell in pixels), `--fps`, `--colours`, `--fit crop|stretch` (crop keeps the aspect ratio and
fills the screen), `--start` / `--duration` to pick a part of the video, `--preview` for a PNG sheet.
With the defaults above, the full clip (160 s) is 177 KB of frame data. The decoded frames are not
stored in git.

To edit the people markup, change the ellipses in `tools/lotus/people_keyframes.json` (keyframe `k`
is the source time `3k` s, an empty list means no people) and regenerate.

## Build

```sh
JAVA_HOME=/path/to/jdk8 ./build.sh   # or plain `./build.sh` if javac is on PATH
```

The build uses JDK 8 with `-source/-target 1.3` against the CLDC/MIDP bootclasspath, so only
CLDC/MIDP APIs can be used.

## Desktop preview

```sh
javac -d /tmp/desk src/LotusFilm.java tools/preview/LotusPreview.java
java -cp /tmp/desk:res LotusPreview /tmp/preview 240 320 0 150 400
```
