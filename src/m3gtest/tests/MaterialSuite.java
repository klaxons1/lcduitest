/*
 * M3G Tester - Material
 */
package m3gtest.tests;

import javax.microedition.m3g.*;

import m3gtest.*;

public class MaterialSuite extends TestSuite {

    public MaterialSuite() {
        super("Material", "Material", "colors, shininess, vertexColorTracking");

        add(new TestCase("default_colors") {
            public void run() {
                Material mat = new Material();
                // Default colors per spec? Let's observe
                Assert.info("default AMBIENT", Ui.hex(mat.getColor(Material.AMBIENT)));
                Assert.info("default DIFFUSE", Ui.hex(mat.getColor(Material.DIFFUSE)));
                Assert.info("default SPECULAR", Ui.hex(mat.getColor(Material.SPECULAR)));
                Assert.info("default EMISSIVE", Ui.hex(mat.getColor(Material.EMISSIVE)));
                Assert.info("shininess", mat.getShininess());
            }
        });

        add(new TestCase("set_get_colors") {
            public void run() {
                Material mat = new Material();
                mat.setColor(Material.AMBIENT, 0xFF0000);
                Assert.assertEquals("ambient red", 0xFF0000, mat.getColor(Material.AMBIENT) & 0xFFFFFF);

                mat.setColor(Material.DIFFUSE, 0x00FF00);
                Assert.assertEquals("diffuse green", 0x00FF00, mat.getColor(Material.DIFFUSE) & 0xFFFFFF);

                mat.setColor(Material.SPECULAR, 0x0000FF);
                Assert.assertEquals("specular blue", 0x0000FF, mat.getColor(Material.SPECULAR) & 0xFFFFFF);

                mat.setColor(Material.EMISSIVE, 0xFFFFFF);
                Assert.assertEquals("emissive white", 0xFFFFFF, mat.getColor(Material.EMISSIVE) & 0xFFFFFF);
            }
        });

        add(new TestCase("set_color_invalid_target") {
            public void run() {
                Assert.expectException("invalid target", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new Material().setColor(999, 0xFF0000);
                    }
                });
                Assert.expectException("get invalid target", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new Material().getColor(999);
                    }
                });
            }
        });

        add(new TestCase("shininess") {
            public void run() {
                Material mat = new Material();
                mat.setShininess(10.0f);
                Assert.assertEquals("shininess 10", 10.0f, mat.getShininess(), 0.001f);
                mat.setShininess(0.0f);
                Assert.assertEquals("shininess 0", 0.0f, mat.getShininess(), 0.001f);
                mat.setShininess(128.0f);
                Assert.assertEquals("shininess 128", 128.0f, mat.getShininess(), 0.001f);

                Assert.expectException("negative shininess", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new Material().setShininess(-1.0f);
                    }
                });
            }
        });

        add(new TestCase("vertex_color_tracking") {
            public void run() {
                Material mat = new Material();
                mat.setVertexColorTrackingEnable(true);
                Assert.assertTrue("tracking true", mat.isVertexColorTrackingEnabled());
                mat.setVertexColorTrackingEnable(false);
                Assert.assertFalse("tracking false", mat.isVertexColorTrackingEnabled());
            }
        });
    }
}
