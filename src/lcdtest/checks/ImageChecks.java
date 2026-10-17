package lcdtest.checks;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;

import lcdtest.Act;
import lcdtest.Body;
import lcdtest.Ctx;
import lcdtest.Pix;
import lcdtest.Suite;
import lcdtest.Util;

/** javax.microedition.lcdui.Image contract checks. */
public final class ImageChecks {

    private ImageChecks() {
    }

    static int[] pixels(Image im) throws Exception {
        int w = im.getWidth();
        int h = im.getHeight();
        int[] a = new int[w * h];
        im.getRGB(a, 0, w, 0, 0, w, h);
        return a;
    }

    static Image solid(int w, int h, int[] colors) {
        int[] rgb = new int[w * h];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                rgb[y * w + x] = 0xFF000000 | colors[(y * w + x) % colors.length];
            }
        }
        return Image.createRGBImage(rgb, w, h, false);
    }

    /** Expected result of a transform, expressed as destination pixels. */
    static int[] transformed(int[] src, int w, int h, int transform) {
        boolean swap = transform == Sprite.TRANS_ROT90 || transform == Sprite.TRANS_ROT270
                || transform == Sprite.TRANS_MIRROR_ROT90
                || transform == Sprite.TRANS_MIRROR_ROT270;
        int dw = swap ? h : w;
        int dh = swap ? w : h;
        int[] out = new int[dw * dh];
        for (int y = 0; y < dh; y++) {
            for (int x = 0; x < dw; x++) {
                int sx;
                int sy;
                switch (transform) {
                    case Sprite.TRANS_NONE:
                        sx = x; sy = y; break;
                    case Sprite.TRANS_ROT90:
                        sx = y; sy = h - 1 - x; break;
                    case Sprite.TRANS_ROT180:
                        sx = w - 1 - x; sy = h - 1 - y; break;
                    case Sprite.TRANS_ROT270:
                        sx = w - 1 - y; sy = x; break;
                    case Sprite.TRANS_MIRROR:
                        sx = w - 1 - x; sy = y; break;
                    case Sprite.TRANS_MIRROR_ROT90:
                        sx = y; sy = x; break;
                    case Sprite.TRANS_MIRROR_ROT180:
                        sx = x; sy = h - 1 - y; break;
                    default: /* TRANS_MIRROR_ROT270 */
                        sx = w - 1 - y; sy = h - 1 - x; break;
                }
                out[y * dw + x] = src[sy * w + sx];
            }
        }
        return out;
    }

    static boolean equal(int[] a, int[] b) {
        if (a.length != b.length) {
            return false;
        }
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }

    public static void register(Suite s) {
        final String G = "Image";

        s.add("IMG-001", G, "createImage(w,h) returns a white mutable image",
                "Image.createImage(int,int): mutable, initially white", new Body() {
                    public void run(Ctx c) throws Exception {
                        Image im = Image.createImage(7, 5);
                        c.notNull("image", im);
                        if (im == null) {
                            return;
                        }
                        c.eqInt("getWidth", 7, im.getWidth());
                        c.eqInt("getHeight", 5, im.getHeight());
                        c.isTrue("isMutable", im.isMutable());
                        int[] a = pixels(im);
                        c.eqInt("pixel count", 35, a.length);
                        int white = Util.mapped(im.getGraphics(), Util.WHITE) | 0xFF000000;
                        boolean allWhite = true;
                        for (int i = 0; i < a.length; i++) {
                            if ((a[i] & 0xFFFFFF) != (white & 0xFFFFFF)) {
                                allWhite = false;
                            }
                        }
                        c.isTrue("all pixels white, first=" + Util.hex8(a[0]), allWhite);
                    }
                });

        s.add("IMG-002", G, "createImage rejects non-positive sizes",
                "Image.createImage(int,int): IAE if width or height <= 0", new Body() {
                    public void run(Ctx c) {
                        c.throwsEx("width 0", IllegalArgumentException.class, new Act() {
                            public void run() {
                                Image.createImage(0, 4);
                            }
                        });
                        c.throwsEx("height 0", IllegalArgumentException.class, new Act() {
                            public void run() {
                                Image.createImage(4, 0);
                            }
                        });
                        c.throwsEx("width -1", IllegalArgumentException.class, new Act() {
                            public void run() {
                                Image.createImage(-1, 4);
                            }
                        });
                        c.throwsEx("height -1", IllegalArgumentException.class, new Act() {
                            public void run() {
                                Image.createImage(4, -1);
                            }
                        });
                    }
                });

        s.add("IMG-003", G, "getGraphics checks mutability",
                "Image.getGraphics(): mutable returns Graphics, immutable throws ISE", new Body() {
                    public void run(Ctx c) {
                        Image mutable = Image.createImage(4, 4);
                        c.notNull("mutable getGraphics", mutable.getGraphics());
                        final Image immutable = Image.createImage(mutable);
                        c.isFalse("copy isMutable", immutable.isMutable());
                        c.throwsEx("immutable getGraphics", IllegalStateException.class, new Act() {
                            public void run() {
                                immutable.getGraphics();
                            }
                        });
                    }
                });

        s.add("IMG-004", G, "new Graphics has the documented initial state",
                "Image.getGraphics(): full clip, black, default font, SOLID, origin", new Body() {
                    public void run(Ctx c) {
                        Image im = Image.createImage(13, 9);
                        Graphics g = im.getGraphics();
                        c.eqInt("clipX", 0, g.getClipX());
                        c.eqInt("clipY", 0, g.getClipY());
                        c.eqInt("clipWidth", 13, g.getClipWidth());
                        c.eqInt("clipHeight", 9, g.getClipHeight());
                        c.eqInt("translateX", 0, g.getTranslateX());
                        c.eqInt("translateY", 0, g.getTranslateY());
                        c.eqInt("stroke", Graphics.SOLID, g.getStrokeStyle());
                        c.notNull("font", g.getFont());
                        c.same("default font instance", Font.getDefaultFont(), g.getFont());
                        c.eqInt("colour", 0, g.getColor() & 0xFFFFFF);
                    }
                });

        s.add("IMG-005", G, "createImage(Image) copies pixels",
                "Image.createImage(Image): immutable snapshot of the source", new Body() {
                    public void run(Ctx c) throws Exception {
                        Pix p = new Pix(6, 4);
                        int[] expected = p.blank();
                        p.g.setColor(Util.RED);
                        p.g.fillRect(1, 1, 3, 2);
                        p.fill(expected, 1, 1, 3, 2, Util.RED);
                        int[] actual = pixels(p.image);
                        c.isTrue("source painted", equal(expected, actual));
                        Image copy = Image.createImage(p.image);
                        c.isFalse("copy isMutable", copy.isMutable());
                        c.eqInt("copy width", 6, copy.getWidth());
                        c.eqInt("copy height", 4, copy.getHeight());
                        c.isTrue("copy pixels match source", equal(actual, pixels(copy)));
                        p.g.setColor(Util.BLUE);
                        p.g.fillRect(0, 0, 6, 4);
                        c.isTrue("copy unaffected by later source edits",
                                equal(actual, pixels(copy)));
                    }
                });

        s.add("IMG-006", G, "createImage(Image) rejects null",
                "Image.createImage(Image): NullPointerException for null", new Body() {
                    public void run(Ctx c) {
                        c.throwsEx("null source", NullPointerException.class, new Act() {
                            public void run() {
                                Image.createImage((Image) null);
                            }
                        });
                    }
                });

        s.add("IMG-007", G, "createImage(Image,x,y,w,h) copies a region",
                "Image.createImage(Image,int,int,int,int): region copy", new Body() {
                    public void run(Ctx c) throws Exception {
                        int[] cols = {Util.RED, Util.GREEN, Util.BLUE,
                                Util.YELLOW, Util.CYAN, Util.MAGENTA};
                        Image src = solid(3, 2, cols);
                        Image sub = Image.createImage(src, 1, 0, 2, 2, Sprite.TRANS_NONE);
                        c.eqInt("region width", 2, sub.getWidth());
                        c.eqInt("region height", 2, sub.getHeight());
                        c.isFalse("region isMutable", sub.isMutable());
                        int[] got = pixels(sub);
                        int[] srcPix = pixels(src);
                        int[] want = {srcPix[1], srcPix[2], srcPix[4], srcPix[5]};
                        c.isTrue("region pixels", equal(want, got));
                    }
                });

        s.add("IMG-008", G, "region copy rejects out-of-bounds areas",
                "Image.createImage(...transform): IAE outside the source", new Body() {
                    public void run(Ctx c) {
                        final Image src = Image.createImage(4, 4);
                        c.throwsEx("x+w > width", IllegalArgumentException.class, new Act() {
                            public void run() {
                                Image.createImage(src, 1, 0, 4, 2, Sprite.TRANS_NONE);
                            }
                        });
                        c.throwsEx("y+h > height", IllegalArgumentException.class, new Act() {
                            public void run() {
                                Image.createImage(src, 0, 1, 2, 4, Sprite.TRANS_NONE);
                            }
                        });
                        c.throwsEx("negative x", IllegalArgumentException.class, new Act() {
                            public void run() {
                                Image.createImage(src, -1, 0, 2, 2, Sprite.TRANS_NONE);
                            }
                        });
                        c.throwsEx("negative y", IllegalArgumentException.class, new Act() {
                            public void run() {
                                Image.createImage(src, 0, -1, 2, 2, Sprite.TRANS_NONE);
                            }
                        });
                        c.throwsEx("zero width", IllegalArgumentException.class, new Act() {
                            public void run() {
                                Image.createImage(src, 0, 0, 0, 2, Sprite.TRANS_NONE);
                            }
                        });
                        c.throwsEx("zero height", IllegalArgumentException.class, new Act() {
                            public void run() {
                                Image.createImage(src, 0, 0, 2, 0, Sprite.TRANS_NONE);
                            }
                        });
                    }
                });

        s.add("IMG-009", G, "transforms produce the documented geometry",
                "Image.createImage(Image,x,y,w,h,transform): ROT90/270 swap dimensions",
                new Body() {
                    public void run(Ctx c) throws Exception {
                        int[] cols = {Util.RED, Util.GREEN, Util.BLUE,
                                Util.YELLOW, Util.CYAN, Util.MAGENTA};
                        Image src = solid(3, 2, cols);
                        int[] srcPix = pixels(src);
                        int[] ts = {Sprite.TRANS_NONE, Sprite.TRANS_ROT90, Sprite.TRANS_ROT180,
                                Sprite.TRANS_ROT270, Sprite.TRANS_MIRROR,
                                Sprite.TRANS_MIRROR_ROT90, Sprite.TRANS_MIRROR_ROT180,
                                Sprite.TRANS_MIRROR_ROT270};
                        String[] names = {"NONE", "ROT90", "ROT180", "ROT270", "MIRROR",
                                "MIRROR_ROT90", "MIRROR_ROT180", "MIRROR_ROT270"};
                        for (int i = 0; i < ts.length; i++) {
                            Image t = Image.createImage(src, 0, 0, 3, 2, ts[i]);
                            boolean swap = (ts[i] == Sprite.TRANS_ROT90
                                    || ts[i] == Sprite.TRANS_ROT270
                                    || ts[i] == Sprite.TRANS_MIRROR_ROT90
                                    || ts[i] == Sprite.TRANS_MIRROR_ROT270);
                            c.eqInt(names[i] + " width", swap ? 2 : 3, t.getWidth());
                            c.eqInt(names[i] + " height", swap ? 3 : 2, t.getHeight());
                            c.isTrue(names[i] + " pixels",
                                    equal(transformed(srcPix, 3, 2, ts[i]), pixels(t)));
                        }
                    }
                });

        s.add("IMG-010", G, "transforms reject illegal arguments",
                "Image.createImage(...transform): IAE for bad transform or area", new Body() {
                    public void run(Ctx c) {
                        final Image src = Image.createImage(4, 4);
                        c.throwsEx("transform 7", IllegalArgumentException.class, new Act() {
                            public void run() {
                                Image.createImage(src, 0, 0, 4, 4, 7);
                            }
                        });
                        c.throwsEx("transform -1", IllegalArgumentException.class, new Act() {
                            public void run() {
                                Image.createImage(src, 0, 0, 4, 4, -1);
                            }
                        });
                        c.throwsEx("area outside", IllegalArgumentException.class, new Act() {
                            public void run() {
                                Image.createImage(src, 2, 2, 4, 4, Sprite.TRANS_NONE);
                            }
                        });
                        c.throwsEx("null source", NullPointerException.class, new Act() {
                            public void run() {
                                Image.createImage(null, 0, 0, 4, 4, Sprite.TRANS_NONE);
                            }
                        });
                    }
                });

        s.add("IMG-011", G, "createRGBImage keeps pixel colours",
                "Image.createRGBImage(int[],int,int,boolean)", new Body() {
                    public void run(Ctx c) throws Exception {
                        int[] rgb = {0xFFFF0000, 0xFF00FF00, 0xFF0000FF,
                                0xFFFFFFFF, 0xFF000000, 0xFF808080};
                        Image im = Image.createRGBImage(rgb, 3, 2, false);
                        c.eqInt("width", 3, im.getWidth());
                        c.eqInt("height", 2, im.getHeight());
                        c.isFalse("isMutable", im.isMutable());
                        int[] got = pixels(im);
                        int ok = 0;
                        for (int i = 0; i < rgb.length; i++) {
                            if ((got[i] & 0xFFFFFF) == (rgb[i] & 0xFFFFFF)) {
                                ok++;
                            }
                        }
                        c.eqInt("pixels preserved", 6, ok);
                    }
                });

        s.add("IMG-012", G, "createRGBImage validates its arguments",
                "Image.createRGBImage: NPE null, IAE size, AIOOBE short array", new Body() {
                    public void run(Ctx c) {
                        final int[] rgb = new int[4];
                        c.throwsEx("null array", NullPointerException.class, new Act() {
                            public void run() {
                                Image.createRGBImage(null, 2, 2, false);
                            }
                        });
                        c.throwsEx("width 0", IllegalArgumentException.class, new Act() {
                            public void run() {
                                Image.createRGBImage(rgb, 0, 2, false);
                            }
                        });
                        c.throwsEx("height -1", IllegalArgumentException.class, new Act() {
                            public void run() {
                                Image.createRGBImage(rgb, 2, -1, false);
                            }
                        });
                        c.throwsEx("array too short", ArrayIndexOutOfBoundsException.class,
                                new Act() {
                                    public void run() {
                                        Image.createRGBImage(rgb, 3, 3, false);
                                    }
                                });
                    }
                });

        s.add("IMG-013", G, "getRGB round-trips the whole image",
                "Image.getRGB: full image, positive scanlength", new Body() {
                    public void run(Ctx c) throws Exception {
                        Pix p = new Pix(5, 3);
                        p.g.setColor(Util.RED);
                        p.g.fillRect(0, 0, 2, 3);
                        p.g.setColor(Util.BLUE);
                        p.g.fillRect(2, 0, 3, 3);
                        int[] got = new int[15];
                        p.image.getRGB(got, 0, 5, 0, 0, 5, 3);
                        c.isTrue("matches snapshot", equal(p.snapshot(), got));
                    }
                });

        s.add("IMG-014", G, "getRGB reads a sub-region",
                "Image.getRGB: region extraction honours scanlength", new Body() {
                    public void run(Ctx c) throws Exception {
                        int[] cols = {Util.RED, Util.GREEN, Util.BLUE,
                                Util.YELLOW, Util.CYAN, Util.MAGENTA};
                        Image src = solid(3, 2, cols);
                        int[] all = pixels(src);
                        int[] got = new int[4];
                        src.getRGB(got, 0, 2, 1, 0, 2, 2);
                        int[] want = {all[1], all[2], all[4], all[5]};
                        c.isTrue("2x2 region at (1,0)", equal(want, got));
                    }
                });

        s.add("IMG-015", G, "getRGB validates region and scanlength",
                "Image.getRGB: IAE for area outside or scanlength too small", new Body() {
                    public void run(Ctx c) {
                        final Image im = Image.createImage(4, 4);
                        final int[] buf = new int[16];
                        c.throwsEx("x+w > width", IllegalArgumentException.class, new Act() {
                            public void run() {
                                im.getRGB(buf, 0, 4, 1, 0, 4, 2);
                            }
                        });
                        c.throwsEx("y+h > height", IllegalArgumentException.class, new Act() {
                            public void run() {
                                im.getRGB(buf, 0, 4, 0, 1, 2, 4);
                            }
                        });
                        c.throwsEx("negative x", IllegalArgumentException.class, new Act() {
                            public void run() {
                                im.getRGB(buf, 0, 4, -1, 0, 2, 2);
                            }
                        });
                        c.throwsEx("scanlength < width", IllegalArgumentException.class, new Act() {
                            public void run() {
                                im.getRGB(buf, 0, 2, 0, 0, 3, 3);
                            }
                        });
                    }
                });

        s.add("IMG-016", G, "getRGB supports negative scanlength",
                "Image.getRGB: negative scanlength stores rows in reverse order", new Body() {
                    public void run(Ctx c) throws Exception {
                        Pix p = new Pix(4, 3);
                        p.g.setColor(Util.RED);
                        p.g.fillRect(0, 0, 4, 1);
                        p.g.setColor(Util.GREEN);
                        p.g.fillRect(0, 1, 4, 1);
                        p.g.setColor(Util.BLUE);
                        p.g.fillRect(0, 2, 4, 1);
                        int[] plain = p.snapshot();
                        int[] rev = new int[15];
                        p.image.getRGB(rev, 14, -4, 0, 0, 4, 3);
                        boolean ok = true;
                        for (int y = 0; y < 3; y++) {
                            for (int x = 0; x < 4; x++) {
                                if (rev[(2 - y) * 4 + x] != plain[y * 4 + x]) {
                                    ok = false;
                                }
                            }
                        }
                        c.isTrue("rows reversed", ok);
                    }
                });

        s.add("IMG-017", G, "getRGB validates array bounds",
                "Image.getRGB: NPE null array, AIOOBE bad offset/scanlength", new Body() {
                    public void run(Ctx c) {
                        final Image im = Image.createImage(4, 4);
                        c.throwsEx("null array", NullPointerException.class, new Act() {
                            public void run() {
                                im.getRGB(null, 0, 4, 0, 0, 4, 4);
                            }
                        });
                        c.throwsEx("negative offset", ArrayIndexOutOfBoundsException.class,
                                new Act() {
                                    public void run() {
                                        im.getRGB(new int[16], -1, 4, 0, 0, 2, 2);
                                    }
                                });
                        c.throwsEx("offset beyond array", ArrayIndexOutOfBoundsException.class,
                                new Act() {
                                    public void run() {
                                        im.getRGB(new int[16], 14, 4, 0, 0, 2, 2);
                                    }
                                });
                        c.throwsEx("scanlength overruns array",
                                ArrayIndexOutOfBoundsException.class, new Act() {
                                    public void run() {
                                        im.getRGB(new int[16], 0, 8, 0, 0, 4, 4);
                                    }
                                });
                    }
                });

        s.add("IMG-018", G, "writes through getGraphics reach the image",
                "Image: mutable image reflects Graphics drawing immediately", new Body() {
                    public void run(Ctx c) throws Exception {
                        Pix p = new Pix(4, 4);
                        p.g.setColor(Util.MAGENTA);
                        p.g.fillRect(1, 1, 2, 2);
                        int[] expected = p.blank();
                        p.fill(expected, 1, 1, 2, 2, Util.MAGENTA);
                        c.isTrue("pixels painted", equal(expected, p.snapshot()));
                    }
                });

        s.add("IMG-019", G, "copy of an immutable image is equivalent",
                "Image.createImage(Image): same pixels for immutable sources", new Body() {
                    public void run(Ctx c) throws Exception {
                        int[] cols = {Util.RED, Util.GREEN, Util.BLUE, Util.WHITE};
                        Image src = solid(2, 2, cols);
                        Image copy = Image.createImage(src);
                        c.eqInt("width", 2, copy.getWidth());
                        c.eqInt("height", 2, copy.getHeight());
                        c.isTrue("pixels match", equal(pixels(src), pixels(copy)));
                    }
                });

        s.add("IMG-020", G, "region copy of a sub-image is consistent",
                "Image.createImage(...transform): composed on copy of copy", new Body() {
                    public void run(Ctx c) throws Exception {
                        int[] cols = {Util.RED, Util.GREEN, Util.BLUE,
                                Util.YELLOW, Util.CYAN, Util.MAGENTA,
                                Util.BLACK, Util.WHITE, Util.RED};
                        Image src = solid(3, 3, cols);
                        Image once = Image.createImage(src, 1, 1, 2, 2, Sprite.TRANS_NONE);
                        Image twice = Image.createImage(once, 0, 0, 1, 1, Sprite.TRANS_NONE);
                        c.eqInt("once 2x2", 2, once.getWidth());
                        c.eqInt("twice 1x1", 1, twice.getWidth());
                        int[] s = pixels(src);
                        c.eqInt("twice pixel", s[4] & 0xFFFFFF, pixels(twice)[0] & 0xFFFFFF);
                    }
                });
    }
}
