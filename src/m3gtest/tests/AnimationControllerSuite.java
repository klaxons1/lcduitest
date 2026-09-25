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
                Assert.assertEquals("speed", 2.0f, ctrl.getSpeed(0), 0.001f);
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

        add(new TestCase("setRefWorldTime") {
            public void run() {
                AnimationController ctrl = new AnimationController();
                ctrl.setRefWorldTime(500);
                Assert.assertEquals("ref time", 500, ctrl.getRefWorldTime());
            }
        });

        add(new TestCase("setPosition") {
            public void run() {
                AnimationController ctrl = new AnimationController();
                ctrl.setPosition(0.75f, 0);
                Assert.assertEquals("pos", 0.75f, ctrl.getPosition(0), 0.001f);
            }
        });
    }
}
