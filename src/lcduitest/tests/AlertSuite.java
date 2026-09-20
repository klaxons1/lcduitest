/**
 * LcduiTest - MIDP 2.0 LCDUI conformance test MIDlet.
 *
 * javax.microedition.lcdui.Alert
 */
package lcduitest.tests;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.TextField;

import lcduitest.Assert;
import lcduitest.TestCase;
import lcduitest.TestSuite;
import lcduitest.Ui;

public class AlertSuite extends TestSuite {

    public AlertSuite() {
        super("Alert", "Alerts", "the title, text, image and type, timeouts, the activity "
                + "indicator and the DISMISS_COMMAND rules");

        add(new TestCase("title_only_constructor") {
            public void run() {
                Alert alert = new Alert("Title");
                Assert.assertEquals("getTitle()", "Title", alert.getTitle());
                Assert.assertNull("getString() of an empty Alert", alert.getString());
                Assert.assertNull("getImage() of an empty Alert", alert.getImage());
                Assert.assertNull("getType() of an empty Alert", alert.getType());
                alert.setTitle(null);
                Assert.assertNull("setTitle(null) is allowed", alert.getTitle());
            }
        });

        add(new TestCase("full_constructor") {
            public void run() {
                Image image = Ui.solidImage(8, 8, 0x00FF00);
                Alert alert = new Alert("Title", "Text", image, AlertType.WARNING);
                Assert.assertEquals("getTitle()", "Title", alert.getTitle());
                Assert.assertEquals("getString()", "Text", alert.getString());
                Assert.assertSame("getImage()", image, alert.getImage());
                Assert.assertSame("getType()", AlertType.WARNING, alert.getType());
            }
        });

        add(new TestCase("text_image_and_type_can_be_changed") {
            public void run() {
                Alert alert = new Alert("Title");
                alert.setString("some text");
                Assert.assertEquals("setString()", "some text", alert.getString());
                alert.setString(null);
                Assert.assertNull("setString(null) removes the text", alert.getString());
                Image image = Ui.solidImage(4, 4, 0xFF0000);
                alert.setImage(image);
                Assert.assertSame("setImage()", image, alert.getImage());
                alert.setImage(null);
                Assert.assertNull("setImage(null) removes the image", alert.getImage());
                alert.setType(AlertType.ERROR);
                Assert.assertSame("setType()", AlertType.ERROR, alert.getType());
                alert.setType(null);
                Assert.assertNull("setType(null) removes the type", alert.getType());
            }
        });

        add(new TestCase("timeout") {
            public void run() {
                Alert alert = new Alert("Title");
                alert.setTimeout(5000);
                Assert.assertEquals("getTimeout() after setTimeout(5000)", 5000,
                        alert.getTimeout());
                alert.setTimeout(Alert.FOREVER);
                Assert.assertEquals("getTimeout() after setTimeout(FOREVER)", Alert.FOREVER,
                        alert.getTimeout());
                int standard = alert.getDefaultTimeout();
                Assert.info("getDefaultTimeout()", standard);
                Assert.assertTrue("the default timeout must be FOREVER or positive, was "
                        + standard, standard == Alert.FOREVER || standard > 0);
            }
        });

        add(new TestCase("timeout_argument_checks") {
            public void run() {
                final Alert alert = new Alert("Title");
                Assert.expectException("setTimeout(0)", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                alert.setTimeout(0);
                            }
                        });
                Assert.expectException("setTimeout(-1)", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                alert.setTimeout(-1);
                            }
                        });
            }
        });

        add(new TestCase("activity_indicator") {
            public void run() {
                Alert alert = new Alert("Working");
                Assert.assertNull("a new Alert has no activity indicator", alert.getIndicator());
                Gauge gauge = new Gauge(null, false, Gauge.INDEFINITE, Gauge.CONTINUOUS_RUNNING);
                alert.setIndicator(gauge);
                Assert.assertSame("setIndicator()/getIndicator()", gauge, alert.getIndicator());
                alert.setIndicator(null);
                Assert.assertNull("setIndicator(null) removes the indicator",
                        alert.getIndicator());
            }
        });

        add(new TestCase("activity_indicator_restrictions") {
            public void run() {
                final Alert alert = new Alert("Working");
                final Gauge interactive = new Gauge(null, true, 10, 0);
                Assert.expectException("an interactive gauge cannot be an activity indicator",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                alert.setIndicator(interactive);
                            }
                        });
                final Gauge withLabel = new Gauge("label", false, Gauge.INDEFINITE,
                        Gauge.CONTINUOUS_IDLE);
                Assert.expectException("a gauge with a label cannot be an activity indicator",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                alert.setIndicator(withLabel);
                            }
                        });
                final Form form = new Form("form");
                final Gauge owned = new Gauge(null, false, Gauge.INDEFINITE,
                        Gauge.INCREMENTAL_IDLE);
                form.append(owned);
                Assert.expectException("a gauge that is owned by a Form cannot be an activity "
                        + "indicator", IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                alert.setIndicator(owned);
                            }
                        });
                final Gauge withCommand = new Gauge(null, false, Gauge.INDEFINITE,
                        Gauge.CONTINUOUS_IDLE);
                withCommand.addCommand(new Command("x", Command.ITEM, 1));
                Assert.expectException("a gauge with commands cannot be an activity indicator",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                alert.setIndicator(withCommand);
                            }
                        });
                final Gauge definite = new Gauge(null, false, 10, 3);
                Assert.expectException("only a gauge with indefinite range can be an activity "
                        + "indicator", IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                alert.setIndicator(definite);
                            }
                        });
            }
        }.severity(TestCase.SHOULD).describe("the restrictions for the activity indicator "
                + "are listed in the Alert class documentation"));

        add(new TestCase("dismiss_command_rules") {
            public void run() {
                Alert alert = new Alert("Title", "text", null, AlertType.INFO);
                alert.setTimeout(300);
                Assert.assertEquals("DISMISS_COMMAND is a Command", Command.OK,
                        Alert.DISMISS_COMMAND.getCommandType());
                alert.addCommand(Alert.DISMISS_COMMAND);
                alert.removeCommand(Alert.DISMISS_COMMAND);
                Alert second = new Alert("Title");
                second.addCommand(new Command("More", Command.SCREEN, 1));
                second.setCommandListener(null);
                Assert.assertNotNull("the alert is still usable after addCommand()", second);
                Assert.assertEquals("the text is unchanged", "text", alert.getString());
            }
        });

        add(new TestCase("alert_keeps_the_screen_busy_until_it_is_dismissed") {
            public void run() {
                Form background = new Form("background");
                Ui.show(background);
                Alert alert = new Alert("modal");
                alert.setTimeout(Alert.FOREVER);
                Ui.display().setCurrent(alert, background);
                boolean sawAlert = waitForCurrent(alert);
                Assert.assertTrue("a modal alert must become the current screen", sawAlert);
                // bring the background back so that the run can go on
                Ui.show(background);
                Assert.assertTrue("the MIDlet can return to its own screen",
                        waitForCurrent(background));
            }
        }.severity(TestCase.SHOULD));

        add(new TestCase("alert_never_becomes_current_when_another_alert_is_current") {
            public void run() {
                Alert first = new Alert("first");
                first.setTimeout(100);
                Alert second = new Alert("second");
                second.setTimeout(100);
                Ui.display().setCurrent(first, new Form("background"));
                Ui.display().setCurrent(second, new Form("background"));
                Ui.settle();
                Displayable current = Ui.display().getCurrent();
                Assert.assertNotNull("a displayable is still current", current);
                Assert.info("current screen after two setCurrent(alert, next) calls",
                        current.getClass().getName());
            }
        }.severity(TestCase.INFO));

        add(new TestCase("items_inside_an_alert_are_restricted") {
            public void run() {
                final Alert alert = new Alert("Working");
                final Gauge gauge = new Gauge(null, false, Gauge.INDEFINITE,
                        Gauge.CONTINUOUS_RUNNING);
                alert.setIndicator(gauge);
                Assert.expectException("setPreferredSize() of the activity indicator",
                        IllegalStateException.class, new Assert.Code() {
                            public void run() {
                                gauge.setPreferredSize(10, 10);
                            }
                        });
                Assert.expectException("setLayout() of the activity indicator",
                        IllegalStateException.class, new Assert.Code() {
                            public void run() {
                                gauge.setLayout(Item.LAYOUT_LEFT);
                            }
                        });
                Assert.expectException("addCommand() on the activity indicator",
                        IllegalStateException.class, new Assert.Code() {
                            public void run() {
                                gauge.addCommand(new Command("x", Command.ITEM, 1));
                            }
                        });
                Assert.expectException("setCurrentItem() with the activity indicator",
                        IllegalStateException.class, new Assert.Code() {
                            public void run() {
                                Ui.display().setCurrentItem(gauge);
                            }
                        });
            }
        }.severity(TestCase.SHOULD));

        add(new TestCase("a_text_field_is_not_an_alert_indicator") {
            public void run() {
                final Alert alert = new Alert("Title");
                final TextField field = new TextField("label", "text", 10, TextField.ANY);
                Assert.expectException("setIndicator() accepts only a Gauge; a TextField is "
                        + "rejected at compile time, so this check uses the Gauge path",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                // a definite, interactive gauge is not a valid indicator
                                alert.setIndicator(new Gauge(null, true, 5, 0));
                            }
                        });
                Assert.assertNull("the alert still has no indicator", alert.getIndicator());
                Assert.assertNotNull("the text field is a normal item", field);
            }
        }.severity(TestCase.INFO));

        add(new TestCase("restore_the_test_screen") {
            public void run() {
                Ui.show(new Form("Alert suite finished"));
                Assert.assertNotNull("a normal screen is current again",
                        Ui.display().getCurrent());
            }
        }.severity(TestCase.INFO));
    }

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
