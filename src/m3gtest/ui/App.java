package m3gtest.ui;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;

import m3gtest.*;
import m3gtest.demos.Demos;

public class App extends MIDlet implements ExitHook, CommandListener, TestRunner.Listener {

    private Display display;
    private List mainMenu;
    private List suiteList;
    private Form envForm;
    private Form resultForm;
    private List resultsList;
    private Results results = new Results();
    private TestRunner runner;
    private Thread runThread;
    private boolean autoExit;
    private String autoSuiteId;
    private int autoRepeat;

    private static final Command CMD_SELECT = new Command("Select", Command.OK, 1);
    private static final Command CMD_BACK = new Command("Back", Command.BACK, 2);
    private static final Command CMD_EXIT = new Command("Exit", Command.EXIT, 10);
    private static final Command CMD_RUN_ALL = new Command("Run all", Command.SCREEN, 1);
    private static final Command CMD_DETAILS = new Command("Details", Command.SCREEN, 2);
    private static final Command CMD_STOP = new Command("Stop", Command.STOP, 1);

    public App() {
        display = Display.getDisplay(this);
        Ui.init(display);
    }

    protected void startApp() {
        String arg = getAppProperty("M3GTester-AutoRun");
        if (arg == null) {
            arg = getAppProperty("autorun");
        }
        if (arg != null && arg.length() > 0) {
            autoExit = true;
            parseAutoArg(arg);
            startAutorun();
            return;
        }
        showMainMenu();
    }

    protected void pauseApp() {}

    protected void destroyApp(boolean unconditional) {
        if (runner != null) {
            runner.cancel();
        }
        if (runThread != null) {
            runThread.interrupt();
        }
    }

    public void requestExit() {
        if (autoExit) {
            try {
                destroyApp(true);
                notifyDestroyed();
            } catch (Throwable t) {}
        }
    }

    private void parseAutoArg(String arg) {
        arg = arg.trim().toLowerCase();
        if (arg.equals("all") || arg.equals("*")) {
            autoSuiteId = null;
            return;
        }
        int colon = arg.indexOf(':');
        if (colon >= 0) {
            try {
                autoRepeat = Integer.parseInt(arg.substring(colon + 1).trim());
            } catch (Exception e) {
                autoRepeat = 0;
            }
            arg = arg.substring(0, colon).trim();
        }
        autoSuiteId = arg;
    }

    private void showMainMenu() {
        if (mainMenu == null) {
            mainMenu = new List("M3G Tester", List.IMPLICIT);
            mainMenu.append("Run all tests", null);
            mainMenu.append("Choose suite", null);
            mainMenu.append("Environment", null);
            mainMenu.append("Results", null);
            mainMenu.append("Visual demos", null);
            mainMenu.append("Exit", null);
            mainMenu.addCommand(CMD_EXIT);
            mainMenu.setCommandListener(this);
        }
        display.setCurrent(mainMenu);
    }

    private void showSuiteList() {
        suiteList = new List("Suites", List.IMPLICIT);
        TestSuite[] suites = Suites.all();
        for (int i = 0; i < suites.length; i++) {
            suiteList.append(suites[i].getTitle() + " (" + suites[i].size() + ")", null);
        }
        suiteList.addCommand(CMD_BACK);
        suiteList.setCommandListener(this);
        display.setCurrent(suiteList);
    }

    private void showEnv() {
        if (envForm == null) {
            envForm = new Form("Environment");
            envForm.append(new Env(this).describe());
            envForm.addCommand(CMD_BACK);
            envForm.setCommandListener(this);
        }
        display.setCurrent(envForm);
    }

