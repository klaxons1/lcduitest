package lcdtest.checks;

import lcdtest.Act;
import lcdtest.Body;
import lcdtest.Ctx;
import lcdtest.Suite;

import javax.microedition.lcdui.Command;

/** javax.microedition.lcdui.Command contract checks. */
public final class CommandChecks {

    private CommandChecks() {
    }

    public static void register(Suite s) {
        final String G = "Command";

        s.add("CMD-001", G, "null short label is rejected",
                "Command(String, int, int): NPE if label is null", new Body() {
                    public void run(Ctx c) {
                        c.throwsEx("new Command(null, SCREEN, 1)", NullPointerException.class, new Act() {
                            public void run() {
                                new Command(null, Command.SCREEN, 1);
                            }
                        });
                        c.throwsEx("new Command(null, null, SCREEN, 1)", NullPointerException.class, new Act() {
                            public void run() {
                                new Command(null, null, Command.SCREEN, 1);
                            }
                        });
                    }
                });

        s.add("CMD-002", G, "invalid command types are rejected",
                "Command: IAE if commandType is not a valid type", new Body() {
                    public void run(Ctx c) {
                        c.throwsEx("type 0", IllegalArgumentException.class, new Act() {
                            public void run() {
                                new Command("x", 0, 1);
                            }
                        });
                        c.throwsEx("type 9", IllegalArgumentException.class, new Act() {
                            public void run() {
                                new Command("x", 9, 1);
                            }
                        });
                    }
                });

        s.add("CMD-003", G, "all eight command types are accepted",
                "Command types: SCREEN, ITEM, BACK, CANCEL, OK, STOP, HELP, EXIT", new Body() {
                    public void run(Ctx c) {
                        int[] types = {Command.SCREEN, Command.ITEM, Command.BACK, Command.CANCEL,
                                Command.OK, Command.STOP, Command.HELP, Command.EXIT};
                        for (int i = 0; i < types.length; i++) {
                            Command cmd = new Command("c", types[i], 1);
                            c.eqInt("getCommandType " + types[i], types[i], cmd.getCommandType());
                        }
                    }
                });

        s.add("CMD-004", G, "short and long labels round-trip",
                "Command.getLabel/getLongLabel: null long label allowed", new Body() {
                    public void run(Ctx c) {
                        Command a = new Command("short", Command.SCREEN, 1);
                        c.eqStr("getLabel()", "short", a.getLabel());
                        c.isNull("getLongLabel() default", a.getLongLabel());
                        Command b = new Command("s", "long", Command.ITEM, 2);
                        c.eqStr("getLabel() with long label", "s", b.getLabel());
                        c.eqStr("getLongLabel()", "long", b.getLongLabel());
                    }
                });

        s.add("CMD-005", G, "priority round-trips",
                "Command.getPriority(): application-chosen priority", new Body() {
                    public void run(Ctx c) {
                        Command a = new Command("a", Command.OK, 3);
                        c.eqInt("getPriority()", 3, a.getPriority());
                        Command b = new Command("b", "b", Command.ITEM, 7);
                        c.eqInt("getPriority() with long label", 7, b.getPriority());
                    }
                });

        s.add("CMD-006", G, "command type constants have the specified values",
                "Command constants per MIDP 2.0", new Body() {
                    public void run(Ctx c) {
                        c.eqInt("SCREEN", 1, Command.SCREEN);
                        c.eqInt("BACK", 2, Command.BACK);
                        c.eqInt("CANCEL", 3, Command.CANCEL);
                        c.eqInt("OK", 4, Command.OK);
                        c.eqInt("HELP", 5, Command.HELP);
                        c.eqInt("STOP", 6, Command.STOP);
                        c.eqInt("EXIT", 7, Command.EXIT);
                        c.eqInt("ITEM", 8, Command.ITEM);
                    }
                });

        s.add("CMD-007", G, "commands with the same fields are distinct objects",
                "Command has identity semantics; no equals() contract", new Body() {
                    public void run(Ctx c) {
                        Command a = new Command("same", Command.OK, 1);
                        Command b = new Command("same", Command.OK, 1);
                        c.isFalse("a.equals(b) is false", a.equals(b));
                        c.isFalse("b.equals(a) is false", b.equals(a));
                        c.isTrue("a.equals(a) is true", a.equals(a));
                    }
                });

        s.add("CMD-008", G, "empty and long labels are accepted",
                "Command labels are arbitrary strings", new Body() {
                    public void run(Ctx c) {
                        c.noThrow("empty label", new Act() {
                            public void run() {
                                new Command("", Command.SCREEN, 1);
                            }
                        });
                        StringBuffer b = new StringBuffer();
                        for (int i = 0; i < 200; i++) {
                            b.append('x');
                        }
                        c.noThrow("200-char label", new Act() {
                            public void run() {
                                new Command("x", Command.SCREEN, 1);
                            }
                        });
                    }
                });
    }
}
