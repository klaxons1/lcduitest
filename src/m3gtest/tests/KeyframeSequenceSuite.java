package m3gtest.tests;

import javax.microedition.m3g.*;
import m3gtest.*;

public class KeyframeSequenceSuite extends TestSuite {

    public KeyframeSequenceSuite() {
        super("keyframe", "KeyframeSequence", "Tests KeyframeSequence.");

        add(new TestCase("ctor") {
            public void run() {
                KeyframeSequence ks = new KeyframeSequence(2, 3, KeyframeSequence.LINEAR);
                Assert.assertEquals("num keyframes", 2, ks.getKeyframeCount());
                Assert.assertEquals("components", 3, ks.getComponentCount());
            }
        });

        add(new TestCase("setKeyframe getKeyframe") {
            public void run() {
                KeyframeSequence ks = new KeyframeSequence(2, 3, KeyframeSequence.LINEAR);
                ks.setKeyframe(0, 0, new float[]{1,2,3});
                ks.setKeyframe(1, 1000, new float[]{4,5,6});
                float[] out = new float[3];
                int time = ks.getKeyframe(0, out);
                Assert.assertEquals("time 0", 0, time);
                Assert.assertEquals("x", 1.0f, out[0], 0.001f);
                Assert.assertEquals("y", 2.0f, out[1], 0.001f);
            }
        });

        add(new TestCase("setDuration") {
            public void run() {
                KeyframeSequence ks = new KeyframeSequence(2, 3, KeyframeSequence.LINEAR);
                ks.setDuration(2000);
                Assert.assertEquals("duration", 2000, ks.getDuration());
            }
        });

        add(new TestCase("setRepeatMode") {
            public void run() {
                KeyframeSequence ks = new KeyframeSequence(1, 3, KeyframeSequence.LINEAR);
                ks.setRepeatMode(KeyframeSequence.LOOP);
                Assert.assertEquals("repeat", KeyframeSequence.LOOP, ks.getRepeatMode());
            }
        });

        add(new TestCase("setValidRange") {
            public void run() {
                KeyframeSequence ks = new KeyframeSequence(3, 3, KeyframeSequence.LINEAR);
                ks.setValidRange(0, 1);
                Assert.assertEquals("first", 0, ks.getValidRangeFirst());
                Assert.assertEquals("last", 1, ks.getValidRangeLast());
            }
        });
    }
}
