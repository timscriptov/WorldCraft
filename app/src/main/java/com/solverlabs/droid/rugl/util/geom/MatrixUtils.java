package com.solverlabs.droid.rugl.util.geom;

import android.opengl.Matrix;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;

/**
 * @author ryanm
 */
public class MatrixUtils {
    private static final Matrix4f id = new Matrix4f();

    private static final float[] idM = new float[16];

    static {
        Matrix.setIdentityM(idM, 0);
    }

    /**
     * Tests if a matrix is the identity matrix
     *
     * @param m
     * @return <code>true</code> iff all elements are 0, apart from the
     * main diagonal which are all 1
     */
    @Contract(pure = true)
    public static boolean isIdentity(@NonNull Matrix4f m) {
        return m.m00 == id.m00 && m.m01 == id.m01 && m.m02 == id.m02 && m.m03 == id.m03
                && m.m10 == id.m10 && m.m11 == id.m11 && m.m12 == id.m12 && m.m13 == id.m13
                && m.m20 == id.m20 && m.m21 == id.m21 && m.m22 == id.m22 && m.m23 == id.m23
                && m.m30 == id.m30 && m.m31 == id.m31 && m.m32 == id.m32 && m.m33 == id.m33;
    }

    /**
     * @param m
     * @return <code>true</code> if the array contains the identity
     * matrix
     */
    @Contract(pure = true)
    public static boolean isIdentity(@NonNull float[] m) {
        if (m.length == 16) {
            for (int i = 0; i < m.length; i++) {
                if (m[i] != idM[i]) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    /**
     * @param angle
     * @param x
     * @param y
     * @return A matrix to rotate around a point
     */
    @NonNull
    public static Matrix4f rotateAround(float angle, float x, float y) {
        Matrix4f m = new Matrix4f();

        m.translate(new Vector2f(x, y));
        m.rotate(angle, 0, 0, 1);
        m.translate(new Vector2f(-x, -y));

        return m;
    }

    /**
     * @param scale
     * @param x
     * @param y
     * @return A matrix to scale around a point
     */
    @NonNull
    public static Matrix4f scaleAround(float scale, float x, float y) {
        Matrix4f m = new Matrix4f();

        m.translate(new Vector2f(x, y));
        m.scale(new Vector3f(scale, scale, scale));
        m.translate(new Vector2f(-x, -y));

        return m;
    }
}
