package m3gtest.tests;

import javax.microedition.m3g.*;
import m3gtest.*;

public class LightSuite extends TestSuite {

    public LightSuite() {
        super("light", "Light", "Tests Light.");

        add(new TestCase("setMode") {
            public void run() {
                Light l = new Light();
                l.setMode(Light.DIRECTIONAL);
                Assert.assertEquals("mode", Light.DIRECTIONAL, l.getMode());
            }
        });

        add(new TestCase("setColor") {
            public void run() {
                Light l = new Light();
                l.setColor(0xFFFFFF);
                Assert.assertEquals("color", 0xFFFFFF, l.getColor());
            }
        });

        add(new TestCase("setIntensity") {
            public void run() {
                Light l = new Light();
                l.setIntensity(0.8f);
                Assert.assertEquals("intensity", 0.8f, l.getIntensity(), 0.001f);
            }
        });

        add(new TestCase("setAttenuation") {
            public void run() {
                Light l = new Light();
                l.setAttenuation(1.0f, 0.1f, 0.01f);
                Assert.assertEquals("constant", 1.0f, l.getConstantAttenuation(), 0.001f);
                Assert.assertEquals("linear", 0.1f, l.getLinearAttenuation(), 0.001f);
                Assert.assertEquals("quad", 0.01f, l.getQuadraticAttenuation(), 0.001f);
            }
        });

        add(new TestCase("setSpotAngle") {
            public void run() {
                Light l = new Light();
                l.setSpotAngle(30.0f);
                Assert.assertEquals("spot angle", 30.0f, l.getSpotAngle(), 0.001f);
            }
        });

        add(new TestCase("setSpotExponent") {
            public void run() {
                Light l = new Light();
                l.setSpotExponent(2.0f);
                Assert.assertEquals("spot exp", 2.0f, l.getSpotExponent(), 0.001f);
            }
        });
    }
}
