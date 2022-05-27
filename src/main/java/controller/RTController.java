package controller;

import engine.EngineRuntime;
import graphics.GraphicsDisplay;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.lwjgl.glfw.GLFW.*;
import static controller.Commands.*;

/**
 * контроллер программы
 */
public class RTController {
    /**
     * Движок
     */
    private EngineRuntime engineRuntime;
    /**
     * Графический дисплей
     */
    private GraphicsDisplay graphicsDisplay;
    /**
     * Множество команд
     */
    public Set<Commands> commandsSet;
    /**
     * Данные для команд, которые требуют одиночного нажатия
     */
    private HashMap<Commands, Boolean> commandsHashSet;
    /**
     * Индикатор выполнения программы
     */
    private boolean isRunning = true;

    /**
     * Метод для запуска приложения
     */
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
        Thread thread = new Thread(engineRuntime::run);
        thread.start();
        graphicsDisplay.run();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Метод для захвата контроля над GUI
     *
     * @param graphicsDisplay захватываемый графический дисплей
     */
    public void hookGraphicsDisplay(GraphicsDisplay graphicsDisplay) {
        this.graphicsDisplay = graphicsDisplay;
    }

    /**
     * Метод для захвата контроля над Engine
     *
     * @param engineRuntime захватываемый движок
     */
    public void hookEngineRuntime(EngineRuntime engineRuntime) {
        this.engineRuntime = engineRuntime;
    }

    /**
     * Getter для движка
     *
     * @return движок
     */
    public EngineRuntime getEngineRuntime() {
        return engineRuntime;
    }

    /**
     * @return графический дисплей
     */
    public GraphicsDisplay getGraphicsDisplay() {
        return graphicsDisplay;
    }

    /**
     * Индикатор выполнения приложения
     *
     * @return выполняется ли приложение
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Метод вызова закрытия приложения
     */
    public void toClose() {
        isRunning = false;
    }

    /**
     * метод для блокировки ключа комманды
     *
     * @param command комманда
     */
    private void lockKey(Commands command) {
        commandsHashSet.put(command, false);
    }

    /**
     * метод для разблокировки ключа комманды
     *
     * @param command комманда
     */
    private void unlockKey(Commands command) {
        commandsHashSet.put(command, true);
    }

    /**
     * метод для получения значения ключа для данной комманды
     *
     * @param command комманда
     * @return ключ комманды
     */
    private boolean getKeyValue(Commands command) {
        return commandsHashSet.get(command);
    }

    /**
     * метод для обработки комманд с единичным значением
     *
     * @param window  идентификатор окна
     * @param command комманда
     * @param key     ключ
     */
    private void keyHandler(long window, Commands command, int key) {
        if (getKeyValue(command) && glfwGetKey(window, key) == GLFW_PRESS) {
            commandsSet.add(command);
            lockKey(command);
        }
        if (!getKeyValue(command) && glfwGetKey(window, key) == GLFW_RELEASE) unlockKey(command);
    }

    /**
     * Преобразование клавиатурного ввода в команды
     *
     * @param window идентификатор окна
     */
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
