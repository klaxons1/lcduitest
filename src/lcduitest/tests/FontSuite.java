/**
 * LcduiTest - MIDP 2.0 LCDUI conformance test MIDlet.
 *
 * javax.microedition.lcdui.Font
 */
package lcduitest.tests;

import javax.microedition.lcdui.Font;

import lcduitest.Assert;
import lcduitest.TestCase;
import lcduitest.TestSuite;

public class FontSuite extends TestSuite {

    private static final int[] FACES = {Font.FACE_SYSTEM, Font.FACE_MONOSPACE,
        Font.FACE_PROPORTIONAL};
    private static final String[] FACE_NAMES = {"FACE_SYSTEM", "FACE_MONOSPACE",
        "FACE_PROPORTIONAL"};
    private static final int[] SIZES = {Font.SIZE_SMALL, Font.SIZE_MEDIUM, Font.SIZE_LARGE};
    private static final String[] SIZE_NAMES = {"SIZE_SMALL", "SIZE_MEDIUM", "SIZE_LARGE"};
    private static final int[] STYLES = {Font.STYLE_PLAIN, Font.STYLE_BOLD, Font.STYLE_ITALIC,
        Font.STYLE_UNDERLINED, Font.STYLE_BOLD | Font.STYLE_ITALIC};
    private static final String[] STYLE_NAMES = {"STYLE_PLAIN", "STYLE_BOLD", "STYLE_ITALIC",
        "STYLE_UNDERLINED", "STYLE_BOLD|STYLE_ITALIC"};

