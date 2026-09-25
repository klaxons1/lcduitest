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
            "Picking",
            "Fog",
            "Multi-light",
            "Blending modes",
            "Background scroll",
            "Animation",
            "Vertex colors",
            "Multiple viewports"
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
            case 7: return new CanvasDemos.FogDemo();
            case 8: return new CanvasDemos.LightingDemo();
            case 9: return new CanvasDemos.CompositingDemo();
            case 10: return new CanvasDemos.BackgroundScrollDemo();
            case 11: return new CanvasDemos.AnimationDemo();
            case 12: return new CanvasDemos.VertexColorDemo();
            case 13: return new CanvasDemos.MultipleViewportsDemo();
            default: return null;
        }
    }
}
