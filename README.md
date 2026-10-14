# lcduitest — J2ME "Lotus" facet film

MIDP 2.0 / CLDC 1.1 MIDlet suite, one MIDlet (`LotusDemo`, suite name "Lotus"). It starts the film
right away, full screen 240×320:

- the video `docs/Phantasm Mineshaft Chase scene.mp4` is cropped to the screen aspect and fills it;
- a jittered triangle mesh covers the screen (18×24 cells, 864 triangles, irregular facets);
- every triangle is drawn in one flat colour taken from the video frame under it;
- frames play at 6 fps, the film loops;
- `res/phantasm-8bit.mid` loops in the background (MMAPI `audio/midi`). Soft key "Music on/off" toggles it.

## Layout

| Path | What |
|------|------|
| `src/LotusDemo.java` | MIDlet: lifecycle, background MIDI, Exit and Music on/off commands |
| `src/LotusCanvas.java` | Canvas and playback thread; redraws only when the film moves to a new frame |
| `src/LotusFilm.java` | Streams `res/lotus.bin`, rebuilds the mesh, draws each triangle as a flat-colour polygon |
| `res/lotus.bin` | Format LOT3: static mesh + 962 frames of 4-bit triangle colours (generated) |
| `tools/make_lotus_data.py` | Video → `res/lotus.bin` (mesh, colour sampling, palette) |
| `tools/preview/LotusPreview.java` | Desktop PNG preview of the frame renderer |
| `libs/cldc11.jar`, `libs/midp21.jar` | CLDC 1.1 / MIDP 2.1 APIs (from `klaxons1/l2d_m3g`) |
| `build.sh` | Builds `deployed/raw/lcduitest.jar` and `.jad` |
| `releases/` | Committed build artifacts (see `releases/README.md`) |

## Data format (LOT3)

Header, then the vertices (normalised 0..1024, scaled to the screen on the device), the triangles
(vertex indices), the palette, and the packed frames one after another: one 4-bit colour per
triangle, so 432 bytes per frame. The device streams the frames from the jar.

## Regenerating the film data

```sh
pip install numpy pillow imageio-ffmpeg
python3 tools/make_lotus_data.py --video "docs/Phantasm Mineshaft Chase scene.mp4" \
    --out res/lotus.bin --fps 6 --cols 18 --rows 24 --colours 16 --fit crop
```

Options: `--fit crop` (default) keeps the aspect ratio and fills the screen, `--fit stretch`
squeezes the whole frame instead; `--cols`/`--rows` set the mesh density; `--jitter` and `--seed`
change the facet shapes; `--start`/`--duration` select a part of the video; `--fps` sets the rate.
The decoded frames are not stored in git.

## Build

```sh
JAVA_HOME=/path/to/jdk8 ./build.sh   # or plain `./build.sh` if javac is on PATH
```

The build uses JDK 8 with `-source/-target 1.3` against the CLDC/MIDP bootclasspath, so only
CLDC/MIDP APIs can be used.

## Desktop preview

```sh
javac -d /tmp/desk src/LotusFilm.java tools/preview/LotusPreview.java
java -cp /tmp/desk:res LotusPreview /tmp/preview 240 320 0 300 600
```
