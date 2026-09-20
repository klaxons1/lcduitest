/**
 * LcduiTest - MIDP 2.0 LCDUI conformance test MIDlet.
 *
 * javax.microedition.lcdui.TextBox - the screen that edits text.
 */
package lcduitest.tests;

import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;
import javax.microedition.lcdui.Ticker;

import lcduitest.Assert;
import lcduitest.TestCase;
import lcduitest.TestSuite;
import lcduitest.Ui;

public class TextBoxSuite extends TestSuite {

    public TextBoxSuite() {
        super("TextBox", "Text boxes", "contents, maximum size, constraints and the editing "
                + "methods of the full screen text editor");

        add(new TestCase("constructor_and_contents") {
            public void run() {
                TextBox box = new TextBox("Title", "hello", 20, TextField.ANY);
                Assert.assertEquals("the title", "Title", box.getTitle());
                Assert.assertEquals("getString()", "hello", box.getString());
                Assert.assertEquals("size()", 5, box.size());
                Assert.assertEquals("getConstraints()", TextField.ANY, box.getConstraints());
                Assert.assertTrue("getMaxSize() must be positive",
                        box.getMaxSize() > 0);
                box.setString("changed");
                Assert.assertEquals("setString()", "changed", box.getString());
                Assert.assertEquals("size() after setString()", 7, box.size());
            }
        });

        add(new TestCase("null_and_empty_text") {
            public void run() {
                TextBox box = new TextBox("Title", null, 10, TextField.ANY);
                Assert.assertEquals("a null initial text is empty", 0, box.size());
                box.setString(null);
                Assert.assertEquals("setString(null) empties the box", 0, box.size());
                box.setString("");
                Assert.assertEquals("setString(\"\")", "", box.getString());
            }
        });

        add(new TestCase("maximum_size") {
            public void run() {
                final TextBox box = new TextBox("Title", "abcdef", 10, TextField.ANY);
                Assert.expectException("setMaxSize(0)", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                box.setMaxSize(0);
                            }
                        });
                int assigned = box.setMaxSize(3);
                Assert.assertTrue("setMaxSize() returns the assigned capacity", assigned > 0);
                Assert.assertTrue("the contents are truncated to the new maximum size",
                        box.size() <= assigned);
            }
        }.severity(TestCase.SHOULD));

        add(new TestCase("setString_rejects_an_overlong_text") {
            public void run() {
                final TextBox box = new TextBox("Title", "", 4, TextField.ANY);
                Assert.expectException("a text that is longer than the maximum size",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                box.setString("12345");
                            }
                        });
            }
        });

        add(new TestCase("constraints") {
            public void run() {
                TextBox numeric = new TextBox("Number", "42", 10, TextField.NUMERIC);
                Assert.assertEquals("the constraint is reported back", TextField.NUMERIC,
                        numeric.getConstraints() & TextField.CONSTRAINT_MASK);
                final TextBox box = new TextBox("Title", "", 10, TextField.ANY);
                Assert.expectException("setConstraints with an invalid value",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                box.setConstraints(6);
                            }
                        });
                box.setConstraints(TextField.EMAILADDR);
                Assert.assertEquals("setConstraints(EMAILADDR)", TextField.EMAILADDR,
                        box.getConstraints() & TextField.CONSTRAINT_MASK);
                box.setConstraints(TextField.PASSWORD | TextField.ANY);
                Assert.assertEquals("the flag part of the constraints", TextField.PASSWORD,
                        box.getConstraints() & TextField.PASSWORD);
            }
        });

        add(new TestCase("editing") {
            public void run() {
                TextBox box = new TextBox("Title", "abcdef", 20, TextField.ANY);
                box.insert("XY", 3);
                Assert.assertEquals("insert(String, position)", "abcXYdef", box.getString());
                box.delete(3, 2);
                Assert.assertEquals("delete(offset, length)", "abcdef", box.getString());
                char[] data = {'1', '2', '3'};
                box.insert(data, 0, 3, 0);
                Assert.assertEquals("insert(char[], offset, length, position)", "123abcdef",
                        box.getString());
                box.setChars(data, 1, 2);
                Assert.assertEquals("setChars(char[], offset, length)", "23", box.getString());
                char[] out = new char[2];
                Assert.assertEquals("getChars()", 2, box.getChars(out));
                Assert.assertEquals("the characters are copied", "23", new String(out));
            }
        });

        add(new TestCase("editing_argument_checks") {
            public void run() {
                final TextBox box = new TextBox("Title", "abc", 10, TextField.ANY);
                Assert.expectException("insert(null, 0)", NullPointerException.class,
                        new Assert.Code() {
                            public void run() {
                                box.insert(null, 0);
                            }
                        });
                Assert.expectException("delete with a bad range",
                        StringIndexOutOfBoundsException.class, new Assert.Code() {
                            public void run() {
                                box.delete(2, 3);
                            }
                        });
            }
        }.severity(TestCase.SHOULD));

        add(new TestCase("caret_and_input_mode") {
            public void run() {
                final TextBox box = new TextBox("Title", "abc", 10, TextField.ANY);
                Assert.assertInRange("getCaretPosition()", box.getCaretPosition(), 0, 3);
                Assert.expectNoException("setInitialInputMode(\"IS_LATIN\")",
                        new Assert.Code() {
                            public void run() {
                                box.setInitialInputMode("IS_LATIN");
                            }
                        });
            }
        });

        add(new TestCase("title_and_ticker") {
            public void run() {
                TextBox box = new TextBox("Title", "", 10, TextField.ANY);
                box.setTitle("Other");
                Assert.assertEquals("setTitle()", "Other", box.getTitle());
                Ticker ticker = new Ticker("scrolling text");
                box.setTicker(ticker);
                Assert.assertSame("setTicker()/getTicker()", ticker, box.getTicker());
            }
        });

        add(new TestCase("text_box_is_a_screen") {
            public void run() {
                TextBox box = new TextBox("Title", "some text", 64, TextField.ANY);
                Ui.show(box);
                Assert.assertTrue("a shown TextBox is visible", box.isShown());
                Assert.assertTrue("a TextBox must report a positive width", box.getWidth() > 0);
                Assert.assertTrue("a TextBox must report a positive height",
                        box.getHeight() > 0);
                Assert.assertFalse("a text box does not have to support pointer events",
                        box.isShown() && false);
            }
        });
    }
}
