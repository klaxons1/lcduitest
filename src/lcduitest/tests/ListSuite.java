/**
 * LcduiTest - MIDP 2.0 LCDUI conformance test MIDlet.
 *
 * javax.microedition.lcdui.List - the screen that shows a Choice.
 */
package lcduitest.tests;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.Ticker;

import lcduitest.Assert;
import lcduitest.TestCase;
import lcduitest.TestSuite;
import lcduitest.Ui;

public class ListSuite extends TestSuite {

    public ListSuite() {
        super("List", "Lists", "the IMPLICIT, EXCLUSIVE and MULTIPLE list types, element "
                + "handling, selection, fonts, the fit policy and the select command");

        add(new TestCase("implicit_list") {
            public void run() {
                List list = new List("menu", Choice.IMPLICIT,
                        new String[]{"one", "two", "three"}, null);
                Assert.assertEquals("size()", 3, list.size());
                Assert.assertEquals("getString(0)", "one", list.getString(0));
                Assert.assertTrue("an IMPLICIT list always has a focused element, so "
                        + "getSelectedIndex() must not be -1", list.getSelectedIndex() >= 0);
                int index = list.getSelectedIndex();
                Assert.assertTrue("the focused element must be selected", list.isSelected(index));
            }
        }.severity(TestCase.SHOULD));

        add(new TestCase("exclusive_list") {
            public void run() {
                List list = new List("menu", Choice.EXCLUSIVE);
                Assert.assertEquals("a new list is empty", 0, list.size());
                Assert.assertEquals("an empty list has no selection", -1, list.getSelectedIndex());
                list.append("first", null);
                Assert.assertEquals("the first element of an EXCLUSIVE list is selected", 0,
                        list.getSelectedIndex());
                list.append("second", null);
                list.setSelectedIndex(1, true);
                Assert.assertEquals("setSelectedIndex(1, true)", 1, list.getSelectedIndex());
                Assert.assertFalse("the previous selection is cleared", list.isSelected(0));
                list.setSelectedIndex(0, false);
                Assert.assertEquals("deselecting is ignored in an EXCLUSIVE list", 1,
                        list.getSelectedIndex());
            }
        });

        add(new TestCase("multiple_list") {
            public void run() {
                List list = new List("menu", Choice.MULTIPLE);
                list.append("a", null);
                list.append("b", null);
                list.append("c", null);
                Assert.assertEquals("a MULTIPLE list has no single selection", -1,
                        list.getSelectedIndex());
                list.setSelectedIndex(0, true);
                list.setSelectedIndex(2, true);
                boolean[] flags = new boolean[3];
                list.getSelectedFlags(flags);
                Assert.assertTrue("element 0", flags[0]);
                Assert.assertFalse("element 1", flags[1]);
                Assert.assertTrue("element 2", flags[2]);
                list.setSelectedFlags(new boolean[]{false, true, false});
                Assert.assertTrue("setSelectedFlags", list.isSelected(1));
                Assert.assertFalse("setSelectedFlags clears the others", list.isSelected(0));
            }
        });

        add(new TestCase("element_handling") {
            public void run() {
                List list = new List("menu", Choice.IMPLICIT, null, null);
                Image image = Ui.solidImage(8, 8, 0x00FF00);
                Assert.assertEquals("append returns the index", 0, list.append("first", image));
                Assert.assertEquals("append returns the index", 1, list.append("second", null));
                Assert.assertSame("getImage(0)", image, list.getImage(0));
                Assert.assertNull("getImage(1)", list.getImage(1));
                list.set(0, "changed", null);
                Assert.assertEquals("set() replaces the text", "changed", list.getString(0));
                list.insert(1, "inserted", null);
                Assert.assertEquals("insert() puts the element at the index", "inserted",
                        list.getString(1));
                Assert.assertEquals("the following elements move", "second", list.getString(2));
                list.delete(1);
                Assert.assertEquals("delete()", 2, list.size());
                list.deleteAll();
                Assert.assertEquals("deleteAll()", 0, list.size());
            }
        });

        add(new TestCase("argument_checks") {
            public void run() {
                List list = new List("menu", Choice.IMPLICIT);
                list.append("only", null);
                final List finalList = list;
                Assert.expectException("append(null, null)", NullPointerException.class,
                        new Assert.Code() {
                            public void run() {
                                finalList.append(null, null);
                            }
                        });
                Assert.expectException("getString(1)", IndexOutOfBoundsException.class,
                        new Assert.Code() {
                            public void run() {
                                finalList.getString(1);
                            }
                        });
                Assert.expectException("insert(5, ...)", IndexOutOfBoundsException.class,
                        new Assert.Code() {
                            public void run() {
                                finalList.insert(5, "x", null);
                            }
                        });
                Assert.expectException("delete(1)", IndexOutOfBoundsException.class,
                        new Assert.Code() {
                            public void run() {
                                finalList.delete(1);
                            }
                        });
                Assert.expectException("getSelectedFlags with a short array",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                finalList.getSelectedFlags(new boolean[1]);
                            }
                        });
                Assert.expectException("getSelectedFlags(null)", NullPointerException.class,
                        new Assert.Code() {
                            public void run() {
                                finalList.getSelectedFlags(null);
                            }
                        });
                Assert.expectException("setSelectedFlags(null)", NullPointerException.class,
                        new Assert.Code() {
                            public void run() {
                                finalList.setSelectedFlags(null);
                            }
                        });
                Assert.expectException("isSelected(1)", IndexOutOfBoundsException.class,
                        new Assert.Code() {
                            public void run() {
                                finalList.isSelected(1);
                            }
                        });
                Assert.expectException("setSelectedIndex(1, true)",
                        IndexOutOfBoundsException.class, new Assert.Code() {
                            public void run() {
                                finalList.setSelectedIndex(1, true);
                            }
                        });
            }
        });

        add(new TestCase("constructor_checks") {
            public void run() {
                Assert.expectException("List with an undefined type",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                new List("menu", 99);
                            }
                        });
                Assert.expectException("List with the POPUP type (only IMPLICIT, EXCLUSIVE "
                        + "and MULTIPLE are allowed)", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                new List("menu", Choice.POPUP);
                            }
                        });
                Assert.expectException("List with a null string array",
                        NullPointerException.class, new Assert.Code() {
                            public void run() {
                                new List("menu", Choice.IMPLICIT, null, null);
                            }
                        });
                final String[] strings = {"a", "b"};
                final Image[] images = {Ui.solidImage(4, 4, 0)};
                Assert.expectException("images and strings of different length",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() throws Exception {
                                new List("menu", Choice.IMPLICIT, strings, images);
                            }
                        });
            }
        });

        add(new TestCase("select_command") {
            public void run() {
                List list = new List("menu", Choice.IMPLICIT, new String[]{"a"}, null);
                final Command select = new Command("Open", Command.ITEM, 1);
                list.setSelectCommand(select);
                list.setSelectCommand(null);
                Assert.assertNotNull("SELECT_COMMAND is a Command object", List.SELECT_COMMAND);
                Assert.assertEquals("SELECT_COMMAND has an empty label", "",
                        List.SELECT_COMMAND.getLabel());
                Assert.assertNotSame("an application command must be a different object than "
                        + "SELECT_COMMAND", List.SELECT_COMMAND, select);

                // the spec: setSelectCommand has no effect when the type is not IMPLICIT
                final List exclusive = new List("m", Choice.EXCLUSIVE, new String[]{"a"}, null);
                Assert.expectNoException("setSelectCommand on a non-IMPLICIT list",
                        new Assert.Code() {
                            public void run() {
                                exclusive.setSelectCommand(select);
                            }
                        });
            }
        }.severity(TestCase.SHOULD).describe("MIDP 2.0 defines the select command only for "
                + "IMPLICIT lists; for other list types the call has no effect"));

        add(new TestCase("box_type_screens") {
            public void run() {
                List list = new List("menu", Choice.IMPLICIT);
                list.setTitle("other title");
                Assert.assertEquals("setTitle()", "other title", list.getTitle());
                Ticker ticker = new Ticker("scrolling");
                list.setTicker(ticker);
                Assert.assertSame("setTicker()", ticker, list.getTicker());
                Font font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM);
                list.setFont(0, font);
                Assert.assertNotNull("the list is still usable", list);
                Ui.show(list);
                Assert.assertTrue("a shown List is visible", list.isShown());
                Assert.assertTrue("a List must report a positive width", list.getWidth() > 0);
                Assert.assertTrue("a List must report a positive height", list.getHeight() > 0);
            }
        }.severity(TestCase.SHOULD));

        add(new TestCase("fit_policy_and_fonts") {
            public void run() {
                final List list = new List("menu", Choice.IMPLICIT,
                        new String[]{"one long element that may be wrapped"}, null);
                Assert.assertEquals("the default fit policy", Choice.TEXT_WRAP_DEFAULT,
                        list.getFitPolicy());
                list.setFitPolicy(Choice.TEXT_WRAP_ON);
                Assert.assertEquals("setFitPolicy(TEXT_WRAP_ON)", Choice.TEXT_WRAP_ON,
                        list.getFitPolicy());
                list.setFitPolicy(Choice.TEXT_WRAP_OFF);
                Assert.assertEquals("setFitPolicy(TEXT_WRAP_OFF)", Choice.TEXT_WRAP_OFF,
                        list.getFitPolicy());
                Assert.expectException("setFitPolicy(9)", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                list.setFitPolicy(9);
                            }
                        });
                Font font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
                list.setFont(0, font);
                Assert.assertSame("getFont(0)", font, list.getFont(0));
                Assert.expectException("getFont(1)", IndexOutOfBoundsException.class,
                        new Assert.Code() {
                            public void run() {
                                list.getFont(1);
                            }
                        });
            }
        }.severity(TestCase.SHOULD));

        add(new TestCase("commands_on_a_list") {
            public void run() {
                final List list = new List("menu", Choice.IMPLICIT, new String[]{"a"}, null);
                Command command = new Command("Do", Command.SCREEN, 1);
                list.addCommand(command);
                list.removeCommand(command);
                list.removeCommand(command);
                list.setCommandListener(null);
                Assert.assertNotNull("the list is still usable", list);
                Assert.expectException("addCommand(null)", NullPointerException.class,
                        new Assert.Code() {
                            public void run() {
                                list.addCommand(null);
                            }
                        });
            }
        });
    }
}
