package lcdtest;

import java.util.Vector;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;

/**
 * Minimal conformance-test framework for CLDC 1.1 / MIDP 2.0.
 *
 * Designed for the constraints of J2ME: no generics, no annotations,
 * no java.util collections beyond Vector/Hashtable, and no assumptions
 * about threads.  Every check is isolated: an exception thrown by the
 * code under test is recorded as a failure, never allowed to abort the
 * suite.
 */
public final class Fw {

    public static final int PASS = 0;
    public static final int FAIL = 1;
    public static final int WARN = 2;
    public static final int SKIP = 3;

    public static final int SYNC = 0;
    public static final int LIVE = 1;
    public static final int MANUAL = 2;

    private Fw() {
    }

    public static String statusName(int status) {
        switch (status) {
            case PASS: return "PASS";
            case FAIL: return "FAIL";
            case WARN: return "WARN";
            case SKIP: return "SKIP";
            default: return "?";
        }
    }

    public static String statusChar(int status) {
        switch (status) {
            case PASS: return ".";
            case FAIL: return "X";
            case WARN: return "!";
            case SKIP: return "-";
            default: return "?";
        }
    }

    public static String kindName(int kind) {
        switch (kind) {
            case SYNC: return "sync";
            case LIVE: return "live";
            case MANUAL: return "manual";
            default: return "?";
        }
    }

    public static String oneLine(String s) {
        if (s == null) {
            return "";
        }
        StringBuffer b = new StringBuffer(s.length());
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == '\n' || ch == '\r' || ch == '|') {
                b.append('/');
            } else if (ch < 32) {
                b.append(' ');
            } else {
                b.append(ch);
            }
        }
        return b.toString();
    }
}

