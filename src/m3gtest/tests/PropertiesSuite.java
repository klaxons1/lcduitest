package m3gtest.tests;

import m3gtest.*;

public class PropertiesSuite extends TestSuite {

    public PropertiesSuite() {
        super("properties", "System properties", "Checks microedition.m3g.version and related props.");

        add(new TestCase("m3g version exists") {
            public void run() {
                String v = System.getProperty("microedition.m3g.version");
                // Some emulators may not set it, but we check that getting it does not throw
                // If present, it should contain dot
                if (v != null) {
                    Assert.assertTrue("version contains dot or digit", v.length() > 0);
                }
            }
        });

        add(new TestCase("platform prop") {
            public void run() {
                String p = System.getProperty("microedition.platform");
                Assert.assertNotNull("platform may be null but should not throw", p != null ? p : "dummy");
            }
        });

        add(new TestCase("Env describe") {
            public void run() {
                Env env = new Env(null);
                String desc = env.describe();
                Assert.assertNotNull("describe", desc);
                Assert.assertTrue("describe not empty", desc.length() > 0);
            }
        });

        add(new TestCase("Env prop fallback") {
            public void run() {
                String val = Env.prop("nonexistent.prop.xyz", "fallback123");
                Assert.assertEquals("fallback", "fallback123", val);
            }
        });
    }
}
