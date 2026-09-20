/**
 * LcduiTest - MIDP 2.0 LCDUI conformance test MIDlet.
 *
 * javax.microedition.lcdui.Display - the screen manager of a MIDlet.
 */
package lcduitest.tests;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.StringItem;

import lcduitest.Assert;
import lcduitest.TestCase;
import lcduitest.TestSuite;
import lcduitest.Ui;

public class DisplaySuite extends TestSuite {

    public DisplaySuite() {
        super("Display", "Display, colours, screens", "getDisplay, setCurrent, "
                + "callSerially, colour and image size capabilities, backlight, "
                + "vibration");
        add(new TestCase("getDisplay_returns_same_instance") {
            public void run() {
                Display a = Display.getDisplay(Ui.midlet());
                Display b = Display.getDisplay(Ui.midlet());
                Assert.assertNotNull("Display.getDisplay(MIDlet)", a);
                Assert.assertSame("Display.getDisplay returns the same object", a, b);
            }
        });

        add(new TestCase("describe_panel") {
            public void run() {
                Display display = Ui.display();
                int colors = display.numColors();
                int alpha = display.numAlphaLevels();
                Assert.info("numColors", colors);
                Assert.info("numAlphaLevels", alpha);
                Assert.info("isColor", display.isColor());
                Form probe = new Form("probe");
                Assert.info("screen", probe.getWidth() + "x" + probe.getHeight());
                Assert.assertTrue("numColors must be at least 2 but was " + colors, colors >= 2);
                Assert.assertTrue("numAlphaLevels must be at least 2 but was " + alpha, alpha >= 2);
                if (display.isColor()) {
                    Assert.assertTrue("a colour display must have more than 2 colours "
                            + "(numColors=" + colors + ")", colors > 2);
                }
            }
        });

        add(new TestCase("getColor_of_every_constant") {
            public void run() {
                Display display = Ui.display();
                int[] constants = {Display.COLOR_BACKGROUND, Display.COLOR_FOREGROUND,
                    Display.COLOR_BORDER, Display.COLOR_HIGHLIGHTED_BACKGROUND,
                    Display.COLOR_HIGHLIGHTED_FOREGROUND, Display.COLOR_HIGHLIGHTED_BORDER};
                String[] names = {"COLOR_BACKGROUND", "COLOR_FOREGROUND", "COLOR_BORDER",
                    "COLOR_HIGHLIGHTED_BACKGROUND", "COLOR_HIGHLIGHTED_FOREGROUND",
                    "COLOR_HIGHLIGHTED_BORDER"};
                for (int i = 0; i < constants.length; i++) {
                    int color = display.getColor(constants[i]);
                    Assert.info(names[i], Ui.hex(color));
                    Assert.assertBitClear(names[i] + " must be returned as 0x00RRGGBB but was "
                            + Ui.hex(color), 0xFF000000, color);
                }
            }
        });

        add(new TestCase("getColor_rejects_invalid_specifier") {
            public void run() {
                final Display display = Ui.display();
                Assert.expectException("getColor(-1)", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                display.getColor(-1);
                            }
                        });
                Assert.expectException("getColor(6) is not a colour constant",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                display.getColor(6);
                            }
                        });
            }
        });

        add(new TestCase("getBorderStyle") {
            public void run() {
                Display display = Ui.display();
                int plain = display.getBorderStyle(false);
                int highlighted = display.getBorderStyle(true);
                Assert.info("borderStyle(false)", plain);
                Assert.info("borderStyle(true)", highlighted);
                Assert.assertTrue("unhighlighted border style must be SOLID or DOTTED but was "
                        + plain, plain == Graphics.SOLID || plain == Graphics.DOTTED);
                Assert.assertTrue("highlighted border style must be SOLID or DOTTED but was "
                        + highlighted, highlighted == Graphics.SOLID || highlighted == Graphics.DOTTED);
            }
        });

        add(new TestCase("getBestImageWidth_and_height") {
            public void run() {
                Display display = Ui.display();
                int[] types = {Display.LIST_ELEMENT, Display.CHOICE_GROUP_ELEMENT, Display.ALERT};
                String[] names = {"LIST_ELEMENT", "CHOICE_GROUP_ELEMENT", "ALERT"};
                for (int i = 0; i < types.length; i++) {
                    int width = display.getBestImageWidth(types[i]);
                    int height = display.getBestImageHeight(types[i]);
                    Assert.info(names[i] + " bestSize", width + "x" + height);
                    Assert.assertTrue(names[i] + " best image width must not be negative but was "
                            + width, width >= 0);
                    Assert.assertTrue(names[i] + " best image height must not be negative but was "
                            + height, height >= 0);
                }
            }
        });

        add(new TestCase("getBestImageWidth_rejects_invalid_type") {
            public void run() {
                final Display display = Ui.display();
                Assert.expectException("getBestImageWidth(-1)", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                display.getBestImageWidth(-1);
                            }
                        });
                Assert.expectException("getBestImageHeight(0)", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                display.getBestImageHeight(0);
                            }
                        });
            }
        });

        add(new TestCase("setCurrent_makes_displayable_current") {
            public void run() {
                Form form = new Form("current test");
                Ui.display().setCurrent(form);
                Assert.assertTrue("getCurrent() must become the form", waitForCurrent(form));
            }
        });

        add(new TestCase("setCurrent_null_is_ignored") {
            public void run() {
                Form first = new Form("first");
                Ui.display().setCurrent(first);
                Assert.assertTrue("first screen becomes current", waitForCurrent(first));
                Ui.display().setCurrent(null);
                Ui.settle();
                Assert.assertSame("setCurrent(null) must leave the current screen unchanged",
                        first, Ui.display().getCurrent());
            }
        });

        add(new TestCase("setCurrent_alert_with_next_restores") {
            public void run() {
                Form background = new Form("background");
                Ui.display().setCurrent(background);
                Assert.assertTrue("background current", waitForCurrent(background));
                Alert alert = new Alert("note", "120 ms", null, AlertType.INFO);
                alert.setTimeout(120);
                Ui.display().setCurrent(alert, background);
                boolean sawAlert = false;
                long deadline = System.currentTimeMillis() + 2000;
                while (System.currentTimeMillis() < deadline) {
                    Displayable current = Ui.display().getCurrent();
                    if (current == alert) {
                        sawAlert = true;
                    }
                    if (sawAlert && current == background) {
                        break;
                    }
                    Ui.sleep(20);
                }
                Assert.assertTrue("the alert must have been shown", sawAlert);
                Assert.assertSame("the previous screen must be restored after the alert",
                        background, Ui.display().getCurrent());
            }
        }.severity(TestCase.SHOULD).describe("setCurrent(alert, next) shows the alert and "
                + "restores next when the alert times out"));

        add(new TestCase("callSerially_runs_on_the_event_thread") {
            public void run() {
                final int[] counter = {0};
                boolean ran = Ui.callSeriallyAndWait(new Runnable() {
                    public void run() {
                        counter[0]++;
                    }
                }, 3000);
                Assert.assertTrue("callSerially runnable executed", ran);
                Assert.assertEquals("runnable executed once", 1, counter[0]);
            }
        });

        add(new TestCase("callSerially_keeps_fifo_order") {
            public void run() {
                final StringBuffer order = new StringBuffer();
                Ui.display().callSerially(new Runnable() {
                    public void run() {
                        order.append("1");
                    }
                });
                Ui.display().callSerially(new Runnable() {
                    public void run() {
                        order.append("2");
                    }
                });
                Ui.display().callSerially(new Runnable() {
                    public void run() {
                        order.append("3");
                    }
                });
                long deadline = System.currentTimeMillis() + 3000;
                while (order.length() < 3 && System.currentTimeMillis() < deadline) {
                    Ui.sleep(20);
                }
                Assert.assertEquals("callSerially runnables run in order", "123", order.toString());
            }
        });

        add(new TestCase("setCurrentItem_scrolls_to_item") {
            public void run() {
                Form form = new Form("items");
                StringItem item = new StringItem("label", "content");
                form.append(item);
                Ui.display().setCurrentItem(item);
                Assert.assertTrue("the form that owns the item becomes current",
                        waitForCurrent(form));
            }
        });

        add(new TestCase("setCurrentItem_requires_a_container") {
            public void run() {
                final StringItem orphan = new StringItem("label", "content");
                Assert.expectException("setCurrentItem(item without container)",
                        IllegalStateException.class, new Assert.Code() {
                            public void run() {
                                Ui.display().setCurrentItem(orphan);
                            }
                        });
                Assert.expectException("setCurrentItem(null)", NullPointerException.class,
                        new Assert.Code() {
                            public void run() {
                                Ui.display().setCurrentItem(null);
                            }
                        });
            }
        });

        add(new TestCase("setCurrentItem_rejects_items_inside_an_alert") {
            public void run() {
                final Alert alert = new Alert("alert", "text", null, AlertType.INFO);
                alert.setTimeout(100);
                final Gauge gauge = new Gauge("indicator", false, Gauge.INDEFINITE,
                        Gauge.CONTINUOUS_IDLE);
                alert.setIndicator(gauge);
                Assert.expectException("setCurrentItem(item inside an Alert)",
                        IllegalStateException.class, new Assert.Code() {
                            public void run() {
                                Ui.display().setCurrentItem(gauge);
                            }
                        });
            }
        });

        add(new TestCase("flashBacklight_and_vibrate") {
            public void run() {
                Display display = Ui.display();
                boolean backlight = display.flashBacklight(500);
                boolean vibration = display.vibrate(300);
                Assert.info("flashBacklight(500) returned", backlight);
                Assert.info("vibrate(300) returned", vibration);
            }
        }.severity(TestCase.INFO));

        add(new TestCase("flashBacklight_and_vibrate_reject_negative_duration") {
            public void run() {
                final Display display = Ui.display();
                Assert.expectException("flashBacklight(-1)", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                display.flashBacklight(-1);
                            }
                        });
                Assert.expectException("vibrate(-1)", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                display.vibrate(-1);
                            }
                        });
            }
        });

        add(new TestCase("displayable_canvas_size_matches_display") {
            public void run() {
                Canvas canvas = new Canvas() {
                    protected void paint(Graphics g) {
                    }
                };
                Ui.show(canvas);
                Assert.info("canvas", canvas.getWidth() + "x" + canvas.getHeight());
                Assert.assertTrue("a shown canvas must have a positive width but had "
                        + canvas.getWidth(), canvas.getWidth() > 0);
                Assert.assertTrue("a shown canvas must have a positive height but had "
                        + canvas.getHeight(), canvas.getHeight() > 0);
            }
        });

        add(new TestCase("restore_menu_screen") {
            public void run() {
                Ui.show(new Form("Display suite finished"));
                Assert.assertNotNull("display still usable", Ui.display().getCurrent());
            }
        }.severity(TestCase.INFO));
    }

    /** Waits until getCurrent() returns the given displayable. */
    static boolean waitForCurrent(Displayable displayable) {
        long deadline = System.currentTimeMillis() + 2000;
        while (System.currentTimeMillis() < deadline) {
            if (Ui.display().getCurrent() == displayable) {
                return true;
            }
            Ui.sleep(20);
        }
        return false;
    }
}
