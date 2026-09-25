/*
 * M3G Tester - Light
 */
package m3gtest.tests;

import javax.microedition.m3g.*;

import m3gtest.*;

public class LightSuite extends TestSuite {

    public LightSuite() {
        super("Light", "Light", "mode, color, intensity, spot angle/exponent, attenuation");

        add(new TestCase("default_and_mode") {
            public void run() {
                Light light = new Light();
                Assert.info("default mode", light.getMode());
                light.setMode(Light.AMBIENT);
                Assert.assertEquals("AMBIENT", Light.AMBIENT, light.getMode());
                light.setMode(Light.DIRECTIONAL);
                Assert.assertEquals("DIRECTIONAL", Light.DIRECTIONAL, light.getMode());
                light.setMode(Light.OMNI);
                Assert.assertEquals("OMNI", Light.OMNI, light.getMode());
                light.setMode(Light.SPOT);
                Assert.assertEquals("SPOT", Light.SPOT, light.getMode());

                Assert.expectException("invalid mode", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new Light().setMode(999);
                    }
                });
            }
        });

        add(new TestCase("color_and_intensity") {
            public void run() {
                Light light = new Light();
                light.setColor(0xFF0000);
                Assert.assertEquals("red", 0xFF0000, light.getColor() & 0xFFFFFF);
                light.setIntensity(1.0f);
                Assert.assertEquals("intensity 1", 1.0f, light.getIntensity(), 0.001f);
                light.setIntensity(0.5f);
                Assert.assertEquals("intensity 0.5", 0.5f, light.getIntensity(), 0.001f);

                Assert.expectException("negative intensity", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new Light().setIntensity(-0.1f);
                    }
                });
            }
        });

        add(new TestCase("spot_params") {
            public void run() {
                Light light = new Light();
                light.setMode(Light.SPOT);
                light.setSpotAngle(45.0f);
                Assert.assertEquals("spot angle 45", 45.0f, light.getSpotAngle(), 0.001f);
                light.setSpotExponent(2.0f);
                Assert.assertEquals("spot exponent 2", 2.0f, light.getSpotExponent(), 0.001f);

                Assert.expectException("spot angle <=0", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        Light l = new Light();
                        l.setMode(Light.SPOT);
                        l.setSpotAngle(0);
                    }
                });
                Assert.expectException("spot angle >90", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        Light l = new Light();
                        l.setMode(Light.SPOT);
                        l.setSpotAngle(91);
                    }
                });
                Assert.expectException("spot exponent negative", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        Light l = new Light();
                        l.setSpotExponent(-1);
                    }
                });
            }
        });

        add(new TestCase("attenuation") {
            public void run() {
                Light light = new Light();
                light.setMode(Light.OMNI);
                light.setAttenuation(1.0f, 0.1f, 0.01f);
                Assert.assertEquals("constant", 1.0f, light.getConstantAttenuation(), 0.001f);
                Assert.assertEquals("linear", 0.1f, light.getLinearAttenuation(), 0.001f);
                Assert.assertEquals("quadratic", 0.01f, light.getQuadraticAttenuation(), 0.001f);

                Assert.expectException("negative attenuation", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new Light().setAttenuation(-1, 0, 0);
                    }
                });
            }
        });
    }
}
