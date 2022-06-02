import controller.RTController;
import engine.EngineRuntime;
import graphics.GraphicsDisplay;
import org.joml.Vector3i;

public class Main {
    public static void main(String[] args) {
        RTController runtimeController = new RTController();

        GraphicsDisplay graphicsDisplay = new GraphicsDisplay(runtimeController, 1920, 1080, "Setup");
        runtimeController.hookGraphicsDisplay(graphicsDisplay);

        EngineRuntime runtime = new EngineRuntime(runtimeController, EngineRuntime.generateBlockLayer(new Vector3i(0, 5, 0), 25));
        runtimeController.hookEngineRuntime(runtime);


        runtimeController.run();
    }
}
