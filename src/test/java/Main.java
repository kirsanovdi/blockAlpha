import controller.RTController;
import engine.EngineRuntime;
import graphics.GraphicsDisplay;
import org.junit.jupiter.api.Test;

public class Main {
    @Test
    public void test1(){
        RTController runtimeController = new RTController();
        EngineRuntime runtime = new EngineRuntime(runtimeController, EngineRuntime.generateRndBlocks(150000, 250));
        GraphicsDisplay graphicsDisplay = new GraphicsDisplay(runtimeController,1080, 720, "Setup");

        runtimeController.setEngineRuntime(runtime);
        runtimeController.setGraphicsDisplay(graphicsDisplay);

        graphicsDisplay.run();
    }
}
