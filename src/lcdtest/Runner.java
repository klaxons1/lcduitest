package lcdtest;

import java.util.Vector;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.midlet.MIDlet;

/**
 * Executes checks and drives the live UI phases.
 *
 * Synchronous checks run on the calling thread.  Checks that need a real
 * display switch to a ProbeCanvas and are evaluated from callSerially()
 * callbacks, which are delivered on the event thread.
 */
public final class Runner {

    private final MIDlet midlet;
    private final Display display;
    private final Suite suite;
    private final MenuCanvas menu;

    private Report report;
    private ProbeCanvas probe;
    private Vector queue = new Vector();
    private int qIndex;
    private boolean autorun;

    private int phase;
    private int attempts;
    private int normalW;
    private int normalH;
    private int sizeChangedBefore;
    private int paintBefore;
    private int paintBefore2;
    private int serialPaint;
    private int paintPhase;
    private boolean serialACalled;
    private String serialOrder = "";
    private boolean liveStarted;
    private boolean running;

    private static final int PH_SHOW = 0;
    private static final int PH_SERIAL = 1;
    private static final int PH_SERIAL_DONE = 2;
    private static final int PH_REPAINT_DONE = 3;
    private static final int PH_FULLSCREEN = 4;
    private static final int PH_FULLSCREEN_DONE = 5;
    private static final int PH_LEAVING = 6;
    private static final int PH_HIDDEN = 7;

    public Runner(MIDlet midlet, Display display, Suite suite, MenuCanvas menu) {
        this.midlet = midlet;
        this.display = display;
        this.suite = suite;
        this.menu = menu;
    }

    public MenuCanvas menu() {
        return menu;
    }

    public Display display() {
        return display;
    }

    public Report report() {
        return report;
    }

    public Suite suite() {
        return suite;
    }

    public void setAutorun(boolean on) {
        autorun = on;
    }

    public void runAll() {
        runChecks(suite.checks(), "all");
    }

    public void runGroup(String group) {
        runChecks(suite.checksIn(group), group);
    }

    public void runSyncOnly() {
        runChecks(suite.checksOfKind(Fw.SYNC), "sync");
    }

    public void runLiveOnly() {
        Vector v = suite.checksOfKind(Fw.LIVE);
        if (v.size() > 0) {
            runChecks(v, "live");
        } else {
            finishWithNoChecks("no live checks");
        }
    }

    public void runOne(String id) {
        Check c = suite.byId(id);
        Vector v = new Vector();
        if (c != null) {
            v.addElement(c);
        }
        runChecks(v, id);
    }

    private void finishWithNoChecks(String why) {
        report = new Report(now());
        report.env(Env.collect(display));
        System.out.println("LCDUITEST: " + why);
        finish();
    }

    private void runChecks(Vector checks, String label) {
        if (running) {
            menu.setStatus("run already in progress");
            return;
        }
        running = true;
        report = new Report(now());
        report.env(Env.collect(display));
        queue = checks;
        qIndex = 0;
        probe = null;
        phase = PH_SHOW;
        attempts = 0;
        paintPhase = 0;
        serialACalled = false;
        serialOrder = "";
        liveStarted = false;
        if (autorun) {
            menu.setStatus("running " + label + "...");
        }
        // Everything that does not need a display can run right here.
        runSyncChecks();
        if (hasLiveChecks()) {
            startLive();
        } else {
            finish();
        }
    }

