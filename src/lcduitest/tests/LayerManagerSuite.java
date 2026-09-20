/**
 * LcduiTest - MIDP 2.0 LCDUI conformance test MIDlet.
 *
 * javax.microedition.lcdui.game.LayerManager and the abstract
 * javax.microedition.lcdui.game.Layer (exercised through Sprite).
 */
package lcduitest.tests;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Layer;
import javax.microedition.lcdui.game.LayerManager;
import javax.microedition.lcdui.game.Sprite;

import lcduitest.Assert;
import lcduitest.TestCase;
import lcduitest.TestSuite;
import lcduitest.Ui;

public class LayerManagerSuite extends TestSuite {

    public LayerManagerSuite() {
        super("LayerManager", "LayerManager", "adding, inserting, removing and querying "
                + "layers, the view window and the paint order");

        add(new TestCase("an_empty_manager") {
            public void run() {
                LayerManager manager = new LayerManager();
                Assert.assertEquals("getSize() of a new manager", 0, manager.getSize());
                Assert.expectException("getLayerAt(0) of an empty manager",
                        IndexOutOfBoundsException.class, new Assert.Code() {
                            public void run() {
                                new LayerManager().getLayerAt(0);
                            }
                        });
                Assert.expectException("getLayerAt(-1)", IndexOutOfBoundsException.class,
                        new Assert.Code() {
                            public void run() {
                                new LayerManager().getLayerAt(-1);
                            }
                        });
            }
        });

        add(new TestCase("append_and_getLayerAt") {
            public void run() {
                final LayerManager manager = new LayerManager();
                Layer first = newSprite(0xFF0000);
                Layer second = newSprite(0x00FF00);
                manager.append(first);
                manager.append(second);
                Assert.assertEquals("getSize()", 2, manager.getSize());
                Assert.assertSame("getLayerAt(0) is the first appended layer", first,
                        manager.getLayerAt(0));
                Assert.assertSame("getLayerAt(1)", second, manager.getLayerAt(1));
                Assert.expectException("getLayerAt(2)", IndexOutOfBoundsException.class,
                        new Assert.Code() {
                            public void run() {
                                manager.getLayerAt(2);
                            }
                        });
                manager.append(first);
                Assert.assertEquals("appending a layer twice must not add it twice",
                        2, manager.getSize());
                Assert.assertSame("the re-appended layer has the highest index now "
                        + "(it is furthest away from the user)", first,
                        manager.getLayerAt(1));
                Assert.assertSame("the other layer moved to index 0", second,
                        manager.getLayerAt(0));
            }
        });

        add(new TestCase("insert") {
            public void run() {
                final LayerManager manager = new LayerManager();
                final Layer first = newSprite(0xFF0000);
                Layer second = newSprite(0x00FF00);
                Layer third = newSprite(0x0000FF);
                manager.append(first);
                manager.append(third);
                manager.insert(second, 1);
                Assert.assertEquals("getSize() after insert()", 3, manager.getSize());
                Assert.assertSame("index 0", first, manager.getLayerAt(0));
                Assert.assertSame("index 1", second, manager.getLayerAt(1));
                Assert.assertSame("index 2", third, manager.getLayerAt(2));
                Assert.expectException("insert(layer, -1)", IndexOutOfBoundsException.class,
                        new Assert.Code() {
                            public void run() {
                                manager.insert(newSprite(0), -1);
                            }
                        });
                Assert.expectException("insert(layer, size + 1)",
                        IndexOutOfBoundsException.class, new Assert.Code() {
                            public void run() {
                                manager.insert(newSprite(0), 4);
                            }
                        });
            }
        });

        add(new TestCase("remove") {
            public void run() {
                final LayerManager manager = new LayerManager();
                final Layer layer = newSprite(0xFF0000);
                Layer other = newSprite(0x00FF00);
                manager.append(layer);
                manager.append(other);
                manager.remove(layer);
                Assert.assertEquals("getSize() after remove()", 1, manager.getSize());
                Assert.assertSame("the remaining layer", other, manager.getLayerAt(0));
                manager.remove(layer);
                Assert.assertEquals("removing a layer that is not in the manager does "
                        + "nothing", 1, manager.getSize());
                Assert.expectException("remove(null)", NullPointerException.class,
                        new Assert.Code() {
                            public void run() {
                                manager.remove(null);
                            }
                        });
            }
        });

        add(new TestCase("null_layers_are_rejected") {
            public void run() {
                final LayerManager manager = new LayerManager();
                Assert.expectException("append(null)", NullPointerException.class,
                        new Assert.Code() {
                            public void run() {
                                manager.append(null);
                            }
                        });
                Assert.expectException("insert(null, 0)", NullPointerException.class,
                        new Assert.Code() {
                            public void run() {
                                manager.insert(null, 0);
                            }
                        });
            }
        });

        add(new TestCase("view_window") {
            public void run() {
                final LayerManager manager = new LayerManager();
                Assert.expectNoException("setViewWindow(0, 0, 10, 10)", new Assert.Code() {
                    public void run() {
                        new LayerManager().setViewWindow(0, 0, 10, 10);
                    }
                });
                Assert.expectException("setViewWindow with a negative width",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                new LayerManager().setViewWindow(0, 0, -1, 10);
                            }
                        });
                Assert.expectException("setViewWindow with a negative height",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                new LayerManager().setViewWindow(0, 0, 10, -1);
                            }
                        });
                Assert.expectNoException("setViewWindow(0, 0, 0, 0)",
                        new Assert.Code() {
                            public void run() {
                                manager.setViewWindow(0, 0, 0, 0);
                            }
                        });
            }
        });

        add(new TestCase("paint_draws_the_layers") {
            public void run() {
                LayerManager manager = new LayerManager();
                Sprite top = newSprite(0xFF0000);
                Sprite bottom = newSprite(0x0000FF);
                top.setPosition(4, 4);
                bottom.setPosition(4, 4);
                manager.append(top);
                manager.append(bottom);
                Image target = Ui.solidImage(20, 20, 0xFFFFFF);
                Graphics g = target.getGraphics();
                manager.paint(g, 0, 0);
                Assert.assertEquals("the layer with index 0 is painted on top",
                        0xFF0000, Ui.pixel(target, 6, 6));
                Assert.assertEquals("the background is untouched",
                        0xFFFFFF, Ui.pixel(target, 0, 0));

                manager.remove(top);
                g = target.getGraphics();
                g.setColor(0xFFFFFF);
                g.fillRect(0, 0, 20, 20);
                manager.paint(g, 0, 0);
                Assert.assertEquals("after removing the top layer the other one is visible",
                        0x0000FF, Ui.pixel(target, 6, 6));
            }
        }.severity(TestCase.SHOULD).describe("the paint order of the layers is one of the "
                + "few things about the game API that can be checked pixel by pixel"));

        add(new TestCase("paint_argument_checks") {
            public void run() {
                final LayerManager manager = new LayerManager();
                manager.append(newSprite(0xFF0000));
                final Image target = Ui.solidImage(8, 8, 0xFFFFFF);
                Assert.expectException("paint(null, 0, 0)", NullPointerException.class,
                        new Assert.Code() {
                            public void run() {
                                manager.paint(null, 0, 0);
                            }
                        });
                Assert.expectNoException("paint() moves the layers with the offset",
                        new Assert.Code() {
                            public void run() {
                                LayerManager other = new LayerManager();
                                Sprite sprite = newSprite(0x00FF00);
                                sprite.setPosition(0, 0);
                                other.append(sprite);
                                other.paint(target.getGraphics(), 10, 10);
                            }
                        });
                Assert.assertEquals("the offset moved the layer to (10,10)", 0x00FF00,
                        Ui.pixel(target, 12, 12));
            }
        }.severity(TestCase.SHOULD));

        add(new TestCase("a_layer_is_only_in_one_manager") {
            public void run() {
                LayerManager first = new LayerManager();
                LayerManager second = new LayerManager();
                Sprite sprite = newSprite(0xFF0000);
                first.append(sprite);
                second.append(sprite);
                Assert.assertEquals("the second manager holds the layer", 1,
                        second.getSize());
                Assert.info("the size of the first manager after the layer was moved",
                        first.getSize());
            }
        }.severity(TestCase.INFO).describe("the specification does not say what happens to "
                + "the other manager, so only the first manager's size is reported"));

        add(new TestCase("layer_position_and_visibility") {
            public void run() {
                Layer layer = newSprite(0xFF0000);
                Assert.assertEquals("a new layer is at x = 0", 0, layer.getX());
                Assert.assertEquals("a new layer is at y = 0", 0, layer.getY());
                Assert.assertTrue("a new layer is visible", layer.isVisible());
                layer.setPosition(7, 9);
                Assert.assertEquals("setPosition() x", 7, layer.getX());
                Assert.assertEquals("setPosition() y", 9, layer.getY());
                layer.move(3, -4);
                Assert.assertEquals("move() adds to the position", 10, layer.getX());
                Assert.assertEquals("move() adds to the position", 5, layer.getY());
                layer.setVisible(false);
                Assert.assertFalse("setVisible(false)", layer.isVisible());
                layer.setVisible(true);
                Assert.assertTrue("setVisible(true)", layer.isVisible());
                Assert.assertTrue("getWidth() must be positive", layer.getWidth() > 0);
                Assert.assertTrue("getHeight() must be positive", layer.getHeight() > 0);
            }
        });
    }

    /** A hidden sprite of the given colour, 8x8 pixels. */
    static Sprite newSprite(int rgb) {
        Sprite sprite = new Sprite(Ui.solidImage(8, 8, rgb));
        sprite.setVisible(true);
        return sprite;
    }
}
