package lcdtest.checks;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.ImageItem;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.Spacer;
import javax.microedition.lcdui.StringItem;

import lcdtest.Act;
import lcdtest.Body;
import lcdtest.Ctx;
import lcdtest.Suite;

/** StringItem, ImageItem and Spacer checks. */
public final class WidgetChecks {

    private WidgetChecks() {
    }

    public static void register(Suite s) {
        final String G = "Widget";

        // ---------------------------------------------------------------
        // StringItem
        // ---------------------------------------------------------------

        s.add("SIT-001", G, "StringItem text is readable and writable",
                "StringItem: ctor text, getText/setText(null)", new Body() {
                    public void run(Ctx c) {
                        StringItem si = new StringItem("label", "text");
                        c.eqStr("label", "label", si.getLabel());
                        c.eqStr("text", "text", si.getText());
                        si.setText("other");
                        c.eqStr("setText", "other", si.getText());
                        si.setText(null);
                        c.isTrue("null text leaves the item empty",
                                si.getText() == null || si.getText().length() == 0);
                    }
                });

        s.add("SIT-002", G, "two-argument constructor is PLAIN",
                "StringItem(String,String) == StringItem(String,String,PLAIN)", new Body() {
                    public void run(Ctx c) {
                        StringItem a = new StringItem("l", "t");
                        StringItem b = new StringItem("l", "t", Item.PLAIN);
                        c.eqInt("appearance a", Item.PLAIN, a.getAppearanceMode());
                        c.eqInt("appearance b", Item.PLAIN, b.getAppearanceMode());
                        c.eqInt("HYPERLINK value", 1, Item.HYPERLINK);
                        c.eqInt("BUTTON value", 2, Item.BUTTON);
                    }
                });

        s.add("SIT-003", G, "appearance mode is validated",
                "StringItem ctor: IAE for an invalid appearance mode", new Body() {
                    public void run(Ctx c) {
                        c.noThrow("HYPERLINK", new Act() {
                            public void run() {
                                new StringItem("l", "t", Item.HYPERLINK);
                            }
                        });
                        c.noThrow("BUTTON", new Act() {
                            public void run() {
                                new StringItem("l", "t", Item.BUTTON);
                            }
                        });
                        c.throwsEx("mode 3", IllegalArgumentException.class, new Act() {
                            public void run() {
                                new StringItem("l", "t", 3);
                            }
                        });
                        c.throwsEx("mode -1", IllegalArgumentException.class, new Act() {
                            public void run() {
                                new StringItem("l", "t", -1);
                            }
                        });
                    }
                });

        s.add("SIT-004", G, "fonts can be set and cleared",
                "StringItem.setFont/getFont: null restores the default", new Body() {
                    public void run(Ctx c) {
                        StringItem si = new StringItem(null, "text");
                        Font bold = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD,
                                Font.SIZE_LARGE);
                        si.setFont(bold);
                        c.same("font set", bold, si.getFont());
                        si.setFont(null);
                        c.notNull("default font", si.getFont());
                        c.same("default font is Font.getDefaultFont()",
                                Font.getDefaultFont(), si.getFont());
                    }
                });

        s.add("SIT-005", G, "StringItem is a normal Item",
                "StringItem: layout, preferred size and commands", new Body() {
                    public void run(Ctx c) {
                        StringItem si = new StringItem("l", "text");
                        c.eqInt("default layout", Item.LAYOUT_DEFAULT, si.getLayout());
                        si.setLayout(Item.LAYOUT_CENTER | Item.LAYOUT_NEWLINE_BEFORE);
                        c.isTrue("CENTER retained",
                                (si.getLayout() & Item.LAYOUT_CENTER) != 0);
                        si.setPreferredSize(50, 20);
                        c.eqInt("preferred width", 50, si.getPreferredWidth());
                        c.eqInt("preferred height", 20, si.getPreferredHeight());
                        final StringItem f = si;
                        c.noThrow("addCommand", new Act() {
                            public void run() {
                                f.addCommand(new Command("c", Command.ITEM, 1));
                            }
                        });
                    }
                });

        s.add("SIT-006", G, "empty StringItems still report a size",
                "StringItem(null,null): minimum sizes are non-negative", new Body() {
                    public void run(Ctx c) {
                        StringItem si = new StringItem(null, null);
                        c.atLeast("min width", si.getMinimumWidth(), 0);
                        c.atLeast("min height", si.getMinimumHeight(), 0);
                        c.atLeast("preferred width", si.getPreferredWidth(), 0);
                        c.atLeast("preferred height", si.getPreferredHeight(), 0);
                    }
                });

        // ---------------------------------------------------------------
        // ImageItem
        // ---------------------------------------------------------------

