/**
 * LcduiTest - MIDP 2.0 LCDUI conformance test MIDlet.
 *
 * javax.microedition.lcdui.StringItem
 */
package lcduitest.tests;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.StringItem;

import lcduitest.Assert;
import lcduitest.TestCase;
import lcduitest.TestSuite;

public class StringItemSuite extends TestSuite {

    public StringItemSuite() {
        super("StringItem", "Text items", "text, labels, appearance modes, fonts and the "
                + "preferred size");

        add(new TestCase("text_round_trip") {
            public void run() {
                StringItem item = new StringItem("label", "before");
                Assert.assertEquals("label", "label", item.getLabel());
                Assert.assertEquals("getText()", "before", item.getText());
                item.setText("after");
                Assert.assertEquals("setText()", "after", item.getText());
                item.setText("");
                Assert.assertEquals("an empty text is allowed", "", item.getText());
                item.setText(null);
                Assert.assertNull("setText(null) removes the text", item.getText());
            }
        });

        add(new TestCase("appearance_modes") {
            public void run() {
                StringItem plain = new StringItem("l", "t");
                StringItem link = new StringItem("l", "t", Item.PLAIN);
                StringItem hyper = new StringItem("l", "t", Item.HYPERLINK);
                StringItem button = new StringItem("l", "t", Item.BUTTON);
                Assert.assertEquals("the default appearance is PLAIN", Item.PLAIN,
                        plain.getAppearanceMode());
                Assert.assertEquals("an explicit PLAIN is reported back", Item.PLAIN,
                        link.getAppearanceMode());
                Assert.assertEquals("HYPERLINK", Item.HYPERLINK, hyper.getAppearanceMode());
                Assert.assertEquals("BUTTON", Item.BUTTON, button.getAppearanceMode());
                Assert.expectException("an invalid appearance mode",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                new StringItem("l", "t", 3);
                            }
                        });
            }
        });

        add(new TestCase("font") {
            public void run() {
                StringItem item = new StringItem("label", "text");
                Assert.assertNotNull("getFont() must not be null", item.getFont());
                Font large = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_LARGE);
                item.setFont(large);
                Assert.assertSame("setFont()/getFont()", large, item.getFont());
                item.setFont(null);
                Assert.assertNotNull("setFont(null) must fall back to the default font",
                        item.getFont());
            }
        });

        add(new TestCase("preferred_size_follows_the_text") {
            public void run() {
                StringItem shortText = new StringItem(null, "a");
                StringItem longText = new StringItem(null, "a much longer text that should "
                        + "need more room than a single character");
                Assert.info("preferred width of the short text", shortText.getPreferredWidth());
                Assert.info("preferred width of the long text", longText.getPreferredWidth());
                Assert.assertTrue("a longer text must not have a smaller preferred width",
                        longText.getPreferredWidth() >= shortText.getPreferredWidth());
                Assert.assertTrue("a StringItem must have a positive preferred height",
                        shortText.getPreferredHeight() > 0);
            }
        }.severity(TestCase.SHOULD));

        add(new TestCase("locked_size_overrides_the_computed_size") {
            public void run() {
                StringItem item = new StringItem(null, "text");
                item.setPreferredSize(80, 24);
                Assert.assertEquals("a locked width", 80, item.getPreferredWidth());
                Assert.assertEquals("a locked height", 24, item.getPreferredHeight());
            }
        });

        add(new TestCase("label_can_be_removed_and_set") {
            public void run() {
                StringItem item = new StringItem(null, "text");
                Assert.assertNull("a StringItem can be created without a label",
                        item.getLabel());
                item.setLabel("added later");
                Assert.assertEquals("setLabel()", "added later", item.getLabel());
                Assert.assertEquals("the text is untouched by setLabel()", "text",
                        item.getText());
            }
        });
    }
}
