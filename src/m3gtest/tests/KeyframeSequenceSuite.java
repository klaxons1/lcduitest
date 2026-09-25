/*
 * M3G Tester - KeyframeSequence
 */
package m3gtest.tests;

import javax.microedition.m3g.*;

import m3gtest.*;

public class KeyframeSequenceSuite extends TestSuite {

    public KeyframeSequenceSuite() {
        super("KeyframeSequence", "KeyframeSequence", "constructor, keyframes, interpolation, repeat, range");

        add(new TestCase("constructor") {
            public void run() {
                KeyframeSequence ks = new KeyframeSequence(2, 3, KeyframeSequence.LINEAR);
                Assert.assertEquals("keyframe count", 2, ks.getKeyframeCount());
                Assert.assertEquals("component count", 3, ks.getComponentCount());
                Assert.assertEquals("interpolation LINEAR", KeyframeSequence.LINEAR, ks.getInterpolationType());

                KeyframeSequence ks2 = new KeyframeSequence(5, 1, KeyframeSequence.SPLINE);
                Assert.assertEquals("5 keyframes", 5, ks2.getKeyframeCount());

                KeyframeSequence ks3 = new KeyframeSequence(3, 4, KeyframeSequence.SLERP);
                Assert.assertEquals("SLERP", KeyframeSequence.SLERP, ks3.getInterpolationType());
            }
        });

        add(new TestCase("constructor_invalid") {
            public void run() {
                Assert.expectException("0 keyframes", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new KeyframeSequence(0, 3, KeyframeSequence.LINEAR);
                    }
                });
                Assert.expectException("0 components", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new KeyframeSequence(2, 0, KeyframeSequence.LINEAR);
                    }
                });
                Assert.expectException("5 components", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new KeyframeSequence(2, 5, KeyframeSequence.LINEAR);
                    }
                });
                Assert.expectException("invalid interpolation", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new KeyframeSequence(2, 3, 999);
                    }
                });
            }
        });

        add(new TestCase("set_keyframe_and_get") {
            public void run() {
                KeyframeSequence ks = new KeyframeSequence(2, 3, KeyframeSequence.LINEAR);
                ks.setKeyframe(0, 0, new float[]{0, 0, 0});
                ks.setKeyframe(1, 1000, new float[]{1, 1, 1});

                Assert.assertEquals("time 0", 0, ks.getKeyframe(0, null));
                Assert.assertEquals("time 1", 1000, ks.getKeyframe(1, null));

                float[] out = new float[3];
                ks.getKeyframe(0, out);
                Assert.assertEquals("value 0,0", 0f, out[0], 0.001f);
                ks.getKeyframe(1, out);
                Assert.assertEquals("value 1,0", 1f, out[0], 0.001f);

                // Set with null values should throw NPE?
                Assert.expectException("null values", NullPointerException.class, new Assert.Code() {
                    public void run() {
                        new KeyframeSequence(2, 3, KeyframeSequence.LINEAR).setKeyframe(0, 0, null);
                    }
                });
                Assert.expectException("short values array", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new KeyframeSequence(2, 3, KeyframeSequence.LINEAR).setKeyframe(0, 0, new float[]{0, 0});
                    }
                });
            }
        });

        add(new TestCase("repeat_mode_and_range") {
            public void run() {
                KeyframeSequence ks = new KeyframeSequence(2, 3, KeyframeSequence.LINEAR);
                ks.setRepeatMode(KeyframeSequence.LOOP);
                Assert.assertEquals("LOOP", KeyframeSequence.LOOP, ks.getRepeatMode());
                ks.setRepeatMode(KeyframeSequence.CLAMP);
                Assert.assertEquals("CLAMP", KeyframeSequence.CLAMP, ks.getRepeatMode());

                ks.setValidRange(0, 1);
                Assert.assertEquals("first", 0, ks.getValidRangeFirst());
                Assert.assertEquals("last", 1, ks.getValidRangeLast());

                ks.setValidRange(0, 0);
                Assert.assertEquals("single frame range", 0, ks.getValidRangeFirst());

                Assert.expectException("invalid repeat", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new KeyframeSequence(2, 3, KeyframeSequence.LINEAR).setRepeatMode(999);
                    }
                });
                Assert.expectException("valid range first > last", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        KeyframeSequence k = new KeyframeSequence(2, 3, KeyframeSequence.LINEAR);
                        k.setValidRange(1, 0);
                    }
                });
            }
        });

        add(new TestCase("duration") {
            public void run() {
                KeyframeSequence ks = new KeyframeSequence(2, 3, KeyframeSequence.LINEAR);
                ks.setKeyframe(0, 0, new float[]{0, 0, 0});
                ks.setKeyframe(1, 1000, new float[]{1, 1, 1});
                Assert.assertEquals("duration 1000", 1000, ks.getDuration());
            }
        });
    }
}
