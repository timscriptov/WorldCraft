package com.solverlabs.droid.rugl.util.geom;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.Contract;

import java.nio.FloatBuffer;

public class Matrix2f extends Matrix {
    private static final long serialVersionUID = 1;
    public float m00;
    public float m01;
    public float m10;
    public float m11;

    public Matrix2f() {
        setIdentity();
    }

    public Matrix2f(Matrix2f src) {
        load(src);
    }

    @NonNull
    public static Matrix2f load(Matrix2f src, Matrix2f dest) {
        if (dest == null) {
            dest = new Matrix2f();
        }
        dest.m00 = src.m00;
        dest.m01 = src.m01;
        dest.m10 = src.m10;
        dest.m11 = src.m11;
        return dest;
    }

    @NonNull
    public static Matrix2f add(Matrix2f left, Matrix2f right, Matrix2f dest) {
        if (dest == null) {
            dest = new Matrix2f();
        }
        dest.m00 = left.m00 + right.m00;
        dest.m01 = left.m01 + right.m01;
        dest.m10 = left.m10 + right.m10;
        dest.m11 = left.m11 + right.m11;
        return dest;
    }

    @NonNull
    public static Matrix2f sub(Matrix2f left, Matrix2f right, Matrix2f dest) {
        if (dest == null) {
            dest = new Matrix2f();
        }
        dest.m00 = left.m00 - right.m00;
        dest.m01 = left.m01 - right.m01;
        dest.m10 = left.m10 - right.m10;
        dest.m11 = left.m11 - right.m11;
        return dest;
    }

    @NonNull
    public static Matrix2f mul(Matrix2f left, Matrix2f right, Matrix2f dest) {
        if (dest == null) {
            dest = new Matrix2f();
        }
        float m00 = (left.m00 * right.m00) + (left.m10 * right.m01);
        float m01 = (left.m01 * right.m00) + (left.m11 * right.m01);
        float m10 = (left.m00 * right.m10) + (left.m10 * right.m11);
        float m11 = (left.m01 * right.m10) + (left.m11 * right.m11);
        dest.m00 = m00;
        dest.m01 = m01;
        dest.m10 = m10;
        dest.m11 = m11;
        return dest;
    }

    @NonNull
    public static Vector2f transform(Matrix2f left, Vector2f right, Vector2f dest) {
        if (dest == null) {
            dest = new Vector2f();
        }
        float x = (left.m00 * right.x) + (left.m10 * right.y);
        float y = (left.m01 * right.x) + (left.m11 * right.y);
        dest.x = x;
        dest.y = y;
        return dest;
    }

    @NonNull
    public static Matrix2f transpose(Matrix2f src, Matrix2f dest) {
        if (dest == null) {
            dest = new Matrix2f();
        }
        float m01 = src.m10;
        float m10 = src.m01;
        dest.m01 = m01;
        dest.m10 = m10;
        return dest;
    }

    @Nullable
    public static Matrix2f invert(@NonNull Matrix2f src, Matrix2f dest) {
        float determinant = src.determinant();
        if (determinant != 0.0f) {
            if (dest == null) {
                dest = new Matrix2f();
            }
            float determinant_inv = 1.0f / determinant;
            float t00 = src.m11 * determinant_inv;
            float t01 = (-src.m01) * determinant_inv;
            float t11 = src.m00 * determinant_inv;
            float t10 = (-src.m10) * determinant_inv;
            dest.m00 = t00;
            dest.m01 = t01;
            dest.m10 = t10;
            dest.m11 = t11;
            return dest;
        }
        return null;
    }

    @NonNull
    public static Matrix2f negate(Matrix2f src, Matrix2f dest) {
        if (dest == null) {
            dest = new Matrix2f();
        }
        dest.m00 = -src.m00;
        dest.m01 = -src.m01;
        dest.m10 = -src.m10;
        dest.m11 = -src.m11;
        return dest;
    }

    @NonNull
    @Contract("_ -> param1")
    public static Matrix2f setIdentity(@NonNull Matrix2f src) {
        src.m00 = 1.0f;
        src.m01 = 0.0f;
        src.m10 = 0.0f;
        src.m11 = 1.0f;
        return src;
    }

    @NonNull
    @Contract("_ -> param1")
    public static Matrix2f setZero(@NonNull Matrix2f src) {
        src.m00 = 0.0f;
        src.m01 = 0.0f;
        src.m10 = 0.0f;
        src.m11 = 0.0f;
        return src;
    }

    public Matrix2f load(Matrix2f src) {
        return load(src, this);
    }

    @Override
    public Matrix load(@NonNull FloatBuffer buf) {
        this.m00 = buf.get();
        this.m01 = buf.get();
        this.m10 = buf.get();
        this.m11 = buf.get();
        return this;
    }

    @Override
    public Matrix loadTranspose(@NonNull FloatBuffer buf) {
        this.m00 = buf.get();
        this.m10 = buf.get();
        this.m01 = buf.get();
        this.m11 = buf.get();
        return this;
    }

    @Override
    public Matrix store(@NonNull FloatBuffer buf) {
        buf.put(this.m00);
        buf.put(this.m01);
        buf.put(this.m10);
        buf.put(this.m11);
        return this;
    }

    @Override
    public Matrix storeTranspose(@NonNull FloatBuffer buf) {
        buf.put(this.m00);
        buf.put(this.m10);
        buf.put(this.m01);
        buf.put(this.m11);
        return this;
    }

    @Override
    public Matrix transpose() {
        return transpose(this);
    }

    public Matrix2f transpose(Matrix2f dest) {
        return transpose(this, dest);
    }

    @Override
    public Matrix invert() {
        return invert(this, this);
    }

    @NonNull
    public String toString() {
        return String.valueOf(this.m00) + ' ' + this.m10 + ' ' + '\n' +
                this.m01 + ' ' + this.m11 + ' ' + '\n';
    }

    @Override
    public Matrix negate() {
        return negate(this);
    }

    public Matrix2f negate(Matrix2f dest) {
        return negate(this, this);
    }

    @Override
    public Matrix setIdentity() {
        return setIdentity(this);
    }

    @Override
    public Matrix setZero() {
        return setZero(this);
    }

    @Override
    public float determinant() {
        return (this.m00 * this.m11) - (this.m01 * this.m10);
    }
}
