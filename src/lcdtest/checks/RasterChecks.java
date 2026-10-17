package lcdtest.checks;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;

import lcdtest.Body;
import lcdtest.Ctx;
import lcdtest.Host;
import lcdtest.Pix;
import lcdtest.Suite;
import lcdtest.Util;

/**
 * Pixel-level rasterisation conformance.
 *
 * The MIDP 2.0 Graphics specification defines the exact pixels covered
 * by fillRect/drawRect/drawLine, the clip and translation model, image
 * anchors, the eight drawRegion transforms, drawRGB addressing, and
 * getRGB.  These checks verify those rules against the implementation's
 * own colour mapping (Graphics.getDisplayColor).
 */
public final class RasterChecks {

    private RasterChecks() {
    }

    public static void register(Suite s) {
        final String G = "Rasterisation";

        s.add("PIX-001", G, "a new mutable image is white and opaque",
                "Image.createImage(w,h): every pixel is white", new Body() {
                    public void run(Ctx c) {
                        Pix p = new Pix(16, 12);
                        int[] e = p.blank();
                        p.expect(c, e, "new image pixels");
                    }
                });

        s.add("PIX-002", G, "fillRect covers exactly x..x+w-1, y..y+h-1",
                "Graphics.fillRect(int,int,int,int)", new Body() {
                    public void run(Ctx c) {
                        Pix p = new Pix(20, 20);
                        int[] e = p.blank();
                        p.fill(e, 3, 4, 5, 6, Util.BLACK);
                        p.g.setColor(Util.BLACK);
                        p.g.fillRect(3, 4, 5, 6);
                        p.expect(c, e, "fillRect area");
                    }
                });

        s.add("PIX-003", G, "fillRect ignores non-positive sizes",
                "Graphics.fillRect: nothing drawn if width or height <= 0", new Body() {
                    public void run(Ctx c) {
                        Pix p = new Pix(12, 12);
                        int[] before = p.snapshot();
                        p.g.setColor(Util.BLACK);
                        p.g.fillRect(2, 2, 0, 3);
                        p.g.fillRect(2, 2, 3, 0);
                        p.g.fillRect(2, 2, -2, 3);
                        p.g.fillRect(2, 2, 3, -2);
                        p.expectSame(c, before, "fillRect non-positive sizes");
                    }
                });

        s.add("PIX-004", G, "drawRect covers (width+1) by (height+1) pixels",
                "Graphics.drawRect: outline covers width+1 by height+1", new Body() {
                    public void run(Ctx c) {
                        Pix p = new Pix(20, 20);
                        int[] e = p.blank();
                        // Outline of the rectangle at (3,4), width 5, height 4:
                        // x in [3..8], y in [4..8].
                        for (int x = 3; x <= 8; x++) {
                            p.set(e, x, 4, Util.BLACK);
                            p.set(e, x, 8, Util.BLACK);
                        }
                        for (int y = 4; y <= 8; y++) {
                            p.set(e, 3, y, Util.BLACK);
                            p.set(e, 8, y, Util.BLACK);
                        }
                        p.g.setColor(Util.BLACK);
                        p.g.drawRect(3, 4, 5, 4);
                        p.expect(c, e, "drawRect outline");
                    }
                });

        s.add("PIX-005", G, "drawRect ignores negative sizes",
                "Graphics.drawRect: nothing drawn if width or height < 0", new Body() {
                    public void run(Ctx c) {
                        Pix p = new Pix(12, 12);
                        int[] before = p.snapshot();
                        p.g.setColor(Util.BLACK);
                        p.g.drawRect(2, 2, -1, 3);
                        p.g.drawRect(2, 2, 3, -1);
                        p.expectSame(c, before, "drawRect negative sizes");
                    }
                });

        s.add("PIX-006", G, "drawLine sets both endpoints, horizontal",
                "Graphics.drawLine(int,int,int,int)", new Body() {
                    public void run(Ctx c) {
                        Pix p = new Pix(16, 12);
                        int[] e = p.blank();
                        for (int x = 3; x <= 9; x++) {
                            p.set(e, x, 5, Util.BLACK);
                        }
                        p.g.setColor(Util.BLACK);
                        p.g.drawLine(3, 5, 9, 5);
                        p.expect(c, e, "horizontal line");
                    }
                });

        s.add("PIX-007", G, "drawLine sets both endpoints, vertical",
                "Graphics.drawLine(int,int,int,int)", new Body() {
                    public void run(Ctx c) {
                        Pix p = new Pix(16, 12);
                        int[] e = p.blank();
                        for (int y = 3; y <= 9; y++) {
                            p.set(e, 5, y, Util.BLACK);
                        }
                        p.g.setColor(Util.BLACK);
                        p.g.drawLine(5, 3, 5, 9);
                        p.expect(c, e, "vertical line");
                    }
                });

        s.add("PIX-008", G, "drawLine accepts reversed coordinates",
                "Graphics.drawLine: endpoints are symmetric", new Body() {
                    public void run(Ctx c) {
                        Pix p = new Pix(16, 12);
                        int[] e = p.blank();
                        for (int x = 2; x <= 8; x++) {
                            p.set(e, x, 6, Util.BLACK);
                        }
                        p.g.setColor(Util.BLACK);
                        p.g.drawLine(8, 6, 2, 6);
                        p.expect(c, e, "reversed horizontal line");
                    }
                });

        s.add("PIX-009", G, "setClip confines rendering",
                "Graphics.setClip: rendering has no effect outside the clip", new Body() {
                    public void run(Ctx c) {
                        Pix p = new Pix(20, 20);
                        int[] e = p.blank();
                        p.fill(e, 4, 5, 6, 7, Util.BLACK);
                        p.g.setClip(4, 5, 6, 7);
                        p.g.setColor(Util.BLACK);
                        p.g.fillRect(0, 0, 20, 20);
                        p.expect(c, e, "clip restricted fill");
                    }
                });

        s.add("PIX-010", G, "clipRect intersects the current clip",
                "Graphics.clipRect: intersection of current clip and rectangle", new Body() {
                    public void run(Ctx c) {
                        Pix p = new Pix(20, 20);
                        int[] e = p.blank();
                        p.fill(e, 5, 5, 7, 7, Util.BLACK);
                        p.g.setClip(2, 2, 10, 10);
                        p.g.clipRect(5, 5, 10, 10);
                        c.eqInt("clip X", 5, p.g.getClipX());
                        c.eqInt("clip Y", 5, p.g.getClipY());
                        c.eqInt("clip width", 7, p.g.getClipWidth());
                        c.eqInt("clip height", 7, p.g.getClipHeight());
                        p.g.setColor(Util.BLACK);
                        p.g.fillRect(0, 0, 20, 20);
                        p.expect(c, e, "intersected clip fill");
                    }
                });

        s.add("PIX-011", G, "translate moves the drawing origin",
                "Graphics.translate: subsequent coordinates are relative to the new origin", new Body() {
                    public void run(Ctx c) {
                        Pix p = new Pix(20, 20);
                        int[] e = p.blank();
                        p.fill(e, 5, 6, 2, 2, Util.BLACK);
                        p.g.translate(3, 4);
                        p.g.setColor(Util.BLACK);
                        p.g.fillRect(2, 2, 2, 2);
                        p.expect(c, e, "translated fill");
                    }
                });

        s.add("PIX-012", G, "translate is cumulative in rendering",
                "Graphics.translate: calls are cumulative", new Body() {
                    public void run(Ctx c) {
                        Pix p = new Pix(20, 20);
                        int[] e = p.blank();
                        p.fill(e, 7, 8, 1, 1, Util.BLACK);
                        p.g.translate(3, 4);
                        p.g.translate(4, 4);
                        p.g.setColor(Util.BLACK);
                        p.g.fillRect(0, 0, 1, 1);
                        p.expect(c, e, "cumulative translation");
                    }
                });

        s.add("PIX-013", G, "drawImage places the anchor correctly (LEFT|TOP)",
                "Graphics.drawImage: image top-left at the anchor point", new Body() {
                    public void run(Ctx c) {
                        int[] pal = palette(c, 4);
                        if (pal == null) {
                            return;
                        }
                        Src src = makeSrc(4, 3, pal);
                        Pix p = new Pix(20, 20);
                        int[] e = p.blank();
                        Pix.blit(e, 20, src.pixels, src.w, src.h, 0, 0, 10, 10, src.w, src.h);
                        p.g.drawImage(src.image, 10, 10, Graphics.TOP | Graphics.LEFT);
                        p.expect(c, e, "drawImage LEFT|TOP");
                    }
                });

        s.add("PIX-014", G, "drawImage centres and right-aligns the anchor correctly",
                "Graphics.drawImage: HCENTER/VCENTER/RIGHT/BOTTOM anchors", new Body() {
                    public void run(Ctx c) {
                        int[] pal = palette(c, 4);
                        if (pal == null) {
                            return;
                        }
                        Src src = makeSrc(4, 3, pal);
                        Pix p = new Pix(20, 20);
                        int[] e = p.blank();
                        // w/2=2, h/2=1
                        Pix.blit(e, 20, src.pixels, src.w, src.h, 0, 0, 10 - 2, 10 - 1,
                                src.w, src.h);
                        p.g.drawImage(src.image, 10, 10, Graphics.HCENTER | Graphics.VCENTER);
                        p.expect(c, e, "drawImage HCENTER|VCENTER");

                        Pix q = new Pix(20, 20);
                        int[] f = q.blank();
                        Pix.blit(f, 20, src.pixels, src.w, src.h, 0, 0, 10 - 4, 10 - 3,
                                src.w, src.h);
                        q.g.drawImage(src.image, 10, 10, Graphics.RIGHT | Graphics.BOTTOM);
                        q.expect(c, f, "drawImage RIGHT|BOTTOM");
                    }
                });

        s.add("PIX-015", G, "transparent source pixels leave the destination untouched",
                "Graphics.drawImage: transparent pixels leave destination untouched", new Body() {
                    public void run(Ctx c) {
                        int[] rgb = {0x00FF0000, 0xFFFF0000, 0x8000FF00, 0xFF0000FF};
                        Image src = Image.createRGBImage(rgb, 2, 2, true);
                        Pix p = new Pix(20, 20);
                        int[] before = p.snapshot();
                        p.g.drawImage(src, 5, 5, Graphics.TOP | Graphics.LEFT);
                        int[] after = p.snapshot();
                        c.isTrue("transparent pixel untouched",
                                after[5 * 20 + 5] == before[5 * 20 + 5]);
                        c.isTrue("opaque red pixel drawn",
                                (after[5 * 20 + 6] & 0xFFFFFF) == p.mapped(0xFF0000));
                        c.isTrue("opaque blue pixel drawn",
                                (after[6 * 20 + 6] & 0xFFFFFF) == p.mapped(0x0000FF));
                        c.advTrue("semi-transparent pixel was composited over white",
                                after[6 * 20 + 5] != before[6 * 20 + 5]);
                    }
                });

        registerTransforms(s, G);

        s.add("PIX-024", G, "drawRegion honours anchor points",
                "Graphics.drawRegion: anchor point positions the transformed region", new Body() {
                    public void run(Ctx c) {
                        int[] pal = palette(c, 4);
                        if (pal == null) {
                            return;
                        }
                        Src src = makeSrc(2, 2, pal);
                        Pix p = new Pix(20, 20);
                        int[] e = p.blank();
                        Pix.blit(e, 20, src.pixels, src.w, src.h, 0, 0, 10 - 1, 10 - 1, 2, 2);
                        p.g.drawRegion(src.image, 0, 0, 2, 2, Sprite.TRANS_NONE, 10, 10,
                                Graphics.HCENTER | Graphics.VCENTER);
                        p.expect(c, e, "drawRegion centred");
                    }
                });

        s.add("PIX-025", G, "copyArea copies an exact rectangle",
                "Graphics.copyArea: destination receives an exact copy", new Body() {
                    public void run(Ctx c) {
                        Pix p = new Pix(20, 20);
                        int[] e = p.blank();
                        // paint a source block
                        for (int y = 1; y < 6; y++) {
                            for (int x = 1; x < 6; x++) {
                                p.set(e, x, y, ((x + y) & 1) == 0 ? Util.BLACK : Util.RED);
                            }
                        }
                        p.g.setColor(Util.BLACK);
                        p.g.fillRect(1, 1, 5, 5);
                        p.g.setColor(Util.RED);
                        for (int y = 1; y < 6; y++) {
                            for (int x = 1; x < 6; x++) {
                                if (((x + y) & 1) == 0) {
                                    p.g.fillRect(x, y, 1, 1);
                                }
                            }
                        }
                        int[] before = p.snapshot();
                        Pix.blit(e, 20, before, 20, 20, 1, 1, 9, 9, 5, 5);
                        p.g.copyArea(1, 1, 5, 5, 9, 9, Graphics.TOP | Graphics.LEFT);
                        p.expect(c, e, "copyArea");
                    }
                });

        s.add("PIX-026", G, "copyArea handles overlapping regions like a snapshot",
                "Graphics.copyArea: result as if source copied before writing", new Body() {
                    public void run(Ctx c) {
                        Pix p = new Pix(20, 20);
                        for (int y = 2; y < 10; y++) {
                            for (int x = 2; x < 10; x++) {
                                p.g.setColor(((x + y) & 1) == 0 ? Util.BLACK : Util.RED);
                                p.g.fillRect(x, y, 1, 1);
                            }
                        }
                        int[] before = p.snapshot();
                        int[] e = new int[before.length];
                        System.arraycopy(before, 0, e, 0, before.length);
                        Pix.blit(e, 20, before, 20, 20, 2, 2, 5, 5, 6, 6);
                        p.g.copyArea(2, 2, 6, 6, 5, 5, Graphics.TOP | Graphics.LEFT);
                        p.expect(c, e, "overlapping copyArea");
                    }
                });

        s.add("PIX-027", G, "drawRGB honours processAlpha=false",
                "Graphics.drawRGB: alpha ignored when processAlpha is false", new Body() {
                    public void run(Ctx c) {
                        Pix p = new Pix(12, 12);
                        int[] rgb = {0x00FF0000, 0x8000FF00, 0x000000FF, 0xFFFFFFFF};
                        int[] e = p.blank();
                        p.fill(e, 3, 3, 1, 1, 0xFF0000);
                        p.fill(e, 4, 3, 1, 1, 0x00FF00);
                        p.fill(e, 3, 4, 1, 1, 0x0000FF);
                        p.fill(e, 4, 4, 1, 1, 0xFFFFFF);
                        p.g.drawRGB(rgb, 0, 2, 3, 3, 2, 2, false);
                        p.expect(c, e, "drawRGB opaque");
                    }
                });

        s.add("PIX-028", G, "drawRGB honours processAlpha=true",
                "Graphics.drawRGB: alpha 0 leaves destination untouched", new Body() {
                    public void run(Ctx c) {
                        Pix p = new Pix(12, 12);
                        int[] rgb = {0x00FF0000, 0xFFFF0000, 0x0000FF00, 0xFF0000FF};
                        int[] before = p.snapshot();
                        p.g.drawRGB(rgb, 0, 2, 3, 3, 2, 2, true);
                        int[] after = p.snapshot();
                        c.isTrue("transparent first pixel untouched",
                                after[3 * 12 + 3] == before[3 * 12 + 3]);
                        c.isTrue("opaque red drawn",
                                (after[3 * 12 + 4] & 0xFFFFFF) == p.mapped(0xFF0000));
                        c.isTrue("transparent green untouched",
                                after[4 * 12 + 3] == before[4 * 12 + 3]);
                        c.isTrue("opaque blue drawn",
                                (after[4 * 12 + 4] & 0xFFFFFF) == p.mapped(0x0000FF));
                    }
                });

        s.add("PIX-029", G, "drawRGB supports negative scanlength",
                "Graphics.drawRGB: any scanlength is acceptable if references stay in bounds", new Body() {
                    public void run(Ctx c) {
                        Pix p = new Pix(12, 12);
                        int[] rgb = {0x00FF0000, 0x0000FF00, 0x000000FF, 0x00FFFF00};
                        int[] e = p.blank();
                        p.fill(e, 3, 3, 1, 1, 0x0000FF);
                        p.fill(e, 4, 3, 1, 1, 0xFFFF00);
                        p.fill(e, 3, 4, 1, 1, 0xFF0000);
                        p.fill(e, 4, 4, 1, 1, 0x00FF00);
                        // offset 2, scanlength -2: row 0 uses indices 2,3;
                        // row 1 uses indices 0,1.
                        p.g.drawRGB(rgb, 2, -2, 3, 3, 2, 2, false);
                        p.expect(c, e, "drawRGB negative scanlength");
                    }
                });

        s.add("PIX-030", G, "createRGBImage stores the specified pixels",
                "Image.createRGBImage: P(a,b)=rgb[a+b*width]", new Body() {
                    public void run(Ctx c) {
                        int[] rgb = {0xFF112233, 0xFF445566, 0xFF778899, 0xFFAABBCC};
                        Image im = Image.createRGBImage(rgb, 2, 2, false);
                        c.eqInt("width", 2, im.getWidth());
                        c.eqInt("height", 2, im.getHeight());
                        c.isFalse("immutable", im.isMutable());
                        int[] out = new int[4];
                        im.getRGB(out, 0, 2, 0, 0, 2, 2);
                        Pix p = new Pix(4, 4);
                        for (int i = 0; i < 4; i++) {
                            c.eqInt("pixel " + i + " rgb",
                                    p.mapped(rgb[i] & 0xFFFFFF), out[i] & 0xFFFFFF);
                            c.eqInt("pixel " + i + " alpha", 0xFF, (out[i] >>> 24) & 0xFF);
                        }
                    }
                });

        s.add("PIX-031", G, "createImage(Image) copies mutable pixels exactly",
                "Image.createImage(Image): immutable copy of a mutable image", new Body() {
                    public void run(Ctx c) {
                        Pix p = new Pix(12, 10);
                        p.g.setColor(Util.RED);
                        p.g.fillRect(2, 2, 4, 4);
                        p.g.setColor(Util.BLUE);
                        p.g.fillRect(7, 5, 3, 3);
                        Image copy = Image.createImage(p.image);
                        c.isFalse("copy is immutable", copy.isMutable());
                        c.eqInt("copy width", p.w, copy.getWidth());
                        c.eqInt("copy height", p.h, copy.getHeight());
                        int[] a = p.snapshot();
                        int[] b = new int[a.length];
                        copy.getRGB(b, 0, p.w, 0, 0, p.w, p.h);
                        for (int i = 0; i < a.length; i++) {
                            if (a[i] != b[i]) {
                                c.isTrue("pixel mismatch at " + (i % p.w) + "," + (i / p.w)
                                        + " exp=" + Util.hex8(a[i]) + " got=" + Util.hex8(b[i]), false);
                                return;
                            }
                        }
                        c.isTrue("all copied pixels match", true);
                    }
                });

        s.add("PIX-032", G, "createImage(region, ROT90) rotates correctly",
                "Image.createImage(Image,int,int,int,int,int): transformed region", new Body() {
                    public void run(Ctx c) {
                        int[] pal = palette(c, 4);
                        if (pal == null) {
                            return;
                        }
                        Src src = makeSrc(2, 3, pal);
                        Image im = Image.createImage(src.image, 0, 0, src.w, src.h,
                                Sprite.TRANS_ROT90);
                        c.eqInt("rotated width", src.h, im.getWidth());
                        c.eqInt("rotated height", src.w, im.getHeight());
                        int[] out = new int[src.w * src.h];
                        im.getRGB(out, 0, im.getWidth(), 0, 0, im.getWidth(), im.getHeight());
                        for (int y = 0; y < src.h; y++) {
                            for (int x = 0; x < src.w; x++) {
                                int dx = src.h - 1 - y;
                                int dy = x;
                                c.eqInt("rot pixel " + dx + "," + dy,
                                        src.pixels[y * src.w + x], out[dy * im.getWidth() + dx]);
                            }
                        }
                    }
                });

        s.add("PIX-033", G, "PNG resource decodes to the expected pixels",
                "Image.createImage(String): supported image formats such as PNG", new Body() {
                    public void run(Ctx c) {
                        int[] expected = {0xFF0000, 0x00FF00, 0x0000FF, 0xFFFF00, 0x00FFFF, 0xFF00FF};
                        Image im;
                        try {
                            im = Image.createImage("/test.png");
                        } catch (Throwable t) {
                            c.isTrue("load /test.png: " + Ctx.exText(t), false);
                            return;
                        }
                        c.eqInt("png width", 3, im.getWidth());
                        c.eqInt("png height", 2, im.getHeight());
                        c.isFalse("png image immutable", im.isMutable());
                        int[] out = new int[6];
                        im.getRGB(out, 0, 3, 0, 0, 3, 2);
                        Pix p = new Pix(4, 4);
                        for (int i = 0; i < 6; i++) {
                            c.eqInt("png pixel " + i,
                                    p.mapped(expected[i]), out[i] & 0xFFFFFF);
                            c.eqInt("png alpha " + i, 0xFF, (out[i] >>> 24) & 0xFF);
                        }
                    }
                });

        s.add("PIX-034", G, "PNG bytes decode via createImage(byte[])",
                "Image.createImage(byte[],int,int): decode from a byte array", new Body() {
                    public void run(Ctx c) {
                        try {
                            InputStream in = getClass().getResourceAsStream("/test.png");
                            byte[] data = Util.readAll(in);
                            in.close();
                            Image im = Image.createImage(data, 0, data.length);
                            c.eqInt("decoded width", 3, im.getWidth());
                            c.eqInt("decoded height", 2, im.getHeight());
                        } catch (Throwable t) {
                            c.isTrue("PNG byte decode threw " + Ctx.exText(t), false);
                        }
                    }
                });

        s.add("PIX-035", G, "PNG bytes decode via createImage(InputStream)",
                "Image.createImage(InputStream): decode from a stream", new Body() {
                    public void run(Ctx c) {
                        try {
                            InputStream in = getClass().getResourceAsStream("/test.png");
                            Image im = Image.createImage(in);
                            c.eqInt("stream width", 3, im.getWidth());
                            c.eqInt("stream height", 2, im.getHeight());
                        } catch (Throwable t) {
                            c.isTrue("PNG stream decode threw " + Ctx.exText(t), false);
                        }
                    }
                });

        s.add("PIX-036", G, "drawString does not paint an empty string",
                "Graphics.drawString: empty string draws nothing", new Body() {
                    public void run(Ctx c) {
                        Pix p = new Pix(40, 24);
                        int[] before = p.snapshot();
                        p.g.setColor(Util.BLACK);
                        p.g.drawString("", 5, 5, Graphics.TOP | Graphics.LEFT);
                        p.expectSame(c, before, "empty drawString");
                    }
                });

        s.add("PIX-037", G, "drawString ink stays inside the advance box",
                "Graphics.drawString: glyphs are placed relative to the anchor and font metrics", new Body() {
                    public void run(Ctx c) {
                        Pix p = new Pix(60, 30);
                        Font f = Font.getDefaultFont();
                        String text = "Agy";
                        int tw = f.stringWidth(text);
                        int th = f.getHeight();
                        p.g.setColor(Util.BLACK);
                        p.g.drawString(text, 5, 5, Graphics.TOP | Graphics.LEFT);
                        int[] a = p.snapshot();
                        int bg = p.background();
                        int ink = 0;
                        int outside = 0;
                        for (int y = 0; y < p.h; y++) {
                            for (int x = 0; x < p.w; x++) {
                                if ((a[y * p.w + x] & 0xFFFFFF) != bg) {
                                    ink++;
                                    if (x < 5 || x >= 5 + tw || y < 5 || y >= 5 + th) {
                                        outside++;
                                    }
                                }
                            }
                        }
                        c.advTrue("text produced ink", ink > 0);
                        c.isTrue("no ink outside the advance box (" + outside + ")", outside == 0);
                    }
                });

        s.add("PIX-038", G, "drawString anchors are mutually consistent",
                "Graphics.drawString: LEFT and RIGHT anchors differ by the string width", new Body() {
                    public void run(Ctx c) {
                        Font f = Font.getDefaultFont();
                        String text = "Hi";
                        int tw = f.stringWidth(text);
                        Pix a = new Pix(80, 24);
                        a.g.setColor(Util.BLACK);
                        a.g.drawString(text, 5, 5, Graphics.TOP | Graphics.LEFT);
                        Pix b = new Pix(80, 24);
                        b.g.setColor(Util.BLACK);
                        b.g.drawString(text, 5 + tw, 5, Graphics.TOP | Graphics.RIGHT);
                        int[] pa = a.snapshot();
                        int[] pb = b.snapshot();
                        int bg = a.background();
                        int bad = 0;
                        for (int y = 0; y < a.h; y++) {
                            for (int x = 0; x < a.w; x++) {
                                int va = pa[y * a.w + x] & 0xFFFFFF;
                                int vb = pb[y * b.w + x] & 0xFFFFFF;
                                // Both should agree except that antialiasing may
                                // differ slightly; compare ink presence.
                                boolean inkA = va != bg;
                                boolean inkB = vb != bg;
                                if (inkA != inkB) {
                                    bad++;
                                }
                            }
                        }
                        c.advTrue("LEFT and RIGHT anchors render the same ink (" + bad
                                + " differing pixels)", bad <= 2);
                    }
                });

        s.add("PIX-039", G, "fillTriangle stays within its bounding box",
                "Graphics.fillTriangle: lines connecting the vertices are included", new Body() {
                    public void run(Ctx c) {
                        Pix p = new Pix(20, 20);
                        int[] before = p.snapshot();
                        p.g.setColor(Util.BLACK);
                        p.g.fillTriangle(2, 2, 12, 2, 2, 12);
                        int[] a = p.snapshot();
                        int bg = p.background();
                        int outside = 0;
                        for (int y = 0; y < p.h; y++) {
                            for (int x = 0; x < p.w; x++) {
                                boolean ink = (a[y * p.w + x] & 0xFFFFFF) != bg;
                                if (ink && (x < 2 || y < 2 || x > 12 || y > 12)) {
                                    outside++;
                                }
                            }
                        }
                        c.isTrue("no ink outside bounding box (" + outside + ")", outside == 0);
                        c.advTrue("interior pixel filled",
                                (a[4 * p.w + 4] & 0xFFFFFF) != bg);
                        c.advTrue("far corner outside triangle untouched",
                                (a[11 * p.w + 11] & 0xFFFFFF) == bg);
                        boolean changed = false;
                        for (int i = 0; i < a.length; i++) {
                            if (a[i] != before[i]) {
                                changed = true;
                                break;
                            }
                        }
                        c.isTrue("triangle drew something", changed);
                    }
                });

        s.add("PIX-040", G, "arcs stay within their bounding box",
                "Graphics.drawArc/fillArc: arc covers width+1 by height+1 at most", new Body() {
                    public void run(Ctx c) {
                        Pix p = new Pix(24, 24);
                        p.g.setColor(Util.BLACK);
                        p.g.drawArc(3, 4, 12, 10, 0, 180);
                        p.g.fillArc(3, 4, 12, 10, 180, 180);
                        int[] a = p.snapshot();
                        int bg = p.background();
                        int outside = 0;
                        for (int y = 0; y < p.h; y++) {
                            for (int x = 0; x < p.w; x++) {
                                boolean ink = (a[y * p.w + x] & 0xFFFFFF) != bg;
                                if (ink && (x < 3 || y < 4 || x > 15 || y > 14)) {
                                    outside++;
                                }
                            }
                        }
                        c.advTrue("no ink outside arc bounds (" + outside + ")", outside == 0);
                    }
                });

        s.add("PIX-041", G, "rounded rectangle corners are rounded",
                "Graphics.fillRoundRect: arcWidth/arcHeight control the corners", new Body() {
                    public void run(Ctx c) {
                        Pix p = new Pix(24, 24);
                        p.g.setColor(Util.BLACK);
                        p.g.fillRoundRect(4, 4, 14, 14, 8, 8);
                        int[] a = p.snapshot();
                        int bg = p.background();
                        c.advTrue("top-left corner pixel is not filled",
                                (a[4 * p.w + 4] & 0xFFFFFF) == bg);
                        c.advTrue("centre is filled",
                                (a[11 * p.w + 11] & 0xFFFFFF) != bg);
                        c.advTrue("edge midpoint is filled",
                                (a[4 * p.w + 11] & 0xFFFFFF) != bg);
                    }
                });
    }

