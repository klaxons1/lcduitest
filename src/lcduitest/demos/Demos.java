/**
 * LcduiTest - MIDP 2.0 LCDUI conformance test MIDlet.
 *
 * The catalogue of visual demos. Each entry is a screen that shows a part of
 * the lcdui API that has to be judged by a human: glyph shapes, arc
 * rasterisation, layout, colours, animations, input events.
 */
package lcduitest.demos;

import javax.microedition.lcdui.Displayable;

public class Demos {

    private Demos() {
    }

    public static Demo[] all() {
        return new Demo[]{
            new Demo("Colours, grey levels, colour constants") {
                public Displayable createScreen() {
                    return new CanvasDemos.Colors();
                }
            },
            new Demo("Lines, rectangles, arcs, triangles") {
                public Displayable createScreen() {
                    return new CanvasDemos.Shapes();
                }
            },
            new Demo("Arcs: 4 of each type, all quadrants, sweeping angles") {
                public Displayable createScreen() {
                    return new CanvasDemos.Arcs();
                }
            },
            new Demo("Fonts, sizes, styles, text anchors") {
                public Displayable createScreen() {
                    return new CanvasDemos.Text();
                }
            },
            new Demo("Image anchors and drawRegion transforms") {
                public Displayable createScreen() {
                    return new CanvasDemos.Images();
                }
            },
            new Demo("drawRGB and alpha blending") {
                public Displayable createScreen() {
                    return new CanvasDemos.Rgb();
                }
            },
            new Demo("Clipping and translation") {
                public Displayable createScreen() {
                    return new CanvasDemos.Clipping();
                }
            },
            new Demo("Ticker on a Canvas") {
                public Displayable createScreen() {
                    return new CanvasDemos.TickerDemo();
                }
            },
            new Demo("Game API: GameCanvas, Sprite, TiledLayer") {
                public Displayable createScreen() {
                    return new CanvasDemos.Game();
                }
            },
            new Demo("Form with every Item type") {
                public Displayable createScreen() {
                    return new ItemDemos.AllItems();
                }
            },
            new Demo("List types (IMPLICIT / EXCLUSIVE / MULTIPLE)") {
                public Displayable createScreen() {
                    return new ItemDemos.Lists();
                }
            },
            new Demo("Alert types and a modal alert") {
                public Displayable createScreen() {
                    return new ItemDemos.Alerts();
                }
            },
            new Demo("CustomItem: key and pointer events") {
                public Displayable createScreen() {
                    return new ItemDemos.CustomItemDemo();
                }
            }
        };
    }
}
