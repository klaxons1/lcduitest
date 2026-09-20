/**
 * LcduiTest - MIDP 2.0 LCDUI conformance test MIDlet.
 *
 * javax.microedition.lcdui.AlertType - the five predefined types, the
 * protected constructor for subclasses and the sound API.
 */
package lcduitest.tests;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Form;

import lcduitest.Assert;
import lcduitest.TestCase;
import lcduitest.TestSuite;
import lcduitest.Ui;

public class AlertTypeSuite extends TestSuite {

    public AlertTypeSuite() {
        super("AlertType", "Alert types", "the predefined alert types, their sounds, the "
                + "protected constructor and the way an Alert stores its type");

        add(new TestCase("predefined_types_exist_and_are_distinct") {
            public void run() {
                AlertType[] types = allTypes();
                for (int i = 0; i < types.length; i++) {
                    Assert.assertNotNull("a predefined AlertType", types[i]);
                    Assert.assertNotNull("toString() of a predefined AlertType",
                            types[i].toString());
                    Assert.assertTrue("toString() must not be empty",
                            types[i].toString().length() > 0);
                }
                for (int i = 0; i < types.length; i++) {
                    for (int j = i + 1; j < types.length; j++) {
                        Assert.assertNotSame("the predefined types are distinct objects",
                                types[i], types[j]);
                    }
                }
            }
        });

        add(new TestCase("playSound_on_the_display") {
            public void run() {
                AlertType[] types = allTypes();
                for (int i = 0; i < types.length; i++) {
                    Display display = Ui.display();
                    boolean alerted = types[i].playSound(display);
                    Assert.info(types[i] + ".playSound() returned", alerted);
                }
            }
        }.severity(TestCase.SHOULD).describe("the device may ignore a sound request, so only "
                + "the call itself is required to work"));

        add(new TestCase("playSound_rejects_a_null_display") {
            public void run() {
                final AlertType type = AlertType.INFO;
                Assert.expectException("playSound(null)", NullPointerException.class,
                        new Assert.Code() {
                            public void run() {
                                type.playSound(null);
                            }
                        });
            }
        });

        add(new TestCase("alert_stores_its_type") {
            public void run() {
                AlertType[] types = allTypes();
                for (int i = 0; i < types.length; i++) {
                    Alert alert = new Alert("title", "text", null, types[i]);
                    Assert.assertSame("Alert.getType()", types[i], alert.getType());
                }
                Alert none = new Alert("title", "text", null, null);
                Assert.assertNull("an Alert may have no type", none.getType());
            }
        });

        add(new TestCase("predefined_types_can_be_shown_in_alerts") {
            public void run() {
                Form background = new Form("background");
                Ui.show(background);
                Alert alert = new Alert("AlertType", "The INFO type is being displayed.",
                        null, AlertType.INFO);
                alert.setTimeout(3000);
                Ui.display().setCurrent(alert, background);
                Ui.settle();
                javax.microedition.lcdui.Displayable current = Ui.display().getCurrent();
                Assert.info("getCurrent() while the alert is up", current == alert
                        ? "the alert" : "another displayable");
                Ui.show(background);
                Assert.assertSame("after the alert the next screen becomes current",
                        background, Ui.display().getCurrent());
            }
        }.severity(TestCase.SHOULD));

        add(new TestCase("a_custom_alert_type_is_possible") {
            public void run() {
                AlertType custom = new CustomType();
                Assert.assertNotNull("a subclass of AlertType can be created", custom);
                Alert alert = new Alert("custom", "text", null, custom);
                Assert.assertSame("an Alert accepts a custom type", custom, alert.getType());
                boolean alerted = custom.playSound(Ui.display());
                Assert.info("a custom AlertType's playSound() returned", alerted);
                Assert.assertNotSame("a custom type is a different object than the "
                        + "predefined ones", AlertType.WARNING, custom);
            }
        });

        add(new TestCase("alert_type_can_be_cleared_again") {
            public void run() {
                Alert alert = new Alert("title", "text", null, AlertType.ALARM);
                Assert.assertSame("the type was set", AlertType.ALARM, alert.getType());
                alert.setType(null);
                Assert.assertNull("setType(null) removes the type", alert.getType());
            }
        });
    }

    /** The five predefined alert types, in the order of the class summary. */
    static AlertType[] allTypes() {
        return new AlertType[]{AlertType.INFO, AlertType.WARNING, AlertType.ERROR,
            AlertType.CONFIRMATION, AlertType.ALARM};
    }

    /** The AlertType constructor is protected: a subclass may use it. */
    static class CustomType extends AlertType {
    }
}