        s.add("IMI-001", G, "ImageItem stores its image and layout",
                "ImageItem(String,Image,int,String): getImage/getLayout/getAltText",
                new Body() {
                    public void run(Ctx c) {
                        Image img = Image.createImage(4, 3);
                        ImageItem ii = new ImageItem("l", img, Item.LAYOUT_CENTER, "alt");
                        c.eqStr("label", "l", ii.getLabel());
                        c.notNull("image", ii.getImage());
                        c.eqInt("width", 4, ii.getImage().getWidth());
                        c.eqInt("height", 3, ii.getImage().getHeight());
                        c.eqInt("layout", Item.LAYOUT_CENTER, ii.getLayout());
                        c.eqStr("alt text", "alt", ii.getAltText());
                        c.eqInt("appearance", Item.PLAIN, ii.getAppearanceMode());
                    }
                });

        s.add("IMI-002", G, "four-argument constructor is PLAIN",
                "ImageItem(label,img,layout,alt) == ...(layout,alt,PLAIN)", new Body() {
                    public void run(Ctx c) {
                        Image img = Image.createImage(2, 2);
                        ImageItem a = new ImageItem(null, img, Item.LAYOUT_DEFAULT, null);
                        ImageItem b = new ImageItem(null, img, Item.LAYOUT_DEFAULT, null,
                                Item.PLAIN);
                        c.eqInt("a appearance", Item.PLAIN, a.getAppearanceMode());
                        c.eqInt("b appearance", Item.PLAIN, b.getAppearanceMode());
                    }
                });

        s.add("IMI-003", G, "ImageItem validates layout and appearance",
                "ImageItem: IAE for illegal layout or appearance mode", new Body() {
                    public void run(Ctx c) {
                        final Image img = Image.createImage(2, 2);
                        c.throwsEx("layout 0x10000", IllegalArgumentException.class,
                                new Act() {
                                    public void run() {
                                        new ImageItem(null, img, 0x10000, null);
                                    }
                                });
                        c.throwsEx("appearance 5", IllegalArgumentException.class,
                                new Act() {
                                    public void run() {
                                        new ImageItem(null, img, Item.LAYOUT_DEFAULT,
                                                null, 5);
                                    }
                                });
                    }
                });

        s.add("IMI-004", G, "ImageItem accepts a null image",
                "ImageItem(null image): getImage null, sizes still valid", new Body() {
                    public void run(Ctx c) {
                        ImageItem ii = new ImageItem("label", null, Item.LAYOUT_DEFAULT,
                                "alt");
                        c.isNull("image", ii.getImage());
                        c.eqStr("alt", "alt", ii.getAltText());
                        c.atLeast("minimum width", ii.getMinimumWidth(), 0);
                        c.atLeast("minimum height", ii.getMinimumHeight(), 0);
                    }
                });

        s.add("IMI-005", G, "a mutable ImageItem image is snapshotted",
                "ImageItem: later painting does not alter the stored image", new Body() {
                    public void run(Ctx c) throws Exception {
                        Image m = Image.createImage(3, 3);
                        int[] before = new int[9];
                        m.getRGB(before, 0, 3, 0, 0, 3, 3);
                        ImageItem ii = new ImageItem(null, m, Item.LAYOUT_DEFAULT, null);
                        javax.microedition.lcdui.Graphics mg = m.getGraphics();
                        mg.setColor(0xFF0000);
                        mg.fillRect(0, 0, 3, 3);
                        Image got = ii.getImage();
                        c.notNull("image", got);
                        if (got == null) {
                            return;
                        }
                        int[] after = new int[9];
                        got.getRGB(after, 0, 3, 0, 0, 3, 3);
                        boolean same = true;
                        for (int i = 0; i < before.length; i++) {
                            if (before[i] != after[i]) {
                                same = false;
                            }
                        }
                        c.isTrue("snapshot taken at set time", same);
                    }
                });

        s.add("IMI-006", G, "alt text and setImage update the item",
                "ImageItem.setAltText/setImage: values are stored", new Body() {
                    public void run(Ctx c) {
                        ImageItem ii = new ImageItem(null, null, Item.LAYOUT_DEFAULT,
                                null);
                        ii.setAltText("replacement");
                        c.eqStr("alt", "replacement", ii.getAltText());
                        ii.setAltText(null);
                        c.isNull("alt cleared", ii.getAltText());
                        Image one = Image.createImage(2, 2);
                        Image two = Image.createImage(5, 5);
                        ii.setImage(one);
                        c.notNull("image set", ii.getImage());
                        ii.setImage(two);
                        c.eqInt("replaced width", 5, ii.getImage().getWidth());
                        ii.setImage(null);
                        c.isNull("image cleared", ii.getImage());
                    }
                });

        s.add("IMI-007", G, "appearance mode is fixed by the constructor",
                "ImageItem.getAppearanceMode: BUTTON and HYPERLINK report back",
                new Body() {
                    public void run(Ctx c) {
                        ImageItem b = new ImageItem(null, null, Item.LAYOUT_DEFAULT,
                                null, Item.BUTTON);
                        c.eqInt("BUTTON", Item.BUTTON, b.getAppearanceMode());
                        ImageItem h = new ImageItem(null, null, Item.LAYOUT_DEFAULT,
                                null, Item.HYPERLINK);
                        c.eqInt("HYPERLINK", Item.HYPERLINK, h.getAppearanceMode());
                    }
                });

