/**
 * M3G Tester - Application shell: menu tree, test execution, progress display, result
 * browser and visual demos.
 *
 * Based on lcduitest.ui.App but adapted for M3G suites (no categories).
 */
package m3gtest.ui;

import java.util.Vector;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextBox;
import javax.microedition.midlet.MIDlet;

import m3gtest.Env;
import m3gtest.ExitHook;
import m3gtest.Results;
import m3gtest.Suites;
import m3gtest.TestCase;
import m3gtest.TestResult;
import m3gtest.TestRunner;
import m3gtest.TestSuite;
import m3gtest.Ui;
import m3gtest.demos.Demos;

public class App implements CommandListener {

    private static final int LIST_SUITES = 1;
    private static final int LIST_DEMOS = 2;
    private static final int LIST_SUITE_RESULTS = 3;
    private static final int LIST_TEST_RESULTS = 4;

    private final MIDlet midlet;
    private final ExitHook exitHook;
    private final Display display;

    private final List mainMenu = new List("M3G Tester", List.IMPLICIT);
    private final List listScreen = new List("", List.IMPLICIT);
    private int listKind = LIST_SUITES;
    private Canvas canvasScreen;
    private final Form progressForm = new Form("Running");
    private final Gauge progressGauge = new Gauge("progress", false, 100, 0);
    private final StringItem progressItem = new StringItem("", "");
    private final Form detailForm = new Form("Test detail");

    private final Command backCommand = new Command("Back", Command.BACK, 2);
    private final Command okCommand = new Command("Select", Command.OK, 1);
    private final Command stopCommand = new Command("Stop", Command.STOP, 1);
    private final Command rerunCommand = new Command("Run again", Command.SCREEN, 3);
    private final Command runAllCommand = new Command("Run ALL", Command.SCREEN, 4);
    private final Command saveCommand = new Command("Save report", Command.SCREEN, 5);

    private Displayable previous;
    private TestSuite[] allSuites;

    private Results results;
    private TestRunner runner;
    private final Vector suiteRuns = new Vector();
    private Results.SuiteRun selectedSuiteRun;
    private TestResult selectedResult;
    private boolean runningSingle;

    public App(MIDlet midlet, ExitHook exitHook) {
        this.midlet = midlet;
        this.exitHook = exitHook;
        this.display = Display.getDisplay(midlet);
        Ui.init(midlet);
        progressForm.append(progressGauge);
        progressForm.append(progressItem);
        progressForm.addCommand(stopCommand);
        progressForm.setCommandListener(this);
    }

    public void start() {
        int tests = 0;
        TestSuite[] all = Suites.all();
        allSuites = all;
        for (int i = 0; i < all.length; i++) {
            tests += all[i].size();
        }
        mainMenu.deleteAll();
        mainMenu.append("Run ALL (" + tests + " tests)", null);
        mainMenu.append("Choose suite (" + all.length + ")", null);
        mainMenu.append("Visual demos (" + Demos.titles().length + ")", null);
        mainMenu.append("Environment", null);
        mainMenu.append("Last stored run", null);
        mainMenu.append("Help / about", null);
        mainMenu.addCommand(okCommand);
        mainMenu.setCommandListener(this);
        display.setCurrent(mainMenu);
        String autorun = null;
        try {
            autorun = midlet.getAppProperty("M3GTester-AutoRun");
            if (autorun == null) autorun = midlet.getAppProperty("autorun");
        } catch (Throwable t) {}
        if (autorun == null) autorun = System.getProperty("m3gtest.autorun");
        if (autorun != null && autorun.length() > 0 && !autorun.equals("false") && !autorun.equals("0")) {
            runSuites(all, "ALL suites", true);
        }
    }

    public void pause() {}

    public void destroy() {
        if (runner != null) {
            runner.cancel();
        }
    }

    private void go(Displayable displayable) {
        Displayable current = display.getCurrent();
        if (current != null && current != displayable) {
            previous = current;
        }
        display.setCurrent(displayable);
    }

    private void goBack() {
        if (previous != null) {
            Displayable target = previous;
            previous = null;
            display.setCurrent(target);
        } else {
            display.setCurrent(mainMenu);
        }
    }

    private void showSuites() {
        listKind = LIST_SUITES;
        listScreen.setTitle("Suites");
        listScreen.deleteAll();
        listScreen.removeCommand(runAllCommand);
        listScreen.removeCommand(saveCommand);
        TestSuite[] suites = Suites.all();
        for (int i = 0; i < suites.length; i++) {
            TestSuite suite = suites[i];
            listScreen.append(suite.getTitle() + " [" + suite.size() + "]", null);
        }
        listScreen.addCommand(backCommand);
        listScreen.addCommand(okCommand);
        listScreen.addCommand(runAllCommand);
        listScreen.setCommandListener(this);
        go(listScreen);
    }

