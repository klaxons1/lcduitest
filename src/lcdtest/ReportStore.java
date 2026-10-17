package lcdtest;

import javax.microedition.rms.RecordStore;

/** Persists the last machine-readable report in RMS. */
public final class ReportStore {

    private static final String NAME = "lcdtest-report";
    private static final int CHUNK = 1200;

    private ReportStore() {
    }

    public static boolean save(String text) {
        RecordStore rs = null;
        try {
            rs = RecordStore.openRecordStore(NAME, true);
            int next = rs.getNextRecordID();
            for (int i = 1; i < next; i++) {
                try {
                    rs.deleteRecord(i);
                } catch (Throwable ignored) {
                    // deleted already or never existed
                }
            }
            int pos = 0;
            while (pos < text.length()) {
                int len = Math.min(CHUNK, text.length() - pos);
                byte[] data = text.substring(pos, pos + len).getBytes();
                rs.addRecord(data, 0, data.length);
                pos += len;
            }
            return true;
        } catch (Throwable t) {
            return false;
        } finally {
            close(rs);
        }
    }

    public static String load() {
        RecordStore rs = null;
        try {
            rs = RecordStore.openRecordStore(NAME, false);
            StringBuffer b = new StringBuffer();
            int next = rs.getNextRecordID();
            for (int i = 1; i < next; i++) {
                try {
                    byte[] data = rs.getRecord(i);
                    if (data != null) {
                        b.append(new String(data));
                    }
                } catch (Throwable ignored) {
                    // skip missing record ids
                }
            }
            return b.toString();
        } catch (Throwable t) {
            return "";
        } finally {
            close(rs);
        }
    }

    public static boolean exists() {
        String s = load();
        return s.length() > 0 && s.startsWith("LCDUITEST|");
    }

    private static void close(RecordStore rs) {
        if (rs != null) {
            try {
                rs.closeRecordStore();
            } catch (Throwable ignored) {
                // ignore
            }
        }
    }
}
