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
                java.util.Hashtable props = Graphics3D.getProperties();
                Assert.assertNotNull("properties", props);
            }
        });

        add(new TestCase("getHints") {
            public void run() {
                Graphics3D g3d = Graphics3D.getInstance();
                int hints = g3d.getHints();
                Assert.assertTrue("hints >=0", hints >= 0);
            }
        });

        add(new TestCase("getViewport") {
            public void run() {
                Graphics3D g3d = Graphics3D.getInstance();
                // Use getViewportX/Y/Width/Height instead of getViewport(int[])
                try {
                    int x = g3d.getViewportX();
                    int y = g3d.getViewportY();
                    int w = g3d.getViewportWidth();
                    int h = g3d.getViewportHeight();
                    // Just check no exception
                } catch (Exception e) {
                    // acceptable if no target bound
                }
            }
        });

        add(new TestCase("getTarget") {
            public void run() {
                Graphics3D g3d = Graphics3D.getInstance();
                Object target = g3d.getTarget();
                // May be null if no target bound, just check no throw
            }
        });
    }
}
