package com.solverlabs.droid.rugl.util.geom;

import java.nio.FloatBuffer;


public class Vector4f extends Vector implements ReadableVector4f, WritableVector4f {
    private static final long serialVersionUID = 1;
    public float w;
    public float x;
    public float y;
    public float z;

    public Vector4f() {
    }

    public Vector4f(ReadableVector4f src) {
        set(src);
    }

    public Vector4f(float x, float y, float z, float w) {
        set(x, y, z, w);
    }

    public static Vector4f add(Vector4f left, Vector4f right, Vector4f dest) {
        if (dest == null) {
            return new Vector4f(left.x + right.x, left.y + right.y, left.z + right.z, left.w + right.w);
        }
        dest.set(left.x + right.x, left.y + right.y, left.z + right.z, left.w + right.w);
        return dest;
    }

    public static Vector4f sub(Vector4f left, Vector4f right, Vector4f dest) {
        if (dest == null) {
            return new Vector4f(left.x - right.x, left.y - right.y, left.z - right.z, left.w - right.w);
        }
        dest.set(left.x - right.x, left.y - right.y, left.z - right.z, left.w - right.w);
        return dest;
    }

    public static float dot(Vector4f left, Vector4f right) {
        return (left.x * right.x) + (left.y * right.y) + (left.z * right.z) + (left.w * right.w);
    }

    public static float angle(Vector4f a, Vector4f b) {
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

    @Override
    public void set(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Vector4f set(ReadableVector4f src) {
        this.x = src.getX();
        this.y = src.getY();
        this.z = src.getZ();
        this.w = src.getW();
        return this;
    }

    @Override
    public float lengthSquared() {
        return (this.x * this.x) + (this.y * this.y) + (this.z * this.z) + (this.w * this.w);
    }

    public Vector4f translate(float x, float y, float z, float w) {
        this.x += x;
        this.y += y;
        this.z += z;
        this.w += w;
        return this;
    }

    @Override
    public Vector negate() {
        this.x = -this.x;
        this.y = -this.y;
        this.z = -this.z;
        this.w = -this.w;
        return this;
    }

    public Vector4f negate(Vector4f dest) {
        if (dest == null) {
            dest = new Vector4f();
        }
        dest.x = -this.x;
        dest.y = -this.y;
        dest.z = -this.z;
        dest.w = -this.w;
        return dest;
    }

    public Vector4f normalise(Vector4f dest) {
        float l = length();
        if (dest == null) {
            return new Vector4f(this.x / l, this.y / l, this.z / l, this.w / l);
        }
        dest.set(this.x / l, this.y / l, this.z / l, this.w / l);
        return dest;
    }

    @Override
    public Vector load(FloatBuffer buf) {
        this.x = buf.get();
        this.y = buf.get();
        this.z = buf.get();
        this.w = buf.get();
        return this;
    }

    @Override
    public Vector scale(float scale) {
        this.x *= scale;
        this.y *= scale;
        this.z *= scale;
        this.w *= scale;
        return this;
    }

    @Override
    public Vector store(FloatBuffer buf) {
        buf.put(this.x);
        buf.put(this.y);
        buf.put(this.z);
        buf.put(this.w);
        return this;
    }

    public String toString() {
        return "Vector4f: " + this.x + " " + this.y + " " + this.z + " " + this.w;
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

    @Override
    public float getW() {
        return this.w;
    }

    @Override
    public void setW(float w) {
        this.w = w;
    }
}