    private void showDemos() {
        String[] titles = Demos.titles();
        listKind = LIST_DEMOS;
        listScreen.setTitle("Visual demos");
        listScreen.deleteAll();
        listScreen.removeCommand(runAllCommand);
        listScreen.removeCommand(saveCommand);
        for (int i = 0; i < titles.length; i++) {
            listScreen.append(titles[i], null);
        }
        listScreen.addCommand(backCommand);
        listScreen.addCommand(okCommand);
        listScreen.setCommandListener(this);
        go(listScreen);
    }

    private void showText(String title, String text) {
        TextBox box = new TextBox(title, text, 8192, 0);
        box.addCommand(backCommand);
        box.setCommandListener(this);
        go(box);
    }

    private void showEnvironment() {
        showText("Environment", new Env(midlet).describe());
    }

    private void showHelp() {
        StringBuffer sb = new StringBuffer();
        sb.append("M3G conformance test MIDlet for JSR-184.\n\n");
        sb.append("Every menu entry runs a group of test cases that exercises the "
                + "javax.microedition.m3g API against the M3G 1.1 specification.\n\n");
        sb.append("Marks in the result list:\n");
        sb.append("  +  pass\n");
        sb.append("  x  failure (specification violated)\n");
        sb.append("  !  error (unexpected exception)\n");
        sb.append("  i  observation\n");
        sb.append("  M  manual test\n\n");
        sb.append("Automated (CI) mode: start the MIDlet with\n");
        sb.append("  M3GTester-AutoRun=true\n");
        sb.append("to run everything, print a machine readable log on stdout and store "
                + "the summary in the RecordStore. Add M3GTester-Exit=true to shut down when over.\n");
        showText("Help / about", sb.toString());
    }

