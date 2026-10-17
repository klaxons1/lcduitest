package lcdtest;

import java.util.Vector;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/** Main menu and simple hierarchical list browser. */
public final class MenuCanvas extends Canvas implements CommandListener {

    interface Action {
        void run();
    }

    static final class Entry {
        final String label;
        final Action action;

        Entry(String label, Action action) {
            this.label = label;
            this.action = action;
        }
    }

    static final class State {
        final String title;
        final Vector entries;
        int selected;
        int top;

        State(String title, Vector entries) {
            this.title = title;
            this.entries = entries;
        }
    }

    private final Display display;
    private final Suite suite;
    private Runner runner;
    private final Vector stack = new Vector();
    private final Command okCommand = new Command("Select", Command.OK, 1);
    private final Command backCommand = new Command("Back", Command.BACK, 2);
    private final Command exitCommand = new Command("Exit", Command.EXIT, 3);
    private String status = "";
    private int pressedRow = -1;

    public MenuCanvas(Display display, Suite suite) {
        this.display = display;
        this.suite = suite;
        setTitle("LCDUI Conformance");
        addCommand(okCommand);
        addCommand(backCommand);
        addCommand(exitCommand);
        setCommandListener(this);
        stack.addElement(new State("LCDUI Conformance Suite", buildRoot()));
    }

    public void setRunner(Runner r) {
        this.runner = r;
    }

    Display display() {
        return display;
    }

    void setStatus(String s) {
        status = s == null ? "" : s;
        repaint();
    }

    private Vector buildRoot() {
        Vector v = new Vector();
        v.addElement(new Entry("Run all checks (" + suite.size() + ")", new Action() {
            public void run() {
                runner.runAll();
            }
        }));
        v.addElement(new Entry("Run one group...", new Action() {
            public void run() {
                openGroups(false);
            }
        }));
        v.addElement(new Entry("Run a single check...", new Action() {
            public void run() {
                openGroups(true);
            }
        }));
        v.addElement(new Entry("Run live checks only (" + liveCount() + ")",
                new Action() {
                    public void run() {
                        runner.runLiveOnly();
                    }
                }));
        v.addElement(new Entry("Manual checks (" + manualCount() + ")", new Action() {
            public void run() {
                runner.startManual();
            }
        }));
        v.addElement(new Entry("Component gallery...", new Action() {
            public void run() {
                openGallery();
            }
        }));
        v.addElement(new Entry("Last saved report", new Action() {
            public void run() {
                showLastReport();
            }
        }));
        v.addElement(new Entry("Environment", new Action() {
            public void run() {
                showText("Environment", Env.collect(display));
            }
        }));
        v.addElement(new Entry("About", new Action() {
            public void run() {
                showText("About", aboutText());
            }
        }));
        v.addElement(new Entry("Exit", new Action() {
            public void run() {
                exit();
            }
        }));
        return v;
    }

    private int liveCount() {
        return suite.checksOfKind(Fw.LIVE).size();
    }

    private int manualCount() {
        return suite.checksOfKind(Fw.MANUAL).size();
    }

    private void openGroups(final boolean single) {
        Vector v = new Vector();
        for (int i = 0; i < suite.groups().size(); i++) {
            final String g = (String) suite.groups().elementAt(i);
            int n = suite.checksIn(g).size();
            if (n == 0) {
                continue;
            }
            String label = single ? (g + " (" + n + ")")
                    : ("Run " + g + " (" + n + ")");
            v.addElement(new Entry(label, new Action() {
                public void run() {
                    if (single) {
                        openChecks(g);
                    } else {
                        runner.runGroup(g);
                    }
                }
            }));
        }
        push(single ? "Pick a group" : "Run group", v);
    }

    private void openChecks(String group) {
        Vector v = new Vector();
        Vector checks = suite.checksIn(group);
        for (int i = 0; i < checks.size(); i++) {
            final Check c = (Check) checks.elementAt(i);
            v.addElement(new Entry(c.id + " " + c.title, new Action() {
                public void run() {
                    runner.runOne(c.id);
                }
            }));
        }
        push(group, v);
    }

    private void openGallery() {
        Vector v = new Vector();
        v.addElement(new Entry("Graphics primitives", new Action() {
            public void run() {
                display.setCurrent(new Gallery.Primitives(display, MenuCanvas.this));
            }
        }));
        v.addElement(new Entry("Component zoo (Form)", new Action() {
            public void run() {
                display.setCurrent(Gallery.zoo(display, MenuCanvas.this));
            }
        }));
        v.addElement(new Entry("Alert types", new Action() {
            public void run() {
                openAlerts();
            }
        }));
        v.addElement(new Entry("List types", new Action() {
            public void run() {
                openLists();
            }
        }));
        v.addElement(new Entry("TextBox editor", new Action() {
            public void run() {
                display.setCurrent(Gallery.textBox(display, MenuCanvas.this));
            }
        }));
        push("Gallery", v);
    }

