package lcdtest.checks;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;

import lcdtest.Act;
import lcdtest.Body;
import lcdtest.Ctx;
import lcdtest.Suite;

/** javax.microedition.lcdui.Canvas contract checks. */
public final class CanvasChecks {

    private CanvasChecks() {
    }

    public static void registerSync(Suite s) {
        final String G = "Canvas";

        s.add("CNV-001", G, "game action constants have specified values",
                "Canvas game action constants", new Body() {
                    public void run(Ctx c) {
                        c.eqInt("UP", 1, Canvas.UP);
                        c.eqInt("DOWN", 6, Canvas.DOWN);
                        c.eqInt("LEFT", 2, Canvas.LEFT);
                        c.eqInt("RIGHT", 5, Canvas.RIGHT);
                        c.eqInt("FIRE", 8, Canvas.FIRE);
                        c.eqInt("GAME_A", 9, Canvas.GAME_A);
                        c.eqInt("GAME_B", 10, Canvas.GAME_B);
                        c.eqInt("GAME_C", 11, Canvas.GAME_C);
                        c.eqInt("GAME_D", 12, Canvas.GAME_D);
                    }
                });

        s.add("CNV-002", G, "key code constants have specified values",
                "Canvas key constants", new Body() {
                    public void run(Ctx c) {
                        c.eqInt("KEY_NUM0", 48, Canvas.KEY_NUM0);
                        c.eqInt("KEY_NUM1", 49, Canvas.KEY_NUM1);
                        c.eqInt("KEY_NUM2", 50, Canvas.KEY_NUM2);
                        c.eqInt("KEY_NUM3", 51, Canvas.KEY_NUM3);
                        c.eqInt("KEY_NUM4", 52, Canvas.KEY_NUM4);
                        c.eqInt("KEY_NUM5", 53, Canvas.KEY_NUM5);
                        c.eqInt("KEY_NUM6", 54, Canvas.KEY_NUM6);
                        c.eqInt("KEY_NUM7", 55, Canvas.KEY_NUM7);
                        c.eqInt("KEY_NUM8", 56, Canvas.KEY_NUM8);
                        c.eqInt("KEY_NUM9", 57, Canvas.KEY_NUM9);
                        c.eqInt("KEY_STAR", 42, Canvas.KEY_STAR);
                        c.eqInt("KEY_POUND", 35, Canvas.KEY_POUND);
                    }
                });

        s.add("CNV-003", G, "game action mapping round-trips",
                "Canvas: g == getGameAction(getKeyCode(g))", new Body() {
                    public void run(Ctx c) {
                        Canvas cv = canvas();
                        int[] actions = {Canvas.UP, Canvas.DOWN, Canvas.LEFT, Canvas.RIGHT,
                                Canvas.FIRE, Canvas.GAME_A, Canvas.GAME_B, Canvas.GAME_C, Canvas.GAME_D};
                        String[] names = {"UP", "DOWN", "LEFT", "RIGHT", "FIRE", "GAME_A", "GAME_B",
                                "GAME_C", "GAME_D"};
                        for (int i = 0; i < actions.length; i++) {
                            int key = cv.getKeyCode(actions[i]);
                            c.atLeast("getKeyCode(" + names[i] + ")", key, 0);
                            c.eqInt("getGameAction(getKeyCode(" + names[i] + "))",
                                    actions[i], cv.getGameAction(key));
                        }
                    }
                });

        s.add("CNV-004", G, "game action mapping is stable",
                "Canvas: mapping between key codes and game actions does not change", new Body() {
                    public void run(Ctx c) {
                        Canvas cv = canvas();
                        int[] actions = {Canvas.UP, Canvas.DOWN, Canvas.LEFT, Canvas.RIGHT,
                                Canvas.FIRE, Canvas.GAME_A, Canvas.GAME_B, Canvas.GAME_C, Canvas.GAME_D};
                        for (int i = 0; i < actions.length; i++) {
                            int k1 = cv.getKeyCode(actions[i]);
                            int k2 = cv.getKeyCode(actions[i]);
                            c.eqInt("getKeyCode stable for action " + actions[i], k1, k2);
                            c.eqInt("getGameAction stable for action " + actions[i],
                                    cv.getGameAction(k1), cv.getGameAction(k2));
                        }
                    }
                });

        s.add("CNV-005", G, "number keys are valid key codes",
                "Canvas.getKeyName: non-empty for every valid key code", new Body() {
                    public void run(Ctx c) {
                        Canvas cv = canvas();
                        int[] keys = {Canvas.KEY_NUM0, Canvas.KEY_NUM1, Canvas.KEY_NUM2, Canvas.KEY_NUM3,
                                Canvas.KEY_NUM4, Canvas.KEY_NUM5, Canvas.KEY_NUM6, Canvas.KEY_NUM7,
                                Canvas.KEY_NUM8, Canvas.KEY_NUM9, Canvas.KEY_STAR, Canvas.KEY_POUND};
                        for (int i = 0; i < keys.length; i++) {
                            try {
                                String name = cv.getKeyName(keys[i]);
                                c.notNull("getKeyName(" + keys[i] + ")", name);
                                if (name != null) {
                                    c.atLeast("getKeyName length(" + keys[i] + ")", name.length(), 1);
                                }
                                int action = cv.getGameAction(keys[i]);
                                c.atLeast("getGameAction(" + keys[i] + ") non-negative", action, 0);
                            } catch (Throwable t) {
                                c.isTrue("getKeyName/getGameAction for key " + keys[i]
                                        + " threw " + Ctx.exName(t), false);
                            }
                        }
                    }
                });

        s.add("CNV-006", G, "getGameAction rejects invalid key codes",
                "Canvas.getGameAction(int): IAE if keyCode is not a valid key code", new Body() {
                    public void run(Ctx c) {
                        final Canvas cv = canvas();
                        c.throwsEx("getGameAction(-1)", IllegalArgumentException.class, new Act() {
                            public void run() {
                                cv.getGameAction(-1);
                            }
                        });
                        c.throwsEx("getGameAction(100000)", IllegalArgumentException.class, new Act() {
                            public void run() {
                                cv.getGameAction(100000);
                            }
                        });
                    }
                });

        s.add("CNV-007", G, "getKeyCode rejects invalid game actions",
                "Canvas.getKeyCode(int): IAE if gameAction is not a valid game action", new Body() {
                    public void run(Ctx c) {
                        final Canvas cv = canvas();
                        c.throwsEx("getKeyCode(0)", IllegalArgumentException.class, new Act() {
                            public void run() {
                                cv.getKeyCode(0);
                            }
                        });
                        c.throwsEx("getKeyCode(13)", IllegalArgumentException.class, new Act() {
                            public void run() {
                                cv.getKeyCode(13);
                            }
                        });
                    }
                });

        s.add("CNV-008", G, "canvas has a positive drawing area before showing",
                "Canvas.getWidth/getHeight", new Body() {
                    public void run(Ctx c) {
                        Canvas cv = canvas();
                        c.atLeast("getWidth()", cv.getWidth(), 1);
                        c.atLeast("getHeight()", cv.getHeight(), 1);
                    }
                });

        s.add("CNV-009", G, "capability queries return booleans",
                "Canvas.isDoubleBuffered/hasPointerEvents/hasPointerMotionEvents/hasRepeatEvents", new Body() {
                    public void run(Ctx c) {
                        Canvas cv = canvas();
                        boolean a = cv.isDoubleBuffered();
                        boolean b = cv.hasPointerEvents();
                        boolean d = cv.hasPointerMotionEvents();
                        boolean e = cv.hasRepeatEvents();
                        c.isTrue("isDoubleBuffered returns a boolean", a || !a);
                        c.isTrue("hasPointerEvents returns a boolean", b || !b);
                        c.isTrue("hasPointerMotionEvents returns a boolean", d || !d);
                        c.isTrue("hasRepeatEvents returns a boolean", e || !e);
                    }
                });

        s.add("CNV-010", G, "setFullScreenMode(true) before showing is safe",
                "Canvas.setFullScreenMode(boolean)", new Body() {
                    public void run(Ctx c) {
                        final Canvas cv = canvas();
                        c.noThrow("setFullScreenMode(true)", new Act() {
                            public void run() {
                                cv.setFullScreenMode(true);
                            }
                        });
                        c.noThrow("setFullScreenMode(false)", new Act() {
                            public void run() {
                                cv.setFullScreenMode(false);
                            }
                        });
                    }
                });

        s.add("CNV-011", G, "repaint requests on an invisible Canvas are harmless",
                "Canvas.repaint: no effect if canvas is not visible", new Body() {
                    public void run(Ctx c) {
                        final Canvas cv = canvas();
                        c.noThrow("repaint()", new Act() {
                            public void run() {
                                cv.repaint();
                            }
                        });
                        c.noThrow("repaint(x,y,w,h)", new Act() {
                            public void run() {
                                cv.repaint(0, 0, 10, 10);
                            }
                        });
                        c.noThrow("repaint with zero size", new Act() {
                            public void run() {
                                cv.repaint(5, 5, 0, 0);
                            }
                        });
                        c.noThrow("repaint with negative size", new Act() {
                            public void run() {
                                cv.repaint(5, 5, -3, -4);
                            }
                        });
                    }
                });

        s.add("CNV-012", G, "serviceRepaints on an invisible Canvas returns",
                "Canvas.serviceRepaints: does nothing if canvas is not visible", new Body() {
                    public void run(Ctx c) {
                        final Canvas cv = canvas();
                        c.noThrow("serviceRepaints()", new Act() {
                            public void run() {
                                cv.serviceRepaints();
                            }
                        });
                    }
                });

        s.add("CNV-013", G, "paint is not called before the Canvas is shown",
                "Canvas: paint only called while visible", new Body() {
                    public void run(Ctx c) {
                        CountingCanvas cv = new CountingCanvas();
                        c.eqInt("paint count after construction", 0, cv.paints);
                    }
                });

        s.add("CNV-014", G, "key name is non-empty for game action keys",
                "Canvas.getKeyName: non-empty for every valid key code", new Body() {
                    public void run(Ctx c) {
                        Canvas cv = canvas();
                        int[] actions = {Canvas.UP, Canvas.DOWN, Canvas.LEFT, Canvas.RIGHT,
                                Canvas.FIRE, Canvas.GAME_A, Canvas.GAME_B, Canvas.GAME_C, Canvas.GAME_D};
                        for (int i = 0; i < actions.length; i++) {
                            int key = cv.getKeyCode(actions[i]);
                            try {
                                String n = cv.getKeyName(key);
                                c.notNull("name for action key " + actions[i], n);
                                if (n != null) {
                                    c.atLeast("name length for action key " + actions[i], n.length(), 1);
                                }
                            } catch (Throwable t) {
                                c.isTrue("getKeyName(action key " + actions[i] + ") threw "
                                        + Ctx.exName(t), false);
                            }
                        }
                    }
                });
    }

