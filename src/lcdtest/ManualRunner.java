package lcdtest;

import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.StringItem;

/** Walks a human through the manual checks and records the verdicts. */
public final class ManualRunner implements CommandListener {

    private final Runner runner;
    private final Display display;
    private final Report report;
    private final Vector checks;
    private final MenuCanvas menu;
    private final Command pass = new Command("PASS", Command.SCREEN, 1);
    private final Command fail = new Command("FAIL", Command.SCREEN, 2);
    private final Command skip = new Command("SKIP", Command.SCREEN, 3);
    private int index;

    ManualRunner(Runner runner, Display display, Report report, Vector checks,
            MenuCanvas menu) {
        this.runner = runner;
        this.display = display;
        this.report = report;
        this.checks = checks;
        this.menu = menu;
    }

    void start() {
        showNext();
    }

    private void showNext() {
        if (index >= checks.size()) {
            display.setCurrent(menu);
            runner.manualFinished();
            return;
        }
        Check c = (Check) checks.elementAt(index);
        Form f = new Form("Manual " + (index + 1) + "/" + checks.size());
        f.append(new StringItem(c.id, c.title));
        f.append(new StringItem("What to check", c.spec));
        f.append(new StringItem("Verdict", "Use the PASS / FAIL / SKIP commands "
                + "after inspecting the device. PASS and FAIL are recorded in "
                + "the report; SKIP leaves the check unverified."));
        f.addCommand(pass);
        f.addCommand(fail);
        f.addCommand(skip);
        f.setCommandListener(this);
        display.setCurrent(f);
    }

    public void commandAction(Command c, Displayable d) {
        Check check = (Check) checks.elementAt(index);
        int status;
        String detail;
        if (c == pass) {
            status = Fw.PASS;
            detail = "manual: observed conformant";
        } else if (c == fail) {
            status = Fw.FAIL;
            detail = "manual: observed non-conformant";
        } else {
            status = Fw.SKIP;
            detail = "manual: skipped by tester";
        }
        report.add(new Result(check, status, detail, 0, true));
        System.out.println("LCDUITEST|R|" + check.id + "|" + Fw.statusName(status)
                + "|" + detail);
        index++;
        showNext();
    }
}
