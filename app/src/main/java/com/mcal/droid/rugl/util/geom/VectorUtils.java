package com.mcal.droid.rugl.util.geom;

import androidx.annotation.NonNull;

/**
 * 2D vector utilities
 */
public class VectorUtils {
    /**
     * Rotates a vector through 90 degrees
     *
     * @param v The vector to rotate
     */
    public static void rotate90(@NonNull Vector2f v) {
        float x = v.x;

        v.x = -v.y;
        v.y = x;
    }

    /**
     * Rotates a vector through -90 degrees
     *
     * @param v The vector to rotate
     */
    public static void rotateMinus90(@NonNull Vector2f v) {
        float y = -v.x;

        v.x = v.y;
        v.y = y;
    }

    /**
     * Rotates a vector
     *
     * @param v     The vector to rotate
     * @param angle The angle through which to rotate
     */
    public static void rotate(@NonNull Vector2f v, float angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        float x = v.x;
        float y = v.y;

        v.x = (float) (cos * x - sin * y);
        v.y = (float) (sin * x + cos * y);
    }

    /**
     * Computes the square of the distance between two points
     *
     * @param v A point
     * @param w A point
     * @return The square of the distance between v and w
     */
    public static float distanceSquared(@NonNull ReadableVector2f v, @NonNull ReadableVector2f w) {
        float dx = v.getX() - w.getX();
        float dy = v.getY() - w.getY();
        return dx * dx + dy * dy;
    }

    /**
     * Computes the distance between two points
     *
     * @param v A point
     * @param w A point
     * @return The distance between v and w
     */
    public static float distance(Vector2f v, Vector2f w) {
        return (float) Math.sqrt(distanceSquared(v, w));
    }

    /**
     * Computes the scale of the projection of v onto w.
     *
     * @param v A vector
     * @param w Another vector
     * @return The amount by which to scale w to get the components of
     * v that lie parallel to w
     */
    public static float projection(Vector2f v, Vector2f w) {
        float vd = Vector2f.dot(v, w);

        return vd / w.lengthSquared();
    }
}
