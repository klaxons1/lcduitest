/**
 * LcduiTest - MIDP 2.0 LCDUI conformance test MIDlet.
 *
 * javax.microedition.lcdui.Gauge - interactive, definite and indefinite
 * gauges with the value rules of the MIDP 2.0 specification.
 */
package lcduitest.tests;

import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;

import lcduitest.Assert;
import lcduitest.TestCase;
import lcduitest.TestSuite;
import lcduitest.Ui;

public class GaugeSuite extends TestSuite {

    public GaugeSuite() {
        super("Gauge", "Gauge items", "interactive and non-interactive gauges, definite and "
                + "indefinite range, value clamping and the error checks");

        add(new TestCase("interactive_gauge") {
            public void run() {
                Gauge gauge = new Gauge("volume", true, 10, 5);
                Assert.assertEquals("label", "volume", gauge.getLabel());
                Assert.assertTrue("isInteractive()", gauge.isInteractive());
                Assert.assertEquals("getMaxValue()", 10, gauge.getMaxValue());
                Assert.assertEquals("getValue()", 5, gauge.getValue());
                gauge.setValue(0);
                Assert.assertEquals("setValue(0)", 0, gauge.getValue());
                gauge.setValue(10);
                Assert.assertEquals("setValue(maxValue)", 10, gauge.getValue());
            }
        });

        add(new TestCase("interactive_gauge_clamps_the_initial_value") {
            public void run() {
                Gauge low = new Gauge(null, true, 10, -3);
                Assert.assertEquals("an initial value below zero is set to zero", 0,
                        low.getValue());
                Gauge high = new Gauge(null, true, 10, 42);
                Assert.assertEquals("an initial value above maxValue is set to maxValue", 10,
                        high.getValue());
            }
        });

        add(new TestCase("interactive_gauge_requires_a_positive_maximum") {
            public void run() {
                Assert.expectException("an interactive gauge with maxValue 0",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                new Gauge(null, true, 0, 0);
                            }
                        });
                Assert.expectException("an interactive gauge with a negative maxValue",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                new Gauge(null, true, -10, 0);
                            }
                        });
                Assert.expectException("an interactive gauge cannot have INDEFINITE range",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                new Gauge(null, true, Gauge.INDEFINITE, 0);
                            }
                        });
            }
        });

        add(new TestCase("non_interactive_definite_gauge") {
            public void run() {
                Gauge gauge = new Gauge("progress", false, 100, 30);
                Assert.assertFalse("isInteractive()", gauge.isInteractive());
                Assert.assertEquals("getMaxValue()", 100, gauge.getMaxValue());
                Assert.assertEquals("getValue()", 30, gauge.getValue());
                gauge.setValue(99);
                Assert.assertEquals("setValue(99)", 99, gauge.getValue());
            }
        });

        add(new TestCase("non_interactive_gauge_value_range_checks") {
            public void run() {
                final Gauge gauge = new Gauge("progress", false, 10, 5);
                Assert.expectException("setValue(-1)", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                gauge.setValue(-1);
                            }
                        });
                Assert.expectException("setValue(maxValue+1)", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                gauge.setValue(11);
                            }
                        });
            }
        });

        add(new TestCase("shrinking_the_maximum_clips_the_value") {
            public void run() {
                Gauge gauge = new Gauge("progress", false, 10, 10);
                gauge.setMaxValue(4);
                Assert.assertEquals("the value is clipped to the new maximum", 4,
                        gauge.getValue());
                gauge.setMaxValue(20);
                Assert.assertEquals("growing the maximum leaves the value alone", 4,
                        gauge.getValue());
            }
        });

        add(new TestCase("indefinite_gauge") {
            public void run() {
                Gauge gauge = new Gauge("working", false, Gauge.INDEFINITE,
                        Gauge.CONTINUOUS_RUNNING);
                Assert.assertFalse("isInteractive()", gauge.isInteractive());
                Assert.assertEquals("getMaxValue() must be INDEFINITE", Gauge.INDEFINITE,
                        gauge.getMaxValue());
                Assert.assertEquals("the initial state", Gauge.CONTINUOUS_RUNNING,
                        gauge.getValue());
                gauge.setValue(Gauge.INCREMENTAL_UPDATING);
                Assert.assertEquals("setValue(INCREMENTAL_UPDATING)", Gauge.INCREMENTAL_UPDATING,
                        gauge.getValue());
                gauge.setValue(Gauge.CONTINUOUS_IDLE);
                Assert.assertEquals("setValue(CONTINUOUS_IDLE)", Gauge.CONTINUOUS_IDLE,
                        gauge.getValue());
            }
        });

        add(new TestCase("indefinite_gauge_rejects_numeric_values") {
            public void run() {
                final Gauge gauge = new Gauge("working", false, Gauge.INDEFINITE,
                        Gauge.INCREMENTAL_IDLE);
                Assert.expectException("setValue(5) on an indefinite gauge",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                gauge.setValue(5);
                            }
                        });
                Assert.expectException("setValue(-1) on an indefinite gauge",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                gauge.setValue(-1);
                            }
                        });
            }
        });

        add(new TestCase("indefinite_gauge_requires_a_state_as_initial_value") {
            public void run() {
                Assert.expectException("an indefinite gauge with initial value 5",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                new Gauge(null, false, Gauge.INDEFINITE, 5);
                            }
                        });
                Assert.expectException("an indefinite gauge with initial value -1",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                new Gauge(null, false, Gauge.INDEFINITE, -1);
                            }
                        });
            }
        });

        add(new TestCase("non_interactive_gauge_requires_a_valid_maximum") {
            public void run() {
                Assert.expectException("maxValue 0", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                new Gauge(null, false, 0, 0);
                            }
                        });
                Assert.expectException("a negative maxValue that is not INDEFINITE",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                new Gauge(null, false, -7, 0);
                            }
                        });
            }
        });

        add(new TestCase("setMaxValue_between_definite_and_indefinite_range") {
            public void run() {
                Gauge gauge = new Gauge("progress", false, 10, 7);
                gauge.setMaxValue(Gauge.INDEFINITE);
                Assert.assertEquals("the gauge now has indefinite range", Gauge.INDEFINITE,
                        gauge.getMaxValue());
                int value = gauge.getValue();
                Assert.assertTrue("after switching to indefinite range the value must be one "
                        + "of the four state constants but was " + value,
                        value == Gauge.CONTINUOUS_IDLE || value == Gauge.INCREMENTAL_IDLE
                        || value == Gauge.CONTINUOUS_RUNNING
                        || value == Gauge.INCREMENTAL_UPDATING);

                gauge.setMaxValue(20);
                Assert.assertEquals("the gauge has a definite range again", 20,
                        gauge.getMaxValue());
                Assert.assertEquals("switching back to definite range resets the value to zero",
                        0, gauge.getValue());
            }
        });

        add(new TestCase("setMaxValue_rejects_invalid_values") {
            public void run() {
                final Gauge nonInteractive = new Gauge(null, false, 10, 5);
                Assert.expectException("setMaxValue(0)", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                nonInteractive.setMaxValue(0);
                            }
                        });
                Assert.expectException("setMaxValue(-7)", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                nonInteractive.setMaxValue(-7);
                            }
                        });
                final Gauge interactive = new Gauge(null, true, 10, 5);
                Assert.expectException("an interactive gauge cannot become INDEFINITE",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                interactive.setMaxValue(Gauge.INDEFINITE);
                            }
                        });
            }
        });

        add(new TestCase("gauge_inside_a_form") {
            public void run() {
                Form form = new Form("gauge");
                Gauge gauge = new Gauge("volume", true, 10, 5);
                form.append(gauge);
                Ui.show(form);
                Assert.info("preferred size", gauge.getPreferredWidth() + "x"
                        + gauge.getPreferredHeight());
                Assert.assertTrue("a Gauge must have a positive preferred width",
                        gauge.getPreferredWidth() > 0);
                Assert.assertTrue("a Gauge must have a positive preferred height",
                        gauge.getPreferredHeight() > 0);
            }
        }.severity(TestCase.SHOULD));
    }
}
