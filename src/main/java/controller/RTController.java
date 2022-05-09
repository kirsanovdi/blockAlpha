package controller;

import engine.EngineRuntime;
import graphics.GraphicsDisplay;
import graphics.Shader;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;

public class RTController {
    private EngineRuntime engineRuntime;
    private GraphicsDisplay graphicsDisplay;

    public Camera camera;

    private boolean isRunning = true;

    public void run() {
        if(engineRuntime == null) throw new RuntimeException("engineRuntime was null");
        if(graphicsDisplay == null) throw new RuntimeException("graphicsDisplay was null");

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

    public void setupCamera(){
        camera = new Camera(graphicsDisplay.width, graphicsDisplay.height, new Vector3f(0.0f, 0.0f, 2.0f), graphicsDisplay, this);
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

    public void environmentInput(long window){
        if (glfwGetKey(window, GLFW_KEY_Q) == GLFW_PRESS) {
            glfwSetWindowShouldClose(window, true);
            toClose();
        }

        if (glfwGetKey(window, GLFW_KEY_Z) == GLFW_PRESS) {
            engineRuntime.saveState("state1");
        }
        if (glfwGetKey(window, GLFW_KEY_X) == GLFW_PRESS) {
            engineRuntime.loadState("state1");
        }

        if (glfwGetKey(window, GLFW_KEY_C) == GLFW_PRESS) {
            engineRuntime.saveState("state2");
        }
        if (glfwGetKey(window, GLFW_KEY_V) == GLFW_PRESS) {
            engineRuntime.loadState("state2");
        }
    }

    public void changeSpeed(long window){
        if (glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS) {
            camera.changeSpeed(1.0f);
        }
        if (glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == GLFW_RELEASE) {
            camera.changeSpeed(0.1f);
        }
        if (glfwGetKey(window, GLFW_KEY_LEFT_CONTROL) == GLFW_PRESS) {
            camera.changeSpeed(0.025f);
        }
    }

    public void modelInput(long window){
        final Vector3f convertOrientation = new Vector3f(camera.orientation);
        convertOrientation.y = 0f;
        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) {
            camera.moveForward(convertOrientation);
        }
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
            camera.moveBackward(convertOrientation);
        }
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) {
            camera.moveLeft(convertOrientation);
        }
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) {
            camera.moveRight(convertOrientation);
        }
        camera.changeSpeed(window);
        camera.mouseInput(window);
    }

    public void updateCamera(Shader shader){
        camera.Matrix(45.0f, 0.1f, 10000.0f, shader, "camMatrix");
    }
}
