/**
 * LcduiTest - MIDP 2.0 LCDUI conformance test MIDlet.
 *
 * javax.microedition.lcdui.game.TiledLayer
 */
package lcduitest.tests;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.TiledLayer;

import lcduitest.Assert;
import lcduitest.TestCase;
import lcduitest.TestSuite;
import lcduitest.Ui;

public class TiledLayerSuite extends TestSuite {

    public TiledLayerSuite() {
        super("TiledLayer", "TiledLayer", "the grid, the static tile set, animated tiles and "
                + "the pixels that are painted");

        add(new TestCase("constructor") {
            public void run() {
                TiledLayer layer = new TiledLayer(4, 3, sheet(), 8, 8);
                Assert.assertEquals("getColumns()", 4, layer.getColumns());
                Assert.assertEquals("getRows()", 3, layer.getRows());
                Assert.assertEquals("getCellWidth()", 8, layer.getCellWidth());
                Assert.assertEquals("getCellHeight()", 8, layer.getCellHeight());
                Assert.assertEquals("the layer is 4 tiles wide", 32, layer.getWidth());
                Assert.assertEquals("the layer is 3 tiles high", 24, layer.getHeight());
                for (int row = 0; row < 3; row++) {
                    for (int col = 0; col < 4; col++) {
                        Assert.assertEquals("a new cell is empty (" + col + "," + row + ")",
                                0, layer.getCell(col, row));
                    }
                }
            }
        });

        add(new TestCase("constructor_argument_checks") {
            public void run() {
                final Image image = sheet();
                Assert.expectException("new TiledLayer(4, 4, null, 8, 8)",
                        NullPointerException.class, new Assert.Code() {
                            public void run() {
                                new TiledLayer(4, 4, null, 8, 8);
                            }
                        });
                Assert.expectException("zero columns", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                new TiledLayer(0, 4, image, 8, 8);
                            }
                        });
                Assert.expectException("zero rows", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                new TiledLayer(4, 0, image, 8, 8);
                            }
                        });
                Assert.expectException("a tile width of zero", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                new TiledLayer(4, 4, image, 0, 8);
                            }
                        });
                Assert.expectException("a tile height of zero", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                new TiledLayer(4, 4, image, 8, 0);
                            }
                        });
                Assert.expectException("a tile width that does not divide the image width",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                new TiledLayer(4, 4, image, 5, 8);
                            }
                        });
            }
        });

        add(new TestCase("cells") {
            public void run() {
                TiledLayer layer = new TiledLayer(4, 3, sheet(), 8, 8);
                layer.setCell(1, 2, 3);
                Assert.assertEquals("setCell()/getCell()", 3, layer.getCell(1, 2));
                layer.setCell(1, 2, 0);
                Assert.assertEquals("index 0 empties the cell", 0, layer.getCell(1, 2));
                final TiledLayer probe = layer;
                Assert.expectException("getCell(-1, 0)", IndexOutOfBoundsException.class,
                        new Assert.Code() {
                            public void run() {
                                probe.getCell(-1, 0);
                            }
                        });
                Assert.expectException("getCell(0, 3)", IndexOutOfBoundsException.class,
                        new Assert.Code() {
                            public void run() {
                                probe.getCell(0, 3);
                            }
                        });
                Assert.expectException("setCell(4, 0, 1)", IndexOutOfBoundsException.class,
                        new Assert.Code() {
                            public void run() {
                                probe.setCell(4, 0, 1);
                            }
                        });
                Assert.expectException("a tile index that does not exist",
                        IndexOutOfBoundsException.class, new Assert.Code() {
                            public void run() {
                                probe.setCell(0, 0, 99);
                            }
                        });
            }
        });

        add(new TestCase("fillCells") {
            public void run() {
                TiledLayer layer = new TiledLayer(4, 4, sheet(), 8, 8);
                layer.fillCells(1, 1, 2, 2, 2);
                Assert.assertEquals("filled cell", 2, layer.getCell(1, 1));
                Assert.assertEquals("filled cell", 2, layer.getCell(2, 2));
                Assert.assertEquals("a cell outside the filled region", 0,
                        layer.getCell(0, 0));
                final TiledLayer probe = layer;
                Assert.expectException("a region that extends beyond the grid",
                        IndexOutOfBoundsException.class, new Assert.Code() {
                            public void run() {
                                probe.fillCells(3, 3, 2, 2, 1);
                            }
                        });
                Assert.expectException("a negative column count",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                probe.fillCells(0, 0, -1, 1, 1);
                            }
                        });
                Assert.expectException("a negative row count", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                probe.fillCells(0, 0, 1, -1, 1);
                            }
                        });
            }
        });

        add(new TestCase("animated_tiles") {
            public void run() {
                TiledLayer layer = new TiledLayer(4, 4, sheet(), 8, 8);
                int first = layer.createAnimatedTile(1);
                int second = layer.createAnimatedTile(2);
                Assert.assertEquals("the first animated tile has index -1", -1, first);
                Assert.assertEquals("the second animated tile has index -2", -2, second);
                Assert.assertEquals("getAnimatedTile(-1)", 1, layer.getAnimatedTile(-1));
                layer.setAnimatedTile(-1, 3);
                Assert.assertEquals("setAnimatedTile(-1, 3)", 3, layer.getAnimatedTile(-1));
                layer.setCell(0, 0, -1);
                Assert.assertEquals("a cell can hold an animated tile", -1,
                        layer.getCell(0, 0));
                final TiledLayer probe = layer;
                Assert.expectException("createAnimatedTile with an invalid static index",
                        IndexOutOfBoundsException.class, new Assert.Code() {
                            public void run() {
                                probe.createAnimatedTile(99);
                            }
                        });
                Assert.expectException("setAnimatedTile with an unknown animated tile",
                        IndexOutOfBoundsException.class, new Assert.Code() {
                            public void run() {
                                probe.setAnimatedTile(-5, 1);
                            }
                        });
                Assert.expectException("setAnimatedTile with an unknown static index",
                        IndexOutOfBoundsException.class, new Assert.Code() {
                            public void run() {
                                probe.setAnimatedTile(-1, 99);
                            }
                        });
            }
        });

        add(new TestCase("setStaticTileSet") {
            public void run() {
                TiledLayer layer = new TiledLayer(4, 4, sheet(), 8, 8);
                layer.setCell(0, 0, 4);
                int animated = layer.createAnimatedTile(1);
                layer.setCell(1, 1, animated);
                layer.setStaticTileSet(sheet(), 8, 8);
                Assert.assertEquals("the cell contents survive a tile set with the same "
                        + "number of tiles", 4, layer.getCell(0, 0));
                Assert.assertEquals("the animated tile survives", animated,
                        layer.getCell(1, 1));

                layer.setStaticTileSet(Ui.solidImage(8, 8, 0x123456), 8, 8);
                Assert.assertEquals("a smaller tile set clears the grid", 0,
                        layer.getCell(0, 0));
                Assert.assertEquals("a smaller tile set removes the animated tiles too",
                        0, layer.getCell(1, 1));

                final TiledLayer probe = layer;
                Assert.expectException("setStaticTileSet(null, 8, 8)",
                        NullPointerException.class, new Assert.Code() {
                            public void run() {
                                probe.setStaticTileSet(null, 8, 8);
                            }
                        });
                Assert.expectException("a tile width of zero",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                probe.setStaticTileSet(sheet(), 0, 8);
                            }
                        });
                Assert.expectException("a tile width that does not divide the image",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                probe.setStaticTileSet(sheet(), 6, 8);
                            }
                        });
            }
        }.severity(TestCase.SHOULD));

        add(new TestCase("paint_draws_the_tiles") {
            public void run() {
                TiledLayer layer = new TiledLayer(2, 2, sheet(), 8, 8);
                layer.setCell(0, 0, 1);
                layer.setCell(1, 0, 2);
                layer.setCell(0, 1, 3);
                layer.setCell(1, 1, 4);
                Image target = Ui.solidImage(16, 16, 0xFFFFFF);
                Graphics g = target.getGraphics();
                layer.paint(g);
                Assert.assertEquals("tile 1 is the first tile of the sheet", 0xFF0000,
                        Ui.pixel(target, 2, 2));
                Assert.assertEquals("tile 2", 0x00FF00, Ui.pixel(target, 10, 2));
                Assert.assertEquals("tile 3", 0x0000FF, Ui.pixel(target, 2, 10));
                Assert.assertEquals("tile 4", 0xFFFF00, Ui.pixel(target, 10, 10));
                Assert.assertEquals("the top-left pixel belongs to tile 1", 0xFF0000,
                        Ui.pixel(target, 0, 0));
            }
        }.severity(TestCase.SHOULD));

        add(new TestCase("an_animated_tile_is_painted_with_its_static_tile") {
            public void run() {
                TiledLayer layer = new TiledLayer(2, 2, sheet(), 8, 8);
                int animated = layer.createAnimatedTile(2);
                layer.setCell(0, 0, animated);
                Image target = Ui.solidImage(16, 16, 0xFFFFFF);
                layer.paint(target.getGraphics());
                Assert.assertEquals("the animated tile starts out as tile 2", 0x00FF00,
                        Ui.pixel(target, 2, 2));
                layer.setAnimatedTile(animated, 4);
                layer.paint(target.getGraphics());
                Assert.assertEquals("after setAnimatedTile() the new tile is painted",
                        0xFFFF00, Ui.pixel(target, 2, 2));
            }
        }.severity(TestCase.SHOULD));

        add(new TestCase("a_tiled_layer_is_a_layer") {
            public void run() {
                TiledLayer layer = new TiledLayer(4, 4, sheet(), 8, 8);
                Assert.assertEquals("a new layer is at (0,0)", 0, layer.getX());
                layer.setPosition(5, 6);
                Assert.assertEquals("setPosition() x", 5, layer.getX());
                Assert.assertEquals("setPosition() y", 6, layer.getY());
                layer.move(-1, 1);
                Assert.assertEquals("move()", 4, layer.getX());
                Assert.assertEquals("move()", 7, layer.getY());
                Assert.assertTrue("a new layer is visible", layer.isVisible());
                layer.setVisible(false);
                Assert.assertFalse("setVisible(false)", layer.isVisible());
                Assert.assertEquals("the width of the layer is grid width", 32,
                        layer.getWidth());
                Assert.assertEquals("the height of the layer is grid height", 32,
                        layer.getHeight());
            }
        });
    }

    /** A 32x16 sheet with eight 8x8 tiles of different colours. */
    static Image sheet() {
        Image image = Image.createImage(32, 16);
        Graphics g = image.getGraphics();
        int[] colours = {0xFF0000, 0x00FF00, 0x0000FF, 0xFFFF00, 0xFF00FF, 0x00FFFF,
            0x808080, 0x000000};
        for (int i = 0; i < colours.length; i++) {
            g.setColor(colours[i]);
            g.fillRect((i % 4) * 8, (i / 4) * 8, 8, 8);
        }
        return image;
    }
}
