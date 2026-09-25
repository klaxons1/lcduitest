/*
 * M3G Tester - catalogue of visual demos
 */
package m3gtest.demos;

import javax.microedition.lcdui.Displayable;

public class Demos {

    private Demos() {
    }

    public static Demo[] all() {
        return new Demo[]{
            new Demo("Rotating cube - immediate mode") {
                public Displayable createScreen() {
                    return new CanvasDemos.RotatingCube();
                }
            },
            new Demo("World + Camera + Light - retained mode") {
                public Displayable createScreen() {
                    return new CanvasDemos.WorldDemo();
                }
            },
            new Demo("Texture + Material + Fog") {
                public Displayable createScreen() {
                    return new CanvasDemos.TexturedCube();
                }
            },
            new Demo("Sprite3D") {
                public Displayable createScreen() {
                    return new CanvasDemos.Sprite3DDemo();
                }
            },
            new Demo("MorphingMesh") {
                public Displayable createScreen() {
                    return new CanvasDemos.MorphingDemo();
                }
            },
            new Demo("Transparency & CompositingMode") {
                public Displayable createScreen() {
                    return new CanvasDemos.TransparencyDemo();
                }
            },
            new Demo("Picking - RayIntersection") {
                public Displayable createScreen() {
                    return new CanvasDemos.PickingDemo();
                }
            }
        };
    }
}
