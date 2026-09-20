/**
 * LcduiTest - MIDP 2.0 LCDUI conformance test MIDlet.
 *
 * javax.microedition.lcdui.Spacer
 */
package lcduitest.tests;

import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.Spacer;

import lcduitest.Assert;
import lcduitest.TestCase;
import lcduitest.TestSuite;

public class SpacerSuite extends TestSuite {

    public SpacerSuite() {
        super("Spacer", "Spacer items", "the minimum size, the preferred size and the label "
                + "behaviour of an item that draws nothing");

        add(new TestCase("minimum_size") {
            public void run() {
                Spacer spacer = new Spacer(20, 30);
                Assert.assertEquals("getMinimumWidth()", 20, spacer.getMinimumWidth());
                Assert.assertEquals("getMinimumHeight()", 30, spacer.getMinimumHeight());
                Assert.assertEquals("a Spacer must report the minimum width as its preferred "
                        + "width", 20, spacer.getPreferredWidth());
                Assert.assertEquals("a Spacer must report the minimum height as its preferred "
                        + "height", 30, spacer.getPreferredHeight());
            }
        });

        add(new TestCase("setMinimumSize") {
            public void run() {
                Spacer spacer = new Spacer(1, 1);
                spacer.setMinimumSize(15, 25);
                Assert.assertEquals("setMinimumSize() width", 15, spacer.getMinimumWidth());
                Assert.assertEquals("setMinimumSize() height", 25, spacer.getMinimumHeight());
                spacer.setMinimumSize(0, 0);
                Assert.assertEquals("a zero minimum width is allowed", 0,
                        spacer.getMinimumWidth());
                Assert.assertEquals("a zero minimum height is allowed", 0,
                        spacer.getMinimumHeight());
            }
        });

        add(new TestCase("negative_minimum_size_is_rejected") {
            public void run() {
                Assert.expectException("Spacer(-1, 10)", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                new Spacer(-1, 10);
                            }
                        });
                Assert.expectException("Spacer(10, -1)", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                new Spacer(10, -1);
                            }
                        });
                final Spacer spacer = new Spacer(1, 1);
                Assert.expectException("setMinimumSize(-1, 10)", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                spacer.setMinimumSize(-1, 10);
                            }
                        });
                Assert.expectException("setMinimumSize(10, -1)", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                spacer.setMinimumSize(10, -1);
                            }
                        });
            }
        });

        add(new TestCase("spacer_is_an_item_like_any_other") {
            public void run() {
                Spacer spacer = new Spacer(5, 5);
                Assert.assertNull("a new Spacer has no label", spacer.getLabel());
                spacer.setLabel("space");
                Assert.assertEquals("the label can be set", "space", spacer.getLabel());
                Assert.assertEquals("the default layout", Item.LAYOUT_DEFAULT, spacer.getLayout());
                spacer.setLayout(Item.LAYOUT_LEFT);
                Assert.assertEquals("the layout can be set", Item.LAYOUT_LEFT,
                        spacer.getLayout());
                spacer.setPreferredSize(9, 9);
                Assert.assertEquals("a Spacer honours a locked preferred width", 9,
                        spacer.getPreferredWidth());
                Assert.assertEquals("a Spacer honours a locked preferred height", 9,
                        spacer.getPreferredHeight());
            }
        }.severity(TestCase.SHOULD));
    }
}