    private void openAlerts() {
        Vector v = new Vector();
        String[] names = {"INFO (timed)", "WARNING (timed)", "ERROR (timed)",
                "ALARM (timed)", "CONFIRMATION (modal, 2 commands)",
                "Activity indicator"};
        for (int i = 0; i < names.length; i++) {
            final int k = i;
            v.addElement(new Entry(names[i], new Action() {
                public void run() {
                    Gallery.showAlert(display, MenuCanvas.this, k);
                }
            }));
        }
        push("Alerts", v);
    }

    private void openLists() {
        Vector v = new Vector();
        int[] types = {javax.microedition.lcdui.Choice.IMPLICIT,
                javax.microedition.lcdui.Choice.EXCLUSIVE,
                javax.microedition.lcdui.Choice.MULTIPLE};
        String[] names = {"IMPLICIT", "EXCLUSIVE", "MULTIPLE"};
        for (int i = 0; i < names.length; i++) {
            final int t = types[i];
            v.addElement(new Entry(names[i] + " list", new Action() {
                public void run() {
                    display.setCurrent(Gallery.list(display, MenuCanvas.this, t));
                }
            }));
        }
        push("Lists", v);
    }

    private void showLastReport() {
        String saved = ReportStore.load();
        if (saved == null || saved.length() == 0) {
            setStatus("no saved report in RMS");
            return;
        }
        String human = saved;
        int p = saved.indexOf("LCDTEST-HUMAN-BEGIN");
        if (p >= 0) {
            int q = saved.indexOf("LCDTEST-HUMAN-END", p);
            if (q > p) {
                human = saved.substring(p + "LCDTEST-HUMAN-BEGIN".length(), q);
            }
        }
        if (human.indexOf("LCDUITEST|") >= 0 && p < 0) {
            human = "Saved machine report:\n\n" + saved;
        }
        TextCanvas t = new TextCanvas(display, this, "Saved report", human);
        display.setCurrent(t);
    }

    private void showText(String title, String body) {
        display.setCurrent(new TextCanvas(display, this, title, body));
    }

    private String aboutText() {
        return "LCDUI Conformance Suite 1.0\n\n"
                + "A black-box conformance and correctness test suite for "
                + "javax.microedition.lcdui on J2ME/MIDP 2.0 emulators and devices.\n\n"
                + suite.size() + " checks in " + suite.groups().size() + " groups, "
                + liveCount() + " of them requiring a live display and "
                + manualCount() + " requiring human inspection.\n\n"
                + "Expectations are taken from the MIDP 2.0 and CLDC 1.1 "
                + "specifications (JSR 118 / JSR 139), cross-checked against "
                + "the J2ME_Docs API reference.\n\n"
                + "Results are printed to System.out in the LCDUITEST|1.0 "
                + "line format and stored in RecordStore 'lcdtest-report'.\n\n"
                + "Keys: UP/DOWN move, FIRE/5 select, soft keys select/back.";
    }

    private void exit() {
        try {
            if (Host.midlet != null) {
                Host.midlet.notifyDestroyed();
            }
        } catch (Throwable t) {
            setStatus("exit failed: " + Ctx.exName(t));
        }
    }

    // ------------------------------------------------------------------
    // State management
    // ------------------------------------------------------------------

    private State state() {
        return (State) stack.lastElement();
    }

    private void push(String title, Vector entries) {
        if (entries.size() == 0) {
            setStatus("nothing to show");
            return;
        }
        stack.addElement(new State(title, entries));
        repaint();
    }

    private void pop() {
        if (stack.size() > 1) {
            stack.removeElementAt(stack.size() - 1);
            repaint();
        } else {
            setStatus("top level menu");
        }
    }

    private void move(int delta) {
        State s = state();
        s.selected += delta;
        if (s.selected < 0) {
            s.selected = 0;
        }
        if (s.selected >= s.entries.size()) {
            s.selected = s.entries.size() - 1;
        }
        repaint();
    }

    private void activate() {
        if (stack.size() == 0) {
            return;
        }
        State s = state();
        if (s.selected < 0 || s.selected >= s.entries.size()) {
            return;
        }
        Entry e = (Entry) s.entries.elementAt(s.selected);
        try {
            e.action.run();
        } catch (Throwable t) {
            setStatus("error: " + Ctx.exText(t));
        }
    }

