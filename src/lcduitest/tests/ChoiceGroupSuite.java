/**
 * LcduiTest - MIDP 2.0 LCDUI conformance test MIDlet.
 *
 * javax.microedition.lcdui.ChoiceGroup - the EXCLUSIVE, MULTIPLE and POPUP
 * choice group of a Form, including the selection rules of the Choice
 * interface.
 */
package lcduitest.tests;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;

import lcduitest.Assert;
import lcduitest.TestCase;
import lcduitest.TestSuite;
import lcduitest.Ui;

public class ChoiceGroupSuite extends TestSuite {

    public ChoiceGroupSuite() {
        super("ChoiceGroup", "Choice groups", "EXCLUSIVE / MULTIPLE / POPUP, element "
                + "handling, selection state, fonts and the fit policy");

        add(new TestCase("exclusive_group_selects_the_first_element") {
            public void run() {
                ChoiceGroup group = new ChoiceGroup("size", Choice.EXCLUSIVE,
                        new String[]{"small", "medium", "large"}, null);
                Assert.assertEquals("size()", 3, group.size());
                Assert.assertEquals("an EXCLUSIVE group starts with the first element selected",
                        0, group.getSelectedIndex());
                Assert.assertTrue("isSelected(0)", group.isSelected(0));
                Assert.assertFalse("isSelected(1)", group.isSelected(1));
            }
        });

        add(new TestCase("exclusive_group_selection_rules") {
            public void run() {
                ChoiceGroup group = new ChoiceGroup("size", Choice.EXCLUSIVE,
                        new String[]{"a", "b", "c"}, null);
                group.setSelectedIndex(2, true);
                Assert.assertEquals("the newly selected element", 2, group.getSelectedIndex());
                Assert.assertFalse("selecting an element deselects the previous one",
                        group.isSelected(0));
                group.setSelectedIndex(1, false);
                Assert.assertEquals("deselecting is ignored in an EXCLUSIVE group",
                        2, group.getSelectedIndex());
            }
        });

        add(new TestCase("multiple_group_selection_rules") {
            public void run() {
                ChoiceGroup group = new ChoiceGroup("colours", Choice.MULTIPLE,
                        new String[]{"red", "green", "blue"}, null);
                Assert.assertEquals("a MULTIPLE group has no single selection", -1,
                        group.getSelectedIndex());
                group.setSelectedIndex(0, true);
                group.setSelectedIndex(2, true);
                Assert.assertTrue("isSelected(0)", group.isSelected(0));
                Assert.assertFalse("isSelected(1)", group.isSelected(1));
                Assert.assertTrue("isSelected(2)", group.isSelected(2));
                group.setSelectedIndex(0, false);
                Assert.assertFalse("an element can be deselected again", group.isSelected(0));
                boolean[] flags = new boolean[3];
                group.getSelectedFlags(flags);
                Assert.assertFalse("getSelectedFlags: element 0", flags[0]);
                Assert.assertFalse("getSelectedFlags: element 1", flags[1]);
                Assert.assertTrue("getSelectedFlags: element 2", flags[2]);
            }
        });

        add(new TestCase("popup_group_behaves_like_exclusive") {
            public void run() {
                ChoiceGroup group = new ChoiceGroup("size", Choice.POPUP,
                        new String[]{"small", "large"}, null);
                Assert.assertEquals("a POPUP group has exactly one selected element", 0,
                        group.getSelectedIndex());
                group.setSelectedIndex(1, true);
                Assert.assertEquals("setSelectedIndex(1, true)", 1, group.getSelectedIndex());
                Assert.assertFalse("the previous selection was cleared", group.isSelected(0));
            }
        });

        add(new TestCase("setSelectedFlags") {
            public void run() {
                ChoiceGroup exclusive = new ChoiceGroup("e", Choice.EXCLUSIVE,
                        new String[]{"a", "b", "c"}, null);
                exclusive.setSelectedFlags(new boolean[]{false, false, false});
                Assert.assertEquals("an all-false array selects the first element of an "
                        + "EXCLUSIVE group", 0, exclusive.getSelectedIndex());
                exclusive.setSelectedFlags(new boolean[]{true, true, false});
                Assert.assertEquals("with two true values the first one wins", 0,
                        exclusive.getSelectedIndex());

                ChoiceGroup multiple = new ChoiceGroup("m", Choice.MULTIPLE,
                        new String[]{"a", "b", "c"}, null);
                multiple.setSelectedFlags(new boolean[]{true, false, true});
                Assert.assertTrue("MULTIPLE: element 0", multiple.isSelected(0));
                Assert.assertTrue("MULTIPLE: element 2", multiple.isSelected(2));
                Assert.assertFalse("MULTIPLE: element 1", multiple.isSelected(1));
            }
        });

        add(new TestCase("selection_argument_checks") {
            public void run() {
                final ChoiceGroup group = new ChoiceGroup("g", Choice.MULTIPLE,
                        new String[]{"a", "b"}, null);
                Assert.expectException("getSelectedFlags with a short array",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                group.getSelectedFlags(new boolean[1]);
                            }
                        });
                Assert.expectException("getSelectedFlags(null)", NullPointerException.class,
                        new Assert.Code() {
                            public void run() {
                                group.getSelectedFlags(null);
                            }
                        });
                Assert.expectException("setSelectedFlags with a short array",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                group.setSelectedFlags(new boolean[1]);
                            }
                        });
                Assert.expectException("setSelectedFlags(null)", NullPointerException.class,
                        new Assert.Code() {
                            public void run() {
                                group.setSelectedFlags(null);
                            }
                        });
                Assert.expectException("isSelected(2) on a group with two elements",
                        IndexOutOfBoundsException.class, new Assert.Code() {
                            public void run() {
                                group.isSelected(2);
                            }
                        });
                Assert.expectException("setSelectedIndex(5, true)",
                        IndexOutOfBoundsException.class, new Assert.Code() {
                            public void run() {
                                group.setSelectedIndex(5, true);
                            }
                        });
            }
        });

        add(new TestCase("element_handling") {
            public void run() {
                ChoiceGroup group = new ChoiceGroup("g", Choice.MULTIPLE, null, null);
                Image image = Ui.solidImage(8, 8, 0xFF0000);
                Assert.assertEquals("append returns the index", 0, group.append("first", image));
                Assert.assertEquals("append returns the index", 1, group.append("second", null));
                Assert.assertEquals("getString(0)", "first", group.getString(0));
                Assert.assertSame("getImage(0)", image, group.getImage(0));
                Assert.assertNull("getImage(1)", group.getImage(1));
                group.set(0, "changed", null);
                Assert.assertEquals("set() replaces the text", "changed", group.getString(0));
                group.insert(1, "inserted", null);
                Assert.assertEquals("insert puts the element at the index", "inserted",
                        group.getString(1));
                Assert.assertEquals("the elements behind it move", "second", group.getString(2));
                Assert.assertEquals("size after insert", 3, group.size());
                group.delete(1);
                Assert.assertEquals("delete removes one element", 2, group.size());
                Assert.assertEquals("the elements are still in order", "second",
                        group.getString(1));
                group.deleteAll();
                Assert.assertEquals("deleteAll()", 0, group.size());
                Assert.assertEquals("an empty group has no selection", -1,
                        group.getSelectedIndex());
                group.deleteAll();
                Assert.assertEquals("deleteAll() on an empty group is harmless", 0, group.size());
            }
        });

        add(new TestCase("element_argument_checks") {
            public void run() {
                final ChoiceGroup group = new ChoiceGroup("g", Choice.EXCLUSIVE,
                        new String[]{"only"}, null);
                Assert.expectException("append(null, null)", NullPointerException.class,
                        new Assert.Code() {
                            public void run() {
                                group.append(null, null);
                            }
                        });
                Assert.expectException("getString(1)", IndexOutOfBoundsException.class,
                        new Assert.Code() {
                            public void run() {
                                group.getString(1);
                            }
                        });
                Assert.expectException("delete(1)", IndexOutOfBoundsException.class,
                        new Assert.Code() {
                            public void run() {
                                group.delete(1);
                            }
                        });
                Assert.expectException("set(1, \"x\", null)", IndexOutOfBoundsException.class,
                        new Assert.Code() {
                            public void run() {
                                group.set(1, "x", null);
                            }
                        });
                Assert.expectException("insert(2, \"x\", null)", IndexOutOfBoundsException.class,
                        new Assert.Code() {
                            public void run() {
                                group.insert(2, "x", null);
                            }
                        });
            }
        });

        add(new TestCase("constructor_and_type_checks") {
            public void run() {
                Assert.expectException("IMPLICIT is not allowed for a ChoiceGroup",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                new ChoiceGroup("g", Choice.IMPLICIT);
                            }
                        });
                Assert.expectException("an invalid choice type",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                new ChoiceGroup("g", 99);
                            }
                        });
                Assert.expectException("ChoiceGroup(String, int) with a null label is legal, "
                        + "but a null string array is not", NullPointerException.class,
                        new Assert.Code() {
                            public void run() {
                                new ChoiceGroup("g", Choice.EXCLUSIVE, null, null);
                            }
                        });
                final String[] strings = {"a", "b"};
                final Image[] tooFewImages = {Ui.solidImage(4, 4, 0)};
                Assert.expectException("an image array of a different length",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() throws Exception {
                                new ChoiceGroup("g", Choice.EXCLUSIVE, strings, tooFewImages);
                            }
                        });
            }
        });

        add(new TestCase("fit_policy") {
            public void run() {
                final ChoiceGroup group = new ChoiceGroup("g", Choice.EXCLUSIVE,
                        new String[]{"a long element that may or may not wrap"}, null);
                int initial = group.getFitPolicy();
                Assert.info("initial fit policy", initial);
                Assert.assertTrue("the initial fit policy must be one of the three constants",
                        initial == Choice.TEXT_WRAP_DEFAULT || initial == Choice.TEXT_WRAP_ON
                        || initial == Choice.TEXT_WRAP_OFF);
                group.setFitPolicy(Choice.TEXT_WRAP_ON);
                Assert.assertEquals("setFitPolicy(TEXT_WRAP_ON)", Choice.TEXT_WRAP_ON,
                        group.getFitPolicy());
                group.setFitPolicy(Choice.TEXT_WRAP_OFF);
                Assert.assertEquals("setFitPolicy(TEXT_WRAP_OFF)", Choice.TEXT_WRAP_OFF,
                        group.getFitPolicy());
                group.setFitPolicy(Choice.TEXT_WRAP_DEFAULT);
                Assert.assertEquals("setFitPolicy(TEXT_WRAP_DEFAULT)", Choice.TEXT_WRAP_DEFAULT,
                        group.getFitPolicy());
                Assert.expectException("setFitPolicy(7)", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                group.setFitPolicy(7);
                            }
                        });
            }
        });

        add(new TestCase("per_element_font") {
            public void run() {
                final ChoiceGroup group = new ChoiceGroup("g", Choice.MULTIPLE,
                        new String[]{"a", "b"}, null);
                final Font font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_LARGE);
                group.setFont(0, font);
                Assert.assertSame("getFont(0) returns the font that was set", font,
                        group.getFont(0));
                group.setFont(1, null);
                Assert.assertNotNull("setFont(i, null) must fall back to the default font",
                        group.getFont(1));
                Assert.expectException("setFont with an invalid index",
                        IndexOutOfBoundsException.class, new Assert.Code() {
                            public void run() {
                                group.setFont(2, font);
                            }
                        });
            }
        }.severity(TestCase.SHOULD));

        add(new TestCase("choice_group_inside_a_form") {
            public void run() {
                Form form = new Form("choices");
                ChoiceGroup group = new ChoiceGroup("exclusive", Choice.EXCLUSIVE,
                        new String[]{"one", "two"}, null);
                form.append(group);
                Ui.show(form);
                Assert.info("preferred size", group.getPreferredWidth() + "x"
                        + group.getPreferredHeight());
                Assert.assertTrue("a ChoiceGroup must have a positive preferred height",
                        group.getPreferredHeight() > 0);
            }
        }.severity(TestCase.SHOULD));
    }
}
