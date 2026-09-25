package m3gtest.tests;

import javax.microedition.m3g.*;
import m3gtest.*;

public class FogSuite extends TestSuite {

    public FogSuite() {
        super("fog", "Fog", "Tests Fog.");

        add(new TestCase("setColor") {
            public void run() {
                Fog fog = new Fog();
                fog.setColor(0xAABBCC);
                Assert.assertEquals("color", 0xAABBCC, fog.getColor());
            }
        });

        add(new TestCase("setMode") {
            public void run() {
                Fog fog = new Fog();
                fog.setMode(Fog.LINEAR);
                Assert.assertEquals("mode", Fog.LINEAR, fog.getMode());
                fog.setMode(Fog.EXPONENTIAL);
                Assert.assertEquals("mode exp", Fog.EXPONENTIAL, fog.getMode());
            }
        });

        add(new TestCase("setLinear") {
            public void run() {
                Fog fog = new Fog();
                fog.setLinear(1.0f, 10.0f);
                Assert.assertEquals("near", 1.0f, fog.getNearDistance(), 0.001f);
                Assert.assertEquals("far", 10.0f, fog.getFarDistance(), 0.001f);
            }
        });

        add(new TestCase("setDensity") {
            public void run() {
                Fog fog = new Fog();
                fog.setDensity(0.5f);
                Assert.assertEquals("density", 0.5f, fog.getDensity(), 0.001f);
            }
        });
    }
}
