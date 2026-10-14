#!/usr/bin/env python3
"""
Film soundtrack -> AMR-NB 12.2 kbps for the J2ME "Lotus" demo.

Takes the audio track of docs/Phantasm Mineshaft Chase scene.mp4 (AAC, 44.1 kHz stereo) and writes
a raw AMR-NB file (#!AMR header, 8 kHz mono, 12.2 kbps, the highest AMR-NB mode).

AMR-NB carries roughly 200-3400 Hz, so the chain is built around that band:

  1. Decode with ffmpeg to 44.1 kHz float, downmix to mono (mid = (L + R) / 2). The side signal
     is about 8 dB below the mid, so nothing is lost that the codec would keep.
  2. Bass: the low band (below 200 Hz) carries about 40% of the energy, which the codec cannot
     reproduce. A harmonic layer is generated from it (tanh waveshaping, band-passed to
     200-900 Hz, about 14 dB under the bass level), so the bass is heard through its harmonics
     ("missing fundamental"). The signal is then high-passed at 150 Hz.
  3. Resample to 8 kHz with a Kaiser-windowed polyphase filter (anti-aliasing above 4 kHz).
  4. ffmpeg: compressor (3:1 above -24 dBFS), linear gain to -18 LUFS (EBU R128, ebur128
     measurement), capped so the true peak stays at -1.5 dBTP, and a look-ahead limiter at
     -1.5 dBFS, because AMR-NB distorts on overload.
  5. Encode with libopencore_amrnb at 12.2 kbps, mono, 8 kHz, DTX off (music).

Then the result is decoded again and measured against the 8 kHz input (segmental SNR, level,
band energy); the numbers are printed.

Needs: numpy, scipy, soundfile, and ffmpeg built with libopencore_amrnb. ffmpeg is taken from
$FFMPEG, from PATH, or from the imageio-ffmpeg package (its static build includes it).

Usage: tools/make_film_audio.py [--video PATH] [--out PATH] [--work DIR]
"""
import argparse
import json
import os
import re
import shutil
import subprocess
import sys

import numpy as np
import soundfile as sf
from scipy.signal import butter, resample_poly, sosfiltfilt

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
SRC_RATE = 44100
OUT_RATE = 8000
TARGET_LUFS = -18.0
TRUE_PEAK = -1.5
LIMIT = 10 ** (-1.5 / 20)
AMR_BITRATE = 12200
COMPRESSOR = "acompressor=threshold=0.063:ratio=3:attack=10:release=200"


def find_ffmpeg():
    env = os.environ.get("FFMPEG")
    if env:
        return env
    exe = shutil.which("ffmpeg")
    if exe:
        return exe
    try:
        import imageio_ffmpeg
        return imageio_ffmpeg.get_ffmpeg_exe()
    except ImportError:
        sys.exit("ffmpeg not found: set FFMPEG, put it on PATH, or pip install imageio-ffmpeg")


