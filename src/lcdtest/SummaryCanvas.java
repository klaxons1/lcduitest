package lcdtest;

import java.util.Vector;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/** Post-run summary with access to the full report. */
public final class SummaryCanvas extends Canvas implements CommandListener {

    private final Runner runner;
    private final Report report;
    private final MenuCanvas menu;
    private final Display display;
    private final Command reportCommand = new Command("Report", Command.OK, 1);
    private final Command menuCommand = new Command("Menu", Command.BACK, 1);

    public SummaryCanvas(Runner runner, Report report, MenuCanvas menu) {
        this.runner = runner;
        this.report = report;
        this.menu = menu;
        this.display = runner.display();
        setTitle("Results");
        addCommand(reportCommand);
        addCommand(menuCommand);
        setCommandListener(this);
    }

    protected void paint(Graphics g) {
        int w = getWidth();
        int h = getHeight();
        Font small = Font.getDefaultFont();
        Font bold = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD,
                Font.SIZE_MEDIUM);
        g.setColor(0xFFFFFF);
        g.fillRect(0, 0, w, h);

        String verdict = report.verdict();
        int band = bold.getHeight() + 8;
        g.setColor(report.count(Fw.FAIL) > 0 ? 0x800000 : 0x006000);
        g.fillRect(0, 0, w, band);
        g.setColor(0xFFFFFF);
        g.drawString(verdict, w / 2, 4, Graphics.TOP | Graphics.HCENTER);

        int y = band + 6;
        g.setColor(0x000000);
        g.drawString("Pass " + report.count(Fw.PASS) + "   Fail "
                + report.count(Fw.FAIL) + "   Warn " + report.count(Fw.WARN)
                + "   Skip " + report.count(Fw.SKIP) + "   Total " + report.total(),
                3, y, Graphics.TOP | Graphics.LEFT);
        y += small.getHeight() + 4;
        g.drawString("Manual " + countManual(), 3, y, Graphics.TOP | Graphics.LEFT);
        y += small.getHeight() + 6;

        // Group summary, compact two columns.
        g.drawString("Groups (pass/fail/warn/skip of total)", 3, y,
                Graphics.TOP | Graphics.LEFT);
        y += small.getHeight() + 2;
        java.util.Hashtable groups = report.byGroup();
        Vector names = new Vector();
        java.util.Enumeration e = groups.keys();
        while (e.hasMoreElements()) {
            names.addElement(e.nextElement());
        }
        Report.sortStrings(names);
        int colW = w / 2;
        for (int i = 0; i < names.size(); i++) {
            String name = (String) names.elementAt(i);
            int[] s = (int[]) groups.get(name);
            int col = i % 2;
            int row = i / 2;
            int x = 3 + col * colW;
            int yy = y + row * small.getHeight();
            if (yy > h - 4 * small.getHeight()) {
                break;
            }
            String line = trim(name, small, colW - 6) + " "
                    + s[0] + "/" + s[1] + "/" + s[2] + "/" + s[3] + " of " + s[4];
            g.drawString(line, x, yy, Graphics.TOP | Graphics.LEFT);
        }
        y = h - 3 * small.getHeight();
        g.setColor(0xE0E0E0);
        g.fillRect(0, y - 2, w, 3 * small.getHeight());
        g.setColor(0x000000);
        g.drawString("OK / FIRE: full report", 3, y, Graphics.TOP | Graphics.LEFT);
        g.drawString("BACK / LEFT: main menu", 3, y + small.getHeight(),
                Graphics.TOP | Graphics.LEFT);
        g.drawString("Report saved to RMS as lcdtest-report", 3,
                y + 2 * small.getHeight(), Graphics.TOP | Graphics.LEFT);
    }

    private int countManual() {
        int n = 0;
        for (int i = 0; i < report.results().size(); i++) {
            if (((Result) report.results().elementAt(i)).manual) {
                n++;
            }
        }
        return n;
    }

    private static String trim(String s, Font f, int width) {
        if (f.stringWidth(s) <= width) {
            return s;
        }
        String t = s;
        while (t.length() > 1 && f.stringWidth(t + ".") > width) {
            t = t.substring(0, t.length() - 1);
        }
        return t + ".";
    }

    protected void keyPressed(int keyCode) {
        int action = 0;
        try {
            action = getGameAction(keyCode);
        } catch (Throwable t) {
            // ignore
        }
        if (action == Canvas.FIRE || keyCode == Canvas.KEY_NUM5) {
            showReport();
        } else if (action == Canvas.LEFT || action == Canvas.RIGHT) {
            display.setCurrent(menu);
        }
    }

    private void showReport() {
        TextCanvas t = new TextCanvas(display, this, "Conformance report",
                report.toHuman());
        display.setCurrent(t);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == reportCommand) {
            showReport();
        } else if (c == menuCommand) {
            display.setCurrent(menu);
        }
    }
}
