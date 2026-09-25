package m3gtest.tests;

import javax.microedition.m3g.*;
import m3gtest.*;

public class ConstantsSuite extends TestSuite {

    public ConstantsSuite() {
        super("constants", "Constants", "Checks that well-known M3G constants exist and are distinct.");
        add(new TestCase("CompositingMode blending distinct") {
            public void run() {
                // Values differ between M3G 1.0 and 1.1, so check distinctness not exact numbers
                int alpha = CompositingMode.ALPHA;
                int alphaAdd = CompositingMode.ALPHA_ADD;
                int mod = CompositingMode.MODULATE;
                int modX2 = CompositingMode.MODULATE_X2;
                int replace = CompositingMode.REPLACE;
                Assert.assertTrue("ALPHA >=0", alpha >= 0);
                Assert.assertTrue("distinct ALPHA vs ALPHA_ADD", alpha != alphaAdd);
                Assert.assertTrue("distinct ALPHA vs MODULATE", alpha != mod);
                Assert.assertTrue("distinct MODULATE vs REPLACE", mod != replace);
                Assert.assertTrue("distinct MODULATE_X2 vs REPLACE", modX2 != replace);
            }
        });
        add(new TestCase("Texture filtering distinct") {
            public void run() {
                int nearest = Texture2D.FILTER_NEAREST;
                int linear = Texture2D.FILTER_LINEAR;
                int base = Texture2D.FILTER_BASE_LEVEL;
                Assert.assertTrue("NEAREST defined", nearest >= 0);
                Assert.assertTrue("LINEAR defined", linear >= 0);
                Assert.assertTrue("BASE_LEVEL defined", base >= 0);
                Assert.assertTrue("NEAREST != LINEAR", nearest != linear);
            }
        });
        add(new TestCase("Image2D formats distinct") {
            public void run() {
                int rgb = Image2D.RGB;
                int rgba = Image2D.RGBA;
                int alpha = Image2D.ALPHA;
                Assert.assertTrue("RGB >=0", rgb >= 0);
                Assert.assertTrue("RGBA distinct", rgb != rgba);
                Assert.assertTrue("ALPHA distinct", alpha != rgb && alpha != rgba);
            }
        });
        add(new TestCase("Light modes distinct") {
            public void run() {
                int amb = Light.AMBIENT;
                int dir = Light.DIRECTIONAL;
                int omni = Light.OMNI;
                int spot = Light.SPOT;
                Assert.assertTrue("AMBIENT distinct", amb != dir && amb != omni && amb != spot);
                Assert.assertTrue("DIRECTIONAL distinct", dir != omni && dir != spot);
            }
        });
        add(new TestCase("PolygonMode culling distinct") {
            public void run() {
                int none = PolygonMode.CULL_NONE;
                int back = PolygonMode.CULL_BACK;
                int front = PolygonMode.CULL_FRONT;
                Assert.assertTrue("CULL_NONE distinct", none != back && none != front);
                Assert.assertTrue("CULL_BACK != FRONT", back != front);
            }
        });
        add(new TestCase("VertexArray dimensions") {
            public void run() {
                VertexArray va = new VertexArray(3, 3, 2);
                Assert.assertEquals("vertex count", 3, va.getVertexCount());
                Assert.assertEquals("component count", 3, va.getComponentCount());
                Assert.assertEquals("component type", 2, va.getComponentType());
            }
        });
        add(new TestCase("Transform") {
            public void run() {
                Transform t = new Transform();
                Assert.assertNotNull("Transform", t);
            }
        });
        add(new TestCase("Material colors distinct") {
            public void run() {
                int amb = Material.AMBIENT;
                int diff = Material.DIFFUSE;
                int emiss = Material.EMISSIVE;
                int spec = Material.SPECULAR;
                Assert.assertTrue("AMBIENT != DIFFUSE", amb != diff);
                Assert.assertTrue("DIFFUSE != SPECULAR", diff != spec);
                Assert.assertTrue("EMISSIVE distinct", emiss != amb && emiss != diff);
            }
        });
        add(new TestCase("Background modes distinct") {
            public void run() {
                int border = Background.BORDER;
                int repeat = Background.REPEAT;
                Assert.assertTrue("BORDER != REPEAT", border != repeat);
            }
        });
        add(new TestCase("Camera types distinct") {
            public void run() {
                int gen = Camera.GENERIC;
                int par = Camera.PARALLEL;
                int persp = Camera.PERSPECTIVE;
                Assert.assertTrue("GENERIC distinct", gen != par && gen != persp);
                Assert.assertTrue("PARALLEL != PERSPECTIVE", par != persp);
            }
        });
    }
}