    private void showResults() {
        resultsList = new List("Results", List.IMPLICIT);
        if (results.isEmpty()) {
            resultsList.append("(no results yet)", null);
        } else {
            resultsList.append("Total: " + results.total() + " Passed: " + results.passed() + " Failed: " + results.failed(), null);
            for (int i = 0; i < results.suiteCount(); i++) {
                Results.SuiteRun sr = results.suiteRun(i);
                Results.Stats st = sr.stats;
                resultsList.append(sr.suite.getTitle() + " " + st.passed + "/" + st.total + (st.failed > 0 ? " FAIL" : " OK"), null);
            }
        }
        resultsList.addCommand(CMD_BACK);
        resultsList.addCommand(CMD_DETAILS);
        resultsList.setCommandListener(this);
        display.setCurrent(resultsList);
    }

    private void showResultDetails() {
        StringBuffer sb = new StringBuffer();
        sb.append("Platform: ").append(results.getPlatform()).append("\n");
        sb.append("Time: ").append(results.getMillis()).append(" ms\n");
        sb.append("Total: ").append(results.total()).append(" Passed: ").append(results.passed()).append(" Failed: ").append(results.failed()).append("\n\n");
        for (int i = 0; i < results.suiteCount(); i++) {
            Results.SuiteRun sr = results.suiteRun(i);
            sb.append("[").append(sr.suite.getId()).append("] ").append(sr.suite.getTitle()).append("\n");
            for (int j = 0; j < sr.results.size(); j++) {
                TestResult tr = (TestResult) sr.results.elementAt(j);
                sb.append(" ").append(tr.getMark()).append(" ").append(tr.getName());
                if (tr.isFailure()) {
                    sb.append(" -> ").append(tr.getMessage());
                }
                sb.append("\n");
            }
            sb.append("\n");
        }
        resultForm = new Form("Report");
        resultForm.append(sb.toString());
        resultForm.addCommand(CMD_BACK);
        resultForm.setCommandListener(this);
        display.setCurrent(resultForm);
    }

