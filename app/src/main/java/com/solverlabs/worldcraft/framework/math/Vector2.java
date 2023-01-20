package com.solverlabs.worldcraft.framework.math;


import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.util.FloatMath;

import org.jetbrains.annotations.Contract;

public class Vector2 {
    public float x;
    public float y;
    public static float TO_RADIANS = 0.017453294f;
    public static float TO_DEGREES = 57.295776f;

    public Vector2() {
    }

    public Vector2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vector2(@NonNull Vector2 other) {
        this.x = other.x;
        this.y = other.y;
    }

    public Vector2 cpy() {
        return new Vector2(this.x, this.y);
    }

    public Vector2 set(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public Vector2 set(@NonNull Vector2 other) {
        this.x = other.x;
        this.y = other.y;
        return this;
    }

    public Vector2 add(float x, float y) {
        this.x += x;
        this.y += y;
        return this;
    }

    public Vector2 add(@NonNull Vector2 other) {
        this.x += other.x;
        this.y += other.y;
        return this;
    }

    public Vector2 sub(float x, float y) {
        this.x -= x;
        this.y -= y;
        return this;
    }

    public Vector2 sub(@NonNull Vector2 other) {
        this.x -= other.x;
        this.y -= other.y;
        return this;
    }

    public Vector2 mul(float scalar) {
        this.x *= scalar;
        this.y *= scalar;
        return this;
    }

    public float len() {
        return FloatMath.sqrt((this.x * this.x) + (this.y * this.y));
    }

    public Vector2 nor() {
        float len = len();
        if (len != 0.0f) {
            this.x /= len;
            this.y /= len;
        }
        return this;
    }

    public float angle() {
        float angle = ((float) Math.atan2(this.y, this.x)) * TO_DEGREES;
        if (angle < 0.0f) {
            return angle + 360.0f;
        }
        return angle;
    }

    public Vector2 rotate(float angle) {
        float rad = angle * TO_RADIANS;
        float cos = FloatMath.cos(rad);
        float sin = FloatMath.sin(rad);
        float newX = (this.x * cos) - (this.y * sin);
        float newY = (this.x * sin) + (this.y * cos);
        this.x = newX;
        this.y = newY;
        return this;
    }

    public float dist(@NonNull Vector2 other) {
        float distX = this.x - other.x;
        float distY = this.y - other.y;
        return FloatMath.sqrt((distX * distX) + (distY * distY));
    }

    public float dist(float x, float y) {
        float distX = this.x - x;
        float distY = this.y - y;
        return FloatMath.sqrt((distX * distX) + (distY * distY));
    }

    public float distSquared(@NonNull Vector2 other) {
        float distX = this.x - other.x;
        float distY = this.y - other.y;
        return (distX * distX) + (distY * distY);
    }

    public float distSquared(float x, float y) {
        float distX = this.x - x;
        float distY = this.y - y;
        return (distX * distX) + (distY * distY);
    }
}
