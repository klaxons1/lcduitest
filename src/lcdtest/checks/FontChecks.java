package lcdtest.checks;

import javax.microedition.lcdui.Font;

import lcdtest.Body;
import lcdtest.Ctx;
import lcdtest.Suite;

/** javax.microedition.lcdui.Font contract checks. */
public final class FontChecks {

    private FontChecks() {
    }

    public static void register(Suite s) {
        final String G = "Font";

        s.add("FNT-001", G, "default font is available",
                "Font.getDefaultFont(): returns the system default font", new Body() {
                    public void run(Ctx c) {
                        Font f = Font.getDefaultFont();
                        c.notNull("getDefaultFont()", f);
                        if (f == null) {
                            return;
                        }
                        c.atLeast("height", f.getHeight(), 1);
                        c.atLeast("baseline", f.getBaselinePosition(), 0);
                        c.isTrue("baseline <= height", f.getBaselinePosition() <= f.getHeight());
                    }
                });

        s.add("FNT-002", G, "font attributes belong to the documented sets",
                "Font.getFace/getSize/getStyle", new Body() {
                    public void run(Ctx c) {
                        Font f = Font.getDefaultFont();
                        int face = f.getFace();
                        c.oneOf("face", face, new int[]{Font.FACE_SYSTEM, Font.FACE_MONOSPACE,
                                Font.FACE_PROPORTIONAL});
                        int size = f.getSize();
                        c.oneOf("size", size, new int[]{Font.SIZE_SMALL, Font.SIZE_MEDIUM,
                                Font.SIZE_LARGE});
                        int style = f.getStyle();
                        c.inRange("style", style, 0, Font.STYLE_BOLD | Font.STYLE_ITALIC
                                | Font.STYLE_UNDERLINED);
                        c.eqBool("isPlain", style == 0, f.isPlain());
                        c.eqBool("isBold", (style & Font.STYLE_BOLD) != 0, f.isBold());
                        c.eqBool("isItalic", (style & Font.STYLE_ITALIC) != 0, f.isItalic());
                        c.eqBool("isUnderlined", (style & Font.STYLE_UNDERLINED) != 0,
                                f.isUnderlined());
                    }
                });

        s.add("FNT-003", G, "getFont(face,style,size) returns valid fonts",
                "Font.getFont(int,int,int): always returns a valid font", new Body() {
                    public void run(Ctx c) {
                        int[] faces = {Font.FACE_SYSTEM, Font.FACE_MONOSPACE, Font.FACE_PROPORTIONAL};
                        int[] sizes = {Font.SIZE_SMALL, Font.SIZE_MEDIUM, Font.SIZE_LARGE};
                        int[] styles = {Font.STYLE_PLAIN, Font.STYLE_BOLD, Font.STYLE_ITALIC,
                                Font.STYLE_UNDERLINED,
                                Font.STYLE_BOLD | Font.STYLE_ITALIC,
                                Font.STYLE_BOLD | Font.STYLE_UNDERLINED,
                                Font.STYLE_ITALIC | Font.STYLE_UNDERLINED,
                                Font.STYLE_BOLD | Font.STYLE_ITALIC | Font.STYLE_UNDERLINED};
                        for (int fi = 0; fi < faces.length; fi++) {
                            for (int si = 0; si < sizes.length; si++) {
                                for (int ti = 0; ti < styles.length; ti++) {
                                    Font f = Font.getFont(faces[fi], styles[ti], sizes[si]);
                                    if (f == null) {
                                        c.isTrue("getFont(" + faces[fi] + "," + styles[ti]
                                                + "," + sizes[si] + ") is null", false);
                                        continue;
                                    }
                                    c.oneOf("face of requested " + faces[fi], f.getFace(),
                                            new int[]{Font.FACE_SYSTEM, Font.FACE_MONOSPACE,
                                                    Font.FACE_PROPORTIONAL});
                                    c.oneOf("size of requested " + sizes[si], f.getSize(),
                                            new int[]{Font.SIZE_SMALL, Font.SIZE_MEDIUM,
                                                    Font.SIZE_LARGE});
                                }
                            }
                        }
                    }
                });

        s.add("FNT-004", G, "getFont rejects illegal face, style and size",
                "Font.getFont(int,int,int): IAE for illegal values", new Body() {
                    public void run(Ctx c) {
                        c.throwsEx("bad face", IllegalArgumentException.class, new lcdtest.Act() {
                            public void run() {
                                Font.getFont(12345, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
                            }
                        });
                        c.throwsEx("bad size", IllegalArgumentException.class, new lcdtest.Act() {
                            public void run() {
                                Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, 99);
                            }
                        });
                        c.throwsEx("bad style bit", IllegalArgumentException.class, new lcdtest.Act() {
                            public void run() {
                                Font.getFont(Font.FACE_SYSTEM, 0x40, Font.SIZE_MEDIUM);
                            }
                        });
                    }
                });

        s.add("FNT-005", G, "getFont(fontSpecifier) validates its argument",
                "Font.getFont(int): FONT_STATIC_TEXT / FONT_INPUT_TEXT", new Body() {
                    public void run(Ctx c) {
                        c.notNull("FONT_STATIC_TEXT", Font.getFont(Font.FONT_STATIC_TEXT));
                        c.notNull("FONT_INPUT_TEXT", Font.getFont(Font.FONT_INPUT_TEXT));
                        c.throwsEx("getFont(99)", IllegalArgumentException.class, new lcdtest.Act() {
                            public void run() {
                                Font.getFont(99);
                            }
                        });
                    }
                });

        s.add("FNT-006", G, "character and string widths are non-negative",
                "Font.charWidth/charsWidth/stringWidth", new Body() {
                    public void run(Ctx c) {
                        Font f = Font.getDefaultFont();
                        char[] sample = {'a', 'Z', '0', ' ', '.', '#'};
                        for (int i = 0; i < sample.length; i++) {
                            c.atLeast("charWidth(" + sample[i] + ")", f.charWidth(sample[i]), 0);
                        }
                        c.atLeast("stringWidth(\"Hello\")", f.stringWidth("Hello"), 0);
                        c.atLeast("charsWidth(sample)", f.charsWidth(sample, 0, sample.length), 0);
                        c.atLeast("substringWidth(\"Hello\",1,2)", f.substringWidth("Hello", 1, 2), 0);
                    }
                });

        s.add("FNT-007", G, "zero-length strings have zero width",
                "Font: advance width of an empty string is zero", new Body() {
                    public void run(Ctx c) {
                        Font f = Font.getDefaultFont();
                        c.eqInt("stringWidth(\"\")", 0, f.stringWidth(""));
                        c.eqInt("charsWidth(empty)", 0, f.charsWidth(new char[0], 0, 0));
                        c.eqInt("substringWidth(len 0)", 0, f.substringWidth("abc", 1, 0));
                        c.eqInt("substringWidth(0,0)", 0, f.substringWidth("abc", 0, 0));
                    }
                });

        s.add("FNT-008", G, "string and character widths agree",
                "Font.stringWidth == Font.charsWidth for the same text", new Body() {
                    public void run(Ctx c) {
                        Font f = Font.getDefaultFont();
                        String s = "MIDP 2.0";
                        char[] ch = s.toCharArray();
                        c.eqInt("stringWidth vs charsWidth", f.stringWidth(s),
                                f.charsWidth(ch, 0, ch.length));
                    }
                });

        s.add("FNT-009", G, "substringWidth agrees with stringWidth",
                "Font.substringWidth: width of the specified substring", new Body() {
                    public void run(Ctx c) {
                        Font f = Font.getDefaultFont();
                        String s = "abcdef";
                        c.eqInt("substringWidth(1,3)", f.stringWidth("bcd"),
                                f.substringWidth(s, 1, 3));
                        c.eqInt("substringWidth(0,len)", f.stringWidth(s),
                                f.substringWidth(s, 0, s.length()));
                        c.eqInt("substringWidth whole prefix", f.stringWidth("ab"),
                                f.substringWidth(s, 0, 2));
                    }
                });

        s.add("FNT-010", G, "advance width is additive",
                "Font.stringWidth: inter-character spacing makes widths additive", new Body() {
                    public void run(Ctx c) {
                        Font f = Font.getDefaultFont();
                        String a = "abc";
                        String b = "XYZ";
                        c.eqInt("width(a+b) == width(a)+width(b)",
                                f.stringWidth(a) + f.stringWidth(b), f.stringWidth(a + b));
                    }
                });

        s.add("FNT-011", G, "null arguments are rejected",
                "Font: NPE for null string or char array", new Body() {
                    public void run(Ctx c) {
                        final Font f = Font.getDefaultFont();
                        c.throwsEx("stringWidth(null)", NullPointerException.class, new lcdtest.Act() {
                            public void run() {
                                f.stringWidth(null);
                            }
                        });
                        c.throwsEx("substringWidth(null)", NullPointerException.class, new lcdtest.Act() {
                            public void run() {
                                f.substringWidth(null, 0, 0);
                            }
                        });
                        c.throwsEx("charsWidth(null)", NullPointerException.class, new lcdtest.Act() {
                            public void run() {
                                f.charsWidth(null, 0, 0);
                            }
                        });
                    }
                });

        s.add("FNT-012", G, "bad ranges are rejected",
                "Font: AIOOBE / StringIndexOutOfBoundsException for invalid ranges", new Body() {
                    public void run(Ctx c) {
                        final Font f = Font.getDefaultFont();
                        final char[] ch = {'a', 'b'};
                        c.throwsEx("charsWidth(offset 1 len 2)",
                                ArrayIndexOutOfBoundsException.class, new lcdtest.Act() {
                                    public void run() {
                                        f.charsWidth(ch, 1, 2);
                                    }
                                });
                        c.throwsEx("charsWidth(negative offset)",
                                ArrayIndexOutOfBoundsException.class, new lcdtest.Act() {
                                    public void run() {
                                        f.charsWidth(ch, -1, 1);
                                    }
                                });
                        c.throwsEx("substringWidth(offset 1 len 3)",
                                StringIndexOutOfBoundsException.class, new lcdtest.Act() {
                                    public void run() {
                                        f.substringWidth("abc", 1, 3);
                                    }
                                });
                    }
                });

        s.add("FNT-013", G, "fonts provide a usable line height and baseline",
                "Font.getHeight/getBaselinePosition", new Body() {
                    public void run(Ctx c) {
                        int[] faces = {Font.FACE_SYSTEM, Font.FACE_MONOSPACE, Font.FACE_PROPORTIONAL};
                        int[] sizes = {Font.SIZE_SMALL, Font.SIZE_MEDIUM, Font.SIZE_LARGE};
                        for (int i = 0; i < faces.length; i++) {
                            for (int j = 0; j < sizes.length; j++) {
                                Font f = Font.getFont(faces[i], Font.STYLE_PLAIN, sizes[j]);
                                c.atLeast("height for face/size " + i + "/" + j,
                                        f.getHeight(), 1);
                                c.inRange("baseline for face/size " + i + "/" + j,
                                        f.getBaselinePosition(), 0, f.getHeight());
                            }
                        }
                    }
                });

        s.add("FNT-014", G, "underlined and bold variants are distinct requests",
                "Font.getFont: styles are accepted and reported consistently", new Body() {
                    public void run(Ctx c) {
                        Font plain = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN,
                                Font.SIZE_MEDIUM);
                        Font bold = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD,
                                Font.SIZE_MEDIUM);
                        c.advTrue("bold request is not necessarily wider",
                                bold.stringWidth("Test") >= plain.stringWidth("Test"));
                        c.advTrue("bold and plain differ or match by design",
                                bold.getStyle() != plain.getStyle()
                                        || bold.getHeight() == plain.getHeight());
                    }
                });
    }
}
