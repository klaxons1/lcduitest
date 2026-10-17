package lcdtest.checks;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.CustomItem;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.Ticker;

import lcdtest.Act;
import lcdtest.Body;
import lcdtest.Ctx;
import lcdtest.Suite;

/** Ticker and CustomItem checks. */
public final class SimpleWidgetChecks {

    private SimpleWidgetChecks() {
    }

    /** A minimal CustomItem that exposes the protected API for testing. */
    static class DemoItem extends CustomItem {
        static final int C_NONE = NONE;
        static final int C_TRAVERSE_HORIZONTAL = TRAVERSE_HORIZONTAL;
        static final int C_TRAVERSE_VERTICAL = TRAVERSE_VERTICAL;
        static final int C_KEY_PRESS = KEY_PRESS;
        static final int C_KEY_RELEASE = KEY_RELEASE;
        static final int C_KEY_REPEAT = KEY_REPEAT;
        static final int C_POINTER_PRESS = POINTER_PRESS;
        static final int C_POINTER_RELEASE = POINTER_RELEASE;
        static final int C_POINTER_DRAG = POINTER_DRAG;
        int painted;
        int lastW = -1;
        int lastH = -1;
        int changedW = -1;
        int changedH = -1;

        DemoItem(String label) {
            super(label);
        }

        protected int getMinContentWidth() {
            return 10;
        }

        protected int getMinContentHeight() {
            return 6;
        }

        protected int getPrefContentWidth(int height) {
            return height < 0 ? 20 : 24;
        }

        protected int getPrefContentHeight(int width) {
            return 8;
        }

        protected void sizeChanged(int w, int h) {
            changedW = w;
            changedH = h;
        }

        protected void paint(Graphics g, int w, int h) {
            painted++;
            lastW = w;
            lastH = h;
            g.setColor(0x00FF00);
            g.fillRect(0, 0, w, h);
        }

        int modes() {
            return getInteractionModes();
        }

        boolean doTraverse(int dir, int vw, int vh, int[] rect) {
            return traverse(dir, vw, vh, rect);
        }

        void doRepaint() {
            repaint();
        }

        void doInvalidate() {
            invalidate();
        }

        void doSizeChanged(int w, int h) {
            sizeChanged(w, h);
        }
    }

