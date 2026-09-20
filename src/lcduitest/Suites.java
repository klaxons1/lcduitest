/**
 * LcduiTest - MIDP 2.0 LCDUI conformance test MIDlet.
 *
 * The registry of every test suite, grouped into the categories that the main
 * menu shows. Suites are built explicitly, CLDC has no reflection, and the
 * order follows the MIDP 2.0 javax.microedition.lcdui package summary:
 * every class and every interface of the package has a suite here.
 */
package lcduitest;

import lcduitest.tests.AlertSuite;
import lcduitest.tests.AlertTypeSuite;
import lcduitest.tests.CanvasSuite;
import lcduitest.tests.ChoiceGroupSuite;
import lcduitest.tests.CommandSuite;
import lcduitest.tests.ConstantsSuite;
import lcduitest.tests.CustomItemSuite;
import lcduitest.tests.DateFieldSuite;
import lcduitest.tests.DisplaySuite;
import lcduitest.tests.DisplayableSuite;
import lcduitest.tests.FontSuite;
import lcduitest.tests.FormSuite;
import lcduitest.tests.FrameworkSuite;
import lcduitest.tests.GameCanvasSuite;
import lcduitest.tests.GaugeSuite;
import lcduitest.tests.GraphicsSuite;
import lcduitest.tests.ImageItemSuite;
import lcduitest.tests.ImageSuite;
import lcduitest.tests.ItemSuite;
import lcduitest.tests.LayerManagerSuite;
import lcduitest.tests.ListSuite;
import lcduitest.tests.SpacerSuite;
import lcduitest.tests.SpriteSuite;
import lcduitest.tests.StringItemSuite;
import lcduitest.tests.TextBoxSuite;
import lcduitest.tests.TextFieldSuite;
import lcduitest.tests.TickerSuite;
import lcduitest.tests.TiledLayerSuite;

public class Suites {

    private static TestSuite[] cache;

    private Suites() {
    }

    public static String[] categoryNames() {
        return new String[]{
            "core: Display, Displayable, Command",
            "core: Canvas, Graphics, Font",
            "core: Image",
            "items: Form, Item, choices, text, date, gauge",
            "screens: List, TextBox, Alert, Ticker",
            "game: GameCanvas, Layer, LayerManager, Sprite, TiledLayer",
            "self test of the harness"
        };
    }

    public static int categoryCount() {
        return categoryNames().length;
    }

    public static String categoryName(int index) {
        return categoryNames()[index];
    }

    /** All suites of a category, in the order they are run. */
    public static TestSuite[] byCategory(int category) {
        switch (category) {
            case 0:
                return new TestSuite[]{new DisplaySuite(), new DisplayableSuite(), new CommandSuite()};
            case 1:
                return new TestSuite[]{new CanvasSuite(), new GraphicsSuite(), new FontSuite()};
            case 2:
                return new TestSuite[]{new ImageSuite(), new ConstantsSuite()};
            case 3:
                return new TestSuite[]{new FormSuite(), new ItemSuite(), new TextFieldSuite(),
                    new ChoiceGroupSuite(), new GaugeSuite(), new DateFieldSuite(),
                    new StringItemSuite(), new ImageItemSuite(), new SpacerSuite(),
                    new CustomItemSuite()};
            case 4:
                return new TestSuite[]{new ListSuite(), new TextBoxSuite(), new AlertSuite(),
                    new AlertTypeSuite(), new TickerSuite()};
            case 5:
                return new TestSuite[]{new GameCanvasSuite(), new LayerManagerSuite(),
                    new SpriteSuite(), new TiledLayerSuite()};
            default:
                return new TestSuite[]{new FrameworkSuite()};
        }
    }

    public static int categorySuiteCount(int category) {
        return byCategory(category).length;
    }

    public static int categoryTestCount(int category) {
        TestSuite[] suites = byCategory(category);
        int count = 0;
        for (int i = 0; i < suites.length; i++) {
            count += suites[i].size();
        }
        return count;
    }

    /** Every suite of the whole MIDlet, in run order. */
    public static TestSuite[] all() {
        if (cache == null) {
            int count = 0;
            for (int i = 0; i < categoryCount(); i++) {
                count += categorySuiteCount(i);
            }
            TestSuite[] suites = new TestSuite[count];
            int next = 0;
            for (int i = 0; i < categoryCount(); i++) {
                TestSuite[] category = byCategory(i);
                for (int j = 0; j < category.length; j++) {
                    suites[next++] = category[j];
                }
            }
            cache = suites;
        }
        return cache;
    }

    public static int totalTestCount() {
        TestSuite[] suites = all();
        int count = 0;
        for (int i = 0; i < suites.length; i++) {
            count += suites[i].size();
        }
        return count;
    }
}
