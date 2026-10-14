#!/bin/sh
# Builds the CLDC toolchain for this Linux host from klaxons1/j2me_cldc:
#   build/j2me/preverify   CLDC preverifier (tools/preverifier), used by build.sh
#   build/j2me/kvm         CLDC 1.1 virtual machine (kvm/VmUnix), only with KVM=1,
#                          used by tools/kvm/check_film.sh
#
# Nothing from that repository is vendored: it is cloned (depth 1) into build/j2me_cldc.
# The sources need a few portability fixes for a modern 64-bit gcc and JDK 8; they are applied
# to the clone here, not to this repository.
#
# Usage: tools/fetch_j2me_tools.sh            preverifier only
#        KVM=1 tools/fetch_j2me_tools.sh      preverifier and KVM
# Needs: git, gcc, make, and a JDK 8 (JAVA_HOME, or javac on PATH).
set -e
cd "$(dirname "$0")/.."

REPO_URL=https://github.com/klaxons1/j2me_cldc.git
ROOT=$PWD/build
SRC=$ROOT/j2me_cldc
OUT=$ROOT/j2me
BIN=$OUT/bin

if [ -n "$JAVA_HOME" ]; then
    JAVAC_REAL=$JAVA_HOME/bin/javac
else
    JAVAC_REAL=$(command -v javac || true)
fi
if [ -z "$JAVAC_REAL" ] || [ ! -x "$JAVAC_REAL" ]; then
    echo "fetch_j2me_tools: no javac found (set JAVA_HOME to a JDK 8)" >&2
    exit 1
fi

mkdir -p "$BIN"
if [ ! -d "$SRC/.git" ]; then
    rm -rf "$SRC"
    git clone --depth 1 -q "$REPO_URL" "$SRC"
fi

# gcc-3.3 is the name the upstream makefiles call. The sources rely on tentative definitions
# (-fcommon) and do not build with modern gcc otherwise.
cat > "$BIN/gcc-3.3" <<'EOF'
#!/bin/sh
exec gcc -fcommon -w -fno-strict-aliasing "$@"
EOF
# The KVM makefiles pass -target jsr14; JDK 8 does not know it, and 1.4 bytecode is what it means.
cat > "$BIN/javac" <<EOF
#!/bin/sh
n=\$#; i=0
while [ \$i -lt \$n ]; do
    a=\$1; shift
    case "\$a" in jsr14) a=1.4 ;; esac
    set -- "\$@" "\$a"
    i=\$((i + 1))
done
exec "$JAVAC_REAL" "\$@"
EOF
chmod +x "$BIN/gcc-3.3" "$BIN/javac"
export PATH="$BIN:$(dirname "$JAVAC_REAL"):$PATH"

# 1. Preverifier, 64-bit fix: UCALIGN cast a pointer to int, which crashes on 64-bit hosts
#    (segfault in instruction_length while checking the api classes).
OOBJ=$SRC/tools/preverifier/src/oobj.h
if grep -q 'ALIGN((int)(n))' "$OOBJ"; then
    sed -i 's/ALIGN((int)(n))/ALIGN((long)(n))/' "$OOBJ"
fi

PVDIR=$SRC/tools/preverifier/build/linux
make -C "$PVDIR" clean >/dev/null
rm -f "$PVDIR/preverify"
make -C "$PVDIR"
cp "$PVDIR/preverify" "$OUT/preverify"
echo "built $OUT/preverify"

if [ "$KVM" != "1" ]; then
    exit 0
fi

# 2. KVM build needs the preverifier binary in place (the makefile checks for it).
cp "$OUT/preverify" "$PVDIR/preverify"

# 3. tools/jcc uses sun.misc.Compare and sun.misc.Sort (JDK 1.4 internals, gone from JDK 8).
#    Replacements: Sort is a stable merge sort. Only the order of constant-pool entries depends on it.
JCC_SUN=$SRC/tools/jcc/src/sun/misc
mkdir -p "$JCC_SUN"
cat > "$JCC_SUN/Compare.java" <<'EOF'
package sun.misc;

/** Stand-in for the JDK 1.4 internal sun.misc.Compare (build-only). */
public interface Compare {
    int doCompare(Object o1, Object o2);
}
EOF
cat > "$JCC_SUN/Sort.java" <<'EOF'
package sun.misc;

/** Stand-in for the JDK 1.4 internal sun.misc.Sort (build-only): stable merge sort. */
public class Sort {
    public static void quicksort(Object[] a, Compare c) {
        if (a == null || a.length < 2) return;
        Object[] tmp = new Object[a.length];
        msort(a, tmp, 0, a.length, c);
    }

    private static void msort(Object[] a, Object[] tmp, int lo, int hi, Compare c) {
        if (hi - lo < 2) return;
        int mid = (lo + hi) >>> 1;
        msort(a, tmp, lo, mid, c);
        msort(a, tmp, mid, hi, c);
        int i = lo, j = mid, k = lo;
        while (i < mid && j < hi) {
            if (c.doCompare(a[j], a[i]) < 0) tmp[k++] = a[j++];
            else tmp[k++] = a[i++];
        }
        while (i < mid) tmp[k++] = a[i++];
        while (j < hi) tmp[k++] = a[j++];
        System.arraycopy(tmp, lo, a, lo, hi - lo);
    }
}
EOF

# 4. verifierUtil.c declares static functions inside blocks, which modern gcc rejects.
VU=$SRC/kvm/VmCommon/src/verifierUtil.c
if grep -q '^    static int Vfy_verifyMethod(METHOD vMethod);$' "$VU"; then
    sed -i '/^    static int Vfy_verifyMethod(METHOD vMethod);$/d; /^    static bool_t Vfy_checkNewInstructions(METHOD vMethod);$/d' "$VU"
    sed -i 's/^int verifyClass(INSTANCE_CLASS thisClass) {$/static int Vfy_verifyMethod(METHOD vMethod);\nstatic bool_t Vfy_checkNewInstructions(METHOD vMethod);\n\nint verifyClass(INSTANCE_CLASS thisClass) {/' "$VU"
fi

# 5. KVM. The default build has ROMIZING on: jcc turns api/classes.zip into a ROM image.
KVMDIR=$SRC/kvm/VmUnix/build
make -C "$KVMDIR" clean >/dev/null 2>&1 || true
rm -rf "$KVMDIR/obj" "$KVMDIR/fp_obj" "$KVMDIR/kvm"
make -C "$KVMDIR" PLATFORM=linux GCC=true
cp "$KVMDIR/kvm" "$OUT/kvm"
echo "built $OUT/kvm"
