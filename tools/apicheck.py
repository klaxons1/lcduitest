#!/usr/bin/env python3
"""Checks the sources against the CLDC 1.1 and MIDP 2.0 API signature files.

The IntelliJ J2ME SDK compiles the project against the real cldcapi11.jar and
midpapi20.jar, so a call that exists in the desktop JDK but not in CLDC (for
example Calendar.set(year, month, day, hour, minute)) is a compile error even
though it looks perfectly normal.  The signature files that MicroEmulator uses
for its own API conformance tests list the exact surface of both APIs, so the
same surface can be checked here without a J2ME compiler.

Usage:
    python3 tools/apicheck.py [file ...]

Without arguments every .java file under src/ is checked.  The signature files
are looked up in $MICROEMU_SRC, default ~/.local/src/barteo-microemu.
"""

import os
import re
import sys
import xml.etree.ElementTree as ET

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
SIG_DIRS = [
    os.path.expanduser(os.environ.get("MICROEMU_SRC", "~/.local/src/barteo-microemu")),
    "/tmp/microemu/microemu-master/microemulator",
]
SIG_FILES = [
    "api/cldcapi11/src/test/resources/cldcapi11-signature.xml",
    "api/midpapi20/src/test/resources/midpapi20-signature.xml",
]

# Methods of java.lang.Object that every class inherits.
OBJECT_METHODS = {"equals": 1, "hashCode": 0, "toString": 0, "getClass": 0,
                  "notify": 0, "notifyAll": 0, "wait": 0}

PRIMITIVE_DEFAULTS = {"int", "long", "short", "byte", "char", "boolean", "float",
                      "double", "void"}


class ApiClass(object):

    def __init__(self, name, modifiers, is_interface):
        self.name = name
        self.modifiers = modifiers
        self.is_interface = is_interface
        self.super_name = None
        self.interfaces = []
        self.methods = {}          # name -> set of arities
        self.fields = {}           # name -> type
        self.ctor_arities = set()

    def add_method(self, name, arity):
        self.methods.setdefault(name, set()).add(arity)

    def __repr__(self):
        return "<%s %s>" % ("interface" if self.is_interface else "class", self.name)


def load_signatures():
    classes = {}
    for directory in SIG_DIRS:
        found = False
        for relative in SIG_FILES:
            path = os.path.join(directory, relative)
            if os.path.exists(path):
                found = True
                parse_signature(path, classes)
        if found:
            return classes, directory
    sys.stderr.write("signature files not found, looked in %s\n" % ", ".join(SIG_DIRS))
    sys.exit(2)


def parse_signature(path, classes):
    root = ET.parse(path).getroot()
    for element in root:
        if element.tag not in ("class", "interface"):
            continue
        name = element.get("name")
        api = ApiClass(name, element.get("modifiers", ""), element.tag == "interface")
        api.super_name = element.get("extends")
        for child in element:
            if child.tag == "implements":
                for iface in child:
                    api.interfaces.append(iface.get("name"))
            elif child.tag == "constructor":
                api.ctor_arities.add(count_parameters(child))
            elif child.tag == "method":
                api.add_method(child.get("name"), count_parameters(child))
            elif child.tag == "field":
                api.fields[child.get("name")] = child.get("type")
        classes[name] = api


def count_parameters(element):
    return len(element.findall("parameter"))


def build_simple_names(classes):
    simple = {}
    for name, api in classes.items():
        simple.setdefault(name.rsplit(".", 1)[-1], []).append(name)
    return simple


def chain(classes, fqn):
    """The class itself, its superclasses and all its interfaces."""
    seen = []
    todo = [fqn]
    while todo:
        current = todo.pop(0)
        if current is None or current in seen:
            continue
        api = classes.get(current)
        if api is None:
            continue
        seen.append(current)
        todo.append(api.super_name)
        todo.extend(api.interfaces)
    return seen


def lookup_method(classes, fqn, name, arity):
    for candidate in chain(classes, fqn):
        api = classes[candidate]
        if name in api.methods and arity in api.methods[name]:
            return True, None
    if name in OBJECT_METHODS and arity == OBJECT_METHODS[name]:
        return True, None
    for candidate in chain(classes, fqn):
        api = classes[candidate]
        if name in api.methods:
            return False, "arity %d, expected %s" % (arity, sorted(api.methods[name]))
    return False, "no such method"


def lookup_field(classes, fqn, name):
    for candidate in chain(classes, fqn):
        api = classes[candidate]
        if name in api.fields:
            return True
    return False


