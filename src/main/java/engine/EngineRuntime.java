package engine;

import controller.Commands;
import controller.RTController;
import controller.Settings;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static controller.Commands.*;

public class EngineRuntime {

    public final ConcurrentHashMap<Vector3i, Block> blocks;
    public final Set<Line> lines;
    private final RTController rtController;

    private final Model model;
    private final Settings settings;

    private Vector3i selectedCord, lastCord;
    private Vector3f selectedFloatCord;

    public EngineRuntime(RTController rtController, Block[] initBlocks) {
        this.rtController = rtController;
        model = new Model(new Vector3f(0.0f, 10.0f, 0.0f), 0.5f, rtController.getGraphicsDisplay().getCamera());
        settings = new Settings();
        selectedCord = null;
        selectedFloatCord = null;
        lastCord = null;
        blocks = new ConcurrentHashMap<>();
        setBlocks(initBlocks);
        lines = new HashSet<>();
    }

    private void setBlocks(Block[] initBlocks) {
        for (Block initBlock : initBlocks) {
            if (initBlock == null) throw new RuntimeException("initBlock was null");
            blocks.put(initBlock.cord, initBlock);
            updateBlockSpace(initBlock.cord);
        }
    }

    private void removeId(int id){
        for(Block block: blocks.values()){
            if(block.id == id && block.cord != lastCord) blocks.remove(block.cord);
        }
    }

