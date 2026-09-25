/*
 * M3G Tester - World
 */
package m3gtest.tests;

import javax.microedition.m3g.*;

import m3gtest.*;

public class WorldSuite extends TestSuite {

    public WorldSuite() {
        super("World", "World", "active camera, background, addChild, etc");

        add(new TestCase("constructor_and_defaults") {
            public void run() {
                World w = new World();
                Assert.assertNull("default active camera null", w.getActiveCamera());
                Assert.assertNull("default background null", w.getBackground());
                Assert.assertEquals("default child count 0", 0, w.getChildCount());
            }
        });

        add(new TestCase("active_camera") {
            public void run() {
                World w = new World();
                Camera cam = new Camera();
                w.addChild(cam);
                w.setActiveCamera(cam);
                Assert.assertSame("active camera", cam, w.getActiveCamera());

                w.setActiveCamera(null);
                Assert.assertNull("active camera null after set null", w.getActiveCamera());

                Camera cam2 = new Camera();
                // Camera not in world - should still be allowed? Spec says active camera must be in world or null? Let's test
                try {
                    w.setActiveCamera(cam2);
                    Assert.info("setActiveCamera not in world allowed", true);
                } catch (IllegalArgumentException e) {
                    Assert.info("setActiveCamera not in world throws IAE", true);
                }
            }
        });

        add(new TestCase("background") {
            public void run() {
                World w = new World();
                Background bg = new Background();
                bg.setColor(0xFF0000);
                w.setBackground(bg);
                Assert.assertSame("background", bg, w.getBackground());
                w.setBackground(null);
                Assert.assertNull("background null after set null", w.getBackground());
            }
        });

        add(new TestCase("world_is_group") {
            public void run() {
                World w = new World();
                Assert.assertTrue("World instanceof Group", w instanceof Group);
                Group g = new Group();
                w.addChild(g);
                Assert.assertEquals("child count 1", 1, w.getChildCount());
            }
        });
    }
}