IMPORT = re.compile(r"^\s*import\s+([\w.]+)\s*;", re.M)
DECL = re.compile(r"\b([A-Za-z_][\w.]*)\s+([a-z_]\w*)\s*(?=[=;,)])")
CALL = re.compile(r"(?<![\w.])([A-Za-z_][\w.]*)\s*\.\s*(\w+)\s*\(")
CONST = re.compile(r"(?<![\w.])([A-Za-z_][\w.]*)\s*\.\s*([A-Z][A-Z_0-9]{2,})\b(?!\s*\()")


def blank(match):
    """Same length as the match so line numbers are preserved, but not spaces:
    a blanked string still counts as a real argument."""
    return re.sub(r"[^\n]", "x", match.group(0))


def strip_source(text):
    text = re.sub(r"/\*.*?\*/", blank, text, flags=re.S)
    text = re.sub(r"//[^\n]*", blank, text)
    text = re.sub(r'"(\\.|[^"\\])*"', blank, text)
    return text


def count_args(text, start):
    """Number of top level arguments of the call whose '(' is at start."""
    depth = 0
    args = 0
    has_content = False
    index = start
    while index < len(text):
        char = text[index]
        if char in "([{":
            depth += 1
            if depth == 1:
                has_content = False
            index += 1
            continue
        if char in ")]}":
            depth -= 1
            if depth <= 0:
                return 0 if not has_content else args + 1
            index += 1
            continue
        if depth == 1:
            if char == ",":
                args += 1
            elif not char.isspace():
                has_content = True
        index += 1
    return 0 if not has_content else args + 1


def check_file(path, classes, simple, report):
    text = strip_source(open(path).read())
    mapping = {}
    for imported in IMPORT.findall(text):
        short = imported.rsplit(".", 1)[-1]
        mapping[short] = imported
    # types used by the file: local variables, parameters and fields, with the
    # line they are declared on so that a name reused in another test case is
    # resolved to the declaration that is in scope
    line_starts = []
    offset = 0
    for line in text.split("\n"):
        line_starts.append(offset)
        offset += len(line) + 1
    for match in DECL.finditer(text):
        type_name, var = match.group(1), match.group(2)
        resolved = mapping.get(type_name) or type_name
        line = text.count("\n", 0, match.start())
        # an unknown type is recorded as None: the name shadows anything that
        # was declared earlier in the file but is out of scope here
        mapping.setdefault(var, []).append((line, resolved if resolved in classes else None))
    for var in mapping:
        if isinstance(mapping[var], list):
            mapping[var].sort()

    check_calls(path, text, classes, simple, mapping, report)
    check_constants(path, text, classes, simple, mapping, report)


def resolve(receiver, classes, simple, mapping, line):
    declared = mapping.get(receiver)
    if isinstance(declared, list):
        best = None
        for decl_line, fqn in declared:
            if decl_line <= line:
                best = fqn
        return best
    if isinstance(declared, str) and declared in classes:
        return declared
    if receiver in classes:
        return receiver
    if receiver in classes:
        return receiver
    candidates = simple.get(receiver)
    if candidates and len(candidates) == 1:
        return candidates[0]
    return None


def check_calls(path, text, classes, simple, mapping, report):
    for match in CALL.finditer(text):
        receiver, member = match.group(1), match.group(2)
        line = text.count("\n", 0, match.start())
        fqn = resolve(receiver, classes, simple, mapping, line)
        if fqn is None:
            continue
        arity = count_args(text, match.end() - 1)
        ok, why = lookup_method(classes, fqn, member, arity)
        if not ok:
            report.append((path, line + 1, fqn, member, arity, why))


def check_constants(path, text, classes, simple, mapping, report):
    for match in CONST.finditer(text):
        receiver, member = match.group(1), match.group(2)
        line = text.count("\n", 0, match.start())
        fqn = resolve(receiver, classes, simple, mapping, line)
        if fqn is None:
            continue
        if not lookup_field(classes, fqn, member):
            report.append((path, line + 1, fqn, member, None, "no such field"))


def main(argv):
    classes, directory = load_signatures()
    simple = build_simple_names(classes)
    files = argv[1:]
    if not files:
        for base, _dirs, names in os.walk(os.path.join(ROOT, "src")):
            for name in sorted(names):
                if name.endswith(".java"):
                    files.append(os.path.join(base, name))
    files.sort()
    report = []
    for path in files:
        check_file(path, classes, simple, report)
    print("signatures: %s" % directory)
    print("checking %d file(s) against %d API classes" % (len(files), len(classes)))
    if not report:
        print("no API surface problems found")
        return 0
    print("%d problem(s):" % len(report))
    for path, line, fqn, member, arity, why in report:
        where = "%s:%d" % (os.path.relpath(path, ROOT), line)
        if arity is None:
            print("  %s  %s.%s - %s" % (where, fqn, member, why))
        else:
            print("  %s  %s.%s(%d args) - %s" % (where, fqn, member, arity, why))
    return 1


if __name__ == "__main__":
    sys.exit(main(sys.argv))
