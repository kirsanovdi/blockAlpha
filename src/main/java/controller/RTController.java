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

    public boolean wasInputHandled;

    private boolean isRunning = true;

    public void run() {
        if (engineRuntime == null) throw new RuntimeException("engineRuntime was null");
        if (graphicsDisplay == null) throw new RuntimeException("graphicsDisplay was null");
        commandsSet = new LinkedHashSet<>();
        commandsHashSet = new HashMap<>();
        commandsHashSet.put(REMOVE, true);
        commandsHashSet.put(ADD, true);
        commandsHashSet.put(JUMP, true);
        commandsHashSet.put(START_DEBUG, true);
        commandsHashSet.put(END_DEBUG, true);
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

    public void hookGraphicsDisplay(GraphicsDisplay graphicsDisplay) {
        this.graphicsDisplay = graphicsDisplay;
    }

    public void hookEngineRuntime(EngineRuntime engineRuntime) {
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

    public void toClose() {
        isRunning = false;
    }

    private void lockKey(Commands command) {
        commandsHashSet.put(command, false);
    }

    private void unlockKey(Commands command) {
        commandsHashSet.put(command, true);
    }

    private boolean getKeyValue(Commands command) {
        return commandsHashSet.get(command);
    }

    private void keyHandler(long window, Commands command, int key){
        if (getKeyValue(command) && glfwGetKey(window, key) == GLFW_PRESS) {
            commandsSet.add(command);
            lockKey(command);
        }
        if (!getKeyValue(command) && glfwGetKey(window, key) == GLFW_RELEASE) unlockKey(command);
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

        keyHandler(window, JUMP, GLFW_KEY_SPACE);
        keyHandler(window, REMOVE, GLFW_KEY_R);
        keyHandler(window, ADD, GLFW_KEY_T);
        keyHandler(window, START_DEBUG, GLFW_KEY_1);
        keyHandler(window, END_DEBUG, GLFW_KEY_2);
    }
}