    // ------------------------------------------------------------------
    // Painting
    // ------------------------------------------------------------------

    protected void paint(Graphics g) {
        Font f = Font.getDefaultFont();
        int w = getWidth();
        int h = getHeight();
        int titleH = f.getHeight() + 6;
        int footH = 2 * f.getHeight() + 6;
        int lh = f.getHeight() + 2;

        g.setColor(0xFFFFFF);
        g.fillRect(0, 0, w, h);
        g.setColor(0x000000);
        g.fillRect(0, 0, w, titleH);
        g.setColor(0xFFFFFF);
        g.drawString(state().title, 3, 3, Graphics.TOP | Graphics.LEFT);

        State s = state();
        int visible = Math.max(1, (h - titleH - footH) / lh);
        if (s.selected < s.top) {
            s.top = s.selected;
        } else if (s.selected >= s.top + visible) {
            s.top = s.selected - visible + 1;
        }
        int maxTop = Math.max(0, s.entries.size() - visible);
        if (s.top > maxTop) {
            s.top = maxTop;
        }
        int y = titleH + 3;
        for (int i = 0; i < visible && s.top + i < s.entries.size(); i++) {
            int idx = s.top + i;
            Entry e = (Entry) s.entries.elementAt(idx);
            if (idx == s.selected) {
                g.setColor(0x000000);
                g.fillRect(1, y - 1, w - 2, lh);
                g.setColor(0xFFFFFF);
            } else {
                g.setColor(0x000000);
            }
            g.drawString(fit(e.label, f, w - 8), 4, y, Graphics.TOP | Graphics.LEFT);
            y += lh;
        }

        int fy = h - footH;
        g.setColor(0xE8E8E8);
        g.fillRect(0, fy, w, footH);
        g.setColor(0x000000);
        g.drawString("UP/DOWN  FIRE=select  BACK=menu", 3, fy + 2,
                Graphics.TOP | Graphics.LEFT);
        String foot = status.length() > 0 ? status
                : ("CDLC 1.1 / MIDP 2.0   " + suite.size() + " checks");
        g.drawString(fit(foot, f, w - 6), 3, fy + f.getHeight() + 2,
                Graphics.TOP | Graphics.LEFT);
        if (s.entries.size() > visible) {
            g.setColor(0x808080);
            int barH = Math.max(6, (h - titleH - footH) * visible
                    / s.entries.size());
            int barY = titleH + (h - titleH - footH - barH) * s.top
                    / Math.max(1, s.entries.size() - visible);
            g.fillRect(w - 3, barY, 2, barH);
        }
    }

    private static String fit(String s, Font f, int width) {
        if (f.stringWidth(s) <= width) {
            return s;
        }
        String t = s;
        while (t.length() > 1 && f.stringWidth(t + "..") > width) {
            t = t.substring(0, t.length() - 1);
        }
        return t + "..";
    }

    private int rowAt(int y) {
        Font f = Font.getDefaultFont();
        int top = f.getHeight() + 9;
        int lh = f.getHeight() + 2;
        int row = (y - top) / lh;
        return row;
    }

    protected void pointerPressed(int x, int y) {
        int row = rowAt(y);
        State s = state();
        if (row >= 0 && s.top + row < s.entries.size()) {
            pressedRow = s.top + row;
            s.selected = pressedRow;
            repaint();
        }
    }

    protected void pointerReleased(int x, int y) {
        int row = rowAt(y);
        State s = state();
        if (pressedRow >= 0 && row >= 0 && s.top + row == pressedRow) {
            activate();
        }
        pressedRow = -1;
    }

    protected void keyPressed(int keyCode) {
        int action = 0;
        try {
            action = getGameAction(keyCode);
        } catch (Throwable t) {
            // fall through to numeric handling
        }
        if (action == Canvas.UP || keyCode == Canvas.KEY_NUM2) {
            move(-1);
        } else if (action == Canvas.DOWN || keyCode == Canvas.KEY_NUM8) {
            move(1);
        } else if (action == Canvas.FIRE || action == Canvas.RIGHT
                || keyCode == Canvas.KEY_NUM5) {
            activate();
        } else if (action == Canvas.LEFT) {
            pop();
        }
    }

    public void startAutorun() {
        status = "autorun";
        runner.runAll();
    }

    public void commandAction(Command c, Displayable d) {
        if (c == okCommand) {
            activate();
        } else if (c == backCommand) {
            pop();
        } else if (c == exitCommand) {
            exit();
        }
    }
}
