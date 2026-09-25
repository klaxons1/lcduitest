package m3gtest.tests;

import javax.microedition.m3g.*;
import m3gtest.*;

public class FrameworkSuite extends TestSuite {

    public FrameworkSuite() {
        super("framework", "Framework / Graphics3D singleton", "Checks Graphics3D instance and basic lifecycle.");

        add(new TestCase("getInstance not null") {
            public void run() {
                Graphics3D g3d = Graphics3D.getInstance();
                Assert.assertNotNull("Graphics3D instance", g3d);
            }
        });

        add(new TestCase("getInstance same") {
            public void run() {
                Graphics3D a = Graphics3D.getInstance();
                Graphics3D b = Graphics3D.getInstance();
                Assert.assertSame("same instance", a, b);
            }
        });

        add(new TestCase("getProperties") {
            public void run() {
                Graphics3D g3d = Graphics3D.getInstance();
                java.util.Hashtable props = g3d.getProperties();
                Assert.assertNotNull("properties", props);
            }
        });

        add(new TestCase("getHints") {
            public void run() {
                Graphics3D g3d = Graphics3D.getInstance();
                int hints = g3d.getHints();
                // hints is bitmask, just check it does not throw
                Assert.assertTrue("hints >=0", hints >= 0);
            }
        });

        add(new TestCase("setHints") {
            public void run() {
                Graphics3D g3d = Graphics3D.getInstance();
                int old = g3d.getHints();
                g3d.setHints(0);
                Assert.assertEquals("hints 0", 0, g3d.getHints());
                g3d.setHints(old);
            }
        });

        add(new TestCase("getViewport") {
            public void run() {
                Graphics3D g3d = Graphics3D.getInstance();
                int[] rect = new int[4];
                // Without bound target, getViewport may throw or return 0, but should not crash VM
                try {
                    g3d.getViewport(rect);
                } catch (Exception e) {
                    // acceptable if no target bound
                }
            }
        });
    }
}
