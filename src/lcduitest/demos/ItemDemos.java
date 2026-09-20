/**
 * LcduiTest - MIDP 2.0 LCDUI conformance test MIDlet.
 *
 * Screen (item) based demos: a form with every item type, the three list
 * types, the alert types and a custom item that reports the input events a
 * device actually delivers.
 */
package lcduitest.demos;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.ItemCommandListener;
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
import javax.microedition.lcdui.ItemStateListener;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.Spacer;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;
import javax.microedition.lcdui.Ticker;

import lcduitest.Ui;

public class ItemDemos {

    private ItemDemos() {
    }

    /* ------------------------------------------------------------------ */

    static class AllItems extends Form implements ItemStateListener {

        private final StringItem stateReport = new StringItem("last itemStateChanged", "none");
        private final Gauge gauge = new Gauge("interactive gauge", true, 10, 5);

        AllItems() {
            super("Every item type");
            append(new StringItem("StringItem (PLAIN)", "plain text, no commands"));
            append(new StringItem("StringItem (HYPERLINK)", "hyperlink", Item.HYPERLINK));
            append(new ImageItem("ImageItem TOP|LEFT", Paint.sample(), ImageItem.LAYOUT_LEFT, "alt"));
            append(new ImageItem("ImageItem NEWLINE_BEFORE|AFTER", Paint.sample(),
                    ImageItem.LAYOUT_NEWLINE_BEFORE | ImageItem.LAYOUT_CENTER
                    | ImageItem.LAYOUT_NEWLINE_AFTER, "alt"));
            append(new TextField("TextField ANY", "hello", 40, TextField.ANY));
            append(new TextField("TextField NUMERIC", "12345", 10, TextField.NUMERIC));
            append(new TextField("TextField PASSWORD", "secret", 20,
                    TextField.PASSWORD | TextField.INITIAL_CAPS_WORD));
            append(new TextField("TextField EMAILADDR", "a@b.c", 40, TextField.EMAILADDR));
            append(new DateField("DateField DATE", DateField.DATE));
            append(new DateField("DateField TIME", DateField.TIME));
            append(new DateField("DateField DATE_TIME", DateField.DATE_TIME));
            append(new Gauge("non interactive, definite", false, 20, 7));
            append(new Gauge("non interactive, INDEFINITE", false, Gauge.INDEFINITE,
                    Gauge.INCREMENTAL_UPDATING));
            append(gauge);
            append(new ChoiceGroup("ChoiceGroup EXCLUSIVE", Choice.EXCLUSIVE,
                    new String[]{"one", "two", "three"}, null));
            append(new ChoiceGroup("ChoiceGroup MULTIPLE", Choice.MULTIPLE,
                    new String[]{"red", "green", "blue"}, null));
            append(new ChoiceGroup("ChoiceGroup POPUP", Choice.POPUP,
                    new String[]{"small", "medium", "large"}, null));
            append(new Spacer(20, 20));
            append(stateReport);
            append(new CustomItemDemo.Events("CustomItem: keys / pointer"));
            setItemStateListener(this);
            setTicker(new Ticker("A form with every lcdui Item type"));
        }

        public void itemStateChanged(Item item) {
            stateReport.setText(item.getClass().getName() + " changed"
                    + (item == gauge ? " gauge value=" + gauge.getValue() : ""));
        }
    }

    /* ------------------------------------------------------------------ */

    static class Lists extends Form implements CommandListener {

        private final Command implicit = new Command("IMPLICIT list", Command.SCREEN, 1);
        private final Command exclusive = new Command("EXCLUSIVE list", Command.SCREEN, 2);
        private final Command multiple = new Command("MULTIPLE list", Command.SCREEN, 3);

        Lists() {
            super("List types");
            append("Choose a List type. A List is a Screen, so it uses the whole "
                    + "display and gets its own commands.");
            append("IMPLICIT: the select key fires the select command");
            append("EXCLUSIVE: exactly one element can be selected");
            append("MULTIPLE: any number of elements can be selected");
            addCommand(implicit);
            addCommand(exclusive);
            addCommand(multiple);
            setCommandListener(this);
        }

