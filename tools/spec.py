#!/usr/bin/env python3
"""Query the local MIDP 2.0 LCDUI javadoc mirror for method/field semantics.

Usage:  tools/spec.py Class.method [Class2.method2 ...]
        tools/spec.py --field Class.FIELD
Prints the javadoc detail text (signature, description, parameters, throws).
Requires the J2ME_Docs mirror (see docs/README.md); falls back to nothing if absent.
"""
import re, sys, html, os, subprocess

DOCS = os.environ.get("MIDP_DOCS_DIR", os.path.expanduser("~/.local/doc/J2ME_Docs"))
BASE = os.path.join(DOCS, "docs/midp-2.0/javax/microedition/lcdui")
GAME = ("GameCanvas", "Layer", "LayerManager", "Sprite", "TiledLayer")


def strip(s):
    s = re.sub(r'<[^>]+>', ' ', s)
    return ' '.join(html.unescape(s).split())


def page(cls):
    path = os.path.join(BASE, "game" if cls in GAME else "", cls + ".html")
    if not os.path.exists(path):
        return None
    return open(path, encoding='utf8', errors='ignore').read()


def lookup(cls, member):
    h = page(cls)
    if h is None:
        return "%s: no javadoc page found" % cls
    parts = re.split(r'<A NAME="%s(?:\([^"]*\))?"><!-- --></A>' % re.escape(member), h)
    if len(parts) < 2:
        return "%s.%s: not found" % (cls, member)
    body = parts[1]
    nxt = re.search(r'<A NAME="[A-Za-z_]', body)
    if nxt:
        body = body[:nxt.start()]
    return "%s.%s\n%s" % (cls, member, strip(body)[:2500])


if __name__ == "__main__":
    args = sys.argv[1:]
    if not args:
        print(__doc__)
    for a in args:
        if "." not in a:
            continue
        cls, member = a.split(".", 1)
        print(lookup(cls, member))
        print("-" * 90)
