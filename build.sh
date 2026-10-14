#!/bin/sh
# Builds the MIDlet suite: compiles src/ against the CLDC 1.1 / MIDP 2.1 jars in libs/, runs the
# CLDC preverifier over the classes, packages them + res/ into deployed/raw/lcduitest.jar and writes
# a matching JAD.
#
# The preverifier is built from klaxons1/j2me_cldc by tools/fetch_j2me_tools.sh on first use
# (into build/j2me/preverify). Set PREVERIFY=/path/to/preverify to use another one.
#
# Usage: ./build.sh            (uses javac from PATH or $JAVA_HOME/bin)
#        JAVA_HOME=/path/to/jdk8 ./build.sh
# Requires a JDK 8 (-source/-target 1.3 is what the CLDC toolchain expects).
set -e
cd "$(dirname "$0")"

if [ -n "$JAVA_HOME" ]; then
    JAVAC="$JAVA_HOME/bin/javac"
    JAR="$JAVA_HOME/bin/jar"
else
    JAVAC=javac
    JAR=jar
fi

PREVERIFY=${PREVERIFY:-build/j2me/preverify}
if [ ! -x "$PREVERIFY" ]; then
    tools/fetch_j2me_tools.sh
fi

OUT=deployed/raw
CLASSES=build/classes
PREVERIFIED=build/preverified
LIBS=build/libs-unz
rm -rf "$CLASSES" "$PREVERIFIED" "$OUT" "$LIBS"
mkdir -p "$CLASSES" "$PREVERIFIED" "$OUT" "$LIBS/cldc" "$LIBS/midp"

BOOT=libs/cldc11.jar:libs/midp21.jar
"$JAVAC" -nowarn -source 1.3 -target 1.3 -encoding UTF-8 \
    -bootclasspath "$BOOT" -d "$CLASSES" src/*.java

# Preverify against the unpacked libraries (the jars give the same output plus CRC warnings).
(cd "$LIBS/cldc" && "$JAR" xf ../../../libs/cldc11.jar)
(cd "$LIBS/midp" && "$JAR" xf ../../../libs/midp21.jar)
"$PREVERIFY" -classpath "$LIBS/cldc:$LIBS/midp" -d "$PREVERIFIED" "$CLASSES"

# Jar: preverified classes + resources (res/ goes to the jar root, so /lotus.bin and /phantasm-8bit.mid resolve).
cp -R res/. "$PREVERIFIED"/
"$JAR" cfm "$OUT/lcduitest.jar" META-INF/MANIFEST.MF -C "$PREVERIFIED" .

SIZE=$(wc -c < "$OUT/lcduitest.jar" | tr -d ' ')
cat > "$OUT/lcduitest.jad" <<JAD
MicroEdition-Configuration: CLDC-1.1
MicroEdition-Profile: MIDP-2.0
MIDlet-Vendor: Anonymous developer
MIDlet-Version: 0.0.10
MIDlet-Name: Lotus
MIDlet-1: Lotus,,LotusDemo
MIDlet-Jar-URL: lcduitest.jar
MIDlet-Jar-Size: $SIZE
JAD

echo "built $OUT/lcduitest.jar ($SIZE bytes) and $OUT/lcduitest.jad"
