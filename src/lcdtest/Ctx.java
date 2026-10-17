package lcdtest;

import javax.microedition.lcdui.Display;

/**
 * Per-check assertion sink.
 *
 * Strict assertions (eq*, isTrue, throwsEx, ...) represent normative
 * MIDP 2.0 requirements.  Advisory assertions (adv*) represent
 * recommendations, implementation-defined corners, or behaviour that
 * cannot be verified portably; they downgrade a failure to WARN.
 */
public class Ctx {

    static final int MAX_DETAIL = 360;

    final Display display;
    final Check check;
    ProbeCanvas probe;

    final StringBuffer detail = new StringBuffer();
    int assertions;
    int failures;
    int advisories;
    int skips;
    String skipReason;
    boolean aborted;

    Ctx(Display display, Check check) {
        this.display = display;
        this.check = check;
    }

    // ------------------------------------------------------------------
    // Core reporting
    // ------------------------------------------------------------------

    private void issue(boolean advisory, String what, String expected, String actual) {
        if (aborted) {
            return;
        }
        assertions++;
        if (advisory) {
            advisories++;
        } else {
            failures++;
        }
        if (detail.length() < MAX_DETAIL) {
            if (detail.length() > 0) {
                detail.append("; ");
            }
            detail.append(what);
            detail.append(" [expected=").append(expected);
            detail.append(" actual=").append(actual).append(']');
        }
    }

    private void ok() {
        if (!aborted) {
            assertions++;
        }
    }

    public void skip(String why) {
        skips++;
        skipReason = why;
        aborted = true;
    }

    public void warn(String note) {
        if (detail.length() < MAX_DETAIL) {
            if (detail.length() > 0) {
                detail.append("; ");
            }
            detail.append("note: ").append(note);
        }
    }

    public String detailString() {
        StringBuffer b = new StringBuffer(detail.toString());
        if (skips > 0 && skipReason != null && skipReason.length() > 0) {
            if (b.length() > 0) {
                b.append("; ");
            }
            b.append("skipped: ").append(skipReason);
        }
        return b.toString();
    }

    public int status() {
        if (failures > 0) {
            return Fw.FAIL;
        }
        if (skips > 0) {
            return Fw.SKIP;
        }
        if (advisories > 0 || assertions == 0) {
            return Fw.WARN;
        }
        return Fw.PASS;
    }

    // ------------------------------------------------------------------
    // Strict assertions
    // ------------------------------------------------------------------

    public void eqInt(String what, int expected, int actual) {
        if (expected == actual) {
            ok();
        } else {
            issue(false, what, String.valueOf(expected), String.valueOf(actual));
        }
    }

    public void eqStr(String what, String expected, String actual) {
        if (expected == null ? actual == null : expected.equals(actual)) {
            ok();
        } else {
            issue(false, what, quote(expected), quote(actual));
        }
    }

    public void eqBool(String what, boolean expected, boolean actual) {
        if (expected == actual) {
            ok();
        } else {
            issue(false, what, String.valueOf(expected), String.valueOf(actual));
        }
    }

    public void same(String what, Object expected, Object actual) {
        if (expected == actual) {
            ok();
        } else {
            issue(false, what, idOf(expected), idOf(actual));
        }
    }

    public void isTrue(String what, boolean condition) {
        if (condition) {
            ok();
        } else {
            issue(false, what, "true", "false");
        }
    }

    public void isFalse(String what, boolean condition) {
        isTrue(what, !condition);
    }

    public void isNull(String what, Object value) {
        if (value == null) {
            ok();
        } else {
            issue(false, what, "null", String.valueOf(value));
        }
    }

    public void notNull(String what, Object value) {
        if (value != null) {
            ok();
        } else {
            issue(false, what, "non-null", "null");
        }
    }

    public void inRange(String what, int value, int lo, int hi) {
        if (value >= lo && value <= hi) {
            ok();
        } else {
            issue(false, what, "[" + lo + ".." + hi + "]", String.valueOf(value));
        }
    }

    public void oneOf(String what, int value, int[] allowed) {
        for (int i = 0; i < allowed.length; i++) {
            if (value == allowed[i]) {
                ok();
                return;
            }
        }
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < allowed.length; i++) {
            if (i > 0) {
                b.append('/');
            }
            b.append(allowed[i]);
        }
        issue(false, what, b.toString(), String.valueOf(value));
    }

    public void atLeast(String what, int value, int min) {
        if (value >= min) {
            ok();
        } else {
            issue(false, what, ">=" + min, String.valueOf(value));
        }
    }

    public void throwsEx(String what, Class expected, Act act) {
        try {
            act.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) {
                ok();
            } else {
                issue(false, what, expected.getName(), t.getClass().getName());
            }
            return;
        }
        issue(false, what, expected.getName(), "no exception");
    }

    public void throwsOneOf(String what, Class[] expected, Act act) {
        try {
            act.run();
        } catch (Throwable t) {
            for (int i = 0; i < expected.length; i++) {
                if (expected[i].isInstance(t)) {
                    ok();
                    return;
                }
            }
            StringBuffer b = new StringBuffer();
            for (int i = 0; i < expected.length; i++) {
                if (i > 0) {
                    b.append(" or ");
                }
                b.append(expected[i].getName());
            }
            issue(false, what, b.toString(), t.getClass().getName());
            return;
        }
        issue(false, what, expected[0].getName(), "no exception");
    }

    public void noThrow(String what, Act act) {
        try {
            act.run();
            ok();
        } catch (Throwable t) {
            issue(false, what, "no exception", t.getClass().getName());
        }
    }

    // ------------------------------------------------------------------
    // Advisory assertions
    // ------------------------------------------------------------------

    public void advTrue(String what, boolean condition) {
        if (condition) {
            ok();
        } else {
            issue(true, what, "true", "false");
        }
    }

    public void advEqInt(String what, int expected, int actual) {
        if (expected == actual) {
            ok();
        } else {
            issue(true, what, String.valueOf(expected), String.valueOf(actual));
        }
    }

    public void advEqStr(String what, String expected, String actual) {
        if (expected == null ? actual == null : expected.equals(actual)) {
            ok();
        } else {
            issue(true, what, quote(expected), quote(actual));
        }
    }

    public void advSame(String what, Object expected, Object actual) {
        if (expected == actual) {
            ok();
        } else {
            issue(true, what, idOf(expected), idOf(actual));
        }
    }

    public void advInRange(String what, int value, int lo, int hi) {
        if (value >= lo && value <= hi) {
            ok();
        } else {
            issue(true, what, "[" + lo + ".." + hi + "]", String.valueOf(value));
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    static String quote(String s) {
        if (s == null) {
            return "null";
        }
        if (s.length() > 42) {
            s = s.substring(0, 42) + "...";
        }
        return "\"" + s + "\"";
    }

    static String idOf(Object o) {
        if (o == null) {
            return "null";
        }
        return o.getClass().getName() + "@" + Integer.toHexString(o.hashCode());
    }

    public static String exName(Throwable t) {
        return t == null ? "null" : t.getClass().getName();
    }

    public static String exText(Throwable t) {
        if (t == null) {
            return "";
        }
        String m = t.getMessage();
        return exName(t) + (m == null ? "" : ": " + m);
    }
}

