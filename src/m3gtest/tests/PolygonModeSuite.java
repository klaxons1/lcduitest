/*
 * M3G Tester - PolygonMode
 */
package m3gtest.tests;

import javax.microedition.m3g.*;

import m3gtest.*;

public class PolygonModeSuite extends TestSuite {

    public PolygonModeSuite() {
        super("PolygonMode", "PolygonMode", "culling, winding, shading, perspective, two-sided lighting, local camera lighting");

        add(new TestCase("default_values") {
            public void run() {
                PolygonMode pm = new PolygonMode();
                Assert.info("culling", pm.getCulling());
                Assert.info("winding", pm.getWinding());
                Assert.info("shading", pm.getShading());
                Assert.info("perspective", pm.isPerspectiveCorrectionEnabled());
            }
        });

        add(new TestCase("culling") {
            public void run() {
                PolygonMode pm = new PolygonMode();
                pm.setCulling(PolygonMode.CULL_NONE);
                Assert.assertEquals("CULL_NONE", PolygonMode.CULL_NONE, pm.getCulling());
                pm.setCulling(PolygonMode.CULL_BACK);
                Assert.assertEquals("CULL_BACK", PolygonMode.CULL_BACK, pm.getCulling());
                pm.setCulling(PolygonMode.CULL_FRONT);
                Assert.assertEquals("CULL_FRONT", PolygonMode.CULL_FRONT, pm.getCulling());

                Assert.expectException("invalid culling", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new PolygonMode().setCulling(999);
                    }
                });
            }
        });

        add(new TestCase("winding") {
            public void run() {
                PolygonMode pm = new PolygonMode();
                pm.setWinding(PolygonMode.WINDING_CW);
                Assert.assertEquals("CW", PolygonMode.WINDING_CW, pm.getWinding());
                pm.setWinding(PolygonMode.WINDING_CCW);
                Assert.assertEquals("CCW", PolygonMode.WINDING_CCW, pm.getWinding());

                Assert.expectException("invalid winding", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new PolygonMode().setWinding(999);
                    }
                });
            }
        });

        add(new TestCase("shading") {
            public void run() {
                PolygonMode pm = new PolygonMode();
                pm.setShading(PolygonMode.SHADE_FLAT);
                Assert.assertEquals("FLAT", PolygonMode.SHADE_FLAT, pm.getShading());
                pm.setShading(PolygonMode.SHADE_SMOOTH);
                Assert.assertEquals("SMOOTH", PolygonMode.SHADE_SMOOTH, pm.getShading());

                Assert.expectException("invalid shading", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new PolygonMode().setShading(999);
                    }
                });
            }
        });

        add(new TestCase("perspective_and_lighting_flags") {
            public void run() {
                PolygonMode pm = new PolygonMode();
                pm.setPerspectiveCorrectionEnable(true);
                Assert.assertTrue("perspective true", pm.isPerspectiveCorrectionEnabled());
                pm.setPerspectiveCorrectionEnable(false);
                Assert.assertFalse("perspective false", pm.isPerspectiveCorrectionEnabled());

                pm.setTwoSidedLightingEnable(true);
                Assert.assertTrue("two sided true", pm.isTwoSidedLightingEnabled());
                pm.setTwoSidedLightingEnable(false);
                Assert.assertFalse("two sided false", pm.isTwoSidedLightingEnabled());

                pm.setLocalCameraLightingEnable(true);
                Assert.assertTrue("local camera true", pm.isLocalCameraLightingEnabled());
                pm.setLocalCameraLightingEnable(false);
                Assert.assertFalse("local camera false", pm.isLocalCameraLightingEnabled());
            }
        });
    }
}
