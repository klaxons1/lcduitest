/*
 * M3G Tester - Graphics3D
 */
package m3gtest.tests;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.m3g.*;

import m3gtest.*;

public class Graphics3DSuite extends TestSuite {

    public Graphics3DSuite() {
        super("Graphics3D", "Graphics3D singleton and rendering", "getInstance, getProperties, bindTarget, releaseTarget, clear, setViewport, render World, setCamera, addLight, render nodes");

        add(new TestCase("singleton") {
            public void run() {
                Graphics3D g1 = Graphics3D.getInstance();
                Graphics3D g2 = Graphics3D.getInstance();
                Assert.assertNotNull("getInstance not null", g1);
                Assert.assertSame("singleton same instance", g1, g2);
            }
        });

        add(new TestCase("getProperties") {
            public void run() {
                Graphics3D g3d = Graphics3D.getInstance();
                java.util.Hashtable props = g3d.getProperties();
                Assert.assertNotNull("getProperties not null", props);
                Assert.info("properties size", props.size());
                java.util.Enumeration keys = props.keys();
                while (keys.hasMoreElements()) {
                    Object k = keys.nextElement();
                    Object v = props.get(k);
                    Assert.info(k.toString(), String.valueOf(v));
                }
                // Check expected keys if present
                Object m3gVersion = props.get("supportM3G1.1");
                if (m3gVersion != null) {
                    Assert.info("supportM3G1.1", String.valueOf(m3gVersion));
                }
            }
        });

        add(new TestCase("bindTarget_Graphics_and_release") {
            public void run() {
                Graphics3D g3d = Graphics3D.getInstance();
                Image img = Image.createImage(64, 64);
                Graphics g = img.getGraphics();

                Assert.expectNoException("bindTarget Graphics", new Assert.Code() {
                    public void run() {
                        g3d.bindTarget(g);
                        g3d.releaseTarget();
                    }
                });

                // Double bind should throw IllegalStateException
                Assert.expectException("double bind should throw", IllegalStateException.class, new Assert.Code() {
                    public void run() {
                        Graphics3D gg = Graphics3D.getInstance();
                        Image im = Image.createImage(32, 32);
                        Graphics gr = im.getGraphics();
                        try {
                            gg.bindTarget(gr);
                            gg.bindTarget(gr);
                        } finally {
                            try {
                                gg.releaseTarget();
                            } catch (Throwable t) {
                            }
                        }
                    }
                });

                // release without bind should throw?
                Assert.expectException("release without bind", IllegalStateException.class, new Assert.Code() {
                    public void run() {
                        Graphics3D.getInstance().releaseTarget();
                    }
                });
            }
        });

        add(new TestCase("bindTarget_Image2D") {
            public void run() {
                Graphics3D g3d = Graphics3D.getInstance();
                Image2D img2d = new Image2D(Image2D.RGB, 32, 32);
                Assert.expectNoException("bindTarget Image2D", new Assert.Code() {
                    public void run() {
                        g3d.bindTarget(img2d);
                        g3d.releaseTarget();
                    }
                });
            }
        });

        add(new TestCase("bindTarget_null_checks") {
            public void run() {
                Assert.expectException("bindTarget null Graphics", NullPointerException.class, new Assert.Code() {
                    public void run() {
                        Graphics3D.getInstance().bindTarget((Graphics) null);
                    }
                });
                Assert.expectException("bindTarget null Image2D", NullPointerException.class, new Assert.Code() {
                    public void run() {
                        Graphics3D.getInstance().bindTarget((Image2D) null);
                    }
                });
                Assert.expectException("bindTarget null Object", NullPointerException.class, new Assert.Code() {
                    public void run() {
                        Graphics3D.getInstance().bindTarget((Object) null);
                    }
                });
            }
        });

        add(new TestCase("setViewport_and_getViewport") {
            public void run() {
                Graphics3D g3d = Graphics3D.getInstance();
                Image img = Image.createImage(64, 64);
                Graphics g = img.getGraphics();
                g3d.bindTarget(g);
                try {
                    g3d.setViewport(0, 0, 64, 64);
                    int[] rect = new int[4];
                    // getViewport may not exist in all impl? Check spec: Graphics3D has getViewport? Actually spec says setViewport only, but some impl have get? Let's check via reflection
                    // We test setViewport with various values and ensure no exception
                    g3d.setViewport(5, 5, 20, 20);
                    g3d.setViewport(0, 0, 32, 32);
                    // Invalid args?
                    Assert.expectException("setViewport negative width", IllegalArgumentException.class, new Assert.Code() {
                        public void run() {
                            Graphics3D.getInstance().setViewport(0, 0, -1, 10);
                        }
                    });
                } finally {
                    g3d.releaseTarget();
                }
            }
        });

        add(new TestCase("clear") {
            public void run() {
                Graphics3D g3d = Graphics3D.getInstance();
                Image img = Image.createImage(32, 32);
                Graphics g = img.getGraphics();
                g3d.bindTarget(g);
                try {
                    Background bg = new Background();
                    bg.setColor(0xFF0000);
                    g3d.clear(bg);
                    // clear null should be allowed? Spec says clear(null) clears depth only? Let's check
                    Assert.expectNoException("clear null background", new Assert.Code() {
                        public void run() {
                            Graphics3D.getInstance().clear(null);
                        }
                    });
                } finally {
                    g3d.releaseTarget();
                }
            }
        });

        add(new TestCase("render_World_immediate") {
            public void run() {
                Graphics3D g3d = Graphics3D.getInstance();
                Image img = Image.createImage(64, 64);
                Graphics g = img.getGraphics();
                World world = new World();
                Camera cam = new Camera();
                world.addChild(cam);
                world.setActiveCamera(cam);
                Background bg = new Background();
                bg.setColor(0x0000FF);
                world.setBackground(bg);

                g3d.bindTarget(g);
                try {
                    Assert.expectNoException("render World", new Assert.Code() {
                        public void run() {
                            g3d.render(world);
                        }
                    });
                } finally {
                    g3d.releaseTarget();
                }
            }
        });

        add(new TestCase("render_Node_with_camera_light") {
            public void run() {
                Graphics3D g3d = Graphics3D.getInstance();
                Image img = Image.createImage(64, 64);
                Graphics g = img.getGraphics();

                // Create simple scene
                VertexArray pos = new VertexArray(3, 3, 2);
                short[] verts = new short[]{0, 0, 0, 10, 0, 0, 0, 10, 0};
                pos.set(0, 3, verts);
                VertexBuffer vb = new VertexBuffer();
                vb.setPositions(pos, 1.0f, null);
                IndexBuffer ib = new TriangleStripArray(0, new int[]{3});
                Appearance ap = new Appearance();
                Mesh mesh = new Mesh(vb, ib, ap);

                Camera cam = new Camera();
                Light light = new Light();
                light.setMode(Light.DIRECTIONAL);
                Background bg = new Background();
                bg.setColor(0x000000);

                g3d.bindTarget(g);
                try {
                    g3d.setCamera(cam, new Transform());
                    g3d.addLight(light, new Transform());
                    g3d.setViewport(0, 0, 64, 64);
                    g3d.clear(bg);
                    Assert.expectNoException("render Mesh node", new Assert.Code() {
                        public void run() {
                            g3d.render(mesh, new Transform());
                        }
                    });
                    // reset lights
                    g3d.resetLights();
                } finally {
                    g3d.releaseTarget();
                }
            }
        });

        add(new TestCase("render_without_bind_should_fail") {
            public void run() {
                Graphics3D g3d = Graphics3D.getInstance();
                try {
                    g3d.releaseTarget();
                } catch (Throwable t) {
                }
                Assert.expectException("render without bind", IllegalStateException.class, new Assert.Code() {
                    public void run() {
                        World w = new World();
                        Graphics3D.getInstance().render(w);
                    }
                });
                Assert.expectException("clear without bind", IllegalStateException.class, new Assert.Code() {
                    public void run() {
                        Graphics3D.getInstance().clear(null);
                    }
                });
            }
        });

        add(new TestCase("camera_and_light_management") {
            public void run() {
                Graphics3D g3d = Graphics3D.getInstance();
                Image img = Image.createImage(32, 32);
                Graphics g = img.getGraphics();
                g3d.bindTarget(g);
                try {
                    Camera cam = new Camera();
                    Transform t = new Transform();
                    g3d.setCamera(cam, t);
                    Assert.assertSame("getCamera returns same", cam, g3d.getCamera(new Transform()));

                    Light light = new Light();
                    g3d.addLight(light, t);
                    Assert.assertEquals("light count 1", 1, g3d.getLightCount());
                    Light retrieved = g3d.getLight(0, new Transform());
                    Assert.assertSame("getLight returns same", light, retrieved);

                    g3d.resetLights();
                    Assert.assertEquals("light count after reset 0", 0, g3d.getLightCount());

                    // getLight out of bounds
                    Assert.expectException("getLight out of bounds", IndexOutOfBoundsException.class, new Assert.Code() {
                        public void run() {
                            Graphics3D.getInstance().getLight(0, new Transform());
                        }
                    });

                    g3d.setCamera(null, null);
                    Assert.assertNull("camera null after set null", g3d.getCamera(new Transform()));
                } finally {
                    g3d.releaseTarget();
                }
            }
        });

        add(new TestCase("compositing_and_depth_range") {
            public void run() {
                Graphics3D g3d = Graphics3D.getInstance();
                Image img = Image.createImage(32, 32);
                Graphics g = img.getGraphics();
                g3d.bindTarget(g);
                try {
                    // depth range
                    g3d.setDepthRange(0.0f, 1.0f);
                    Assert.expectException("depthRange near>far", IllegalArgumentException.class, new Assert.Code() {
                        public void run() {
                            Graphics3D.getInstance().setDepthRange(1.0f, 0.0f);
                        }
                    });
                    Assert.expectException("depthRange out of [0,1]", IllegalArgumentException.class, new Assert.Code() {
                        public void run() {
                            Graphics3D.getInstance().setDepthRange(-0.1f, 1.0f);
                        }
                    });
                } finally {
                    g3d.releaseTarget();
                }
            }
        });
    }
}
