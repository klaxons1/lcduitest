package lcdtest.checks;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;

import lcdtest.Act;
import lcdtest.Body;
import lcdtest.Ctx;
import lcdtest.Suite;

/** javax.microedition.lcdui.List checks. */
public final class ListChecks {

    private ListChecks() {
    }

    static List list(int type) {
        return new List("list", type);
    }

    public static void register(Suite s) {
        final String G = "List";

        s.add("LST-001", G, "List accepts IMPLICIT, EXCLUSIVE and MULTIPLE",
                "List(String,int): POPUP and other values are rejected", new Body() {
                    public void run(Ctx c) {
                        int[] ok = {Choice.IMPLICIT, Choice.EXCLUSIVE, Choice.MULTIPLE};
                        String[] names = {"IMPLICIT", "EXCLUSIVE", "MULTIPLE"};
                        for (int i = 0; i < ok.length; i++) {
                            List l = new List("list", ok[i]);
                            c.eqInt("size of " + names[i], 0, l.size());
                            c.eqStr("title of " + names[i], "list", l.getTitle());
                            c.isNull("no ticker", l.getTicker());
                        }
                        int[] bad = {Choice.POPUP, 0, 5, -1};
                        for (int i = 0; i < bad.length; i++) {
                            final int t = bad[i];
                            c.throwsEx("type " + t, IllegalArgumentException.class,
                                    new Act() {
                                        public void run() {
                                            new List("list", t);
                                        }
                                    });
                        }
                    }
                });

        s.add("LST-002", G, "List builds from string and image arrays",
                "List(String,int,String[],Image[]): null and length rules", new Body() {
                    public void run(Ctx c) {
                        String[] items = {"a", "b"};
                        Image img = Image.createImage(2, 2);
                        List l = new List("l", Choice.EXCLUSIVE, items,
                                new Image[]{null, img});
                        c.eqInt("size", 2, l.size());
                        c.eqStr("0", "a", l.getString(0));
                        c.same("1 image", img, l.getImage(1));
                        c.noThrow("null images", new Act() {
                            public void run() {
                                new List("l", Choice.EXCLUSIVE, new String[]{"a"}, null);
                            }
                        });
                        c.throwsEx("null strings", NullPointerException.class, new Act() {
                            public void run() {
                                new List("l", Choice.EXCLUSIVE, null, null);
                            }
                        });
                        c.throwsEx("null string element", NullPointerException.class,
                                new Act() {
                                    public void run() {
                                        new List("l", Choice.EXCLUSIVE,
                                                new String[]{"a", null}, null);
                                    }
                                });
                        c.throwsEx("image length mismatch", IllegalArgumentException.class,
                                new Act() {
                                    public void run() {
                                        new List("l", Choice.EXCLUSIVE,
                                                new String[]{"a", "b"}, new Image[1]);
                                    }
                                });
                    }
                });

        s.add("LST-003", G, "SELECT_COMMAND has the documented values",
                "List.SELECT_COMMAND: label \"\", type SCREEN, priority 0", new Body() {
                    public void run(Ctx c) {
                        Command cmd = List.SELECT_COMMAND;
                        c.notNull("command", cmd);
                        if (cmd == null) {
                            return;
                        }
                        c.eqStr("label", "", cmd.getLabel());
                        c.eqInt("type", Command.SCREEN, cmd.getCommandType());
                        c.eqInt("priority", 0, cmd.getPriority());
                    }
                });

        s.add("LST-004", G, "an empty implicit List has no selection",
                "List(IMPLICIT): getSelectedIndex -1, no elements", new Body() {
                    public void run(Ctx c) {
                        List l = list(Choice.IMPLICIT);
                        c.eqInt("size", 0, l.size());
                        c.eqInt("selected", -1, l.getSelectedIndex());
                        boolean[] f = new boolean[0];
                        c.eqInt("flags count", 0, l.getSelectedFlags(f));
                    }
                });

        s.add("LST-005", G, "appending to an empty implicit List selects the element",
                "List(IMPLICIT): first element becomes selected", new Body() {
                    public void run(Ctx c) {
                        List l = list(Choice.IMPLICIT);
                        l.append("first", null);
                        c.eqInt("selected index", 0, l.getSelectedIndex());
                        l.append("second", null);
                        c.eqInt("selection stays", 0, l.getSelectedIndex());
                        l.setSelectedIndex(1, true);
                        c.eqInt("moved to 1", 1, l.getSelectedIndex());
                    }
                });

        s.add("LST-006", G, "append, insert and delete maintain order",
                "List: element management, IOOBE for bad indexes", new Body() {
                    public void run(Ctx c) {
                        List l = new List("l", Choice.IMPLICIT);
                        c.eqInt("append 0", 0, l.append("a", null));
                        c.eqInt("append 1", 1, l.append("c", null));
                        l.insert(1, "b", null);
                        c.eqInt("size", 3, l.size());
                        c.eqStr("0", "a", l.getString(0));
                        c.eqStr("1", "b", l.getString(1));
                        c.eqStr("2", "c", l.getString(2));
                        final List f = l;
                        c.throwsEx("insert(size()+1)", IndexOutOfBoundsException.class,
                                new Act() {
                                    public void run() {
                                        f.insert(f.size() + 1, "x", null);
                                    }
                                });
                        c.throwsEx("insert(-1)", IndexOutOfBoundsException.class, new Act() {
                            public void run() {
                                f.insert(-1, "x", null);
                            }
                        });
                        l.delete(1);
                        c.eqStr("after delete", "c", l.getString(1));
                        c.throwsEx("delete(size())", IndexOutOfBoundsException.class,
                                new Act() {
                                    public void run() {
                                        f.delete(f.size());
                                    }
                                });
                    }
                });

        s.add("LST-007", G, "element strings may not be null",
                "List: append/insert/set reject null strings with NPE", new Body() {
                    public void run(Ctx c) {
                        final List l = list(Choice.EXCLUSIVE);
                        l.append("a", null);
                        c.throwsEx("append(null)", NullPointerException.class, new Act() {
                            public void run() {
                                l.append(null, null);
                            }
                        });
                        c.throwsEx("insert(null)", NullPointerException.class, new Act() {
                            public void run() {
                                l.insert(0, null, null);
                            }
                        });
                        c.throwsEx("set(null)", NullPointerException.class, new Act() {
                            public void run() {
                                l.set(0, null, null);
                            }
                        });
                    }
                });

        s.add("LST-008", G, "selection survives insertion and deletion",
                "List(EXCLUSIVE): selection indexes track the element", new Body() {
                    public void run(Ctx c) {
                        List l = list(Choice.EXCLUSIVE);
                        l.append("a", null);
                        l.append("b", null);
                        l.setSelectedIndex(1, true);
                        l.insert(0, "z", null);
                        c.eqInt("shifted", 2, l.getSelectedIndex());
                        l.delete(0);
                        c.eqInt("shifted back", 1, l.getSelectedIndex());
                        c.eqStr("same element", "b", l.getString(1));
                    }
                });

        s.add("LST-009", G, "MULTIPLE selection is independent per element",
                "List(MULTIPLE): getSelectedIndex -1, flags reflect each element",
                new Body() {
                    public void run(Ctx c) {
                        List l = list(Choice.MULTIPLE);
                        l.append("a", null);
                        l.append("b", null);
                        l.append("c", null);
                        l.setSelectedIndex(0, true);
                        l.setSelectedIndex(2, true);
                        c.eqInt("getSelectedIndex", -1, l.getSelectedIndex());
                        boolean[] f = new boolean[3];
                        c.eqInt("count", 2, l.getSelectedFlags(f));
                        c.isTrue("0", f[0]);
                        c.isFalse("1", f[1]);
                        c.isTrue("2", f[2]);
                    }
                });

        s.add("LST-010", G, "setSelectedFlags picks one for an exclusive List",
                "List(EXCLUSIVE): first true wins, none true selects first", new Body() {
                    public void run(Ctx c) {
                        List l = list(Choice.EXCLUSIVE);
                        l.append("a", null);
                        l.append("b", null);
                        l.setSelectedFlags(new boolean[]{false, true});
                        c.eqInt("first true", 1, l.getSelectedIndex());
                        l.setSelectedFlags(new boolean[]{false, false});
                        c.eqInt("none true", 0, l.getSelectedIndex());
                    }
                });

        s.add("LST-011", G, "flag arrays are validated",
                "List.getSelectedFlags/setSelectedFlags: IAE short, NPE null",
                new Body() {
                    public void run(Ctx c) {
                        final List l = list(Choice.MULTIPLE);
                        l.append("a", null);
                        l.append("b", null);
                        c.throwsEx("short result", IllegalArgumentException.class,
                                new Act() {
                                    public void run() {
                                        l.getSelectedFlags(new boolean[1]);
                                    }
                                });
                        c.throwsEx("null result", NullPointerException.class, new Act() {
                            public void run() {
                                l.getSelectedFlags(null);
                            }
                        });
                        c.throwsEx("short input", IllegalArgumentException.class, new Act() {
                            public void run() {
                                l.setSelectedFlags(new boolean[1]);
                            }
                        });
                        c.throwsEx("null input", NullPointerException.class, new Act() {
                            public void run() {
                                l.setSelectedFlags(null);
                            }
                        });
                    }
                });

        s.add("LST-012", G, "deleteAll empties the List",
                "List.deleteAll: size 0, selection -1", new Body() {
                    public void run(Ctx c) {
                        List l = list(Choice.IMPLICIT);
                        l.append("a", null);
                        l.append("b", null);
                        l.deleteAll();
                        c.eqInt("size", 0, l.size());
                        c.eqInt("selection", -1, l.getSelectedIndex());
                    }
                });

        s.add("LST-013", G, "fit policy round-trips",
                "List.setFitPolicy/getFitPolicy: TEXT_WRAP_* values", new Body() {
                    public void run(Ctx c) {
                        List l = list(Choice.EXCLUSIVE);
                        c.eqInt("default", Choice.TEXT_WRAP_DEFAULT, l.getFitPolicy());
                        l.setFitPolicy(Choice.TEXT_WRAP_OFF);
                        c.advEqInt("OFF accepted", Choice.TEXT_WRAP_OFF, l.getFitPolicy());
                        l.setFitPolicy(Choice.TEXT_WRAP_ON);
                        c.advEqInt("ON accepted", Choice.TEXT_WRAP_ON, l.getFitPolicy());
                        final List f = l;
                        c.throwsEx("invalid policy", IllegalArgumentException.class,
                                new Act() {
                                    public void run() {
                                        f.setFitPolicy(42);
                                    }
                                });
                    }
                });

        s.add("LST-014", G, "fonts are hints with a default",
                "List.setFont/getFont: null restores the default", new Body() {
                    public void run(Ctx c) {
                        final List l = list(Choice.EXCLUSIVE);
                        l.append("a", null);
                        Font mono = Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_PLAIN,
                                Font.SIZE_SMALL);
                        l.setFont(0, mono);
                        c.same("font returned", mono, l.getFont(0));
                        l.setFont(0, null);
                        c.notNull("default font", l.getFont(0));
                        c.throwsEx("getFont(1)", IndexOutOfBoundsException.class, new Act() {
                            public void run() {
                                l.getFont(1);
                            }
                        });
                    }
                });

