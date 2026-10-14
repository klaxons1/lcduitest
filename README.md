# lcduitest — J2ME "Lotus" demo

MIDP 2.0 / CLDC 1.1 MIDlet suite. `LotusDemo` (MIDlet-1) is a full-screen tunnel
animation built from frames of `docs/Phantasm Mineshaft Chase scene.mp4`:

- every cell of a film frame becomes a single-colour polygon;
- the cells are arranged into a lotus-petal tunnel that twists, breathes and flows toward the viewer;
- `res/phantasm-8bit.mid` loops in the background (MMAPI `audio/midi`).

`LCDUITester` (MIDlet-2) is the original test MIDlet and is unchanged.

## Layout

| Path | What |
|------|------|
| `src/LotusDemo.java` | MIDlet: lifecycle, background MIDI, Exit / Music command |
| `src/LotusCanvas.java` | Canvas and render thread (~25 fps, drawRGB blit) |
| `src/LotusTunnel.java` | Integer-only tunnel renderer and scanline quad fill |
| `src/LotusData.java` | Reader for `res/lotus.bin` |
| `res/lotus.bin` | 96 frames × 8 bands × 32 sectors, 64-colour palette (generated) |
| `tools/make_lotus_data.py` | Frame extraction → `res/lotus.bin` |
| `tools/preview/LotusPreview.java` | Desktop PNG preview of the renderer (no phone needed) |
| `libs/cldc11.jar`, `libs/midp21.jar` | CLDC 1.1 / MIDP 2.1 APIs (from `klaxons1/l2d_m3g`) |
| `build.sh` | Builds `deployed/raw/lcduitest.jar` and `.jad` |

## Build

```sh
JAVA_HOME=/path/to/jdk8 ./build.sh   # or plain `./build.sh` if javac is on PATH
```

The build uses JDK 8 with `-source/-target 1.3` against the CLDC/MIDP bootclasspath, so only
CLDC/MIDP APIs can be used.

## Regenerating the animation data

```sh
pip install numpy pillow imageio-ffmpeg
python3 tools/make_lotus_data.py --video "docs/Phantasm Mineshaft Chase scene.mp4" \
    --out res/lotus.bin --frames 96
```

Options: `--frames` (how many frames are sampled evenly across the clip) and `--palette`
(number of colours, up to 255). The decoded frames are not stored in git.

## Desktop preview

```sh
javac -d /tmp/desk src/LotusData.java src/LotusTunnel.java tools/preview/LotusPreview.java
java -cp /tmp/desk LotusPreview res/lotus.bin /tmp/preview 176 208 0 5000 10000
```
