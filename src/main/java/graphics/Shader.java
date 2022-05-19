package graphics;

import static org.lwjgl.opengl.GL46.*;
import static org.lwjgl.opengl.GL46.glDeleteProgram;

public class Shader {
    private int id;

    Shader(String vertexString, String fragmentString) {
        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertexString);
        glCompileShader(vertexShader);

        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, fragmentString);
        glCompileShader(fragmentShader);

        id = glCreateProgram();
        //System.out.println(id);
        glAttachShader(id, vertexShader);
        glAttachShader(id, fragmentShader);
        glLinkProgram(id);

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }

    public int getId() {
        return id;
    }

    void activate() {
        glUseProgram(id);
    }

    void delete() {
        glDeleteProgram(id);
    }
}
