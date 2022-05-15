package graphics;

import controller.RTController;
import controller.Settings;
import engine.Block;
import engine.EngineRuntime;
import org.joml.Vector3f;

public class DataTransformation {
    private final EngineRuntime engineRuntime;
    private final int[] indicesRaw;
    private final float[] cordsRaw;

    private int sizeI = 0, sizeC = 0, verticesCount = 0;

    DataTransformation(RTController controller) {
        this.engineRuntime = controller.getEngineRuntime();
        indicesRaw = new int[Settings.translationSize];
        cordsRaw = new float[Settings.translationSize];
    }

    public void update() {
        reset();
        for(Block block: engineRuntime.blocks.values()){
            transferBlock(block);
        }
    }

    public float[] getCords() {
        final float[] cords = new float[sizeC];
        System.arraycopy(cordsRaw, 0, cords, 0, sizeC);
        return cords;
    }

    public int[] getIndices() {
        final int[] indices = new int[sizeI];
        System.arraycopy(indicesRaw, 0, indices, 0, sizeI);
        return indices;
    }

    private void transfer(float[] cords, int[] indices) {
        System.arraycopy(cords, 0, cordsRaw, sizeC, cords.length);
        System.arraycopy(indices, 0, indicesRaw, sizeI, indices.length);
        sizeC += cords.length;
        sizeI += indices.length;
    }

    private void reset() {
        sizeI = 0;
        sizeC = 0;
        verticesCount = 0;
    }

    public int indicesSize() {
        return sizeI;
    }

    public void transferSquare(Vector3f a, Vector3f b, Vector3f c, Vector3f d, long id) {
        final float yId = (float) id % 16L;
        final float xId = (float) id / 16L;
        final float delta = 1.0f / 16.0f;
        final float[] tempCordsRaw = new float[]{
                a.x, a.y, a.z, xId,         yId,
                b.x, b.y, b.z, xId,         yId + delta,
                c.x, c.y, c.z, xId + delta, yId + delta,
                d.x, d.y, d.z, xId + delta, yId
        };
        final int[] tempIndicesRaw = new int[]{
                verticesCount, verticesCount + 2, verticesCount + 1,
                verticesCount, verticesCount + 3, verticesCount + 2
        };
        verticesCount += 4;
        transfer(tempCordsRaw, tempIndicesRaw);
    }

    public void transferBlock(Block block){
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
        transferSquare(vertex[4], vertex[6], vertex[7], vertex[5], block.sideIds[0]);//near
        transferSquare(vertex[6], vertex[2], vertex[3], vertex[7], block.sideIds[1]);//up
        transferSquare(vertex[0], vertex[4], vertex[5], vertex[1], block.sideIds[2]);//down
        transferSquare(vertex[1], vertex[3], vertex[2], vertex[0], block.sideIds[3]);//far
        transferSquare(vertex[0], vertex[2], vertex[6], vertex[4], block.sideIds[4]);//left
        transferSquare(vertex[5], vertex[7], vertex[3], vertex[1], block.sideIds[5]);//right
    }
}