        s.add("IMI-008", G, "a null image has no content size",
                "ImageItem: null image and null label yield zero minimum height",
                new Body() {
                    public void run(Ctx c) {
                        ImageItem ii = new ImageItem(null, null, Item.LAYOUT_DEFAULT,
                                null);
                        c.advEqInt("min height", 0, ii.getMinimumHeight());
                        c.advEqInt("min width", 0, ii.getMinimumWidth());
                    }
                });

        // ---------------------------------------------------------------
        // Spacer
        // ---------------------------------------------------------------

        s.add("SPC-001", G, "Spacer reports its minimum size",
                "Spacer(int,int): getMinimumWidth/getMinimumHeight", new Body() {
                    public void run(Ctx c) {
                        Spacer sp = new Spacer(12, 5);
                        c.eqInt("min width", 12, sp.getMinimumWidth());
                        c.eqInt("min height", 5, sp.getMinimumHeight());
                        c.atLeast("preferred width", sp.getPreferredWidth(), 12);
                        c.atLeast("preferred height", sp.getPreferredHeight(), 5);
                    }
                });

        s.add("SPC-002", G, "Spacer rejects negative sizes",
                "Spacer ctor/setMinimumSize: IAE for a negative dimension", new Body() {
                    public void run(Ctx c) {
                        c.throwsEx("negative width", IllegalArgumentException.class,
                                new Act() {
                                    public void run() {
                                        new Spacer(-1, 4);
                                    }
                                });
                        c.throwsEx("negative height", IllegalArgumentException.class,
                                new Act() {
                                    public void run() {
                                        new Spacer(4, -1);
                                    }
                                });
                        final Spacer sp = new Spacer(1, 1);
                        c.throwsEx("setMinimumSize(-1,2)",
                                IllegalArgumentException.class, new Act() {
                                    public void run() {
                                        sp.setMinimumSize(-1, 2);
                                    }
                                });
                        c.throwsEx("setMinimumSize(2,-1)",
                                IllegalArgumentException.class, new Act() {
                                    public void run() {
                                        sp.setMinimumSize(2, -1);
                                    }
                                });
                    }
                });

        s.add("SPC-003", G, "setMinimumSize changes the reported size",
                "Spacer.setMinimumSize: values take effect", new Body() {
                    public void run(Ctx c) {
                        Spacer sp = new Spacer(0, 0);
                        sp.setMinimumSize(20, 7);
                        c.eqInt("min width", 20, sp.getMinimumWidth());
                        c.eqInt("min height", 7, sp.getMinimumHeight());
                        c.atLeast("preferred width", sp.getPreferredWidth(), 20);
                        c.atLeast("preferred height", sp.getPreferredHeight(), 7);
                    }
                });

        s.add("SPC-004", G, "Spacer rejects labels",
                "Spacer.setLabel: always IllegalStateException", new Body() {
                    public void run(Ctx c) {
                        final Spacer sp = new Spacer(1, 1);
                        c.isNull("initial label", sp.getLabel());
                        c.throwsEx("setLabel(\"x\")", IllegalStateException.class,
                                new Act() {
                                    public void run() {
                                        sp.setLabel("x");
                                    }
                                });
                        c.throwsEx("setLabel(null)", IllegalStateException.class, new Act() {
                            public void run() {
                                sp.setLabel(null);
                            }
                        });
                    }
                });

        s.add("SPC-005", G, "Spacer rejects commands",
                "Spacer.addCommand/setDefaultCommand: always ISE", new Body() {
                    public void run(Ctx c) {
                        final Spacer sp = new Spacer(1, 1);
                        final Command cmd = new Command("c", Command.ITEM, 1);
                        c.throwsEx("addCommand", IllegalStateException.class, new Act() {
                            public void run() {
                                sp.addCommand(cmd);
                            }
                        });
                        c.throwsEx("setDefaultCommand", IllegalStateException.class,
                                new Act() {
                                    public void run() {
                                        sp.setDefaultCommand(cmd);
                                    }
                                });
                    }
                });

        s.add("SPC-006", G, "Spacer supports layout and preferred size",
                "Spacer: inherited Item layout and size APIs", new Body() {
                    public void run(Ctx c) {
                        Spacer sp = new Spacer(2, 2);
                        sp.setLayout(Item.LAYOUT_RIGHT | Item.LAYOUT_BOTTOM);
                        c.isTrue("RIGHT retained",
                                (sp.getLayout() & Item.LAYOUT_RIGHT) != 0);
                        sp.setPreferredSize(30, 15);
                        c.eqInt("preferred width", 30, sp.getPreferredWidth());
                        c.eqInt("preferred height", 15, sp.getPreferredHeight());
                    }
                });
    }
}
