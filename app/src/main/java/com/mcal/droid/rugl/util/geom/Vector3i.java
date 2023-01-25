package com.mcal.droid.rugl.util.geom;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;

/**
 * Integer-based vector
 */
public class Vector3i {
    /***/
    public int x;

    /***/
    public int y;

    /***/
    public int z;

    public Vector3i(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3i(@NonNull Vector3i vector) {
        this(vector.x, vector.y, vector.z);
    }

    public Vector3i() {

    }

    public Vector3i(@NonNull Vector3f vector) {
        this((int) vector.x, (int) vector.y, (int) vector.z);
    }

    public static int distance(Vector3i vec1, Vector3i vec2) {
        return (int) Math.sqrt(squaredDistance(vec1, vec2));
    }

    @Contract(pure = true)
    public static int squaredDistance(@NonNull Vector3i vec1, @NonNull Vector3i vec2) {
        int dx = vec1.x - vec2.x;
        int dy = vec1.y - vec2.y;
        int dz = vec1.z - vec2.z;
        return (dx * dx) + (dy * dy) + (dz * dz);
    }

    /**
     * @param v
     */
    public void set(@NonNull Vector3i v) {
        x = v.x;
        y = v.y;
        z = v.z;
    }

    /**
     * @param x
     * @param y
     * @param z
     */
    public void set(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Vector3i) {
            Vector3i v = (Vector3i) o;
            return x == v.x && y == v.y && z == v.z;
        }

        return false;
    }

    @NonNull
    @Override
    public String toString() {
        return "( " + x + ", " + y + ", " + z + " )";
    }
}
