#!/bin/sh
# Runs the LOT7 decoder (src/LotusFilm.java) on the CLDC KVM and on the desktop JVM, and checks that
# both give the same checksum over a fixed sequence of seeks (see tools/kvm/KvmCheck.java).
# Needs build/j2me/{preverify,kvm}: run tools/fetch_j2me_tools.sh with KVM=1 first.
#
# Usage: tools/kvm/check_film.sh
set -e
cd "$(dirname "$0")/../.."
ROOT=$PWD
JAVAC=${JAVA_HOME:+$JAVA_HOME/bin/}javac
JAVA=${JAVA_HOME:+$JAVA_HOME/bin/}java
JAR=${JAVA_HOME:+$JAVA_HOME/bin/}jar
PREVERIFY=$ROOT/build/j2me/preverify
KVM=$ROOT/build/j2me/kvm
for t in "$PREVERIFY" "$KVM"; do
    if [ ! -x "$t" ]; then
        echo "check_film: $t missing, run KVM=1 tools/fetch_j2me_tools.sh" >&2
        exit 1
    fi
done

B=$ROOT/build/film-check
rm -rf "$B"
mkdir -p "$B/cls" "$B/pv" "$B/desk" "$B/cldc"
(cd "$B/cldc" && "$JAR" xf "$ROOT/libs/cldc11.jar")

# CLDC side: compile to 1.3 bytecode against the CLDC API, preverify, run in the KVM.
"$JAVAC" -nowarn -source 1.3 -target 1.3 -bootclasspath "$ROOT/libs/cldc11.jar" \
    -d "$B/cls" "$ROOT/src/LotusFilm.java" "$ROOT/tools/kvm/KvmCheck.java"
"$PREVERIFY" -classpath "$B/cldc" -d "$B/pv" "$B/cls"
cp "$ROOT/res/lotus.bin" "$B/pv/"
KVM_OUT=$(cd "$B/pv" && timeout 300 "$KVM" -heapsize 8m KvmCheck)
echo "kvm:     $KVM_OUT"

# Desktop side: same sources, normal JVM, resource from res/.
"$JAVAC" -nowarn -d "$B/desk" "$ROOT/src/LotusFilm.java" "$ROOT/tools/kvm/KvmCheck.java"
DESK_OUT=$("$JAVA" -cp "$B/desk:$ROOT/res" KvmCheck)
echo "desktop: $DESK_OUT"

K=$(echo "$KVM_OUT" | sed -n 's/.*checksum=//p')
D=$(echo "$DESK_OUT" | sed -n 's/.*checksum=//p')
if [ -z "$K" ] || [ "$K" != "$D" ]; then
    echo "check_film: FAIL, checksums differ" >&2
    exit 1
fi
echo "check_film: OK (checksum $K)"
