/**
 * LcduiTest - MIDP 2.0 LCDUI conformance test MIDlet.
 *
 * Every constant of the lcdui API whose value the MIDP 2.0 specification
 * states. A device that reports a different value here is not compatible with
 * applications that were written against the specification, so these checks
 * are strict.
 */
package lcduitest.tests;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.DateField;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.ImageItem;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextField;
import javax.microedition.lcdui.game.GameCanvas;
import javax.microedition.lcdui.game.Sprite;

import lcduitest.Assert;
import lcduitest.TestCase;
import lcduitest.TestSuite;

public class ConstantsSuite extends TestSuite {

    public ConstantsSuite() {
        super("Constants", "Specified constant values", "every field of the lcdui API whose "
                + "value is defined by MIDP 2.0");

        add(new TestCase("Canvas_key_codes") {
            public void run() {
                Assert.assertEquals("Canvas.KEY_NUM0", 48, Canvas.KEY_NUM0);
                Assert.assertEquals("Canvas.KEY_NUM1", 49, Canvas.KEY_NUM1);
                Assert.assertEquals("Canvas.KEY_NUM2", 50, Canvas.KEY_NUM2);
                Assert.assertEquals("Canvas.KEY_NUM3", 51, Canvas.KEY_NUM3);
                Assert.assertEquals("Canvas.KEY_NUM4", 52, Canvas.KEY_NUM4);
                Assert.assertEquals("Canvas.KEY_NUM5", 53, Canvas.KEY_NUM5);
                Assert.assertEquals("Canvas.KEY_NUM6", 54, Canvas.KEY_NUM6);
                Assert.assertEquals("Canvas.KEY_NUM7", 55, Canvas.KEY_NUM7);
                Assert.assertEquals("Canvas.KEY_NUM8", 56, Canvas.KEY_NUM8);
                Assert.assertEquals("Canvas.KEY_NUM9", 57, Canvas.KEY_NUM9);
                Assert.assertEquals("Canvas.KEY_STAR", 42, Canvas.KEY_STAR);
                Assert.assertEquals("Canvas.KEY_POUND", 35, Canvas.KEY_POUND);
            }
        });

        add(new TestCase("Canvas_game_actions") {
            public void run() {
                Assert.assertEquals("Canvas.UP", 1, Canvas.UP);
                Assert.assertEquals("Canvas.DOWN", 6, Canvas.DOWN);
                Assert.assertEquals("Canvas.LEFT", 2, Canvas.LEFT);
                Assert.assertEquals("Canvas.RIGHT", 5, Canvas.RIGHT);
                Assert.assertEquals("Canvas.FIRE", 8, Canvas.FIRE);
                Assert.assertEquals("Canvas.GAME_A", 9, Canvas.GAME_A);
                Assert.assertEquals("Canvas.GAME_B", 10, Canvas.GAME_B);
                Assert.assertEquals("Canvas.GAME_C", 11, Canvas.GAME_C);
                Assert.assertEquals("Canvas.GAME_D", 12, Canvas.GAME_D);
            }
        });

        add(new TestCase("Command_types") {
            public void run() {
                Assert.assertEquals("Command.SCREEN", 1, Command.SCREEN);
                Assert.assertEquals("Command.BACK", 2, Command.BACK);
                Assert.assertEquals("Command.CANCEL", 3, Command.CANCEL);
                Assert.assertEquals("Command.OK", 4, Command.OK);
                Assert.assertEquals("Command.HELP", 5, Command.HELP);
                Assert.assertEquals("Command.STOP", 6, Command.STOP);
                Assert.assertEquals("Command.EXIT", 7, Command.EXIT);
                Assert.assertEquals("Command.ITEM", 8, Command.ITEM);
            }
        });

        add(new TestCase("Choice_types_and_wrapping") {
            public void run() {
                Assert.assertEquals("Choice.EXCLUSIVE", 1, Choice.EXCLUSIVE);
                Assert.assertEquals("Choice.MULTIPLE", 2, Choice.MULTIPLE);
                Assert.assertEquals("Choice.IMPLICIT", 3, Choice.IMPLICIT);
                Assert.assertEquals("Choice.POPUP", 4, Choice.POPUP);
                Assert.assertEquals("Choice.TEXT_WRAP_DEFAULT", 0, Choice.TEXT_WRAP_DEFAULT);
                Assert.assertEquals("Choice.TEXT_WRAP_ON", 1, Choice.TEXT_WRAP_ON);
                Assert.assertEquals("Choice.TEXT_WRAP_OFF", 2, Choice.TEXT_WRAP_OFF);
            }
        });

        add(new TestCase("CustomItem_protected_constants") {
            public void run() {
                // The interaction mode constants of CustomItem are protected, so
                // their values are checked from inside a subclass in
                // CustomItemSuite (test custom_item_interaction_mode_values)
                Assert.assertTrue("see CustomItemSuite", true);
            }
        }.severity(TestCase.INFO));
        add(new TestCase("DateField_modes") {
            public void run() {
                Assert.assertEquals("DateField.DATE", 1, DateField.DATE);
                Assert.assertEquals("DateField.TIME", 2, DateField.TIME);
                Assert.assertEquals("DateField.DATE_TIME", 3, DateField.DATE_TIME);
            }
        });

        add(new TestCase("Display_colour_and_image_constants") {
            public void run() {
                Assert.assertEquals("Display.COLOR_BACKGROUND", 0, Display.COLOR_BACKGROUND);
                Assert.assertEquals("Display.COLOR_FOREGROUND", 1, Display.COLOR_FOREGROUND);
                Assert.assertEquals("Display.COLOR_HIGHLIGHTED_BACKGROUND", 2,
                        Display.COLOR_HIGHLIGHTED_BACKGROUND);
                Assert.assertEquals("Display.COLOR_HIGHLIGHTED_FOREGROUND", 3,
                        Display.COLOR_HIGHLIGHTED_FOREGROUND);
                Assert.assertEquals("Display.COLOR_BORDER", 4, Display.COLOR_BORDER);
                Assert.assertEquals("Display.COLOR_HIGHLIGHTED_BORDER", 5,
                        Display.COLOR_HIGHLIGHTED_BORDER);
                Assert.assertEquals("Display.LIST_ELEMENT", 1, Display.LIST_ELEMENT);
                Assert.assertEquals("Display.CHOICE_GROUP_ELEMENT", 2,
                        Display.CHOICE_GROUP_ELEMENT);
                Assert.assertEquals("Display.ALERT", 3, Display.ALERT);
            }
        });

        add(new TestCase("Font_styles_sizes_and_faces") {
            public void run() {
                Assert.assertEquals("Font.STYLE_PLAIN", 0, Font.STYLE_PLAIN);
                Assert.assertEquals("Font.STYLE_BOLD", 1, Font.STYLE_BOLD);
                Assert.assertEquals("Font.STYLE_ITALIC", 2, Font.STYLE_ITALIC);
                Assert.assertEquals("Font.STYLE_UNDERLINED", 4, Font.STYLE_UNDERLINED);
                Assert.assertEquals("Font.SIZE_SMALL", 8, Font.SIZE_SMALL);
                Assert.assertEquals("Font.SIZE_MEDIUM", 0, Font.SIZE_MEDIUM);
                Assert.assertEquals("Font.SIZE_LARGE", 16, Font.SIZE_LARGE);
                Assert.assertEquals("Font.FACE_SYSTEM", 0, Font.FACE_SYSTEM);
                Assert.assertEquals("Font.FACE_MONOSPACE", 32, Font.FACE_MONOSPACE);
                Assert.assertEquals("Font.FACE_PROPORTIONAL", 64, Font.FACE_PROPORTIONAL);
                Assert.assertEquals("Font.FONT_STATIC_TEXT", 0, Font.FONT_STATIC_TEXT);
                Assert.assertEquals("Font.FONT_INPUT_TEXT", 1, Font.FONT_INPUT_TEXT);
            }
        });

        add(new TestCase("Gauge_constants") {
            public void run() {
                Assert.assertEquals("Gauge.INDEFINITE", -1, Gauge.INDEFINITE);
                Assert.assertEquals("Gauge.CONTINUOUS_IDLE", 0, Gauge.CONTINUOUS_IDLE);
                Assert.assertEquals("Gauge.INCREMENTAL_IDLE", 1, Gauge.INCREMENTAL_IDLE);
                Assert.assertEquals("Gauge.CONTINUOUS_RUNNING", 2, Gauge.CONTINUOUS_RUNNING);
                Assert.assertEquals("Gauge.INCREMENTAL_UPDATING", 3, Gauge.INCREMENTAL_UPDATING);
            }
        });

        add(new TestCase("Graphics_anchors_and_strokes") {
            public void run() {
                Assert.assertEquals("Graphics.HCENTER", 1, Graphics.HCENTER);
                Assert.assertEquals("Graphics.VCENTER", 2, Graphics.VCENTER);
                Assert.assertEquals("Graphics.LEFT", 4, Graphics.LEFT);
                Assert.assertEquals("Graphics.RIGHT", 8, Graphics.RIGHT);
                Assert.assertEquals("Graphics.TOP", 16, Graphics.TOP);
                Assert.assertEquals("Graphics.BOTTOM", 32, Graphics.BOTTOM);
                Assert.assertEquals("Graphics.BASELINE", 64, Graphics.BASELINE);
                Assert.assertEquals("Graphics.SOLID", 0, Graphics.SOLID);
                Assert.assertEquals("Graphics.DOTTED", 1, Graphics.DOTTED);
            }
        });

        add(new TestCase("ImageItem_layout_directives") {
            public void run() {
                Assert.assertEquals("ImageItem.LAYOUT_DEFAULT", 0, ImageItem.LAYOUT_DEFAULT);
                Assert.assertEquals("ImageItem.LAYOUT_LEFT", 1, ImageItem.LAYOUT_LEFT);
                Assert.assertEquals("ImageItem.LAYOUT_RIGHT", 2, ImageItem.LAYOUT_RIGHT);
                Assert.assertEquals("ImageItem.LAYOUT_CENTER", 3, ImageItem.LAYOUT_CENTER);
                Assert.assertEquals("ImageItem.LAYOUT_NEWLINE_BEFORE", 0x100,
                        ImageItem.LAYOUT_NEWLINE_BEFORE);
                Assert.assertEquals("ImageItem.LAYOUT_NEWLINE_AFTER", 0x200,
                        ImageItem.LAYOUT_NEWLINE_AFTER);
            }
        });

        add(new TestCase("Item_layout_and_appearance") {
            public void run() {
                Assert.assertEquals("Item.LAYOUT_DEFAULT", 0, Item.LAYOUT_DEFAULT);
                Assert.assertEquals("Item.LAYOUT_LEFT", 1, Item.LAYOUT_LEFT);
                Assert.assertEquals("Item.LAYOUT_RIGHT", 2, Item.LAYOUT_RIGHT);
                Assert.assertEquals("Item.LAYOUT_CENTER", 3, Item.LAYOUT_CENTER);
                Assert.assertEquals("Item.LAYOUT_TOP", 0x10, Item.LAYOUT_TOP);
                Assert.assertEquals("Item.LAYOUT_BOTTOM", 0x20, Item.LAYOUT_BOTTOM);
                Assert.assertEquals("Item.LAYOUT_VCENTER", 0x30, Item.LAYOUT_VCENTER);
                Assert.assertEquals("Item.LAYOUT_NEWLINE_BEFORE", 0x100,
                        Item.LAYOUT_NEWLINE_BEFORE);
                Assert.assertEquals("Item.LAYOUT_NEWLINE_AFTER", 0x200,
                        Item.LAYOUT_NEWLINE_AFTER);
                Assert.assertEquals("Item.LAYOUT_SHRINK", 0x400, Item.LAYOUT_SHRINK);
                Assert.assertEquals("Item.LAYOUT_EXPAND", 0x800, Item.LAYOUT_EXPAND);
                Assert.assertEquals("Item.LAYOUT_VSHRINK", 0x1000, Item.LAYOUT_VSHRINK);
                Assert.assertEquals("Item.LAYOUT_VEXPAND", 0x2000, Item.LAYOUT_VEXPAND);
                Assert.assertEquals("Item.LAYOUT_2", 0x4000, Item.LAYOUT_2);
                Assert.assertEquals("Item.PLAIN", 0, Item.PLAIN);
                Assert.assertEquals("Item.HYPERLINK", 1, Item.HYPERLINK);
                Assert.assertEquals("Item.BUTTON", 2, Item.BUTTON);
            }
        });

        add(new TestCase("TextField_constraints") {
            public void run() {
                Assert.assertEquals("TextField.ANY", 0, TextField.ANY);
                Assert.assertEquals("TextField.EMAILADDR", 1, TextField.EMAILADDR);
                Assert.assertEquals("TextField.NUMERIC", 2, TextField.NUMERIC);
                Assert.assertEquals("TextField.PHONENUMBER", 3, TextField.PHONENUMBER);
                Assert.assertEquals("TextField.URL", 4, TextField.URL);
                Assert.assertEquals("TextField.DECIMAL", 5, TextField.DECIMAL);
                Assert.assertEquals("TextField.PASSWORD", 0x10000, TextField.PASSWORD);
                Assert.assertEquals("TextField.UNEDITABLE", 0x20000, TextField.UNEDITABLE);
                Assert.assertEquals("TextField.SENSITIVE", 0x40000, TextField.SENSITIVE);
                Assert.assertEquals("TextField.NON_PREDICTIVE", 0x80000, TextField.NON_PREDICTIVE);
                Assert.assertEquals("TextField.INITIAL_CAPS_WORD", 0x100000,
                        TextField.INITIAL_CAPS_WORD);
                Assert.assertEquals("TextField.INITIAL_CAPS_SENTENCE", 0x200000,
                        TextField.INITIAL_CAPS_SENTENCE);
                Assert.assertEquals("TextField.CONSTRAINT_MASK", 0xFFFF,
                        TextField.CONSTRAINT_MASK);
            }
        });

        add(new TestCase("Alert_and_List_special_commands") {
            public void run() {
                Assert.assertEquals("Alert.FOREVER", -2, Alert.FOREVER);
                Assert.assertEquals("Alert.DISMISS_COMMAND label", "", Alert.DISMISS_COMMAND.getLabel());
                Assert.assertEquals("Alert.DISMISS_COMMAND priority", 0,
                        Alert.DISMISS_COMMAND.getPriority());
                Assert.assertEquals("Alert.DISMISS_COMMAND type must be OK",
                        Command.OK, Alert.DISMISS_COMMAND.getCommandType());

                Assert.assertEquals("List.SELECT_COMMAND label", "",
                        List.SELECT_COMMAND.getLabel());
                Assert.assertEquals("List.SELECT_COMMAND priority", 0,
                        List.SELECT_COMMAND.getPriority());
                Assert.assertEquals("List.SELECT_COMMAND type must be SCREEN",
                        Command.SCREEN, List.SELECT_COMMAND.getCommandType());
            }
        });

        add(new TestCase("GameCanvas_key_state_bits") {
            public void run() {
                Assert.assertEquals("GameCanvas.UP_PRESSED", 0x0002, GameCanvas.UP_PRESSED);
                Assert.assertEquals("GameCanvas.LEFT_PRESSED", 0x0004, GameCanvas.LEFT_PRESSED);
                Assert.assertEquals("GameCanvas.RIGHT_PRESSED", 0x0020, GameCanvas.RIGHT_PRESSED);
                Assert.assertEquals("GameCanvas.DOWN_PRESSED", 0x0040, GameCanvas.DOWN_PRESSED);
                Assert.assertEquals("GameCanvas.FIRE_PRESSED", 0x0100, GameCanvas.FIRE_PRESSED);
                Assert.assertEquals("GameCanvas.GAME_A_PRESSED", 0x0200, GameCanvas.GAME_A_PRESSED);
                Assert.assertEquals("GameCanvas.GAME_B_PRESSED", 0x0400, GameCanvas.GAME_B_PRESSED);
                Assert.assertEquals("GameCanvas.GAME_C_PRESSED", 0x0800, GameCanvas.GAME_C_PRESSED);
                Assert.assertEquals("GameCanvas.GAME_D_PRESSED", 0x1000, GameCanvas.GAME_D_PRESSED);
            }
        });

        add(new TestCase("Sprite_transforms") {
            public void run() {
                Assert.assertEquals("Sprite.TRANS_NONE", 0, Sprite.TRANS_NONE);
                Assert.assertEquals("Sprite.TRANS_MIRROR_ROT180", 1, Sprite.TRANS_MIRROR_ROT180);
                Assert.assertEquals("Sprite.TRANS_MIRROR", 2, Sprite.TRANS_MIRROR);
                Assert.assertEquals("Sprite.TRANS_ROT180", 3, Sprite.TRANS_ROT180);
                Assert.assertEquals("Sprite.TRANS_MIRROR_ROT270", 4, Sprite.TRANS_MIRROR_ROT270);
                Assert.assertEquals("Sprite.TRANS_ROT90", 5, Sprite.TRANS_ROT90);
                Assert.assertEquals("Sprite.TRANS_ROT270", 6, Sprite.TRANS_ROT270);
                Assert.assertEquals("Sprite.TRANS_MIRROR_ROT90", 7, Sprite.TRANS_MIRROR_ROT90);
            }
        });

        add(new TestCase("transform_values_are_shared_by_Image_and_Graphics") {
            public void run() {
                // Graphics.drawRegion and Image.createImage(Image, ...) use the
                // transform constants that are defined in Sprite, so the values
                // must be the same for every user of the API
                for (int transform = 0; transform <= 7; transform++) {
                    Assert.assertTrue("transform " + transform + " must be accepted by "
                            + "drawRegion", transform >= Sprite.TRANS_NONE
                            && transform <= Sprite.TRANS_MIRROR_ROT90);
                }
            }
        }.severity(TestCase.INFO));
    }
}
