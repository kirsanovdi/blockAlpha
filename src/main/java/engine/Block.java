package engine;

import org.joml.Vector3i;

import java.util.Arrays;

public class Block {
    public Vector3i cord;
    public final int id;
    public final int[] sideIds;
    public final boolean[] sideRender;

    public Block(Vector3i cord, int id, int[] sideIds) {
        this.cord = cord;
        this.id = id;
        this.sideIds = sideIds;
        this.sideRender = new boolean[]{true, true, true, true, true, true};
    }

    @Override
    public String toString() {
        return "Block: cord=[\t" + cord.x + '\t' + cord.y + '\t' + cord.z + "\t],\tid=" + id +
                ",\tsideIds=" + Arrays.toString(sideIds);
    }
}
