/*
 * M3G Tester - AnimationController
 */
package m3gtest.tests;

import javax.microedition.m3g.*;

import m3gtest.*;

public class AnimationControllerSuite extends TestSuite {

    public AnimationControllerSuite() {
        super("AnimationController", "AnimationController", "speed, weight, active interval, position, reference world time");

        add(new TestCase("default_values") {
            public void run() {
                AnimationController ac = new AnimationController();
                Assert.info("default speed", ac.getSpeed());
                Assert.info("default weight", ac.getWeight());
                Assert.assertEquals("default speed 1.0", 1.0f, ac.getSpeed(), 0.001f);
                Assert.assertEquals("default weight 1.0", 1.0f, ac.getWeight(), 0.001f);
                Assert.assertEquals("default position 0", 0.0f, ac.getPosition(), 0.001f);
            }
        });

        add(new TestCase("speed_and_weight") {
            public void run() {
                AnimationController ac = new AnimationController();
                ac.setSpeed(2.0f, 1000);
                Assert.assertEquals("speed 2", 2.0f, ac.getSpeed(), 0.001f);
                ac.setWeight(0.5f);
                Assert.assertEquals("weight 0.5", 0.5f, ac.getWeight(), 0.001f);
                ac.setWeight(0.0f);
                Assert.assertEquals("weight 0", 0.0f, ac.getWeight(), 0.001f);
                ac.setWeight(1.0f);
                Assert.assertEquals("weight 1", 1.0f, ac.getWeight(), 0.001f);

                Assert.expectException("negative weight", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new AnimationController().setWeight(-0.1f);
                    }
                });
            }
        });

        add(new TestCase("active_interval") {
            public void run() {
                AnimationController ac = new AnimationController();
                ac.setActiveInterval(0, 1000);
                Assert.assertEquals("start 0", 0, ac.getActiveIntervalStart());
                Assert.assertEquals("end 1000", 1000, ac.getActiveIntervalEnd());

                ac.setActiveInterval(100, 200);
                Assert.assertEquals("start 100", 100, ac.getActiveIntervalStart());
                Assert.assertEquals("end 200", 200, ac.getActiveIntervalEnd());
            }
        });

        add(new TestCase("position_and_ref_world_time") {
            public void run() {
                AnimationController ac = new AnimationController();
                ac.setPosition(0.5f, 1000);
                Assert.assertEquals("position 0.5", 0.5f, ac.getPosition(), 0.001f);
                Assert.assertEquals("ref world time 1000", 1000, ac.getRefWorldTime());

                ac.setPosition(0.0f, 0);
                Assert.assertEquals("position 0", 0.0f, ac.getPosition(), 0.001f);
            }
        });
    }
}