    private static void registerTransforms(Suite s, final String G) {
        final int[] transforms = {Sprite.TRANS_NONE, Sprite.TRANS_ROT90, Sprite.TRANS_ROT180,
                Sprite.TRANS_ROT270, Sprite.TRANS_MIRROR, Sprite.TRANS_MIRROR_ROT90,
                Sprite.TRANS_MIRROR_ROT180, Sprite.TRANS_MIRROR_ROT270};
        final String[] names = {"NONE", "ROT90", "ROT180", "ROT270", "MIRROR",
                "MIRROR_ROT90", "MIRROR_ROT180", "MIRROR_ROT270"};
        for (int t = 0; t < transforms.length; t++) {
            final int transform = transforms[t];
            final String name = names[t];
            final int base = 16 + t;
            s.add("PIX-0" + base, G, "drawRegion transform " + name + " places pixels exactly",
                    "Sprite.TRANS_" + name + " / Graphics.drawRegion", new Body() {
                        public void run(Ctx c) {
                            int[] pal = palette(c, 6);
                            if (pal == null) {
                                return;
                            }
                            Src src = makeSrc(2, 3, pal);
                            int tw = (transform == Sprite.TRANS_ROT90
                                    || transform == Sprite.TRANS_ROT270
                                    || transform == Sprite.TRANS_MIRROR_ROT90
                                    || transform == Sprite.TRANS_MIRROR_ROT270) ? src.h : src.w;
                            int th = (transform == Sprite.TRANS_ROT90
                                    || transform == Sprite.TRANS_ROT270
                                    || transform == Sprite.TRANS_MIRROR_ROT90
                                    || transform == Sprite.TRANS_MIRROR_ROT270) ? src.w : src.h;
                            Pix p = new Pix(24, 24);
                            int[] e = p.blank();
                            for (int y = 0; y < src.h; y++) {
                                for (int x = 0; x < src.w; x++) {
                                    int dx;
                                    int dy;
                                    switch (transform) {
                                        case Sprite.TRANS_NONE:
                                            dx = x; dy = y; break;
                                        case Sprite.TRANS_ROT90:
                                            dx = src.h - 1 - y; dy = x; break;
                                        case Sprite.TRANS_ROT180:
                                            dx = src.w - 1 - x; dy = src.h - 1 - y; break;
                                        case Sprite.TRANS_ROT270:
                                            dx = y; dy = src.w - 1 - x; break;
                                        case Sprite.TRANS_MIRROR:
                                            dx = src.w - 1 - x; dy = y; break;
                                        case Sprite.TRANS_MIRROR_ROT90:
                                            dx = src.h - 1 - y; dy = src.w - 1 - x; break;
                                        case Sprite.TRANS_MIRROR_ROT180:
                                            dx = x; dy = src.h - 1 - y; break;
                                        case Sprite.TRANS_MIRROR_ROT270:
                                            dx = y; dy = x; break;
                                        default:
                                            dx = x; dy = y; break;
                                    }
                                    e[(5 + dy) * p.w + (5 + dx)] = src.pixels[y * src.w + x];
                                }
                            }
                            p.g.drawRegion(src.image, 0, 0, src.w, src.h, transform,
                                    5, 5, Graphics.TOP | Graphics.LEFT);
                            p.expect(c, e, "drawRegion " + name + " ("
                                    + tw + "x" + th + ")");
                        }
                    });
        }
    }

