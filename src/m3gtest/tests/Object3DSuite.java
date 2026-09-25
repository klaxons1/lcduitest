/*
 * M3G Tester - Object3D base class
 */
package m3gtest.tests;

import javax.microedition.m3g.*;

import m3gtest.*;

public class Object3DSuite extends TestSuite {

    public Object3DSuite() {
        super("Object3D", "Object3D base", "userID, userObject, duplicate, find, references, animation tracks, animate");

        add(new TestCase("user_id_default_and_set") {
            public void run() {
                VertexArray va = new VertexArray(1, 3, 1);
                Assert.assertEquals("default userID is 0", 0, va.getUserID());
                va.setUserID(42);
                Assert.assertEquals("getUserID after set", 42, va.getUserID());
                va.setUserID(0);
                Assert.assertEquals("set back to 0", 0, va.getUserID());
                va.setUserID(-1);
                Assert.assertEquals("negative ID allowed", -1, va.getUserID());
                va.setUserID(0x7fffffff);
                Assert.assertEquals("max int", 0x7fffffff, va.getUserID());
            }
        });

        add(new TestCase("user_object_default_and_set") {
            public void run() {
                VertexArray va = new VertexArray(1, 3, 1);
                Assert.assertNull("default userObject is null", va.getUserObject());
                Object obj = new Object();
                va.setUserObject(obj);
                Assert.assertSame("getUserObject returns same instance", obj, va.getUserObject());
                va.setUserObject(null);
                Assert.assertNull("set null", va.getUserObject());
                String s = "hello";
                va.setUserObject(s);
                Assert.assertEquals("string object", s, va.getUserObject());
            }
        });

        add(new TestCase("duplicate_creates_new_instance") {
            public void run() {
                VertexArray va = new VertexArray(4, 3, 1);
                byte[] data = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
                va.set(0, 4, data);
                va.setUserID(123);
                va.setUserObject("test");

                Object3D dup = va.duplicate();
                Assert.assertNotNull("duplicate not null", dup);
                Assert.assertNotSame("duplicate is different instance", va, dup);
                Assert.assertTrue("duplicate is VertexArray", dup instanceof VertexArray);
                VertexArray vaDup = (VertexArray) dup;
                Assert.assertEquals("duplicate vertex count", va.getVertexCount(), vaDup.getVertexCount());
                Assert.assertEquals("duplicate userID same as original", 123, vaDup.getUserID());
                // userObject handling: spec says userObject may be Hashtable for loaded objects, but for created objects it's null or set? Actually duplicate should copy userID but what about userObject? Spec says duplicate has same properties, but userObject is application specific. Test that it is copied or at least not crash.
                Assert.info("duplicate userObject", String.valueOf(vaDup.getUserObject()));
            }
        });

        add(new TestCase("duplicate_node_copies_descendants") {
            public void run() {
                Group g = new Group();
                Mesh mesh = createSimpleMesh();
                g.addChild(mesh);
                g.setUserID(10);
                mesh.setUserID(20);

                Object3D dup = g.duplicate();
                Assert.assertTrue("duplicate is Group", dup instanceof Group);
                Group gDup = (Group) dup;
                Assert.assertEquals("duplicate userID", 10, gDup.getUserID());
                Assert.assertEquals("duplicate child count", 1, gDup.getChildCount());
                Assert.assertNotSame("child is duplicated", mesh, gDup.getChild(0));
                Assert.assertEquals("child userID copied", 20, gDup.getChild(0).getUserID());
                Assert.assertNull("duplicate parent is null", gDup.getParent());
            }
        });

        add(new TestCase("find_by_user_id") {
            public void run() {
                Group root = new Group();
                Group child = new Group();
                Mesh mesh = createSimpleMesh();
                root.addChild(child);
                child.addChild(mesh);
                root.setUserID(1);
                child.setUserID(2);
                mesh.setUserID(3);

                Assert.assertSame("find root from root", root, root.find(1));
                Assert.assertSame("find child from root", child, root.find(2));
                Assert.assertSame("find mesh from root", mesh, root.find(3));
                Assert.assertSame("find mesh from child", mesh, child.find(3));
                Assert.assertNull("find non-existing", root.find(9999));
                // find self
                Assert.assertSame("find self", mesh, mesh.find(3));
            }
        });

        add(new TestCase("getReferences") {
            public void run() {
                Group g = new Group();
                Mesh mesh = createSimpleMesh();
                g.addChild(mesh);
                int count = g.getReferences(null);
                Assert.assertEquals("Group with 1 child has 1 reference", 1, count);

                Object3D[] refs = new Object3D[count];
                int count2 = g.getReferences(refs);
                Assert.assertEquals("count consistent", count, count2);
                Assert.assertSame("reference is mesh", mesh, refs[0]);

                // Object with no references
                VertexArray va = new VertexArray(1, 3, 1);
                Assert.assertEquals("VertexArray has 0 references", 0, va.getReferences(null));

                // Appearance has references
                Appearance app = new Appearance();
                Material mat = new Material();
                app.setMaterial(mat);
                int appRefs = app.getReferences(null);
                Assert.assertGreater("Appearance with material has >=1 ref", appRefs, 0);
            }
        });

        add(new TestCase("animation_track_management") {
            public void run() {
                VertexArray va = new VertexArray(1, 3, 1);
                Assert.assertEquals("initial track count 0", 0, va.getAnimationTrackCount());

                KeyframeSequence ks = new KeyframeSequence(2, 3, KeyframeSequence.LINEAR);
                ks.setKeyframe(0, 0, new float[]{0, 0, 0});
                ks.setKeyframe(1, 1000, new float[]{1, 1, 1});
                AnimationController ac = new AnimationController();
                AnimationTrack track = new AnimationTrack(ks, AnimationTrack.COLOR);

                va.addAnimationTrack(track);
                Assert.assertEquals("track count after add", 1, va.getAnimationTrackCount());
                Assert.assertSame("getAnimationTrack", track, va.getAnimationTrack(0));

                va.removeAnimationTrack(track);
                Assert.assertEquals("track count after remove", 0, va.getAnimationTrackCount());

                // add null should throw NPE
                Assert.expectException("addAnimationTrack(null)", NullPointerException.class, new Assert.Code() {
                    public void run() {
                        new VertexArray(1, 3, 1).addAnimationTrack(null);
                    }
                });

                // get out of bounds
                Assert.expectException("getAnimationTrack out of bounds", IndexOutOfBoundsException.class, new Assert.Code() {
                    public void run() {
                        new VertexArray(1, 3, 1).getAnimationTrack(0);
                    }
                });
            }
        });

        add(new TestCase("animate_returns_validity") {
            public void run() {
                Group g = new Group();
                int validity = g.animate(0);
                Assert.info("animate validity with no tracks", validity);
                // Should be >=0 or max int recommended when no animations
                Assert.assertTrue("validity >=0 or is max", validity >= 0 || validity == Integer.MAX_VALUE);

                // With animation
                VertexArray va = new VertexArray(1, 3, 1);
                KeyframeSequence ks = new KeyframeSequence(2, 1, KeyframeSequence.LINEAR);
                ks.setKeyframe(0, 0, new float[]{0});
                ks.setKeyframe(1, 1000, new float[]{1});
                AnimationController ac = new AnimationController();
                ac.setActiveInterval(0, 1000);
                AnimationTrack track = new AnimationTrack(ks, ac, AnimationTrack.ALPHA);
                // Track needs to be attached to something animatable? Actually Object3D itself can have tracks targeting its properties? For VertexArray, not all properties animatable. Use Material as better example.
                Material mat = new Material();
                mat.addAnimationTrack(track);
                int v2 = mat.animate(500);
                Assert.info("animate with track validity", v2);
            }
        });

        add(new TestCase("getReferences_with_small_array") {
            public void run() {
                // Spec says if array too small, extra refs are ignored? Actually method fills given array and returns count. Implementation should not crash.
                Group g = new Group();
                g.addChild(createSimpleMesh());
                g.addChild(createSimpleMesh());
                int total = g.getReferences(null);
                Assert.assertEquals("2 children", 2, total);
                Object3D[] small = new Object3D[1];
                int returned = g.getReferences(small);
                Assert.assertEquals("returned count still total", 2, returned);
                Assert.assertNotNull("first element filled", small[0]);
            }
        });
    }

    private static Mesh createSimpleMesh() {
        // Simple triangle
        VertexArray positions = new VertexArray(3, 3, 2);
        short[] verts = new short[]{0, 0, 0, 1, 0, 0, 0, 1, 0};
        positions.set(0, 3, verts);
        VertexBuffer vb = new VertexBuffer();
        vb.setPositions(positions, 1.0f, null);
        IndexBuffer ib = new TriangleStripArray(0, new int[]{3});
        Appearance ap = new Appearance();
        return new Mesh(vb, ib, ap);
    }
}
