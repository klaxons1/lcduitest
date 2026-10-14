# lcduitest — J2ME "Lotus" facet film

MIDP 2.0 / CLDC 1.1 MIDlet suite, one MIDlet (`LotusDemo`, suite name "Lotus"). It starts the film
right away, full screen 240×320:

- the video `docs/Phantasm Mineshaft Chase scene.mp4` is cropped to the screen aspect and fills it;
- every frame is modelled with its own adaptive triangles (about 150 on average, up to 500);
- each triangle is one flat colour taken from the video;
- frames play at 5 fps, the film loops;
- `res/phantasm-8bit.mid` loops in the background (MMAPI `audio/midi`). Soft key "Music on/off" toggles it.

## Layout

| Path | What |
|------|------|
| `src/LotusDemo.java` | MIDlet: lifecycle, background MIDI, Exit and Music on/off commands |
| `src/LotusCanvas.java` | Canvas and playback thread; redraws only when the film moves to a new frame |
| `src/LotusFilm.java` | Streams `res/lotus.bin`, decodes each frame's adaptive tree and draws its triangles |
| `res/lotus.bin` | Format LOT4: 802 frames, each a quadtree of triangles with palette colours (generated) |
| `tools/make_lotus_data.py` | Video → `res/lotus.bin` (adaptive triangulation per frame, colour sampling, palette) |
| `tools/preview/LotusPreview.java` | Desktop PNG preview of the frame renderer |
| `libs/cldc11.jar`, `libs/midp21.jar` | CLDC 1.1 / MIDP 2.1 APIs (from `klaxons1/l2d_m3g`) |
| `build.sh` | Builds `deployed/raw/lcduitest.jar` and `.jad` |
| `releases/` | Committed build artifacts (see `releases/README.md`) |

## How a frame is modelled

Each frame gets its own triangles. A quadtree covers the screen, and a cell is split while its
colour spread is above `--detail` or it is larger than `--max-cell`, down to `--min-cell`. Flat
areas stay as a few big triangles, while faces, hair and edges get many small ones. Each leaf is
cut into two triangles along the diagonal that fits the picture best. Each triangle is one flat
colour, the mean of the video pixels under it. Cells share their borders, so there are no gaps.

## Data format (LOT4)

Header (fps, frame count, source size 240×320, minimum cell, max leaves per frame, palette),
then per frame: a length, a bit-packed tree (split bit per node, diagonal bit per leaf), and two
palette colour bytes per leaf. The device streams the frames from the jar and draws each frame
as it is decoded.

## Regenerating the film data

```sh
pip install numpy pillow imageio-ffmpeg
python3 tools/make_lotus_data.py --video "docs/Phantasm Mineshaft Chase scene.mp4" \
    --out res/lotus.bin --fps 5 --detail 20 --min-cell 8 --max-cell 96 --colours 64
```

Options: `--detail` (lower = more triangles), `--min-cell` / `--max-cell` (smallest and largest
triangle cell in pixels), `--fps`, `--colours`, `--fit crop|stretch` (crop keeps the aspect ratio
and fills the screen), `--start` / `--duration` to pick a part of the video.
With the defaults above, the full clip (160 s) is 260 KB of frame data at about 146 cells per
frame on average. The decoded frames are not stored in git.

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
