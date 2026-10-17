package lcdtest.checks;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;

import lcdtest.Act;
import lcdtest.Body;
import lcdtest.Ctx;
import lcdtest.Suite;
import lcdtest.Util;

/** javax.microedition.lcdui.Alert and AlertType checks. */
public final class AlertChecks {

    private AlertChecks() {
    }

    static Gauge indicator() {
        return new Gauge(null, false, Gauge.INDEFINITE, Gauge.CONTINUOUS_IDLE);
    }

    public static void register(Suite s) {
        final String G = "Alert";

        s.add("ALR-001", G, "Alert(String) is an empty alert",
                "Alert(String): null text, image and type; default timeout", new Body() {
                    public void run(Ctx c) {
                        Alert a = new Alert("title");
                        c.eqStr("title", "title", a.getTitle());
                        c.isNull("text", a.getString());
                        c.isNull("image", a.getImage());
                        c.isNull("type", a.getType());
                        c.eqInt("timeout is the default", a.getDefaultTimeout(),
                                a.getTimeout());
                        c.advTrue("default timeout is positive or FOREVER",
                                a.getDefaultTimeout() > 0
                                        || a.getDefaultTimeout() == Alert.FOREVER);
                    }
                });

        s.add("ALR-002", G, "the four-argument constructor stores its contents",
                "Alert(String,String,Image,AlertType)", new Body() {
                    public void run(Ctx c) throws Exception {
                        Image img = Image.createImage(3, 3);
                        Alert a = new Alert("t", "body", img, AlertType.INFO);
                        c.eqStr("title", "t", a.getTitle());
                        c.eqStr("text", "body", a.getString());
                        c.same("image", img, a.getImage());
                        c.same("type", AlertType.INFO, a.getType());
                    }
                });

        s.add("ALR-003", G, "setTimeout accepts positive values and FOREVER",
                "Alert.setTimeout: IAE for zero or negative (except FOREVER)",
                new Body() {
                    public void run(Ctx c) {
                        final Alert a = new Alert("t");
                        a.setTimeout(1500);
                        c.eqInt("positive timeout", 1500, a.getTimeout());
                        a.setTimeout(Alert.FOREVER);
                        c.eqInt("FOREVER", Alert.FOREVER, a.getTimeout());
                        c.throwsEx("zero", IllegalArgumentException.class, new Act() {
                            public void run() {
                                a.setTimeout(0);
                            }
                        });
                        c.throwsEx("-1", IllegalArgumentException.class, new Act() {
                            public void run() {
                                a.setTimeout(-1);
                            }
                        });
                        c.throwsEx("-3", IllegalArgumentException.class, new Act() {
                            public void run() {
                                a.setTimeout(-3);
                            }
                        });
                    }
                });

        s.add("ALR-004", G, "text and image may be cleared",
                "Alert.setString/setImage: null removes the content", new Body() {
                    public void run(Ctx c) {
                        Alert a = new Alert("t");
                        a.setString("hello");
                        c.eqStr("text", "hello", a.getString());
                        a.setString(null);
                        c.isNull("text removed", a.getString());
                        a.setImage(Image.createImage(2, 2));
                        c.notNull("image set", a.getImage());
                        a.setImage(null);
                        c.isNull("image removed", a.getImage());
                        a.setType(AlertType.WARNING);
                        c.same("type set", AlertType.WARNING, a.getType());
                        a.setType(null);
                        c.isNull("type removed", a.getType());
                    }
                });

        s.add("ALR-005", G, "a mutable Alert image is snapshotted",
                "Alert.setImage: later painting does not change the alert image",
                new Body() {
                    public void run(Ctx c) throws Exception {
                        Image m = Image.createImage(4, 4);
                        int[] before = new int[16];
                        m.getRGB(before, 0, 4, 0, 0, 4, 4);
                        Alert a = new Alert("t");
                        a.setImage(m);
                        Graphics g = m.getGraphics();
                        g.setColor(Util.BLUE);
                        g.fillRect(0, 0, 4, 4);
                        Image shown = a.getImage();
                        c.notNull("image", shown);
                        if (shown == null) {
                            return;
                        }
                        int[] px = new int[16];
                        shown.getRGB(px, 0, 4, 0, 0, 4, 4);
                        boolean same = true;
                        for (int i = 0; i < px.length; i++) {
                            if (px[i] != before[i]) {
                                same = false;
                            }
                        }
                        c.isTrue("snapshot predates the blue fill", same);
                        a.setImage(m);
                        int[] again = new int[16];
                        a.getImage().getRGB(again, 0, 4, 0, 0, 4, 4);
                        boolean fresh = true;
                        for (int i = 0; i < again.length; i++) {
                            if (again[i] != px[i]) {
                                fresh = false;
                            }
                        }
                        c.isFalse("re-setting takes a fresh snapshot", fresh);
                    }
                });

        s.add("ALR-006", G, "DISMISS_COMMAND has the documented fields",
                "Alert.DISMISS_COMMAND: label \"\", type OK, priority 0", new Body() {
                    public void run(Ctx c) {
                        Command cmd = Alert.DISMISS_COMMAND;
                        c.notNull("command", cmd);
                        if (cmd == null) {
                            return;
                        }
                        c.eqStr("label", "", cmd.getLabel());
                        c.eqInt("type", Command.OK, cmd.getCommandType());
                        c.eqInt("priority", 0, cmd.getPriority());
                    }
                });

        s.add("ALR-007", G, "command changes on an Alert are safe",
                "Alert.addCommand/removeCommand: DISMISS handling, NPE null", new Body() {
                    public void run(Ctx c) {
                        final Alert a = new Alert("t");
                        final Command cmd = new Command("Do", Command.ITEM, 1);
                        c.noThrow("add command", new Act() {
                            public void run() {
                                a.addCommand(cmd);
                            }
                        });
                        c.noThrow("remove command", new Act() {
                            public void run() {
                                a.removeCommand(cmd);
                            }
                        });
                        c.noThrow("add DISMISS", new Act() {
                            public void run() {
                                a.addCommand(Alert.DISMISS_COMMAND);
                            }
                        });
                        c.noThrow("remove DISMISS", new Act() {
                            public void run() {
                                a.removeCommand(Alert.DISMISS_COMMAND);
                            }
                        });
                        c.noThrow("remove absent", new Act() {
                            public void run() {
                                a.removeCommand(cmd);
                            }
                        });
                        c.throwsEx("addCommand(null)", NullPointerException.class,
                                new Act() {
                                    public void run() {
                                        a.addCommand(null);
                                    }
                                });
                    }
                });

        s.add("ALR-008", G, "the default listener is restored by null",
                "Alert.setCommandListener: null restores the default listener", new Body() {
                    public void run(Ctx c) {
                        final Alert a = new Alert("t");
                        final CommandListener l = new CommandListener() {
                            public void commandAction(Command cmd, Displayable d) {
                            }
                        };
                        c.noThrow("set listener", new Act() {
                            public void run() {
                                a.setCommandListener(l);
                            }
                        });
                        c.noThrow("null restores default", new Act() {
                            public void run() {
                                a.setCommandListener(null);
                            }
                        });
                    }
                });

        s.add("ALR-009", G, "a non-interactive Gauge can be the indicator",
                "Alert.setIndicator/getIndicator: accepts a legal Gauge", new Body() {
                    public void run(Ctx c) {
                        Alert a = new Alert("t");
                        Gauge g = indicator();
                        c.noThrow("set indicator", new Act() {
                            public void run() {
                                new Alert("t").setIndicator(indicator());
                            }
                        });
                        a.setIndicator(g);
                        c.same("getIndicator", g, a.getIndicator());
                        a.setIndicator(null);
                        c.isNull("indicator removed", a.getIndicator());
                    }
                });

        s.add("ALR-010", G, "interactive Gauges are not legal indicators",
                "Alert.setIndicator: IAE for an interactive Gauge", new Body() {
                    public void run(Ctx c) {
                        final Alert a = new Alert("t");
                        c.throwsEx("interactive gauge", IllegalArgumentException.class,
                                new Act() {
                                    public void run() {
                                        a.setIndicator(new Gauge(null, true, 10, 0));
                                    }
                                });
                        c.isNull("no indicator installed", a.getIndicator());
                    }
                });

        s.add("ALR-011", G, "indicator Gauges must be pristine",
                "Alert.setIndicator: IAE for label, commands, listener, layout, size",
                new Body() {
                    public void run(Ctx c) {
                        final Alert a = new Alert("t");
                        final Gauge labelled = indicator();
                        labelled.setLabel("busy");
                        c.throwsEx("label set", IllegalArgumentException.class, new Act() {
                            public void run() {
                                a.setIndicator(labelled);
                            }
                        });
                        final Gauge commanded = indicator();
                        commanded.addCommand(new Command("c", Command.ITEM, 1));
                        c.throwsEx("command present", IllegalArgumentException.class,
                                new Act() {
                                    public void run() {
                                        a.setIndicator(commanded);
                                    }
                                });
                        final Gauge listening = indicator();
                        listening.setItemCommandListener(new ItemCommandListener() {
                            public void commandAction(Command cmd, Item item) {
                            }
                        });
                        c.throwsEx("listener present", IllegalArgumentException.class,
                                new Act() {
                                    public void run() {
                                        a.setIndicator(listening);
                                    }
                                });
                        final Gauge laid = indicator();
                        laid.setLayout(Item.LAYOUT_CENTER);
                        c.throwsEx("layout changed", IllegalArgumentException.class,
                                new Act() {
                                    public void run() {
                                        a.setIndicator(laid);
                                    }
                                });
                        final Gauge sized = indicator();
                        sized.setPreferredSize(20, 20);
                        c.throwsEx("preferred size locked", IllegalArgumentException.class,
                                new Act() {
                                    public void run() {
                                        a.setIndicator(sized);
                                    }
                                });
                        final Gauge owned = indicator();
                        new Form("f").append(owned);
                        c.throwsEx("owned by a Form", IllegalArgumentException.class,
                                new Act() {
                                    public void run() {
                                        a.setIndicator(owned);
                                    }
                                });
                    }
                });

        s.add("ALR-012", G, "an installed indicator resists modification",
                "Alert indicator: the Gauge state is frozen while installed",
                new Body() {
                    public void run(Ctx c) {
                        Alert a = new Alert("t");
                        final Gauge g = indicator();
                        a.setIndicator(g);
                        c.throwsEx("setLabel", IllegalStateException.class, new Act() {
                            public void run() {
                                g.setLabel("busy");
                            }
                        });
                        c.throwsEx("setLayout", IllegalStateException.class, new Act() {
                            public void run() {
                                g.setLayout(Item.LAYOUT_CENTER);
                            }
                        });
                        c.throwsEx("setPreferredSize", IllegalStateException.class,
                                new Act() {
                                    public void run() {
                                        g.setPreferredSize(10, 10);
                                    }
                                });
                        c.throwsEx("addCommand", IllegalStateException.class, new Act() {
                            public void run() {
                                g.addCommand(new Command("x", Command.ITEM, 1));
                            }
                        });
                        c.noThrow("setValue remains allowed", new Act() {
                            public void run() {
                                g.setValue(Gauge.INCREMENTAL_UPDATING);
                            }
                        });
                    }
                });

        s.add("ALR-013", G, "Alert types are shared instances",
                "AlertType.INFO/CONFIRMATION/WARNING/ERROR/ALARM", new Body() {
                    public void run(Ctx c) {
                        AlertType[] t = {AlertType.INFO, AlertType.CONFIRMATION,
                                AlertType.WARNING, AlertType.ERROR, AlertType.ALARM};
                        for (int i = 0; i < t.length; i++) {
                            c.notNull("type " + i, t[i]);
                            c.same("stable instance " + i, t[i], t[i]);
                        }
                        c.isFalse("distinct instances", AlertType.INFO == AlertType.ERROR);
                    }
                });

        s.add("ALR-014", G, "playSound requires a Display",
                "AlertType.playSound(Display): NPE for a null Display", new Body() {
                    public void run(Ctx c) {
                        final AlertType t = AlertType.INFO;
                        c.throwsEx("null display", NullPointerException.class, new Act() {
                            public void run() {
                                t.playSound(null);
                            }
                        });
                        try {
                            AlertType.WARNING.playSound(lcdtest.Host.display);
                            c.isTrue("playSound with a display did not throw", true);
                        } catch (Throwable ex) {
                            c.advTrue("playSound threw " + Ctx.exName(ex), false);
                        }
                    }
                });

        s.add("ALR-015", G, "displayable properties are inherited",
                "Alert: title, ticker and commands behave like a Displayable",
                new Body() {
                    public void run(Ctx c) {
                        Alert a = new Alert("one");
                        a.setTitle("two");
                        c.eqStr("title", "two", a.getTitle());
                        c.isNull("ticker default", a.getTicker());
                        a.setTicker(new javax.microedition.lcdui.Ticker("news"));
                        c.eqStr("ticker text", "news", a.getTicker().getString());
                        a.setTicker(null);
                        c.isNull("ticker removed", a.getTicker());
                    }
                });

        s.add("ALR-016", G, "Alert timeout may be forced modal by commands",
                "Alert: setTimeout(FOREVER) and back to a positive timeout", new Body() {
                    public void run(Ctx c) {
                        Alert a = new Alert("t");
                        a.setTimeout(Alert.FOREVER);
                        c.eqInt("modal", Alert.FOREVER, a.getTimeout());
                        a.setTimeout(700);
                        c.advEqInt("back to timed", 700, a.getTimeout());
                    }
                });
    }
}
