package lcdtest.checks;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.Spacer;
import javax.microedition.lcdui.StringItem;

import lcdtest.Act;
import lcdtest.Body;
import lcdtest.Ctx;
import lcdtest.Suite;

/** javax.microedition.lcdui.Item contract checks. */
public final class ItemChecks {

    private ItemChecks() {
    }

    static StringItem item() {
        return new StringItem("label", "text");
    }

    public static void register(Suite s) {
        final String G = "Item";

        s.add("ITM-001", G, "label is readable and writable",
                "Item.getLabel/setLabel: null label is allowed", new Body() {
                    public void run(Ctx c) {
                        StringItem it = new StringItem("lbl", "x");
                        c.eqStr("initial label", "lbl", it.getLabel());
                        it.setLabel("other");
                        c.eqStr("set label", "other", it.getLabel());
                        it.setLabel(null);
                        c.isNull("null clears the label", it.getLabel());
                    }
                });

        s.add("ITM-002", G, "new items use the default layout",
                "Item.getLayout(): LAYOUT_DEFAULT unless changed", new Body() {
                    public void run(Ctx c) {
                        StringItem it = item();
                        c.eqInt("default layout", Item.LAYOUT_DEFAULT, it.getLayout());
                        it.setLayout(Item.LAYOUT_LEFT | Item.LAYOUT_TOP);
                        int l = it.getLayout();
                        c.isTrue("LEFT retained", (l & Item.LAYOUT_LEFT) != 0);
                        c.isTrue("TOP retained", (l & Item.LAYOUT_TOP) != 0);
                        c.isFalse("no RIGHT", (l & Item.LAYOUT_RIGHT) != 0);
                        c.isFalse("no BOTTOM", (l & Item.LAYOUT_BOTTOM) != 0);
                    }
                });

        s.add("ITM-003", G, "layout accepts every documented directive",
                "Item.setLayout: LAYOUT_* combinations are accepted", new Body() {
                    public void run(Ctx c) {
                        int[] vals = {Item.LAYOUT_DEFAULT, Item.LAYOUT_LEFT, Item.LAYOUT_RIGHT,
                                Item.LAYOUT_CENTER, Item.LAYOUT_TOP, Item.LAYOUT_BOTTOM,
                                Item.LAYOUT_VCENTER, Item.LAYOUT_NEWLINE_BEFORE,
                                Item.LAYOUT_NEWLINE_AFTER, Item.LAYOUT_SHRINK,
                                Item.LAYOUT_EXPAND, Item.LAYOUT_VSHRINK,
                                Item.LAYOUT_VEXPAND, Item.LAYOUT_2,
                                Item.LAYOUT_LEFT | Item.LAYOUT_TOP
                                        | Item.LAYOUT_NEWLINE_BEFORE
                                        | Item.LAYOUT_SHRINK};
                        for (int i = 0; i < vals.length; i++) {
                            final int v = vals[i];
                            final StringItem it = item();
                            c.noThrow("setLayout(" + v + ")", new Act() {
                                public void run() {
                                    it.setLayout(v);
                                }
                            });
                        }
                    }
                });

        s.add("ITM-004", G, "layout rejects undefined bits",
                "Item.setLayout: IAE for values that are not layout directives",
                new Body() {
                    public void run(Ctx c) {
                        final StringItem it = item();
                        c.throwsEx("bit 0x10000", IllegalArgumentException.class, new Act() {
                            public void run() {
                                it.setLayout(0x10000);
                            }
                        });
                        c.throwsEx("negative layout", IllegalArgumentException.class, new Act() {
                            public void run() {
                                it.setLayout(-1);
                            }
                        });
                    }
                });

        s.add("ITM-005", G, "preferred size can be locked and unlocked",
                "Item.setPreferredSize/getPreferredWidth/Height", new Body() {
                    public void run(Ctx c) {
                        StringItem it = item();
                        it.setPreferredSize(30, 12);
                        c.eqInt("locked width", 30, it.getPreferredWidth());
                        c.eqInt("locked height", 12, it.getPreferredHeight());
                        it.setPreferredSize(20, -1);
                        c.eqInt("locked width only", 20, it.getPreferredWidth());
                        c.atLeast("computed height", it.getPreferredHeight(), 0);
                        it.setPreferredSize(-1, -1);
                        c.atLeast("unlocked width", it.getPreferredWidth(), 0);
                        c.atLeast("unlocked height", it.getPreferredHeight(), 0);
                    }
                });

        s.add("ITM-006", G, "preferred size rejects values below -1",
                "Item.setPreferredSize: IAE if width or height < -1", new Body() {
                    public void run(Ctx c) {
                        final StringItem it = item();
                        c.throwsEx("width -2", IllegalArgumentException.class, new Act() {
                            public void run() {
                                it.setPreferredSize(-2, 10);
                            }
                        });
                        c.throwsEx("height -2", IllegalArgumentException.class, new Act() {
                            public void run() {
                                it.setPreferredSize(10, -2);
                            }
                        });
                        c.noThrow("both -1", new Act() {
                            public void run() {
                                it.setPreferredSize(-1, -1);
                            }
                        });
                    }
                });

        s.add("ITM-007", G, "minimum sizes are non-negative",
                "Item.getMinimumWidth/getMinimumHeight", new Body() {
                    public void run(Ctx c) {
                        Item[] items = {item(), new Spacer(3, 4),
                                new StringItem(null, "a long piece of text to wrap")};
                        for (int i = 0; i < items.length; i++) {
                            c.atLeast("min width #" + i, items[i].getMinimumWidth(), 0);
                            c.atLeast("min height #" + i, items[i].getMinimumHeight(), 0);
                        }
                    }
                });

        s.add("ITM-008", G, "commands can be added, re-added and removed",
                "Item.addCommand/removeCommand: duplicate add is a no-op", new Body() {
                    public void run(Ctx c) {
                        final StringItem it = item();
                        final Command cmd = new Command("Do", Command.ITEM, 1);
                        c.noThrow("addCommand", new Act() {
                            public void run() {
                                it.addCommand(cmd);
                            }
                        });
                        c.noThrow("addCommand twice", new Act() {
                            public void run() {
                                it.addCommand(cmd);
                            }
                        });
                        c.noThrow("removeCommand", new Act() {
                            public void run() {
                                it.removeCommand(cmd);
                            }
                        });
                        c.noThrow("removeCommand when absent", new Act() {
                            public void run() {
                                it.removeCommand(cmd);
                            }
                        });
                        c.throwsEx("addCommand(null)", NullPointerException.class, new Act() {
                            public void run() {
                                it.addCommand(null);
                            }
                        });
                    }
                });

        s.add("ITM-009", G, "removing the default command clears the default",
                "Item.removeCommand: equivalent to setDefaultCommand(null) + remove", new Body() {
                    public void run(Ctx c) {
                        final StringItem it = item();
                        final Command cmd = new Command("Go", Command.ITEM, 1);
                        c.noThrow("setDefaultCommand", new Act() {
                            public void run() {
                                it.setDefaultCommand(cmd);
                            }
                        });
                        c.noThrow("remove default", new Act() {
                            public void run() {
                                it.removeCommand(cmd);
                            }
                        });
                        c.noThrow("null default", new Act() {
                            public void run() {
                                it.setDefaultCommand(null);
                            }
                        });
                    }
                });

        s.add("ITM-010", G, "item command listener can be installed and removed",
                "Item.setItemCommandListener: accepts listener and null", new Body() {
                    public void run(Ctx c) {
                        final StringItem it = item();
                        final ItemCommandListener l = new ItemCommandListener() {
                            public void commandAction(Command c, Item item) {
                            }
                        };
                        c.noThrow("set listener", new Act() {
                            public void run() {
                                it.setItemCommandListener(l);
                            }
                        });
                        c.noThrow("clear listener", new Act() {
                            public void run() {
                                it.setItemCommandListener(null);
                            }
                        });
                    }
                });

        s.add("ITM-011", G, "notifyStateChanged requires a Form owner",
                "Item.notifyStateChanged: ISE when the Item is not in a Form", new Body() {
                    public void run(Ctx c) {
                        final StringItem lone = item();
                        c.throwsEx("unowned item", IllegalStateException.class, new Act() {
                            public void run() {
                                lone.notifyStateChanged();
                            }
                        });
                        Form f = new Form("f");
                        final StringItem owned = item();
                        f.append(owned);
                        c.noThrow("owned item", new Act() {
                            public void run() {
                                owned.notifyStateChanged();
                            }
                        });
                    }
                });

        s.add("ITM-012", G, "Spacer reports the minimum size it was built with",
                "Spacer: getMinimumWidth/getMinimumHeight", new Body() {
                    public void run(Ctx c) {
                        Spacer sp = new Spacer(9, 7);
                        c.eqInt("minimum width", 9, sp.getMinimumWidth());
                        c.eqInt("minimum height", 7, sp.getMinimumHeight());
                        c.atLeast("preferred width", sp.getPreferredWidth(), 0);
                        c.atLeast("preferred height", sp.getPreferredHeight(), 0);
                    }
                });

        s.add("ITM-013", G, "item size recomputes when content changes",
                "Item: getPreferredWidth/Height reflect current contents", new Body() {
                    public void run(Ctx c) {
                        StringItem it = new StringItem(null, "short");
                        int small = it.getPreferredWidth();
                        it.setText("a considerably longer string of text");
                        int large = it.getPreferredWidth();
                        c.advTrue("longer text is not narrower (" + small + " -> " + large + ")",
                                large >= small);
                    }
                });

        s.add("ITM-014", G, "setPreferredSize(0,0) clamps to the minimum size",
                "Item.setPreferredSize: values below minimum are replaced by minimum",
                new Body() {
                    public void run(Ctx c) {
                        StringItem it = item();
                        it.setPreferredSize(0, 0);
                        c.eqInt("width is minimum", it.getMinimumWidth(),
                                it.getPreferredWidth());
                        c.eqInt("height is minimum", it.getMinimumHeight(),
                                it.getPreferredHeight());
                    }
                });

        s.add("ITM-015", G, "an Item can move between Forms",
                "Form: delete then append transfers ownership", new Body() {
                    public void run(Ctx c) {
                        Form a = new Form("a");
                        final Form b = new Form("b");
                        final StringItem it = item();
                        a.append(it);
                        a.delete(0);
                        c.noThrow("append to second Form", new Act() {
                            public void run() {
                                b.append(it);
                            }
                        });
                        c.eqInt("first Form empty", 0, a.size());
                        c.eqInt("second Form size", 1, b.size());
                        c.same("same instance", it, b.get(0));
                    }
                });
    }
}
