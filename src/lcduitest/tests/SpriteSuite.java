/**
 * LcduiTest - MIDP 2.0 LCDUI conformance test MIDlet.
 *
 * javax.microedition.lcdui.game.Sprite
 */
package lcduitest.tests;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;
import javax.microedition.lcdui.game.TiledLayer;

import lcduitest.Assert;
import lcduitest.TestCase;
import lcduitest.TestSuite;
import lcduitest.Ui;

public class SpriteSuite extends TestSuite {

    public SpriteSuite() {
        super("Sprite", "Sprite", "the three constructors, frames and frame sequences, "
                + "transforms, the reference pixel and the collision tests");

        add(new TestCase("constructors") {
            public void run() {
                Image whole = sheet();
                Sprite single = new Sprite(whole);
                Assert.assertEquals("Sprite(Image) uses the whole image as one frame",
                        1, single.getRawFrameCount());
                Assert.assertEquals("getWidth() of the single frame sprite",
                        whole.getWidth(), single.getWidth());
                Assert.assertEquals("getHeight() of the single frame sprite",
                        whole.getHeight(), single.getHeight());

                Sprite frames = new Sprite(whole, 8, 8);
                Assert.assertEquals("Sprite(Image, 8, 8) of a 32x16 image has 8 frames",
                        8, frames.getRawFrameCount());
                Assert.assertEquals("the first frame is a 8x8 tile", 8, frames.getWidth());

                Sprite copy = new Sprite(frames);
                Assert.assertEquals("Sprite(Sprite) copies the frame count",
                        8, copy.getRawFrameCount());
                Assert.assertEquals("Sprite(Sprite) copies the frame size",
                        8, copy.getHeight());
            }
        });

        add(new TestCase("constructor_argument_checks") {
            public void run() {
                final Image image = sheet();
                Assert.expectException("new Sprite((Image) null)", NullPointerException.class,
                        new Assert.Code() {
                            public void run() {
                                new Sprite((Image) null);
                            }
                        });
                Assert.expectException("a frame width of 0", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                new Sprite(image, 0, 8);
                            }
                        });
                Assert.expectException("a frame height of 0", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                new Sprite(image, 8, 0);
                            }
                        });
                Assert.expectException("a frame width that does not divide the image width",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                new Sprite(image, 5, 8);
                            }
                        });
                Assert.expectException("a frame height that does not divide the image height",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                new Sprite(image, 8, 5);
                            }
                        });
            }
        });

        add(new TestCase("setImage") {
            public void run() {
                Sprite sprite = new Sprite(sheet(), 8, 8);
                Image other = Ui.solidImage(16, 8, 0x123456);
                sprite.setImage(other, 8, 8);
                Assert.assertEquals("the raw frame count follows the new image",
                        2, sprite.getRawFrameCount());
                Assert.assertEquals("the frame width follows the new image",
                        8, sprite.getWidth());
                final Sprite sprite2 = sprite;
                Assert.expectException("setImage(null, 8, 8)", NullPointerException.class,
                        new Assert.Code() {
                            public void run() {
                                sprite2.setImage(null, 8, 8);
                            }
                        });
                Assert.expectException("setImage with a frame that is too wide",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                sprite2.setImage(Ui.solidImage(16, 8, 0), 5, 8);
                            }
                        });
            }
        });

        add(new TestCase("frames_and_frame_sequence") {
            public void run() {
                Sprite sprite = new Sprite(sheet(), 8, 8);
                Assert.assertEquals("the default sequence length", 8,
                        sprite.getFrameSequenceLength());
                Assert.assertEquals("the default frame index", 0, sprite.getFrame());
                sprite.setFrame(3);
                Assert.assertEquals("setFrame(3)", 3, sprite.getFrame());
                sprite.nextFrame();
                Assert.assertEquals("nextFrame()", 4, sprite.getFrame());
                sprite.prevFrame();
                Assert.assertEquals("prevFrame()", 3, sprite.getFrame());
                sprite.setFrame(7);
                sprite.nextFrame();
                Assert.assertEquals("nextFrame() wraps to the first frame", 0,
                        sprite.getFrame());
                sprite.prevFrame();
                Assert.assertEquals("prevFrame() wraps to the last frame", 7,
                        sprite.getFrame());

                sprite.setFrameSequence(new int[]{2, 4, 6});
                Assert.assertEquals("the sequence length", 3,
                        sprite.getFrameSequenceLength());
                Assert.assertEquals("setFrameSequence resets the current index", 0,
                        sprite.getFrame());
                sprite.nextFrame();
                Assert.assertEquals("the sequence steps through its members", 1,
                        sprite.getFrame());
                sprite.setFrameSequence(null);
                Assert.assertEquals("a null sequence restores the default",
                        8, sprite.getFrameSequenceLength());
            }
        });

        add(new TestCase("frame_argument_checks") {
            public void run() {
                final Sprite sprite = new Sprite(sheet(), 8, 8);
                Assert.expectException("setFrame(-1)", IndexOutOfBoundsException.class,
                        new Assert.Code() {
                            public void run() {
                                sprite.setFrame(-1);
                            }
                        });
                Assert.expectException("setFrame(8) for a sprite with 8 frames",
                        IndexOutOfBoundsException.class, new Assert.Code() {
                            public void run() {
                                sprite.setFrame(8);
                            }
                        });
                Assert.expectException("a frame sequence with an out of range index",
                        ArrayIndexOutOfBoundsException.class, new Assert.Code() {
                            public void run() {
                                sprite.setFrameSequence(new int[]{0, 8});
                            }
                        });
                Assert.expectException("a frame sequence with a negative index",
                        ArrayIndexOutOfBoundsException.class, new Assert.Code() {
                            public void run() {
                                sprite.setFrameSequence(new int[]{-1});
                            }
                        });
            }
        });

        add(new TestCase("transforms") {
            public void run() {
                Sprite sprite = new Sprite(sheet(), 16, 8);
                Assert.assertEquals("the frame size of the transformed sprite", 16,
                        sprite.getWidth());
                Assert.assertEquals("the frame size of the transformed sprite", 8,
                        sprite.getHeight());
                int[] transforms = {Sprite.TRANS_NONE, Sprite.TRANS_ROT90, Sprite.TRANS_ROT180,
                    Sprite.TRANS_ROT270, Sprite.TRANS_MIRROR, Sprite.TRANS_MIRROR_ROT90,
                    Sprite.TRANS_MIRROR_ROT180, Sprite.TRANS_MIRROR_ROT270};
                for (int i = 0; i < transforms.length; i++) {
                    sprite.setTransform(transforms[i]);
                    Assert.info("transform 0x" + Integer.toHexString(transforms[i])
                            + " size", sprite.getWidth() + "x" + sprite.getHeight());
                }
                sprite.setTransform(Sprite.TRANS_ROT90);
                Assert.assertEquals("a 90 degree rotation swaps width and height",
                        8, sprite.getWidth());
                Assert.assertEquals("a 90 degree rotation swaps width and height",
                        16, sprite.getHeight());
                sprite.setTransform(Sprite.TRANS_NONE);
                Assert.assertEquals("TRANS_NONE restores the width", 16, sprite.getWidth());
                final Sprite probe = sprite;
                Assert.expectException("setTransform(8)", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                probe.setTransform(8);
                            }
                        });
                Assert.expectException("setTransform(-1)", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                probe.setTransform(-1);
                            }
                        });
            }
        });

        add(new TestCase("reference_pixel") {
            public void run() {
                Sprite sprite = new Sprite(sheet(), 8, 8);
                Assert.assertEquals("the reference pixel starts at 0,0", 0,
                        sprite.getRefPixelX());
                Assert.assertEquals("the reference pixel starts at 0,0", 0,
                        sprite.getRefPixelY());
                sprite.defineReferencePixel(4, 2);
                Assert.assertEquals("defineReferencePixel x", 4, sprite.getRefPixelX());
                Assert.assertEquals("defineReferencePixel y", 2, sprite.getRefPixelY());
                sprite.setRefPixelPosition(20, 30);
                Assert.assertEquals("setRefPixelPosition x", 20, sprite.getRefPixelX());
                Assert.assertEquals("setRefPixelPosition y", 30, sprite.getRefPixelY());
                Assert.assertEquals("the layer position follows the reference pixel",
                        16, sprite.getX());
                Assert.assertEquals("the layer position follows the reference pixel",
                        28, sprite.getY());
            }
        });

        add(new TestCase("collision_with_another_sprite") {
            public void run() {
                Sprite left = new Sprite(Ui.solidImage(8, 8, 0xFF0000));
                Sprite right = new Sprite(Ui.solidImage(8, 8, 0x00FF00));
                left.setPosition(0, 0);
                right.setPosition(8, 0);
                Assert.assertFalse("sprites that only touch do not collide",
                        left.collidesWith(right, false));
                right.setPosition(7, 0);
                Assert.assertTrue("overlapping sprites collide", left.collidesWith(right,
                        false));
                final Sprite probe = left;
                Assert.expectException("collidesWith(null, false)", NullPointerException.class,
                        new Assert.Code() {
                            public void run() {
                                probe.collidesWith((Sprite) null, false);
                            }
                        });
                right.setVisible(false);
                Assert.assertFalse("an invisible sprite does not collide",
                        left.collidesWith(right, false));
            }
        });

        add(new TestCase("collision_with_an_image_and_a_tiled_layer") {
            public void run() {
                Sprite sprite = new Sprite(Ui.solidImage(8, 8, 0xFF0000));
                sprite.setPosition(0, 0);
                Image image = Ui.solidImage(8, 8, 0x0000FF);
                final Sprite probe = sprite;
                Assert.assertFalse("an image far away does not collide",
                        sprite.collidesWith(image, 20, 20, false));
                Assert.assertTrue("an image at the same place collides",
                        sprite.collidesWith(image, 4, 4, false));
                Assert.expectException("collidesWith((Image) null, 0, 0, false)",
                        NullPointerException.class, new Assert.Code() {
                            public void run() {
                                probe.collidesWith((Image) null, 0, 0, false);
                            }
                        });

                TiledLayer layer = new TiledLayer(4, 4, sheet(), 8, 8);
                final Sprite sprite2 = sprite;
                Assert.assertFalse("an empty tiled layer does not collide",
                        sprite.collidesWith(layer, false));
                layer.setCell(0, 0, 1);
                layer.setPosition(0, 0);
                Assert.info("collision with a tiled layer that has a tile at (0,0)",
                        sprite.collidesWith(layer, false));
                Assert.expectException("collidesWith((TiledLayer) null, false)",
                        NullPointerException.class, new Assert.Code() {
                            public void run() {
                                sprite2.collidesWith((TiledLayer) null, false);
                            }
                        });
            }
        }.severity(TestCase.SHOULD));

        add(new TestCase("collision_rectangle") {
            public void run() {
                final Sprite sprite = new Sprite(sheet(), 8, 8);
                Assert.expectNoException("defineCollisionRectangle(1, 1, 4, 4)",
                        new Assert.Code() {
                            public void run() {
                                sprite.defineCollisionRectangle(1, 1, 4, 4);
                            }
                        });
                Assert.expectException("defineCollisionRectangle with a negative width",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                sprite.defineCollisionRectangle(0, 0, -1, 4);
                            }
                        });
                Assert.expectException("defineCollisionRectangle with a negative height",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                sprite.defineCollisionRectangle(0, 0, 4, -1);
                            }
                        });
            }
        });

        add(new TestCase("paint_draws_the_sprite") {
            public void run() {
                Sprite sprite = new Sprite(Ui.solidImage(8, 8, 0xFF0000));
                sprite.setPosition(4, 4);
                final Image target = Ui.solidImage(20, 20, 0xFFFFFF);
                Graphics g = target.getGraphics();
                sprite.paint(g);
                Assert.assertEquals("the sprite is painted at its position", 0xFF0000,
                        Ui.pixel(target, 6, 6));
                Assert.assertEquals("outside the sprite the target is unchanged",
                        0xFFFFFF, Ui.pixel(target, 1, 1));

                sprite.setVisible(false);
                g = target.getGraphics();
                g.setColor(0xFFFFFF);
                g.fillRect(0, 0, 20, 20);
                sprite.paint(g);
                Assert.assertEquals("an invisible sprite is not painted", 0xFFFFFF,
                        Ui.pixel(target, 6, 6));
            }
        }.severity(TestCase.SHOULD));
    }

    /** A 32x16 sheet with eight 8x8 frames of different colours. */
    static Image sheet() {
        Image image = Image.createImage(32, 16);
        Graphics g = image.getGraphics();
        int[] colours = {0xFF0000, 0x00FF00, 0x0000FF, 0xFFFF00, 0xFF00FF, 0x00FFFF,
            0x808080, 0x000000};
        for (int i = 0; i < colours.length; i++) {
            int col = i % 4;
            int row = i / 4;
            g.setColor(colours[i]);
            g.fillRect(col * 8, row * 8, 8, 8);
        }
        return image;
    }
}
