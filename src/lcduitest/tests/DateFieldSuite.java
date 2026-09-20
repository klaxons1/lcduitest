/**
 * LcduiTest - MIDP 2.0 LCDUI conformance test MIDlet.
 *
 * javax.microedition.lcdui.DateField
 */
package lcduitest.tests;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.microedition.lcdui.DateField;

import lcduitest.Assert;
import lcduitest.TestCase;
import lcduitest.TestSuite;

public class DateFieldSuite extends TestSuite {

    public DateFieldSuite() {
        super("DateField", "Date and time fields", "the three input modes, date round trips, "
                + "the uninitialised state and the time zone constructor");

        add(new TestCase("modes_end_exclusive_of_implicit_values") {
            public void run() {
                DateField date = new DateField("d", DateField.DATE);
                DateField time = new DateField("t", DateField.TIME);
                DateField both = new DateField("b", DateField.DATE_TIME);
                Assert.assertEquals("getInputMode() of a DATE field", DateField.DATE,
                        date.getInputMode());
                Assert.assertEquals("getInputMode() of a TIME field", DateField.TIME,
                        time.getInputMode());
                Assert.assertEquals("getInputMode() of a DATE_TIME field", DateField.DATE_TIME,
                        both.getInputMode());
            }
        });

        add(new TestCase("invalid_mode_is_rejected") {
            public void run() {
                Assert.expectException("DateField with mode 0", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                new DateField("d", 0);
                            }
                        });
                Assert.expectException("DateField with mode 4", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                new DateField("d", 4);
                            }
                        });
                final DateField field = new DateField("d", DateField.DATE);
                Assert.expectException("setInputMode(0)", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                field.setInputMode(0);
                            }
                        });
            }
        });

        add(new TestCase("setInputMode_switches_the_mode") {
            public void run() {
                DateField field = new DateField("d", DateField.DATE);
                field.setInputMode(DateField.DATE_TIME);
                Assert.assertEquals("setInputMode(DATE_TIME)", DateField.DATE_TIME,
                        field.getInputMode());
                field.setInputMode(DateField.TIME);
                Assert.assertEquals("setInputMode(TIME)", DateField.TIME, field.getInputMode());
            }
        });

        add(new TestCase("date_round_trip") {
            public void run() {
                DateField field = new DateField("when", DateField.DATE);
                Calendar calendar = Calendar.getInstance();
                calendar.set(2024, Calendar.MARCH, 17, 0, 0, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                Date input = calendar.getTime();
                field.setDate(input);
                Date output = field.getDate();
                Assert.assertNotNull("getDate() must return the date that was set", output);
                Calendar check = Calendar.getInstance();
                check.setTime(output);
                Assert.assertEquals("year", 2024, check.get(Calendar.YEAR));
                Assert.assertEquals("month", Calendar.MARCH, check.get(Calendar.MONTH));
                Assert.assertEquals("day of month", 17, check.get(Calendar.DAY_OF_MONTH));
            }
        });

        add(new TestCase("date_can_be_cleared") {
            public void run() {
                DateField field = new DateField("when", DateField.DATE_TIME);
                field.setDate(new Date());
                Assert.assertNotNull("a date was set", field.getDate());
                field.setDate(null);
                Assert.assertNull("setDate(null) puts the field into the "
                        + "\"not initialized\" state", field.getDate());
            }
        });

        add(new TestCase("date_field_with_a_time_zone") {
            public void run() {
                DateField field = new DateField("when", DateField.DATE_TIME,
                        TimeZone.getTimeZone("GMT"));
                Assert.assertEquals("the mode is kept", DateField.DATE_TIME,
                        field.getInputMode());
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
                calendar.set(2020, Calendar.JANUARY, 2, 3, 4, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                field.setDate(calendar.getTime());
                Date output = field.getDate();
                Assert.assertNotNull("getDate() of a field with a time zone", output);
                Calendar check = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
                check.setTime(output);
                Assert.assertEquals("the date is preserved", 2020, check.get(Calendar.YEAR));
                Assert.assertEquals("the day is preserved", 2,
                        check.get(Calendar.DAY_OF_MONTH));
            }
        }.severity(TestCase.SHOULD));

        add(new TestCase("time_mode_ignores_the_date_part") {
            public void run() {
                DateField field = new DateField("t", DateField.TIME);
                Calendar epochDay = Calendar.getInstance();
                epochDay.set(1970, Calendar.JANUARY, 1, 13, 45, 0);
                epochDay.set(Calendar.MILLISECOND, 0);
                field.setDate(epochDay.getTime());
                Date output = field.getDate();
                Assert.assertNotNull("getDate() in TIME mode", output);
                Calendar check = Calendar.getInstance();
                check.setTime(output);
                Assert.assertEquals("the hour is preserved", 13, check.get(Calendar.HOUR_OF_DAY));
                Assert.assertEquals("the minute is preserved", 45, check.get(Calendar.MINUTE));
            }
        }.severity(TestCase.SHOULD).describe("the specification only requires one minute "
                + "precision, so the state before the minutes is not compared"));

        add(new TestCase("label") {
            public void run() {
                DateField field = new DateField("label", DateField.DATE);
                Assert.assertEquals("the constructor label", "label", field.getLabel());
                field.setLabel("other");
                Assert.assertEquals("setLabel()", "other", field.getLabel());
            }
        });

        add(new TestCase("preferred_size") {
            public void run() {
                DateField field = new DateField("when", DateField.DATE_TIME);
                Assert.info("preferred size", field.getPreferredWidth() + "x"
                        + field.getPreferredHeight());
                Assert.assertTrue("a DateField must have a positive preferred width",
                        field.getPreferredWidth() > 0);
                Assert.assertTrue("a DateField must have a positive preferred height",
                        field.getPreferredHeight() > 0);
            }
        }.severity(TestCase.SHOULD));
    }
}