def run(cmd, capture=False):
    if capture:
        p = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, check=True)
        return p.stdout.decode("utf-8", "replace")
    subprocess.run(cmd, check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    return ""


def bass_layer(mid, rate):
    """High-passed mid plus a harmonic layer generated from the bass. Returns (signal, bass RMS, harmonic RMS before scaling)."""
    sos_low = butter(4, 200.0, btype="lowpass", fs=rate, output="sos")
    low = sosfiltfilt(sos_low, mid)
    rms_low = np.sqrt(np.mean(low ** 2)) + 1e-9
    shaped = np.tanh(2.5 * low / rms_low)  # odd harmonics of the bass notes
    sos_harm = butter(4, [200.0, 900.0], btype="bandpass", fs=rate, output="sos")
    harm = sosfiltfilt(sos_harm, shaped)
    rms_harm = np.sqrt(np.mean(harm ** 2)) + 1e-9
    harm *= rms_low * 10 ** (-14.0 / 20) / rms_harm
    sos_hp = butter(4, 150.0, btype="highpass", fs=rate, output="sos")
    return sosfiltfilt(sos_hp, mid) + harm, rms_low, rms_harm


def ebur_measure(ff, wav):
    """Integrated loudness (LUFS) and true peak (dBTP) by ffmpeg's ebur128 filter."""
    out = run([ff, "-hide_banner", "-nostats", "-i", wav, "-af", "ebur128=peak=true",
               "-f", "null", "-"], capture=True)
    summary = out[out.rindex("Summary:"):]
    loud = float(re.search(r"I:\s+(-?[\d.]+) LUFS", summary).group(1))
    peak = float(re.search(r"True peak:\s+Peak:\s+(-?[\d.]+) dBFS", summary).group(1))
    return loud, peak


def segmental_snr(ref, dec, frame=160):
    n = min(len(ref), len(dec)) // frame * frame
    r = ref[:n].reshape(-1, frame)
    d = dec[:n].reshape(-1, frame)
    err = np.sum((r - d) ** 2, axis=1) + 1e-12
    sig = np.sum(r ** 2, axis=1) + 1e-12
    snr = 10 * np.log10(sig / err)
    snr = np.clip(snr, -10, 35)
    return float(np.mean(snr)), n


def band_db(x, rate, lo, hi):
    from scipy.signal import welch
    f, p = welch(x, rate, nperseg=512)
    s = (f >= lo) & (f < hi)
    return 10 * np.log10(np.sum(p[s]) + 1e-20)


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--video", default=os.path.join(ROOT, "docs", "Phantasm Mineshaft Chase scene.mp4"))
    ap.add_argument("--out", default=os.path.join(ROOT, "audio", "film-soundtrack-amrnb-12k2.amr"))
    ap.add_argument("--work", default=os.path.join(ROOT, "build", "audio"),
                    help="scratch directory (git-ignored)")
    args = ap.parse_args()
    ff = find_ffmpeg()
    os.makedirs(args.work, exist_ok=True)
    os.makedirs(os.path.dirname(args.out), exist_ok=True)
    w = args.work

    # 1. Decode and downmix.
    stereo = os.path.join(w, "src_44k_stereo_f32.wav")
    run([ff, "-hide_banner", "-y", "-i", args.video, "-vn", "-map", "0:a:0",
         "-c:a", "pcm_f32le", "-ar", str(SRC_RATE), stereo])
    x, rate = sf.read(stereo, dtype="float64")
    assert rate == SRC_RATE
    mid = 0.5 * (x[:, 0] + x[:, 1])

    # 2. Bass harmonics, high-pass at 150 Hz.
    sig, rms_low, _ = bass_layer(mid, rate)
    print("bass: low band RMS %.1f dBFS, harmonic layer 14 dB under it" % (20 * np.log10(rms_low)))

    # 3. Resample to 8 kHz (polyphase, Kaiser window; cut-off well above 3.4 kHz).
    g = np.gcd(OUT_RATE, rate)
    y = resample_poly(sig, OUT_RATE // g, rate // g, window=("kaiser", 8.6))
    peak = np.max(np.abs(y))
    if peak > 0.98:
        y *= 0.98 / peak
    mono8 = os.path.join(w, "mono_8k_f32.wav")
    sf.write(mono8, y.astype(np.float32), OUT_RATE, subtype="FLOAT")

    # 4. Compress, then gain to the target loudness (capped so the true peak stays under
    #    TRUE_PEAK), then a look-ahead limiter. Loudness is measured with ebur128, which handles
    #    8 kHz input correctly (loudnorm's measurement does not).
    comp = os.path.join(w, "comp_8k_f32.wav")
    run([ff, "-hide_banner", "-y", "-i", mono8, "-af", COMPRESSOR, "-c:a", "pcm_f32le", comp])
    loud, peak = ebur_measure(ff, comp)
    gain = min(TARGET_LUFS - loud, TRUE_PEAK - peak)
    print("before gain: %.1f LUFS, peak %.1f dBTP; gain %+.2f dB (target %.1f LUFS, peak cap %.1f dBTP)"
          % (loud, peak, gain, TARGET_LUFS, TRUE_PEAK))
    chain = "%s,volume=%.3fdB,alimiter=limit=%.4f:attack=5:release=50:level=disabled" % (
        COMPRESSOR, gain, LIMIT)
    final_wav = os.path.join(w, "final_8k_s16.wav")
    run([ff, "-hide_banner", "-y", "-i", mono8, "-af", chain, "-ar", str(OUT_RATE), "-ac", "1",
         "-c:a", "pcm_s16le", final_wav])
    print("final: %.1f LUFS, peak %.1f dBTP" % ebur_measure(ff, final_wav))

    # 5. AMR-NB 12.2 kbps.
    run([ff, "-hide_banner", "-y", "-i", final_wav, "-ar", str(OUT_RATE), "-ac", "1",
         "-c:a", "libopencore_amrnb", "-b:a", str(AMR_BITRATE), "-f", "amr", args.out])

    # 6. Check: decode the AMR and compare with the 8 kHz input.
    dec = os.path.join(w, "decoded_8k_s16.wav")
    run([ff, "-hide_banner", "-y", "-i", args.out, "-ar", str(OUT_RATE), "-ac", "1",
         "-c:a", "pcm_s16le", dec])
    ref, _ = sf.read(final_wav, dtype="float64")
    out, _ = sf.read(dec, dtype="float64")
    snr, n = segmental_snr(ref, out)
    size = os.path.getsize(args.out)
    dur = len(ref) / float(OUT_RATE)
    with open(args.out, "rb") as f:
        magic = f.read(6)
    print("wrote %s: %d bytes, %.1f s, %.2f kbps average, magic %r" %
          (args.out, size, dur, size * 8 / dur / 1000.0, magic))
    print("decoded: %.1f LUFS, peak %.1f dBTP" % ebur_measure(ff, dec))
    print("segmental SNR (20 ms frames, clipped -10..35 dB): %.2f dB" % snr)
    for lo, hi in [(150, 300), (300, 1000), (1000, 2500), (2500, 3400)]:
        print("band %4d-%4d Hz: source %.1f dB, decoded %.1f dB" %
              (lo, hi, band_db(ref, OUT_RATE, lo, hi), band_db(out, OUT_RATE, lo, hi)))
    if magic != b"#!AMR\n":
        sys.exit("output is not a raw AMR-NB file")


if __name__ == "__main__":
    main()
