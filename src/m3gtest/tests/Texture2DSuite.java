/*
 * M3G Tester - Texture2D
 */
package m3gtest.tests;

import javax.microedition.m3g.*;

import m3gtest.*;

public class Texture2DSuite extends TestSuite {

    public Texture2DSuite() {
        super("Texture2D", "Texture2D", "constructor, image, filtering, wrapping, blending, transform");

        add(new TestCase("constructor_and_image") {
            public void run() {
                Image2D img = new Image2D(Image2D.RGB, 32, 32);
                Texture2D tex = new Texture2D(img);
                Assert.assertNotNull("texture not null", tex);
                Assert.assertSame("getImage returns same", img, tex.getImage());

                Texture2D tex2 = new Texture2D((Image2D) null);
                Assert.assertNull("null image allowed", tex2.getImage());

                // Set image later
                Image2D img2 = new Image2D(Image2D.RGBA, 16, 16);
                tex2.setImage(img2);
                Assert.assertSame("setImage", img2, tex2.getImage());
                tex2.setImage(null);
                Assert.assertNull("setImage null", tex2.getImage());
            }
        });

        add(new TestCase("filtering") {
            public void run() {
                Texture2D tex = new Texture2D(new Image2D(Image2D.RGB, 16, 16));
                tex.setFiltering(Texture2D.FILTER_NEAREST, Texture2D.FILTER_NEAREST);
                Assert.assertEquals("level filter nearest", Texture2D.FILTER_NEAREST, tex.getLevelFilter());
                Assert.assertEquals("image filter nearest", Texture2D.FILTER_NEAREST, tex.getImageFilter());

                tex.setFiltering(Texture2D.FILTER_LINEAR, Texture2D.FILTER_LINEAR);
                Assert.assertEquals("level linear", Texture2D.FILTER_LINEAR, tex.getLevelFilter());
                Assert.assertEquals("image linear", Texture2D.FILTER_LINEAR, tex.getImageFilter());

                tex.setFiltering(Texture2D.FILTER_BASE_LEVEL, Texture2D.FILTER_NEAREST);
                Assert.assertEquals("base level", Texture2D.FILTER_BASE_LEVEL, tex.getLevelFilter());

                Assert.expectException("invalid level filter", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new Texture2D(new Image2D(Image2D.RGB, 16, 16)).setFiltering(999, Texture2D.FILTER_NEAREST);
                    }
                });
                Assert.expectException("invalid image filter", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new Texture2D(new Image2D(Image2D.RGB, 16, 16)).setFiltering(Texture2D.FILTER_NEAREST, 999);
                    }
                });
            }
        });

        add(new TestCase("wrapping") {
            public void run() {
                Texture2D tex = new Texture2D(new Image2D(Image2D.RGB, 16, 16));
                tex.setWrapping(Texture2D.WRAP_CLAMP, Texture2D.WRAP_REPEAT);
                Assert.assertEquals("wrap S clamp", Texture2D.WRAP_CLAMP, tex.getWrappingS());
                Assert.assertEquals("wrap T repeat", Texture2D.WRAP_REPEAT, tex.getWrappingT());

                tex.setWrapping(Texture2D.WRAP_REPEAT, Texture2D.WRAP_CLAMP);
                Assert.assertEquals("wrap S repeat", Texture2D.WRAP_REPEAT, tex.getWrappingS());

                Assert.expectException("invalid wrap S", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new Texture2D(new Image2D(Image2D.RGB, 16, 16)).setWrapping(999, Texture2D.WRAP_CLAMP);
                    }
                });
            }
        });

        add(new TestCase("blending") {
            public void run() {
                Texture2D tex = new Texture2D(new Image2D(Image2D.RGB, 16, 16));
                tex.setBlending(Texture2D.FUNC_MODULATE);
                Assert.assertEquals("blend modulate", Texture2D.FUNC_MODULATE, tex.getBlending());

                tex.setBlending(Texture2D.FUNC_REPLACE);
                Assert.assertEquals("blend replace", Texture2D.FUNC_REPLACE, tex.getBlending());

                tex.setBlending(Texture2D.FUNC_BLEND);
                Assert.assertEquals("blend blend", Texture2D.FUNC_BLEND, tex.getBlending());

                tex.setBlending(Texture2D.FUNC_DECAL);
                Assert.assertEquals("decal", Texture2D.FUNC_DECAL, tex.getBlending());

                tex.setBlending(Texture2D.FUNC_ADD);
                Assert.assertEquals("add", Texture2D.FUNC_ADD, tex.getBlending());

                Assert.expectException("invalid blend", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new Texture2D(new Image2D(Image2D.RGB, 16, 16)).setBlending(999);
                    }
                });
            }
        });

        add(new TestCase("blend_color") {
            public void run() {
                Texture2D tex = new Texture2D(new Image2D(Image2D.RGB, 16, 16));
                tex.setBlendColor(0xFF00FF00);
                Assert.assertEquals("blend color", 0xFF00FF00, tex.getBlendColor());
                tex.setBlendColor(0x00000000);
                Assert.assertEquals("transparent", 0x00000000, tex.getBlendColor());
            }
        });

        add(new TestCase("transformable_methods") {
            public void run() {
                Texture2D tex = new Texture2D(new Image2D(Image2D.RGB, 16, 16));
                Transform t = new Transform();
                t.postTranslate(1, 2, 0);
                tex.setTransform(t);
                Transform out = new Transform();
                tex.getTransform(out);
                float[] m = new float[16];
                out.get(m);
                Assert.assertEquals("transform tx", 1f, m[3], 0.0001f);

                tex.setOrientation(90, 0, 0, 1);
                float[] angAxis = new float[4];
                tex.getOrientation(angAxis);
                Assert.assertEquals("orientation 90", 90f, angAxis[0], 0.001f);

                tex.setScale(2.0f, 2.0f, 1.0f);
                float[] scale = new float[3];
                tex.getScale(scale);
                Assert.assertEquals("scaleX 2", 2.0f, scale[0], 0.0001f);
                Assert.assertEquals("scaleY 2", 2.0f, scale[1], 0.0001f);

                tex.setTranslation(0.5f, 0.5f, 0);
                float[] trans = new float[3];
                tex.getTranslation(trans);
                Assert.assertEquals("transX 0.5", 0.5f, trans[0], 0.0001f);
                Assert.assertEquals("transY 0.5", 0.5f, trans[1], 0.0001f);

                // composite
                Transform comp = new Transform();
                tex.getCompositeTransform(comp);
                Assert.assertNotNull("composite not null", comp);
            }
        });
    }
}
