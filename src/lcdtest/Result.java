package lcdtest;

/** Outcome of one check. */
public class Result {

    final Check check;
    int status;
    String detail;
    long millis;
    boolean manual;

    Result(Check check, int status, String detail, long millis) {
        this.check = check;
        this.status = status;
        this.detail = detail == null ? "" : detail;
        this.millis = millis;
    }

    Result(Check check, int status, String detail, long millis, boolean manual) {
        this(check, status, detail, millis);
        this.manual = manual;
    }

    Result(String id, String group, String title, int kind, int status,
           String detail, long millis, boolean manual) {
        this(new Check(id, group, title, "", kind, null), status, detail, millis);
        this.manual = manual;
    }
}