        public void commandAction(Command c, Displayable d) {
            int type = c == implicit ? Choice.IMPLICIT
                    : (c == exclusive ? Choice.EXCLUSIVE : Choice.MULTIPLE);
            List list = new List("List type=" + type, type);
            Image icon = Paint.sample();
            for (int i = 1; i <= 15; i++) {
                list.append("element " + i, (i % 3 == 0) ? icon : null);
            }
            if (type == Choice.IMPLICIT) {
                list.setSelectCommand(new Command("Open", Command.ITEM, 1));
            }
            list.addCommand(new Command("Back", Command.BACK, 1));
            list.setCommandListener(new ListWatcher(type));
            Display.getDisplay(Ui.midlet()).setCurrent(list);
        }

        /** Shows the selection state of the list that is currently open. */
        private static class ListWatcher implements CommandListener {

            private final int type;

            ListWatcher(int type) {
                this.type = type;
            }

            public void commandAction(Command c, Displayable d) {
                List list = (List) d;
                if (c.getCommandType() == Command.BACK) {
                    Display.getDisplay(Ui.midlet()).setCurrent(new Lists());
                    return;
                }
                boolean[] flags = new boolean[list.size()];
                list.getSelectedFlags(flags);
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < flags.length; i++) {
                    if (flags[i]) {
                        sb.append(i).append(' ');
                    }
                }
                Alert alert = new Alert("selection", "index=" + list.getSelectedIndex()
                        + " flags=[" + sb.toString() + "]", null, AlertType.INFO);
                alert.setTimeout(2000);
                Display.getDisplay(Ui.midlet()).setCurrent(alert);
            }
        }
    }

    /* ------------------------------------------------------------------ */

    static class Alerts extends Form implements CommandListener {

        private final Command info = new Command("INFO", Command.SCREEN, 1);
        private final Command warning = new Command("WARNING", Command.SCREEN, 2);
        private final Command error = new Command("ERROR", Command.SCREEN, 3);
        private final Command alarm = new Command("ALARM", Command.SCREEN, 4);
        private final Command confirmation = new Command("CONFIRMATION", Command.SCREEN, 5);
        private final Command modal = new Command("modal + gauge", Command.SCREEN, 6);

        Alerts() {
            super("Alert types");
            append("Each AlertType plays its own sound and shows its own icon. "
                    + "The modal alert stays until it is dismissed and carries a "
                    + "Gauge as a progress indicator.");
            addCommand(info);
            addCommand(warning);
            addCommand(error);
            addCommand(alarm);
            addCommand(confirmation);
            addCommand(modal);
            setCommandListener(this);
        }

        public void commandAction(Command c, Displayable d) {
            Display display = Display.getDisplay(Ui.midlet());
            if (c == modal) {
                Alert alert = new Alert("Modal alert", "timeout = FOREVER", null, AlertType.INFO);
                alert.setTimeout(Alert.FOREVER);
                Gauge gauge = new Gauge(null, false, Gauge.INDEFINITE, Gauge.CONTINUOUS_RUNNING);
                alert.setIndicator(gauge);
                display.setCurrent(alert, new TextBox("back", "after the modal alert", 64, 0));
                return;
            }
            AlertType type = c == info ? AlertType.INFO
                    : (c == warning ? AlertType.WARNING
                    : (c == error ? AlertType.ERROR
                    : (c == alarm ? AlertType.ALARM : AlertType.CONFIRMATION)));
            Alert alert = new Alert(type.toString() + " alert",
                    "AlertType." + type + " plays a sound and shows an icon.", Paint.sample(), type);
            alert.setTimeout(3000);
            display.setCurrent(alert);
        }
    }

    /* ------------------------------------------------------------------ */

    /**
     * CustomItem that reports what the device delivers: keys, pointer events,
     * traversal and size changes. This is one of the most useful emulator
     * checks because CustomItem receives the raw events.
     */
    static class CustomItemDemo extends Form implements ItemStateListener {

        private final Events item = new Events("custom item");

        CustomItemDemo() {
            super("CustomItem events");
            append("Interact with the yellow box below: the device's key and pointer "
                    + "events are shown inside it.");
            append(item);
            append("ItemStateListener is called for notifyStateChanged().");
            setItemStateListener(this);
        }

        public void itemStateChanged(Item changed) {
            this.item.setFooter("itemStateChanged delivered");
            this.item.refresh();
        }

        static class Events extends CustomItem implements ItemCommandListener {

            private String last = "waiting for input";
            private String footer = "";
            private int keys;
            private int pointers;
            private final Command ping = new Command("Ping", Command.ITEM, 1);

            Events(String label) {
                super(label);
                addCommand(ping);
                setDefaultCommand(ping);
                setItemCommandListener(this);
            }

            void setFooter(String text) {
                footer = text;
            }

            /** repaint() is protected, so the outer demo asks for it through this. */
            void refresh() {
                repaint();
            }

            protected int getMinContentWidth() {
                return 40;
            }

            protected int getMinContentHeight() {
                return 3 * Font.getDefaultFont().getHeight();
            }

            protected int getPrefContentWidth(int height) {
                return 400;
            }

            protected int getPrefContentHeight(int width) {
                return 3 * Font.getDefaultFont().getHeight();
            }

            protected void paint(Graphics g, int w, int h) {
                Font font = Font.getDefaultFont();
                g.setColor(0xFFFF80);
                g.fillRect(0, 0, w, h);
                g.setColor(0x000000);
                g.drawRect(0, 0, w - 1, h - 1);
                g.setFont(font);
                int line = font.getHeight() + 1;
                g.drawString("keys=" + keys + " pointer=" + pointers, 2, 1, Graphics.TOP | Graphics.LEFT);
                g.drawString(cut(last, w / font.charWidth('W') - 1), 2, 1 + line, Graphics.TOP | Graphics.LEFT);
                g.drawString(cut(footer, w / font.charWidth('W') - 1), 2, 1 + 2 * line,
                        Graphics.TOP | Graphics.LEFT);
                g.drawString("modes=" + getInteractionModes(), 2, 1 + 3 * line,
                        Graphics.TOP | Graphics.LEFT);
            }

            private static String cut(String text, int max) {
                if (max < 4 || text.length() <= max) {
                    return text;
                }
                return text.substring(0, max - 1);
            }

            protected void keyPressed(int keyCode) {
                keys++;
                last = "keyPressed " + keyCode + " action=" + getGameAction(keyCode);
                repaint();
            }

            protected void keyReleased(int keyCode) {
                last = "keyReleased " + keyCode;
                repaint();
            }

            protected void keyRepeated(int keyCode) {
                last = "keyRepeated " + keyCode;
                repaint();
            }

            protected void pointerPressed(int x, int y) {
                pointers++;
                last = "pointerPressed " + x + "," + y + " (" + pointers + " total)";
                repaint();
            }

            protected void pointerDragged(int x, int y) {
                last = "pointerDragged " + x + "," + y;
                repaint();
            }

            protected void pointerReleased(int x, int y) {
                last = "pointerReleased " + x + "," + y;
                repaint();
            }

            protected boolean traverse(int dir, int viewportWidth, int viewportHeight, int[] visRect) {
                last = "traverse dir=" + dir;
                repaint();
                return false;
            }

            protected void traverseOut() {
                last = "traverseOut";
                repaint();
            }

            protected void sizeChanged(int w, int h) {
                last = "sizeChanged " + w + "x" + h;
                repaint();
            }

            public void commandAction(Command c, Item item) {
                last = "item command \"" + c.getLabel() + "\"";
                notifyStateChanged();
                repaint();
            }
        }
    }
}
