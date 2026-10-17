package lcdtest.checks;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Image;

import lcdtest.Act;
import lcdtest.Body;
import lcdtest.Ctx;
import lcdtest.Suite;

/** javax.microedition.lcdui.ChoiceGroup checks. */
public final class ChoiceChecks {

    private ChoiceChecks() {
    }

    static ChoiceGroup group(int type) {
        return new ChoiceGroup("cg", type);
    }

    static boolean[] flags(int n) {
        return new boolean[n];
    }

    public static void register(Suite s) {
        final String G = "Choice";

        s.add("CHC-001", G, "ChoiceGroup accepts EXCLUSIVE, MULTIPLE and POPUP",
                "ChoiceGroup(String,int): valid types, IMPLICIT rejected", new Body() {
                    public void run(Ctx c) {
                        int[] ok = {Choice.EXCLUSIVE, Choice.MULTIPLE, Choice.POPUP};
                        for (int i = 0; i < ok.length; i++) {
                            ChoiceGroup g = new ChoiceGroup("cg", ok[i]);
                            c.eqInt("size of type " + ok[i], 0, g.size());
                            c.eqStr("label of type " + ok[i], "cg", g.getLabel());
                        }
                        int[] bad = {Choice.IMPLICIT, 0, 5, -1};
                        for (int i = 0; i < bad.length; i++) {
                            final int t = bad[i];
                            c.throwsEx("type " + t, IllegalArgumentException.class,
                                    new Act() {
                                        public void run() {
                                            new ChoiceGroup("cg", t);
                                        }
                                    });
                        }
                        c.eqInt("empty selected index EXCLUSIVE", -1,
                                group(Choice.EXCLUSIVE).getSelectedIndex());
                        c.eqInt("empty selected index MULTIPLE", -1,
                                group(Choice.MULTIPLE).getSelectedIndex());
                    }
                });

        s.add("CHC-002", G, "ChoiceGroup builds from string and image arrays",
                "ChoiceGroup(String,int,String[],Image[]): constructor rules",
                new Body() {
                    public void run(Ctx c) {
                        String[] items = {"a", "b", "c"};
                        Image[] imgs = {null, Image.createImage(2, 2), null};
                        ChoiceGroup cg = new ChoiceGroup("l", Choice.EXCLUSIVE, items,
                                imgs);
                        c.eqInt("size", 3, cg.size());
                        c.eqStr("0", "a", cg.getString(0));
                        c.eqStr("2", "c", cg.getString(2));
                        c.isNull("image 0", cg.getImage(0));
                        c.notNull("image 1", cg.getImage(1));
                        c.noThrow("null image array", new Act() {
                            public void run() {
                                new ChoiceGroup("l", Choice.EXCLUSIVE,
                                        new String[]{"a"}, null);
                            }
                        });
                        c.throwsEx("null string array", NullPointerException.class,
                                new Act() {
                                    public void run() {
                                        new ChoiceGroup("l", Choice.EXCLUSIVE, null, null);
                                    }
                                });
                        c.throwsEx("null string element", NullPointerException.class,
                                new Act() {
                                    public void run() {
                                        new ChoiceGroup("l", Choice.EXCLUSIVE,
                                                new String[]{"a", null}, null);
                                    }
                                });
                        c.throwsEx("image length mismatch", IllegalArgumentException.class,
                                new Act() {
                                    public void run() {
                                        new ChoiceGroup("l", Choice.EXCLUSIVE,
                                                new String[]{"a", "b"},
                                                new Image[]{Image.createImage(2, 2)});
                                    }
                                });
                    }
                });

        s.add("CHC-003", G, "append returns the assigned index",
                "ChoiceGroup.append(String,Image): index grows, NPE for null string",
                new Body() {
                    public void run(Ctx c) {
                        ChoiceGroup cg = group(Choice.MULTIPLE);
                        Image img = Image.createImage(3, 3);
                        c.eqInt("index 0", 0, cg.append("a", img));
                        c.eqInt("index 1", 1, cg.append("b", null));
                        c.eqInt("size", 2, cg.size());
                        c.same("image stored", img, cg.getImage(0));
                        c.isNull("null image", cg.getImage(1));
                        final ChoiceGroup f = group(Choice.MULTIPLE);
                        c.throwsEx("null string", NullPointerException.class, new Act() {
                            public void run() {
                                f.append(null, null);
                            }
                        });
                    }
                });

        s.add("CHC-004", G, "insert validates its index",
                "ChoiceGroup.insert: IOOBE outside [0..size()], NPE null", new Body() {
                    public void run(Ctx c) {
                        final ChoiceGroup cg = group(Choice.MULTIPLE);
                        cg.append("a", null);
                        cg.append("c", null);
                        c.throwsEx("index -1", IndexOutOfBoundsException.class, new Act() {
                            public void run() {
                                cg.insert(-1, "x", null);
                            }
                        });
                        c.throwsEx("index size()+1", IndexOutOfBoundsException.class,
                                new Act() {
                                    public void run() {
                                        cg.insert(cg.size() + 1, "x", null);
                                    }
                                });
                        cg.insert(1, "b", null);
                        cg.insert(cg.size(), "d", null);
                        c.eqInt("size", 4, cg.size());
                        c.eqStr("order 0", "a", cg.getString(0));
                        c.eqStr("order 1", "b", cg.getString(1));
                        c.eqStr("order 2", "c", cg.getString(2));
                        c.eqStr("order 3", "d", cg.getString(3));
                        c.throwsEx("null string", NullPointerException.class, new Act() {
                            public void run() {
                                cg.insert(0, null, null);
                            }
                        });
                    }
                });

        s.add("CHC-005", G, "delete and deleteAll shrink the group",
                "ChoiceGroup.delete/deleteAll: IOOBE and emptying", new Body() {
                    public void run(Ctx c) {
                        ChoiceGroup cg = group(Choice.MULTIPLE);
                        cg.append("a", null);
                        cg.append("b", null);
                        cg.append("c", null);
                        cg.delete(1);
                        c.eqInt("size", 2, cg.size());
                        c.eqStr("0", "a", cg.getString(0));
                        c.eqStr("1", "c", cg.getString(1));
                        final ChoiceGroup f = cg;
                        c.throwsEx("delete(size())", IndexOutOfBoundsException.class,
                                new Act() {
                                    public void run() {
                                        f.delete(f.size());
                                    }
                                });
                        c.throwsEx("delete(-1)", IndexOutOfBoundsException.class, new Act() {
                            public void run() {
                                f.delete(-1);
                            }
                        });
                        cg.deleteAll();
                        c.eqInt("emptied", 0, cg.size());
                    }
                });

        s.add("CHC-006", G, "set replaces both parts of an element",
                "ChoiceGroup.set: string and image replaced, IOOBE/NPE", new Body() {
                    public void run(Ctx c) {
                        ChoiceGroup cg = group(Choice.MULTIPLE);
                        Image one = Image.createImage(2, 2);
                        Image two = Image.createImage(3, 3);
                        cg.append("a", one);
                        cg.set(0, "b", two);
                        c.eqStr("string replaced", "b", cg.getString(0));
                        c.same("image replaced", two, cg.getImage(0));
                        cg.set(0, "c", null);
                        c.isNull("image removed", cg.getImage(0));
                        final ChoiceGroup f = cg;
                        c.throwsEx("index out of range", IndexOutOfBoundsException.class,
                                new Act() {
                                    public void run() {
                                        f.set(1, "x", null);
                                    }
                                });
                        c.throwsEx("null string", NullPointerException.class, new Act() {
                            public void run() {
                                f.set(0, null, null);
                            }
                        });
                    }
                });

        s.add("CHC-007", G, "a new MULTIPLE group selects nothing",
                "ChoiceGroup(MULTIPLE): getSelectedIndex -1, flags all false",
                new Body() {
                    public void run(Ctx c) {
                        ChoiceGroup cg = group(Choice.MULTIPLE);
                        cg.append("a", null);
                        cg.append("b", null);
                        boolean[] f = flags(2);
                        int n = cg.getSelectedFlags(f);
                        c.eqInt("selected count", 0, n);
                        c.isFalse("0 unselected", f[0]);
                        c.isFalse("1 unselected", f[1]);
                        c.eqInt("getSelectedIndex", -1, cg.getSelectedIndex());
                    }
                });

        s.add("CHC-008", G, "MULTIPLE selection is per element",
                "ChoiceGroup(MULTIPLE): setSelectedIndex and getSelectedFlags",
                new Body() {
                    public void run(Ctx c) {
                        ChoiceGroup cg = group(Choice.MULTIPLE);
                        cg.append("a", null);
                        cg.append("b", null);
                        cg.append("c", null);
                        cg.setSelectedIndex(0, true);
                        cg.setSelectedIndex(2, true);
                        c.isTrue("0 selected", cg.isSelected(0));
                        c.isFalse("1 unselected", cg.isSelected(1));
                        c.isTrue("2 selected", cg.isSelected(2));
                        c.eqInt("getSelectedIndex still -1", -1, cg.getSelectedIndex());
                        boolean[] f = flags(3);
                        c.eqInt("selected count", 2, cg.getSelectedFlags(f));
                        cg.setSelectedIndex(0, false);
                        c.isFalse("deselect 0", cg.isSelected(0));
                        final ChoiceGroup g = cg;
                        c.throwsEx("isSelected(3)", IndexOutOfBoundsException.class,
                                new Act() {
                                    public void run() {
                                        g.isSelected(3);
                                    }
                                });
                    }
                });

        s.add("CHC-009", G, "EXCLUSIVE keeps exactly one selection",
                "ChoiceGroup(EXCLUSIVE): selecting an element clears the previous",
                new Body() {
                    public void run(Ctx c) {
                        ChoiceGroup cg = group(Choice.EXCLUSIVE);
                        cg.append("a", null);
                        cg.append("b", null);
                        cg.append("c", null);
                        cg.setSelectedIndex(1, true);
                        c.eqInt("selected index", 1, cg.getSelectedIndex());
                        cg.setSelectedIndex(2, true);
                        c.eqInt("selected index moved", 2, cg.getSelectedIndex());
                        c.isFalse("old selection cleared", cg.isSelected(1));
                        cg.setSelectedIndex(2, false);
                        c.eqInt("false is ignored", 2, cg.getSelectedIndex());
                        boolean[] f = flags(3);
                        c.eqInt("exactly one selected", 1, cg.getSelectedFlags(f));
                        c.isTrue("index 2 flagged", f[2]);
                    }
                });

        s.add("CHC-010", G, "appending to an empty EXCLUSIVE group selects it",
                "Choice: first element of an exclusive/popup choice is selected",
                new Body() {
                    public void run(Ctx c) {
                        ChoiceGroup ex = group(Choice.EXCLUSIVE);
                        ex.append("first", null);
                        c.eqInt("exclusive first selected", 0, ex.getSelectedIndex());
                        ex.append("second", null);
                        c.eqInt("new element unselected", 0, ex.getSelectedIndex());
                        ChoiceGroup pop = group(Choice.POPUP);
                        pop.append("first", null);
                        c.eqInt("popup first selected", 0, pop.getSelectedIndex());
                    }
                });

        s.add("CHC-011", G, "selection follows an element through insertion",
                "Choice: selected state is a property of the element", new Body() {
                    public void run(Ctx c) {
                        ChoiceGroup cg = group(Choice.EXCLUSIVE);
                        cg.append("a", null);
                        cg.append("b", null);
                        cg.setSelectedIndex(1, true);
                        cg.insert(0, "z", null);
                        c.eqInt("selection shifted to 2", 2, cg.getSelectedIndex());
                        c.eqStr("still the same element", "b", cg.getString(2));
                        cg.delete(0);
                        c.eqInt("selection shifted back to 1", 1, cg.getSelectedIndex());
                    }
                });

        s.add("CHC-012", G, "selected flags arrays are validated",
                "ChoiceGroup.getSelectedFlags/setSelectedFlags: IAE short, NPE null",
                new Body() {
                    public void run(Ctx c) {
                        final ChoiceGroup cg = group(Choice.MULTIPLE);
                        cg.append("a", null);
                        cg.append("b", null);
                        c.throwsEx("short result array", IllegalArgumentException.class,
                                new Act() {
                                    public void run() {
                                        cg.getSelectedFlags(new boolean[1]);
                                    }
                                });
                        c.throwsEx("null result array", NullPointerException.class,
                                new Act() {
                                    public void run() {
                                        cg.getSelectedFlags(null);
                                    }
                                });
                        c.throwsEx("short input array", IllegalArgumentException.class,
                                new Act() {
                                    public void run() {
                                        cg.setSelectedFlags(new boolean[1]);
                                    }
                                });
                        c.throwsEx("null input array", NullPointerException.class, new Act() {
                            public void run() {
                                cg.setSelectedFlags(null);
                            }
                        });
                        boolean[] longFlags = {true, false, true, true};
                        cg.setSelectedFlags(longFlags);
                        c.isTrue("0 selected", cg.isSelected(0));
                        c.isFalse("1 unselected", cg.isSelected(1));
                        boolean[] got = new boolean[4];
                        cg.getSelectedFlags(got);
                        c.isFalse("extra element cleared", got[2]);
                    }
                });

        s.add("CHC-013", G, "setSelectedFlags chooses exactly one for EXCLUSIVE",
                "ChoiceGroup(EXCLUSIVE): first true wins, none true selects first",
                new Body() {
                    public void run(Ctx c) {
                        ChoiceGroup cg = group(Choice.EXCLUSIVE);
                        cg.append("a", null);
                        cg.append("b", null);
                        cg.append("c", null);
                        boolean[] two = {false, true, true};
                        cg.setSelectedFlags(two);
                        c.eqInt("first true chosen", 1, cg.getSelectedIndex());
                        boolean[] none = {false, false, false};
                        cg.setSelectedFlags(none);
                        c.eqInt("none true selects first", 0, cg.getSelectedIndex());
                        c.isTrue("first flagged", cg.isSelected(0));
                    }
                });

        s.add("CHC-014", G, "fit policy round-trips",
                "ChoiceGroup.setFitPolicy/getFitPolicy: TEXT_WRAP_* values", new Body() {
                    public void run(Ctx c) {
                        ChoiceGroup cg = group(Choice.EXCLUSIVE);
                        c.eqInt("default policy", Choice.TEXT_WRAP_DEFAULT,
                                cg.getFitPolicy());
                        int[] vals = {Choice.TEXT_WRAP_ON, Choice.TEXT_WRAP_OFF,
                                Choice.TEXT_WRAP_DEFAULT};
                        for (int i = 0; i < vals.length; i++) {
                            cg.setFitPolicy(vals[i]);
                            c.advEqInt("policy " + vals[i], vals[i], cg.getFitPolicy());
                        }
                        final ChoiceGroup f = cg;
                        c.throwsEx("invalid policy", IllegalArgumentException.class,
                                new Act() {
                                    public void run() {
                                        f.setFitPolicy(99);
                                    }
                                });
                    }
                });

        s.add("CHC-015", G, "fonts are hints, default font always available",
                "ChoiceGroup.setFont/getFont: null restores the default", new Body() {
                    public void run(Ctx c) {
                        final ChoiceGroup cg = group(Choice.MULTIPLE);
                        cg.append("a", null);
                        cg.append("b", null);
                        final Font bold = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD,
                                Font.SIZE_MEDIUM);
                        c.noThrow("set font", new Act() {
                            public void run() {
                                cg.setFont(0, bold);
                            }
                        });
                        c.same("font reported back", bold, cg.getFont(0));
                        cg.setFont(0, null);
                        c.notNull("default font after null", cg.getFont(0));
                        final ChoiceGroup f = cg;
                        c.throwsEx("bad index", IndexOutOfBoundsException.class, new Act() {
                            public void run() {
                                f.setFont(2, bold);
                            }
                        });
                    }
                });

        s.add("CHC-016", G, "element text may not be null",
                "ChoiceGroup: strings are always non-null", new Body() {
                    public void run(Ctx c) {
                        ChoiceGroup cg = group(Choice.MULTIPLE);
                        cg.append("a", null);
                        c.notNull("string 0", cg.getString(0));
                        cg.set(0, "", null);
                        c.eqStr("empty string allowed", "", cg.getString(0));
                    }
                });

        s.add("CHC-017", G, "getString and getImage validate the index",
                "ChoiceGroup.getString/getImage: IOOBE outside range", new Body() {
                    public void run(Ctx c) {
                        final ChoiceGroup cg = group(Choice.MULTIPLE);
                        cg.append("a", null);
                        c.throwsEx("getString(1)", IndexOutOfBoundsException.class,
                                new Act() {
                                    public void run() {
                                        cg.getString(1);
                                    }
                                });
                        c.throwsEx("getImage(-1)", IndexOutOfBoundsException.class,
                                new Act() {
                                    public void run() {
                                        cg.getImage(-1);
                                    }
                                });
                    }
                });
    }
}
