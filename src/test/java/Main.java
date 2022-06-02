import controller.RTController;
import engine.EngineRuntime;
import graphics.GraphicsDisplay;
import org.joml.Vector3i;
import org.junit.jupiter.api.Test;


public class Main {
    @Test
    public void testLaunch() {
        RTController runtimeController = new RTController();

        GraphicsDisplay graphicsDisplay = new GraphicsDisplay(runtimeController, 1920, 1080, "Setup");
        runtimeController.hookGraphicsDisplay(graphicsDisplay);

        EngineRuntime runtime = new EngineRuntime(runtimeController, EngineRuntime.generateBlockLayer(new Vector3i(0, 5, 0), 25));
        runtimeController.hookEngineRuntime(runtime);


        runtimeController.run();
    }

    @Test
    public void testSaveLoad(){
        RTController runtimeController = new RTController();

        GraphicsDisplay graphicsDisplay = new GraphicsDisplay(runtimeController, 1920, 1080, "Debug");
        runtimeController.hookGraphicsDisplay(graphicsDisplay);
        EngineRuntime runtime = new EngineRuntime(runtimeController, EngineRuntime.generateBlockLayer(new Vector3i(0, 5, 0), 25));
        runtimeController.hookEngineRuntime(runtime);
        runtime.saveState("temp");
        runtime.loadState("temp");
    }

    @Test
    public void testRuntimeCheck(){
        RTController runtimeController = new RTController();

        GraphicsDisplay graphicsDisplay = new GraphicsDisplay(runtimeController, 1920, 1080, "Debug");
        runtimeController.hookGraphicsDisplay(graphicsDisplay);
        EngineRuntime runtime = new EngineRuntime(runtimeController, EngineRuntime.generateBlockLayer(new Vector3i(0, 5, 0), 25));
        runtimeController.hookEngineRuntime(runtime);
        runtime.check();
    }

}
