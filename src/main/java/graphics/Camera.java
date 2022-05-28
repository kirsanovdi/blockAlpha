package graphics;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL46.glGetUniformLocation;
import static org.lwjgl.opengl.GL46.glUniformMatrix4fv;

/**
 * Камера
 */
public class Camera {
    /**
     * Вектор, указывающий направлекие вверх
     */
    private final Vector3f up;
    /**
     * Ширина экрана
     */
    public final int width;
    /**
     * Высота экрана
     */
    public final int height;

    /**
     * Позиция камеры
     */
    public final Vector3f position;
    /**
     * Ориентация камеры
     */
    public final Vector3f orientation;
    /**
     * Чувствительность мыши
     */
    public float sensitivity = 100.0f;
    /**
     * Число Пи
     */
    private static final float pi = 3.14159265359f;
    /**
     * Идентификатор для контроля зажатия кнопки мыши
     */
    private boolean firstClick;

    /**
     * Идентификатор для контроля захвата курсора
     */
    private boolean cursorHookHandler = true;

    /**
     * Конструктор камеры
     * @param width ширина экрана
     * @param height высота экрана
     * @param position начальная позиция камеры
     */
    public Camera(int width, int height, Vector3f position) {
        up = new Vector3f(0.0f, 1.0f, 0.0f);
        firstClick = false;
        this.height = height;
        this.width = width;
        this.orientation = new Vector3f(0.0f, 0.0f, -1.0f);
        this.position = position;
    }

    /**
     * Расчёт и передача в шейдер матрицы камеры
     * @param FOVdeg угол обзора
     * @param nearPlane близость прорисовки
     * @param farPlane дальность прорисовки
     * @param shader шейдер
     * @param uniform uniform для матрицы камеры
     */
    public void Matrix(float FOVdeg, float nearPlane, float farPlane, Shader shader, String uniform) {
        Matrix4f view = new Matrix4f();
        Vector3f center = new Vector3f().add(position).add(orientation);
        view = view.lookAt(position, center, up);
        Matrix4f proj = new Matrix4f();
        proj = proj.perspective(FOVdeg / 180.0f * pi, (float) (width / height), nearPlane, farPlane);

        int projLoc = glGetUniformLocation(shader.getId(), uniform);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            glUniformMatrix4fv(projLoc, false, proj.mul(view).get(stack.mallocFloat(16)));
        } catch (Exception e) {
            System.out.println(e + " error in Camera");
        }
    }

    /**
     * Задание позиции камеры.
     * Переменная при этом не создаётся заново, происходит лишь изменение её полей
     * @param position координаты новой позиции камеры
     */
    public void setPos(Vector3f position) {
        this.position.x = position.x;
        this.position.y = position.y;
        this.position.z = position.z;
    }

    /**
     * Контроль ввода передвижения курсора
     * @param window окно GUI
     */
    public void mouseInput(long window) {
        if (glfwGetKey(window, GLFW_KEY_LEFT_BRACKET) == GLFW_PRESS) {
            cursorHookHandler = true;
        }

        if (cursorHookHandler) {
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
            if (firstClick) {
                glfwSetCursorPos(window, width / 2.0f, height / 2.0f);
                firstClick = false;
            }
            double[] mouseX = {0.0}, mouseY = {0.0};
            glfwGetCursorPos(window, mouseX, mouseY);
            float rotX = sensitivity * (float) (mouseY[0] - height / 2.0) / height;
            float rotY = sensitivity * (float) (mouseX[0] - width / 2.0) / width;

            Vector3f newOrientation = new Vector3f(orientation);


            newOrientation.rotateY(-rotY / 180.0f * pi);
            newOrientation.rotateX((float) (rotX / 180.0f * Math.sin(orientation.z * pi / 2.0) * pi));
            newOrientation.rotateZ((float) (-rotX / 180.0f * Math.sin(orientation.x * pi / 2.0) * pi));

            if (newOrientation.angle(up) > 5.0 / 180.0 * pi && newOrientation.angle(up) < 175.0 / 180.0 * pi) {
                orientation.x = newOrientation.x;
                orientation.y = newOrientation.y;
                orientation.z = newOrientation.z;
            }
            orientation.normalize();
            glfwSetCursorPos(window, width / 2.0f, height / 2.0f);
        }
        if (glfwGetKey(window, GLFW_KEY_RIGHT_BRACKET) == GLFW_PRESS) {
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
            firstClick = true;
            cursorHookHandler = false;
        }
    }
}
