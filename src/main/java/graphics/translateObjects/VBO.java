package graphics.translateObjects;

import graphics.DataTransformation;

import static org.lwjgl.opengl.GL33.*;

/**Vertex Buffer Object, буфер вершин*/
public class VBO {
    private final int id;
    private final DataTransformation dataTransformation;

    protected VBO(DataTransformation dataTransformation) {
        id = glGenBuffers();
        this.dataTransformation = dataTransformation;
    }

    protected void bindRefresh() {
        bind();
        refresh();
    }

    private void refresh() {
        glBufferData(GL_ARRAY_BUFFER, dataTransformation.getCords(), GL_DYNAMIC_DRAW);
    }

    protected void bind() {
        glBindBuffer(GL_ARRAY_BUFFER, id);
    }

    protected void unbind() {
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    protected void delete() {
        glDeleteBuffers(id);
    }
}
