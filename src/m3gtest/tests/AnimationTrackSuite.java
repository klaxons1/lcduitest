/*
 * M3G Tester - AnimationTrack
 */
package m3gtest.tests;

import javax.microedition.m3g.*;

import m3gtest.*;

public class AnimationTrackSuite extends TestSuite {

    public AnimationTrackSuite() {
        super("AnimationTrack", "AnimationTrack", "constructor, keyframeSequence, controller, property");

        add(new TestCase("constructor_with_controller") {
            public void run() {
                KeyframeSequence ks = new KeyframeSequence(2, 3, KeyframeSequence.LINEAR);
                ks.setKeyframe(0, 0, new float[]{0, 0, 0});
                ks.setKeyframe(1, 1000, new float[]{1, 1, 1});
                AnimationController ac = new AnimationController();
                AnimationTrack track = new AnimationTrack(ks, ac, AnimationTrack.COLOR);
                Assert.assertSame("keyframe sequence", ks, track.getKeyframeSequence());
                Assert.assertSame("controller", ac, track.getController());
                Assert.assertEquals("property COLOR", AnimationTrack.COLOR, track.getTargetProperty());
            }
        });

        add(new TestCase("constructor_without_controller") {
            public void run() {
                KeyframeSequence ks = new KeyframeSequence(2, 1, KeyframeSequence.LINEAR);
                ks.setKeyframe(0, 0, new float[]{0});
                ks.setKeyframe(1, 1000, new float[]{1});
                AnimationTrack track = new AnimationTrack(ks, AnimationTrack.ALPHA);
                Assert.assertNull("controller null", track.getController());
                Assert.assertSame("ks", ks, track.getKeyframeSequence());
            }
        });

        add(new TestCase("set_controller_and_keyframe") {
            public void run() {
                KeyframeSequence ks = new KeyframeSequence(2, 3, KeyframeSequence.LINEAR);
                ks.setKeyframe(0, 0, new float[]{0, 0, 0});
                ks.setKeyframe(1, 1000, new float[]{1, 1, 1});
                AnimationController ac = new AnimationController();
                AnimationTrack track = new AnimationTrack(ks, AnimationTrack.COLOR);
                track.setController(ac);
                Assert.assertSame("controller set", ac, track.getController());
                track.setController(null);
                Assert.assertNull("controller null after set null", track.getController());

                KeyframeSequence ks2 = new KeyframeSequence(2, 3, KeyframeSequence.LINEAR);
                ks2.setKeyframe(0, 0, new float[]{0, 0, 0});
                ks2.setKeyframe(1, 500, new float[]{0, 1, 0});
                track.setKeyframeSequence(ks2);
                Assert.assertSame("ks2", ks2, track.getKeyframeSequence());
            }
        });

        add(new TestCase("invalid_args") {
            public void run() {
                Assert.expectException("null keyframe", NullPointerException.class, new Assert.Code() {
                    public void run() {
                        new AnimationTrack(null, AnimationTrack.COLOR);
                    }
                });
                Assert.expectException("null ks with controller", NullPointerException.class, new Assert.Code() {
                    public void run() {
                        new AnimationTrack(null, new AnimationController(), AnimationTrack.COLOR);
                    }
                });
                Assert.expectException("invalid property", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        KeyframeSequence ks = new KeyframeSequence(2, 3, KeyframeSequence.LINEAR);
                        ks.setKeyframe(0, 0, new float[]{0, 0, 0});
                        ks.setKeyframe(1, 1000, new float[]{1, 1, 1});
                        new AnimationTrack(ks, 999);
                    }
                });
                Assert.expectException("setKeyframeSequence null", NullPointerException.class, new Assert.Code() {
                    public void run() {
                        KeyframeSequence ks = new KeyframeSequence(2, 3, KeyframeSequence.LINEAR);
                        ks.setKeyframe(0, 0, new float[]{0, 0, 0});
                        ks.setKeyframe(1, 1000, new float[]{1, 1, 1});
                        AnimationTrack t = new AnimationTrack(ks, AnimationTrack.COLOR);
                        t.setKeyframeSequence(null);
                    }
                });
            }
        });

        add(new TestCase("property_constants") {
            public void run() {
                // Just ensure constants exist and are distinct
                int[] props = new int[]{
                    AnimationTrack.ALPHA,
                    AnimationTrack.AMBIENT_COLOR,
                    AnimationTrack.COLOR,
                    AnimationTrack.CROP,
                    AnimationTrack.DENSITY,
                    AnimationTrack.DIFFUSE_COLOR,
                    AnimationTrack.EMISSIVE_COLOR,
                    AnimationTrack.FAR_DISTANCE,
                    AnimationTrack.FIELD_OF_VIEW,
                    AnimationTrack.INTENSITY,
                    AnimationTrack.MORPH_WEIGHTS,
                    AnimationTrack.NEAR_DISTANCE,
                    AnimationTrack.ORIENTATION,
                    AnimationTrack.PICKABILITY,
                    AnimationTrack.SCALE,
                    AnimationTrack.SHININESS,
                    AnimationTrack.SPECULAR_COLOR,
                    AnimationTrack.SPOT_ANGLE,
                    AnimationTrack.SPOT_EXPONENT,
                    AnimationTrack.TRANSLATION,
                    AnimationTrack.VISIBILITY
                };
                for (int i = 0; i < props.length; i++) {
                    for (int j = i + 1; j < props.length; j++) {
                        Assert.assertNotEquals("properties distinct " + i + " vs " + j, props[i], props[j]);
                    }
                }
                Assert.info("property count", props.length);
            }
        });
    }
}
