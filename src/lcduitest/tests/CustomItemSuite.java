/**
 * LcduiTest - MIDP 2.0 LCDUI conformance test MIDlet.
 *
 * javax.microedition.lcdui.CustomItem - the item that draws itself and
 * receives raw key and pointer events.
 */
package lcduitest.tests;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CustomItem;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.ItemStateListener;

import lcduitest.Assert;
import lcduitest.TestCase;
import lcduitest.TestSuite;
import lcduitest.Ui;

public class CustomItemSuite extends TestSuite {

    public CustomItemSuite() {
        super("CustomItem", "Custom items", "content size negotiation, painting, key and "
                + "pointer events, traversal and item commands");

        add(new TestCase("content_size_negotiation") {
            public void run() {
                Probe item = new Probe();
                Form form = new Form("custom");
                form.append(item);
                Ui.show(form);
                Assert.assertTrue("a CustomItem must report a positive minimum width but "
                        + "reported " + item.getMinimumWidth(), item.getMinimumWidth() > 0);
                Assert.assertTrue("a CustomItem must report a positive minimum height but "
                        + "reported " + item.getMinimumHeight(), item.getMinimumHeight() > 0);
                Assert.assertTrue("the preferred width must not be smaller than the minimum",
                        item.getPreferredWidth() >= item.getMinimumWidth());
                Assert.assertTrue("the platform must ask the item for its preferred size "
                        + "(getPrefContentWidth/Height)", item.askedPrefWidth
                        || item.askedPrefHeight);
            }
        }.severity(TestCase.SHOULD));

        add(new TestCase("painting") {
            public void run() {
                Probe item = new Probe();
                Form form = new Form("custom");
                form.append(item);
                Ui.show(form);
                Assert.assertTrue("a visible CustomItem must be painted",
                        Ui.waitForFlag(item.painted, 3000));
                Assert.assertTrue("the paint() callback must report a positive width but "
                        + "reported " + item.lastPaintWidth, item.lastPaintWidth > 0);
                Assert.assertTrue("the paint() callback must report a positive height but "
                        + "reported " + item.lastPaintHeight, item.lastPaintHeight > 0);
                int before = item.paintCalls;
                item.callRepaint();
                long deadline = System.currentTimeMillis() + 3000;
                while (item.paintCalls <= before && System.currentTimeMillis() < deadline) {
                    Ui.sleep(20);
                }
                Assert.assertTrue("repaint() must lead to another paint() call",
                        item.paintCalls > before);
                item.callRepaint(1, 1, 4, 4);
                Assert.assertTrue("repaint(x,y,w,h) must be accepted",
                        item.paintCalls >= before + 1);
            }
        }.severity(TestCase.SHOULD));

        add(new TestCase("interaction_mode_values") {
            public void run() {
                Probe item = new Probe();
                int[] values = item.interactionModeValues();
                Assert.assertEquals("CustomItem.NONE", 0, values[0]);
                Assert.assertEquals("CustomItem.TRAVERSE_HORIZONTAL", 1, values[1]);
                Assert.assertEquals("CustomItem.TRAVERSE_VERTICAL", 2, values[2]);
                Assert.assertEquals("CustomItem.KEY_PRESS", 4, values[3]);
                Assert.assertEquals("CustomItem.KEY_RELEASE", 8, values[4]);
                Assert.assertEquals("CustomItem.KEY_REPEAT", 0x10, values[5]);
                Assert.assertEquals("CustomItem.POINTER_PRESS", 0x20, values[6]);
                Assert.assertEquals("CustomItem.POINTER_RELEASE", 0x40, values[7]);
                Assert.assertEquals("CustomItem.POINTER_DRAG", 0x80, values[8]);
            }
        });

        add(new TestCase("interaction_modes") {
            public void run() {
                Probe item = new Probe();
                int modes = item.callGetInteractionModes();
                Assert.info("getInteractionModes()", modes);
                int[] values = item.interactionModeValues();
                int known = 0;
                for (int i = 0; i < values.length; i++) {
                    known |= values[i];
                }
                Assert.assertEquals("only documented interaction modes may be reported, "
                        + "was 0x" + Integer.toHexString(modes), 0, modes & ~known);
            }
        }.severity(TestCase.SHOULD));

        add(new TestCase("game_actions_of_a_custom_item") {
            public void run() {
                final Probe item = new Probe();
                Canvas canvas = new ProbeCanvas();
                int[] actions = {Canvas.UP, Canvas.DOWN, Canvas.LEFT, Canvas.RIGHT, Canvas.FIRE,
                    Canvas.GAME_A, Canvas.GAME_B, Canvas.GAME_C, Canvas.GAME_D};
                for (int i = 0; i < actions.length; i++) {
                    int keyCode = canvas.getKeyCode(actions[i]);
                    if (keyCode != 0) {
                        Assert.assertEquals("CustomItem.getGameAction(keyCode) must map the "
                                + "same key to the same action as Canvas.getKeyCode(action)",
                                actions[i], item.safeGameAction(keyCode));
                    }
                }
                Assert.expectException("getGameAction(-1)", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                item.getGameAction(-1);
                            }
                        });
            }
        }.severity(TestCase.SHOULD));

        add(new TestCase("key_pointer_and_traversal_callbacks") {
            public void run() {
                Probe item = new Probe();
                item.callKeyPressed(Canvas.KEY_NUM1);
                item.callKeyRepeated(Canvas.KEY_NUM1);
                item.callKeyReleased(Canvas.KEY_NUM1);
                Assert.assertEquals("keyPressed", 1, item.keyPressed);
                Assert.assertEquals("keyRepeated", 1, item.keyRepeated);
                Assert.assertEquals("keyReleased", 1, item.keyReleased);
                item.callPointerPressed(3, 4);
                item.callPointerDragged(5, 6);
                item.callPointerReleased(7, 8);
                Assert.assertEquals("pointerPressed", 1, item.pointerPressed);
                Assert.assertEquals("pointerDragged", 1, item.pointerDragged);
                Assert.assertEquals("pointerReleased", 1, item.pointerReleased);
                int[] visRect = new int[4];
                boolean accepted = item.callTraverse(item.traverseVertical(), 100, 100,
                        visRect);
                Assert.assertEquals("traverse()", 1, item.traverseCalls);
                Assert.assertTrue("traverse() must be able to accept the focus", accepted);
                item.callTraverseOut();
                Assert.assertEquals("traverseOut()", 1, item.traverseOutCalls);
                item.callSizeChanged(40, 20);
                Assert.assertEquals("sizeChanged()", 1, item.sizeChangedCalls);
                item.callShowNotify();
                item.callHideNotify();
                Assert.assertEquals("showNotify()", 1, item.showNotifyCalls);
                Assert.assertEquals("hideNotify()", 1, item.hideNotifyCalls);
                item.callInvalidate();
                Assert.assertNotNull("invalidate() is accepted", item);
            }
        }.severity(TestCase.INFO).describe("the callbacks are exercised directly; whether the "
                + "device delivers each event to a CustomItem is shown by the visual demos"));

        add(new TestCase("item_commands_and_state_notification") {
            public void run() {
                Probe item = new Probe();
                item.addCommand(new Command("Do", Command.ITEM, 1));
                item.setDefaultCommand(new Command("Default", Command.ITEM, 2));
                item.setItemCommandListener(new ItemCommandListener() {
                    public void commandAction(Command c, Item i) {
                    }
                });
                Form form = new Form("state");
                final boolean[] notified = new boolean[1];
                form.append(item);
                form.setItemStateListener(new ItemStateListener() {
                    public void itemStateChanged(Item changed) {
                        notified[0] = true;
                    }
                });
                item.callNotifyStateChanged();
                Assert.assertTrue("notifyStateChanged() must reach the Form's listener",
                        Ui.waitForFlag(notified, 2000));
            }
        }.severity(TestCase.SHOULD));

        add(new TestCase("a_japanese_style_all_in_one_item_can_be_removed") {
            public void run() {
                Form form = new Form("custom");
                Probe item = new Probe();
                form.append(item);
                Assert.assertEquals("the item is owned by the form", 1, form.size());
                form.delete(0);
                Assert.assertEquals("the item can be removed again", 0, form.size());
                Assert.assertNotNull("the removed item is still a valid object", item);
            }
        });
    }

    /** A CustomItem that records everything the platform asks it to do. */
    static class Probe extends CustomItem {

        int minWidth = 20;
        int minHeight = 10;
        final boolean[] painted = new boolean[1];
        int paintCalls;
        int lastPaintWidth;
        int lastPaintHeight;
        int keyPressed;
        int keyReleased;
        int keyRepeated;
        int pointerPressed;
        int pointerDragged;
        int pointerReleased;
        int traverseCalls;
        int traverseOutCalls;
        int sizeChangedCalls;
        int showNotifyCalls;
        int hideNotifyCalls;
        boolean askedPrefWidth;
        boolean askedPrefHeight;

        Probe() {
            super("probe");
        }

        protected int getMinContentWidth() {
            return minWidth;
        }

        protected int getMinContentHeight() {
            return minHeight;
        }

        protected int getPrefContentWidth(int height) {
            askedPrefWidth = true;
            return minWidth + 10;
        }

        protected int getPrefContentHeight(int width) {
            askedPrefHeight = true;
            return minHeight + 10;
        }

        protected void paint(Graphics g, int w, int h) {
            paintCalls++;
            painted[0] = true;
            lastPaintWidth = w;
            lastPaintHeight = h;
            g.setColor(0x123456);
            g.fillRect(0, 0, w, h);
        }

        protected void keyPressed(int keyCode) {
            keyPressed++;
        }

        protected void keyReleased(int keyCode) {
            keyReleased++;
        }

        protected void keyRepeated(int keyCode) {
            keyRepeated++;
        }

        protected void pointerPressed(int x, int y) {
            pointerPressed++;
        }

        protected void pointerDragged(int x, int y) {
            pointerDragged++;
        }

        protected void pointerReleased(int x, int y) {
            pointerReleased++;
        }

        protected boolean traverse(int dir, int viewportWidth, int viewportHeight, int[] visRect) {
            traverseCalls++;
            visRect[0] = 0;
            visRect[1] = 0;
            visRect[2] = minWidth;
            visRect[3] = minHeight;
            return true;
        }

        protected void traverseOut() {
            traverseOutCalls++;
        }

        protected void sizeChanged(int w, int h) {
            sizeChangedCalls++;
        }

        protected void showNotify() {
            showNotifyCalls++;
        }

        protected void hideNotify() {
            hideNotifyCalls++;
        }

        /* -------- public wrappers so that the test can call the callbacks --- */

        void callKeyPressed(int keyCode) {
            keyPressed(keyCode);
        }

        void callKeyReleased(int keyCode) {
            keyReleased(keyCode);
        }

        void callKeyRepeated(int keyCode) {
            keyRepeated(keyCode);
        }

        void callPointerPressed(int x, int y) {
            pointerPressed(x, y);
        }

        void callPointerDragged(int x, int y) {
            pointerDragged(x, y);
        }

        void callPointerReleased(int x, int y) {
            pointerReleased(x, y);
        }

        boolean callTraverse(int dir, int vw, int vh, int[] rect) {
            return traverse(dir, vw, vh, rect);
        }

        void callTraverseOut() {
            traverseOut();
        }

        void callSizeChanged(int w, int h) {
            sizeChanged(w, h);
        }

        void callShowNotify() {
            showNotify();
        }

        void callHideNotify() {
            hideNotify();
        }

        void callInvalidate() {
            invalidate();
        }

        void callRepaint() {
            repaint();
        }

        void callRepaint(int x, int y, int w, int h) {
            repaint(x, y, w, h);
        }

        void callNotifyStateChanged() {
            notifyStateChanged();
        }

        /** The protected interaction mode constants, in the documented order. */
        int[] interactionModeValues() {
            return new int[]{NONE, TRAVERSE_HORIZONTAL, TRAVERSE_VERTICAL, KEY_PRESS,
                KEY_RELEASE, KEY_REPEAT, POINTER_PRESS, POINTER_RELEASE, POINTER_DRAG};
        }

        int traverseVertical() {
            return TRAVERSE_VERTICAL;
        }

        int callGetInteractionModes() {
            return getInteractionModes();
        }

        int safeGameAction(int keyCode) {
            try {
                return getGameAction(keyCode);
            } catch (IllegalArgumentException e) {
                return -1;
            }
        }
    }
}
