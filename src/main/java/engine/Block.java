package engine;

import org.joml.Vector3i;

public class Block {
    public Vector3i cord;
    public final int id;
    public Block(Vector3i cord, int id){
        this.cord = cord;
        this.id = id;
    }
}
