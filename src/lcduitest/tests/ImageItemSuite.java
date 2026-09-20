/**
 * LcduiTest - MIDP 2.0 LCDUI conformance test MIDlet.
 *
 * javax.microedition.lcdui.ImageItem
 */
package lcduitest.tests;

import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.ImageItem;
import javax.microedition.lcdui.Item;

import lcduitest.Assert;
import lcduitest.TestCase;
import lcduitest.TestSuite;
import lcduitest.Ui;

public class ImageItemSuite extends TestSuite {

    public ImageItemSuite() {
        super("ImageItem", "Image items", "images, alternative text, the layout directives "
                + "and the appearance modes");

        add(new TestCase("image_and_alt_text_round_trip") {
            public void run() {
                Image image = Ui.solidImage(10, 8, 0x3366CC);
                ImageItem item = new ImageItem("label", image, ImageItem.LAYOUT_DEFAULT, "alt");
                Assert.assertEquals("label", "label", item.getLabel());
                Assert.assertSame("getImage()", image, item.getImage());
                Assert.assertEquals("getAltText()", "alt", item.getAltText());
                item.setAltText("other alt");
                Assert.assertEquals("setAltText()", "other alt", item.getAltText());
                Image replacement = Ui.solidImage(4, 4, 0xFF0000);
                item.setImage(replacement);
                Assert.assertSame("setImage()", replacement, item.getImage());
                item.setImage(null);
                Assert.assertNull("setImage(null) removes the image", item.getImage());
            }
        });

        add(new TestCase("image_can_be_mutable") {
            public void run() {
                Image image = Image.createImage(6, 6);
                image.getGraphics().setColor(0x00FF00);
                image.getGraphics().fillRect(0, 0, 6, 6);
                ImageItem item = new ImageItem("label", image, ImageItem.LAYOUT_DEFAULT, null);
                Assert.assertTrue("the ImageItem must accept a mutable image",
                        item.getImage().isMutable());
                Assert.assertEquals("the pixels of the mutable image are used", 0x00FF00,
                        Ui.pixel(item.getImage(), 2, 2));
            }
        });

        add(new TestCase("layout_directives") {
            public void run() {
                final ImageItem item = new ImageItem("label", null, ImageItem.LAYOUT_DEFAULT,
                        null);
                Assert.assertEquals("the initial layout", ImageItem.LAYOUT_DEFAULT,
                        item.getLayout());
                int layout = ImageItem.LAYOUT_CENTER | ImageItem.LAYOUT_NEWLINE_BEFORE
                        | ImageItem.LAYOUT_NEWLINE_AFTER;
                item.setLayout(layout);
                Assert.assertEquals("setLayout()/getLayout()", layout, item.getLayout());
                Assert.expectException("an invalid layout value",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                item.setLayout(0x8000);
                            }
                        });
            }
        }.severity(TestCase.SHOULD));

        add(new TestCase("constructor_layout_check") {
            public void run() {
                Assert.expectException("an invalid layout in the constructor",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                new ImageItem("label", null, 0x8000, "alt");
                            }
                        });
            }
        });

        add(new TestCase("appearance_modes") {
            public void run() {
                ImageItem plain = new ImageItem("l", null, ImageItem.LAYOUT_DEFAULT, null);
                ImageItem hyper = new ImageItem("l", null, ImageItem.LAYOUT_DEFAULT, null,
                        Item.HYPERLINK);
                ImageItem button = new ImageItem("l", null, ImageItem.LAYOUT_DEFAULT, null,
                        Item.BUTTON);
                Assert.assertEquals("the default appearance is PLAIN", Item.PLAIN,
                        plain.getAppearanceMode());
                Assert.assertEquals("HYPERLINK", Item.HYPERLINK, hyper.getAppearanceMode());
                Assert.assertEquals("BUTTON", Item.BUTTON, button.getAppearanceMode());
                Assert.expectException("an invalid appearance mode",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                new ImageItem("l", null, ImageItem.LAYOUT_DEFAULT, null, 9);
                            }
                        });
            }
        });

        add(new TestCase("preferred_size_follows_the_image") {
            public void run() {
                ImageItem small = new ImageItem(null, Ui.solidImage(4, 4, 0), 0, null);
                ImageItem large = new ImageItem(null, Ui.solidImage(40, 30, 0), 0, null);
                Assert.assertTrue("a larger image must not have a smaller preferred width",
                        large.getPreferredWidth() >= small.getPreferredWidth());
                Assert.assertTrue("a larger image must not have a smaller preferred height",
                        large.getPreferredHeight() >= small.getPreferredHeight());
                Assert.info("preferred size of the 40x30 image",
                        large.getPreferredWidth() + "x" + large.getPreferredHeight());
            }
        }.severity(TestCase.SHOULD));

        add(new TestCase("setPreferredSize_after_creation") {
            public void run() {
                ImageItem item = new ImageItem(null, null, ImageItem.LAYOUT_DEFAULT, null);
                item.setPreferredSize(64, 48);
                Assert.assertEquals("a locked width", 64, item.getPreferredWidth());
                Assert.assertEquals("a locked height", 48, item.getPreferredHeight());
                Assert.assertTrue("a null image must be allowed",
                        item.getImage() == null && item.getAltText() == null);
            }
        });
    }
}
