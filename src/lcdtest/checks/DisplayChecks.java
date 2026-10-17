package lcdtest.checks;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Ticker;

import lcdtest.Act;
import lcdtest.Body;
import lcdtest.Ctx;
import lcdtest.Host;
import lcdtest.Suite;

/**
 * javax.microedition.lcdui.Display and Displayable contract checks.
 *
 * Source: MIDP 2.0 javadoc, javax.microedition.lcdui.Display /
 * Displayable (JSR 118, https://nikita36078.github.io/J2ME_Docs/docs/midp-2.0/).
 */
public final class DisplayChecks {

    private DisplayChecks() {
    }

    public static void register(Suite s) {
        group(s);
    }

    private static void group(Suite s) {
        final String G = "Display";

        s.add("DSP-001", G, "getDisplay(null) throws NullPointerException",
                "Display.getDisplay(MIDlet): NPE if m is null", new Body() {
                    public void run(Ctx c) {
                        c.throwsEx("getDisplay(null)", NullPointerException.class, new Act() {
                            public void run() {
                                Display.getDisplay(null);
                            }
                        });
                    }
                });

        s.add("DSP-002", G, "getDisplay returns one stable instance per MIDlet",
                "Display class description: exactly one instance per MIDlet", new Body() {
                    public void run(Ctx c) {
                        Display a = Display.getDisplay(Host.midlet);
                        Display b = Display.getDisplay(Host.midlet);
                        c.same("getDisplay(this) repeated", a, b);
                        c.same("getDisplay(this) == running display", a, Host.display);
                    }
                });

        s.add("DSP-003", G, "numColors is at least 2",
                "Display.numColors(): B/W display has 2 colours", new Body() {
                    public void run(Ctx c) {
                        c.atLeast("numColors()", Host.display.numColors(), 2);
                    }
                });

        s.add("DSP-004", G, "monochrome display reports exactly 2 levels",
                "Display.numColors(): 'number of colours for a black and white display is 2'", new Body() {
                    public void run(Ctx c) {
                        if (!Host.display.isColor()) {
                            c.eqInt("numColors() on B/W", 2, Host.display.numColors());
                        } else {
                            c.warn("colour display; monochrome rule not applicable");
                        }
                    }
                });

        s.add("DSP-005", G, "numAlphaLevels is at least 2",
                "Display.numAlphaLevels(): minimum legal return value is 2", new Body() {
                    public void run(Ctx c) {
                        c.atLeast("numAlphaLevels()", Host.display.numAlphaLevels(), 2);
                    }
                });

        s.add("DSP-006", G, "getBorderStyle returns SOLID or DOTTED",
                "Display.getBorderStyle(boolean): returns Graphics.DOTTED or SOLID", new Body() {
                    public void run(Ctx c) {
                        int[] allowed = {Graphics.SOLID, Graphics.DOTTED};
                        c.oneOf("getBorderStyle(false)", Host.display.getBorderStyle(false), allowed);
                        c.oneOf("getBorderStyle(true)", Host.display.getBorderStyle(true), allowed);
                    }
                });

        s.add("DSP-007", G, "getColor accepts the six colour specifiers",
                "Display.getColor(int): predefined specifiers, 0x00RRGGBB", new Body() {
                    public void run(Ctx c) {
                        int[] specs = {Display.COLOR_BACKGROUND, Display.COLOR_FOREGROUND,
                                Display.COLOR_HIGHLIGHTED_BACKGROUND, Display.COLOR_HIGHLIGHTED_FOREGROUND,
                                Display.COLOR_BORDER, Display.COLOR_HIGHLIGHTED_BORDER};
                        String[] names = {"BACKGROUND", "FOREGROUND", "HL_BACKGROUND", "HL_FOREGROUND",
                                "BORDER", "HL_BORDER"};
                        for (int i = 0; i < specs.length; i++) {
                            int v = Host.display.getColor(specs[i]);
                            c.inRange("getColor(" + names[i] + ")", v, 0, 0xFFFFFF);
                        }
                    }
                });

        s.add("DSP-008", G, "getColor rejects invalid specifiers",
                "Display.getColor(int): IllegalArgumentException if specifier invalid", new Body() {
                    public void run(Ctx c) {
                        c.throwsEx("getColor(-1)", IllegalArgumentException.class, new Act() {
                            public void run() {
                                Host.display.getColor(-1);
                            }
                        });
                        c.throwsEx("getColor(6)", IllegalArgumentException.class, new Act() {
                            public void run() {
                                Host.display.getColor(6);
                            }
                        });
                    }
                });

        s.add("DSP-009", G, "best image sizes are non-negative",
                "Display.getBestImageWidth/Height(int): may be zero, must not be negative", new Body() {
                    public void run(Ctx c) {
                        int[] types = {Display.LIST_ELEMENT, Display.CHOICE_GROUP_ELEMENT, Display.ALERT};
                        String[] n = {"LIST_ELEMENT", "CHOICE_GROUP_ELEMENT", "ALERT"};
                        for (int i = 0; i < types.length; i++) {
                            c.atLeast("getBestImageWidth(" + n[i] + ")",
                                    Host.display.getBestImageWidth(types[i]), 0);
                            c.atLeast("getBestImageHeight(" + n[i] + ")",
                                    Host.display.getBestImageHeight(types[i]), 0);
                        }
                    }
                });

        s.add("DSP-010", G, "best image size rejects invalid image types",
                "Display.getBestImageWidth/Height(int): IAE if imageType illegal", new Body() {
                    public void run(Ctx c) {
                        c.throwsEx("getBestImageWidth(0)", IllegalArgumentException.class, new Act() {
                            public void run() {
                                Host.display.getBestImageWidth(0);
                            }
                        });
                        c.throwsEx("getBestImageHeight(4)", IllegalArgumentException.class, new Act() {
                            public void run() {
                                Host.display.getBestImageHeight(4);
                            }
                        });
                    }
                });

        s.add("DSP-011", G, "negative backlight duration is rejected",
                "Display.flashBacklight(int): IAE if duration is negative", new Body() {
                    public void run(Ctx c) {
                        c.throwsEx("flashBacklight(-1)", IllegalArgumentException.class, new Act() {
                            public void run() {
                                Host.display.flashBacklight(-1);
                            }
                        });
                    }
                });

        s.add("DSP-012", G, "negative vibration duration is rejected",
                "Display.vibrate(int): IAE if duration is negative", new Body() {
                    public void run(Ctx c) {
                        c.throwsEx("vibrate(-1)", IllegalArgumentException.class, new Act() {
                            public void run() {
                                Host.display.vibrate(-1);
                            }
                        });
                    }
                });

        s.add("DSP-013", G, "backlight and vibrator return boolean, never throw",
                "Display.flashBacklight/vibrate: return false when unsupported", new Body() {
                    public void run(Ctx c) {
                        boolean b = Host.display.flashBacklight(0);
                        boolean v = Host.display.vibrate(0);
                        c.isTrue("flashBacklight(0) returned a boolean", b || !b);
                        c.isTrue("vibrate(0) returned a boolean", v || !v);
                    }
                });

        s.add("DSP-014", G, "setCurrent(Alert, null) throws NPE",
                "Display.setCurrent(Alert, Displayable): NPE if nextDisplayable null", new Body() {
                    public void run(Ctx c) {
                        final Alert a = new Alert("x");
                        c.throwsEx("setCurrent(alert, null)", NullPointerException.class, new Act() {
                            public void run() {
                                Host.display.setCurrent(a, null);
                            }
                        });
                    }
                });

        s.add("DSP-015", G, "setCurrent(null, Displayable) throws NPE",
                "Display.setCurrent(Alert, Displayable): NPE if alert null", new Body() {
                    public void run(Ctx c) {
                        final Form f = new Form("x");
                        c.throwsEx("setCurrent(null, form)", NullPointerException.class, new Act() {
                            public void run() {
                                Host.display.setCurrent(null, f);
                            }
                        });
                    }
                });

        s.add("DSP-016", G, "setCurrent(Alert, Alert) throws IllegalArgumentException",
                "Display.setCurrent(Alert, Displayable): IAE if nextDisplayable is an Alert", new Body() {
                    public void run(Ctx c) {
                        final Alert a = new Alert("a");
                        final Alert b = new Alert("b");
                        c.throwsEx("setCurrent(alert, alert)", IllegalArgumentException.class, new Act() {
                            public void run() {
                                Host.display.setCurrent(a, b);
                            }
                        });
                    }
                });

        s.add("DSP-017", G, "setCurrentItem(null) throws NPE",
                "Display.setCurrentItem(Item): NPE if item is null", new Body() {
                    public void run(Ctx c) {
                        c.throwsEx("setCurrentItem(null)", NullPointerException.class, new Act() {
                            public void run() {
                                Host.display.setCurrentItem(null);
                            }
                        });
                    }
                });

        s.add("DSP-018", G, "setCurrentItem requires an owned Item",
                "Display.setCurrentItem(Item): ISE if item is not owned by a container", new Body() {
                    public void run(Ctx c) {
                        final javax.microedition.lcdui.StringItem it =
                                new javax.microedition.lcdui.StringItem("l", "t");
                        c.throwsEx("setCurrentItem(unowned)", IllegalStateException.class, new Act() {
                            public void run() {
                                Host.display.setCurrentItem(it);
                            }
                        });
                    }
                });

        s.add("DSP-019", G, "setCurrentItem rejects items inside an Alert",
                "Display.setCurrentItem(Item): ISE if item is owned by an Alert", new Body() {
                    public void run(Ctx c) {
                        final Alert alert = new Alert("a");
                        final Gauge g = new Gauge(null, false, Gauge.INDEFINITE, Gauge.CONTINUOUS_IDLE);
                        alert.setIndicator(g);
                        c.same("indicator accepted", g, alert.getIndicator());
                        c.throwsEx("setCurrentItem(alert indicator)", IllegalStateException.class, new Act() {
                            public void run() {
                                Host.display.setCurrentItem(g);
                            }
                        });
                    }
                });

        s.add("DSP-020", G, "setCurrent(null) is allowed",
                "Display.setCurrent(Displayable): null is allowed", new Body() {
                    public void run(Ctx c) {
                        c.noThrow("setCurrent(null)", new Act() {
                            public void run() {
                                Host.display.setCurrent((Displayable) null);
                            }
                        });
                    }
                });

        s.add("DSP-021", G, "display colour scheme values are plausible",
                "Display.getColor: colours matching the system colour scheme", new Body() {
                    public void run(Ctx c) {
                        int bg = Host.display.getColor(Display.COLOR_BACKGROUND);
                        int fg = Host.display.getColor(Display.COLOR_FOREGROUND);
                        if (!Host.display.isColor()) {
                            c.warn("monochrome display; contrast rule not applied");
                        } else {
                            c.advTrue("foreground differs from background", bg != fg);
                        }
                    }
                });

        // ------------------------------------------------------------------
        // Displayable
        // ------------------------------------------------------------------
        final String D = "Displayable";

        s.add("DSPL-001", D, "title get/set round-trip",
                "Displayable.getTitle/setTitle(String)", new Body() {
                    public void run(Ctx c) {
                        Form f = new Form("one");
                        c.eqStr("getTitle() initial", "one", f.getTitle());
                        f.setTitle("two");
                        c.eqStr("getTitle() after setTitle", "two", f.getTitle());
                        f.setTitle(null);
                        c.isNull("setTitle(null)", f.getTitle());
                    }
                });

        s.add("DSPL-002", D, "null title is legal in constructors",
                "Screen/Form/List/TextBox/Alert constructors: title may be null", new Body() {
                    public void run(Ctx c) {
                        Form f = new Form(null);
                        c.isNull("new Form(null).getTitle()", f.getTitle());
                        c.noThrow("new Alert(null)", new Act() {
                            public void run() {
                                new Alert(null);
                            }
                        });
                        c.noThrow("new List(null, IMPLICIT)", new Act() {
                            public void run() {
                                new javax.microedition.lcdui.List(null,
                                        javax.microedition.lcdui.Choice.IMPLICIT);
                            }
                        });
                    }
                });

        s.add("DSPL-003", D, "ticker get/set round-trip and null removal",
                "Displayable.setTicker(Ticker): null removes the ticker", new Body() {
                    public void run(Ctx c) {
                        Form f = new Form("t");
                        Ticker t = new Ticker("hello");
                        f.setTicker(t);
                        c.same("getTicker()", t, f.getTicker());
                        f.setTicker(null);
                        c.isNull("setTicker(null)", f.getTicker());
                    }
                });

        s.add("DSPL-004", D, "one Ticker can be shared by several Displayables",
                "Displayable.setTicker: same ticker may be shared", new Body() {
                    public void run(Ctx c) {
                        Ticker t = new Ticker("shared");
                        Form a = new Form("a");
                        Form b = new Form("b");
                        a.setTicker(t);
                        b.setTicker(t);
                        c.same("first displayable", t, a.getTicker());
                        c.same("second displayable", t, b.getTicker());
                        c.same("ticker identity preserved", a.getTicker(), b.getTicker());
                    }
                });

        s.add("DSPL-005", D, "addCommand(null) throws NPE",
                "Displayable.addCommand(Command): NPE if cmd is null", new Body() {
                    public void run(Ctx c) {
                        final Form f = new Form("f");
                        c.throwsEx("addCommand(null)", NullPointerException.class, new Act() {
                            public void run() {
                                f.addCommand(null);
                            }
                        });
                    }
                });

        s.add("DSPL-006", D, "removeCommand(null) does nothing",
                "Displayable.removeCommand(Command): null does nothing", new Body() {
                    public void run(Ctx c) {
                        final Form f = new Form("f");
                        c.noThrow("removeCommand(null)", new Act() {
                            public void run() {
                                f.removeCommand(null);
                            }
                        });
                    }
                });

        s.add("DSPL-007", D, "removing an absent Command is harmless",
                "Displayable.removeCommand: not present has no effect", new Body() {
                    public void run(Ctx c) {
                        Form f = new Form("f");
                        javax.microedition.lcdui.Command cmd =
                                new javax.microedition.lcdui.Command("go", javax.microedition.lcdui.Command.OK, 1);
                        f.addCommand(cmd);
                        f.removeCommand(cmd);
                        f.removeCommand(cmd);
                        c.isTrue("removeCommand twice does not throw", true);
                    }
                });

        s.add("DSPL-008", D, "null CommandListener is allowed",
                "Displayable.setCommandListener: null removes the listener", new Body() {
                    public void run(Ctx c) {
                        Form f = new Form("f");
                        f.setCommandListener(null);
                        c.isTrue("setCommandListener(null) accepted", true);
                    }
                });

        s.add("DSPL-009", D, "isShown is false for a non-current Displayable",
                "Displayable.isShown(): only true when visible", new Body() {
                    public void run(Ctx c) {
                        Form f = new Form("not-current");
                        c.isFalse("new Form isShown()", f.isShown());
                    }
                });

        s.add("DSPL-010", D, "displayable size is available before showing",
                "Displayable.getWidth/getHeight: proper result even if not shown", new Body() {
                    public void run(Ctx c) {
                        Form f = new Form("size");
                        c.atLeast("Form.getWidth()", f.getWidth(), 1);
                        c.atLeast("Form.getHeight()", f.getHeight(), 1);
                        javax.microedition.lcdui.List list =
                                new javax.microedition.lcdui.List("l", javax.microedition.lcdui.Choice.IMPLICIT);
                        c.atLeast("List.getWidth()", list.getWidth(), 1);
                        javax.microedition.lcdui.TextBox tb =
                                new javax.microedition.lcdui.TextBox(null, "", 16,
                                        javax.microedition.lcdui.TextField.ANY);
                        c.atLeast("TextBox.getWidth()", tb.getWidth(), 1);
                    }
                });

        s.add("DSPL-011", D, "adding the same Command twice is idempotent",
                "Displayable.addCommand: reference equality prevents duplicates", new Body() {
                    public void run(Ctx c) {
                        Form f = new Form("f");
                        javax.microedition.lcdui.Command cmd =
                                new javax.microedition.lcdui.Command("go", javax.microedition.lcdui.Command.OK, 1);
                        f.addCommand(cmd);
                        f.addCommand(cmd);
                        c.isTrue("duplicate addCommand accepted", true);
                    }
                });

        s.add("DSPL-012", D, "Canvas is not shown before setCurrent",
                "Displayable.isShown on an unshown Canvas", new Body() {
                    public void run(Ctx c) {
                        Canvas cv = new Canvas() {
                            protected void paint(Graphics g) {
                            }
                        };
                        c.isFalse("new Canvas isShown()", cv.isShown());
                    }
                });
    }
}
