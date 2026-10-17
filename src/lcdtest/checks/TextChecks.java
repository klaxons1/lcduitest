package lcdtest.checks;

import java.util.Calendar;
import java.util.Date;

import javax.microedition.lcdui.DateField;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;

import lcdtest.Act;
import lcdtest.Body;
import lcdtest.Ctx;
import lcdtest.Suite;

/** TextField, TextBox and DateField contract checks. */
public final class TextChecks {

    private TextChecks() {
    }

    static Date date(int year, int month, int day, int hour, int minute, int second) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, second);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    static void dateParts(Ctx c, String what, Date d, int year, int month, int day,
            int hour, int minute, int second) {
        if (d == null) {
            c.isTrue(what + " is null", false);
            return;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        c.eqInt(what + " year", year, cal.get(Calendar.YEAR));
        c.eqInt(what + " month", month, cal.get(Calendar.MONTH));
        c.eqInt(what + " day", day, cal.get(Calendar.DAY_OF_MONTH));
        c.eqInt(what + " hour", hour, cal.get(Calendar.HOUR_OF_DAY));
        c.eqInt(what + " minute", minute, cal.get(Calendar.MINUTE));
        c.eqInt(what + " second", second, cal.get(Calendar.SECOND));
    }

    public static void register(Suite s) {
        final String G = "Text";

        // ---------------------------------------------------------------
        // TextField
        // ---------------------------------------------------------------

        s.add("TXT-001", G, "TextField construction and null text",
                "TextField(String,String,int,int): null text means empty", new Body() {
                    public void run(Ctx c) {
                        TextField tf = new TextField("label", null, 20, TextField.ANY);
                        c.eqStr("label", "label", tf.getLabel());
                        c.eqStr("text", "", tf.getString());
                        c.eqInt("size", 0, tf.size());
                        c.eqInt("max size", 20, tf.getMaxSize());
                        c.eqInt("constraints", TextField.ANY, tf.getConstraints());
                        c.eqInt("caret", 0, tf.getCaretPosition());
                    }
                });

        s.add("TXT-002", G, "TextField rejects a non-positive capacity",
                "TextField ctor: IAE if maxSize <= 0", new Body() {
                    public void run(Ctx c) {
                        final String[] args = new String[0];
                        c.throwsEx("maxSize 0", IllegalArgumentException.class, new Act() {
                            public void run() {
                                new TextField("l", "", 0, TextField.ANY);
                            }
                        });
                        c.throwsEx("maxSize -1", IllegalArgumentException.class, new Act() {
                            public void run() {
                                new TextField("l", "", -1, TextField.ANY);
                            }
                        });
                    }
                });

        s.add("TXT-003", G, "TextField rejects initial text longer than maxSize",
                "TextField ctor: IAE when text.length() > maxSize", new Body() {
                    public void run(Ctx c) {
                        c.throwsEx("length 5 into 4", IllegalArgumentException.class,
                                new Act() {
                                    public void run() {
                                        new TextField("l", "abcde", 4, TextField.ANY);
                                    }
                                });
                        c.noThrow("length 4 into 4", new Act() {
                            public void run() {
                                new TextField("l", "abcd", 4, TextField.ANY);
                            }
                        });
                    }
                });

        s.add("TXT-004", G, "TextField rejects invalid constraints",
                "TextField ctor: IAE for undefined constraint values", new Body() {
                    public void run(Ctx c) {
                        c.throwsEx("low value 6", IllegalArgumentException.class, new Act() {
                            public void run() {
                                new TextField("l", "", 4, 6);
                            }
                        });
                        c.throwsEx("undefined bit", IllegalArgumentException.class, new Act() {
                            public void run() {
                                new TextField("l", "", 4, 0x400000);
                            }
                        });
                    }
                });

        s.add("TXT-005", G, "TextField reports an assigned capacity of at least one",
                "TextField.getMaxSize: implementation may shrink, never below 1",
                new Body() {
                    public void run(Ctx c) {
                        TextField tf = new TextField("l", "", 5000, TextField.ANY);
                        c.atLeast("max size", tf.getMaxSize(), 1);
                        c.advTrue("assigned capacity <= requested", tf.getMaxSize() <= 5000);
                    }
                });

        s.add("TXT-006", G, "setString replaces the contents",
                "TextField.setString: null empties, oversize text throws IAE", new Body() {
                    public void run(Ctx c) {
                        final TextField tf = new TextField("l", "abc", 8, TextField.ANY);
                        tf.setString("hello");
                        c.eqStr("new text", "hello", tf.getString());
                        c.eqInt("size", 5, tf.size());
                        tf.setString(null);
                        c.eqStr("null empties", "", tf.getString());
                        c.eqInt("size after null", 0, tf.size());
                        c.throwsEx("too long", IllegalArgumentException.class, new Act() {
                            public void run() {
                                tf.setString("123456789");
                            }
                        });
                        c.eqStr("contents unchanged after IAE", "", tf.getString());
                    }
                });

        s.add("TXT-007", G, "insert clamps its position",
                "TextField.insert: position <= 0 prepends, >= size appends", new Body() {
                    public void run(Ctx c) {
                        TextField tf = new TextField("l", "bc", 20, TextField.ANY);
                        tf.insert("a", 0);
                        c.eqStr("insert at 0", "abc", tf.getString());
                        tf.insert("X", -5);
                        c.eqStr("negative position prepends", "Xabc", tf.getString());
                        tf.insert("Z", 99);
                        c.eqStr("large position appends", "XabcZ", tf.getString());
                        tf.insert("m", 2);
                        c.eqStr("middle insertion", "XamcZ", tf.getString());
                    }
                });

        s.add("TXT-008", G, "insert validates its arguments",
                "TextField.insert: NPE null, IAE when capacity would be exceeded", new Body() {
                    public void run(Ctx c) {
                        final TextField tf = new TextField("l", "12345", 6, TextField.ANY);
                        c.throwsEx("null source", NullPointerException.class, new Act() {
                            public void run() {
                                tf.insert(null, 0);
                            }
                        });
                        c.throwsEx("too long", IllegalArgumentException.class, new Act() {
                            public void run() {
                                tf.insert("xx", 0);
                            }
                        });
                        c.eqStr("unchanged", "12345", tf.getString());
                    }
                });

        s.add("TXT-009", G, "insert(char[]) copies a subrange",
                "TextField.insert(char[],offset,length,position)", new Body() {
                    public void run(Ctx c) {
                        TextField tf = new TextField("l", "ab", 20, TextField.ANY);
                        char[] data = {'0', '1', '2', '3'};
                        tf.insert(data, 1, 2, 1);
                        c.eqStr("inserted subrange", "a12b", tf.getString());
                        final TextField tf2 = new TextField("l", "x", 20, TextField.ANY);
                        c.throwsEx("offset+length > data.length",
                                ArrayIndexOutOfBoundsException.class, new Act() {
                                    public void run() {
                                        tf2.insert(new char[]{'a'}, 0, 2, 0);
                                    }
                                });
                    }
                });

        s.add("TXT-010", G, "delete removes a range",
                "TextField.delete: SIOOBE for invalid range, IAE for constraints",
                new Body() {
                    public void run(Ctx c) {
                        final TextField tf = new TextField("l", "abcdef", 20,
                                TextField.ANY);
                        tf.delete(1, 2);
                        c.eqStr("deleted middle", "adef", tf.getString());
                        tf.delete(0, 4);
                        c.eqStr("deleted all", "", tf.getString());
                        c.noThrow("delete(0,0) on empty", new Act() {
                            public void run() {
                                tf.delete(0, 0);
                            }
                        });
                        final TextField tf2 = new TextField("l", "abc", 20,
                                TextField.ANY);
                        c.throwsEx("offset > size",
                                StringIndexOutOfBoundsException.class, new Act() {
                                    public void run() {
                                        tf2.delete(4, 0);
                                    }
                                });
                        c.throwsEx("negative offset",
                                StringIndexOutOfBoundsException.class, new Act() {
                                    public void run() {
                                        tf2.delete(-1, 1);
                                    }
                                });
                        c.throwsEx("length beyond end",
                                StringIndexOutOfBoundsException.class, new Act() {
                                    public void run() {
                                        tf2.delete(1, 5);
                                    }
                                });
                    }
                });

        s.add("TXT-011", G, "setMaxSize truncates the contents",
                "TextField.setMaxSize: returns assigned size, truncates, IAE <= 0",
                new Body() {
                    public void run(Ctx c) {
                        TextField tf = new TextField("l", "abcdef", 10, TextField.ANY);
                        int assigned = tf.setMaxSize(3);
                        c.atLeast("assigned", assigned, 1);
                        c.advTrue("assigned <= 3", assigned <= 3);
                        c.eqStr("contents truncated", "abc", tf.getString());
                        final TextField tf2 = new TextField("l", "", 10, TextField.ANY);
                        c.throwsEx("setMaxSize(0)", IllegalArgumentException.class,
                                new Act() {
                                    public void run() {
                                        tf2.setMaxSize(0);
                                    }
                                });
                    }
                });

        s.add("TXT-012", G, "constraints accept documented values",
                "TextField.setConstraints: legal modes and flags are accepted",
                new Body() {
                    public void run(Ctx c) {
                        final TextField tf = new TextField("l", "", 30, TextField.ANY);
                        int[] vals = {TextField.ANY, TextField.EMAILADDR,
                                TextField.NUMERIC, TextField.PHONENUMBER, TextField.URL,
                                TextField.DECIMAL,
                                TextField.ANY | TextField.PASSWORD,
                                TextField.ANY | TextField.UNEDITABLE,
                                TextField.ANY | TextField.SENSITIVE,
                                TextField.ANY | TextField.NON_PREDICTIVE,
                                TextField.ANY | TextField.INITIAL_CAPS_WORD,
                                TextField.ANY | TextField.INITIAL_CAPS_SENTENCE,
                                TextField.PASSWORD | TextField.UNEDITABLE
                                        | TextField.SENSITIVE
                                        | TextField.NON_PREDICTIVE};
                        for (int i = 0; i < vals.length; i++) {
                            final int v = vals[i];
                            c.noThrow("setConstraints(" + v + ")", new Act() {
                                public void run() {
                                    tf.setConstraints(v);
                                }
                            });
                        }
                    }
                });

        s.add("TXT-013", G, "getConstraints reports the constraint mode",
                "TextField: getConstraints & CONSTRAINT_MASK round-trips", new Body() {
                    public void run(Ctx c) {
                        TextField tf = new TextField("l", "", 30, TextField.ANY);
                        int[] modes = {TextField.ANY, TextField.EMAILADDR,
                                TextField.NUMERIC, TextField.PHONENUMBER, TextField.URL,
                                TextField.DECIMAL};
                        for (int i = 0; i < modes.length; i++) {
                            tf.setConstraints(modes[i]);
                            c.eqInt("mode " + modes[i], modes[i],
                                    tf.getConstraints() & TextField.CONSTRAINT_MASK);
                        }
                        tf.setConstraints(TextField.ANY | TextField.PASSWORD);
                        c.isTrue("PASSWORD retained",
                                (tf.getConstraints() & TextField.PASSWORD) != 0);
                        c.advTrue("PASSWORD implies SENSITIVE",
                                (tf.getConstraints() & TextField.SENSITIVE) != 0);
                        c.advTrue("PASSWORD implies NON_PREDICTIVE",
                                (tf.getConstraints() & TextField.NON_PREDICTIVE) != 0);
                    }
                });

        s.add("TXT-014", G, "setConstraints rejects invalid values",
                "TextField.setConstraints: IAE for undefined bits or modes", new Body() {
                    public void run(Ctx c) {
                        final TextField tf = new TextField("l", "", 30, TextField.ANY);
                        c.throwsEx("mode 7", IllegalArgumentException.class, new Act() {
                            public void run() {
                                tf.setConstraints(7);
                            }
                        });
                        c.throwsEx("undefined high bit", IllegalArgumentException.class,
                                new Act() {
                                    public void run() {
                                        tf.setConstraints(TextField.ANY | 0x400000);
                                    }
                                });
                        c.throwsEx("negative value", IllegalArgumentException.class,
                                new Act() {
                                    public void run() {
                                        tf.setConstraints(-1);
                                    }
                                });
                    }
                });

        s.add("TXT-015", G, "NUMERIC contents are syntax-checked",
                "TextField: NUMERIC accepts integers, rejects other text", new Body() {
                    public void run(Ctx c) {
                        c.noThrow("constructor with 123", new Act() {
                            public void run() {
                                new TextField("l", "123", 10, TextField.NUMERIC);
                            }
                        });
                        c.noThrow("minus sign", new Act() {
                            public void run() {
                                new TextField("l", "-123", 10, TextField.NUMERIC);
                            }
                        });
                        c.throwsEx("constructor with abc", IllegalArgumentException.class,
                                new Act() {
                                    public void run() {
                                        new TextField("l", "abc", 10, TextField.NUMERIC);
                                    }
                                });
                        final TextField tf = new TextField("l", "1", 10, TextField.NUMERIC);
                        c.throwsEx("setString with abc", IllegalArgumentException.class,
                                new Act() {
                                    public void run() {
                                        tf.setString("abc");
                                    }
                                });
                        c.eqStr("contents unchanged", "1", tf.getString());
                    }
                });

        s.add("TXT-016", G, "DECIMAL contents are syntax-checked",
                "TextField: DECIMAL accepts decimal fractions", new Body() {
                    public void run(Ctx c) {
                        String[] good = {"123", "-123", "0.5", ".5", "-0.25", ""};
                        for (int i = 0; i < good.length; i++) {
                            final String v = good[i];
                            c.noThrow("decimal \"" + v + "\"", new Act() {
                                public void run() {
                                    new TextField("l", v, 10, TextField.DECIMAL);
                                }
                            });
                        }
                        c.throwsEx("decimal \"abc\"", IllegalArgumentException.class,
                                new Act() {
                                    public void run() {
                                        new TextField("l", "abc", 10, TextField.DECIMAL);
                                    }
                                });
                    }
                });

        s.add("TXT-017", G, "getChars and setChars work on arrays",
                "TextField.getChars/setChars: copy semantics and bounds", new Body() {
                    public void run(Ctx c) {
                        TextField tf = new TextField("l", "abc", 10, TextField.ANY);
                        char[] data = new char[5];
                        int n = tf.getChars(data);
                        c.eqInt("returned count", 3, n);
                        c.eqStr("copied", "abc", new String(data, 0, n));
                        char[] src = {'x', 'y', 'z'};
                        tf.setChars(src, 1, 2);
                        c.eqStr("setChars subrange", "yz", tf.getString());
                        tf.setChars(null, -3, 99);
                        c.eqStr("null data empties", "", tf.getString());
                        c.throwsEx("short array", ArrayIndexOutOfBoundsException.class,
                                new Act() {
                                    public void run() {
                                        TextField t = new TextField("l", "abc", 10,
                                                TextField.ANY);
                                        t.getChars(new char[2]);
                                    }
                                });
                        c.throwsEx("null array", NullPointerException.class, new Act() {
                            public void run() {
                                TextField t = new TextField("l", "", 10, TextField.ANY);
                                t.getChars(null);
                            }
                        });
                    }
                });

        s.add("TXT-018", G, "setInitialInputMode accepts hints",
                "TextField.setInitialInputMode: subset names are hints only", new Body() {
                    public void run(Ctx c) {
                        final TextField tf = new TextField("l", "", 10, TextField.ANY);
                        c.noThrow("LATIN", new Act() {
                            public void run() {
                                tf.setInitialInputMode("IS_LATIN");
                            }
                        });
                        c.noThrow("null hint", new Act() {
                            public void run() {
                                tf.setInitialInputMode(null);
                            }
                        });
                    }
                });

        // ---------------------------------------------------------------
        // DateField
        // ---------------------------------------------------------------

        s.add("DTF-001", G, "a new DateField is uninitialised",
                "DateField: getDate() null until set, getInputMode", new Body() {
                    public void run(Ctx c) {
                        DateField df = new DateField("d", DateField.DATE);
                        c.isNull("initial date", df.getDate());
                        c.eqInt("mode", DateField.DATE, df.getInputMode());
                        DateField dt = new DateField("d", DateField.DATE_TIME);
                        c.isNull("initial date DATE_TIME", dt.getDate());
                        DateField tm = new DateField("d", DateField.TIME);
                        c.isNull("initial date TIME", tm.getDate());
                    }
                });

        s.add("DTF-002", G, "DateField rejects invalid input modes",
                "DateField ctor/setInputMode: IAE unless DATE, TIME or DATE_TIME",
                new Body() {
                    public void run(Ctx c) {
                        final int[] bad = {0, 4, -1};
                        for (int i = 0; i < bad.length; i++) {
                            final int m = bad[i];
                            c.throwsEx("ctor mode " + m, IllegalArgumentException.class,
                                    new Act() {
                                        public void run() {
                                            new DateField("d", m);
                                        }
                                    });
                        }
                        final DateField df = new DateField("d", DateField.DATE);
                        c.throwsEx("setInputMode(0)", IllegalArgumentException.class,
                                new Act() {
                                    public void run() {
                                        df.setInputMode(0);
                                    }
                                });
                        c.eqInt("mode unchanged", DateField.DATE, df.getInputMode());
                    }
                });

        s.add("DTF-003", G, "DATE mode stores the date and zeroes the time",
                "DateField(DATE): getDate returns midnight of the selected day",
                new Body() {
                    public void run(Ctx c) {
                        DateField df = new DateField("d", DateField.DATE);
                        df.setDate(date(2005, 5, 15, 13, 45, 30));
                        Date got = df.getDate();
                        dateParts(c, "DATE", got, 2005, 5, 15, 0, 0, 0);
                    }
                });

        s.add("DTF-004", G, "TIME mode uses the 1970 epoch date",
                "DateField(TIME): date components are 1 Jan 1970", new Body() {
                    public void run(Ctx c) {
                        DateField df = new DateField("d", DateField.TIME);
                        df.setDate(date(1970, 0, 1, 10, 30, 45));
                        dateParts(c, "TIME", df.getDate(), 1970, 0, 1, 10, 30, 0);
                    }
                });

        s.add("DTF-005", G, "DATE_TIME keeps date and time to the minute",
                "DateField(DATE_TIME): seconds are dropped", new Body() {
                    public void run(Ctx c) {
                        DateField df = new DateField("d", DateField.DATE_TIME);
                        df.setDate(date(2005, 5, 15, 13, 45, 30));
                        dateParts(c, "DATE_TIME", df.getDate(), 2005, 5, 15, 13, 45, 0);
                    }
                });

        s.add("DTF-006", G, "setDate(null) uninitialises the field",
                "DateField.setDate: null clears the value", new Body() {
                    public void run(Ctx c) {
                        DateField df = new DateField("d", DateField.DATE_TIME);
                        df.setDate(date(2000, 0, 1, 0, 0, 0));
                        c.notNull("set", df.getDate());
                        df.setDate(null);
                        c.isNull("cleared", df.getDate());
                    }
                });

        s.add("DTF-007", G, "setInputMode changes the mode and contents",
                "DateField.setInputMode: mode is reflected by getInputMode", new Body() {
                    public void run(Ctx c) {
                        DateField df = new DateField("d", DateField.DATE);
                        df.setDate(date(2005, 5, 15, 0, 0, 0));
                        df.setInputMode(DateField.DATE_TIME);
                        c.eqInt("mode DATE_TIME", DateField.DATE_TIME, df.getInputMode());
                        c.notNull("value retained or cleared legally", df.getDate());
                        df.setInputMode(DateField.TIME);
                        c.eqInt("mode TIME", DateField.TIME, df.getInputMode());
                    }
                });

        s.add("DTF-008", G, "TIME mode rejects a date beyond the epoch day",
                "DateField(TIME): getDate null for times outside 1 Jan 1970", new Body() {
                    public void run(Ctx c) {
                        DateField df = new DateField("d", DateField.TIME);
                        df.setDate(date(1970, 0, 2, 10, 0, 0));
                        Date got = df.getDate();
                        c.advTrue("out-of-epoch value reports null (" + got + ")",
                                got == null);
                    }
                });

        // ---------------------------------------------------------------
        // TextBox
        // ---------------------------------------------------------------

        s.add("TXB-001", G, "TextBox construction and contents",
                "TextBox(String,String,int,int): title, text, capacity", new Body() {
                    public void run(Ctx c) {
                        TextBox tb = new TextBox("title", "hello", 20, TextField.ANY);
                        c.eqStr("title", "title", tb.getTitle());
                        c.eqStr("text", "hello", tb.getString());
                        c.eqInt("size", 5, tb.size());
                        c.eqInt("max size", 20, tb.getMaxSize());
                        c.eqInt("constraints", TextField.ANY, tb.getConstraints());
                        TextBox empty = new TextBox("t", null, 10, TextField.ANY);
                        c.eqStr("null text", "", empty.getString());
                    }
                });

        s.add("TXB-002", G, "TextBox constructor argument checks",
                "TextBox ctor: IAE maxSize, oversize text, invalid constraints",
                new Body() {
                    public void run(Ctx c) {
                        c.throwsEx("maxSize 0", IllegalArgumentException.class, new Act() {
                            public void run() {
                                new TextBox("t", "", 0, TextField.ANY);
                            }
                        });
                        c.throwsEx("text too long", IllegalArgumentException.class,
                                new Act() {
                                    public void run() {
                                        new TextBox("t", "abcd", 3, TextField.ANY);
                                    }
                                });
                        c.throwsEx("bad constraints", IllegalArgumentException.class,
                                new Act() {
                                    public void run() {
                                        new TextBox("t", "", 5, 9);
                                    }
                                });
                    }
                });

        s.add("TXB-003", G, "TextBox editing methods",
                "TextBox: setString, insert clamping, delete bounds", new Body() {
                    public void run(Ctx c) {
                        final TextBox tb = new TextBox("t", "bc", 20, TextField.ANY);
                        tb.insert("a", 0);
                        c.eqStr("prepend", "abc", tb.getString());
                        tb.insert("Z", 99);
                        c.eqStr("append", "abcZ", tb.getString());
                        tb.delete(1, 2);
                        c.eqStr("delete middle", "aZ", tb.getString());
                        c.throwsEx("delete out of range",
                                StringIndexOutOfBoundsException.class, new Act() {
                                    public void run() {
                                        tb.delete(5, 1);
                                    }
                                });
                        c.throwsEx("setString too long", IllegalArgumentException.class,
                                new Act() {
                                    public void run() {
                                        tb.setString("123456789012345678901");
                                    }
                                });
                        tb.setString(null);
                        c.eqStr("null empties", "", tb.getString());
                    }
                });

        s.add("TXB-004", G, "TextBox capacity can shrink",
                "TextBox.setMaxSize: IAE <= 0, contents truncated", new Body() {
                    public void run(Ctx c) {
                        TextBox tb = new TextBox("t", "abcdef", 10, TextField.ANY);
                        int assigned = tb.setMaxSize(2);
                        c.atLeast("assigned", assigned, 1);
                        c.advTrue("assigned <= 2", assigned <= 2);
                        c.eqStr("truncated", "ab", tb.getString());
                        final TextBox tb2 = new TextBox("t", "", 10, TextField.ANY);
                        c.throwsEx("setMaxSize(0)", IllegalArgumentException.class,
                                new Act() {
                                    public void run() {
                                        tb2.setMaxSize(0);
                                    }
                                });
                    }
                });

        s.add("TXB-005", G, "TextBox constraint handling",
                "TextBox.setConstraints: modes accepted, invalid rejected", new Body() {
                    public void run(Ctx c) {
                        final TextBox tb = new TextBox("t", "", 20, TextField.ANY);
                        int[] modes = {TextField.ANY, TextField.EMAILADDR,
                                TextField.NUMERIC, TextField.PHONENUMBER, TextField.URL,
                                TextField.DECIMAL};
                        for (int i = 0; i < modes.length; i++) {
                            final int m = modes[i];
                            c.noThrow("setConstraints(" + m + ")", new Act() {
                                public void run() {
                                    tb.setConstraints(m);
                                }
                            });
                            c.eqInt("mode round-trip", m,
                                    tb.getConstraints() & TextField.CONSTRAINT_MASK);
                        }
                        tb.setConstraints(TextField.ANY | TextField.PASSWORD);
                        c.isTrue("PASSWORD retained",
                                (tb.getConstraints() & TextField.PASSWORD) != 0);
                        c.advTrue("PASSWORD implies SENSITIVE",
                                (tb.getConstraints() & TextField.SENSITIVE) != 0);
                        c.throwsEx("invalid constraints", IllegalArgumentException.class,
                                new Act() {
                                    public void run() {
                                        tb.setConstraints(7);
                                    }
                                });
                    }
                });

        s.add("TXB-006", G, "TextBox supports char array access",
                "TextBox.getChars/setChars", new Body() {
                    public void run(Ctx c) {
                        TextBox tb = new TextBox("t", "abc", 10, TextField.ANY);
                        char[] buf = new char[8];
                        int n = tb.getChars(buf);
                        c.eqInt("chars copied", 3, n);
                        c.eqStr("copied text", "abc", new String(buf, 0, n));
                        tb.setChars(new char[]{'1', '2', '3'}, 1, 2);
                        c.eqStr("setChars", "23", tb.getString());
                        tb.setChars(null, 5, 5);
                        c.eqStr("null clears", "", tb.getString());
                    }
                });

        s.add("TXB-007", G, "TextBox caret stays in range",
                "TextBox.getCaretPosition: within [0..size()]", new Body() {
                    public void run(Ctx c) {
                        TextBox tb = new TextBox("t", "abc", 10, TextField.ANY);
                        c.inRange("caret", tb.getCaretPosition(), 0, tb.size());
                        tb.setString("");
                        c.inRange("empty caret", tb.getCaretPosition(), 0, 0);
                    }
                });
    }
}
