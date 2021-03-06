package graphics;

import controller.RTController;
import controller.Settings;
import engine.Block;
import engine.EngineRuntime;
import engine.Line;
import org.joml.Vector3f;

/**
 * Преобразователь данных движка в данные для отправки в видеопроцессор
 */
public class DataTransformation {
    /**
     * Движок
     */
    private final EngineRuntime engineRuntime;
    /**
     * Массив индексов вершин
     */
    private final int[] indicesRaw;
    /**
     * Массив значений вершин
     */
    private final float[] cordsRaw;

    /**
     * Вспомогательные поля
     */
    private int sizeI = 0, sizeC = 0, verticesCount = 0;

    /**
     * Конструктор преобразователя
     * @param controller контроллер
     */
    DataTransformation(RTController controller) {
        this.engineRuntime = controller.getEngineRuntime();
        indicesRaw = new int[Settings.translationSize];
        cordsRaw = new float[Settings.translationSize];
    }

    /**
     * Метод вызова обновления данных для трансфера
     */
    public void update() {
        reset();
        for (Block block : engineRuntime.blocks.values()) {
            transferBlock(block);
        }
        synchronized (engineRuntime.lines) {
            for (Line line : engineRuntime.lines) {
                transferLine(line);
            }
        }
    }

    /**
     * Getter для получения массива преобразованных данных вершин
     * @return массив преобразованных данных вершин
     */
    public float[] getCords() {
        final float[] cords = new float[sizeC];
        System.arraycopy(cordsRaw, 0, cords, 0, sizeC);
        return cords;
    }

    /**
     * Getter для получения массива индексов
     * @return массив индексов
     */
    public int[] getIndices() {
        final int[] indices = new int[sizeI];
        System.arraycopy(indicesRaw, 0, indices, 0, sizeI);
        return indices;
    }

    /**
     * Добавление нового пакета данных в массиы значений и индексов
     * @param cords массив добавляемых значений вершин
     * @param indices массив добавляемых индексов
     */
    private void transfer(float[] cords, int[] indices) {
        System.arraycopy(cords, 0, cordsRaw, sizeC, cords.length);
        System.arraycopy(indices, 0, indicesRaw, sizeI, indices.length);
        sizeC += cords.length;
        sizeI += indices.length;
    }

    /**
     * Обнуление значений вспомогательных полей
     */
    private void reset() {
        sizeI = 0;
        sizeC = 0;
        verticesCount = 0;
    }

    /**
     * Размер массива индексов
     * @return размер массива индексов
     */
    public int indicesSize() {
        return sizeI;
    }

    /**
     * Преобразование и передача данных четырёхугольника(квадрата) в массивы индексов и значений вершин
     * @param a первая координата квадрата
     * @param b вторая координата квадрата
     * @param c третья координата квадрата
     * @param d четвёртая координата квадрата
     * @param id id стороны
     */
    public void transferSquare(Vector3f a, Vector3f b, Vector3f c, Vector3f d, long id) {
        final float yId = (float) (id / 16L) / 16.0f;
        final float xId = (float) (id % 16L) / 16.0f;
        final float delta = 1.0f / 16.0f;

        final float kX = (b.y - a.y) * (c.z - a.z) - (c.y - a.y) * ( b.z - a.z);
        final float kY = (c.x - a.x) * (b.z - a.z) - (b.x - a.x) * ( c.z - a.z);
        final float kZ = (b.x - a.x) * (c.y - a.y) - (c.x - a.x) * ( b.y - a.y);

        final float[] tempCordsRaw = new float[]{
                a.x, a.y, a.z, xId,         yId        , kX, kY, kZ,
                b.x, b.y, b.z, xId,         yId + delta, kX, kY, kZ,
                c.x, c.y, c.z, xId + delta, yId + delta, kX, kY, kZ,
                d.x, d.y, d.z, xId + delta, yId        , kX, kY, kZ
        };
        final int[] tempIndicesRaw = new int[]{
                verticesCount, verticesCount + 2, verticesCount + 1,
                verticesCount, verticesCount + 3, verticesCount + 2
        };
        verticesCount += 4;
        transfer(tempCordsRaw, tempIndicesRaw);
    }

    /**
     * Преобразование и передача данных отрезка в массивы индексов и значений вершин
     * @param line прямая(отрезок)
     */
    private void transferLine(Line line){
        final float delta = 0.001f;
        Vector3f startUp = new Vector3f(line.start).add(0.0f, delta, 0.0f);
        Vector3f startDown = new Vector3f(line.start).add(0.0f, -delta, 0.0f);
        Vector3f endUp = new Vector3f(line.end).add(0.0f, delta, 0.0f);
        Vector3f endDown = new Vector3f(line.end).add(0.0f, -delta, 0.0f);
        transferSquare(startUp, startDown, endDown, endUp, 17);
        transferSquare(endUp, endDown, startDown, startUp, 17);
    }

    /**
     * Преобразование и передача данных блока в массивы индексов и значений вершин
     * @param block блок(куб)
     */
    public void transferBlock(Block block) {
        final float delta = Settings.blockSize / 2.0f;
        final Vector3f center = new Vector3f(block.cord).add(delta, delta, delta);
        final Vector3f[] vertex = new Vector3f[]{
                new Vector3f(center.x - delta, center.y - delta, center.z - delta),//0 - far down left
                new Vector3f(center.x + delta, center.y - delta, center.z - delta),//1 - far down right
                new Vector3f(center.x - delta, center.y + delta, center.z - delta),//2 - far up left
                new Vector3f(center.x + delta, center.y + delta, center.z - delta),//3 - far up right
                new Vector3f(center.x - delta, center.y - delta, center.z + delta),//4 - near down left
                new Vector3f(center.x + delta, center.y - delta, center.z + delta),//5 - near down right
                new Vector3f(center.x - delta, center.y + delta, center.z + delta),//6 - near up left
                new Vector3f(center.x + delta, center.y + delta, center.z + delta) //7 - near up right
        };
        if (block.sideRender[0]) transferSquare(vertex[4], vertex[6], vertex[7], vertex[5], block.sideIds[0]);//near
        if (block.sideRender[1]) transferSquare(vertex[6], vertex[2], vertex[3], vertex[7], block.sideIds[1]);//up
        if (block.sideRender[2]) transferSquare(vertex[0], vertex[4], vertex[5], vertex[1], block.sideIds[2]);//down
        if (block.sideRender[3]) transferSquare(vertex[1], vertex[3], vertex[2], vertex[0], block.sideIds[3]);//far
        if (block.sideRender[4]) transferSquare(vertex[0], vertex[2], vertex[6], vertex[4], block.sideIds[4]);//left
        if (block.sideRender[5]) transferSquare(vertex[5], vertex[7], vertex[3], vertex[1], block.sideIds[5]);//right
    }
}
