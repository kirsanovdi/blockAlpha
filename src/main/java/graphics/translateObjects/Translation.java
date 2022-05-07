package graphics.translateObjects;

import graphics.DataTransformation;

public class Translation {
    private final VBO vertexBufferObject;
    private final VAO vertexArrayObject;
    private final EBO elementBufferObject;

    public Translation(DataTransformation dataTransformation){
        vertexArrayObject = new VAO();
        vertexArrayObject.bind();
        vertexBufferObject = new VBO(dataTransformation);
        elementBufferObject = new EBO(dataTransformation);

        vertexBufferObject.bindRefresh();
        elementBufferObject.bindRefresh();

        vertexArrayObject.LinkAttrib(vertexBufferObject, 0, 20, 0);
        vertexArrayObject.LinkAttrib(vertexBufferObject, 1, 20, 12);

        vertexArrayObject.unbind();
        vertexBufferObject.unbind();
        elementBufferObject.unbind();
    }

    public void update(){
        vertexArrayObject.bind();
        vertexBufferObject.bindRefresh();
        elementBufferObject.bindRefresh();

        vertexArrayObject.LinkAttrib(vertexBufferObject, 0, 20, 0);
        vertexArrayObject.LinkAttrib(vertexBufferObject, 1, 20, 12);

        vertexArrayObject.unbind();
        vertexBufferObject.unbind();
        elementBufferObject.unbind();
    }

    public void setupVAO(){
        vertexArrayObject.bind();
    }

    public void destroy(){
        vertexArrayObject.delete();
        vertexBufferObject.delete();
        elementBufferObject.delete();
    }
}
