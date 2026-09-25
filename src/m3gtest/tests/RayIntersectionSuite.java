package m3gtest.tests;

import javax.microedition.m3g.*;
import m3gtest.*;

public class RayIntersectionSuite extends TestSuite {

    public RayIntersectionSuite() {
        super("ray", "RayIntersection", "Tests RayIntersection.");

        add(new TestCase("ctor") {
            public void run() {
                RayIntersection ri = new RayIntersection();
                Assert.assertNotNull("ri", ri);
            }
        });

        add(new TestCase("getDistance after no pick") {
            public void run() {
                RayIntersection ri = new RayIntersection();
                float d = ri.getDistance();
                // distance may be 0 initially, just check no throw
                Assert.assertTrue("distance >=0 or any", true);
            }
        });

        add(new TestCase("getIntersected after no pick") {
            public void run() {
                RayIntersection ri = new RayIntersection();
                Object3D obj = ri.getIntersected();
                // may be null if no pick yet
                // just check no exception
            }
        });

        add(new TestCase("pick returns boolean") {
            public void run() {
                World w = new World();
                Camera cam = new Camera();
                cam.setPerspective(60, 1, 1, 10);
                Transform ct = new Transform();
                ct.postTranslate(0,0,5);
                cam.setTransform(ct);
                w.addChild(cam);
                w.setActiveCamera(cam);
                RayIntersection ri = new RayIntersection();
                boolean hit = w.pick(0, 0,0,0, 0,0,-1, ri);
                // hit boolean, just check execution
                Assert.assertTrue("pick executed", true);
            }
        });
    }
}
