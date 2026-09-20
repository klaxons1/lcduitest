/**
 * LcduiTest - MIDP 2.0 LCDUI conformance test MIDlet.
 *
 * javax.microedition.lcdui.TextField
 */
package lcduitest.tests;

import javax.microedition.lcdui.TextField;

import lcduitest.Assert;
import lcduitest.TestCase;
import lcduitest.TestSuite;

public class TextFieldSuite extends TestSuite {

    public TextFieldSuite() {
        super("TextField", "Text fields", "contents, maximum size, the constraint modes, "
                + "insert / delete / getChars / setChars and the carets");

        add(new TestCase("constructor_and_contents") {
            public void run() {
                TextField field = new TextField("label", "hello", 20, TextField.ANY);
                Assert.assertEquals("label", "label", field.getLabel());
                Assert.assertEquals("getString()", "hello", field.getString());
                Assert.assertEquals("size()", 5, field.size());
                Assert.assertEquals("getConstraints()", TextField.ANY, field.getConstraints());
                Assert.info("getMaxSize()", field.getMaxSize());
                Assert.assertTrue("getMaxSize() must be positive but was " + field.getMaxSize(),
                        field.getMaxSize() > 0);
                field.setString("other");
                Assert.assertEquals("setString()", "other", field.getString());
                Assert.assertEquals("size() follows setString()", 5, field.size());
            }
        });

        add(new TestCase("setString_truncates_or_rejects") {
            public void run() {
                final TextField field = new TextField("label", null, 4, TextField.ANY);
                Assert.assertEquals("a null initial text is an empty text field", 0,
                        field.size());
                field.setString(null);
                Assert.assertEquals("setString(null) empties the field", 0, field.size());
                Assert.expectException("a text that is longer than the maximum size",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                field.setString("12345");
                            }
                        });
                field.setString("1234");
                Assert.assertEquals("a text of exactly the maximum size is accepted", "1234",
                        field.getString());
            }
        });

        add(new TestCase("max_size") {
            public void run() {
                TextField field = new TextField("label", "abcdef", 10, TextField.ANY);
                int assigned = field.setMaxSize(3);
                Assert.assertTrue("setMaxSize() must return a positive capacity",
                        assigned > 0);
                Assert.assertEquals("the contents are truncated to the new maximum size", 3,
                        field.getString().length());
                Assert.assertEquals("the truncation keeps the first characters", "abc",
                        field.getString());
                Assert.assertEquals("getMaxSize()", assigned, field.getMaxSize());
            }
        }.severity(TestCase.SHOULD));

        add(new TestCase("max_size_rejects_invalid_values") {
            public void run() {
                final TextField field = new TextField("label", "", 10, TextField.ANY);
                Assert.expectException("setMaxSize(0)", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                field.setMaxSize(0);
                            }
                        });
                Assert.expectException("setMaxSize(-5)", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                field.setMaxSize(-5);
                            }
                        });
                Assert.expectException("a TextField with a maximum size of 0",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                new TextField("label", "", 0, TextField.ANY);
                            }
                        });
            }
        });

        add(new TestCase("constraint_round_trip") {
            public void run() {
                int[] constraints = {TextField.ANY, TextField.EMAILADDR, TextField.URL,
                    TextField.NUMERIC, TextField.PHONENUMBER, TextField.DECIMAL};
                String[] values = {"", "a@b.c", "http://x.y", "42", "+49 30 12345", "1.5"};
                for (int i = 0; i < constraints.length; i++) {
                    TextField field = new TextField("label", values[i], 32, constraints[i]);
                    Assert.assertEquals("the constraint mode is reported back",
                            constraints[i], field.getConstraints() & TextField.CONSTRAINT_MASK);
                    Assert.assertEquals("the text is reported back", values[i],
                            field.getString());
                    field.setConstraints(TextField.ANY);
                    Assert.assertEquals("setConstraints(ANY)",
                            TextField.ANY, field.getConstraints() & TextField.CONSTRAINT_MASK);
                }
            }
        });

        add(new TestCase("constraints_reject_invalid_values") {
            public void run() {
                final TextField field = new TextField("label", "", 10, TextField.ANY);
                Assert.expectException("setConstraints(6)", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                field.setConstraints(6);
                            }
                        });
                Assert.expectException("setConstraints(-1)", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                field.setConstraints(-1);
                            }
                        });
                Assert.expectException("a TextField with an invalid constraint",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                new TextField("label", "", 10, 6);
                            }
                        });
            }
        });

        add(new TestCase("numeric_constraint_rejects_letters") {
            public void run() {
                final TextField field = new TextField("number", "123", 10, TextField.NUMERIC);
                Assert.expectException("setString(\"abc\") on a NUMERIC field",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                field.setString("abc");
                            }
                        });
                Assert.expectException("a NUMERIC field with an illegal initial text",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                new TextField("number", "abc", 10, TextField.NUMERIC);
                            }
                        });
                field.setString("-42");
                Assert.assertEquals("a NUMERIC field accepts an optional minus sign", "-42",
                        field.getString());
            }
        }.severity(TestCase.SHOULD).describe("the implementation must restrict a NUMERIC "
                + "field to an optional minus sign and digits"));

        add(new TestCase("password_and_flag_constraints") {
            public void run() {
                TextField field = new TextField("secret", "hidden", 20,
                        TextField.PASSWORD | TextField.INITIAL_CAPS_WORD);
                Assert.assertEquals("the mode part of PASSWORD|INITIAL_CAPS_WORD is ANY",
                        TextField.ANY, field.getConstraints() & TextField.CONSTRAINT_MASK);
                Assert.assertEquals("the flags are reported back", TextField.PASSWORD,
                        field.getConstraints() & TextField.PASSWORD);
                Assert.assertEquals("the text itself is readable through the API", "hidden",
                        field.getString());
                Assert.assertEquals("CONSTRAINT_MASK must mask out the flags",
                        TextField.ANY, TextField.PASSWORD & TextField.CONSTRAINT_MASK);
            }
        });

        add(new TestCase("insert_and_delete") {
            public void run() {
                TextField field = new TextField("label", "abcdef", 20, TextField.ANY);
                field.insert("XY", 3);
                Assert.assertEquals("insert(String, position)", "abcXYdef", field.getString());
                field.delete(3, 2);
                Assert.assertEquals("delete(offset, length)", "abcdef", field.getString());
                char[] data = {'1', '2', '3', '4'};
                field.insert(data, 1, 2, 0);
                Assert.assertEquals("insert(char[], offset, length, position)", "23abcdef",
                        field.getString());
                field.setChars(data, 0, 4);
                Assert.assertEquals("setChars(char[], offset, length)", "1234",
                        field.getString());
                char[] out = new char[4];
                Assert.assertEquals("getChars() returns the number of characters", 4,
                        field.getChars(out));
                Assert.assertEquals("the characters are copied into the array",
                        "1234", new String(out));
            }
        });

        add(new TestCase("insert_and_delete_argument_checks") {
            public void run() {
                final TextField field = new TextField("label", "abc", 10, TextField.ANY);
                Assert.expectException("insert(null, 0)", NullPointerException.class,
                        new Assert.Code() {
                            public void run() {
                                field.insert(null, 0);
                            }
                        });
                Assert.expectException("delete with a bad range",
                        StringIndexOutOfBoundsException.class, new Assert.Code() {
                            public void run() {
                                field.delete(2, 5);
                            }
                        });
                Assert.expectException("a position outside the contents",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                field.insert("x", 99);
                            }
                        });
            }
        }.severity(TestCase.SHOULD));

        add(new TestCase("caret_position") {
            public void run() {
                TextField field = new TextField("label", "abc", 10, TextField.ANY);
                int caret = field.getCaretPosition();
                Assert.info("getCaretPosition()", caret);
                Assert.assertInRange("the caret must be inside the contents", caret, 0, 3);
            }
        });

        add(new TestCase("initial_input_mode") {
            public void run() {
                final TextField field = new TextField("label", "", 10, TextField.ANY);
                Assert.expectNoException("setInitialInputMode(\"IS_LATIN\")",
                        new Assert.Code() {
                            public void run() {
                                field.setInitialInputMode("IS_LATIN");
                            }
                        });
                Assert.expectNoException("setInitialInputMode(null)", new Assert.Code() {
                    public void run() {
                        field.setInitialInputMode(null);
                    }
                });
            }
        });

        add(new TestCase("label_can_be_changed") {
            public void run() {
                TextField field = new TextField("before", "text", 10, TextField.ANY);
                field.setLabel("after");
                Assert.assertEquals("setLabel/getLabel", "after", field.getLabel());
                field.setLabel(null);
                Assert.assertNull("setLabel(null)", field.getLabel());
            }
        });
    }
}
