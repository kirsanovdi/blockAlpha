package controller;

public class Settings {
    public static final int translationSize = 20000000;

    public static final String textureName = "texturePack.png";
    public static final int textureWidth = 4096;
    public static final int textureHeight = 4096;

    public static final float blockSize = 1.0f;

    public static final float rayPrecision = 100f;
    public static final int rayDistance = 100;

    public static final float fovDeg = 100.0f;

    public boolean debug;
    public Settings(){
        debug = false;
    }
}
