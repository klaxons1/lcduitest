/*
 * M3G Tester
 */
package m3gtest;

import java.util.Enumeration;
import java.util.Vector;

public class TestSuite extends TestCase {

    private final String id;
    private final String title;
    private String summary;
    private final Vector cases = new Vector();

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

    public int size() {
        return cases.size();
    }

    public TestCase get(int index) {
        return (TestCase) cases.elementAt(index);
    }

    public TestCase add(TestCase test) {
        cases.addElement(test);
        return test;
    }

    public Enumeration elements() {
        return cases.elements();
    }

    public void run() throws Exception {
        Enumeration e = cases.elements();
        while (e.hasMoreElements()) {
            ((TestCase) e.nextElement()).run();
        }
    }
}
