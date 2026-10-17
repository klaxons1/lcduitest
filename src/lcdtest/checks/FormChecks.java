package lcdtest.checks;

import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.ImageItem;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemStateListener;
import javax.microedition.lcdui.Spacer;
import javax.microedition.lcdui.StringItem;

import lcdtest.Act;
import lcdtest.Body;
import lcdtest.Ctx;
import lcdtest.Suite;

/** javax.microedition.lcdui.Form item-management checks. */
public final class FormChecks {

    private FormChecks() {
    }

    static StringItem str(String text) {
        return new StringItem(null, text);
    }

    public static void register(Suite s) {
        final String G = "Form";

        s.add("FRM-001", G, "a new Form is empty",
                "Form: size() == 0, get() out of range", new Body() {
                    public void run(Ctx c) {
                        final Form f = new Form("form");
                        c.eqInt("size", 0, f.size());
                        c.eqStr("title", "form", f.getTitle());
                        c.throwsEx("get(0)", IndexOutOfBoundsException.class, new Act() {
                            public void run() {
                                f.get(0);
                            }
                        });
                        c.throwsEx("get(-1)", IndexOutOfBoundsException.class, new Act() {
                            public void run() {
                                f.get(-1);
                            }
                        });
                    }
                });

        s.add("FRM-002", G, "append(Item) grows the Form and returns the index",
                "Form.append(Item): returns assigned index, size grows", new Body() {
                    public void run(Ctx c) {
                        Form f = new Form("f");
                        Item a = str("a");
                        Item b = str("b");
                        c.eqInt("first index", 0, f.append(a));
                        c.eqInt("size after first", 1, f.size());
                        c.eqInt("second index", 1, f.append(b));
                        c.eqInt("size after second", 2, f.size());
                        c.same("get(0)", a, f.get(0));
                        c.same("get(1)", b, f.get(1));
                    }
                });

        s.add("FRM-003", G, "append(String) wraps the text in a StringItem",
                "Form.append(String) == append(new StringItem(null, str))", new Body() {
                    public void run(Ctx c) {
                        Form f = new Form("f");
                        f.append("hello");
                        Item it = f.get(0);
                        c.notNull("item", it);
                        if (!(it instanceof StringItem)) {
                            c.isTrue("item is StringItem", false);
                            return;
                        }
                        StringItem si = (StringItem) it;
                        c.isNull("label", si.getLabel());
                        c.eqStr("text", "hello", si.getText());
                        c.eqInt("appearance", StringItem.PLAIN, si.getAppearanceMode());
                        c.throwsEx("append(null String)", NullPointerException.class,
                                new Act() {
                                    public void run() {
                                        new Form("x").append((String) null);
                                    }
                                });
                    }
                });

        s.add("FRM-004", G, "append(Image) wraps the image in an ImageItem",
                "Form.append(Image) == append(new ImageItem(null, img, LAYOUT_DEFAULT, null))",
                new Body() {
                    public void run(Ctx c) {
                        Form f = new Form("f");
                        Image img = Image.createImage(3, 3);
                        f.append(img);
                        Item it = f.get(0);
                        c.notNull("item", it);
                        if (!(it instanceof ImageItem)) {
                            c.isTrue("item is ImageItem", false);
                            return;
                        }
                        ImageItem ii = (ImageItem) it;
                        c.isNull("label", ii.getLabel());
                        c.notNull("image", ii.getImage());
                        if (ii.getImage() != null) {
                            c.eqInt("image width", img.getWidth(), ii.getImage().getWidth());
                            c.eqInt("image height", img.getHeight(), ii.getImage().getHeight());
                            int[] want = new int[9];
                            int[] got = new int[9];
                            img.getRGB(want, 0, 3, 0, 0, 3, 3);
                            ii.getImage().getRGB(got, 0, 3, 0, 0, 3, 3);
                            boolean eq = true;
                            for (int i = 0; i < want.length; i++) {
                                if (want[i] != got[i]) {
                                    eq = false;
                                }
                            }
                            c.isTrue("image pixels preserved", eq);
                        }
                        c.isNull("altText", ii.getAltText());
                        c.eqInt("layout", Item.LAYOUT_DEFAULT, ii.getLayout());
                        c.throwsEx("append(null Image)", NullPointerException.class, new Act() {
                            public void run() {
                                new Form("x").append((Image) null);
                            }
                        });
                    }
                });

        s.add("FRM-005", G, "append(Item) rejects null and owned items",
                "Form.append(Item): NPE null, ISE already owned", new Body() {
                    public void run(Ctx c) {
                        final Form f = new Form("f");
                        c.throwsEx("null item", NullPointerException.class, new Act() {
                            public void run() {
                                f.append((Item) null);
                            }
                        });
                        final Item it = str("x");
                        f.append(it);
                        c.throwsEx("same Form again", IllegalStateException.class, new Act() {
                            public void run() {
                                f.append(it);
                            }
                        });
                        final Form other = new Form("other");
                        c.throwsEx("owned by another Form", IllegalStateException.class,
                                new Act() {
                                    public void run() {
                                        other.append(it);
                                    }
                                });
                        c.eqInt("other form unchanged", 0, other.size());
                    }
                });

        s.add("FRM-006", G, "insert places items at the requested index",
                "Form.insert(int,Item): index range [0..size()], order preserved",
                new Body() {
                    public void run(Ctx c) {
                        Form f = new Form("f");
                        Item a = str("a");
                        Item b = str("b");
                        Item z = str("z");
                        f.append(a);
                        f.append(b);
                        f.insert(1, z);
                        c.eqInt("size", 3, f.size());
                        c.same("0", a, f.get(0));
                        c.same("1", z, f.get(1));
                        c.same("2", b, f.get(2));
                        Item tail = str("tail");
                        f.insert(f.size(), tail);
                        c.same("insert at size appends", tail, f.get(3));
                    }
                });

        s.add("FRM-007", G, "insert validates its index and item",
                "Form.insert: IOOBE outside [0..size()], NPE null, ISE owned", new Body() {
                    public void run(Ctx c) {
                        final Form f = new Form("f");
                        final Item it = str("it");
                        c.throwsEx("index -1", IndexOutOfBoundsException.class, new Act() {
                            public void run() {
                                f.insert(-1, it);
                            }
                        });
                        c.throwsEx("index size()+1", IndexOutOfBoundsException.class, new Act() {
                            public void run() {
                                f.insert(f.size() + 1, it);
                            }
                        });
                        c.throwsEx("null item", NullPointerException.class, new Act() {
                            public void run() {
                                f.insert(0, null);
                            }
                        });
                        f.append(it);
                        c.throwsEx("owned item", IllegalStateException.class, new Act() {
                            public void run() {
                                f.insert(0, it);
                            }
                        });
                    }
                });

        s.add("FRM-008", G, "delete removes the item and shifts the rest",
                "Form.delete(int): size shrinks, indices shift", new Body() {
                    public void run(Ctx c) {
                        Form f = new Form("f");
                        Item a = str("a");
                        Item b = str("b");
                        Item d = str("d");
                        f.append(a);
                        f.append(b);
                        f.append(d);
                        f.delete(1);
                        c.eqInt("size", 2, f.size());
                        c.same("0", a, f.get(0));
                        c.same("1", d, f.get(1));
                        f.delete(0);
                        c.same("delete first shifts", d, f.get(0));
                        f.delete(0);
                        c.eqInt("empty again", 0, f.size());
                    }
                });

        s.add("FRM-009", G, "delete validates its index",
                "Form.delete(int): IOOBE outside [0..size()-1]", new Body() {
                    public void run(Ctx c) {
                        final Form f = new Form("f");
                        f.append(str("a"));
                        c.throwsEx("index 1", IndexOutOfBoundsException.class, new Act() {
                            public void run() {
                                f.delete(1);
                            }
                        });
                        c.throwsEx("index -1", IndexOutOfBoundsException.class, new Act() {
                            public void run() {
                                f.delete(-1);
                            }
                        });
                        c.eqInt("size unchanged", 1, f.size());
                    }
                });

        s.add("FRM-010", G, "deleteAll empties the Form",
                "Form.deleteAll(): removes every item, safe when empty", new Body() {
                    public void run(Ctx c) {
                        final Form f = new Form("f");
                        c.noThrow("deleteAll on empty", new Act() {
                            public void run() {
                                f.deleteAll();
                            }
                        });
                        f.append("a");
                        f.append("b");
                        f.append("c");
                        f.deleteAll();
                        c.eqInt("size", 0, f.size());
                        c.throwsEx("get(0) after deleteAll", IndexOutOfBoundsException.class,
                                new Act() {
                                    public void run() {
                                        f.get(0);
                                    }
                                });
                    }
                });

        s.add("FRM-011", G, "set replaces an item in place",
                "Form.set(int,Item): equivalent to insert(n,item); delete(n+1)",
                new Body() {
                    public void run(Ctx c) {
                        Form f = new Form("f");
                        Item a = str("a");
                        Item b = str("b");
                        Item r = str("r");
                        f.append(a);
                        f.append(b);
                        f.set(0, r);
                        c.eqInt("size unchanged", 2, f.size());
                        c.same("replacement at 0", r, f.get(0));
                        c.same("second untouched", b, f.get(1));
                        c.eqInt("old item unowned? append works", 2, f.append(a));
                    }
                });

        s.add("FRM-012", G, "set validates its index and item",
                "Form.set: IOOBE, NPE null, ISE owned", new Body() {
                    public void run(Ctx c) {
                        final Form f = new Form("f");
                        Item a = str("a");
                        Item b = str("b");
                        f.append(a);
                        f.append(b);
                        final Item extra = str("extra");
                        c.throwsEx("index size()", IndexOutOfBoundsException.class, new Act() {
                            public void run() {
                                f.set(f.size(), extra);
                            }
                        });
                        c.throwsEx("index -1", IndexOutOfBoundsException.class, new Act() {
                            public void run() {
                                f.set(-1, extra);
                            }
                        });
                        c.throwsEx("null item", NullPointerException.class, new Act() {
                            public void run() {
                                f.set(0, null);
                            }
                        });
                        final Item owned = str("owned");
                        f.append(owned);
                        c.throwsEx("owned item", IllegalStateException.class, new Act() {
                            public void run() {
                                f.set(0, owned);
                            }
                        });
                    }
                });

        s.add("FRM-013", G, "get validates its index",
                "Form.get(int): IOOBE outside [0..size()-1]", new Body() {
                    public void run(Ctx c) {
                        final Form f = new Form("f");
                        f.append("a");
                        c.throwsEx("get(1)", IndexOutOfBoundsException.class, new Act() {
                            public void run() {
                                f.get(1);
                            }
                        });
                        c.throwsEx("get(-1)", IndexOutOfBoundsException.class, new Act() {
                            public void run() {
                                f.get(-1);
                            }
                        });
                        c.noThrow("get(0)", new Act() {
                            public void run() {
                                f.get(0);
                            }
                        });
                    }
                });

        s.add("FRM-014", G, "the constructor item array is copied",
                "Form(String,Item[]): constructs from an array", new Body() {
                    public void run(Ctx c) {
                        Item a = str("a");
                        Item b = str("b");
                        Item[] items = {a, b};
                        Form f = new Form("f", items);
                        c.eqInt("size", 2, f.size());
                        c.same("0", a, f.get(0));
                        c.same("1", b, f.get(1));
                        items[0] = null;
                        c.same("items array change does not affect Form", a, f.get(0));
                    }
                });

        s.add("FRM-015", G, "ItemStateListener can be installed and removed",
                "Form.setItemStateListener: accepts a listener and null", new Body() {
                    public void run(Ctx c) {
                        final Form f = new Form("f");
                        final ItemStateListener l = new ItemStateListener() {
                            public void itemStateChanged(Item item) {
                            }
                        };
                        c.noThrow("set listener", new Act() {
                            public void run() {
                                f.setItemStateListener(l);
                            }
                        });
                        c.noThrow("clear listener", new Act() {
                            public void run() {
                                f.setItemStateListener(null);
                            }
                        });
                    }
                });

        s.add("FRM-016", G, "items keep their own layout after append",
                "Form: appending does not rewrite Item.getLayout()", new Body() {
                    public void run(Ctx c) {
                        Form f = new Form("f");
                        Spacer sp = new Spacer(2, 2);
                        sp.setLayout(Item.LAYOUT_CENTER | Item.LAYOUT_NEWLINE_BEFORE);
                        int asked = sp.getLayout();
                        f.append(sp);
                        c.eqInt("layout unchanged", asked, sp.getLayout());
                    }
                });
    }
}
