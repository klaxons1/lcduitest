package m3gtest.tests;

import javax.microedition.m3g.*;
import m3gtest.*;

public class PolygonModeSuite extends TestSuite {

    public PolygonModeSuite() {
        super("polygonmode", "PolygonMode", "Tests PolygonMode.");

        add(new TestCase("setCulling") {
            public void run() {
                PolygonMode pm = new PolygonMode();
                pm.setCulling(PolygonMode.CULL_BACK);
                Assert.assertEquals("culling", PolygonMode.CULL_BACK, pm.getCulling());
            }
        });

        add(new TestCase("setShading") {
            public void run() {
                PolygonMode pm = new PolygonMode();
                pm.setShading(PolygonMode.SHADE_SMOOTH);
                Assert.assertEquals("shading", PolygonMode.SHADE_SMOOTH, pm.getShading());
            }
        });

        add(new TestCase("setTwoSidedLighting") {
            public void run() {
                PolygonMode pm = new PolygonMode();
                pm.setTwoSidedLightingEnable(true);
                Assert.assertTrue("two sided", pm.isTwoSidedLightingEnabled());
            }
        });

        add(new TestCase("setLocalCameraLighting") {
            public void run() {
                PolygonMode pm = new PolygonMode();
                pm.setLocalCameraLightingEnable(true);
                Assert.assertTrue("local camera", pm.isLocalCameraLightingEnabled());
            }
        });

        add(new TestCase("setWinding") {
            public void run() {
                PolygonMode pm = new PolygonMode();
                pm.setWinding(PolygonMode.WINDING_CW);
                Assert.assertEquals("winding", PolygonMode.WINDING_CW, pm.getWinding());
            }
        });
    }
}
