package m3gtest;

import m3gtest.tests.*;

public class Suites {

    private Suites() {}

    public static TestSuite[] all() {
        return new TestSuite[] {
            new ConstantsSuite(),
            new FrameworkSuite(),
            new PropertiesSuite(),
            new Object3DSuite(),
            new TransformSuite(),
            new VertexArraySuite(),
            new VertexBufferSuite(),
            new IndexBufferSuite(),
            new AppearanceSuite(),
            new MaterialSuite(),
            new CompositingModeSuite(),
            new PolygonModeSuite(),
            new Texture2DSuite(),
            new Image2DSuite(),
            new BackgroundSuite(),
            new FogSuite(),
            new LightSuite(),
            new CameraSuite(),
            new GroupSuite(),
            new MeshSuite(),
            new MorphingMeshSuite(),
            new SkinnedMeshSuite(),
            new Sprite3DSuite(),
            new WorldSuite(),
            new AnimationTrackSuite(),
            new AnimationControllerSuite(),
            new KeyframeSequenceSuite(),
            new RayIntersectionSuite(),
            new Graphics3DSuite(),
            new LoaderSuite(),
            new NodeSuite()
        };
    }

    public static int totalTests() {
        int n = 0;
        TestSuite[] suites = all();
        for (int i = 0; i < suites.length; i++) {
            n += suites[i].size();
        }
        return n;
    }
}
