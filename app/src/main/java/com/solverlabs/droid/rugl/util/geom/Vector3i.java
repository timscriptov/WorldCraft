package com.solverlabs.droid.rugl.util.geom;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;

public class Vector3i {
    public int x;
    public int y;
    public int z;

    public Vector3i(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3i(@NonNull Vector3i vector) {
        this(vector.x, vector.y, vector.z);
    }

    public Vector3i(@NonNull Vector3f vector) {
        this((int) vector.x, (int) vector.y, (int) vector.z);
    }

    public Vector3i() {
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

    public void set(@NonNull Vector3i v) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
    }

    public void set(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public boolean equals(Object o) {
        if (o instanceof Vector3i) {
            Vector3i v = (Vector3i) o;
            return this.x == v.x && this.y == v.y && this.z == v.z;
        }
        return false;
    }

    public int hashCode() {
        return (this.x ^ this.y) ^ this.z;
    }

    @NonNull
    public String toString() {
        return "( " + this.x + ", " + this.y + ", " + this.z + " )";
    }
}
