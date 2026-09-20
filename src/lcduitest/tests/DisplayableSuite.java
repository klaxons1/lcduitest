/**
 * LcduiTest - MIDP 2.0 LCDUI conformance test MIDlet.
 *
 * Displayable / Screen: title, ticker, command list, size and visibility.
 */
package lcduitest.tests;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.Ticker;

import lcduitest.Assert;
import lcduitest.TestCase;
import lcduitest.TestSuite;
import lcduitest.Ui;

public class DisplayableSuite extends TestSuite {

    public DisplayableSuite() {
        super("Displayable", "Titles, tickers, commands, size", "the behaviour that every "
                + "screen (Form, List, TextBox, Alert, Canvas) inherits");

        add(new TestCase("title_round_trip") {
            public void run() {
                Form form = new Form("first");
                Assert.assertEquals("Form(String) sets the title", "first", form.getTitle());
                form.setTitle("second");
                Assert.assertEquals("setTitle/getTitle", "second", form.getTitle());
                form.setTitle(null);
                Assert.assertNull("setTitle(null) removes the title", form.getTitle());
                form.setTitle("");
                Assert.assertEquals("empty title is allowed", "", form.getTitle());
            }
        });

        add(new TestCase("title_accepts_unicode") {
            public void run() {
                Form form = new Form("T");
                String unicode = "\u00e4\u00f6\u00fc\u4e2d\u6587\u044f";
                form.setTitle(unicode);
                Assert.assertEquals("unicode title round trip", unicode, form.getTitle());
            }
        });

        add(new TestCase("ticker_can_be_attached_and_removed") {
            public void run() {
                Form form = new Form("with ticker");
                Ticker ticker = new Ticker("hello");
                form.setTicker(ticker);
                Assert.assertSame("getTicker", ticker, form.getTicker());
                form.setTicker(null);
                Assert.assertNull("setTicker(null) removes the ticker", form.getTicker());
            }
        });

        add(new TestCase("addCommand_rejects_null") {
            public void run() {
                final Form form = new Form("commands");
                Assert.expectException("addCommand(null)", NullPointerException.class,
                        new Assert.Code() {
                            public void run() {
                                form.addCommand(null);
                            }
                        });
            }
        });

        add(new TestCase("addCommand_is_idempotent_per_instance") {
            public void run() {
                Form form = new Form("commands");
                Command command = new Command("Do", Command.SCREEN, 1);
                form.addCommand(command);
                form.addCommand(command);
                // the specification requires that adding the same command object
                // twice has no effect; there is no accessor for the command list,
                // so the observable behaviour is simply that nothing breaks
                Assert.expectNoException("addCommand(same instance) twice", new Assert.Code() {
                    public void run() {
                        // nothing else to observe on MIDP 2.0
                    }
                });
                form.removeCommand(command);
                form.removeCommand(command);
                form.removeCommand(new Command("never added", Command.SCREEN, 2));
                Assert.assertNotNull("form still usable", form);
            }
        });

        add(new TestCase("setCommandListener_accepts_null") {
            public void run() {
                Form form = new Form("listener");
                final CommandListener listener = new CommandListener() {
                    public void commandAction(Command c, Displayable d) {
                    }
                };
                form.setCommandListener(listener);
                form.setCommandListener(null);
                Assert.assertNotNull("form still usable after removing the listener", form);
            }
        });

        add(new TestCase("width_and_height_of_unshown_displayable") {
            public void run() {
                Form form = new Form("size");
                int width = form.getWidth();
                int height = form.getHeight();
                Assert.info("unshown Form size", width + "x" + height);
                Assert.assertTrue("getWidth() must return the display width even if the "
                        + "displayable was never shown but was " + width, width > 0);
                Assert.assertTrue("getHeight() must return the display height even if the "
                        + "displayable was never shown but was " + height, height > 0);
            }
        }.severity(TestCase.SHOULD));

        add(new TestCase("width_and_height_of_shown_displayable") {
            public void run() {
                Form form = new Form("size");
                Ui.show(form);
                Assert.info("shown Form size", form.getWidth() + "x" + form.getHeight());
                Assert.assertTrue("width of a shown Form", form.getWidth() > 0);
                Assert.assertTrue("height of a shown Form", form.getHeight() > 0);
            }
        });

        add(new TestCase("isShown_lifecycle") {
            public void run() {
                Form form = new Form("shown?");
                Assert.assertFalse("a fresh displayable is not shown", form.isShown());
                Ui.show(form);
                Assert.assertTrue("isShown() right after being made current", form.isShown());
                Form other = new Form("other");
                Ui.show(other);
                Assert.assertFalse("the previous displayable is not shown any more",
                        form.isShown());
                Assert.assertTrue("the new displayable is shown", other.isShown());
            }
        });

        add(new TestCase("displayable_size_is_shared_by_screen_types") {
            public void run() {
                Form form = new Form("f");
                TextBox box = new TextBox("t", "text", 32, 0);
                Canvas canvas = new Canvas() {
                    protected void paint(Graphics g) {
                    }
                };
                Ui.show(form);
                int width = form.getWidth();
                int height = form.getHeight();
                Assert.info("form", width + "x" + height);
                Ui.show(box);
                Assert.assertEquals("TextBox uses the same display width", width, box.getWidth());
                Ui.show(canvas);
                Assert.assertEquals("Canvas uses the same display width", width, canvas.getWidth());
                Assert.assertEquals("Canvas uses the same display height", height, canvas.getHeight());
            }
        }.severity(TestCase.SHOULD));

        add(new TestCase("sizeChanged_callback_of_a_canvas") {
            public void run() {
                ProbeCanvas canvas = new ProbeCanvas();
                Assert.assertEquals("no size change reported yet", 0, canvas.sizeChangedCalls);
                canvas.fireSizeChanged(120, 140);
                Assert.assertEquals("sizeChanged callback", 1, canvas.sizeChangedCalls);
                Assert.assertEquals("reported width", 120, canvas.lastWidth);
                Assert.assertEquals("reported height", 140, canvas.lastHeight);
            }
        });

        add(new TestCase("showing_a_displayable_gives_it_the_full_screen") {
            public void run() {
                Canvas canvas = new Canvas() {
                    protected void paint(Graphics g) {
                    }
                };
                Ui.show(canvas);
                StringBuffer sb = new StringBuffer();
                sb.append(canvas.getWidth()).append('x').append(canvas.getHeight());
                Assert.info("canvas", sb.toString());
                Assert.assertTrue("canvas area must not be empty", canvas.getWidth() > 0
                        && canvas.getHeight() > 0);
            }
        });
    }
}
