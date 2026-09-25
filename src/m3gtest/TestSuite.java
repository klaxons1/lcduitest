/**
 * M3G Tester - test suite container.
 * A named group of TestCases, one group per area of the M3G API.
 * Suites are built explicitly (no reflection in CLDC), see Suites.java.
 */
package m3gtest;

import java.util.Enumeration;
import java.util.Vector;

public class TestSuite extends TestCase {

    private final String id;
    private final String title;
    private String summary;
    private final Vector cases = new Vector();

    /**
     * @param id      short machine readable id, e.g. "World"
     * @param title   title shown in the menu, e.g. "World"
     * @param summary one line description of the area that is covered
     */
    public TestSuite(String id, String title, String summary) {
        super(id);
        this.id = id;
        this.title = title;
        this.summary = summary;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    /** Number of test cases in this suite. */
    public int size() {
        return cases.size();
    }

    public TestCase get(int index) {
        return (TestCase) cases.elementAt(index);
    }

    /** Adds a test case; returns it so callers can chain .severity()/.describe(). */
    public TestCase add(TestCase test) {
        cases.addElement(test);
        return test;
    }

    public Enumeration elements() {
        return cases.elements();
    }

    /**
     * Running a suite on its own runs every test case in it. The runner
     * normally runs the cases, not the suite.
     */
    public void run() throws Exception {
        Enumeration e = cases.elements();
        while (e.hasMoreElements()) {
            ((TestCase) e.nextElement()).run();
        }
    }
}
