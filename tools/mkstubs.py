#!/usr/bin/env python3
"""Generates compilable stubs of the MIDP 2.0 / CLDC 1.1 API from the
MicroEmulator signature files.

The stubs make it possible to compile the MIDlet without a J2ME SDK: the
signature files are the same description of the API that MicroEmulator uses for
its own conformance tests, so a project that compiles against the stubs also
compiles against midpapi20.jar / cldcapi11.jar in IntelliJ.

Usage:
    python3 tools/mkstubs.py OUTDIR [package.prefix ...]

Default prefix is javax.microedition (the java.* part of CLDC is taken from the
JDK that is on the bootclasspath).  Prints the number of generated files.
"""

import os
import re
import sys
import xml.etree.ElementTree as ET

SIG_DIRS = [
    os.path.expanduser(os.environ.get("MICROEMU_SRC", "~/.local/src/barteo-microemu")),
    "/tmp/microemu/microemu-master/microemulator",
]
SIG_FILES = [
    "api/cldcapi11/src/test/resources/cldcapi11-signature.xml",
    "api/midpapi20/src/test/resources/midpapi20-signature.xml",
]


def default_value(type_name):
    if type_name == "boolean":
        return "false"
    if type_name in ("int", "short", "byte"):
        return "0"
    if type_name == "long":
        return "0L"
    if type_name == "char":
        return "(char) 0"
    if type_name == "float":
        return "0.0f"
    if type_name == "double":
        return "0.0d"
    if type_name == "String":
        return "null"
    return "null"


def constant_value(type_name, raw):
    if raw is None:
        return default_value(type_name)
    text = raw.strip()
    if type_name == "java.lang.String":
        return '"%s"' % text.replace('\\', '\\\\').replace('"', '\\"')
    if type_name == "char":
        return "(char) %s" % text
    if type_name == "long":
        return "%sL" % text
    return text


def parameter_list(element):
    parts = []
    for index, param in enumerate(element.findall("parameter")):
        parts.append("%s p%d" % (param.get("type"), index))
    return ", ".join(parts)


def throws_list(element, known):
    names = [e.get("name") for e in element.findall("exception")]
    names = [n for n in names if n in known]
    if not names:
        return ""
    return " throws " + ", ".join(names)


def emit_class(element, known, is_interface, out_dir):
    name = element.get("name")
    modifiers = element.get("modifiers", "")
    package, _, simple = name.rpartition(".")
    lines = []
    if package:
        lines.append("package %s;" % package)
        lines.append("")
    header = "%s %s %s" % (modifiers, "interface" if is_interface else "class", simple)
    super_name = element.get("extends")
    if super_name:
        header += " extends %s" % super_name
    implemented = []
    for child in element:
        if child.tag == "implements":
            for iface in child:
                implemented.append(iface.get("name"))
    if implemented:
        if is_interface:
            header += " extends %s" % ", ".join(implemented)
        else:
            header += " implements %s" % ", ".join(implemented)
    lines.append(header.strip() + " {")
    fields = [c for c in element if c.tag == "field"]
    ctors = [c for c in element if c.tag == "constructor"]
    methods = [c for c in element if c.tag == "method"]
    for field in fields:
        type_name = field.get("type")
        lines.append("    %s %s %s = %s;" % (field.get("modifiers", ""), type_name,
                                             field.get("name"),
                                             constant_value(type_name,
                                                            field.get("constant-value"))))
    if not is_interface and not any(len(c.findall("parameter")) == 0 for c in ctors):
        # a generated constructor has an empty body, so a subclass in the same
        # package needs a zero argument constructor to call
        lines.append("    /* package */ %s() { }" % simple)
    for ctor in ctors:
        mods = ctor.get("modifiers", "public")
        if is_interface:
            lines.append("    /* no constructor in an interface */")
        else:
            lines.append("    %s %s(%s)%s { }" % (mods, simple, parameter_list(ctor),
                                                   throws_list(ctor, known)))
    for method in methods:
        mods = method.get("modifiers", "")
        return_type = method.get("return")
        signature = "    %s %s %s(%s)%s" % (mods, return_type, method.get("name"),
                                           parameter_list(method), throws_list(method, known))
        if is_interface or "abstract" in mods or "native" in mods:
            lines.append(signature + ";")
        else:
            body = "" if return_type == "void" else " return %s;" % default_value(return_type)
            lines.append(signature + " {%s }" % body)
    lines.append("}")
    path = os.path.join(out_dir, *(package.split(".") if package else []), simple + ".java")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as handle:
        handle.write("\n".join(lines) + "\n")
    return path


def main(argv):
    if len(argv) < 2:
        sys.stderr.write(__doc__)
        return 2
    out_dir = argv[1]
    prefixes = argv[2:] or ["javax.microedition"]
    elements = []
    for directory in SIG_DIRS:
        for relative in SIG_FILES:
            path = os.path.join(directory, relative)
            if os.path.exists(path):
                elements.extend([e for e in ET.parse(path).getroot()
                                 if e.tag in ("class", "interface")])
        if elements:
            break
    known = set(e.get("name") for e in elements)
    count = 0
    for element in elements:
        name = element.get("name")
        if not any(name.startswith(prefix + ".") or name == prefix for prefix in prefixes):
            continue
        emit_class(element, known, element.tag == "interface", out_dir)
        count += 1
    print("generated %d stub file(s) in %s" % (count, out_dir))
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
