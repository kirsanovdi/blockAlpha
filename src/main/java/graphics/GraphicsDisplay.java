package graphics;

import controller.RTController;
import controller.Settings;
import engine.LightPoint;
import graphics.translateObjects.Translation;
import org.joml.Random;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.util.Set;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.opengl.GL46.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Графический вывод, GUI
 */
public class GraphicsDisplay {

    /**
     * Ширина окна
     */
    public final int width;

    /**
     * Высота окна
     */
    public final int height;

    /**
     * Название окна
     */
    private final String name;

    /**
     * Идентификатор окна
     */
    private long window;

    /**
     * Вспомогательные поля
     */
    private double frames, lastTime;

    /**
     * Контроллер
     */
    private final RTController rtController;

    /**
     * Камера
     */
    private final Camera camera;

    /**
     * GLSL код вершинного шейдера
     */
    String vertexShaderSource;

    /**
     * GLSL код фрагментного шейдера
     */
    String fragmentShaderSource;


    /**
     * Подсчёт и вывод fps
     */
    private void printRenderTime() {
        frames++;
        final double currentTime = glfwGetTime();
        if (currentTime - lastTime > 2.0) {
            System.out.println("gui \t" + frames / 2.0);
            lastTime = currentTime;
            frames = 0;
        }
    }

    /**
     * Основной коструктор GraphicsDisplay,
     *
     * @param rtController контроллер
     * @param width        ширина окна
     * @param height       высота окна
     * @param name         название окна
     */
    public GraphicsDisplay(RTController rtController, int width, int height, String name) {
        this.height = height;
        this.width = width;
        this.name = name;
        this.camera = new Camera(width, height, new Vector3f(0.0f, 0.0f, 2.0f));
        this.rtController = rtController;

        try {
            vertexShaderSource = Files.readString(new File("src/main/java/graphics/translateObjects/vertexShader").toPath());
            fragmentShaderSource = Files.readString(new File("src/main/java/graphics/translateObjects/fragmentShader").toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Запуск GUI
     */
    public void run() {
        System.out.println("GraphicsDisplay has launched with LWJGL " + Version.getVersion());

        init();
        loop();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    /**
     * Getter для камеры
     *
     * @return камера
     */
    public Camera getCamera() {
        return camera;
    }

    /**
     * Инициализация GLFW, окна и Callback функций
     */
    private void init() {
        // Set up an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        // Create the window
        window = glfwCreateWindow(width, height, name, NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        // Set up a key callback. It will be called every time a key is pressed, repeated or released.
        /*glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if ((key == GLFW_KEY_ESCAPE || key == GLFW_KEY_Q) && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true);
            }
        });*/

        // Get the thread stack and push a new frame
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            if (videoMode == null) throw new RuntimeException("Failed ti get resolution of the primary monitor");

            // Center the window
            glfwSetWindowPos(
                    window,
                    (videoMode.width() - pWidth.get(0)) / 2,
                    (videoMode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);
    }

    /**
     * Передача значений источников света в шейдер
     *
     * @param shader шейдер
     */
    private void translateLightPoints(Shader shader) {
        int lightSize = glGetUniformLocation(shader.getId(), "lightSize");
        int lightColor = glGetUniformLocation(shader.getId(), "lightColor");
        int lightPos = glGetUniformLocation(shader.getId(), "lightPos");

        Set<LightPoint> lightPoints = rtController.getEngineRuntime().lightPoints;
        final int size = lightPoints.size();
        float[] cords = new float[size * 3], colors = new float[size * 4];
        int count = 0;
        for (LightPoint point : lightPoints) {
            colors[count * 4] = point.color.x;
            colors[count * 4 + 1] = point.color.y;
            colors[count * 4 + 2] = point.color.z;
            colors[count * 4 + 3] = point.color.w;
            cords[count * 3] = point.cord.x;
            cords[count * 3 + 1] = point.cord.y;
            cords[count * 3 + 2] = point.cord.z;
            count++;
        }

        glUniform1i(lightSize, size);
        glUniform4fv(lightColor, colors);
        glUniform3fv(lightPos, cords);
    }

    /**
     * Метод, содержащий цикл отрисовки окна
     */
    private void loop() {
        GL.createCapabilities();
        glClearColor(1.0f, 1.0f, 1.0f, 0.0f);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);

        Shader shader = new Shader(vertexShaderSource, fragmentShaderSource);

        Texture texture = new Texture(Settings.textureName, 0, Settings.textureWidth, Settings.textureHeight);
        texture.texUnit(shader, "tex0");

        Texture texture2 = new Texture("texturePackSpecularMap.png", 1, Settings.textureWidth, Settings.textureHeight);
        texture2.texUnit(shader, "tex1");


        DataTransformation dataTransformation = new DataTransformation(rtController);

        Translation translation = new Translation(dataTransformation);

        while (!glfwWindowShouldClose(window)) {

            dataTransformation.update();
            translation.update();


            glClearColor(0.07f, 0.13f, 0.17f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            shader.activate();

            rtController.Input(window);
            camera.mouseInput(window);
            camera.Matrix(45.0f, 0.1f, 10000.0f, shader, "camMatrix");

            translateLightPoints(shader);

            int camPos = glGetUniformLocation(shader.getId(), "camPos");
            glUniform3fv(camPos, new float[]{camera.position.x, camera.position.y, camera.position.z});


            texture.bind();
            texture2.bind();
            translation.setupVAO();

            glDrawElements(GL_TRIANGLES, dataTransformation.indicesSize(), GL_UNSIGNED_INT, 0);

            glfwSwapBuffers(window);
            glfwPollEvents();
            //printRenderTime();
        }

        translation.destroy();
        texture.delete();
        shader.delete();

    }
}
