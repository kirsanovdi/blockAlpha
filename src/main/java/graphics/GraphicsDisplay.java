package graphics;

import controller.RTController;
import controller.Settings;
import graphics.translateObjects.Translation;
import org.joml.Vector3f;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class GraphicsDisplay {

    /**Параметры GUI*/
    public final int width, height;
    private final String name;
    private long window;
    /**Вспомогательные поля*/
    private double frames, lastTime;
    
    private final RTController rtController;
    private final Camera camera;

    /**Подсчёт и вывод fps*/
    private void printRenderTime() {
        frames++;
        final double currentTime = glfwGetTime();
        if (currentTime - lastTime > 2.0) {
            System.out.println("gui \t" + frames/2.0);
            lastTime = currentTime;
            frames = 0;
        }
    }

    String vertexShaderSource = """
            #version 330 core
            layout (location = 0) in vec3 aPos;
            layout (location = 1) in vec2 aTex;
            out vec2 texCoord;
                        
            uniform mat4 camMatrix;
                        
            void main()
            {
                gl_Position = camMatrix * vec4(aPos, 1.0f);
                texCoord = aTex;
            }
            """;

    String fragmentShaderSource = """
            #version 330 core
            out vec4 FragColor;
            in vec2 texCoord;
            uniform sampler2D tex0;
            void main()
            {
                if(texture(tex0, texCoord).r + texture(tex0, texCoord).g + texture(tex0, texCoord).b > 2.9) discard;
                FragColor = texture(tex0, texCoord);
            }
            """;

    /**Основной коструктор*/
    public GraphicsDisplay(RTController rtController, int width, int height, String name) {
        this.height = height;
        this.width = width;
        this.name = name;
        this.camera = new Camera(width, height, new Vector3f(0.0f, 0.0f, 2.0f));
        this.rtController = rtController;
    }

    /**Запуск GUI*/
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

    public Camera getCamera(){
        return camera;
    }

    /**инициализация GLFW, окна и Callback функций*/
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
            if(videoMode == null) throw new RuntimeException("Failed ti get resolution of the primary monitor");

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

    /**Метод, содержащий цикл отрисовки окна*/
    private void loop() {
        GL.createCapabilities();
        glClearColor(1.0f, 1.0f, 1.0f, 0.0f);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);

        Shader shader = new Shader(vertexShaderSource, fragmentShaderSource);

        Texture texture = new Texture(Settings.textureName, Settings.textureWidth, Settings.textureHeight);
        texture.texUnit(shader, "tex0", 0);


        DataTransformation dataTransformation = new DataTransformation(rtController);

        Translation translation = new Translation(dataTransformation);

        //dataTransformation.update();
        //translation.update();

        while (!glfwWindowShouldClose(window)) {

            dataTransformation.update();
            translation.update();


            glClearColor(0.07f, 0.13f, 0.17f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            shader.activate();

            rtController.Input(window);
            camera.mouseInput(window);
            camera.Matrix(45.0f, 0.1f, 10000.0f, shader, "camMatrix");

            texture.bind();
            translation.setupVAO();

            glDrawElements(GL_TRIANGLES, dataTransformation.indicesSize(), GL_UNSIGNED_INT, 0);

            glfwSwapBuffers(window);
            glfwPollEvents();
            //System.out.println(rtController.camera.orientation);
            //printRenderTime();
        }

        translation.destroy();
        texture.delete();
        shader.delete();

    }
}