    public static void register(Suite s) {
        final String G = "Simple";

        // ---------------------------------------------------------------
        // Ticker
        // ---------------------------------------------------------------

        s.add("TCK-001", G, "Ticker stores its string",
                "Ticker(String)/getString/setString", new Body() {
                    public void run(Ctx c) {
                        Ticker t = new Ticker("hello");
                        c.eqStr("string", "hello", t.getString());
                        t.setString("world");
                        c.eqStr("set string", "world", t.getString());
                        t.setString("");
                        c.eqStr("empty string allowed", "", t.getString());
                    }
                });

        s.add("TCK-002", G, "Ticker rejects null strings",
                "Ticker: NPE for null in constructor and setString", new Body() {
                    public void run(Ctx c) {
                        c.throwsEx("ctor null", NullPointerException.class, new Act() {
                            public void run() {
                                new Ticker(null);
                            }
                        });
                        final Ticker t = new Ticker("x");
                        c.throwsEx("setString(null)", NullPointerException.class, new Act() {
                            public void run() {
                                t.setString(null);
                            }
                        });
                    }
                });

        s.add("TCK-003", G, "a Ticker can be attached and removed",
                "Displayable.setTicker/getTicker: identity and null clearing",
                new Body() {
                    public void run(Ctx c) {
                        Form f = new Form("f");
                        c.isNull("no ticker initially", f.getTicker());
                        Ticker t = new Ticker("news");
                        f.setTicker(t);
                        c.same("ticker attached", t, f.getTicker());
                        f.setTicker(null);
                        c.isNull("ticker removed", f.getTicker());
                    }
                });

        s.add("TCK-004", G, "one Ticker may be shared",
                "Displayable: the same Ticker instance on several screens", new Body() {
                    public void run(Ctx c) {
                        Ticker t = new Ticker("shared");
                        Form a = new Form("a");
                        Form b = new Form("b");
                        a.setTicker(t);
                        b.setTicker(t);
                        c.same("a", t, a.getTicker());
                        c.same("b", t, b.getTicker());
                        b.setTicker(null);
                        c.same("a unaffected", t, a.getTicker());
                    }
                });

        // ---------------------------------------------------------------
        // CustomItem
        // ---------------------------------------------------------------

        s.add("CUS-001", G, "CustomItem supports the Item API",
                "CustomItem: label, layout and preferred size", new Body() {
                    public void run(Ctx c) {
                        DemoItem it = new DemoItem("demo");
                        c.eqStr("label", "demo", it.getLabel());
                        c.eqInt("layout", Item.LAYOUT_DEFAULT, it.getLayout());
                        it.setLayout(Item.LAYOUT_CENTER | Item.LAYOUT_VEXPAND);
                        c.isTrue("CENTER retained",
                                (it.getLayout() & Item.LAYOUT_CENTER) != 0);
                        it.setPreferredSize(40, 20);
                        c.eqInt("preferred width", 40, it.getPreferredWidth());
                        c.eqInt("preferred height", 20, it.getPreferredHeight());
                    }
                });

        s.add("CUS-002", G, "CustomItem content sizes feed the Item size",
                "CustomItem: getMinimumWidth/Height are consistent with content",
                new Body() {
                    public void run(Ctx c) {
                        DemoItem it = new DemoItem(null);
                        c.atLeast("minimum width", it.getMinimumWidth(), 0);
                        c.atLeast("minimum height", it.getMinimumHeight(), 0);
                        it.setPreferredSize(-1, -1);
                        c.atLeast("preferred width", it.getPreferredWidth(), 0);
                        c.atLeast("preferred height", it.getPreferredHeight(), 0);
                        c.advTrue("min width at least content minimum (10)",
                                it.getMinimumWidth() >= 10);
                        c.advTrue("min height at least content minimum (6)",
                                it.getMinimumHeight() >= 6);
                    }
                });

        s.add("CUS-003", G, "interaction mode bits are documented values",
                "CustomItem.getInteractionModes: bit mask of available input",
                new Body() {
                    public void run(Ctx c) {
                        c.eqInt("NONE", 0, DemoItem.C_NONE);
                        c.eqInt("TRAVERSE_HORIZONTAL", 1, DemoItem.C_TRAVERSE_HORIZONTAL);
                        c.eqInt("TRAVERSE_VERTICAL", 2, DemoItem.C_TRAVERSE_VERTICAL);
                        c.eqInt("KEY_PRESS", 4, DemoItem.C_KEY_PRESS);
                        c.eqInt("KEY_RELEASE", 8, DemoItem.C_KEY_RELEASE);
                        c.eqInt("KEY_REPEAT", 16, DemoItem.C_KEY_REPEAT);
                        c.eqInt("POINTER_PRESS", 32, DemoItem.C_POINTER_PRESS);
                        c.eqInt("POINTER_RELEASE", 64, DemoItem.C_POINTER_RELEASE);
                        c.eqInt("POINTER_DRAG", 128, DemoItem.C_POINTER_DRAG);
                        DemoItem it = new DemoItem(null);
                        int modes = it.modes();
                        c.inRange("mode mask", modes, 0, 255);
                    }
                });

        s.add("CUS-004", G, "default traverse reports no internal traversal",
                "CustomItem.traverse: default implementation returns false", new Body() {
                    public void run(Ctx c) {
                        DemoItem it = new DemoItem(null);
                        int[] rect = {0, 0, 10, 10};
                        c.isFalse("traverse(NONE)", it.doTraverse(DemoItem.C_NONE, 50, 50,
                                rect));
                        c.isFalse("traverse(UP)", it.doTraverse(Canvas.UP, 50, 50, rect));
                        c.eqInt("rect untouched x", 0, rect[0]);
                    }
                });

        s.add("CUS-005", G, "paint and repaint APIs are usable",
                "CustomItem: repaint/invalidate/sizeChanged can be invoked", new Body() {
                    public void run(Ctx c) {
                        final DemoItem it = new DemoItem(null);
                        c.noThrow("repaint", new Act() {
                            public void run() {
                                it.doRepaint();
                            }
                        });
                        c.noThrow("invalidate", new Act() {
                            public void run() {
                                it.doInvalidate();
                            }
                        });
                        c.noThrow("sizeChanged", new Act() {
                            public void run() {
                                it.doSizeChanged(30, 12);
                            }
                        });
                        c.eqInt("sizeChanged stored width", 30, it.changedW);
                        c.eqInt("sizeChanged stored height", 12, it.changedH);
                    }
                });

        s.add("CUS-006", G, "a CustomItem can be placed in a Form",
                "Form.append(CustomItem): ownership and size queries", new Body() {
                    public void run(Ctx c) {
                        Form f = new Form("f");
                        DemoItem it = new DemoItem("custom");
                        c.eqInt("append index", 0, f.append(it));
                        c.same("stored", it, f.get(0));
                        c.eqInt("preferred width positive", it.getPreferredWidth() > 0
                                ? 1 : 0, 1);
                        f.delete(0);
                        c.eqInt("removed", 0, f.size());
                    }
                });

        s.add("CUS-007", G, "CustomItem rejects null labels explicitly?",
                "CustomItem(String label): null label is legal", new Body() {
                    public void run(Ctx c) {
                        DemoItem it = new DemoItem(null);
                        c.isNull("label", it.getLabel());
                        it.setLabel("later");
                        c.eqStr("label set", "later", it.getLabel());
                    }
                });
    }
}