    private boolean hasLiveChecks() {
        for (int i = 0; i < queue.size(); i++) {
            if (((Check) queue.elementAt(i)).kind == Fw.LIVE) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------
    // Synchronous checks
    // ------------------------------------------------------------------

    private void runSyncChecks() {
        Vector sync = new Vector();
        Vector live = new Vector();
        Vector manual = new Vector();
        for (int i = 0; i < queue.size(); i++) {
            Check c = (Check) queue.elementAt(i);
            if (c.kind == Fw.SYNC) {
                sync.addElement(c);
            } else if (c.kind == Fw.LIVE) {
                live.addElement(c);
            } else {
                manual.addElement(c);
            }
        }
        queue = sync;
        qIndex = 0;
        while (qIndex < queue.size()) {
            Check c = (Check) queue.elementAt(qIndex++);
            runSync(c);
        }
        queue = live;
        qIndex = 0;
        // Manual checks that were part of "run all" are reported as not run.
        for (int i = 0; i < manual.size(); i++) {
            report.add(new Result((Check) manual.elementAt(i), Fw.SKIP,
                    "manual check not run (use Manual checks)", 0, true));
        }
    }

    private void runSync(Check c) {
        long t0 = System.currentTimeMillis();
        Ctx ctx = new Ctx(display, c);
        ctx.probe = probe;
        int status;
        String detail;
        try {
            if (c.body == null) {
                ctx.skip("no test body");
            } else {
                c.body.run(ctx);
            }
            status = ctx.status();
            detail = ctx.detailString();
            if (status == Fw.WARN && ctx.assertions == 0 && ctx.skips == 0) {
                detail = append(detail, "no assertions executed");
            }
        } catch (Throwable t) {
            status = Fw.FAIL;
            detail = append(ctx.detailString(), "uncaught " + Ctx.exText(t));
        }
        report.add(new Result(c, status, detail, System.currentTimeMillis() - t0));
        if (autorun) {
            System.out.println("LCDUITEST|R|" + c.id + "|" + Fw.statusName(status)
                    + "|" + Fw.oneLine(detail));
        }
    }

    // ------------------------------------------------------------------
    // Live checks
    // ------------------------------------------------------------------

    private void startLive() {
        liveStarted = true;
        probe = new ProbeCanvas("LCDUI conformance probe");
        try {
            display.setCurrent(probe);
        } catch (Throwable t) {
            recordLive(Fw.FAIL, "CNV-L02", "setCurrent(probe) threw " + Ctx.exText(t));
            finish();
            return;
        }
        schedule();
    }

    private void schedule() {
        display.callSerially(new Runnable() {
            public void run() {
                poll();
            }
        });
    }

    private void poll() {
        attempts++;
        switch (phase) {
            case PH_SHOW:
                if (!probe.isShown() || probe.paintCount == 0) {
                    if (attempts < 80) {
                        schedule();
                    } else {
                        recordLive(Fw.FAIL, "CNV-L02",
                                "Canvas was not shown and painted (isShown="
                                        + probe.isShown() + " paints="
                                        + probe.paintCount + ")");
                        finish();
                    }
                    return;
                }
                runShownChecks();
                phase = PH_SERIAL;
                attempts = 0;
                startSerial();
                schedule();
                return;

            case PH_SERIAL:
                if (!serialACalled && attempts < 40) {
                    schedule();
                    return;
                }
                runSerialChecks();
                phase = PH_SERIAL_DONE;
                attempts = 0;
                paintBefore2 = probe.paintCount;
                probe.repaint();
                schedule();
                return;

            case PH_SERIAL_DONE:
                if (probe.paintCount == paintBefore2 && attempts < 20) {
                    schedule();
                    return;
                }
                recordLive(probe.paintCount > paintBefore2 ? Fw.PASS : Fw.WARN,
                        "CNV-L11", probe.paintCount > paintBefore2
                                ? "paint() was called after repaint()"
                                : "no paint within the observation window");
                runServiceRepaint();
                phase = PH_REPAINT_DONE;
                attempts = 0;
                schedule();
                return;

            case PH_REPAINT_DONE:
                runFullscreenStart();
                phase = PH_FULLSCREEN;
                attempts = 0;
                schedule();
                return;

            case PH_FULLSCREEN:
                if (probe.sizeChangedCount == sizeChangedBefore && attempts < 20) {
                    schedule();
                    return;
                }
                runFullscreenChecks();
                phase = PH_FULLSCREEN_DONE;
                attempts = 0;
                leave();
                schedule();
                return;

            case PH_LEAVING:
                if ((!probe.hideNotified || probe.isShown()) && attempts < 60) {
                    schedule();
                    return;
                }
                runHiddenChecks();
                phase = PH_HIDDEN;
                finish();
                return;

            default:
                finish();
        }
    }

    private void startSerial() {
        serialOrder = "";
        serialACalled = false;
        display.callSerially(new Runnable() {
            public void run() {
                serialACalled = true;
                serialOrder += "A";
            }
        });
        display.callSerially(new Runnable() {
            public void run() {
                serialOrder += "B";
            }
        });
        recordLive(!serialACalled ? Fw.PASS : Fw.FAIL, "CNV-L13",
                serialACalled ? "callSerially ran the Runnable synchronously"
                        : "callSerially returned before the Runnable ran");
    }

    private void runShownChecks() {
        Ctx c;

        c = ctx("DSP-L01");
        c.same("current Displayable", probe, display.getCurrent());
        end(c);

        c = ctx("DSP-L02");
        c.isTrue("isShown() of the current Canvas", probe.isShown());
        end(c);

        c = ctx("CNV-L19");
        c.same("getCurrent()", probe, display.getCurrent());
        end(c);

        c = ctx("CNV-L01");
        c.atLeast("showNotify received", probe.showAt, 0);
        c.atLeast("first paint after showNotify", probe.firstPaintAt, 0);
        c.isTrue("showNotify precedes paint",
                probe.showAt >= 0 && probe.firstPaintAt > probe.showAt);
        end(c);

        c = ctx("CNV-L02");
        c.atLeast("paint count", probe.paintCount, 1);
        c.isTrue("painted flag", probe.painted);
        end(c);

        c = ctx("CNV-L03");
        c.eqInt("isShown() inside paint", 1, probe.gShownInPaint);
        end(c);

        c = ctx("CNV-L04");
        c.eqInt("initial paint colour", 0, probe.gColor & 0xFFFFFF);
        end(c);

        c = ctx("CNV-L05");
        c.eqInt("initial stroke", Graphics.SOLID, probe.gStroke);
        end(c);

        c = ctx("CNV-L06");
        c.isTrue("font is the default font instance", probe.gFontIdentity == 1);
        Font f = Font.getDefaultFont();
        c.eqInt("font face", f.getFace(), probe.gFontFace);
        c.eqInt("font size", f.getSize(), probe.gFontSize);
        c.eqInt("font style", f.getStyle(), probe.gFontStyle);
        end(c);

        c = ctx("CNV-L07");
        c.eqInt("translateX", 0, probe.gTranslateX);
        c.eqInt("translateY", 0, probe.gTranslateY);
        end(c);

        c = ctx("CNV-L08");
        c.atLeast("clip width", probe.gClipW, 1);
        c.atLeast("clip height", probe.gClipH, 1);
        c.inRange("clip x", probe.gClipX, 0, Math.max(0, probe.getWidth() - 1));
        c.inRange("clip y", probe.gClipY, 0, Math.max(0, probe.getHeight() - 1));
        c.isTrue("clip inside canvas",
                probe.gClipX + probe.gClipW <= probe.getWidth()
                        && probe.gClipY + probe.gClipH <= probe.getHeight());
        end(c);

        c = ctx("CNV-L09");
        c.isTrue("copyArea threw IllegalStateException (was "
                        + probe.copyAreaException + ")",
                "java.lang.IllegalStateException".equals(probe.copyAreaException));
        end(c);

        c = ctx("CNV-L16");
        c.atLeast("sizeChanged calls", probe.sizeChangedCount, 1);
        end(c);

        // setCurrent(null) must be ignored.
        c = ctx("CNV-L15");
        display.setCurrent((Displayable) null);
        c.same("current unchanged", probe, display.getCurrent());
        end(c);
    }

    private void runSerialChecks() {
        Ctx c = ctx("CNV-L14");
        c.eqStr("callSerially order", "AB", serialOrder);
        c.isTrue("first Runnable ran", serialACalled);
        end(c);

        Ctx d = ctx("CNV-L10");
        paintBefore = probe.paintCount;
        probe.repaint();
        d.eqInt("no synchronous paint", paintBefore, probe.paintCount);
        end(d);
    }

    private void runServiceRepaint() {
        Ctx c = ctx("CNV-L12");
        serialPaint = probe.paintCount;
        probe.repaint();
        probe.serviceRepaints();
        c.isTrue("paint happened during serviceRepaints()",
                probe.paintCount > serialPaint);
        end(c);
    }

    private void runFullscreenStart() {
        normalW = probe.getWidth();
        normalH = probe.getHeight();
        sizeChangedBefore = probe.sizeChangedCount;
        try {
            probe.setFullScreenMode(true);
        } catch (Throwable t) {
            recordLive(Fw.FAIL, "CNV-L18",
                    "setFullScreenMode(true) threw " + Ctx.exText(t));
        }
    }

    private void runFullscreenChecks() {
        Ctx c = ctx("CNV-L18");
        c.notNull("canvas", probe);
        c.atLeast("full-screen width", probe.getWidth(), normalW);
        c.atLeast("full-screen height", probe.getHeight(), normalH);
        if (probe.sizeChangedCount > sizeChangedBefore) {
            c.eqInt("sizeChanged width matches getWidth()", probe.getWidth(),
                    probe.lastWidth);
            c.eqInt("sizeChanged height matches getHeight()", probe.getHeight(),
                    probe.lastHeight);
        }
        end(c);
        try {
            probe.setFullScreenMode(false);
        } catch (Throwable t) {
            recordLive(Fw.FAIL, "CNV-L18",
                    "setFullScreenMode(false) threw " + Ctx.exText(t));
        }
    }

    private void leave() {
        phase = PH_LEAVING;
        attempts = 0;
        try {
            display.setCurrent(menu);
        } catch (Throwable t) {
            // fall through: hidden checks will fail with a useful detail
        }
    }

    private void runHiddenChecks() {
        Ctx c = ctx("CNV-L17");
        c.isTrue("hideNotify received", probe.hideNotified);
        c.isFalse("isShown() after removal", probe.isShown());
        end(c);
    }

    private Ctx ctx(String id) {
        Check check = suite.byId(id);
        if (check == null) {
            check = new Check(id, "Live", id, "", Fw.LIVE, null);
        }
        Ctx c = new Ctx(display, check);
        c.probe = probe;
        return c;
    }

    private void end(Ctx c) {
        Result r = new Result(c.check, c.status(), c.detailString(), 0);
        r.manual = false;
        report.add(r);
        if (autorun) {
            System.out.println("LCDUITEST|R|" + c.check.id + "|"
                    + Fw.statusName(r.status) + "|" + Fw.oneLine(r.detail));
        }
    }

    private void recordLive(int status, String id, String detail) {
        Check check = suite.byId(id);
        if (check == null) {
            check = new Check(id, "Live", id, "", Fw.LIVE, null);
        }
        report.add(new Result(check, status, detail, 0));
        if (autorun) {
            System.out.println("LCDUITEST|R|" + id + "|" + Fw.statusName(status)
                    + "|" + Fw.oneLine(detail));
        }
    }

    // ------------------------------------------------------------------
    // Manual checks
    // ------------------------------------------------------------------

    public void startManual() {
        if (running) {
            menu.setStatus("run already in progress");
            return;
        }
        running = true;
        report = new Report(now());
        report.env(Env.collect(display));
        Vector manual = suite.checksOfKind(Fw.MANUAL);
        new ManualRunner(this, display, report, manual, menu).start();
    }

    void manualFinished() {
        finish();
    }

    // ------------------------------------------------------------------
    // Finish
    // ------------------------------------------------------------------

    private void finish() {
        if (report == null) {
            running = false;
            return;
        }
        running = false;
        String machine = report.toMachine(true);
        System.out.print(machine);
        System.out.println("LCDTEST-HUMAN-BEGIN");
        System.out.print(report.toHuman());
        System.out.println("LCDTEST-HUMAN-END");
        ReportStore.save(machine);
        if (autorun) {
            System.out.println("LCDTEST-END");
            try {
                midlet.notifyDestroyed();
            } catch (Throwable t) {
                System.out.println("LCDTEST: notifyDestroyed threw " + Ctx.exText(t));
            }
        } else {
            SummaryCanvas summary = new SummaryCanvas(this, report, menu);
            display.setCurrent(summary);
        }
    }

    static String now() {
        return String.valueOf(System.currentTimeMillis());
    }

    private static String append(String a, String b) {
        if (a == null || a.length() == 0) {
            return b;
        }
        if (b == null || b.length() == 0) {
            return a;
        }
        return a + "; " + b;
    }
}
