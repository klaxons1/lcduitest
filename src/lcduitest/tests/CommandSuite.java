/**
 * LcduiTest - MIDP 2.0 LCDUI conformance test MIDlet.
 *
 * Command: labels, types, priorities and the documented exceptions.
 */
package lcduitest.tests;

import javax.microedition.lcdui.Command;

import lcduitest.Assert;
import lcduitest.TestCase;
import lcduitest.TestSuite;

public class CommandSuite extends TestSuite {

    private static final int[] TYPES = {Command.SCREEN, Command.ITEM, Command.BACK, Command.CANCEL,
        Command.OK, Command.HELP, Command.STOP, Command.EXIT};
    private static final String[] TYPE_NAMES = {"SCREEN", "ITEM", "BACK", "CANCEL", "OK", "HELP",
        "STOP", "EXIT"};

    public CommandSuite() {
        super("Command", "Command objects", "labels, the eight command types, priorities and "
                + "the exceptions that the specification mandates");

        add(new TestCase("constants_are_distinct") {
            public void run() {
                for (int i = 0; i < TYPES.length; i++) {
                    Assert.info(TYPE_NAMES[i], TYPES[i]);
                    for (int j = i + 1; j < TYPES.length; j++) {
                        Assert.assertNotEquals("command type " + TYPE_NAMES[i] + " and "
                                + TYPE_NAMES[j] + " must differ", TYPES[j], TYPES[i]);
                    }
                }
            }
        });

        add(new TestCase("three_argument_constructor") {
            public void run() {
                for (int i = 0; i < TYPES.length; i++) {
                    Command command = new Command("label", TYPES[i], i + 1);
                    Assert.assertEquals(TYPE_NAMES[i] + " getCommandType()", TYPES[i],
                            command.getCommandType());
                    Assert.assertEquals(TYPE_NAMES[i] + " getLabel()", "label", command.getLabel());
                    Assert.assertEquals(TYPE_NAMES[i] + " getPriority()", i + 1,
                            command.getPriority());
                    Assert.assertNull(TYPE_NAMES[i] + " getLongLabel() must be null",
                            command.getLongLabel());
                }
            }
        });

        add(new TestCase("four_argument_constructor") {
            public void run() {
                Command command = new Command("short", "long", Command.SCREEN, 3);
                Assert.assertEquals("getLabel", "short", command.getLabel());
                Assert.assertEquals("getLongLabel", "long", command.getLongLabel());
                Assert.assertEquals("getCommandType", Command.SCREEN, command.getCommandType());
                Assert.assertEquals("getPriority", 3, command.getPriority());
            }
        });

        add(new TestCase("four_argument_constructor_accepts_null_long_label") {
            public void run() {
                Command command = new Command("short", null, Command.OK, 1);
                Assert.assertNull("long label null is allowed", command.getLongLabel());
                Assert.assertEquals("short label", "short", command.getLabel());
            }
        });

        add(new TestCase("null_label_is_rejected") {
            public void run() {
                Assert.expectException("Command(null, SCREEN, 1)", NullPointerException.class,
                        new Assert.Code() {
                            public void run() {
                                new Command(null, Command.SCREEN, 1);
                            }
                        });
                Assert.expectException("Command(null, null, SCREEN, 1)", NullPointerException.class,
                        new Assert.Code() {
                            public void run() {
                                new Command(null, null, Command.SCREEN, 1);
                            }
                        });
            }
        });

        add(new TestCase("invalid_command_type_is_rejected") {
            public void run() {
                final int[] invalid = {0, -1, 9, 1000};
                for (int i = 0; i < invalid.length; i++) {
                    final int type = invalid[i];
                    Assert.expectException("Command(type=" + type + ")",
                            IllegalArgumentException.class, new Assert.Code() {
                                public void run() {
                                    new Command("label", type, 1);
                                }
                            });
                }
            }
        });

        add(new TestCase("negative_priority") {
            public void run() {
                Command command = new Command("label", Command.SCREEN, 0);
                Assert.info("priority 0 accepted", command.getPriority());
                try {
                    Command negative = new Command("label", Command.SCREEN, -1);
                    Assert.info("priority -1 accepted (getPriority=" + negative.getPriority() + ")");
                } catch (IllegalArgumentException expected) {
                    Assert.info("priority -1 rejected with IllegalArgumentException");
                }
            }
        }.severity(TestCase.INFO).describe("MIDP 2.0 does not state whether a negative "
                + "priority must be rejected; the observation is reported"));

        add(new TestCase("priority_order_is_reported") {
            public void run() {
                Command low = new Command("low priority", Command.SCREEN, 9);
                Command high = new Command("high priority", Command.SCREEN, 1);
                Assert.info("low", low.getPriority());
                Assert.info("high", high.getPriority());
                Assert.assertTrue("a smaller priority value means a more important command",
                        high.getPriority() < low.getPriority());
            }
        });

        add(new TestCase("empty_label") {
            public void run() {
                Command command = new Command("", Command.SCREEN, 1);
                Assert.assertEquals("the platform must accept an empty label and report it back",
                        "", command.getLabel());
            }
        }.severity(TestCase.SHOULD).describe("MIDP 2.0 mandates IllegalArgumentException for a "
                + "null label, an empty label is not forbidden"));

        add(new TestCase("long_labels_are_reported") {
            public void run() {
                String longLabel = "a long label that does not fit on any soft key";
                Command command = new Command("s", longLabel, Command.SCREEN, 1);
                Assert.assertEquals("getLongLabel returns the string as passed", longLabel,
                        command.getLongLabel());
                Assert.info("short label", command.getLabel().length());
                Assert.info("long label", command.getLongLabel().length());
            }
        });
    }
}