        s.add("LST-015", G, "setSelectCommand is safe on every List type",
                "List.setSelectCommand: IMPLICIT uses it, others ignore the call",
                new Body() {
                    public void run(Ctx c) {
                        final List imp = list(Choice.IMPLICIT);
                        imp.append("a", null);
                        final Command own = new Command("Open", Command.ITEM, 1);
                        c.noThrow("implicit select command", new Act() {
                            public void run() {
                                imp.setSelectCommand(own);
                            }
                        });
                        final List ex = list(Choice.EXCLUSIVE);
                        ex.append("a", null);
                        c.noThrow("exclusive ignored", new Act() {
                            public void run() {
                                ex.setSelectCommand(own);
                            }
                        });
                        c.noThrow("null select command", new Act() {
                            public void run() {
                                imp.setSelectCommand(null);
                            }
                        });
                        c.noThrow("restore SELECT_COMMAND", new Act() {
                            public void run() {
                                imp.setSelectCommand(List.SELECT_COMMAND);
                            }
                        });
                    }
                });

        s.add("LST-016", G, "set replaces both parts of an element",
                "List.set: replaces text and image, IOOBE when out of range",
                new Body() {
                    public void run(Ctx c) {
                        List l = list(Choice.EXCLUSIVE);
                        Image img = Image.createImage(2, 2);
                        l.append("a", null);
                        l.set(0, "b", img);
                        c.eqStr("string", "b", l.getString(0));
                        c.same("image", img, l.getImage(0));
                        final List f = l;
                        c.throwsEx("index 1", IndexOutOfBoundsException.class, new Act() {
                            public void run() {
                                f.set(1, "c", null);
                            }
                        });
                    }
                });

        s.add("LST-017", G, "implicit List reports a single selection",
                "List(IMPLICIT): getSelectedFlags returns exactly one true", new Body() {
                    public void run(Ctx c) {
                        List l = list(Choice.IMPLICIT);
                        l.append("a", null);
                        l.append("b", null);
                        l.append("c", null);
                        boolean[] f = new boolean[3];
                        c.eqInt("one selected", 1, l.getSelectedFlags(f));
                        l.setSelectedIndex(2, true);
                        c.eqInt("moved", 2, l.getSelectedIndex());
                        c.isFalse("previous cleared", l.isSelected(0));
                    }
                });
    }
}
