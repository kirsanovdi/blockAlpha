import controller.RTController;
import engine.EngineRuntime;
import graphics.GraphicsDisplay;
import org.junit.jupiter.api.Test;


public class Main {
    @Test
    public void test1(){
        RTController runtimeController = new RTController();

        GraphicsDisplay graphicsDisplay = new GraphicsDisplay(runtimeController, 1080, 720, "Setup");
        runtimeController.hookGraphicsDisplay(graphicsDisplay);
        runtimeController.setupCamera();

        EngineRuntime runtime = new EngineRuntime(runtimeController, EngineRuntime.generateRndBlocks(1000, 150));
        runtimeController.hookEngineRuntime(runtime);


        runtimeController.run();
    }

    @Test
    public void test2() {
        //String line = "Block: cord=[\t47\t25\t47\t],\tid=0,\tsideIds=[0, 1, 0, 0, 0, 0]";
        //EngineRuntime.loadState();
    }

}
