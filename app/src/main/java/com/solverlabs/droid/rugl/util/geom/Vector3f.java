package com.solverlabs.droid.rugl.util.geom;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;

import java.nio.FloatBuffer;

public class Vector3f extends Vector implements ReadableVector3f, WritableVector3f {
    private static final long serialVersionUID = 1;
    public float x;
    public float y;
    public float z;

    public Vector3f() {
    }

    public Vector3f(ReadableVector3f src) {
        set(src);
    }

    public Vector3f(@NonNull Vector3i vec) {
        set(vec.x, vec.y, vec.z);
    }

    public Vector3f(float x, float y, float z) {
        set(x, y, z);
    }

    @NonNull
    @Contract("_, _, null -> new")
    public static Vector3f add(Vector3f left, Vector3f right, Vector3f dest) {
        if (dest == null) {
            return new Vector3f(left.x + right.x, left.y + right.y, left.z + right.z);
        }
        dest.set(left.x + right.x, left.y + right.y, left.z + right.z);
        return dest;
    }

    @NonNull
    @Contract("_, _, null -> new")
    public static Vector3f sub(Vector3f left, Vector3f right, Vector3f dest) {
        if (dest == null) {
            return new Vector3f(left.x - right.x, left.y - right.y, left.z - right.z);
        }
        dest.set(left.x - right.x, left.y - right.y, left.z - right.z);
        return dest;
    }

    @NonNull
    public static Vector3f cross(Vector3f left, Vector3f right, Vector3f dest) {
        if (dest == null) {
            dest = new Vector3f();
        }
        dest.set((left.y * right.z) - (left.z * right.y), (right.x * left.z) - (right.z * left.x), (left.x * right.y) - (left.y * right.x));
        return dest;
    }

    @Contract(pure = true)
    public static float dot(@NonNull Vector3f left, @NonNull Vector3f right) {
        return (left.x * right.x) + (left.y * right.y) + (left.z * right.z);
    }

    public static float angle(Vector3f a, Vector3f b) {
        float dls = dot(a, b) / (a.length() * b.length());
        if (dls < -1.0f) {
            dls = -1.0f;
        } else if (dls > 1.0f) {
            dls = 1.0f;
        }
        return (float) Math.acos(dls);
    }

    @Override
    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3f set(@NonNull ReadableVector3f src) {
        this.x = src.getX();
        this.y = src.getY();
        this.z = src.getZ();
        return this;
    }

    public Vector3f set(@NonNull Vector3i src) {
        this.x = src.x;
        this.y = src.y;
        this.z = src.z;
        return this;
    }

    @Override
    public float lengthSquared() {
        return (this.x * this.x) + (this.y * this.y) + (this.z * this.z);
    }

    public Vector3f translate(float x, float y, float z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    @Override
    public Vector negate() {
        this.x = -this.x;
        this.y = -this.y;
        this.z = -this.z;
        return this;
    }

    public Vector3f negate(Vector3f dest) {
        if (dest == null) {
            dest = new Vector3f();
        }
        dest.x = -this.x;
        dest.y = -this.y;
        dest.z = -this.z;
        return dest;
    }

    public Vector3f normalise(Vector3f dest) {
        float l = length();
        if (dest == null) {
            return new Vector3f(this.x / l, this.y / l, this.z / l);
        }
        dest.set(this.x / l, this.y / l, this.z / l);
        return dest;
    }

    @Override
    public Vector load(@NonNull FloatBuffer buf) {
        this.x = buf.get();
        this.y = buf.get();
        this.z = buf.get();
        return this;
    }

    @Override
    public Vector scale(float scale) {
        this.x *= scale;
        this.y *= scale;
        this.z *= scale;
        return this;
    }

    @Override
    public Vector store(@NonNull FloatBuffer buf) {
        buf.put(this.x);
        buf.put(this.y);
        buf.put(this.z);
        return this;
    }

    @NonNull
    public String toString() {
        return "Vector3f[" +
                this.x +
                ", " +
                this.y +
                ", " +
                this.z +
                ']';
    }

    @Override
    public final float getX() {
        return this.x;
    }

    @Override
    public final void setX(float x) {
        this.x = x;
    }

    @Override
    public final float getY() {
        return this.y;
    }

    @Override
    public final void setY(float y) {
        this.y = y;
    }

    @Override
    public float getZ() {
        return this.z;
    }

    @Override
    public void setZ(float z) {
        this.z = z;
    }

    public int hashCode() {
        return (Float.floatToIntBits(this.x) ^ Float.floatToIntBits(this.y)) ^ Float.floatToIntBits(this.z);
    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof Vector3f)) {
            return false;
        }
        Vector3f v = (Vector3f) o;
        return Float.compare(this.x, v.x) == 0 && Float.compare(this.y, v.y) == 0 && Float.compare(this.z, v.z) == 0;
    }

    public boolean isZeroVector() {
        return Float.compare(this.x, 0.0f) == 0 && Float.compare(this.y, 0.0f) == 0 && Float.compare(this.z, 0.0f) == 0;
    }
}
