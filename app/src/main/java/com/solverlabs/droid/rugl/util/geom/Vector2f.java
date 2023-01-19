package com.solverlabs.droid.rugl.util.geom;

import java.nio.FloatBuffer;


public class Vector2f extends Vector implements ReadableVector2f, WritableVector2f {
    private static final long serialVersionUID = 1;
    public float x;
    public float y;

    public Vector2f() {
    }

    public Vector2f(ReadableVector2f src) {
        set(src);
    }

    public Vector2f(float x, float y) {
        set(x, y);
    }

    public static float dot(Vector2f left, Vector2f right) {
        return (left.x * right.x) + (left.y * right.y);
    }

    public static float angle(Vector2f a, Vector2f b) {
        float dls = dot(a, b) / (a.length() * b.length());
        if (dls < -1.0f) {
            dls = -1.0f;
        } else if (dls > 1.0f) {
            dls = 1.0f;
        }
        return (float) Math.acos(dls);
    }

    public static Vector2f add(Vector2f left, Vector2f right, Vector2f dest) {
        if (dest == null) {
            return new Vector2f(left.x + right.x, left.y + right.y);
        }
        dest.set(left.x + right.x, left.y + right.y);
        return dest;
    }

    public static Vector2f sub(Vector2f left, Vector2f right, Vector2f dest) {
        if (dest == null) {
            return new Vector2f(left.x - right.x, left.y - right.y);
        }
        dest.set(left.x - right.x, left.y - right.y);
        return dest;
    }

    @Override
    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vector2f set(ReadableVector2f src) {
        this.x = src.getX();
        this.y = src.getY();
        return this;
    }

    @Override
    public float lengthSquared() {
        return (this.x * this.x) + (this.y * this.y);
    }

    public Vector2f translate(float x, float y) {
        this.x += x;
        this.y += y;
        return this;
    }

    @Override
    public Vector negate() {
        this.x = -this.x;
        this.y = -this.y;
        return this;
    }

    public Vector2f negate(Vector2f dest) {
        if (dest == null) {
            dest = new Vector2f();
        }
        dest.x = -this.x;
        dest.y = -this.y;
        return dest;
    }

    public Vector2f normalise(Vector2f dest) {
        float l = length();
        if (dest == null) {
            return new Vector2f(this.x / l, this.y / l);
        }
        dest.set(this.x / l, this.y / l);
        return dest;
    }

    @Override
    public Vector store(FloatBuffer buf) {
        buf.put(this.x);
        buf.put(this.y);
        return this;
    }

    @Override
    public Vector load(FloatBuffer buf) {
        this.x = buf.get();
        this.y = buf.get();
        return this;
    }

    @Override
    public Vector scale(float scale) {
        this.x *= scale;
        this.y *= scale;
        return this;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(64);
        sb.append("Vector2f[");
        sb.append(this.x);
        sb.append(", ");
        sb.append(this.y);
        sb.append(']');
        return sb.toString();
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
}
