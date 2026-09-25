package m3gtest.tests;

import javax.microedition.m3g.*;
import m3gtest.*;

public class AnimationTrackSuite extends TestSuite {

    public AnimationTrackSuite() {
        super("animtrack", "AnimationTrack", "Tests AnimationTrack.");

        add(new TestCase("ctor") {
            public void run() {
                KeyframeSequence ks = new KeyframeSequence(2, 3, KeyframeSequence.LINEAR);
                ks.setKeyframe(0, 0, new float[]{0,0,0});
                ks.setKeyframe(1, 1000, new float[]{1,1,1});
                AnimationTrack track = new AnimationTrack(ks, AnimationTrack.TRANSLATION);
                Assert.assertNotNull("track", track);
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

        add(new TestCase("getTargetProperty") {
            public void run() {
                KeyframeSequence ks = new KeyframeSequence(1, 3, KeyframeSequence.LINEAR);
                ks.setKeyframe(0, 0, new float[]{0,0,0});
                AnimationTrack track = new AnimationTrack(ks, AnimationTrack.ALPHA);
                Assert.assertEquals("prop ALPHA", AnimationTrack.ALPHA, track.getTargetProperty());
            }
        });
    }
}
