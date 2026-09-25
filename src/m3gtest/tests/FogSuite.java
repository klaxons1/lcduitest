/*
 * M3G Tester - Fog
 */
package m3gtest.tests;

import javax.microedition.m3g.*;

import m3gtest.*;

public class FogSuite extends TestSuite {

    public FogSuite() {
        super("Fog", "Fog", "mode, color, density, near/far");

        add(new TestCase("default_and_mode") {
            public void run() {
                Fog fog = new Fog();
                Assert.info("default mode", fog.getMode());
                Assert.info("default color", Ui.hex(fog.getColor()));
                fog.setMode(Fog.LINEAR);
                Assert.assertEquals("LINEAR", Fog.LINEAR, fog.getMode());
                fog.setMode(Fog.EXPONENTIAL);
                Assert.assertEquals("EXP", Fog.EXPONENTIAL, fog.getMode());

                Assert.expectException("invalid mode", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new Fog().setMode(999);
                    }
                });
            }
        });

        add(new TestCase("color") {
            public void run() {
                Fog fog = new Fog();
                fog.setColor(0xFF0000);
                Assert.assertEquals("red", 0xFF0000, fog.getColor() & 0xFFFFFF);
                fog.setColor(0x00FF00);
                Assert.assertEquals("green", 0x00FF00, fog.getColor() & 0xFFFFFF);
            }
        });

        add(new TestCase("density") {
            public void run() {
                Fog fog = new Fog();
                fog.setDensity(0.5f);
                Assert.assertEquals("density 0.5", 0.5f, fog.getDensity(), 0.001f);
                fog.setDensity(0.0f);
                Assert.assertEquals("density 0", 0.0f, fog.getDensity(), 0.001f);

                Assert.expectException("negative density", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new Fog().setDensity(-0.1f);
                    }
                });
            }
        });

        add(new TestCase("near_far") {
            public void run() {
                Fog fog = new Fog();
                fog.setLinear(1.0f, 10.0f);
                Assert.assertEquals("near 1", 1.0f, fog.getNearDistance(), 0.001f);
                Assert.assertEquals("far 10", 10.0f, fog.getFarDistance(), 0.001f);

                fog.setLinear(0.0f, 100.0f);
                Assert.assertEquals("near 0", 0.0f, fog.getNearDistance(), 0.001f);

                Assert.expectException("near > far", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new Fog().setLinear(10.0f, 1.0f);
                    }
                });
                Assert.expectException("negative near", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new Fog().setLinear(-1.0f, 10.0f);
                    }
                });
            }
        });
    }
}
