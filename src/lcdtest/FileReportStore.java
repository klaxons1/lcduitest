package lcdtest;

import java.io.OutputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

/** Optional JSR-75 export, isolated so devices without it can still run. */
public final class FileReportStore {
    public static final String URL = "file:///root/lcduitest-report.txt";

    private FileReportStore() {
    }

    /** Replaces the previous report with UTF-8 plain text. */
    public static void save(String text) throws Exception {
        FileConnection file = null;
        OutputStream out = null;
        try {
            file = (FileConnection) Connector.open(URL, Connector.READ_WRITE);
            if (!file.exists()) {
                file.create();
            }
            file.truncate(0);
            out = file.openOutputStream();
            out.write(text.getBytes("UTF-8"));
            out.flush();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } finally {
                if (file != null) {
                    file.close();
                }
            }
        }
    }
}
