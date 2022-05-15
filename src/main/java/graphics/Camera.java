package graphics;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.glGetUniformLocation;
import static org.lwjgl.opengl.GL33.glUniformMatrix4fv;

public class Camera {
    private final Vector3f up;
    public final int width, height;

    public final Vector3f position, orientation;
    public float sensitivity = 100.0f;

    private static final float pi = 3.14159265359f;
    private boolean firstClick;

    public Camera(int width, int height, Vector3f position) {
        up = new Vector3f(0.0f, 1.0f, 0.0f);
        firstClick = false;
        this.height = height;
        this.width = width;
        this.orientation = new Vector3f(0.0f, 0.0f, -1.0f);
        this.position = position;
    }

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

    public void setPos(Vector3f position){
        this.position.x = position.x;
        this.position.y = position.y;
        this.position.z = position.z;
    }

    public void mouseInput(long window) {
        if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS) {
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

            glfwSetCursorPos(window, width / 2.0f, height / 2.0f);
        }
        if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_RELEASE) {
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
            firstClick = true;
        }
    }
}
