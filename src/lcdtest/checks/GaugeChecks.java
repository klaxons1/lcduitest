package lcdtest.checks;

import javax.microedition.lcdui.Gauge;

import lcdtest.Act;
import lcdtest.Body;
import lcdtest.Ctx;
import lcdtest.Suite;

/** javax.microedition.lcdui.Gauge checks. */
public final class GaugeChecks {

    private GaugeChecks() {
    }

    public static void register(Suite s) {
        final String G = "Gauge";

        s.add("GAU-001", G, "interactive Gauge reports its range",
                "Gauge(String,true,max,initial): getMaxValue/getValue/isInteractive",
                new Body() {
                    public void run(Ctx c) {
                        Gauge g = new Gauge("level", true, 10, 4);
                        c.eqStr("label", "level", g.getLabel());
                        c.isTrue("interactive", g.isInteractive());
                        c.eqInt("max", 10, g.getMaxValue());
                        c.eqInt("value", 4, g.getValue());
                        c.atLeast("minimum width", g.getMinimumWidth(), 0);
                        c.atLeast("preferred width", g.getPreferredWidth(), 0);
                    }
                });

        s.add("GAU-002", G, "initial value is clamped to the range",
                "Gauge ctor: initial < 0 becomes 0, initial > max becomes max",
                new Body() {
                    public void run(Ctx c) {
                        Gauge low = new Gauge("g", true, 10, -5);
                        c.eqInt("negative initial", 0, low.getValue());
                        Gauge high = new Gauge("g", true, 10, 99);
                        c.eqInt("oversize initial", 10, high.getValue());
                        Gauge edge = new Gauge("g", false, 5, 5);
                        c.eqInt("upper edge", 5, edge.getValue());
                    }
                });

        s.add("GAU-003", G, "Gauge constructor validates maxValue",
                "Gauge ctor: IAE for interactive max<=0 and non-interactive max<INDEFINITE",
                new Body() {
                    public void run(Ctx c) {
                        c.throwsEx("interactive max 0", IllegalArgumentException.class,
                                new Act() {
                                    public void run() {
                                        new Gauge("g", true, 0, 0);
                                    }
                                });
                        c.throwsEx("interactive max INDEFINITE",
                                IllegalArgumentException.class, new Act() {
                                    public void run() {
                                        new Gauge("g", true, Gauge.INDEFINITE, 0);
                                    }
                                });
                        c.throwsEx("non-interactive max 0",
                                IllegalArgumentException.class, new Act() {
                                    public void run() {
                                        new Gauge("g", false, 0, 0);
                                    }
                                });
                        c.throwsEx("non-interactive max -2",
                                IllegalArgumentException.class, new Act() {
                                    public void run() {
                                        new Gauge("g", false, -2, 0);
                                    }
                                });
                        c.noThrow("non-interactive INDEFINITE", new Act() {
                            public void run() {
                                new Gauge("g", false, Gauge.INDEFINITE,
                                        Gauge.CONTINUOUS_IDLE);
                            }
                        });
                    }
                });

        s.add("GAU-004", G, "indefinite Gauge state values",
                "Gauge(false,INDEFINITE,state): only documented states are legal",
                new Body() {
                    public void run(Ctx c) {
                        final Gauge g = new Gauge(null, false, Gauge.INDEFINITE,
                                Gauge.INCREMENTAL_IDLE);
                        c.isFalse("interactive", g.isInteractive());
                        c.eqInt("max", Gauge.INDEFINITE, g.getMaxValue());
                        c.eqInt("initial state", Gauge.INCREMENTAL_IDLE, g.getValue());
                        int[] states = {Gauge.CONTINUOUS_IDLE, Gauge.INCREMENTAL_IDLE,
                                Gauge.CONTINUOUS_RUNNING, Gauge.INCREMENTAL_UPDATING};
                        for (int i = 0; i < states.length; i++) {
                            g.setValue(states[i]);
                            c.eqInt("state " + states[i], states[i], g.getValue());
                        }
                        c.throwsEx("state 4", IllegalArgumentException.class, new Act() {
                            public void run() {
                                g.setValue(4);
                            }
                        });
                        c.throwsEx("state -1", IllegalArgumentException.class, new Act() {
                            public void run() {
                                g.setValue(-1);
                            }
                        });
                        c.throwsEx("ctor state 4", IllegalArgumentException.class, new Act() {
                            public void run() {
                                new Gauge(null, false, Gauge.INDEFINITE, 4);
                            }
                        });
                    }
                });

        s.add("GAU-005", G, "setValue clamps for definite ranges",
                "Gauge.setValue: negative becomes 0, oversize becomes max", new Body() {
                    public void run(Ctx c) {
                        Gauge g = new Gauge("g", true, 10, 5);
                        g.setValue(-3);
                        c.eqInt("negative clamped", 0, g.getValue());
                        g.setValue(100);
                        c.eqInt("oversize clamped", 10, g.getValue());
                        g.setValue(7);
                        c.eqInt("in range", 7, g.getValue());
                        Gauge n = new Gauge("g", false, 4, 1);
                        n.setValue(3);
                        c.eqInt("non-interactive value", 3, n.getValue());
                    }
                });

        s.add("GAU-006", G, "interactive maxValue must stay positive",
                "Gauge.setMaxValue: IAE for interactive max<=0", new Body() {
                    public void run(Ctx c) {
                        final Gauge g = new Gauge("g", true, 10, 5);
                        g.setMaxValue(20);
                        c.eqInt("new max", 20, g.getMaxValue());
                        c.throwsEx("max 0", IllegalArgumentException.class, new Act() {
                            public void run() {
                                g.setMaxValue(0);
                            }
                        });
                        c.throwsEx("max INDEFINITE", IllegalArgumentException.class,
                                new Act() {
                                    public void run() {
                                        g.setMaxValue(Gauge.INDEFINITE);
                                    }
                                });
                    }
                });

        s.add("GAU-007", G, "non-interactive Gauge may switch to an indefinite range",
                "Gauge.setMaxValue(INDEFINITE): allowed when not interactive",
                new Body() {
                    public void run(Ctx c) {
                        Gauge g = new Gauge("g", false, 10, 5);
                        g.setMaxValue(Gauge.INDEFINITE);
                        c.eqInt("indefinite max", Gauge.INDEFINITE, g.getMaxValue());
                        c.eqInt("previous value ignored", Gauge.CONTINUOUS_IDLE,
                                g.getValue());
                        g.setMaxValue(6);
                        c.eqInt("back to definite", 6, g.getMaxValue());
                        c.eqInt("value resets to zero", 0, g.getValue());
                        g.setValue(2);
                        c.eqInt("value usable", 2, g.getValue());
                        final Gauge f = g;
                        c.throwsEx("max 0 after reset", IllegalArgumentException.class,
                                new Act() {
                                    public void run() {
                                        f.setMaxValue(0);
                                    }
                                });
                    }
                });

        s.add("GAU-008", G, "shrinking maxValue clamps the current value",
                "Gauge.setMaxValue: value is reduced to the new maximum", new Body() {
                    public void run(Ctx c) {
                        Gauge g = new Gauge("g", true, 10, 9);
                        g.setMaxValue(4);
                        c.eqInt("clamped value", 4, g.getValue());
                        c.eqInt("max", 4, g.getMaxValue());
                    }
                });

        s.add("GAU-009", G, "growable maxValue leaves a valid value alone",
                "Gauge.setMaxValue: value below the new max is preserved", new Body() {
                    public void run(Ctx c) {
                        Gauge g = new Gauge("g", true, 10, 3);
                        g.setMaxValue(30);
                        c.eqInt("value preserved", 3, g.getValue());
                        c.eqInt("max", 30, g.getMaxValue());
                    }
                });

        s.add("GAU-010", G, "Gauge label and item state",
                "Gauge: setLabel/setLayout/setPreferredSize behave like Item",
                new Body() {
                    public void run(Ctx c) {
                        Gauge g = new Gauge("old", false, 8, 2);
                        g.setLabel("new");
                        c.eqStr("label", "new", g.getLabel());
                        g.setLayout(Gauge.LAYOUT_CENTER | Gauge.LAYOUT_NEWLINE_AFTER);
                        c.isTrue("layout CENTER",
                                (g.getLayout() & Gauge.LAYOUT_CENTER) != 0);
                        g.setPreferredSize(40, 10);
                        c.eqInt("preferred width", 40, g.getPreferredWidth());
                        c.eqInt("preferred height", 10, g.getPreferredHeight());
                    }
                });

        s.add("GAU-011", G, "interactivity is fixed at construction",
                "Gauge.isInteractive: true only for the interactive constructor flag",
                new Body() {
                    public void run(Ctx c) {
                        Gauge a = new Gauge("a", true, 5, 0);
                        Gauge b = new Gauge("b", false, 5, 0);
                        Gauge d = new Gauge("d", false, Gauge.INDEFINITE,
                                Gauge.CONTINUOUS_IDLE);
                        c.isTrue("a interactive", a.isInteractive());
                        c.isFalse("b not interactive", b.isInteractive());
                        c.isFalse("d not interactive", d.isInteractive());
                        a.setMaxValue(9);
                        c.isTrue("still interactive", a.isInteractive());
                    }
                });

        s.add("GAU-012", G, "indefinite state constants have documented values",
                "Gauge: CONTINUOUS_IDLE, INCREMENTAL_IDLE, CONTINUOUS_RUNNING, " +
                "INCREMENTAL_UPDATING", new Body() {
                    public void run(Ctx c) {
                        c.eqInt("INDEFINITE", -1, Gauge.INDEFINITE);
                        c.eqInt("CONTINUOUS_IDLE", 0, Gauge.CONTINUOUS_IDLE);
                        c.eqInt("INCREMENTAL_IDLE", 1, Gauge.INCREMENTAL_IDLE);
                        c.eqInt("CONTINUOUS_RUNNING", 2, Gauge.CONTINUOUS_RUNNING);
                        c.eqInt("INCREMENTAL_UPDATING", 3, Gauge.INCREMENTAL_UPDATING);
                    }
                });
    }
}
