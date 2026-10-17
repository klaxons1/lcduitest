package lcdtest;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.CustomItem;
import javax.microedition.lcdui.DateField;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.ImageItem;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.Spacer;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;
import javax.microedition.lcdui.Ticker;

/** Visual demos used for manual inspection. */
public final class Gallery {

    private Gallery() {
    }

    static Image swatch(int w, int h) {
        Image im = Image.createImage(w, h);
        Graphics g = im.getGraphics();
        g.setColor(0xFFFFFF);
        g.fillRect(0, 0, w, h);
        g.setColor(0xFF0000);
        g.fillRect(0, 0, w / 2, h / 2);
        g.setColor(0x0000FF);
        g.fillRect(w / 2, 0, w - w / 2, h / 2);
        g.setColor(0x00A000);
        g.fillRect(0, h / 2, w, h - h / 2);
        return im;
    }

    static Form zoo(final Display display, final MenuCanvas menu) {
        Form f = new Form("Component zoo");
        f.setTicker(new Ticker("Ticker: LCDUI conformance gallery.  "
                + "This text should scroll smoothly."));
        f.append(new StringItem("StringItem", "Plain text"));
        StringItem link = new StringItem("Hyperlink", "tap / select me",
                Item.HYPERLINK);
        link.setDefaultCommand(new Command("Open", Command.ITEM, 1));
        link.setItemCommandListener(new javax.microedition.lcdui.ItemCommandListener() {
            public void commandAction(Command c, Item item) {
                display.setCurrent(new Alert("Hyperlink", "Item command fired",
                        null, AlertType.INFO), menu);
            }
        });
        f.append(link);
        StringItem button = new StringItem("Button", "Press", Item.BUTTON);
        button.setDefaultCommand(new Command("Press", Command.ITEM, 1));
        button.setItemCommandListener(new javax.microedition.lcdui.ItemCommandListener() {
            public void commandAction(Command c, Item item) {
                display.setCurrent(new Alert("Button", "Button command fired",
                        null, AlertType.CONFIRMATION), menu);
            }
        });
        f.append(button);
        f.append(new ImageItem("ImageItem", swatch(24, 24), Item.LAYOUT_CENTER,
                "swatch"));
        f.append(new TextField("ANY", "hello", 20, TextField.ANY));
        f.append(new TextField("NUMERIC", "123", 10, TextField.NUMERIC));
        f.append(new TextField("PASSWORD", "secret", 20, TextField.PASSWORD));
        f.append(new TextField("EMAIL", "a@b.c", 40, TextField.EMAILADDR));
        f.append(new TextField("UNEDITABLE", "read only", 20,
                TextField.UNEDITABLE));
        f.append(new DateField("DATE", DateField.DATE));
        f.append(new DateField("TIME", DateField.TIME));
        f.append(new DateField("DATE_TIME", DateField.DATE_TIME));
        f.append(new Gauge("Interactive gauge", true, 10, 4));
        Gauge busy = new Gauge("Indefinite gauge", false, Gauge.INDEFINITE,
                Gauge.INCREMENTAL_UPDATING);
        f.append(busy);
        ChoiceGroup ex = new ChoiceGroup("EXCLUSIVE", Choice.EXCLUSIVE,
                new String[]{"one", "two", "three"}, null);
        f.append(ex);
        ChoiceGroup mul = new ChoiceGroup("MULTIPLE", Choice.MULTIPLE,
                new String[]{"alpha", "beta", "gamma"}, null);
        f.append(mul);
        ChoiceGroup pop = new ChoiceGroup("POPUP", Choice.POPUP,
                new String[]{"red", "green", "blue"}, null);
        f.append(pop);
        f.append(new Spacer(8, 8));
        f.append(new DemoItem());
        f.addCommand(new Command("Back", Command.BACK, 1));
        f.setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                display.setCurrent(menu);
            }
        });
        return f;
    }

    static TextBox textBox(final Display display, final MenuCanvas menu) {
        TextBox tb = new TextBox("TextBox", "Edit this text with the device "
                + "keyboard.\nTry the constraints and the maximum size.",
                300, TextField.ANY);
        tb.addCommand(new Command("Back", Command.BACK, 1));
        tb.setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                display.setCurrent(menu);
            }
        });
        return tb;
    }

    static List list(final Display display, final MenuCanvas menu, int type) {
        String title = type == Choice.IMPLICIT ? "IMPLICIT list"
                : type == Choice.EXCLUSIVE ? "EXCLUSIVE list" : "MULTIPLE list";
        List l = new List(title, type, new String[]{"First", "Second", "Third",
                "Fourth", "Fifth"}, null);
        final Command back = new Command("Back", Command.BACK, 1);
        l.addCommand(back);
        if (type == Choice.IMPLICIT) {
            l.setSelectCommand(new Command("Open", Command.ITEM, 2));
        }
        l.setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                if (c == back) {
                    display.setCurrent(menu);
                } else {
                    List src = (List) d;
                    int idx = src.getSelectedIndex();
                    String text = idx < 0 ? "no selection"
                            : "selected index " + idx + ": "
                                    + src.getString(idx);
                    display.setCurrent(new Alert("Selection", text, null,
                            AlertType.INFO), src);
                }
            }
        });
        return l;
    }

    static void showAlert(final Display display, final MenuCanvas menu, int kind) {
        if (kind == 5) {
            Alert a = new Alert("Activity", "An indefinite Gauge is shown "
                    + "as the alert indicator.", null, AlertType.INFO);
            Gauge g = new Gauge(null, false, Gauge.INDEFINITE,
                    Gauge.CONTINUOUS_RUNNING);
            try {
                a.setIndicator(g);
            } catch (Throwable t) {
                a.setString("setIndicator threw " + Ctx.exText(t));
            }
            display.setCurrent(a, menu);
            return;
        }
        AlertType[] types = {AlertType.INFO, AlertType.WARNING, AlertType.ERROR,
                AlertType.ALARM, AlertType.CONFIRMATION};
        String[] titles = {"Information", "Warning", "Error", "Alarm",
                "Confirmation"};
        Alert a = new Alert(titles[kind], "This is a " + titles[kind]
                + " alert.\nCheck the icon, the sound and the timeout.",
                swatch(16, 16), types[kind]);
        if (kind == 4) {
            a.setTimeout(Alert.FOREVER);
            a.addCommand(new Command("Yes", Command.OK, 1));
            a.addCommand(new Command("No", Command.CANCEL, 2));
            a.setCommandListener(new CommandListener() {
                public void commandAction(Command c, Displayable d) {
                    display.setCurrent(menu);
                }
            });
        }
        display.setCurrent(a, menu);
    }

    // ------------------------------------------------------------------
    // Graphics primitives
    // ------------------------------------------------------------------

    static final class Primitives extends Canvas implements CommandListener {

        private final Display display;
        private final MenuCanvas menu;
        private int page;
        private final Command back = new Command("Back", Command.BACK, 1);
        private final Command next = new Command("Next", Command.SCREEN, 2);

        Primitives(Display display, MenuCanvas menu) {
            this.display = display;
            this.menu = menu;
            addCommand(back);
            addCommand(next);
            setCommandListener(this);
            setTitle("Graphics primitives");
        }

        protected void paint(Graphics g) {
            int w = getWidth();
            int h = getHeight();
            g.setColor(0xFFFFFF);
            g.fillRect(0, 0, w, h);
            g.setColor(0x000000);
            g.drawString("Page " + (page + 1) + "/4", w / 2, 1,
                    Graphics.TOP | Graphics.HCENTER);
            switch (page) {
                case 0: drawLines(g, w, h); break;
                case 1: drawArcs(g, w, h); break;
                case 2: drawText(g, w, h); break;
                default: drawImages(g, w, h); break;
            }
        }

        private void drawLines(Graphics g, int w, int h) {
            int y = 16;
            g.setColor(0x000000);
            g.drawLine(4, y, w - 4, y);
            g.setStrokeStyle(Graphics.DOTTED);
            g.drawLine(4, y + 6, w - 4, y + 6);
            g.setStrokeStyle(Graphics.SOLID);
            g.drawRect(4, y + 14, 40, 18);
            g.fillRect(50, y + 14, 40, 18);
            g.drawRoundRect(96, y + 14, 40, 18, 8, 8);
            g.fillRoundRect(142, y + 14, 40, 18, 8, 8);
            g.setColor(0xFF0000);
            g.drawRect(4, y + 40, 30, 20);
            g.setColor(0x0000FF);
            g.fillRect(8, y + 44, 30, 20);
        }

        private void drawArcs(Graphics g, int w, int h) {
            g.setColor(0x000000);
            g.drawArc(4, 20, 60, 40, 0, 360);
            g.fillArc(70, 20, 60, 40, 0, 270);
            g.drawArc(4, 70, 50, 30, 45, 180);
            g.fillTriangle(70, 80, 100, 20, 130, 80);
            g.setColor(0x008000);
            g.fillTriangle(4, 120, 60, 100, 60, 140);
        }

        private void drawText(Graphics g, int w, int h) {
            Font f = Font.getDefaultFont();
            Font bold = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD,
                    Font.SIZE_MEDIUM);
            Font large = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_ITALIC
                    | Font.STYLE_UNDERLINED, Font.SIZE_LARGE);
            int y = 20;
            g.setColor(0x000000);
            g.setFont(f);
            g.drawString("LEFT/TOP", 2, y, Graphics.LEFT | Graphics.TOP);
            g.drawString("HCENTER/TOP", w / 2, y, Graphics.HCENTER | Graphics.TOP);
            g.drawString("RIGHT/TOP", w - 2, y, Graphics.RIGHT | Graphics.TOP);
            y += f.getHeight() + 6;
            g.setFont(bold);
            g.drawString("bold medium", 2, y, Graphics.LEFT | Graphics.TOP);
            y += bold.getHeight() + 4;
            g.setFont(large);
            g.drawString("italic large", 2, y, Graphics.LEFT | Graphics.TOP);
            y += large.getHeight() + 4;
            g.setFont(f);
            g.drawChar('A', 2, y, Graphics.LEFT | Graphics.TOP);
            g.drawChars(new char[]{'c', 'h', 'a', 'r', 's'}, 0, 5, 20, y,
                    Graphics.LEFT | Graphics.TOP);
            g.drawSubstring("substring", 3, 5, 70, y, Graphics.LEFT | Graphics.TOP);
        }

        private void drawImages(Graphics g, int w, int h) {
            Image im = swatch(24, 16);
            g.drawImage(im, 2, 20, Graphics.LEFT | Graphics.TOP);
            g.drawImage(im, w / 2, 20, Graphics.HCENTER | Graphics.TOP);
            g.drawRegion(im, 0, 0, 24, 16,
                    javax.microedition.lcdui.game.Sprite.TRANS_ROT90, 2, 44,
                    Graphics.LEFT | Graphics.TOP);
            g.drawRegion(im, 0, 0, 24, 16,
                    javax.microedition.lcdui.game.Sprite.TRANS_MIRROR, 40, 44,
                    Graphics.LEFT | Graphics.TOP);
            // Alpha blending: 50% red over white.
            Image alpha = Image.createImage(30, 20);
            Graphics ag = alpha.getGraphics();
            ag.setColor(0xFFFFFF);
            ag.fillRect(0, 0, 30, 20);
            ag.setColor(0x80FF0000);
            ag.fillRect(0, 0, 15, 20);
            g.drawImage(alpha, 2, 80, Graphics.LEFT | Graphics.TOP);
        }

        protected void keyPressed(int keyCode) {
            int a = 0;
            try {
                a = getGameAction(keyCode);
            } catch (Throwable t) {
                // ignore
            }
            if (a == Canvas.DOWN || keyCode == Canvas.KEY_NUM8) {
                page = (page + 1) % 4;
                repaint();
            } else if (a == Canvas.UP || keyCode == Canvas.KEY_NUM2) {
                page = (page + 3) % 4;
                repaint();
            } else if (a == Canvas.LEFT) {
                display.setCurrent(menu);
            }
        }

        public void commandAction(Command c, Displayable d) {
            if (c == back) {
                display.setCurrent(menu);
            } else if (c == next) {
                page = (page + 1) % 4;
                repaint();
            }
        }
    }

    /** A tiny CustomItem so the zoo exercises the full contract. */
    static final class DemoItem extends CustomItem {
        private int counter;

        DemoItem() {
            super("CustomItem");
        }

        protected int getMinContentWidth() {
            return 40;
        }

        protected int getMinContentHeight() {
            return 18;
        }

        protected int getPrefContentWidth(int height) {
            return 120;
        }

        protected int getPrefContentHeight(int width) {
            return 24;
        }

        protected void paint(Graphics g, int w, int h) {
            g.setColor(0xFFFFCC);
            g.fillRect(0, 0, w, h);
            g.setColor(0x000000);
            g.drawRect(0, 0, w - 1, h - 1);
            g.drawString("CustomItem " + counter, 3, h / 2, Graphics.LEFT
                    | Graphics.VCENTER);
        }

        protected void keyPressed(int keyCode) {
            counter++;
            repaint();
        }

        protected void pointerPressed(int x, int y) {
            counter++;
            repaint();
        }
    }
}
