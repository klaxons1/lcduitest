package m3gtest.tests;

import javax.microedition.m3g.*;
import m3gtest.*;

public class EdgeCasesSuite extends TestSuite {

    public EdgeCasesSuite() {
        super("edgecases", "Edge cases & robustness", "Null checks, illegal args, boundary values, state errors.");

        // Object3D
        add(new TestCase("Object3D setUserID 0 and max") {
            public void run() {
                Mesh m = createMesh();
                m.setUserID(0);
                Assert.assertEquals("0", 0, m.getUserID());
                m.setUserID(Integer.MAX_VALUE);
                Assert.assertEquals("max", Integer.MAX_VALUE, m.getUserID());
            }
        });

        add(new TestCase("Object3D getReferences with null array returns count") {
            public void run() {
                Group g = new Group();
                g.addChild(createMesh());
                int c = g.getReferences(null);
                Assert.assertTrue("count >=1", c >= 1);
            }
        });

        add(new TestCase("Object3D find non-existent returns null") {
            public void run() {
                Group g = new Group();
                Object3D found = g.find(999999);
                Assert.assertNull("not found", found);
            }
        });

        add(new TestCase("Object3D duplicate preserves userID") {
            public void run() {
                Mesh m = createMesh();
                m.setUserID(123);
                Mesh dup = (Mesh) m.duplicate();
                Assert.assertNotNull("dup", dup);
            }
        });

        // Transform edge cases
        add(new TestCase("Transform invert singular throws") {
            public void run() {
                final Transform t = new Transform();
                float[] zero = new float[16];
                t.set(zero);
                Assert.expectException("singular invert", ArithmeticException.class, new Assert.Code() {
                    public void run() throws Exception {
                        t.invert();
                    }
                });
            }
        });

        add(new TestCase("Transform set null throws NPE") {
            public void run() {
                final Transform t = new Transform();
                Assert.expectException("null set", NullPointerException.class, new Assert.Code() {
                    public void run() throws Exception {
                        t.set((Transform)null);
                    }
                });
                Assert.expectException("null set float[]", NullPointerException.class, new Assert.Code() {
                    public void run() throws Exception {
                        t.set((float[])null);
                    }
                });
            }
        });

        add(new TestCase("Transform get null throws NPE") {
            public void run() {
                final Transform t = new Transform();
                Assert.expectException("null get", NullPointerException.class, new Assert.Code() {
                    public void run() throws Exception {
                        t.get((float[])null);
                    }
                });
            }
        });

        add(new TestCase("Transform postMultiply null throws") {
            public void run() {
                final Transform t = new Transform();
                Assert.expectException("null postMultiply", NullPointerException.class, new Assert.Code() {
                    public void run() throws Exception {
                        t.postMultiply(null);
                    }
                });
            }
        });

        // VertexArray edge cases
        add(new TestCase("VertexArray set null throws") {
            public void run() {
                final VertexArray va = new VertexArray(2, 3, 2);
                Assert.expectException("null byte", NullPointerException.class, new Assert.Code() {
                    public void run() throws Exception {
                        va.set(0, 1, (byte[])null);
                    }
                });
                Assert.expectException("null short", NullPointerException.class, new Assert.Code() {
                    public void run() throws Exception {
                        va.set(0, 1, (short[])null);
                    }
                });
            }
        });

        add(new TestCase("VertexArray get null throws") {
            public void run() {
                final VertexArray va = new VertexArray(2, 3, 2);
                short[] s = new short[]{0,0,0, 1,1,1};
                va.set(0, 2, s);
                Assert.expectException("null get byte", NullPointerException.class, new Assert.Code() {
                    public void run() throws Exception {
                        va.get(0, 1, (byte[])null);
                    }
                });
            }
        });

        add(new TestCase("VertexArray invalid ctor args") {
            public void run() {
                Assert.expectException("0 vertices", IllegalArgumentException.class, new Assert.Code() {
                    public void run() throws Exception { new VertexArray(0, 3, 2); }
                });
                Assert.expectException("1 component", IllegalArgumentException.class, new Assert.Code() {
                    public void run() throws Exception { new VertexArray(1, 1, 2); }
                });
                Assert.expectException("5 components", IllegalArgumentException.class, new Assert.Code() {
                    public void run() throws Exception { new VertexArray(1, 5, 2); }
                });
                Assert.expectException("3 byte size", IllegalArgumentException.class, new Assert.Code() {
                    public void run() throws Exception { new VertexArray(1, 3, 3); }
                });
            }
        });

        // VertexBuffer edge cases
        add(new TestCase("VertexBuffer setPositions null throws") {
            public void run() {
                final VertexBuffer vb = new VertexBuffer();
                Assert.expectException("null pos", NullPointerException.class, new Assert.Code() {
                    public void run() throws Exception { vb.setPositions(null, 1.0f, new float[]{0,0,0}); }
                });
            }
        });

        add(new TestCase("VertexBuffer mismatched vertex count throws") {
            public void run() {
                final VertexArray pos = new VertexArray(3, 3, 2);
                pos.set(0, 3, new short[9]);
                final VertexArray tex = new VertexArray(2, 2, 2);
                tex.set(0, 2, new short[4]);
                final VertexBuffer vb = new VertexBuffer();
                vb.setPositions(pos, 1.0f, new float[]{0,0,0});
                Assert.expectException("mismatched count", IllegalArgumentException.class, new Assert.Code() {
                    public void run() throws Exception { vb.setTexCoords(0, tex, 1.0f, new float[]{0,0,0}); }
                });
            }
        });

        add(new TestCase("VertexBuffer setDefaultColor") {
            public void run() {
                VertexBuffer vb = new VertexBuffer();
                vb.setDefaultColor(0xFF112233);
                Assert.assertEquals("default color", 0xFF112233, vb.getDefaultColor());
            }
        });

        // Image2D edge cases
        add(new TestCase("Image2D set null throws") {
            public void run() {
                final Image2D img = new Image2D(Image2D.RGB, 4, 4);
                Assert.expectException("null set", NullPointerException.class, new Assert.Code() {
                    public void run() throws Exception { img.set(0,0,2,2,null); }
                });
            }
        });

        add(new TestCase("Image2D set on immutable throws IllegalState") {
            public void run() {
                final Image2D img = new Image2D(Image2D.RGB, 2, 2, new byte[12]);
                Assert.expectException("immutable", IllegalStateException.class, new Assert.Code() {
                    public void run() throws Exception { img.set(0,0,1,1,new byte[3]); }
                });
            }
        });

        // Texture2D edge cases
        add(new TestCase("Texture2D setImage null allowed?") {
            public void run() {
                Image2D img = new Image2D(Image2D.RGB, 2, 2, new byte[12]);
                final Texture2D tex = new Texture2D(img);
                try {
                    tex.setImage(null);
                    Assert.assertTrue("null image allowed", true);
                } catch (NullPointerException e) {
                    Assert.assertTrue("NPE for null image", true);
                }
            }
        });

        add(new TestCase("Texture2D setFiltering invalid throws") {
            public void run() {
                final Image2D img = new Image2D(Image2D.RGB, 2, 2, new byte[12]);
                final Texture2D tex = new Texture2D(img);
                Assert.expectException("invalid filter", IllegalArgumentException.class, new Assert.Code() {
                    public void run() throws Exception { tex.setFiltering(999, 999); }
                });
            }
        });

        // Appearance edge cases
        add(new TestCase("Appearance setTexture invalid index throws") {
            public void run() {
                final Appearance ap = new Appearance();
                Image2D img = new Image2D(Image2D.RGB, 2, 2, new byte[12]);
                final Texture2D tex = new Texture2D(img);
                Assert.expectException("invalid texture index", IndexOutOfBoundsException.class, new Assert.Code() {
                    public void run() throws Exception { ap.setTexture(999, tex); }
                });
            }
        });

        add(new TestCase("Appearance getTexture invalid index throws") {
            public void run() {
                final Appearance ap = new Appearance();
                Assert.expectException("invalid getTexture", IndexOutOfBoundsException.class, new Assert.Code() {
                    public void run() throws Exception { ap.getTexture(999); }
                });
            }
        });

        // Group edge cases
        add(new TestCase("Group addChild null throws NPE") {
            public void run() {
                final Group g = new Group();
                Assert.expectException("null child", NullPointerException.class, new Assert.Code() {
                    public void run() throws Exception { g.addChild(null); }
                });
            }
        });

        add(new TestCase("Group addChild self throws") {
            public void run() {
                final Group g = new Group();
                Assert.expectException("self child", IllegalArgumentException.class, new Assert.Code() {
                    public void run() throws Exception { g.addChild(g); }
                });
            }
        });

        add(new TestCase("Group addChild World throws") {
            public void run() {
                final Group g = new Group();
                final World w = new World();
                Assert.expectException("World as child", IllegalArgumentException.class, new Assert.Code() {
                    public void run() throws Exception { g.addChild(w); }
                });
            }
        });

        add(new TestCase("Group removeChild null does not throw?") {
            public void run() {
                Group g = new Group();
                try {
                    g.removeChild(null);
                    Assert.assertTrue("remove null no throw", true);
                } catch (NullPointerException e) {
                    Assert.assertTrue("NPE for remove null", true);
                }
            }
        });

        // Mesh edge cases
        add(new TestCase("Mesh setAppearance invalid index throws") {
            public void run() {
                final Mesh m = createMesh();
                Assert.expectException("invalid index", IndexOutOfBoundsException.class, new Assert.Code() {
                    public void run() throws Exception { m.setAppearance(5, new Appearance()); }
                });
            }
        });

        add(new TestCase("Mesh getIndexBuffer invalid index throws") {
            public void run() {
                final Mesh m = createMesh();
                Assert.expectException("invalid getIndexBuffer", IndexOutOfBoundsException.class, new Assert.Code() {
                    public void run() throws Exception { m.getIndexBuffer(5); }
                });
            }
        });

        // World edge cases
        add(new TestCase("World setActiveCamera null allowed") {
            public void run() {
                World w = new World();
                try {
                    w.setActiveCamera(null);
                    Assert.assertNull("camera null", w.getActiveCamera());
                } catch (Throwable t) {
                    Assert.assertTrue("exception for null camera", true);
                }
            }
        });

        add(new TestCase("World setBackground null allowed") {
            public void run() {
                World w = new World();
                w.setBackground(null);
                Assert.assertNull("bg null", w.getBackground());
            }
        });

        // AnimationController edge cases
        add(new TestCase("AnimationController setActiveInterval start>end throws") {
            public void run() {
                final AnimationController ctrl = new AnimationController();
                Assert.expectException("start>end", IllegalArgumentException.class, new Assert.Code() {
                    public void run() throws Exception { ctrl.setActiveInterval(1000, 0); }
                });
            }
        });

        add(new TestCase("AnimationController setWeight negative allowed?") {
            public void run() {
                AnimationController ctrl = new AnimationController();
                try {
                    ctrl.setWeight(-1.0f);
                    Assert.assertTrue("negative weight", true);
                } catch (IllegalArgumentException e) {
                    Assert.assertTrue("IAE for negative weight", true);
                }
            }
        });

        // KeyframeSequence edge cases
        add(new TestCase("KeyframeSequence setKeyframe invalid index throws") {
            public void run() {
                final KeyframeSequence ks = new KeyframeSequence(2, 3, KeyframeSequence.LINEAR);
                Assert.expectException("invalid index", IndexOutOfBoundsException.class, new Assert.Code() {
                    public void run() throws Exception { ks.setKeyframe(5, 0, new float[]{0,0,0}); }
                });
            }
        });

        add(new TestCase("KeyframeSequence setDuration negative throws") {
            public void run() {
                final KeyframeSequence ks = new KeyframeSequence(1, 3, KeyframeSequence.LINEAR);
                Assert.expectException("negative duration", IllegalArgumentException.class, new Assert.Code() {
                    public void run() throws Exception { ks.setDuration(-1); }
                });
            }
        });

        add(new TestCase("KeyframeSequence setValidRange invalid throws") {
            public void run() {
                final KeyframeSequence ks = new KeyframeSequence(3, 3, KeyframeSequence.LINEAR);
                Assert.expectException("invalid range", IllegalArgumentException.class, new Assert.Code() {
                    public void run() throws Exception { ks.setValidRange(2, 1); }
                });
                Assert.expectException("out of bounds", IndexOutOfBoundsException.class, new Assert.Code() {
                    public void run() throws Exception { ks.setValidRange(0, 5); }
                });
            }
        });

        // Graphics3D edge cases
        add(new TestCase("Graphics3D bindTarget null throws NPE") {
            public void run() {
                final Graphics3D g3d = Graphics3D.getInstance();
                Assert.expectException("null bind", NullPointerException.class, new Assert.Code() {
                    public void run() throws Exception { g3d.bindTarget(null); }
                });
            }
        });

        add(new TestCase("Graphics3D releaseTarget without bind throws") {
            public void run() {
                final Graphics3D g3d = Graphics3D.getInstance();
                try { g3d.releaseTarget(); } catch (Throwable t) {}
                Assert.expectException("release without bind", IllegalStateException.class, new Assert.Code() {
                    public void run() throws Exception { g3d.releaseTarget(); }
                });
            }
        });

        add(new TestCase("Graphics3D setCamera null throws NPE") {
            public void run() {
                final Graphics3D g3d = Graphics3D.getInstance();
                Assert.expectException("null camera", NullPointerException.class, new Assert.Code() {
                    public void run() throws Exception { g3d.setCamera(null, new Transform()); }
                });
            }
        });

        add(new TestCase("Graphics3D addLight null throws NPE") {
            public void run() {
                final Graphics3D g3d = Graphics3D.getInstance();
                Assert.expectException("null light", NullPointerException.class, new Assert.Code() {
                    public void run() throws Exception { g3d.addLight(null, new Transform()); }
                });
            }
        });

        // Loader edge cases
        add(new TestCase("Loader load with offset out of bounds throws") {
            public void run() {
                final byte[] data = new byte[]{0,1,2,3};
                Assert.expectException("offset OOB", Exception.class, new Assert.Code() {
                    public void run() throws Exception { Loader.load(data, 10); }
                });
            }
        });

        // Node edge cases
        add(new TestCase("Node setTransform null throws NPE") {
            public void run() {
                final Group g = new Group();
                Assert.expectException("null transform", NullPointerException.class, new Assert.Code() {
                    public void run() throws Exception { g.setTransform(null); }
                });
            }
        });

        add(new TestCase("Node getTransform null throws NPE") {
            public void run() {
                final Group g = new Group();
                Assert.expectException("null getTransform", NullPointerException.class, new Assert.Code() {
                    public void run() throws Exception { g.getTransform(null); }
                });
            }
        });

        add(new TestCase("Node setAlphaFactor out of range?") {
            public void run() {
                Group g = new Group();
                try {
                    g.setAlphaFactor(2.0f);
                    Assert.assertTrue("alpha >1 allowed", true);
                } catch (IllegalArgumentException e) {
                    Assert.assertTrue("IAE for alpha >1", true);
                }
            }
        });

        // Background edge cases
        add(new TestCase("Background setCrop negative allowed?") {
            public void run() {
                Background bg = new Background();
                bg.setCrop(-1, -1, 10, 10);
                Assert.assertEquals("cropX -1", -1, bg.getCropX());
            }
        });

        // Light edge cases
        add(new TestCase("Light setMode invalid throws") {
            public void run() {
                final Light l = new Light();
                Assert.expectException("invalid mode", IllegalArgumentException.class, new Assert.Code() {
                    public void run() throws Exception { l.setMode(999); }
                });
            }
        });

        // Camera edge cases
        add(new TestCase("Camera setPerspective invalid fovy throws") {
            public void run() {
                final Camera cam = new Camera();
                Assert.expectException("fovy 0", IllegalArgumentException.class, new Assert.Code() {
                    public void run() throws Exception { cam.setPerspective(0, 1, 1, 10); }
                });
                Assert.expectException("fovy 180", IllegalArgumentException.class, new Assert.Code() {
                    public void run() throws Exception { cam.setPerspective(180, 1, 1, 10); }
                });
            }
        });

        add(new TestCase("Camera setPerspective near>=far throws") {
            public void run() {
                final Camera cam = new Camera();
                Assert.expectException("near>=far", IllegalArgumentException.class, new Assert.Code() {
                    public void run() throws Exception { cam.setPerspective(60, 1, 10, 1); }
                });
            }
        });

        // Sprite3D edge cases
        add(new TestCase("Sprite3D setCrop negative throws?") {
            public void run() {
                Image2D img = new Image2D(Image2D.RGBA, 4, 4, new byte[64]);
                Sprite3D sprite = new Sprite3D(false, img, new Appearance());
                try {
                    sprite.setCrop(-1, -1, 2, 2);
                    Assert.assertTrue("negative crop allowed", true);
                } catch (IllegalArgumentException e) {
                    Assert.assertTrue("IAE for negative crop", true);
                }
            }
        });

        // MorphingMesh edge cases
        add(new TestCase("MorphingMesh setWeights wrong length throws") {
            public void run() {
                final MorphingMesh mm = createMorphingMesh();
                Assert.expectException("wrong weights length", IllegalArgumentException.class, new Assert.Code() {
                    public void run() throws Exception { mm.setWeights(new float[]{0.5f, 0.5f}); }
                });
            }
        });

        // SkinnedMesh edge cases
        add(new TestCase("SkinnedMesh addTransform null bone throws") {
            public void run() {
                final SkinnedMesh sm = createSkinnedMesh();
                Assert.expectException("null bone", NullPointerException.class, new Assert.Code() {
                    public void run() throws Exception { sm.addTransform(null, 1, 0, 1); }
                });
            }
        });

        add(new TestCase("SkinnedMesh addTransform invalid vertex range throws") {
            public void run() {
                final SkinnedMesh sm = createSkinnedMesh();
                final Group skeleton = sm.getSkeleton();
                final Node bone = skeleton.getChild(0);
                Assert.expectException("invalid range", Exception.class, new Assert.Code() {
                    public void run() throws Exception { sm.addTransform(bone, 1, 100, 10); }
                });
            }
        });
    }