    static int[] palette(Ctx c, int need) {
        int[] candidates = {Util.WHITE, Util.BLACK, Util.RED, Util.GREEN, Util.BLUE,
                Util.YELLOW, Util.MAGENTA, Util.CYAN};
        int[] p = Util.distinctPalette(Host.display, candidates, need);
        if (p == null) {
            c.skip("device palette cannot represent " + need + " distinct test colours");
        }
        return p;
    }

    static final class Src {
        Image image;
        int[] pixels;
        int w;
        int h;
    }

    /** Build an immutable source image with a distinctive colour per pixel. */
    static Src makeSrc(int w, int h, int[] colors) {
        Image im = Image.createImage(w, h);
        Graphics g = im.getGraphics();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                g.setColor(colors[(y * w + x) % colors.length]);
                g.fillRect(x, y, 1, 1);
            }
        }
        Src s = new Src();
        s.image = Image.createImage(im);
        s.w = w;
        s.h = h;
        s.pixels = new int[w * h];
        s.image.getRGB(s.pixels, 0, w, 0, 0, w, h);
        return s;
    }

    static byte[] pngBytes() throws java.io.IOException {
        InputStream in = RasterChecks.class.getResourceAsStream("/test.png");
        if (in == null) {
            throw new java.io.IOException("no /test.png");
        }
        byte[] b = Util.readAll(in);
        in.close();
        return b;
    }

    static Image fromBytes(byte[] b) {
        return Image.createImage(b, 0, b.length);
    }

    static InputStream stream(byte[] b) {
        return new ByteArrayInputStream(b);
    }
}
