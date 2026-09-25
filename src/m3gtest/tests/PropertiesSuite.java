/*
 * M3G Tester - Properties and environment
 */
package m3gtest.tests;

import javax.microedition.m3g.*;

import m3gtest.*;

public class PropertiesSuite extends TestSuite {

    public PropertiesSuite() {
        super("Properties", "Graphics3D properties & env", "Graphics3D.getProperties, system properties, version");

        add(new TestCase("m3g_version_property") {
            public void run() {
                String version = System.getProperty(\"microedition.m3g.version\");
                Assert.info(\"microedition.m3g.version\", String.valueOf(version));
                // Should be \"1.0\" or \"1.1\" if M3G supported
                if (version != null) {
                    Assert.assertTrue(\"version contains 1.\", version.indexOf(\"1.\") >= 0);
                }
            }
        }.severity(TestCase.INFO));

        add(new TestCase("graphics3d_properties_table") {
            public void run() {
                Graphics3D g3d = Graphics3D.getInstance();
                java.util.Hashtable props = g3d.getProperties();
                Assert.assertNotNull(\"properties not null\", props);
                // List known properties
                String[] known = new String[]{
                    \"supportM3G1.1\",
                    \"supportM3G1.0\",
                    \"maxLights\",
                    \"maxViewportWidth\",
                    \"maxViewportHeight\",
                    \"maxViewportDimension\",
                    \"maxTextureDimension\",
                    \"maxSpriteCropDimension\",
                    \"maxTransformsPerVertex\",
                    \"numTextureUnits\",
                    \"maxSubmeshes\",
                    \"maxAnimationTracks\"
                };
                for (int i = 0; i < known.length; i++) {
                    Object v = props.get(known[i]);
                    if (v != null) {
                        Assert.info(known[i], String.valueOf(v));
                    }
                }
            }
        }.severity(TestCase.INFO));

        add(new TestCase("max_values_reasonable") {
            public void run() {
                Graphics3D g3d = Graphics3D.getInstance();
                java.util.Hashtable props = g3d.getProperties();
                Object maxTex = props.get(\"maxTextureDimension\");
                if (maxTex instanceof Integer) {
                    int v = ((Integer) maxTex).intValue();
                    Assert.assertGreater(\"maxTextureDimension >0\", v, 0);
                }
                Object maxViewW = props.get(\"maxViewportWidth\");
                if (maxViewW instanceof Integer) {
                    int v = ((Integer) maxViewW).intValue();
                    Assert.assertGreater(\"maxViewportWidth >0\", v, 0);
                }
            }
        }.severity(TestCase.SHOULD));

        add(new TestCase("is_m3g_supported_flag") {
            public void run() {
                boolean supported = Env.isM3GSupported();
                Assert.assertTrue(\"M3G should be supported for this tester\", supported);
            }
        });
    }
}