    public FontSuite() {
        super("Font", "Fonts", "getFont, the face / style / size matrix, metrics, width "
                + "measurement and the argument checks");

        add(new TestCase("default_font") {
            public void run() {
                Font font = Font.getDefaultFont();
                Assert.assertNotNull("Font.getDefaultFont()", font);
                int height = font.getHeight();
                int baseline = font.getBaselinePosition();
                Assert.info("default font", "face=" + font.getFace() + " style=" + font.getStyle()
                        + " size=" + font.getSize() + " height=" + height
                        + " baseline=" + baseline);
                Assert.assertTrue("the height of a font must be positive but was " + height,
                        height > 0);
                Assert.assertTrue("the baseline must be positive but was " + baseline,
                        baseline > 0);
                Assert.assertTrue("the baseline (" + baseline + ") must not be below the "
                        + "height (" + height + ")", baseline <= height);
            }
        });

        add(new TestCase("font_specifiers") {
            public void run() {
                Font staticText = Font.getFont(Font.FONT_STATIC_TEXT);
                Font inputText = Font.getFont(Font.FONT_INPUT_TEXT);
                Assert.assertNotNull("getFont(FONT_STATIC_TEXT)", staticText);
                Assert.assertNotNull("getFont(FONT_INPUT_TEXT)", inputText);
                Assert.info("static text font height", staticText.getHeight());
                Assert.info("input text font height", inputText.getHeight());
                Assert.assertTrue("the static text font must have a positive height",
                        staticText.getHeight() > 0);
                Assert.assertTrue("the input text font must have a positive height",
                        inputText.getHeight() > 0);
            }
        });

        add(new TestCase("font_specifier_rejects_invalid_value") {
            public void run() {
                Assert.expectException("getFont(42)", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                Font.getFont(42);
                            }
                        });
            }
        });

        add(new TestCase("face_style_size_matrix") {
            public void run() {
                for (int f = 0; f < FACES.length; f++) {
                    for (int s = 0; s < STYLES.length; s++) {
                        for (int z = 0; z < SIZES.length; z++) {
                            Font font = Font.getFont(FACES[f], STYLES[s], SIZES[z]);
                            String what = FACE_NAMES[f] + "/" + STYLE_NAMES[s] + "/"
                                    + SIZE_NAMES[z];
                            Assert.assertNotNull("getFont(" + what + ")", font);
                            Assert.assertEquals("getFace of " + what, FACES[f], font.getFace());
                            Assert.assertEquals("getStyle of " + what, STYLES[s], font.getStyle());
                            Assert.assertEquals("getSize of " + what, SIZES[z], font.getSize());
                            Assert.assertTrue("the height of " + what + " must be positive",
                                    font.getHeight() > 0);
                            boolean bold = (STYLES[s] & Font.STYLE_BOLD) != 0;
                            boolean italic = (STYLES[s] & Font.STYLE_ITALIC) != 0;
                            boolean underlined = (STYLES[s] & Font.STYLE_UNDERLINED) != 0;
                            Assert.assertEquals("isBold of " + what, bold, font.isBold());
                            Assert.assertEquals("isItalic of " + what, italic, font.isItalic());
                            Assert.assertEquals("isUnderlined of " + what, underlined,
                                    font.isUnderlined());
                            Assert.assertEquals("isPlain of " + what, STYLES[s] == Font.STYLE_PLAIN,
                                    font.isPlain());
                        }
                    }
                }
            }
        }.severity(TestCase.SHOULD).describe("getFace / getStyle / getSize must report the "
                + "requested values and the isXxx helpers must agree with the style bits"));

        add(new TestCase("invalid_face_style_or_size") {
            public void run() {
                Assert.expectException("getFont(99, STYLE_PLAIN, SIZE_MEDIUM)",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                Font.getFont(99, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
                            }
                        });
                Assert.expectException("getFont(FACE_SYSTEM, 99, SIZE_MEDIUM)",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                Font.getFont(Font.FACE_SYSTEM, 99, Font.SIZE_MEDIUM);
                            }
                        });
                Assert.expectException("getFont(FACE_SYSTEM, STYLE_PLAIN, 99)",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, 99);
                            }
                        });
            }
        });

        add(new TestCase("char_and_string_width") {
            public void run() {
                Font font = Font.getDefaultFont();
                int w = font.charWidth('W');
                Assert.info("charWidth('W')", w);
                Assert.info("charWidth(' ')", font.charWidth(' '));
                Assert.info("stringWidth(\"Wg\")", font.stringWidth("Wg"));
                Assert.assertTrue("charWidth('W') must be positive but was " + w, w > 0);
                Assert.assertTrue("charWidth(' ') must not be negative",
                        font.charWidth(' ') >= 0);
                Assert.assertTrue("the width of \"WW\" must not be smaller than one 'W'",
                        font.stringWidth("WW") >= w);
                Assert.assertEquals("the width of the empty string", 0, font.stringWidth(""));
            }
        }.severity(TestCase.SHOULD));

        add(new TestCase("charsWidth_matches_stringWidth") {
            public void run() {
                Font font = Font.getDefaultFont();
                char[] chars = "LCDUI tester 0123456789".toCharArray();
                for (int offset = 0; offset < chars.length; offset += 4) {
                    int length = Math.min(6, chars.length - offset);
                    int byChars = font.charsWidth(chars, offset, length);
                    int byString = font.stringWidth(new String(chars, offset, length));
                    Assert.assertEquals("charsWidth(chars," + offset + "," + length + ") must "
                            + "equal stringWidth of the same characters", byString, byChars);
                }
            }
        });

        add(new TestCase("substringWidth_matches_stringWidth") {
            public void run() {
                Font font = Font.getDefaultFont();
                String text = "substring width check";
                for (int offset = 0; offset < text.length(); offset += 5) {
                    int length = Math.min(7, text.length() - offset);
                    int bySubstring = font.substringWidth(text, offset, length);
                    int byString = font.stringWidth(text.substring(offset, offset + length));
                    Assert.assertEquals("substringWidth(" + offset + "," + length + ") must "
                            + "equal stringWidth of that substring", byString, bySubstring);
                }
            }
        }.severity(TestCase.SHOULD));

        add(new TestCase("width_measurement_rejects_null") {
            public void run() {
                final Font font = Font.getDefaultFont();
                Assert.expectException("stringWidth(null)", NullPointerException.class,
                        new Assert.Code() {
                            public void run() {
                                font.stringWidth(null);
                            }
                        });
                Assert.expectException("substringWidth(null, 0, 1)", NullPointerException.class,
                        new Assert.Code() {
                            public void run() {
                                font.substringWidth(null, 0, 1);
                            }
                        });
            }
        });

        add(new TestCase("font_object_identity_and_pool") {
            public void run() {
                Font a = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
                Font b = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
                Assert.info("getFont with equal arguments returns the same instance",
                        a == b);
                Assert.assertNotNull("the font is usable", a.toString());
            }
        }.severity(TestCase.INFO).describe("MIDP 2.0 does not require font objects to be "
                + "cached, the observation is reported"));
    }
}
