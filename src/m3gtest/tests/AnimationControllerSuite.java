package m3gtest.tests;

import javax.microedition.m3g.*;
import m3gtest.*;

public class AnimationControllerSuite extends TestSuite {

    public AnimationControllerSuite() {
        super("animctrl", "AnimationController", "Tests AnimationController.");

        add(new TestCase("setSpeed getSpeed") {
            public void run() {
                AnimationController ctrl = new AnimationController();
                ctrl.setSpeed(2.0f, 0);
                Assert.assertEquals("speed", 2.0f, ctrl.getSpeed(), 0.001f);
            }
        });

        add(new TestCase("setWeight getWeight") {
            public void run() {
                AnimationController ctrl = new AnimationController();
                ctrl.setWeight(0.5f);
                Assert.assertEquals("weight", 0.5f, ctrl.getWeight(), 0.001f);
            }
        });

        add(new TestCase("setActiveInterval") {
            public void run() {
                AnimationController ctrl = new AnimationController();
                ctrl.setActiveInterval(0, 1000);
                Assert.assertEquals("start", 0, ctrl.getActiveIntervalStart());
                Assert.assertEquals("end", 1000, ctrl.getActiveIntervalEnd());
            }
        });

        add(new TestCase("getRefWorldTime") {
            public void run() {
                AnimationController ctrl = new AnimationController();
                int ref = ctrl.getRefWorldTime();
                // Default 0
                Assert.assertTrue("ref time", ref >= 0 || ref == 0);
            }
        });

        add(new TestCase("setPosition getPosition") {
            public void run() {
                AnimationController ctrl = new AnimationController();
                ctrl.setPosition(0.75f, 100);
                float pos = ctrl.getPosition(100);
                Assert.assertEquals("pos", 0.75f, pos, 0.001f);
            }
        });

        add(new TestCase("getPosition at different time") {
            public void run() {
                AnimationController ctrl = new AnimationController();
                ctrl.setSpeed(1.0f, 0);
                ctrl.setPosition(0.0f, 0);
                float p0 = ctrl.getPosition(0);
                float p1000 = ctrl.getPosition(1000);
                Assert.assertEquals("p0", 0.0f, p0, 0.001f);
                Assert.assertTrue("p1000 > p0", p1000 > p0 || p1000 == 1000.0f);
            }
        });
    }
}
