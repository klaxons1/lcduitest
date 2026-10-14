#!/bin/sh
# Builds the MIDlet suite: compiles src/ against the CLDC 1.1 / MIDP 2.1 jars in libs/,
# packages classes + res/ into deployed/raw/lcduitest.jar and writes a matching JAD.
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

OUT=deployed/raw
CLASSES=build/classes
rm -rf "$CLASSES" "$OUT"
mkdir -p "$CLASSES" "$OUT"

BOOT=libs/cldc11.jar:libs/midp21.jar
"$JAVAC" -nowarn -source 1.3 -target 1.3 -encoding UTF-8 \
    -bootclasspath "$BOOT" -d "$CLASSES" src/*.java

# Jar: classes + resources (res/ goes to the jar root, so /lotus.bin and /phantasm-8bit.mid resolve).
cp -R res/. "$CLASSES"/
"$JAR" cfm "$OUT/lcduitest.jar" META-INF/MANIFEST.MF -C "$CLASSES" .

SIZE=$(wc -c < "$OUT/lcduitest.jar" | tr -d ' ')
cat > "$OUT/lcduitest.jad" <<JAD
MicroEdition-Configuration: CLDC-1.1
MicroEdition-Profile: MIDP-2.0
MIDlet-Vendor: Anonymous developer
MIDlet-Version: 0.0.4
MIDlet-Name: Lotus
MIDlet-1: Lotus,,LotusDemo
MIDlet-Jar-URL: lcduitest.jar
MIDlet-Jar-Size: $SIZE
JAD

echo "built $OUT/lcduitest.jar ($SIZE bytes) and $OUT/lcduitest.jad"
