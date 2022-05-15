import controller.RTController;
import engine.EngineRuntime;
import graphics.GraphicsDisplay;
import org.junit.jupiter.api.Test;


public class Main {
    @Test
    public void test1(){
        RTController runtimeController = new RTController();

        GraphicsDisplay graphicsDisplay = new GraphicsDisplay(runtimeController, 1920, 1080, "Setup");
        runtimeController.hookGraphicsDisplay(graphicsDisplay);

        EngineRuntime runtime = new EngineRuntime(runtimeController, EngineRuntime.generateBlockLayer(25));
        runtimeController.hookEngineRuntime(runtime);


        runtimeController.run();
    }

}
