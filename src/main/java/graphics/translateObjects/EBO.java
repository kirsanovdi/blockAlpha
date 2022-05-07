package graphics.translateObjects;

import graphics.DataTransformation;

import static org.lwjgl.opengl.GL46.*;

/**Element Buffer Object, буфер для индексов*/
public class EBO {
    private final int id;
    private final DataTransformation dataTransformation;

    protected EBO(DataTransformation dataTransformation) {
        id = glGenBuffers();
        this.dataTransformation = dataTransformation;
    }

    protected void bindRefresh() {
        bind();
        refresh();
    }

    private void refresh() {
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, dataTransformation.getIndices(), GL_DYNAMIC_DRAW);
    }

    private void bind() {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, id);
    }

    protected void unbind() {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    protected void delete() {
        glDeleteBuffers(id);
    }
}
