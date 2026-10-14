# lcduitest — J2ME "Lotus" film

MIDP 2.0 / CLDC 1.1 MIDlet suite. `LotusDemo` (MIDlet-1) plays the video
`docs/Phantasm Mineshaft Chase scene.mp4` as a film:

- every frame of the video is a grid of 40×22 cells;
- every cell is drawn as one single-colour quad polygon, so the frame is a mosaic of flat colours;
- frames play back at 6 fps (the film's own clock, looping);
- `res/phantasm-8bit.mid` loops in the background (MMAPI `audio/midi`).

`LCDUITester` (MIDlet-2) is the original test MIDlet and is unchanged.

## Layout

| Path | What |
|------|------|
| `src/LotusDemo.java` | MIDlet: lifecycle, background MIDI, Exit / Music on-off command |
| `src/LotusCanvas.java` | Canvas and playback thread; redraws only when the film moves to a new frame |
| `src/LotusFilm.java` | Streams `res/lotus.bin`, decodes cells, draws the frame as single-colour quads |
| `res/lotus.bin` | 962 frames × 40×22 cells, 16-colour palette, 4-bit packed (generated) |
| `tools/make_lotus_data.py` | Video → `res/lotus.bin` |
| `tools/preview/LotusPreview.java` | Desktop PNG preview of the frame renderer |
| `libs/cldc11.jar`, `libs/midp21.jar` | CLDC 1.1 / MIDP 2.1 APIs (from `klaxons1/l2d_m3g`) |
| `build.sh` | Builds `deployed/raw/lcduitest.jar` and `.jad` |
| `releases/` | Committed build artifacts (see `releases/README.md`) |

## Data format (`LOT2`)

Header: magic `LOT2`, version, bits per cell (4), cols, rows, palette size, fps, frame count (u16),
then the palette (RGB) and the packed frames one after another. The device streams the frames
from the jar, so only one packed frame (440 bytes) is held in memory.

## Regenerating the film data

```sh
pip install numpy pillow imageio-ffmpeg
python3 tools/make_lotus_data.py --video "docs/Phantasm Mineshaft Chase scene.mp4" \
    --out res/lotus.bin --fps 6 --cols 40 --rows 22 --colours 16
```

Options: `--start` / `--duration` select a part of the video; `--fps` sets the playback rate
(size scales with fps × duration); `--cols` / `--rows` set the cell grid (more cells means more
detail and more bytes); `--colours` up to 255 (above 16 colours each cell takes 8 bits).
The decoded frames are not stored in git.

Measured for the full clip (160 s): 40×22 at 6 fps is 423 KB raw, about 128 KB deflated in the jar.
48×27 at 8 fps is 831 KB raw, about 234 KB deflated.

## Build

```sh
JAVA_HOME=/path/to/jdk8 ./build.sh   # or plain `./build.sh` if javac is on PATH
```

The build uses JDK 8 with `-source/-target 1.3` against the CLDC/MIDP bootclasspath, so only
CLDC/MIDP APIs can be used.

## Desktop preview

```sh
javac -d /tmp/desk src/LotusFilm.java tools/preview/LotusPreview.java
java -cp /tmp/desk:res LotusPreview /tmp/preview 176 208 0 300 600
```
