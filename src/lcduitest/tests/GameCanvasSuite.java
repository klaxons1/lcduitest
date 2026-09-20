/**
 * LcduiTest - MIDP 2.0 LCDUI conformance test MIDlet.
 *
 * javax.microedition.lcdui.game.GameCanvas - the off screen buffer, the
 * flush operations and the latching key state bits.
 */
package lcduitest.tests;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.GameCanvas;

import lcduitest.Assert;
import lcduitest.TestCase;
import lcduitest.TestSuite;
import lcduitest.Ui;

public class GameCanvasSuite extends TestSuite {

    /** Every defined bit of getKeyStates(). */
    static final int KEY_STATES_MASK = GameCanvas.UP_PRESSED | GameCanvas.DOWN_PRESSED
            | GameCanvas.LEFT_PRESSED | GameCanvas.RIGHT_PRESSED | GameCanvas.FIRE_PRESSED
            | GameCanvas.GAME_A_PRESSED | GameCanvas.GAME_B_PRESSED | GameCanvas.GAME_C_PRESSED
            | GameCanvas.GAME_D_PRESSED;

    public GameCanvasSuite() {
        super("GameCanvas", "GameCanvas", "the off screen buffer, flushGraphics(), "
                + "getKeyStates() and the inherited Canvas behaviour");

        add(new TestCase("constructor_and_inherited_behaviour") {
            public void run() {
                Probe quiet = new Probe(true);
                Probe normal = new Probe(false);
                Assert.assertNotNull("GameCanvas(true)", quiet);
                Assert.assertNotNull("GameCanvas(false)", normal);
                Ui.show(normal);
                Assert.assertTrue("a GameCanvas is a Canvas and gets a size",
                        normal.getWidth() > 0 && normal.getHeight() > 0);
                Assert.info("isDoubleBuffered()", normal.isDoubleBuffered());
                Assert.info("hasPointerEvents()", normal.hasPointerEvents());
                Assert.info("hasRepeatEvents()", normal.hasRepeatEvents());
                normal.setTitle("game canvas");
                Assert.assertEquals("GameCanvas inherits setTitle()/getTitle()",
                        "game canvas", normal.getTitle());
            }
        });

        add(new TestCase("paint_is_called_while_the_canvas_is_shown") {
            public void run() {
                Probe canvas = new Probe(false);
                Ui.show(canvas);
                boolean painted = Ui.waitForCount(canvas.paints, 1, 3000);
                Assert.assertTrue("the game canvas must be painted", painted);
                canvas.repaint();
                Assert.assertTrue("repaint() leads to another paint()",
                        Ui.waitForCount(canvas.paints, 2, 3000));
            }
        }.severity(TestCase.SHOULD));

        add(new TestCase("drawing_into_the_buffer_and_flushing_it") {
            public void run() {
                final Probe canvas = new Probe(false);
                Ui.show(canvas);
                boolean ok = Ui.callSeriallyAndWait(new Runnable() {
                    public void run() {
                        Graphics g = canvas.buffer();
                        if (g == null) {
                            throw new IllegalStateException("getGraphics() returned null");
                        }
                        g.setColor(0x00FF00);
                        g.fillRect(0, 0, 16, 16);
                        canvas.flushGraphics();
                        canvas.flushGraphics(0, 0, 8, 8);
                        canvas.flushGraphics(0, 0, -5, -5);
                    }
                }, 3000);
                Assert.assertTrue("drawing and flushing must be possible on the event "
                        + "thread", ok);
            }
        }.severity(TestCase.SHOULD));

        add(new TestCase("getGraphics_returns_a_usable_graphics_object") {
            public void run() {
                Probe canvas = new Probe(false);
                Graphics first = canvas.buffer();
                Graphics second = canvas.buffer();
                Assert.assertNotNull("getGraphics() of an off screen buffer", first);
                Assert.assertNotNull("getGraphics() twice", second);
                Assert.assertNotSame("a new Graphics object is created for every call",
                        first, second);
                Assert.info("the buffer's initial colour", Ui.hex(first.getColor()));
            }
        }.severity(TestCase.SHOULD));

        add(new TestCase("key_states") {
            public void run() {
                Probe canvas = new Probe(false);
                Ui.show(canvas);
                int states = canvas.states();
                Assert.info("getKeyStates() with no key pressed",
                        "0x" + Integer.toHexString(states));
                Assert.assertEquals("only the defined key bits may be reported, was 0x"
                        + Integer.toHexString(states), 0, states & ~KEY_STATES_MASK);
                int again = canvas.states();
                Assert.assertEquals("the key state is latched and cleared by the read, "
                        + "was 0x" + Integer.toHexString(again & states), 0,
                        again & states & KEY_STATES_MASK);
            }
        }.severity(TestCase.SHOULD));

        add(new TestCase("key_events_reach_a_game_canvas") {
            public void run() {
                Probe canvas = new Probe(false);
                Ui.show(canvas);
                canvas.press(javax.microedition.lcdui.Canvas.KEY_NUM5);
                Assert.assertEquals("keyPressed is inherited from Canvas",
                        1, canvas.keyPressedCalls);
                canvas.release();
                Assert.assertEquals("keyReleased is inherited from Canvas",
                        1, canvas.keyReleasedCalls);
            }
        }.severity(TestCase.INFO).describe("programmatic key callbacks; whether the device "
                + "itself delivers key events to a GameCanvas is a device property"));

        add(new TestCase("a_game_canvas_can_use_the_layer_api") {
            public void run() {
                Probe canvas = new Probe(false);
                Ui.show(canvas);
                Image image = Ui.solidImage(8, 8, 0xFFFF00);
                javax.microedition.lcdui.game.Sprite sprite =
                        new javax.microedition.lcdui.game.Sprite(image);
                javax.microedition.lcdui.game.LayerManager manager =
                        new javax.microedition.lcdui.game.LayerManager();
                manager.append(sprite);
                manager.setViewWindow(0, 0, 20, 20);
                Assert.assertEquals("the manager holds the sprite", 1, manager.getSize());
                Assert.assertTrue("the canvas is shown", canvas.isShown());
            }
        }.severity(TestCase.SHOULD));
    }

    /** A GameCanvas that counts its paints and exposes the protected buffer. */
    static class Probe extends GameCanvas {

        final int[] paints = new int[1];
        int keyPressedCalls;
        int keyReleasedCalls;
        boolean quiet;

        Probe(boolean suppressKeyEvents) {
            super(suppressKeyEvents);
            quiet = suppressKeyEvents;
        }

        public void paint(Graphics g) {
            paints[0]++;
            g.setColor(0x000080);
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        Graphics buffer() {
            return getGraphics();
        }

        int states() {
            return getKeyStates();
        }

        void press(int keyCode) {
            keyPressed(keyCode);
        }

        void release() {
            keyReleased(0);
        }

        protected void keyPressed(int keyCode) {
            keyPressedCalls++;
        }

        protected void keyReleased(int keyCode) {
            keyReleasedCalls++;
        }
    }
}
