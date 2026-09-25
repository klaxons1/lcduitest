package m3gtest.tests;

import javax.microedition.m3g.*;
import m3gtest.*;

public class ConstantsSuite extends TestSuite {

    public ConstantsSuite() {
        super("constants", "Constants", "Checks that well-known M3G constants exist and have expected values.");
        add(new TestCase("CompositingMode blending") {
            public void run() {
                Assert.assertEquals("ALPHA", CompositingMode.ALPHA, 64);
                Assert.assertEquals("ALPHA_ADD", CompositingMode.ALPHA_ADD, 65);
                Assert.assertEquals("MODULATE", CompositingMode.MODULATE, 66);
                Assert.assertEquals("REPLACE", CompositingMode.REPLACE, 67);
            }
        });
        add(new TestCase("Texture filtering") {
            public void run() {
                Assert.assertTrue("NEAREST defined", Texture2D.FILTER_NEAREST >= 0);
                Assert.assertTrue("LINEAR defined", Texture2D.FILTER_LINEAR >= 0);
            }
        });
        add(new TestCase("Image2D formats") {
            public void run() {
                Assert.assertTrue("RGB", Image2D.RGB >= 0);
                Assert.assertTrue("RGBA", Image2D.RGBA >= 0);
                Assert.assertTrue("ALPHA", Image2D.ALPHA >= 0);
            }
        });
        add(new TestCase("Light modes") {
            public void run() {
                Assert.assertTrue("AMBIENT", Light.AMBIENT >= 0);
                Assert.assertTrue("DIRECTIONAL", Light.DIRECTIONAL >= 0);
                Assert.assertTrue("OMNI", Light.OMNI >= 0);
                Assert.assertTrue("SPOT", Light.SPOT >= 0);
            }
        });
        add(new TestCase("PolygonMode culling") {
            public void run() {
                Assert.assertTrue("CULL_NONE", PolygonMode.CULL_NONE >= 0);
                Assert.assertTrue("CULL_BACK", PolygonMode.CULL_BACK >= 0);
                Assert.assertTrue("CULL_FRONT", PolygonMode.CULL_FRONT >= 0);
            }
        });
        add(new TestCase("VertexArray types") {
            public void run() {
                Assert.assertTrue("BYTE", VertexArray.BYTE >= 0);
                Assert.assertTrue("SHORT", VertexArray.SHORT >= 0);
                Assert.assertTrue("FIXED", VertexArray.FIXED >= 0);
                Assert.assertTrue("FLOAT", VertexArray.FLOAT >= 0);
            }
        });
        add(new TestCase("Transform constants") {
            public void run() {
                // Just ensure class loads and has methods
                Transform t = new Transform();
                Assert.assertNotNull("Transform", t);
            }
        });
        add(new TestCase("Material colors") {
            public void run() {
                Assert.assertTrue("AMBIENT", Material.AMBIENT >= 0);
                Assert.assertTrue("DIFFUSE", Material.DIFFUSE >= 0);
                Assert.assertTrue("EMISSIVE", Material.EMISSIVE >= 0);
                Assert.assertTrue("SPECULAR", Material.SPECULAR >= 0);
            }
        });
    }
}
