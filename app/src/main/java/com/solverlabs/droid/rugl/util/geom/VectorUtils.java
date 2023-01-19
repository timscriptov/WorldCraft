package com.solverlabs.droid.rugl.util.geom;

import com.solverlabs.worldcraft.math.MathUtils;


public class VectorUtils {
    public static void rotate90(Vector2f v) {
        float x = v.x;
        v.x = -v.y;
        v.y = x;
    }

    public static void rotateMinus90(Vector2f v) {
        float y = -v.x;
        v.x = v.y;
        v.y = y;
    }

    public static void rotate(Vector2f v, float angle) {
        double cos = MathUtils.cos(angle);
        double sin = MathUtils.sin(angle);
        float x = v.x;
        float y = v.y;
        v.x = (float) ((x * cos) - (y * sin));
        v.y = (float) ((x * sin) + (y * cos));
    }

    public static float distanceSquared(ReadableVector2f v, ReadableVector2f w) {
        float dx = v.getX() - w.getX();
        float dy = v.getY() - w.getY();
        return (dx * dx) + (dy * dy);
    }

    public static float distance(Vector2f v, Vector2f w) {
        return (float) Math.sqrt(distanceSquared(v, w));
    }

    public static float projection(Vector2f v, Vector2f w) {
        float vd = Vector2f.dot(v, w);
        return vd / w.lengthSquared();
    }
}
