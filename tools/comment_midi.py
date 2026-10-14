#!/usr/bin/env python3
"""
Adds comments to a Standard MIDI File as Text meta events (FF 01).

The comments describe the file (tracks, instruments, tempo, length) and sit at tick 0 at the start
of track 0, before any note. Meta text events are ignored by sequencers, so the playback is the
same. The script checks that every non-text event keeps its tick and bytes, then writes the file.

Usage: tools/comment_midi.py [PATH]        (default: res/phantasm-8bit.mid, rewritten in place)
       tools/comment_midi.py --check ORIG NEW
"""
import os
import struct
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DEFAULT = os.path.join(ROOT, "res", "phantasm-8bit.mid")
GM = {0: "Acoustic Grand Piano", 39: "Synth Bass 2", 80: "Lead 1 (square)", 81: "Lead 2 (sawtooth)"}


def vlq_read(d, p):
    v = 0
    while True:
        b = d[p]
        p += 1
        v = (v << 7) | (b & 0x7F)
        if not b & 0x80:
            return v, p


def vlq_bytes(v):
    out = [v & 0x7F]
    v >>= 7
    while v:
        out.append((v & 0x7F) | 0x80)
        v >>= 7
    return bytes(reversed(out))


def parse(d):
    """Returns (header, tracks); tracks = list of event lists (tick, bytes) without end-of-track."""
    assert d[:4] == b"MThd"
    hl = struct.unpack(">I", d[4:8])[0]
    header = d[8:8 + hl]
    fmt, ntr, div = struct.unpack(">HHH", header[:6])
    pos = 8 + hl
    tracks = []
    for _ in range(ntr):
        assert d[pos:pos + 4] == b"MTrk"
        L = struct.unpack(">I", d[pos + 4:pos + 8])[0]
        p, end = pos + 8, pos + 8 + L
        t, status, events = 0, None, []
        while p < end:
            dt, p = vlq_read(d, p)
            t += dt
            start = p
            b = d[p]
            if b == 0xFF:
                ln, q = vlq_read(d, p + 2)
                p = q + ln
            elif b in (0xF0, 0xF7):
                ln, q = vlq_read(d, p + 1)
                p = q + ln
            else:
                if b & 0x80:
                    status = b
                    p += 1
                st = status & 0xF0
                p += 1 if st in (0xC0, 0xD0) else 2
            # bytes of the event after its delta time
            events.append((t, d[start:p]))
        tracks.append((fmt, div, events))
        pos = end
    return header, tracks


def summarise(d):
    """Human facts about the file: track name, channel, programs, note count, pitch range, end tick."""
    header, tracks = parse(d)
    fmt, div, _ = tracks[0]
    rows = []
    for ti, (_, _, events) in enumerate(tracks):
        name, chans, progs, notes, pitches, tempo, ts, end = None, set(), set(), 0, [], None, None, 0
        status = None
        for t, raw in events:
            end = max(end, t)
            if raw[0] == 0xFF:
                typ = raw[1]
                ln, q = vlq_read(raw, 2)
                data = raw[q:q + ln]
                if typ == 0x03 and name is None:
                    name = data.decode("latin-1")
                elif typ == 0x51:
                    tempo = int.from_bytes(data, "big")
                elif typ == 0x58:
                    ts = (data[0], 2 ** data[1])
                continue
            b = raw[0]
            if b & 0x80:
                status = b
                body = raw[1:]
            else:
                body = raw
            st, ch = status & 0xF0, status & 0x0F
            if st in (0x80, 0x90, 0xA0, 0xB0, 0xE0):
                if st == 0x90 and body[1] > 0:
                    notes += 1
                    pitches.append(body[0])
                chans.add(ch + 1)
            elif st in (0xC0, 0xD0):
                if st == 0xC0:
                    progs.add(body[0])
                chans.add(ch + 1)
        rows.append({
            "index": ti, "name": name, "channels": sorted(chans), "programs": sorted(progs),
            "notes": notes, "pitch": (min(pitches), max(pitches)) if pitches else None,
            "end_tick": end, "tempo": tempo, "ts": ts,
        })
    return fmt, div, rows


def comment_lines(d, path):
    fmt, div, rows = summarise(d)
    total = max(r["end_tick"] for r in rows)
    tempo = next(r["tempo"] for r in rows if r["tempo"])
    bpm = 60000000 / tempo
    ts = next(r["ts"] for r in rows if r["ts"])
    seconds = total / float(div) * tempo / 1e6
    lines = [
        "Phantasm 8-bit: background music for the Lotus demo (res/phantasm-8bit.mid).",
        "Format %d, %d tracks, %d ticks per quarter note, %.0f BPM, %d/%d, length %d:%02d."
        % (fmt, len(rows), div, bpm, ts[0], ts[1], int(seconds) // 60, int(seconds) % 60),
    ]
    for r in rows:
        if r["notes"] == 0 and not r["channels"]:
            continue
        chans = ", ".join(str(c) for c in r["channels"])
        if 10 in r["channels"]:
            what = "percussion (GM drum kit)"
        else:
            what = GM.get(r["programs"][0], "program %d" % r["programs"][0]) if r["programs"] else "?"
        lo, hi = r["pitch"] if r["pitch"] else (0, 0)
        lines.append("Track %d %s: channel %s, %s, %d notes, notes %d..%d."
                     % (r["index"], r["name"] or "", chans, what, r["notes"], lo, hi))
    lines.append("Comments are Text meta events at tick 0; sequencers ignore them, so the playback is unchanged.")
    return lines


def add_comments(d, lines):
    """Insert the comments at tick 0 at the start of track 0 (zero delta, so later deltas are unchanged)."""
    hl = struct.unpack(">I", d[4:8])[0]
    head = d[:8 + hl]
    pos = 8 + hl
    L = struct.unpack(">I", d[pos + 4:pos + 8])[0]
    body = d[pos + 8:pos + 8 + L]
    rest = d[pos + 8 + L:]
    texts = b""
    for line in lines:
        data = line.encode("latin-1", "replace")[:250]
        texts += vlq_bytes(0) + b"\xff\x01" + vlq_bytes(len(data)) + data
    new_body = texts + body
    track0 = b"MTrk" + struct.pack(">I", len(new_body)) + new_body
    tail = rest
    # Rebuild: header, new track 0, the other tracks unchanged.
    return head + track0 + tail


def strip_text(d):
    """All events except Text meta events, as (tick, bytes) per track, for comparison."""
    _, tracks = parse(d)
    out = []
    for _, _, events in tracks:
        out.append([(t, raw) for t, raw in events if not (raw[0] == 0xFF and raw[1] == 0x01)])
    return out


def check(orig, new):
    a, b = strip_text(orig), strip_text(new)
    if a != b:
        sys.exit("playback events differ")
    print("playback events identical (%d tracks)" % len(a))


def main():
    if len(sys.argv) == 4 and sys.argv[1] == "--check":
        with open(sys.argv[2], "rb") as f:
            orig = f.read()
        with open(sys.argv[3], "rb") as f:
            new = f.read()
        check(orig, new)
        return
    path = sys.argv[1] if len(sys.argv) > 1 else DEFAULT
    with open(path, "rb") as f:
        d = f.read()
    lines = comment_lines(d, path)
    new = add_comments(d, lines)
    check(d, new)
    with open(path, "wb") as f:
        f.write(new)
    print("wrote %s (%d -> %d bytes)" % (path, len(d), len(new)))
    for line in lines:
        print("  " + line)


if __name__ == "__main__":
    main()
