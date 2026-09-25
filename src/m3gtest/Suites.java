/*
 * M3G Tester - registry of test suites grouped into categories.
 * Order follows JSR-184 package summary.
 */
package m3gtest;

import m3gtest.tests.*;

public class Suites {

    private static TestSuite[] cache;

    private Suites() {
    }

    public static String[] categoryNames() {
        return new String[]{
            "core: Object3D, Transform, Graphics3D, Loader",
            "data: VertexArray, VertexBuffer, IndexBuffer, Image2D, Texture2D",
            "appearance: Appearance, Material, PolygonMode, CompositingMode, Fog, Background",
            "scene: Node, Group, World, Camera, Light, Mesh, MorphingMesh, SkinnedMesh, Sprite3D",
            "animation: KeyframeSequence, AnimationController, AnimationTrack",
            "utils: RayIntersection, Constants, Properties",
            "self test of the harness"
        };
    }

    public static int categoryCount() {
        return categoryNames().length;
    }

    public static String categoryName(int index) {
        return categoryNames()[index];
    }

    public static TestSuite[] byCategory(int category) {
        switch (category) {
            case 0:
                return new TestSuite[]{
                    new Object3DSuite(),
                    new TransformSuite(),
                    new Graphics3DSuite(),
                    new LoaderSuite()
                };
            case 1:
                return new TestSuite[]{
                    new VertexArraySuite(),
                    new VertexBufferSuite(),
                    new IndexBufferSuite(),
                    new Image2DSuite(),
                    new Texture2DSuite()
                };
            case 2:
                return new TestSuite[]{
                    new AppearanceSuite(),
                    new MaterialSuite(),
                    new PolygonModeSuite(),
                    new CompositingModeSuite(),
                    new FogSuite(),
                    new BackgroundSuite()
                };
            case 3:
                return new TestSuite[]{
                    new NodeSuite(),
                    new GroupSuite(),
                    new WorldSuite(),
                    new CameraSuite(),
                    new LightSuite(),
                    new MeshSuite(),
                    new MorphingMeshSuite(),
                    new SkinnedMeshSuite(),
                    new Sprite3DSuite()
                };
            case 4:
                return new TestSuite[]{
                    new KeyframeSequenceSuite(),
                    new AnimationControllerSuite(),
                    new AnimationTrackSuite()
                };
            case 5:
                return new TestSuite[]{
                    new RayIntersectionSuite(),
                    new ConstantsSuite(),
                    new PropertiesSuite()
                };
            default:
                return new TestSuite[]{new FrameworkSuite()};
        }
    }

    public static int categorySuiteCount(int category) {
        return byCategory(category).length;
    }

    public static int categoryTestCount(int category) {
        TestSuite[] suites = byCategory(category);
        int count = 0;
        for (int i = 0; i < suites.length; i++) {
            count += suites[i].size();
        }
        return count;
    }

    public static TestSuite[] all() {
        if (cache == null) {
            int count = 0;
            for (int i = 0; i < categoryCount(); i++) {
                count += categorySuiteCount(i);
            }
            TestSuite[] suites = new TestSuite[count];
            int next = 0;
            for (int i = 0; i < categoryCount(); i++) {
                TestSuite[] category = byCategory(i);
                for (int j = 0; j < category.length; j++) {
                    suites[next++] = category[j];
                }
            }
            cache = suites;
        }
        return cache;
    }

    public static int totalTestCount() {
        TestSuite[] suites = all();
        int count = 0;
        for (int i = 0; i < suites.length; i++) {
            count += suites[i].size();
        }
        return count;
    }
}