    public void saveState(String fileName) {
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            FileWriter fileWriter = new FileWriter(fileName, false);
            for (Block block : blocks.values()) {
                fileWriter.append(block.toString()).append(String.valueOf('\n'));
            }
            fileWriter.flush();
            System.out.println("state " + fileName + " saved");
        } catch (IOException ex) {
            System.out.println("Cannot save state");
            System.out.println("file: " + ex.getMessage());
        }
    }

    public void loadState(String fileName) {
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            blocks.clear();
            for (String line : Files.lines(new File(fileName).toPath()).toList()) {
                final int[] cords = parseLine(line, Pattern.compile("cord=\\[.*?]"), 7, 2, Pattern.compile("\\s+"));
                final int id = parseLine(line, Pattern.compile("id=.*?,"), 3, 1, Pattern.compile("\\s+"))[0];
                final int[] sideIds = parseLine(line, Pattern.compile("sideIds=\\[.*?]"), 9, 1, Pattern.compile(",\\s+"));

                if (cords.length != 3 || sideIds.length != 6) throw new RuntimeException("state corruption");

                Vector3i vector3i = new Vector3i(cords[0], cords[1], cords[2]);
                createBlock(vector3i, id, sideIds);
            }
            System.out.println("state " + fileName + " loaded");
        } catch (IOException ex) {
            System.out.println("Cannot load state");
            System.out.println("file: " + ex.getMessage());
        }
    }

    private static int[] parseLine(String line, Pattern pattern, int st, int delta, Pattern split) {
        Matcher matcher = pattern.matcher(line);
        if (!matcher.find()) throw new RuntimeException("Something wrong with state data");
        String cord = matcher.group();
        cord = cord.substring(st, cord.length() - delta);
        String[] cordS = cord.split(split.pattern());
        int[] result = new int[cordS.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = Integer.parseInt(cordS[i]);
        }
        return result;
    }

    public static Block[] generateBlockLayer(int delta) {
        final Block[] result = new Block[delta * delta];
        for (int z = 0; z < delta; z++) {
            for (int x = 0; x < delta; x++) {
                final int sideId = 17;
                result[z * delta + x] = new Block(new Vector3i(x - delta / 2, 5, z - delta / 2), 0, new int[]{sideId, sideId, sideId, sideId, sideId, sideId});
            }
        }
        return result;
    }

    public boolean checkCord(Vector3i vector3i) {
        return blocks.containsKey(vector3i) && blocks.get(vector3i).id != -1;
    }

    protected Set<Vector3i> getNearest() {
        final Set<Vector3i> vector3is = new LinkedHashSet<>();
        final Vector3f pos = model.getPosition();
        final Vector3i modelInt = getVector3i(pos).add(-2,-2,-2);
        for (int dx = 0; dx < 5; dx++) {
            for (int dy = 0; dy < 5; dy++) {
                for (int dz = 0; dz < 5; dz++) {
                    Vector3i checkedVector3i = new Vector3i(modelInt).add(dx, dy, dz);
                    if (checkCord(checkedVector3i)) {
                        vector3is.add(checkedVector3i);
                    }
                }
            }
        }
        return vector3is;
    }

    protected static Vector3i getVector3i(Vector3f v3f){
        return new Vector3i((int) v3f.x + (v3f.x < 0? -1: 0), (int) v3f.y + (v3f.y < 0? -1: 0), (int) v3f.z + (v3f.z < 0? -1: 0));
    }

    protected void rayTrace() {
        final Vector3f orientation = model.getOrientation();
        final Vector3f pos = model.getCameraPosition();
        final Vector3i startPosI = getVector3i(pos);
        final Vector3f dir = new Vector3f(orientation).div(Settings.rayPrecision);


        Vector3i lastCheckedPos = null;
        lastCord = null;
        selectedCord = null;
        selectedFloatCord = null;

        for (int i = 0; i < Settings.rayDistance * Settings.rayPrecision; i++) {
            pos.add(dir);
            Vector3i posI = getVector3i(pos);
            if (checkCord(posI)) {
                if (lastCheckedPos != null && !lastCheckedPos.equals(startPosI)) {
                    lastCord = lastCheckedPos;
                    selectedCord = posI;
                    selectedFloatCord = pos;
                }
                break;
            }
            lastCheckedPos = posI;
        }
    }

    private void createBlock(Vector3i vector3i, int id, int sideId) {
        createBlock(vector3i, id, new int[]{sideId, sideId, sideId, sideId, sideId, sideId});
    }

    private void createBlock(Vector3i vector3i, int id, int[] sideIds) {
        Vector3i v3i = new Vector3i(vector3i);
        blocks.put(v3i, new Block(v3i, id, sideIds));
        updateBlockSpace(vector3i);
    }

    private void removeBlock(Vector3i vector3i) {
        blocks.remove(vector3i);
        updateBlockSpace(vector3i);
    }

    private void pairUpdate(Block block, Vector3i anotherCord, int sideR, int sideAR) {
        if (checkCord(anotherCord)) {
            block.sideRender[sideR] = false;
            blocks.get(anotherCord).sideRender[sideAR] = false;
        } else {
            block.sideRender[sideR] = true;
        }
    }

    private void pairDeleteUpdate(Vector3i anotherCord, int sideAR) {
        if (checkCord(anotherCord)) blocks.get(anotherCord).sideRender[sideAR] = true;
    }

    private void updateBlockSpace(Vector3i vector3i) {
        final Vector3i center = vector3i;
        if (blocks.containsKey(vector3i)) {
            Block block = blocks.get(vector3i);
            pairUpdate(block, new Vector3i(center).add(0, 0, 1), 0, 3);
            pairUpdate(block, new Vector3i(center).add(0, 0, -1), 3, 0);
            pairUpdate(block, new Vector3i(center).add(0, 1, 0), 1, 2);
            pairUpdate(block, new Vector3i(center).add(0, -1, 0), 2, 1);
            pairUpdate(block, new Vector3i(center).add(1, 0, 0), 5, 4);
            pairUpdate(block, new Vector3i(center).add(-1, 0, 0), 4, 5);
        } else {
            pairDeleteUpdate(new Vector3i(center).add(0, 0, 1), 3);
            pairDeleteUpdate(new Vector3i(center).add(0, 0, -1), 0);
            pairDeleteUpdate(new Vector3i(center).add(0, 1, 0), 2);
            pairDeleteUpdate(new Vector3i(center).add(0, -1, 0), 1);
            pairDeleteUpdate(new Vector3i(center).add(1, 0, 0), 4);
            pairDeleteUpdate(new Vector3i(center).add(-1, 0, 0), 5);
        }
    }

    private void handleInput(Set<Commands> commandsSet) {
        if (commandsSet.contains(REMOVE) && selectedCord != null) removeBlock(selectedCord);
        if (commandsSet.contains(ADD) && lastCord != null && model.checkPosToPlace(lastCord)) createBlock(lastCord, 1, 17);
        if (commandsSet.contains(START_DEBUG) && selectedCord != null){
            synchronized (lines) {
                lines.clear();
                lines.add(new Line(model.getCameraPosition(), new Vector3f(selectedCord)));//settings.debug = true;
            }
        }
        if (commandsSet.contains(END_DEBUG)) settings.debug = false;
    }

    public void run() {
        System.out.println("engineRuntime started");
        while (rtController.isRunning()) {
            removeId(-1);
            rayTrace();

            model.handleInput(rtController.commandsSet, getNearest());
            this.handleInput(rtController.commandsSet);

            synchronized (lines) {
                lines.clear();
                if( selectedFloatCord != null) lines.add(new Line(model.getCameraPosition().add(model.getOrientation().normalize().cross(new Vector3f(0.0f, 1.0f, 0.0f)).mul(0.2f)), new Vector3f(selectedFloatCord)));//settings.debug = true;
            }

            try {
                Thread.sleep(5L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("engineRuntime finished");
    }
}