    private void showDemos() {
        List demoList = new List("Visual demos", List.IMPLICIT);
        String[] titles = Demos.titles();
        for (int i = 0; i < titles.length; i++) {
            demoList.append(titles[i], null);
        }
        demoList.addCommand(CMD_BACK);
        final List dl = demoList;
        demoList.setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                if (c == List.SELECT_COMMAND) {
                    int idx = dl.getSelectedIndex();
                    Displayable demo = Demos.create(idx);
                    if (demo != null) {
                        demo.addCommand(CMD_BACK);
                        demo.setCommandListener(new CommandListener() {
                            public void commandAction(Command cmd, Displayable disp) {
                                if (cmd == CMD_BACK) {
                                    display.setCurrent(dl);
                                }
                            }
                        });
                        display.setCurrent(demo);
                    }
                } else if (c == CMD_BACK) {
                    showMainMenu();
                }
            }
        });
        display.setCurrent(demoList);
    }

    public void commandAction(Command c, Displayable d) {
        if (d == mainMenu) {
            if (c == CMD_EXIT || (c == List.SELECT_COMMAND && mainMenu.getSelectedIndex() == 5)) {
                destroyApp(true);
                notifyDestroyed();
            } else if (c == List.SELECT_COMMAND) {
                int idx = mainMenu.getSelectedIndex();
                if (idx == 0) {
                    startRun(null);
                } else if (idx == 1) {
                    showSuiteList();
                } else if (idx == 2) {
                    showEnv();
                } else if (idx == 3) {
                    showResults();
                } else if (idx == 4) {
                    showDemos();
                }
            } else if (c == CMD_EXIT) {
                destroyApp(true);
                notifyDestroyed();
            }
        } else if (d == suiteList) {
            if (c == CMD_BACK) {
                showMainMenu();
            } else if (c == List.SELECT_COMMAND) {
                int idx = suiteList.getSelectedIndex();
                TestSuite[] suites = Suites.all();
                if (idx >= 0 && idx < suites.length) {
                    startRun(suites[idx]);
                }
            }
        } else if (d == envForm) {
            if (c == CMD_BACK) {
                showMainMenu();
            }
        } else if (d == resultsList) {
            if (c == CMD_BACK) {
                showMainMenu();
            } else if (c == CMD_DETAILS) {
                showResultDetails();
            }
        } else if (d == resultForm) {
            if (c == CMD_BACK) {
                showResults();
            }
        }
    }

    private void startRun(final TestSuite singleSuite) {
        if (runThread != null && runThread.isAlive()) {
            return;
        }
        final Form progress = new Form("Running");
        final StringItem status = new StringItem(null, "Starting...");
        progress.append(status);
        progress.addCommand(CMD_STOP);
        progress.setCommandListener(new CommandListener() {
            public void commandAction(Command cmd, Displayable disp) {
                if (cmd == CMD_STOP) {
                    if (runner != null) {
                        runner.cancel();
                    }
                }
            }
        });
        display.setCurrent(progress);
        results = new Results();
        runner = new TestRunner(this);
        runThread = new Thread(new Runnable() {
            public void run() {
                try {
                    if (singleSuite != null) {
                        runner.run(singleSuite);
                    } else {
                        TestSuite[] suites = Suites.all();
                        for (int i = 0; i < suites.length; i++) {
                            if (runner.isCancelled()) break;
                            runner.run(suites[i]);
                        }
                    }
                } finally {
                    results = runner.getResults();
                    Ui.callSeriallyAndWait(new Runnable() {
                        public void run() {
                            showResults();
                        }
                    }, 5000);
                }
            }
        });
        runThread.start();
    }

    private void startAutorun() {
        final TestSuite[] suites = Suites.all();
        runner = new TestRunner(this);
        runThread = new Thread(new Runnable() {
            public void run() {
                try {
                    if (autoSuiteId == null) {
                        for (int i = 0; i < suites.length; i++) {
                            if (runner.isCancelled()) break;
                            runner.run(suites[i]);
                        }
                    } else {
                        for (int i = 0; i < suites.length; i++) {
                            if (suites[i].getId().equalsIgnoreCase(autoSuiteId) || suites[i].getTitle().toLowerCase().indexOf(autoSuiteId) >= 0) {
                                runner.run(suites[i]);
                                break;
                            }
                        }
                    }
                    Results r = runner.getResults();
                    System.out.println("M3GTester autorun finished: total=" + r.total() + " passed=" + r.passed() + " failed=" + r.failed() + " millis=" + r.getMillis());
                    for (int i = 0; i < r.suiteCount(); i++) {
                        Results.SuiteRun sr = r.suiteRun(i);
                        System.out.println("SUITE " + sr.suite.getId() + " " + sr.stats.passed + "/" + sr.stats.total + " failed=" + sr.stats.failed);
                        for (int j = 0; j < sr.results.size(); j++) {
                            TestResult tr = (TestResult) sr.results.elementAt(j);
                            if (tr.isFailure()) {
                                System.out.println("FAIL " + sr.suite.getId() + "." + tr.getName() + " -> " + tr.getMessage());
                            }
                        }
                    }
                } finally {
                    if (autoExit) {
                        requestExit();
                    }
                }
            }
        });
        runThread.start();
    }

    // Listener
    public void suiteStarted(TestSuite suite, int count) {
        System.out.println("START SUITE " + suite.getId() + " count=" + count);
    }

    public void testStarted(TestSuite suite, TestCase test) {
        System.out.println("START " + suite.getId() + "." + test.getName());
    }

    public void testFinished(TestSuite suite, TestResult result) {
        System.out.println(result.getMark() + " " + suite.getId() + "." + result.getName() + " " + result.getStatusText() + (result.getMessage() != null ? " -> " + result.getMessage() : "") + " " + result.getMillis() + "ms");
    }

    public void suiteFinished(TestSuite suite, Results.Stats stats) {
        System.out.println("END SUITE " + suite.getId() + " passed=" + stats.passed + " failed=" + stats.failed + " total=" + stats.total);
    }
}
