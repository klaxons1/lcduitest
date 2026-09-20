/**
 * LcduiTest - MIDP 2.0 LCDUI conformance test MIDlet.
 *
 * javax.microedition.lcdui.Ticker - the scrolling text of a Displayable.
 */
package lcduitest.tests;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.Ticker;

import lcduitest.Assert;
import lcduitest.TestCase;
import lcduitest.TestSuite;
import lcduitest.Ui;

public class TickerSuite extends TestSuite {

    public TickerSuite() {
        super("Ticker", "Ticker", "the string, the null rules and the way a ticker is attached "
                + "to the different displayables");

        add(new TestCase("constructor_and_getString") {
            public void run() {
                Ticker ticker = new Ticker("hello ticker");
                Assert.assertEquals("getString()", "hello ticker", ticker.getString());
                Assert.assertNotNull("toString() is inherited from Object", ticker.toString());
            }
        });

        add(new TestCase("setString") {
            public void run() {
                Ticker ticker = new Ticker("first");
                ticker.setString("second");
                Assert.assertEquals("setString()", "second", ticker.getString());
                ticker.setString("");
                Assert.assertEquals("an empty string is allowed", "", ticker.getString());
                ticker.setString("a much longer string than usual, to make the ticker scroll");
                Assert.assertEquals("a long string", "a much longer string than usual, to "
                        + "make the ticker scroll", ticker.getString());
            }
        });

        add(new TestCase("null_strings_are_rejected") {
            public void run() {
                Assert.expectException("new Ticker(null)", NullPointerException.class,
                        new Assert.Code() {
                            public void run() {
                                new Ticker(null);
                            }
                        });
                final Ticker ticker = new Ticker("text");
                Assert.expectException("setString(null)", NullPointerException.class,
                        new Assert.Code() {
                            public void run() {
                                ticker.setString(null);
                            }
                        });
                Assert.assertEquals("the string is unchanged after the failed setString()",
                        "text", ticker.getString());
            }
        });

        add(new TestCase("ticker_on_a_form") {
            public void run() {
                Form form = new Form("form");
                Assert.assertNull("a new Form has no ticker", form.getTicker());
                Ticker ticker = new Ticker("scrolling");
                form.setTicker(ticker);
                Assert.assertSame("setTicker()/getTicker()", ticker, form.getTicker());
                form.setTicker(null);
                Assert.assertNull("setTicker(null) removes the ticker", form.getTicker());
            }
        });

        add(new TestCase("ticker_on_a_list_and_on_a_canvas") {
            public void run() {
                Ticker ticker = new Ticker("shared");
                List list = new List("list", List.IMPLICIT);
                list.setTicker(ticker);
                Assert.assertSame("a List takes a ticker", ticker, list.getTicker());
                Canvas canvas = new ProbeCanvas();
                canvas.setTicker(ticker);
                Assert.assertSame("a Canvas takes the same ticker object", ticker,
                        canvas.getTicker());
            }
        }.severity(TestCase.SHOULD));

        add(new TestCase("a_ticker_is_displayed") {
            public void run() {
                Form form = new Form("with ticker");
                form.append("The ticker below must scroll on the emulator's screen.");
                form.setTicker(new Ticker("LcduiTest: javax.microedition.lcdui.Ticker"));
                Ui.show(form);
                Ui.settle();
                Assert.assertTrue("the Form with the ticker is visible", form.isShown());
                boolean painted = Ui.waitForFlag(new boolean[]{Ui.width(form) > 0}, 2000);
                Assert.assertTrue("the displayable has a size", painted);
                Assert.assertNotNull("the ticker is still attached", form.getTicker());
            }
        }.severity(TestCase.SHOULD));
    }
}
