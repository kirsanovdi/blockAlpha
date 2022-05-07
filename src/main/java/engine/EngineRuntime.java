package engine;

import controller.RTController;
import org.joml.Vector3i;

import java.util.HashMap;
import java.util.Random;

public class EngineRuntime {

    public HashMap<Vector3i, Block> blocks;

    private final RTController rtController;

    public EngineRuntime(RTController rtController, Block[] initBlocks){
        this.rtController = rtController;
        blocks = new HashMap<>();
        for (Block initBlock : initBlocks) {
            if(initBlock == null) throw new RuntimeException("initBlock was null");
            blocks.put(initBlock.cord, initBlock);
        }
    }

    public static Block[] generateRndBlocks(int count, int cordBound){
        Random random = new Random();
        HashMap<Vector3i, Block> genBlocks = new HashMap<>();
        for(int i = 0; i < count; i++){
            Block block = new Block(
                    new Vector3i(random.nextInt(cordBound), random.nextInt(cordBound), random.nextInt(cordBound)),
                    0
            );
            genBlocks.put(block.cord, block);
        }
        return genBlocks.values().toArray(new Block[0]);
    }

    public void run(){

    }
}
