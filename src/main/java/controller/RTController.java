package controller;

import engine.EngineRuntime;
import graphics.GraphicsDisplay;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.lwjgl.glfw.GLFW.*;
import static controller.Commands.*;

public class RTController {
    private EngineRuntime engineRuntime;
    private GraphicsDisplay graphicsDisplay;

    public Set<Commands> commandsSet;
    private HashMap<Commands, Boolean> commandsHashSet;
    private boolean clickHandler;
    public boolean wasInputHandled;

    private boolean isRunning = true;

    public void run() {
        if(engineRuntime == null) throw new RuntimeException("engineRuntime was null");
        if(graphicsDisplay == null) throw new RuntimeException("graphicsDisplay was null");
        commandsSet = new LinkedHashSet<>();
        commandsHashSet = new HashMap<>();
        commandsHashSet.put(REMOVE, true);
        commandsHashSet.put(ADD, true);
        commandsHashSet.put(JUMP, true);
        clickHandler = true;
        wasInputHandled = true;
        Thread thread = new Thread(engineRuntime::run);
        thread.start();
        graphicsDisplay.run();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void hookGraphicsDisplay(GraphicsDisplay graphicsDisplay){
        this.graphicsDisplay = graphicsDisplay;
    }

    public void hookEngineRuntime(EngineRuntime engineRuntime){
        this.engineRuntime = engineRuntime;
    }

    public EngineRuntime getEngineRuntime() {
        return engineRuntime;
    }

    public GraphicsDisplay getGraphicsDisplay() {
        return graphicsDisplay;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void toClose(){
        isRunning = false;
    }

    private void lockKey(Commands command){
        commandsHashSet.put(command, false);
    }

    private void unlockKey(Commands command){
        commandsHashSet.put(command, true);
    }

    private boolean getKeyValue(Commands command){
        return commandsHashSet.get(command);
    }

    public void Input(long window) {
        commandsSet = new LinkedHashSet<>();

        if (glfwGetKey(window, GLFW_KEY_Q) == GLFW_PRESS) {
            glfwSetWindowShouldClose(window, true);
            toClose();
        }

        if (glfwGetKey(window, GLFW_KEY_Z) == GLFW_PRESS) engineRuntime.saveState("state1");
        if (glfwGetKey(window, GLFW_KEY_X) == GLFW_PRESS) engineRuntime.loadState("state1");

        if (glfwGetKey(window, GLFW_KEY_C) == GLFW_PRESS) engineRuntime.saveState("state2");
        if (glfwGetKey(window, GLFW_KEY_V) == GLFW_PRESS) engineRuntime.loadState("state2");

        if (glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS) commandsSet.add(SPEED_1);
        if (glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == GLFW_RELEASE) commandsSet.add(SPEED_01);
        if (glfwGetKey(window, GLFW_KEY_LEFT_CONTROL) == GLFW_PRESS) commandsSet.add(SPEED_0025);

        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) commandsSet.add(FORWARD);
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) commandsSet.add(BACKWARD);
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) commandsSet.add(LEFT);
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) commandsSet.add(RIGHT);

        if (getKeyValue(JUMP) && glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS) {
            commandsSet.add(JUMP);
            lockKey(JUMP);
        }
        if(!getKeyValue(JUMP) && glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_RELEASE) unlockKey(JUMP);

        if (getKeyValue(REMOVE) && glfwGetKey(window, GLFW_KEY_R) == GLFW_PRESS) {
            commandsSet.add(REMOVE);
            lockKey(REMOVE);
        }
        if(!getKeyValue(REMOVE) && glfwGetKey(window, GLFW_KEY_R) == GLFW_RELEASE) unlockKey(REMOVE);

        if (getKeyValue(ADD) && glfwGetKey(window, GLFW_KEY_T) == GLFW_PRESS) {
            commandsSet.add(ADD);
            lockKey(ADD);
        }
        if(!getKeyValue(ADD) && glfwGetKey(window, GLFW_KEY_T) == GLFW_RELEASE) unlockKey(ADD);
    }
}
