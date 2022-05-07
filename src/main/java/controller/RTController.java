package controller;

import engine.EngineRuntime;
import graphics.GraphicsDisplay;

public class RTController {
    public EngineRuntime engineRuntime;
    public GraphicsDisplay graphicsDisplay;

    public void setEngineRuntime(EngineRuntime engineRuntime) {
        this.engineRuntime = engineRuntime;
    }

    public void setGraphicsDisplay(GraphicsDisplay graphicsDisplay){
        this.graphicsDisplay = graphicsDisplay;
    }

    public void run() {
        if(engineRuntime == null) throw new RuntimeException("engineRuntime was null");
        if(graphicsDisplay == null) throw new RuntimeException("graphicsDisplay was null");
        graphicsDisplay.run();
    }
}
