package engine;

import controller.Commands;
import controller.RTController;
import controller.Settings;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static controller.Commands.*;

/**
 * Движок игры, в кором прописаны и обрабатываются основные взаимодействия со сценой
 */
public class EngineRuntime {

    /**
     * Хеш-таблица "координата(Vector3i) - блок(Block)" с безопасным многопоточным доступом
     */
    public final ConcurrentHashMap<Vector3i, Block> blocks;
    /**
     * Множество прямых
     */
    public final Set<Line> lines;
    /**
     * Множество источников света
     */
    public final Set<LightPoint> lightPoints;
    /**
     * Контроллер
     */
    private final RTController rtController;
    /**
     * Модель персонажа
     */
    private final Model model;
    /**
     * Настройки сцены
     */
    private final Settings settings;
    /**
     * Координата блока, на который смотрит камера
     */
    private Vector3i selectedCord;
    /**
     * Последняя обработанная в результате rayTrace() координата вне блоков
     */
    private Vector3i lastCord;
    /**
     * Первая обработанная в результате rayTrace() координата внутри блока
     */
    private Vector3f selectedFloatCord;

    /**
     * Конструктор класса EngineRuntime
     *
     * @param rtController контроллер
     * @param initBlocks   массив блоков для дефотной сцены
     */
    public EngineRuntime(RTController rtController, Block[] initBlocks) {
        System.out.println("Инициализация EngineRuntime");
        this.rtController = rtController;
        model = new Model(new Vector3f(0.0f, 10.0f, 0.0f), 0.5f, rtController.getGraphicsDisplay().getCamera());
        settings = new Settings();
        selectedCord = null;
        selectedFloatCord = null;
        lastCord = null;
        blocks = new ConcurrentHashMap<>();
        setBlocks(initBlocks);
        lines = new HashSet<>();
        lightPoints = new HashSet<>();
        lightPoints.add(new LightPoint(new Vector3f(0.0f, 7.0f, -10.0f), new Vector4f(1.0f, 0.0f, 0.0f, 1.0f)));
        lightPoints.add(new LightPoint(new Vector3f(0.0f, 7.0f, 10.0f), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f)));
        System.out.println("Инициализация EngineRuntime завершена");
    }

    /**
     * Инизциализация блоков сцены из массива блоков
     *
     * @param initBlocks массив блоков
     */
    private void setBlocks(Block[] initBlocks) {
        for (Block initBlock : initBlocks) {
            if (initBlock == null) throw new RuntimeException("initBlock was null");
            blocks.put(initBlock.cord, initBlock);
            updateBlockSpace(initBlock.cord);
        }
    }

    /**
     * Удаление всех блоков с заданным id
     *
     * @param id id удаляемых блоков
     */
    private void removeId(int id) {
        for (Block block : blocks.values()) {
            if (block.id == id && block.cord != lastCord) blocks.remove(block.cord);
        }
    }

    /**
     * Сохранение сцены
     *
     * @param fileName файл, в которой созраняется сцена
     */
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

    /**
     * Загрузка сцены
     *
     * @param fileName файл, из которого загружаеся сцена
     */
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

    /**
     * Парсер значений, применяемый для чтения сцены из файла
     *
     * @param line    строка со значенимями
     * @param pattern паттерн, указывающий на местонахождение значений
     * @param st      длина начала паттерна
     * @param delta   длина конца паттерна
     * @param split   сплиттер
     * @return массив значений типа int
     */
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

    /**
     * Генерация платформы из блоков в начале координат
     *
     * @param delta расстояние от центра до края платформы
     * @return массив сгенерированных блоков
     */
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

    /**
     * Проверка на существование блока в координате vector3i
     *
     * @param vector3i проверяемая координата
     * @return наличие блока на указанной координате
     */
    public boolean checkCord(Vector3i vector3i) {
        return blocks.containsKey(vector3i) && blocks.get(vector3i).id != -1;
    }

    /**
     * Множество координат ближайших блоков с разницей в координате <= delta
     *
     * @param delta максимальная разница в координате
     * @return множество координат существующих ближайших блоков
     */
    protected Set<Vector3i> getNearest(int delta) {
        final Set<Vector3i> vector3is = new LinkedHashSet<>();
        final Vector3f pos = model.getPosition();
        final Vector3i modelInt = getVector3i(pos).add(-delta, -delta, -delta);
        final int delta2 = delta * 2;
        for (int dx = 0; dx <= delta2; dx++) {
            for (int dy = 0; dy < delta2; dy++) {
                for (int dz = 0; dz < delta2; dz++) {
                    Vector3i checkedVector3i = new Vector3i(modelInt).add(dx, dy, dz);
                    if (checkCord(checkedVector3i)) {
                        vector3is.add(checkedVector3i);
                    }
                }
            }
        }
        return vector3is;
    }

    /**
     * Преобразование Vector3f в Vector3i c округлением вниз,
     * в том числе отрицательных чисел
     *
     * @param v3f преобразуемый Vector3f
     * @return новый Vector3i
     */
    protected static Vector3i getVector3i(Vector3f v3f) {
        return new Vector3i((int) v3f.x + (v3f.x < 0 ? -1 : 0), (int) v3f.y + (v3f.y < 0 ? -1 : 0), (int) v3f.z + (v3f.z < 0 ? -1 : 0));
    }

    /**
     * Просчёт взаимодействия луча направления ориентации и мира
     * при взаимодействии с миром selectedCord - первый блок на пути луча
     */
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

    /**
     * Создание блока с последующим обновлением прилежащих блоков
     *
     * @param vector3i координаты создаваемого блока
     * @param id       id блока
     * @param sideId   id всех боковых сторон
     */
    private void createBlock(Vector3i vector3i, int id, int sideId) {
        createBlock(vector3i, id, new int[]{sideId, sideId, sideId, sideId, sideId, sideId});
    }

    /**
     * Создание блока с последующим обновлением прилежащих блоков
     *
     * @param vector3i координаты создаваемого блока
     * @param id       id блока
     * @param sideIds  массив[6] id боковых сторон
     */
    private void createBlock(Vector3i vector3i, int id, int[] sideIds) {
        Vector3i v3i = new Vector3i(vector3i);
        blocks.put(v3i, new Block(v3i, id, sideIds));
        updateBlockSpace(vector3i);
    }

    /**
     * Удаление блока с последующим обновлением прилежащих блоков
     *
     * @param vector3i координаты удаляемого блока
     */
    private void removeBlock(Vector3i vector3i) {
        blocks.remove(vector3i);
        updateBlockSpace(vector3i);
    }

    /**
     * Вызов двустороннего обновления сторон при постановке блока,
     * производится проверка на существование блока в координате anotherCord
     *
     * @param block       поставленный блок
     * @param anotherCord координаты парного блока
     * @param sideR       id обновляемой стороны поставленного блока
     * @param sideAR      id обновляемой стороны парного блока
     */
    private void pairUpdate(Block block, Vector3i anotherCord, int sideR, int sideAR) {
        if (checkCord(anotherCord)) {
            block.sideRender[sideR] = false;
            blocks.get(anotherCord).sideRender[sideAR] = false;
        } else {
            block.sideRender[sideR] = true;
        }
    }

    /**
     * Вызов обновления стороны блока,
     * производится проверка на существование блока в координате anotherCord
     *
     * @param anotherCord координаты обновляемого блока
     * @param sideAR      id обновляемой стороны
     */
    private void pairDeleteUpdate(Vector3i anotherCord, int sideAR) {
        if (checkCord(anotherCord)) blocks.get(anotherCord).sideRender[sideAR] = true;
    }

    /**
     * Обновление придежащих сторон ближайших блоков для указанной координаты
     *
     * @param vector3i координата, вокруг которой проверяются блоки
     */
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

    /**
     * Обработка ввода команд для движка
     *
     * @param commandsSet множество команд из контроллера
     */
    private void handleInput(Set<Commands> commandsSet) {
        if (commandsSet.contains(REMOVE) && selectedCord != null) removeBlock(selectedCord);
        if (commandsSet.contains(ADD) && lastCord != null && model.checkPosToPlace(lastCord))
            createBlock(lastCord, 1, 17);
        if (commandsSet.contains(START_DEBUG) && selectedCord != null) {
            synchronized (lines) {
                lines.clear();
                lines.add(new Line(model.getCameraPosition(), new Vector3f(selectedCord)));//settings.debug = true;
            }
        }
        if (commandsSet.contains(END_DEBUG) && selectedFloatCord != null) {
            lightPoints.add(new LightPoint(new Vector3f(selectedFloatCord).add(0.0f, 2.0f, 0.0f), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f)));
        }
    }

    /**
     * Метод для запуска основного цикла движка
     */
    public void run() {
        System.out.println("EngineRuntime started");
        while (rtController.isRunning()) {
            rayTrace();

            model.handleInput(rtController.commandsSet, getNearest(2));
            this.handleInput(rtController.commandsSet);

            synchronized (lines) {
                lines.clear();
                if (selectedFloatCord != null)
                    lines.add(new Line(model.getCameraPosition().add(model.getOrientation().normalize().cross(new Vector3f(0.0f, 1.0f, 0.0f)).mul(0.2f)), new Vector3f(selectedFloatCord)));//settings.debug = true;
            }

            try {
                Thread.sleep(5L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("EngineRuntime finished");
    }
}