    private void showLastStoredRun() {
        Vector lines = Results.loadSummary();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < lines.size(); i++) {
            sb.append(lines.elementAt(i)).append('\n');
        }
        if (sb.length() == 0) {
            sb.append("no stored run\n\n(run a test group with m3gtest.store=true "
                    + "or use 'Save report' on the result screen)");
        }
        showText("Last stored run", sb.toString());
    }

    private void runAll(boolean auto) {
        runSuites(Suites.all(), "ALL suites", auto);
    }

    private void runSuites(TestSuite[] suites, String title, boolean auto) {
        if (runner != null) {
            runner.cancel();
        }
        results = new Results();
        suiteRuns.removeAllElements();
        int total = 0;
        for (int i = 0; i < suites.length; i++) {
            total += suites[i].size();
        }
        progressGauge.setMaxValue(total);
        progressGauge.setValue(0);
        progressItem.setText("0 / " + total + "  " + title);
        progressForm.setTitle(title);
        runner = new TestRunner(new SessionListener());
        Ui.display().setCurrent(progressForm);
        previous = mainMenu;
        Thread thread = new Thread(new Session(suites, auto));
        thread.start();
    }

    private class Session implements Runnable {
        private final TestSuite[] suites;
        private final boolean auto;
        Session(TestSuite[] suites, boolean auto) {
            this.suites = suites;
            this.auto = auto;
        }
        public void run() {
            results.announceBegin();
            for (int i = 0; i < suites.length; i++) {
                TestRunner active = runner;
                if (active == null || active.isCancelled()) {
                    break;
                }
                active.run(suites[i]);
            }
            results.finish();
            results.announceEnd();
            if (Env.flag("m3gtest.store")) {
                results.save();
            }
            // autorun stdout report
            try {
                System.out.println("M3GTester autorun finished: total=" + results.total()
                        + " passed=" + results.passed() + " failed=" + results.failed()
                        + " errors=" + results.errors() + " millis=" + results.getMillis());
                for (int i = 0; i < suiteRuns.size(); i++) {
                    Results.SuiteRun sr = (Results.SuiteRun) suiteRuns.elementAt(i);
                    System.out.println("SUITE " + sr.suite.getId() + " " + sr.stats.passed + "/" + sr.stats.total
                            + " failed=" + sr.stats.failed + " errors=" + sr.stats.errors);
                    for (int j = 0; j < sr.testResults.size(); j++) {
                        TestResult tr = (TestResult) sr.testResults.elementAt(j);
                        if (tr.isFailure()) {
                            System.out.println("FAIL " + sr.suite.getId() + "." + tr.getName() + " -> " + tr.getMessage());
                        }
                    }
                }
            } catch (Throwable t) {}
            display.callSerially(new Runnable() {
                public void run() {
                    showRunResults();
                    if (auto) {
                        boolean shouldExit = false;
                        try {
                            String v = midlet.getAppProperty("M3GTester-Exit");
                            if (v == null) v = midlet.getAppProperty("exit");
                            if (v == null) v = System.getProperty("m3gtest.exit");
                            shouldExit = v != null && v.length() > 0 && !v.equals("false") && !v.equals("0");
                        } catch (Throwable t) {}
                        if (shouldExit) {
                            exitHook.requestExit();
                        }
                    }
                }
            });
        }
    }

    private class SessionListener implements TestRunner.Listener {
        private Results.SuiteRun currentRun;
        private int executed;
        public void suiteStarted(final TestSuite suite, int total) {
            currentRun = new Results.SuiteRun(suite, new TestRunner.SuiteResult());
            currentRun.stats.total = total;
            suiteRuns.addElement(currentRun);
            display.callSerially(new Runnable() {
                public void run() {
                    progressForm.setTitle(suite.getId());
                }
            });
        }
        public void testFinished(TestSuite suite, TestCase test, TestResult result) {
            currentRun.testResults.addElement(result);
            executed++;
            final int done = executed;
            final String text = result.getStatusText() + " " + test.getName();
            display.callSerially(new Runnable() {
                public void run() {
                    progressGauge.setValue(done);
                    progressItem.setText(done + " / " + progressGauge.getMaxValue() + "  " + text);
                }
            });
            Results.announceTest(suite.getId(), result);
        }
        public void suiteFinished(TestSuite suite, TestRunner.SuiteResult stats) {
            Results.SuiteRun run = currentRun;
            run.stats.total = stats.total;
            run.stats.passed = stats.passed;
            run.stats.failed = stats.failed;
            run.stats.errors = stats.errors;
            run.stats.infos = stats.infos;
            run.stats.millis = stats.millis;
            Results.announceSuite(run);
        }
    }

    private void showRunResults() {
        if (results == null || suiteRuns.size() == 0) {
            display.setCurrent(mainMenu);
            return;
        }
        listKind = LIST_SUITE_RESULTS;
        listScreen.setTitle(results.verdict());
        listScreen.deleteAll();
        for (int i = 0; i < suiteRuns.size(); i++) {
            Results.SuiteRun run = (Results.SuiteRun) suiteRuns.elementAt(i);
            String title = run.suite.getId() + ": " + run.stats.passed + "/" + run.stats.total;
            if (run.failures() > 0) {
                title = title + "  fail=" + run.failures();
            }
            listScreen.append(title, null);
        }
        listScreen.removeCommand(runAllCommand);
        listScreen.removeCommand(saveCommand);
        listScreen.addCommand(backCommand);
        listScreen.addCommand(okCommand);
        listScreen.addCommand(runAllCommand);
        listScreen.addCommand(saveCommand);
        listScreen.setCommandListener(this);
        display.setCurrent(listScreen);
        previous = mainMenu;
    }

    private void showSuiteRunResults(int index) {
        selectedSuiteRun = (Results.SuiteRun) suiteRuns.elementAt(index);
        listKind = LIST_TEST_RESULTS;
        listScreen.setTitle(selectedSuiteRun.suite.getId() + ": "
                + selectedSuiteRun.stats.passed + "/" + selectedSuiteRun.stats.total);
        listScreen.deleteAll();
        for (int i = 0; i < selectedSuiteRun.testResults.size(); i++) {
            TestResult result = (TestResult) selectedSuiteRun.testResults.elementAt(i);
            listScreen.append(mark(result) + " " + result.getName(), null);
        }
        listScreen.removeCommand(runAllCommand);
        listScreen.removeCommand(saveCommand);
        listScreen.addCommand(backCommand);
        listScreen.addCommand(okCommand);
        listScreen.setCommandListener(this);
        go(listScreen);
    }

    private static String mark(TestResult result) {
        if (result.isFailure()) {
            return result.getStatus() == TestResult.ERROR ? "!" : "x";
        }
        if (result.getStatus() == TestResult.INFO) {
            return result.getSeverity() == TestCase.MANUAL ? "M" : "i";
        }
        return "+";
    }

    private void showTestDetail(int index) {
        if (selectedSuiteRun == null || index < 0 || index >= selectedSuiteRun.testResults.size()) {
            return;
        }
        selectedResult = (TestResult) selectedSuiteRun.testResults.elementAt(index);
        detailForm.deleteAll();
        detailForm.removeCommand(rerunCommand);
        detailForm.removeCommand(backCommand);
        if (selectedResult.getSeverity() == TestCase.MANUAL
                || selectedResult.getSeverity() == TestCase.INFO) {
            detailForm.addCommand(rerunCommand);
        }
        detailForm.addCommand(backCommand);
        detailForm.setCommandListener(this);
        fillDetail(selectedResult);
        go(detailForm);
    }

    private void fillDetail(TestResult result) {
        detailForm.setTitle("Test detail");
        detailForm.append(new StringItem("status", result.getStatusText()
                + " (" + Results.severityName(result.getSeverity())
                + ", " + result.getMillis() + " ms)"));
        if (selectedSuiteRun != null) {
            detailForm.append(new StringItem("suite", selectedSuiteRun.suite.getId()));
        }
        detailForm.append(new StringItem("test", result.getName()));
        if (result.getTest().getDescription().length() > 0) {
            detailForm.append(new StringItem("checks", result.getTest().getDescription()));
        }
        detailForm.append(new StringItem("result", result.getMessage() == null ? "-" : result.getMessage()));
    }

    private void rerun(final TestCase test) {
        detailForm.setTitle("Running...");
        runningSingle = true;
        Thread thread = new Thread(new Runnable() {
            public void run() {
                final TestResult fresh = TestRunner.execute(test);
                Results.announceTest("single", fresh);
                display.callSerially(new Runnable() {
                    public void run() {
                        runningSingle = false;
                        detailForm.deleteAll();
                        fillDetail(fresh);
                    }
                });
            }
        });
        thread.start();
    }

    private static String describe(TestCase test, TestResult result) {
        StringBuffer sb = new StringBuffer();
        sb.append("status:   ").append(result.getStatusText()).append('\n');
        sb.append("severity: ").append(Results.severityName(test.getSeverity())).append('\n');
        sb.append("time:     ").append(result.getMillis()).append(" ms\n");
        if (test.getDescription().length() > 0) {
            sb.append("\nchecks: ").append(test.getDescription()).append('\n');
        }
        if (result.getMessage() != null) {
            sb.append("\n").append(result.getMessage()).append('\n');
        }
        return sb.toString();
    }

    public void commandAction(Command c, Displayable d) {
        if (c == backCommand) {
            goBack();
            return;
        }
        if (c == stopCommand) {
            if (runner != null) {
                runner.cancel();
            }
            showRunResults();
            return;
        }
        if (c == runAllCommand) {
            runAll(false);
            return;
        }
        if (c == saveCommand) {
            if (results != null) {
                results.save();
                Alert alert = new Alert("Stored", "The summary was written to the RecordStore "
                        + Results.STORE_NAME + ".", null, AlertType.INFO);
                alert.setTimeout(Alert.FOREVER);
                display.setCurrent(alert, listScreen);
            }
            return;
        }
        if (c == rerunCommand) {
            if (selectedResult != null && !runningSingle) {
                rerun(selectedResult.getTest());
            }
            return;
        }
        if (c == okCommand) {
            handleSelect(d);
        }
    }

    private void handleSelect(Displayable d) {
        if (d == mainMenu) {
            int index = mainMenu.getSelectedIndex();
            if (index == 0) {
                runAll(false);
            } else if (index == 1) {
                showSuites();
            } else if (index == 2) {
                showDemos();
            } else if (index == 3) {
                showEnvironment();
            } else if (index == 4) {
                showLastStoredRun();
            } else {
                showHelp();
            }
            return;
        }
        if (d != listScreen) {
            return;
        }
        int index = listScreen.getSelectedIndex();
        if (index < 0) {
            return;
        }
        if (listKind == LIST_SUITES) {
            TestSuite suite = Suites.all()[index];
            TestSuite[] one = new TestSuite[1];
            one[0] = suite;
            runSuites(one, suite.getId(), false);
        } else if (listKind == LIST_DEMOS) {
            Displayable screen = Demos.create(index);
            if (screen != null) {
                screen.addCommand(backCommand);
                screen.setCommandListener(this);
                canvasScreen = (screen instanceof Canvas) ? (Canvas) screen : null;
                go(screen);
            }
        } else if (listKind == LIST_SUITE_RESULTS) {
            showSuiteRunResults(index);
        } else if (listKind == LIST_TEST_RESULTS) {
            showTestDetail(index);
        }
    }

    public Canvas getCanvasScreen() {
        return canvasScreen;
    }
}
