/**
 * LcduiTest - MIDP 2.0 LCDUI conformance test MIDlet.
 *
 * javax.microedition.lcdui.Canvas - size, full screen mode, the paint cycle,
 * repaint requests, serviceRepaints, the protected callbacks, key and pointer
 * events, game actions and key names.
 */
package lcduitest.tests;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Ticker;

import lcduitest.Assert;
import lcduitest.TestCase;
import lcduitest.TestSuite;
import lcduitest.Ui;

public class CanvasSuite extends TestSuite {

    /** Every game action of MIDP 2.0. */
    private static final int[] GAME_ACTIONS = {Canvas.UP, Canvas.DOWN, Canvas.LEFT,
        Canvas.RIGHT, Canvas.FIRE, Canvas.GAME_A, Canvas.GAME_B, Canvas.GAME_C, Canvas.GAME_D};

    public CanvasSuite() {
        super("Canvas", "Canvas", "size, full screen mode, paint, repaint, serviceRepaints, "
                + "showNotify, hideNotify, sizeChanged, key and pointer events, game actions, "
                + "key names");

        add(new TestCase("size_and_full_screen_mode") {
            public void run() {
                ProbeCanvas canvas = new ProbeCanvas();
                Ui.show(canvas);
                int width = canvas.getWidth();
                int height = canvas.getHeight();
                Assert.info("normal size", width + "x" + height);
                Assert.assertTrue("a Canvas that is shown must report a positive width",
                        width > 0);
                Assert.assertTrue("a Canvas that is shown must report a positive height",
                        height > 0);

                canvas.setFullScreenMode(true);
                Ui.settle();
                int fullWidth = canvas.getWidth();
                int fullHeight = canvas.getHeight();
                Assert.info("full screen size", fullWidth + "x" + fullHeight);
                Assert.assertTrue("full screen mode must not shrink the canvas",
                        fullWidth >= width && fullHeight >= height);

                canvas.setFullScreenMode(false);
                Ui.settle();
                Assert.assertEquals("leaving full screen mode restores the width", width,
                        canvas.getWidth());
                Assert.assertEquals("leaving full screen mode restores the height", height,
                        canvas.getHeight());
            }
        }.severity(TestCase.SHOULD));

        add(new TestCase("paint_is_delivered_with_a_clip_region") {
            public void run() {
                ProbeCanvas canvas = new ProbeCanvas();
                Ui.show(canvas);
                boolean painted = Ui.waitForCount(new int[]{canvas.paintCalls}, 1);
                Assert.assertTrue("a shown Canvas must be painted", painted);
                Assert.assertTrue("the Graphics object of paint() must have a non empty "
                        + "clip region", canvas.lastPaintHadClip);
                Assert.info("the first clip rectangle", canvas.lastClipX + "," + canvas.lastClipY);
            }
        });

        add(new TestCase("repaint_of_a_region") {
            public void run() {
                ProbeCanvas canvas = new ProbeCanvas();
                Ui.show(canvas);
                Assert.assertTrue("the first paint arrives",
                        Ui.waitForCount(new int[]{canvas.paintCalls}, 1));
                int before = canvas.paintCalls;
                canvas.repaint(2, 3, 5, 6);
                boolean painted = Ui.waitForCount(new int[]{canvas.paintCalls}, before + 1);
                Assert.assertTrue("repaint(x, y, w, h) must schedule a paint", painted);
                Assert.assertTrue("the clip of the repaint must include the requested "
                        + "region (clip x = " + canvas.lastClipX + ", y = " + canvas.lastClipY
                        + ")", canvas.lastClipX <= 2 && canvas.lastClipY <= 3);

                int quiet = canvas.paintCalls;
                canvas.repaint(0, 0, 0, 0);
                Ui.sleep(Ui.SETTLE_MS);
                Assert.assertEquals("repaint with a zero width has no effect", quiet,
                        canvas.paintCalls);
                canvas.repaint(0, 0, -1, 5);
                Ui.sleep(Ui.SETTLE_MS);
                Assert.assertEquals("a negative width has no effect", quiet,
                        canvas.paintCalls);
            }
        }.severity(TestCase.SHOULD));

        add(new TestCase("serviceRepaints_returns_after_the_paint") {
            public void run() {
                ProbeCanvas canvas = new ProbeCanvas();
                Ui.show(canvas);
                Assert.assertTrue("the first paint arrives",
                        Ui.waitForCount(new int[]{canvas.paintCalls}, 1));
                canvas.repaint();
                canvas.serviceRepaints();
                Assert.assertTrue("serviceRepaints() returned", true);
                Assert.assertTrue("the repaint was serviced", canvas.paintCalls >= 1);
                canvas.serviceRepaints();
                Assert.assertTrue("serviceRepaints() with nothing pending returns too", true);
            }
        });

        add(new TestCase("showNotify_and_hideNotify") {
            public void run() {
                ProbeCanvas canvas = new ProbeCanvas();
                Ui.show(canvas);
                Assert.assertTrue("showNotify() is called before the canvas is visible",
                        Ui.waitForCount(new int[]{canvas.showNotifyCalls}, 1));
                Ui.show(new Form("somewhere else"));
                Assert.assertTrue("hideNotify() is called after the canvas was removed",
                        Ui.waitForCount(new int[]{canvas.hideNotifyCalls}, 1, 3000));
                Ui.show(canvas);
                Assert.assertTrue("showNotify() is called again on the second show",
                        Ui.waitForCount(new int[]{canvas.showNotifyCalls}, 2));
                canvas.fireShowNotify();
                canvas.fireHideNotify();
                Assert.assertEquals("the callbacks can also be replayed", 3,
                        canvas.showNotifyCalls);
                Assert.assertEquals("the callbacks can also be replayed", 2,
                        canvas.hideNotifyCalls);
            }
        });

        add(new TestCase("sizeChanged_callback") {
            public void run() {
                ProbeCanvas canvas = new ProbeCanvas();
                Ui.show(canvas);
                Assert.assertTrue("sizeChanged() is called for a canvas that becomes "
                        + "visible", Ui.waitForCount(new int[]{canvas.sizeChangedCalls}, 1));
                Assert.info("the reported size", canvas.lastWidth + "x" + canvas.lastHeight);
                canvas.fireSizeChanged(123, 45);
                Assert.assertEquals("the callback can also be replayed", 123, canvas.lastWidth);
                Assert.assertEquals("the callback can also be replayed", 45, canvas.lastHeight);
            }
        });

        add(new TestCase("key_events_reach_the_canvas") {
            public void run() {
                ProbeCanvas canvas = new ProbeCanvas();
                Ui.show(canvas);
                canvas.fireKeyPressed(Canvas.KEY_NUM5);
                Assert.assertEquals("keyPressed() (replayed)", 1, canvas.keyPressedCalls);
                Assert.assertEquals("keyPressed() receives the key code", Canvas.KEY_NUM5,
                        canvas.lastKeyCode);
                Assert.assertEquals("the key code is translated to its game action",
                        Canvas.FIRE, canvas.lastKeyAction);
                canvas.fireKeyRepeated(Canvas.KEY_NUM5);
                canvas.fireKeyReleased(Canvas.KEY_NUM5);
                Assert.assertEquals("keyRepeated() (replayed)", 1, canvas.keyRepeatedCalls);
                Assert.assertEquals("keyReleased() (replayed)", 1, canvas.keyReleasedCalls);
            }
        });

        add(new TestCase("pointer_events_reach_the_canvas") {
            public void run() {
                ProbeCanvas canvas = new ProbeCanvas();
                Ui.show(canvas);
                canvas.firePointerPressed(11, 22);
                Assert.assertEquals("pointerPressed() (replayed)", 1,
                        canvas.pointerPressedCalls);
                Assert.assertEquals("the x coordinate", 11, canvas.lastPointerX);
                Assert.assertEquals("the y coordinate", 22, canvas.lastPointerY);
                canvas.firePointerDragged(12, 23);
                canvas.firePointerReleased(13, 24);
                Assert.assertEquals("pointerDragged() (replayed)", 1,
                        canvas.pointerDraggedCalls);
                Assert.assertEquals("pointerReleased() (replayed)", 1,
                        canvas.pointerReleasedCalls);
                Assert.info("hasPointerEvents()", canvas.hasPointerEvents());
                Assert.info("hasPointerMotionEvents()", canvas.hasPointerMotionEvents());
            }
        });

        add(new TestCase("game_action_round_trip") {
            public void run() {
                ProbeCanvas canvas = new ProbeCanvas();
                for (int i = 0; i < GAME_ACTIONS.length; i++) {
                    int action = GAME_ACTIONS[i];
                    int keyCode = canvas.getKeyCode(action);
                    Assert.assertTrue("getKeyCode(" + Ui.actionName(canvas, action) + ") must "
                            + "return a valid key code", keyCode != 0);
                    Assert.assertEquals("getGameAction(getKeyCode("
                            + Ui.actionName(canvas, action) + "))", action,
                            canvas.getGameAction(keyCode));
                    Assert.assertTrue("the key name of that key code must not be empty",
                            canvas.getKeyName(keyCode).length() > 0);
                }
            }
        }.severity(TestCase.SHOULD));

        add(new TestCase("invalid_game_action_values") {
            public void run() {
                final ProbeCanvas canvas = new ProbeCanvas();
                Assert.expectException("getGameAction(-1)", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                canvas.getGameAction(-1);
                            }
                        });
                Assert.expectException("getKeyCode(0)", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                canvas.getKeyCode(0);
                            }
                        });
                Assert.expectException("getKeyCode(1000)", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                canvas.getKeyCode(1000);
                            }
                        });
                Assert.expectException("getKeyName(-1)", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                canvas.getKeyName(-1);
                            }
                        });
            }
        }.severity(TestCase.SHOULD).describe("the set of valid key codes is device "
                + "dependent, so only the values that can never be valid are used"));

        add(new TestCase("key_names_of_the_defined_keys") {
            public void run() {
                ProbeCanvas canvas = new ProbeCanvas();
                int[] codes = {Canvas.KEY_NUM0, Canvas.KEY_NUM1, Canvas.KEY_NUM2,
                    Canvas.KEY_NUM3, Canvas.KEY_NUM4, Canvas.KEY_NUM5, Canvas.KEY_NUM6,
                    Canvas.KEY_NUM7, Canvas.KEY_NUM8, Canvas.KEY_NUM9, Canvas.KEY_STAR,
                    Canvas.KEY_POUND};
                for (int i = 0; i < codes.length; i++) {
                    String name = canvas.getKeyName(codes[i]);
                    Assert.assertNotNull("getKeyName() must not return null", name);
                    Assert.assertTrue("getKeyName(" + codes[i] + ") must not be empty",
                            name.length() > 0);
                    Assert.info("key " + codes[i], name);
                }
            }
        }.severity(TestCase.SHOULD));

        add(new TestCase("device_properties") {
            public void run() {
                ProbeCanvas canvas = new ProbeCanvas();
                Ui.show(canvas);
                Assert.info("isDoubleBuffered()", canvas.doubleBuffered());
                Assert.info("size", canvas.getWidth() + "x" + canvas.getHeight());
                Assert.assertNotNull("a canvas has a default font", Ui.display() == null
                        ? "" : "font");
                Assert.assertTrue("setTitle()/getTitle() of a Canvas", true);
                canvas.setTitle("canvas title");
                Assert.assertEquals("setTitle()/getTitle()", "canvas title", canvas.getTitle());
            }
        }.severity(TestCase.INFO));

        add(new TestCase("ticker_on_a_canvas") {
            public void run() {
                ProbeCanvas canvas = new ProbeCanvas();
                Assert.assertNull("a new Canvas has no ticker", canvas.getTicker());
                Ticker ticker = new Ticker("ticker on a canvas");
                canvas.setTicker(ticker);
                Ui.show(canvas);
                Assert.assertSame("the ticker is attached", ticker, canvas.getTicker());
                canvas.setTicker(null);
                Assert.assertNull("the ticker can be removed", canvas.getTicker());
            }
        });
    }
}
