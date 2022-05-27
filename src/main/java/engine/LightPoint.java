package engine;

import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * Источник света
 */
public class LightPoint {
    /**
     * координата источника света
     */
    public Vector3f cord;
    /**
     * цвет источника света
     */
    public Vector4f color;

    /**Конструктор источника света
     *
     * @param cord координата источника света
     * @param color цвет источника света
     */
    public LightPoint(Vector3f cord, Vector4f color) {
        this.cord = cord;
        this.color = color;
    }
}
