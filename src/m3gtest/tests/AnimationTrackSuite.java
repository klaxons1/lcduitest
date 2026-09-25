package m3gtest.tests;

import javax.microedition.m3g.*;
import m3gtest.*;

public class AnimationTrackSuite extends TestSuite {

    public AnimationTrackSuite() {
        super("animtrack", "AnimationTrack", "Tests AnimationTrack.");

        add(new TestCase("ctor translation 3 components") {
            public void run() {
                KeyframeSequence ks = new KeyframeSequence(2, 3, KeyframeSequence.LINEAR);
                ks.setKeyframe(0, 0, new float[]{0,0,0});
                ks.setKeyframe(1, 1000, new float[]{1,1,1});
                AnimationTrack track = new AnimationTrack(ks, AnimationTrack.TRANSLATION);
                Assert.assertNotNull("track", track);
            }
        });

        add(new TestCase("ctor alpha 1 component") {
            public void run() {
                KeyframeSequence ks = new KeyframeSequence(2, 1, KeyframeSequence.LINEAR);
                ks.setKeyframe(0, 0, new float[]{0});
                ks.setKeyframe(1, 1000, new float[]{1});
                AnimationTrack track = new AnimationTrack(ks, AnimationTrack.ALPHA);
                Assert.assertNotNull("alpha track", track);
                Assert.assertEquals("prop ALPHA", AnimationTrack.ALPHA, track.getTargetProperty());
            }
        });

        add(new TestCase("getKeyframeSequence") {
            public void run() {
                KeyframeSequence ks = new KeyframeSequence(1, 3, KeyframeSequence.LINEAR);
                ks.setKeyframe(0, 0, new float[]{0,0,0});
                AnimationTrack track = new AnimationTrack(ks, AnimationTrack.TRANSLATION);
                Assert.assertSame("ks", ks, track.getKeyframeSequence());
            }
        });

        add(new TestCase("setController") {
            public void run() {
                KeyframeSequence ks = new KeyframeSequence(1, 3, KeyframeSequence.LINEAR);
                ks.setKeyframe(0, 0, new float[]{0,0,0});
                AnimationTrack track = new AnimationTrack(ks, AnimationTrack.TRANSLATION);
                AnimationController ctrl = new AnimationController();
                track.setController(ctrl);
                Assert.assertSame("controller", ctrl, track.getController());
            }
        });

        add(new TestCase("getTargetProperty translation") {
            public void run() {
                KeyframeSequence ks = new KeyframeSequence(1, 3, KeyframeSequence.LINEAR);
                ks.setKeyframe(0, 0, new float[]{0,0,0});
                AnimationTrack track = new AnimationTrack(ks, AnimationTrack.TRANSLATION);
                Assert.assertEquals("prop TRANSLATION", AnimationTrack.TRANSLATION, track.getTargetProperty());
            }
        });

        add(new TestCase("invalid component count throws") {
            public void run() {
                final KeyframeSequence ks = new KeyframeSequence(1, 3, KeyframeSequence.LINEAR);
                ks.setKeyframe(0, 0, new float[]{0,0,0});
                Assert.expectException("ALPHA needs 1 component", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new AnimationTrack(ks, AnimationTrack.ALPHA);
                    }
                });
            }
        });
    }
}
