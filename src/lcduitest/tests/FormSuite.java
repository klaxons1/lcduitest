/**
 * LcduiTest - MIDP 2.0 LCDUI conformance test MIDlet.
 *
 * javax.microedition.lcdui.Form - the container of Items.
 */
package lcduitest.tests;

import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemStateListener;
import javax.microedition.lcdui.Spacer;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;

import lcduitest.Assert;
import lcduitest.TestCase;
import lcduitest.TestSuite;
import lcduitest.Ui;

public class FormSuite extends TestSuite {

    public FormSuite() {
        super("Form", "Form container", "append, insert, delete, set, get, size and the "
                + "item ownership rules");

        add(new TestCase("empty_form") {
            public void run() {
                Form form = new Form("empty");
                Assert.assertEquals("a new Form has no items", 0, form.size());
                Assert.assertEquals("the title", "empty", form.getTitle());
                form.deleteAll();
                Assert.assertEquals("deleteAll() on an empty Form", 0, form.size());
            }
        });

        add(new TestCase("append_string_image_and_item") {
            public void run() {
                Form form = new Form("appending");
                int stringIndex = form.append("plain text");
                Assert.assertEquals("append(String) must return the index of the new item",
                        0, stringIndex);
                int imageIndex = form.append(Ui.solidImage(6, 6, 0x123456));
                Assert.assertEquals("append(Image) must return the next index", 1, imageIndex);
                StringItem item = new StringItem("label", "text");
                int itemIndex = form.append(item);
                Assert.assertEquals("append(Item) must return the next index", 2, itemIndex);
                Assert.assertEquals("size after three appends", 3, form.size());
                Assert.assertSame("get(2) must return the item that was appended", item,
                        form.get(2));
                Assert.assertTrue("get(0) of a String element must be a StringItem",
                        form.get(0) instanceof StringItem);
                Assert.assertTrue("get(1) of an Image element must be an ImageItem",
                        form.get(1) instanceof javax.microedition.lcdui.ImageItem);
            }
        });

        add(new TestCase("append_rejects_null") {
            public void run() {
                final Form form = new Form("nulls");
                Assert.expectException("append((String) null)", NullPointerException.class,
                        new Assert.Code() {
                            public void run() {
                                form.append((String) null);
                            }
                        });
                Assert.expectException("append((Image) null)", NullPointerException.class,
                        new Assert.Code() {
                            public void run() {
                                form.append((Image) null);
                            }
                        });
                Assert.expectException("append((Item) null)", NullPointerException.class,
                        new Assert.Code() {
                            public void run() {
                                form.append((Item) null);
                            }
                        });
            }
        });

        add(new TestCase("an_item_cannot_be_in_two_containers") {
            public void run() {
                final Form first = new Form("first");
                final Form second = new Form("second");
                final StringItem item = new StringItem("label", "text");
                first.append(item);
                Assert.expectException("appending an item that is already owned",
                        IllegalStateException.class, new Assert.Code() {
                            public void run() {
                                second.append(item);
                            }
                        });
                Assert.expectException("inserting an item that is already owned",
                        IllegalStateException.class, new Assert.Code() {
                            public void run() {
                                second.insert(0, item);
                            }
                        });
                Assert.expectException("set() with an item that is already owned",
                        IllegalStateException.class, new Assert.Code() {
                            public void run() {
                                second.append("something");
                                second.set(0, item);
                            }
                        });
            }
        });

        add(new TestCase("insert_into_the_middle") {
            public void run() {
                Form form = new Form("inserting");
                StringItem first = new StringItem(null, "first");
                StringItem second = new StringItem(null, "second");
                form.append(first);
                form.append("last");
                form.insert(1, second);
                Assert.assertEquals("size after insert", 3, form.size());
                Assert.assertSame("the inserted item is at index 1", second, form.get(1));
                Assert.assertEquals("the element that was at index 1 moved to 2", "last",
                        ((StringItem) form.get(2)).getText());
                form.insert(form.size(), new StringItem(null, "at the end"));
                Assert.assertEquals("inserting at size() appends", 4, form.size());
            }
        });

        add(new TestCase("insert_rejects_a_bad_index") {
            public void run() {
                final Form form = new Form("one");
                form.append("only element");
                Assert.expectException("insert(2, item) into a Form with one element",
                        IndexOutOfBoundsException.class, new Assert.Code() {
                            public void run() {
                                form.insert(2, new StringItem(null, "x"));
                            }
                        });
                Assert.expectException("insert(-1, item)", IndexOutOfBoundsException.class,
                        new Assert.Code() {
                            public void run() {
                                form.insert(-1, new StringItem(null, "x"));
                            }
                        });
            }
        });

        add(new TestCase("delete_and_get_reject_a_bad_index") {
            public void run() {
                final Form form = new Form("two");
                form.append("a");
                form.append("b");
                Assert.expectException("delete(2)", IndexOutOfBoundsException.class,
                        new Assert.Code() {
                            public void run() {
                                form.delete(2);
                            }
                        });
                Assert.expectException("delete(-1)", IndexOutOfBoundsException.class,
                        new Assert.Code() {
                            public void run() {
                                form.delete(-1);
                            }
                        });
                Assert.expectException("get(2)", IndexOutOfBoundsException.class,
                        new Assert.Code() {
                            public void run() {
                                form.get(2);
                            }
                        });
                form.delete(0);
                Assert.assertEquals("delete(0) removes one element", 1, form.size());
                Assert.assertEquals("the remaining element is the second one", "b",
                        ((StringItem) form.get(0)).getText());
            }
        });

        add(new TestCase("set_replaces_an_element") {
            public void run() {
                Form form = new Form("replacing");
                form.append("old");
                StringItem replacement = new StringItem("label", "new");
                form.set(0, replacement);
                Assert.assertEquals("set() does not change the size", 1, form.size());
                Assert.assertSame("set() puts the new item at the index", replacement,
                        form.get(0));
            }
        });

        add(new TestCase("deleteAll_empties_the_form") {
            public void run() {
                Form form = new Form("clearing");
                for (int i = 0; i < 5; i++) {
                    form.append("element " + i);
                }
                Assert.assertEquals("five elements", 5, form.size());
                form.deleteAll();
                Assert.assertEquals("deleteAll() removes every element", 0, form.size());
                form.append("after deleteAll");
                Assert.assertEquals("the Form is usable after deleteAll()", 1, form.size());
            }
        });

        add(new TestCase("form_constructor_with_items") {
            public void run() {
                Item[] items = {new StringItem("a", "b"), new Spacer(4, 4),
                    new TextField("text", "value", 8, TextField.ANY)};
                Form form = new Form("with items", items);
                Assert.assertEquals("size of the constructor array", 3, form.size());
                Assert.assertSame("the items are owned by the new Form", items[0], form.get(0));
            }
        });

        add(new TestCase("form_constructor_rejects_null_elements") {
            public void run() {
                final Item[] withNull = {new StringItem("a", "b"), null};
                Assert.expectException("a null element in the items array",
                        NullPointerException.class, new Assert.Code() {
                            public void run() {
                                new Form("bad", withNull);
                            }
                        });
                final Item[] alreadyOwned = new Item[1];
                new Form("owner").append(alreadyOwned[0] = new StringItem("a", "b"));
                Assert.expectException("an item that is already owned",
                        IllegalStateException.class, new Assert.Code() {
                            public void run() {
                                new Form("bad", alreadyOwned);
                            }
                        });
            }
        });

        add(new TestCase("itemStateListener_is_called") {
            public void run() {
                Form form = new Form("state");
                final boolean[] called = new boolean[1];
                final Item[] reported = new Item[1];
                form.setItemStateListener(new ItemStateListener() {
                    public void itemStateChanged(Item item) {
                        called[0] = true;
                        reported[0] = item;
                    }
                });
                Gauge gauge = new Gauge("g", true, 10, 5);
                form.append(gauge);
                gauge.setValue(7);
                boolean notified = Ui.waitForFlag(called, 2000);
                Assert.assertTrue("changing a value through the API must call "
                        + "itemStateChanged (the platform is allowed to deliver it "
                        + "asynchronously)", notified);
                Assert.assertSame("the changed item is reported", gauge, reported[0]);
            }
        }.severity(TestCase.SHOULD).describe("MIDP 2.0 requires itemStateChanged() for user "
                + "driven changes; notifying for API driven changes is the common behaviour"));

        add(new TestCase("layout_and_preferred_size_of_a_form") {
            public void run() {
                Form form = new Form("sizes");
                form.append("some content that is longer than the display width so that the "
                        + "layout code has something to do");
                Assert.info("form width", form.getWidth());
                Assert.info("form height", form.getHeight());
                Assert.assertTrue("a Form must report a positive width even when it is not "
                        + "shown but reported " + form.getWidth(), form.getWidth() > 0);
                Assert.assertTrue("a Form must report a positive height even when it is not "
                        + "shown but reported " + form.getHeight(), form.getHeight() > 0);
            }
        }.severity(TestCase.SHOULD));
    }
}
