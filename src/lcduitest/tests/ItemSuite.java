/**
 * LcduiTest - MIDP 2.0 LCDUI conformance test MIDlet.
 *
 * javax.microedition.lcdui.Item - the behaviour that every Item shares. A
 * StringItem is used as the concrete subclass under test.
 */
package lcduitest.tests;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.ItemStateListener;
import javax.microedition.lcdui.StringItem;

import lcduitest.Assert;
import lcduitest.TestCase;
import lcduitest.TestSuite;
import lcduitest.Ui;

public class ItemSuite extends TestSuite {

    public ItemSuite() {
        super("Item", "Item base class", "labels, layout directives, preferred and minimum "
                + "size, item commands and state change notifications");

        add(new TestCase("label_round_trip") {
            public void run() {
                StringItem item = new StringItem("before", "content");
                Assert.assertEquals("the constructor label", "before", item.getLabel());
                item.setLabel("after");
                Assert.assertEquals("setLabel/getLabel", "after", item.getLabel());
                item.setLabel(null);
                Assert.assertNull("setLabel(null) removes the label", item.getLabel());
                item.setLabel("");
                Assert.assertEquals("an empty label is allowed", "", item.getLabel());
            }
        });

        add(new TestCase("default_layout") {
            public void run() {
                StringItem item = new StringItem("label", "content");
                Assert.assertEquals("a new Item uses LAYOUT_DEFAULT", Item.LAYOUT_DEFAULT,
                        item.getLayout());
            }
        });

        add(new TestCase("setLayout_and_getLayout") {
            public void run() {
                StringItem item = new StringItem("label", "content");
                int layout = Item.LAYOUT_LEFT | Item.LAYOUT_NEWLINE_BEFORE
                        | Item.LAYOUT_NEWLINE_AFTER;
                item.setLayout(layout);
                Assert.assertEquals("getLayout() must report the directives that were set",
                        layout, item.getLayout());
                item.setLayout(Item.LAYOUT_DEFAULT);
                Assert.assertEquals("LAYOUT_DEFAULT resets the directives", Item.LAYOUT_DEFAULT,
                        item.getLayout());
            }
        }.severity(TestCase.SHOULD).describe("the platform may normalise the directives, but "
                + "a plain combination must be reported back"));

        add(new TestCase("setLayout_rejects_invalid_directives") {
            public void run() {
                final StringItem item = new StringItem("label", "content");
                Assert.expectException("a bit that is not a layout directive",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                item.setLayout(0x8000);
                            }
                        });
                Assert.expectException("a negative layout value",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                item.setLayout(-1);
                            }
                        });
            }
        });

        add(new TestCase("setLayout_is_illegal_inside_an_alert") {
            public void run() {
                Alert alert = new Alert("alert", "text", null, AlertType.INFO);
                alert.setTimeout(100);
                final Gauge indicator = new Gauge(null, false, Gauge.INDEFINITE,
                        Gauge.CONTINUOUS_IDLE);
                alert.setIndicator(indicator);
                Assert.expectException("setLayout() of the activity indicator",
                        IllegalStateException.class, new Assert.Code() {
                            public void run() {
                                indicator.setLayout(Item.LAYOUT_LEFT);
                            }
                        });
                Assert.expectException("setPreferredSize() of the activity indicator",
                        IllegalStateException.class, new Assert.Code() {
                            public void run() {
                                indicator.setPreferredSize(10, 10);
                            }
                        });
            }
        }.severity(TestCase.SHOULD).describe("an Item that is used as an Alert indicator is "
                + "not owned by a Form, so state changes like layout are forbidden"));

        add(new TestCase("preferred_size_can_be_locked_and_unlocked") {
            public void run() {
                StringItem item = new StringItem("label", "content");
                item.setPreferredSize(50, 30);
                Assert.assertEquals("a locked width is reported back by getPreferredWidth",
                        50, item.getPreferredWidth());
                Assert.assertEquals("a locked height is reported back by getPreferredHeight",
                        30, item.getPreferredHeight());
                item.setPreferredSize(-1, -1);
                Assert.assertTrue("an unlocked width is computed from the contents but was "
                        + item.getPreferredWidth(), item.getPreferredWidth() >= 0);
                Assert.assertTrue("an unlocked height is computed from the contents but was "
                        + item.getPreferredHeight(), item.getPreferredHeight() >= 0);
            }
        });

        add(new TestCase("setPreferredSize_rejects_values_below_minus_one") {
            public void run() {
                final StringItem item = new StringItem("label", "content");
                Assert.expectException("setPreferredSize(-2, 10)", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                item.setPreferredSize(-2, 10);
                            }
                        });
                Assert.expectException("setPreferredSize(10, -2)", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                item.setPreferredSize(10, -2);
                            }
                        });
            }
        });

        add(new TestCase("minimum_size_is_available") {
            public void run() {
                StringItem item = new StringItem("label", "content");
                int width = item.getMinimumWidth();
                int height = item.getMinimumHeight();
                Assert.info("minimum size", width + "x" + height);
                Assert.assertTrue("getMinimumWidth() must not be negative but was " + width,
                        width >= 0);
                Assert.assertTrue("getMinimumHeight() must not be negative but was " + height,
                        height >= 0);
                Assert.info("preferred size", item.getPreferredWidth() + "x"
                        + item.getPreferredHeight());
                Assert.assertTrue("getPreferredWidth() must not be negative", 
                        item.getPreferredWidth() >= 0);
                Assert.assertTrue("getPreferredHeight() must not be negative",
                        item.getPreferredHeight() >= 0);
            }
        });

        add(new TestCase("item_commands") {
            public void run() {
                StringItem item = new StringItem("label", "content");
                Command command = new Command("Do", Command.ITEM, 1);
                item.addCommand(command);
                item.addCommand(command);
                item.removeCommand(command);
                item.removeCommand(command);
                item.setItemCommandListener(new ItemCommandListener() {
                    public void commandAction(Command c, Item i) {
                    }
                });
                item.setItemCommandListener(null);
                Assert.assertNotNull("the item is still usable", item);
            }
        });

        add(new TestCase("addCommand_rejects_null_and_commands_in_an_alert") {
            public void run() {
                final StringItem item = new StringItem("label", "content");
                Assert.expectException("addCommand(null)", NullPointerException.class,
                        new Assert.Code() {
                            public void run() {
                                item.addCommand(null);
                            }
                        });
                Alert alert = new Alert("alert", "text", null, AlertType.INFO);
                alert.setTimeout(100);
                final Gauge indicator = new Gauge(null, false, Gauge.INDEFINITE,
                        Gauge.CONTINUOUS_IDLE);
                alert.setIndicator(indicator);
                Assert.expectException("addCommand() on an Alert indicator",
                        IllegalStateException.class, new Assert.Code() {
                            public void run() {
                                indicator.addCommand(new Command("x", Command.ITEM, 1));
                            }
                        });
            }
        });

        add(new TestCase("notifyStateChanged_requires_a_form") {
            public void run() {
                final StringItem orphan = new StringItem("label", "content");
                Assert.expectException("notifyStateChanged() outside a Form",
                        IllegalStateException.class, new Assert.Code() {
                            public void run() {
                                orphan.notifyStateChanged();
                            }
                        });
            }
        });

        add(new TestCase("notifyStateChanged_reaches_the_form_listener") {
            public void run() {
                Form form = new Form("state");
                final boolean[] called = new boolean[1];
                final StringItem item = new StringItem("label", "content");
                form.append(item);
                form.setItemStateListener(new ItemStateListener() {
                    public void itemStateChanged(Item changed) {
                        if (changed == item) {
                            called[0] = true;
                        }
                    }
                });
                item.notifyStateChanged();
                Assert.assertTrue("notifyStateChanged() must inform the Form's "
                        + "ItemStateListener", Ui.waitForFlag(called, 2000));
            }
        });

        add(new TestCase("appearance_is_only_defined_for_some_items") {
            public void run() {
                StringItem plain = new StringItem("label", "content");
                StringItem link = new StringItem("label", "content", Item.HYPERLINK);
                StringItem button = new StringItem("label", "content", Item.BUTTON);
                Assert.assertEquals("the default appearance of a StringItem is PLAIN",
                        Item.PLAIN, plain.getAppearanceMode());
                Assert.assertEquals("HYPERLINK", Item.HYPERLINK, link.getAppearanceMode());
                Assert.assertEquals("BUTTON", Item.BUTTON, button.getAppearanceMode());
                Assert.expectException("an invalid appearance mode",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                new StringItem("label", "content", 7);
                            }
                        });
            }
        });
    }
}
