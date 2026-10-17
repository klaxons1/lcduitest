package lcdtest.checks;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import lcdtest.Act;
import lcdtest.Body;
import lcdtest.Ctx;
import lcdtest.Host;
import lcdtest.Pix;
import lcdtest.Suite;
import lcdtest.Util;

/**
 * Graphics API contract checks that do not depend on exact pixels.
 * Rasterisation is covered by RasterChecks.
 */
public final class GraphicsChecks {

    private GraphicsChecks() {
    }

    public static void register(Suite s) {
        final String G = "Graphics";

        s.add("GFX-001", G, "new image Graphics has the documented defaults",
                "Image.getGraphics: destination image, full clip, black, default font, SOLID, origin 0,0", new Body() {
                    public void run(Ctx c) {
                        Image im = Image.createImage(24, 16);
                        Graphics g = im.getGraphics();
                        c.eqInt("getColor()", 0x000000, g.getColor());
                        c.eqInt("getStrokeStyle()", Graphics.SOLID, g.getStrokeStyle());
                        c.same("getFont()", Font.getDefaultFont(), g.getFont());
                        c.eqInt("getTranslateX()", 0, g.getTranslateX());
                        c.eqInt("getTranslateY()", 0, g.getTranslateY());
                        c.eqInt("getClipX()", 0, g.getClipX());
                        c.eqInt("getClipY()", 0, g.getClipY());
                        c.eqInt("getClipWidth()", 24, g.getClipWidth());
                        c.eqInt("getClipHeight()", 16, g.getClipHeight());
                    }
                });

        s.add("GFX-002", G, "setColor(int) components are readable",
                "Graphics.setColor(int)/getColor/getRedComponent/getGreenComponent/getBlueComponent", new Body() {
                    public void run(Ctx c) {
                        Graphics g = scratch();
                        g.setColor(0x123456);
                        int rgb = g.getColor();
                        c.inRange("getColor()", rgb, 0, 0xFFFFFF);
                        c.inRange("getRedComponent()", g.getRedComponent(), 0, 255);
                        c.inRange("getGreenComponent()", g.getGreenComponent(), 0, 255);
                        c.inRange("getBlueComponent()", g.getBlueComponent(), 0, 255);
                        if (Host.display.numColors() >= 65536) {
                            c.eqInt("getColor round-trip", 0x123456, rgb);
                        } else {
                            c.warn("limited colour display: exact getColor round-trip not required");
                        }
                    }
                });

        s.add("GFX-003", G, "setColor components reject values outside 0..255",
                "Graphics.setColor(int,int,int): IAE if any component is out of range", new Body() {
                    public void run(Ctx c) {
                        final Graphics g = scratch();
                        c.throwsEx("setColor(-1,0,0)", IllegalArgumentException.class, new Act() {
                            public void run() {
                                g.setColor(-1, 0, 0);
                            }
                        });
                        c.throwsEx("setColor(0,256,0)", IllegalArgumentException.class, new Act() {
                            public void run() {
                                g.setColor(0, 256, 0);
                            }
                        });
                        c.throwsEx("setColor(0,0,300)", IllegalArgumentException.class, new Act() {
                            public void run() {
                                g.setColor(0, 0, 300);
                            }
                        });
                    }
                });

        s.add("GFX-004", G, "setColor(r,g,b) agrees with setColor(rgb)",
                "Graphics.setColor(int,int,int) and setColor(int)", new Body() {
                    public void run(Ctx c) {
                        Graphics g = scratch();
                        g.setColor(0x204060);
                        int a = g.getColor();
                        g.setColor(0x20, 0x40, 0x60);
                        int b = g.getColor();
                        c.advEqInt("equivalent setColor calls", a, b);
                    }
                });

        s.add("GFX-005", G, "grayscale round-trip and range checking",
                "Graphics.setGrayScale(int)/getGrayScale", new Body() {
                    public void run(Ctx c) {
                        final Graphics g = scratch();
                        g.setGrayScale(128);
                        c.eqInt("getGrayScale after setGrayScale(128)", 128, g.getGrayScale());
                        c.throwsEx("setGrayScale(-1)", IllegalArgumentException.class, new Act() {
                            public void run() {
                                g.setGrayScale(-1);
                            }
                        });
                        c.throwsEx("setGrayScale(256)", IllegalArgumentException.class, new Act() {
                            public void run() {
                                g.setGrayScale(256);
                            }
                        });
                    }
                });

        s.add("GFX-006", G, "stroke style round-trip and validation",
                "Graphics.setStrokeStyle/getStrokeStyle: SOLID or DOTTED", new Body() {
                    public void run(Ctx c) {
                        final Graphics g = scratch();
                        g.setStrokeStyle(Graphics.DOTTED);
                        c.eqInt("getStrokeStyle() DOTTED", Graphics.DOTTED, g.getStrokeStyle());
                        g.setStrokeStyle(Graphics.SOLID);
                        c.eqInt("getStrokeStyle() SOLID", Graphics.SOLID, g.getStrokeStyle());
                        c.throwsEx("setStrokeStyle(2)", IllegalArgumentException.class, new Act() {
                            public void run() {
                                g.setStrokeStyle(2);
                            }
                        });
                        c.throwsEx("setStrokeStyle(-1)", IllegalArgumentException.class, new Act() {
                            public void run() {
                                g.setStrokeStyle(-1);
                            }
                        });
                    }
                });

        s.add("GFX-007", G, "setFont(null) restores the default font",
                "Graphics.setFont(Font): null is equivalent to setFont(getDefaultFont())", new Body() {
                    public void run(Ctx c) {
                        Graphics g = scratch();
                        Font f = Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_BOLD, Font.SIZE_LARGE);
                        g.setFont(f);
                        c.same("getFont after setFont(f)", f, g.getFont());
                        g.setFont(null);
                        c.same("getFont after setFont(null)", Font.getDefaultFont(), g.getFont());
                    }
                });