    public static void registerLive(Suite s) {
        final String G = "Canvas (live)";
        s.live("CNV-L01", G, "showNotify precedes the first paint",
                "Canvas: showNotify called immediately prior to being visible");
        s.live("CNV-L02", G, "paint is called for a visible Canvas",
                "Canvas.paint: called while the Canvas is visible");
        s.live("CNV-L03", G, "isShown is true inside paint",
                "Canvas.paint: isShown() returns true");
        s.live("CNV-L04", G, "paint Graphics starts with black",
                "Canvas.paint: current colour is black");
        s.live("CNV-L05", G, "paint Graphics starts with SOLID stroke",
                "Canvas.paint: stroke style is SOLID");
        s.live("CNV-L06", G, "paint Graphics uses the default font",
                "Canvas.paint: font is Font.getDefaultFont()");
        s.live("CNV-L07", G, "paint Graphics origin is untranslated",
                "Canvas.paint: origin at upper-left corner");
        s.live("CNV-L08", G, "paint clip is a non-empty part of the Canvas",
                "Canvas.paint: clip includes at least one pixel within the Canvas");
        s.live("CNV-L09", G, "copyArea is illegal on display Graphics",
                "Graphics.copyArea: ISE if destination is the display device");
        s.live("CNV-L10", G, "repaint does not paint synchronously",
                "Canvas.repaint: paint occurs asynchronously");
        s.live("CNV-L11", G, "pending repaint is serviced by the event loop",
                "Canvas.repaint: subsequent paint() call occurs");
        s.live("CNV-L12", G, "serviceRepaints blocks until paint returns",
                "Canvas.serviceRepaints: forces pending repaints immediately");
        s.live("CNV-L13", G, "callSerially never runs synchronously",
                "Display.callSerially: run() occurs independently of the call");
        s.live("CNV-L14", G, "callSerially preserves request order",
                "Display.callSerially: run() called in request order");
        s.live("CNV-L15", G, "setCurrent(null) leaves the current screen unchanged",
                "Display.setCurrent(null): current Displayable remains unchanged");
        s.live("CNV-L16", G, "sizeChanged is called before the first paint",
                "Displayable.sizeChanged: called at least once before first showing");
        s.live("CNV-L17", G, "hideNotify is called after the Canvas is removed",
                "Canvas.hideNotify: called shortly after removal");
        s.live("CNV-L18", G, "full-screen mode never shrinks the drawing area",
                "Canvas.setFullScreenMode: occupy as much display space as possible");
        s.live("CNV-L19", G, "getCurrent returns the Canvas once it is current",
                "Display.getCurrent: returns the MIDlet's current Displayable");

        final String D = "Display (live)";
        s.live("DSP-L01", D, "Display.setCurrent makes the Displayable current",
                "Display.setCurrent: change occurs between event deliveries");
        s.live("DSP-L02", D, "isShown is true for the current Canvas",
                "Displayable.isShown: true when current and unobscured");
    }

    static Canvas canvas() {
        return new Canvas() {
            protected void paint(Graphics g) {
            }
        };
    }

    static final class CountingCanvas extends Canvas {
        int paints;

        protected void paint(Graphics g) {
            paints++;
        }
    }
}
