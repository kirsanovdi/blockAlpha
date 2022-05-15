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
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static controller.Commands.*;

public class EngineRuntime {

    public ConcurrentHashMap<Vector3i, Block> blocks;
    private final RTController rtController;

    private final Model model;

    private Vector3i selectedCord, lastCord;

    public EngineRuntime(RTController rtController, Block[] initBlocks) {
        this.rtController = rtController;
        model = new Model(new Vector3f(0.0f, 0.0f, 0.0f), 0.5f, rtController.getGraphicsDisplay().getCamera());
        selectedCord = null;
        lastCord = null;
        setBlocks(initBlocks);
    }

    private void setBlocks(Block[] initBlocks) {
        blocks = new ConcurrentHashMap<>();
        for (Block initBlock : initBlocks) {
            if (initBlock == null) throw new RuntimeException("initBlock was null");
            blocks.put(initBlock.cord, initBlock);
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
            ConcurrentHashMap<Vector3i, Block> newBlocks = new ConcurrentHashMap<>();
            for (String line : Files.lines(new File(fileName).toPath()).toList()) {
                final int[] cords = parseLine(line, Pattern.compile("cord=\\[.*?]"), 7, 2, Pattern.compile("\\s+"));
                final int id = parseLine(line, Pattern.compile("id=.*?,"), 3, 1, Pattern.compile("\\s+"))[0];
                final int[] sideIds = parseLine(line, Pattern.compile("sideIds=\\[.*?]"), 9, 1, Pattern.compile(",\\s+"));

                if (cords.length != 3 || sideIds.length != 6) throw new RuntimeException("state corruption");

                Vector3i vector3i = new Vector3i(cords[0], cords[1], cords[2]);
                newBlocks.put(vector3i, new Block(vector3i, id, sideIds));
            }
            blocks = newBlocks;
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

    public static Block[] generateRndBlocks(int count, int cordBound) {
        Random random = new Random();
        HashMap<Vector3i, Block> genBlocks = new HashMap<>();
        for (int i = 0; i < count; i++) {
            Block block = new Block(
                    new Vector3i(random.nextInt(cordBound), random.nextInt(cordBound), random.nextInt(cordBound)),
                    0,
                    new int[]{random.nextInt(6),
                            random.nextInt(6),
                            random.nextInt(6),
                            random.nextInt(6),
                            random.nextInt(6),
                            random.nextInt(6)}
            );
            genBlocks.put(block.cord, block);
        }
        return genBlocks.values().toArray(new Block[0]);
    }

    public boolean checkCord(Vector3i vector3i) {
        return blocks.containsKey(vector3i);
    }

    protected void checkNearest() {
        Vector3f pos = model.getPosition();
        Vector3i modelInt = new Vector3i((int) pos.x - 2, (int) pos.y - 2, (int) pos.z - 2);
        for (int dx = 0; dx < 5; dx++) {
            for (int dy = 0; dy < 5; dy++) {
                for (int dz = 0; dz < 5; dz++) {
                    Vector3i checkedVector3i = new Vector3i(modelInt).add(dx, dy, dz);
                    if (checkCord(checkedVector3i)) {
                        System.out.println(System.currentTimeMillis() + "\t" + checkedVector3i + " is near");
                    }
                }
            }
        }
    }

    protected void rayTrace() {
        final Vector3f orientation = model.getOrientation();
        final Vector3f pos = model.getPosition();
        final Vector3i startPosI = new Vector3i((int) pos.x, (int) pos.y, (int) pos.z);
        final Vector3f dir = new Vector3f(orientation).div(Settings.rayPrecision);


        Vector3i lastCheckedPos = null;
        lastCord = null;
        selectedCord = null;

        for (int i = 0; i < Settings.rayDistance * Settings.rayPrecision; i++) {
            pos.add(dir);
            Vector3i posI = new Vector3i((int) pos.x, (int) pos.y, (int) pos.z);
            if (checkCord(posI)) {
                if (lastCheckedPos != null && !lastCheckedPos.equals(startPosI)) {
                    lastCord = lastCheckedPos;
                    selectedCord = posI;
                }
                break;
            }
            lastCheckedPos = posI;
        }
        //if(lastCheckedPos == null) throw new RuntimeException("rayTrace from existing block");
    }

    public void createBlock(Vector3i vector3i) {
        blocks.put(vector3i, new Block(vector3i, 0, new int[]{1, 1, 1, 1, 1, 1}));
    }

    private void handleInput(Set<Commands> commandsSet) {
        if (commandsSet.contains(REMOVE) && selectedCord != null) blocks.remove(selectedCord);
        if (commandsSet.contains(ADD) && lastCord != null) createBlock(lastCord);
    }

    public void run() {
        System.out.println("engineRuntime started");
        while (rtController.isRunning()) {
            model.handleInput(rtController.commandsSet);
            this.handleInput(rtController.commandsSet);
            rayTrace();
            try {
                Thread.sleep(5L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("engineRuntime finished");
    }
}