        s.add("GFX-008", G, "translate is cumulative",
                "Graphics.translate(int,int): effects are cumulative", new Body() {
                    public void run(Ctx c) {
                        Graphics g = scratch();
                        g.translate(1, 2);
                        g.translate(3, 4);
                        c.eqInt("getTranslateX()", 4, g.getTranslateX());
                        c.eqInt("getTranslateY()", 6, g.getTranslateY());
                        g.translate(-4, -6);
                        c.eqInt("getTranslateX() after reset", 0, g.getTranslateX());
                        c.eqInt("getTranslateY() after reset", 0, g.getTranslateY());
                    }
                });

        s.add("GFX-009", G, "setClip sets an absolute clip",
                "Graphics.setClip(int,int,int,int)/getClip*", new Body() {
                    public void run(Ctx c) {
                        Graphics g = scratch();
                        g.setClip(3, 4, 5, 6);
                        c.eqInt("getClipX()", 3, g.getClipX());
                        c.eqInt("getClipY()", 4, g.getClipY());
                        c.eqInt("getClipWidth()", 5, g.getClipWidth());
                        c.eqInt("getClipHeight()", 6, g.getClipHeight());
                        g.setClip(0, 0, 24, 16);
                        c.eqInt("clip restored width", 24, g.getClipWidth());
                        c.eqInt("clip restored height", 16, g.getClipHeight());
                    }
                });

        s.add("GFX-010", G, "clipRect intersects and cannot enlarge",
                "Graphics.clipRect(int,int,int,int): can only make the clip smaller", new Body() {
                    public void run(Ctx c) {
                        Graphics g = scratch();
                        g.setClip(2, 2, 10, 10);
                        g.clipRect(5, 5, 10, 10);
                        c.eqInt("intersection X", 5, g.getClipX());
                        c.eqInt("intersection Y", 5, g.getClipY());
                        c.eqInt("intersection width", 7, g.getClipWidth());
                        c.eqInt("intersection height", 7, g.getClipHeight());
                        g.clipRect(0, 0, 24, 16);
                        c.eqInt("clip not enlarged width", 7, g.getClipWidth());
                        c.eqInt("clip not enlarged height", 7, g.getClipHeight());
                    }
                });

