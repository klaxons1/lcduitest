package lcdtest;

import java.util.Vector;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;

/**
 * Aggregate of all results plus report rendering.
 */
public class Report {

    private final Vector results = new Vector();
    private final Vector known = new Vector();
    private String envText = "";
    private final String timestamp;

    Report(String timestamp) {
        this.timestamp = timestamp;
    }

    void know(Check check) {
        known.addElement(check);
    }

    void add(Result r) {
        results.addElement(r);
    }

    Result find(String id) {
        for (int i = 0; i < results.size(); i++) {
            Result r = (Result) results.elementAt(i);
            if (r.check.id.equals(id)) {
                return r;
            }
        }
        return null;
    }

    void env(String text) {
        envText = text == null ? "" : text;
    }

    String env() {
        return envText;
    }

    String timestamp() {
        return timestamp;
    }

    Vector results() {
        return results;
    }

    Vector known() {
        return known;
    }

    int count(int status) {
        int n = 0;
        for (int i = 0; i < results.size(); i++) {
            if (((Result) results.elementAt(i)).status == status) {
                n++;
            }
        }
        return n;
    }

    int total() {
        return results.size();
    }

    /** Group statistics: name -> int[]{pass,fail,warn,skip,total}. */
    java.util.Hashtable byGroup() {
        java.util.Hashtable h = new java.util.Hashtable();
        for (int i = 0; i < results.size(); i++) {
            Result r = (Result) results.elementAt(i);
            int[] s = (int[]) h.get(r.check.group);
            if (s == null) {
                s = new int[5];
                h.put(r.check.group, s);
            }
            s[4]++;
            if (r.status >= 0 && r.status <= 3) {
                s[r.status]++;
            }
        }
        return h;
    }

    String verdict() {
        int fail = count(Fw.FAIL);
        int warn = count(Fw.WARN);
        if (fail > 0) {
            return "NOT CONFORMANT";
        }
        if (warn > 0) {
            return "PASS WITH WARNINGS";
        }
        return "CONFORMANT";
    }

    /** Compact machine-readable report. */
    String toMachine(boolean catalog) {
        StringBuffer b = new StringBuffer(16384);
        b.append("LCDUITEST|1.0\n");
        b.append("WHEN|").append(timestamp).append('\n');
        if (envText.length() > 0) {
            b.append("ENV|").append(Fw.oneLine(envText.replace('\n', ' '))).append('\n');
        }
        for (int i = 0; i < known.size(); i++) {
            Check c = (Check) known.elementAt(i);
            if (catalog) {
                b.append("CHECK|").append(c.id).append('|').append(c.group).append('|')
                        .append(Fw.kindName(c.kind)).append('|')
                        .append(Fw.oneLine(c.title)).append('\n');
            }
        }
        for (int i = 0; i < results.size(); i++) {
            Result r = (Result) results.elementAt(i);
            b.append("R|").append(r.check.id).append('|')
                    .append(Fw.statusName(r.status)).append('|')
                    .append(Fw.oneLine(r.check.title)).append('|')
                    .append(Fw.oneLine(r.detail)).append('\n');
        }
        b.append("S|").append(count(Fw.PASS)).append('|').append(count(Fw.FAIL))
                .append('|').append(count(Fw.WARN)).append('|').append(count(Fw.SKIP))
                .append('|').append(total()).append('|').append(Fw.oneLine(verdict())).append('\n');
        b.append("END\n");
        return b.toString();
    }

    /** Human-readable report for the screen / log. */
    String toHuman() {
        StringBuffer b = new StringBuffer(8192);
        b.append("LCDUI Conformance Suite 1.0\n");
        b.append("Verdict: ").append(verdict()).append('\n');
        b.append("Pass ").append(count(Fw.PASS)).append("  Fail ").append(count(Fw.FAIL))
                .append("  Warn ").append(count(Fw.WARN)).append("  Skip ").append(count(Fw.SKIP))
                .append("  Total ").append(total()).append("\n");
        if (envText.length() > 0) {
            b.append('\n').append(envText).append('\n');
        }
        java.util.Hashtable h = byGroup();
        b.append("\nGroups\n");
        java.util.Vector names = new java.util.Vector();
        java.util.Enumeration e = h.keys();
        while (e.hasMoreElements()) {
            names.addElement(e.nextElement());
        }
        sortStrings(names);
        for (int i = 0; i < names.size(); i++) {
            String g = (String) names.elementAt(i);
            int[] s = (int[]) h.get(g);
            b.append("  ").append(pad(g, 10)).append(" ").append(s[0]).append('/')
                    .append(s[1]).append('/').append(s[2]).append('/').append(s[3])
                    .append(" of ").append(s[4]).append('\n');
        }
        b.append("\nFailures\n");
        int shown = 0;
        for (int i = 0; i < results.size(); i++) {
            Result r = (Result) results.elementAt(i);
            if (r.status == Fw.FAIL) {
                shown++;
                b.append("  ").append(r.check.id).append(' ').append(Fw.oneLine(r.check.title));
                if (r.detail.length() > 0) {
                    b.append("\n      ").append(Fw.oneLine(r.detail));
                }
                b.append('\n');
            }
        }
        if (shown == 0) {
            b.append("  none\n");
        }
        b.append("\nWarnings\n");
        shown = 0;
        for (int i = 0; i < results.size(); i++) {
            Result r = (Result) results.elementAt(i);
            if (r.status == Fw.WARN) {
                shown++;
                b.append("  ").append(r.check.id).append(' ').append(Fw.oneLine(r.check.title));
                if (r.detail.length() > 0) {
                    b.append("\n      ").append(Fw.oneLine(r.detail));
                }
                b.append('\n');
            }
        }
        if (shown == 0) {
            b.append("  none\n");
        }
        b.append("\nLegend: .=pass X=fail !=warn -=skip; ").append(timestamp).append('\n');
        return b.toString();
    }

    static String pad(String s, int n) {
        StringBuffer b = new StringBuffer(s);
        while (b.length() < n) {
            b.append(' ');
        }
        return b.toString();
    }

    static void sortStrings(Vector v) {
        for (int i = 1; i < v.size(); i++) {
            Object key = v.elementAt(i);
            int j = i - 1;
            while (j >= 0 && ((String) v.elementAt(j)).compareTo((String) key) > 0) {
                v.setElementAt(v.elementAt(j), j + 1);
                j--;
            }
            v.setElementAt(key, j + 1);
        }
    }
}
