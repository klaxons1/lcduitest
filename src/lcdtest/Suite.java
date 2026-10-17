package lcdtest;

import java.util.Vector;

import lcdtest.checks.AlertChecks;
import lcdtest.checks.CanvasChecks;
import lcdtest.checks.ChoiceChecks;
import lcdtest.checks.CommandChecks;
import lcdtest.checks.DisplayChecks;
import lcdtest.checks.FontChecks;
import lcdtest.checks.FormChecks;
import lcdtest.checks.GaugeChecks;
import lcdtest.checks.GraphicsChecks;
import lcdtest.checks.ImageChecks;
import lcdtest.checks.ItemChecks;
import lcdtest.checks.ListChecks;
import lcdtest.checks.ManualChecks;
import lcdtest.checks.RasterChecks;
import lcdtest.checks.SimpleWidgetChecks;
import lcdtest.checks.TextChecks;
import lcdtest.checks.WidgetChecks;

/**
 * The complete catalogue of checks.  Registration order is the order in
 * which checks are presented and executed.
 */
public class Suite {

    private final Vector checks = new Vector();
    private final Vector groups = new Vector();

    public void add(String id, String group, String title, String spec, Body body) {
        add(id, group, title, spec, Fw.SYNC, body);
    }

    public void add(String id, String group, String title, String spec, int kind, Body body) {
        checks.addElement(new Check(id, group, title, spec, kind, body));
        if (!groups.contains(group)) {
            groups.addElement(group);
        }
    }

    public void live(String id, String group, String title, String spec, Body body) {
        add(id, group, title, spec, Fw.LIVE, body);
    }

    public void live(String id, String group, String title, String spec) {
        add(id, group, title, spec, Fw.LIVE, null);
    }

    public void manual(String id, String group, String title, String spec) {
        add(id, group, title, spec, Fw.MANUAL, null);
    }

    public Vector checks() {
        return checks;
    }

    public Vector groups() {
        return groups;
    }

    public int size() {
        return checks.size();
    }

    public Check byId(String id) {
        for (int i = 0; i < checks.size(); i++) {
            Check c = (Check) checks.elementAt(i);
            if (c.id.equals(id)) {
                return c;
            }
        }
        return null;
    }

    public Vector checksOfKind(int kind) {
        Vector v = new Vector();
        for (int i = 0; i < checks.size(); i++) {
            Check c = (Check) checks.elementAt(i);
            if (c.kind == kind) {
                v.addElement(c);
            }
        }
        return v;
    }

    public Vector checksIn(String group) {
        Vector v = new Vector();
        for (int i = 0; i < checks.size(); i++) {
            Check c = (Check) checks.elementAt(i);
            if (c.group.equals(group)) {
                v.addElement(c);
            }
        }
        return v;
    }

    public Vector syncIn(String group) {
        Vector v = new Vector();
        for (int i = 0; i < checks.size(); i++) {
            Check c = (Check) checks.elementAt(i);
            if (c.group.equals(group) && c.kind == Fw.SYNC) {
                v.addElement(c);
            }
        }
        return v;
    }

    public static Suite build() {
        Suite s = new Suite();
        DisplayChecks.register(s);
        CommandChecks.register(s);
        CanvasChecks.registerSync(s);
        GraphicsChecks.register(s);
        RasterChecks.register(s);
        FontChecks.register(s);
        ImageChecks.register(s);
        FormChecks.register(s);
        ItemChecks.register(s);
        TextChecks.register(s);
        ChoiceChecks.register(s);
        ListChecks.register(s);
        AlertChecks.register(s);
        GaugeChecks.register(s);
        WidgetChecks.register(s);
        SimpleWidgetChecks.register(s);
        CanvasChecks.registerLive(s);
        ManualChecks.register(s);
        return s;
    }
}