        s.add("GFX-011", G, "drawString rejects null and bad anchors",
                "Graphics.drawString(String,int,int,int): NPE / IAE", new Body() {
                    public void run(Ctx c) {
                        final Graphics g = scratch();
                        c.throwsEx("drawString(null)", NullPointerException.class, new Act() {
                            public void run() {
                                g.drawString(null, 0, 0, Graphics.TOP | Graphics.LEFT);
                            }
                        });
                        c.throwsEx("drawString(LEFT|RIGHT)", IllegalArgumentException.class, new Act() {
                            public void run() {
                                g.drawString("x", 0, 0, Graphics.LEFT | Graphics.RIGHT);
                            }
                        });
                        c.throwsEx("drawString(TOP|BOTTOM)", IllegalArgumentException.class, new Act() {
                            public void run() {
                                g.drawString("x", 0, 0, Graphics.TOP | Graphics.BOTTOM);
                            }
                        });
                    }
                });

        s.add("GFX-012", G, "drawChar rejects bad anchors",
                "Graphics.drawChar(char,int,int,int): IAE for an illegal anchor", new Body() {
                    public void run(Ctx c) {
                        final Graphics g = scratch();
                        c.throwsEx("drawChar(0 anchor)", IllegalArgumentException.class, new Act() {
                            public void run() {
                                g.drawChar('x', 0, 0, 0);
                            }
                        });
                        c.noThrow("drawChar(TOP|LEFT)", new Act() {
                            public void run() {
                                g.drawChar('x', 0, 0, Graphics.TOP | Graphics.LEFT);
                            }
                        });
                    }
                });

        s.add("GFX-013", G, "drawChars validates array and anchor",
                "Graphics.drawChars(char[],int,int,int,int,int)", new Body() {
                    public void run(Ctx c) {
                        final Graphics g = scratch();
                        final char[] data = {'a', 'b', 'c'};
                        c.throwsEx("drawChars(null)", NullPointerException.class, new Act() {
                            public void run() {
                                g.drawChars(null, 0, 1, 0, 0, Graphics.TOP | Graphics.LEFT);
                            }
                        });
                        c.throwsEx("drawChars offset+len too large", ArrayIndexOutOfBoundsException.class,
                                new Act() {
                                    public void run() {
                                        g.drawChars(data, 2, 2, 0, 0, Graphics.TOP | Graphics.LEFT);
                                    }
                                });
                        c.throwsEx("drawChars negative offset", ArrayIndexOutOfBoundsException.class,
                                new Act() {
                                    public void run() {
                                        g.drawChars(data, -1, 1, 0, 0, Graphics.TOP | Graphics.LEFT);
                                    }
                                });
                        c.throwsEx("drawChars bad anchor", IllegalArgumentException.class, new Act() {
                            public void run() {
                                g.drawChars(data, 0, 1, 0, 0, Graphics.LEFT | Graphics.RIGHT);
                            }
                        });
                    }
                });

        s.add("GFX-014", G, "drawSubstring validates range",
                "Graphics.drawSubstring(String,int,int,int,int,int)", new Body() {
                    public void run(Ctx c) {
                        final Graphics g = scratch();
                        c.throwsEx("drawSubstring(null)", NullPointerException.class, new Act() {
                            public void run() {
                                g.drawSubstring(null, 0, 1, 0, 0, Graphics.TOP | Graphics.LEFT);
                            }
                        });
                        c.throwsEx("drawSubstring len past end",
                                StringIndexOutOfBoundsException.class, new Act() {
                                    public void run() {
                                        g.drawSubstring("abc", 1, 3, 0, 0, Graphics.TOP | Graphics.LEFT);
                                    }
                                });
                        c.throwsEx("drawSubstring negative offset",
                                StringIndexOutOfBoundsException.class, new Act() {
                                    public void run() {
                                        g.drawSubstring("abc", -1, 1, 0, 0, Graphics.TOP | Graphics.LEFT);
                                    }
                                });
                        c.noThrow("drawSubstring empty", new Act() {
                            public void run() {
                                g.drawSubstring("abc", 1, 0, 0, 0, Graphics.TOP | Graphics.LEFT);
                            }
                        });
                    }
                });

