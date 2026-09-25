package m3gtest.demos;

import javax.microedition.lcdui.Displayable;

public class Demos {

    private Demos() {}

    public static String[] titles() {
        return new String[] {
            "Rotating cube",
            "World demo",
            "Textured cube",
            "Sprite3D",
            "Morphing",
            "Transparency",
            "Picking"
        };
    }

    public static Displayable create(int index) {
        switch (index) {
            case 0: return new CanvasDemos.RotatingCube();
            case 1: return new CanvasDemos.WorldDemo();
            case 2: return new CanvasDemos.TexturedCube();
            case 3: return new CanvasDemos.Sprite3DDemo();
            case 4: return new CanvasDemos.MorphingDemo();
            case 5: return new CanvasDemos.TransparencyDemo();
            case 6: return new CanvasDemos.PickingDemo();
            default: return null;
        }
    }
}
