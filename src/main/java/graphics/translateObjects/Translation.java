package graphics.translateObjects;

import graphics.DataTransformation;

/**
 * Транслятор данных в GPU
 */
public class Translation {
    /**
     * Вершинный буфер
     */
    private final VBO vertexBufferObject;
    /**
     * Массив вершин
     */
    private final VAO vertexArrayObject;
    /**
     * Элементный(индексный) буфер
     */
    private final EBO elementBufferObject;

    /**
     * Конструктор
     * @param dataTransformation преобразователь игфромации, откуда берутся данные для вершин и индексов
     */
    public Translation(DataTransformation dataTransformation){
        vertexArrayObject = new VAO();
        vertexArrayObject.bind();
        vertexBufferObject = new VBO(dataTransformation);
        elementBufferObject = new EBO(dataTransformation);

        vertexBufferObject.bindRefresh();
        elementBufferObject.bindRefresh();

        vertexArrayObject.LinkAttrib(vertexBufferObject, 0, 32, 0);
        vertexArrayObject.LinkAttrib(vertexBufferObject, 1, 32, 12);
        vertexArrayObject.LinkAttrib(vertexBufferObject, 2, 32, 20);

        vertexArrayObject.unbind();
        vertexBufferObject.unbind();
        elementBufferObject.unbind();
    }

    /**
     * Повторная отправка данных
     */
    public void update(){
        vertexArrayObject.bind();
        vertexBufferObject.bindRefresh();
        elementBufferObject.bindRefresh();

        vertexArrayObject.LinkAttrib(vertexBufferObject, 0, 32, 0);
        vertexArrayObject.LinkAttrib(vertexBufferObject, 1, 32, 12);
        vertexArrayObject.LinkAttrib(vertexBufferObject, 2, 32, 20);

        vertexArrayObject.unbind();
        vertexBufferObject.unbind();
        elementBufferObject.unbind();
    }

    /**
     * Инициализация
     */
    public void setupVAO(){
        vertexArrayObject.bind();
    }

    /**
     * Удаление VAO, VBO, EBO
     */
    public void destroy(){
        vertexArrayObject.delete();
        vertexBufferObject.delete();
        elementBufferObject.delete();
    }
}