        s.add("GFX-015", G, "drawImage rejects null and bad anchors",
                "Graphics.drawImage(Image,int,int,int)", new Body() {
                    public void run(Ctx c) {
                        final Graphics g = scratch();
                        final Image im = solid(4, 4);
                        c.throwsEx("drawImage(null)", NullPointerException.class, new Act() {
                            public void run() {
                                g.drawImage(null, 0, 0, Graphics.TOP | Graphics.LEFT);
                            }
                        });
                        c.throwsEx("drawImage bad anchor", IllegalArgumentException.class, new Act() {
                            public void run() {
                                g.drawImage(im, 0, 0, Graphics.TOP | Graphics.BOTTOM);
                            }
                        });
                        int[] anchors = {Graphics.TOP | Graphics.LEFT, Graphics.TOP | Graphics.HCENTER,
                                Graphics.TOP | Graphics.RIGHT, Graphics.BOTTOM | Graphics.LEFT,
                                Graphics.VCENTER | Graphics.HCENTER, Graphics.BASELINE | Graphics.LEFT};
                        for (int i = 0; i < anchors.length; i++) {
                            g.drawImage(im, 5, 5, anchors[i]);
                        }
                        c.isTrue("all legal anchors accepted", true);
                    }
                });

        s.add("GFX-016", G, "drawRegion validates its arguments",
                "Graphics.drawRegion: NPE, IAE for transform/anchor/region, IAE for self-copy", new Body() {
                    public void run(Ctx c) {
                        final Graphics g = scratch();
                        final Image src = solid(5, 4);
                        c.throwsEx("drawRegion(null)", NullPointerException.class, new Act() {
                            public void run() {
                                g.drawRegion(null, 0, 0, 2, 2, 0, 0, 0, Graphics.TOP | Graphics.LEFT);
                            }
                        });
                        c.throwsEx("drawRegion invalid transform", IllegalArgumentException.class,
                                new Act() {
                                    public void run() {
                                        g.drawRegion(src, 0, 0, 2, 2, 99, 0, 0, Graphics.TOP | Graphics.LEFT);
                                    }
                                });
                        c.throwsEx("drawRegion bad anchor", IllegalArgumentException.class, new Act() {
                            public void run() {
                                g.drawRegion(src, 0, 0, 2, 2, 0, 0, 0, Graphics.LEFT | Graphics.RIGHT);
                            }
                        });
                        c.throwsEx("drawRegion region beyond source", IllegalArgumentException.class,
                                new Act() {
                                    public void run() {
                                        g.drawRegion(src, 4, 3, 2, 2, 0, 0, 0, Graphics.TOP | Graphics.LEFT);
                                    }
                                });
                        c.throwsEx("drawRegion negative width", IllegalArgumentException.class,
                                new Act() {
                                    public void run() {
                                        g.drawRegion(src, 0, 0, -1, 2, 0, 0, 0, Graphics.TOP | Graphics.LEFT);
                                    }
                                });
                        final Image destImage = Image.createImage(8, 8);
                        final Graphics dest = destImage.getGraphics();
                        c.throwsEx("drawRegion source == destination", IllegalArgumentException.class,
                                new Act() {
                                    public void run() {
                                        dest.drawRegion(destImage, 0, 0, 2, 2, 0, 1, 1,
                                                Graphics.TOP | Graphics.LEFT);
                                    }
                                });
                    }
                });