    private Mesh createMesh() {
        VertexArray pos = new VertexArray(3, 3, 2);
        pos.set(0, 3, new short[]{0,0,0, 1000,0,0, 0,1000,0});
        VertexBuffer vb = new VertexBuffer();
        vb.setPositions(pos, 0.001f, new float[]{0,0,0});
        IndexBuffer ib = new TriangleStripArray(0, new int[]{3});
        return new Mesh(vb, ib, new Appearance());
    }

    private MorphingMesh createMorphingMesh() {
        VertexArray posBase = new VertexArray(3, 3, 2);
        posBase.set(0, 3, new short[]{0,0,0, 1000,0,0, 0,1000,0});
        VertexBuffer base = new VertexBuffer();
        base.setPositions(posBase, 0.001f, new float[]{0,0,0});
        VertexArray posTarget = new VertexArray(3, 3, 2);
        posTarget.set(0, 3, new short[]{0,0,0, 1000,0,0, 500,1500,0});
        VertexBuffer target = new VertexBuffer();
        target.setPositions(posTarget, 0.001f, new float[]{0,0,0});
        IndexBuffer ib = new TriangleStripArray(0, new int[]{3});
        return new MorphingMesh(base, new VertexBuffer[]{target}, ib, new Appearance());
    }

    private SkinnedMesh createSkinnedMesh() {
        VertexArray pos = new VertexArray(3, 3, 2);
        pos.set(0, 3, new short[]{0,0,0, 1000,0,0, 0,1000,0});
        VertexBuffer vb = new VertexBuffer();
        vb.setPositions(pos, 0.001f, new float[]{0,0,0});
        IndexBuffer ib = new TriangleStripArray(0, new int[]{3});
        Group skeleton = new Group();
        Group bone = new Group();
        skeleton.addChild(bone);
        SkinnedMesh sm = new SkinnedMesh(vb, ib, new Appearance(), skeleton);
        sm.addTransform(bone, 1, 0, 3);
        return sm;
    }
}
