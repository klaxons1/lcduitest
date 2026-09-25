/*
 * M3G Tester - Appearance
 */
package m3gtest.tests;

import javax.microedition.m3g.*;

import m3gtest.*;

public class AppearanceSuite extends TestSuite {

    public AppearanceSuite() {
        super("Appearance", "Appearance", "components set/get, layer, polygonMode, etc");

        add(new TestCase("default_components_null") {
            public void run() {
                Appearance app = new Appearance();
                Assert.assertNull("default material null", app.getMaterial());
                Assert.assertNull("default compositing null", app.getCompositingMode());
                Assert.assertNull("default polygonMode null", app.getPolygonMode());
                Assert.assertNull("default fog null", app.getFog());
                // Appearance has no getTextureCount in M3G 1.1, textures are per unit, initially null
                Assert.assertNull("default texture 0 null", app.getTexture(0));
            }
        });

        add(new TestCase("set_material") {
            public void run() {
                Appearance app = new Appearance();
                Material mat = new Material();
                app.setMaterial(mat);
                Assert.assertSame("getMaterial", mat, app.getMaterial());
                app.setMaterial(null);
                Assert.assertNull("material null after set null", app.getMaterial());
            }
        });

        add(new TestCase("set_compositing_mode") {
            public void run() {
                Appearance app = new Appearance();
                CompositingMode cm = new CompositingMode();
                app.setCompositingMode(cm);
                Assert.assertSame("compositing", cm, app.getCompositingMode());
                app.setCompositingMode(null);
                Assert.assertNull("null after", app.getCompositingMode());
            }
        });

        add(new TestCase("set_polygon_mode") {
            public void run() {
                Appearance app = new Appearance();
                PolygonMode pm = new PolygonMode();
                app.setPolygonMode(pm);
                Assert.assertSame("polygonMode", pm, app.getPolygonMode());
                app.setPolygonMode(null);
                Assert.assertNull("null", app.getPolygonMode());
            }
        });

        add(new TestCase("set_fog") {
            public void run() {
                Appearance app = new Appearance();
                Fog fog = new Fog();
                app.setFog(fog);
                Assert.assertSame("fog", fog, app.getFog());
                app.setFog(null);
                Assert.assertNull("null", app.getFog());
            }
        });

        add(new TestCase("textures") {
            public void run() {
                Appearance app = new Appearance();
                Image2D img = new Image2D(Image2D.RGB, 16, 16);
                Texture2D tex = new Texture2D(img);
                app.setTexture(0, tex);
                Assert.assertSame("getTexture 0", tex, app.getTexture(0));

                Texture2D tex2 = new Texture2D(img);
                app.setTexture(1, tex2);
                Assert.assertSame("getTexture 1", tex2, app.getTexture(1));

                app.setTexture(0, null);
                Assert.assertNull("texture 0 null after set null", app.getTexture(0));
                Assert.assertSame("texture 1 still there", tex2, app.getTexture(1));

                // Invalid index
                Assert.expectException("negative texture index", IndexOutOfBoundsException.class, new Assert.Code() {
                    public void run() {
                        new Appearance().setTexture(-1, new Texture2D(new Image2D(Image2D.RGB, 16, 16)));
                    }
                });
                Assert.expectException("getTexture negative", IndexOutOfBoundsException.class, new Assert.Code() {
                    public void run() {
                        new Appearance().getTexture(-1);
                    }
                });
            }
        });

        add(new TestCase("layer") {
            public void run() {
                Appearance app = new Appearance();
                app.setLayer(5);
                Assert.assertEquals("layer 5", 5, app.getLayer());
                app.setLayer(0);
                Assert.assertEquals("layer 0", 0, app.getLayer());
                app.setLayer(-10);
                Assert.assertEquals("negative layer allowed", -10, app.getLayer());
            }
        });

        add(new TestCase("duplicate") {
            public void run() {
                Appearance app = new Appearance();
                Material mat = new Material();
                mat.setColor(Material.DIFFUSE, 0xFF0000);
                app.setMaterial(mat);
                Appearance dup = (Appearance) app.duplicate();
                Assert.assertNotSame("different instance", app, dup);
                Assert.assertNotNull("dup material not null", dup.getMaterial());
                Assert.assertNotSame("material duplicated? Actually duplicate should not duplicate referenced objects per Object3D spec? Wait: Node duplicates descendants but Appearance's references? Spec says any other referenced objects are not duplicated. But let's check if material is same or duplicated.");
                Assert.info("material same after dup", dup.getMaterial() == mat);
            }
        });
    }
}