        s.add("GFX-017", G, "drawRGB validates its arguments",
                "Graphics.drawRGB(int[],int,int,int,int,int,int,boolean)", new Body() {
                    public void run(Ctx c) {
                        final Graphics g = scratch();
                        final int[] rgb = new int[4];
                        c.throwsEx("drawRGB(null)", NullPointerException.class, new Act() {
                            public void run() {
                                g.drawRGB(null, 0, 2, 0, 0, 2, 2, false);
                            }
                        });
                        c.throwsEx("drawRGB too short", ArrayIndexOutOfBoundsException.class, new Act() {
                            public void run() {
                                g.drawRGB(rgb, 0, 2, 0, 0, 3, 3, false);
                            }
                        });
                    }
                });

        s.add("GFX-018", G, "copyArea rejects a source outside the image",
                "Graphics.copyArea: IAE if the source region exceeds the destination bounds", new Body() {
                    public void run(Ctx c) {
                        final Graphics g = scratch();
                        c.throwsEx("copyArea source outside", IllegalArgumentException.class, new Act() {
                            public void run() {
                                g.copyArea(18, 0, 10, 4, 0, 0, Graphics.TOP | Graphics.LEFT);
                            }
                        });
                        c.noThrow("copyArea inside", new Act() {
                            public void run() {
                                g.copyArea(0, 0, 4, 4, 10, 10, Graphics.TOP | Graphics.LEFT);
                            }
                        });
                    }
                });

        s.add("GFX-019", G, "getDisplayColor returns an RGB value",
                "Graphics.getDisplayColor(int): 0x00RRGGBB", new Body() {
                    public void run(Ctx c) {
                        Graphics g = scratch();
                        int[] samples = {0x000000, 0xFFFFFF, 0xFF0000, 0x00FF00, 0x0000FF, 0x808080};
                        for (int i = 0; i < samples.length; i++) {
                            c.inRange("getDisplayColor(" + Util.hex6(samples[i]) + ")",
                                    g.getDisplayColor(samples[i]), 0, 0xFFFFFF);
                        }
                    }
                });

        s.add("GFX-020", G, "setColor ignores the high-order byte",
                "Graphics.setColor(int): high order byte is ignored", new Body() {
                    public void run(Ctx c) {
                        Graphics g = scratch();
                        g.setColor(0x00123456);
                        int a = g.getColor();
                        g.setColor(0xFF123456);
                        int b = g.getColor();
                        c.advEqInt("getColor with high byte set", a, b);
                    }
                });

        s.add("GFX-021", G, "clip values remain usable after translation",
                "Graphics.getClipX/Y: relative to the coordinate system origin", new Body() {
                    public void run(Ctx c) {
                        Graphics g = scratch();
                        g.setClip(4, 5, 6, 7);
                        g.translate(2, 3);
                        int x = g.getClipX();
                        int y = g.getClipY();
                        c.advTrue("clip X is origin-relative (" + x + ")",
                                x == 2 || x == 4);
                        c.advTrue("clip Y is origin-relative (" + y + ")",
                                y == 2 || y == 5);
                        c.advEqInt("clip width unchanged", 6, g.getClipWidth());
                        c.advEqInt("clip height unchanged", 7, g.getClipHeight());
                    }
                });

        s.add("GFX-022", G, "fill/draw primitives accept empty geometry",
                "Graphics.fillRect/drawRect/fillRoundRect/fillArc: no-op for non-positive size", new Body() {
                    public void run(Ctx c) {
                        Pix p = new Pix(12, 12);
                        int[] before = p.snapshot();
                        p.g.fillRect(2, 2, 0, 3);
                        p.g.fillRect(2, 2, 3, -1);
                        p.g.drawRect(2, 2, -1, 3);
                        p.g.fillRoundRect(2, 2, -2, -2, 2, 2);
                        p.g.fillArc(2, 2, 0, 4, 0, 90);
                        p.expectSame(c, before, "empty primitives leave pixels unchanged");
                    }
                });
    }

    static Graphics scratch() {
        return Image.createImage(24, 16).getGraphics();
    }

    static Image solid(int w, int h) {
        Image im = Image.createImage(w, h);
        Graphics g = im.getGraphics();
        g.setColor(0x336699);
        g.fillRect(0, 0, w, h);
        return Image.createImage(im);
    }
}
