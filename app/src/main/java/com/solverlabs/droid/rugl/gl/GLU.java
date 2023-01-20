package com.solverlabs.droid.rugl.gl;

import android.opengl.GLES10;
import android.opengl.Matrix;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.Contract;

import java.util.Arrays;


public class GLU {
    private static final float[] sScratch = new float[32];

    @Nullable
    @Contract(pure = true)
    public static String gluErrorString(int error) {
        switch (error) {
            case 0:
                return "no error";
            case 1280:
                return "invalid enum";
            case 1281:
                return "invalid value";
            case 1282:
                return "invalid operation";
            case 1283:
                return "stack overflow";
            case 1284:
                return "stack underflow";
            case 1285:
                return "out of memory";
            default:
                return null;
        }
    }

    public static void gluLookAt(float eyeX, float eyeY, float eyeZ, float centerX, float centerY, float centerZ, float upX, float upY, float upZ, float[] matrix) {
        float[] scratch = matrix != null ? matrix : sScratch;
        setLookAtM(scratch, 0, eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
        GLES10.glMultMatrixf(scratch, 0);
    }

    public static void gluOrtho2D(float left, float right, float bottom, float top) {
        GLES10.glOrthof(left, right, bottom, top, -1.0f, 1.0f);
    }

    public static void gluPerspective(float fovy, float aspect, float near, float far, float[] matrix) {
        float top = near * ((float) Math.tan((fovy * 3.141592653589793d) / 360.0d));
        float bottom = -top;
        float left = bottom * aspect;
        float right = top * aspect;
        if (matrix != null) {
            frustum(left, right, top, bottom, near, far, matrix);
            GLES10.glMultMatrixf(matrix, 0);
            return;
        }
        GLES10.glFrustumf(left, right, bottom, top, near, far);
    }

    private static void frustum(float left, float right, float top, float bottom, float near, float far, float[] matrix) {
        float a = (right + left) / (right - left);
        float b = (top + bottom) / (top - bottom);
        float c = (-(far + near)) / (far - near);
        float d = (-((2.0f * far) * near)) / (far - near);
        Arrays.fill(matrix, 0.0f);
        matrix[0] = (2.0f * near) / (right - left);
        matrix[8] = a;
        matrix[5] = (2.0f * near) / (top - bottom);
        matrix[9] = b;
        matrix[10] = c;
        matrix[14] = d;
        matrix[11] = -1.0f;
    }

    public static boolean gluProject(float objX, float objY, float objZ, float[] model, int modelOffset, float[] project, int projectOffset, int[] view, int viewOffset, float[] win, int winOffset) {
        float[] scratch = sScratch;
        Matrix.multiplyMM(scratch, 0, project, projectOffset, model, modelOffset);
        scratch[16] = objX;
        scratch[17] = objY;
        scratch[18] = objZ;
        scratch[19] = 1.0f;
        Matrix.multiplyMV(scratch, 20, scratch, 0, scratch, 16);
        float w = scratch[23];
        if (w == 0.0f) {
            return false;
        }
        float rw = 1.0f / w;
        win[winOffset] = view[viewOffset] + (view[viewOffset + 2] * ((scratch[20] * rw) + 1.0f) * 0.5f);
        win[winOffset + 1] = view[viewOffset + 1] + (view[viewOffset + 3] * ((scratch[21] * rw) + 1.0f) * 0.5f);
        win[winOffset + 2] = ((scratch[22] * rw) + 1.0f) * 0.5f;
        return true;
    }

    public static boolean gluUnProject(float winX, float winY, float winZ, float[] model, int modelOffset, float[] project, int projectOffset, int[] view, int viewOffset, float[] obj, int objOffset) {
        float[] scratch = sScratch;
        Matrix.multiplyMM(scratch, 0, project, projectOffset, model, modelOffset);
        if (!Matrix.invertM(scratch, 16, scratch, 0)) {
            return false;
        }
        scratch[0] = ((2.0f * (winX - view[viewOffset + 0])) / view[viewOffset + 2]) - 1.0f;
        scratch[1] = ((2.0f * (winY - view[viewOffset + 1])) / view[viewOffset + 3]) - 1.0f;
        scratch[2] = (2.0f * winZ) - 1.0f;
        scratch[3] = 1.0f;
        Matrix.multiplyMV(obj, objOffset, scratch, 16, scratch, 0);
        return true;
    }

    private static void setLookAtM(@NonNull float[] rm, int rmOffset, float eyeX, float eyeY, float eyeZ, float centerX, float centerY, float centerZ, float upX, float upY, float upZ) {
        float fx = centerX - eyeX;
        float fy = centerY - eyeY;
        float fz = centerZ - eyeZ;
        float rlf = 1.0f / Matrix.length(fx, fy, fz);
        float fx2 = fx * rlf;
        float fy2 = fy * rlf;
        float fz2 = fz * rlf;
        float sx = (fy2 * upZ) - (fz2 * upY);
        float sy = (fz2 * upX) - (fx2 * upZ);
        float sz = (fx2 * upY) - (fy2 * upX);
        float rls = 1.0f / Matrix.length(sx, sy, sz);
        float sx2 = sx * rls;
        float sy2 = sy * rls;
        float sz2 = sz * rls;
        float ux = (sy2 * fz2) - (sz2 * fy2);
        float uy = (sz2 * fx2) - (sx2 * fz2);
        float uz = (sx2 * fy2) - (sy2 * fx2);
        rm[rmOffset + 0] = sx2;
        rm[rmOffset + 1] = ux;
        rm[rmOffset + 2] = -fx2;
        rm[rmOffset + 3] = 0.0f;
        rm[rmOffset + 4] = sy2;
        rm[rmOffset + 5] = uy;
        rm[rmOffset + 6] = -fy2;
        rm[rmOffset + 7] = 0.0f;
        rm[rmOffset + 8] = sz2;
        rm[rmOffset + 9] = uz;
        rm[rmOffset + 10] = -fz2;
        rm[rmOffset + 11] = 0.0f;
        rm[rmOffset + 12] = 0.0f;
        rm[rmOffset + 13] = 0.0f;
        rm[rmOffset + 14] = 0.0f;
        rm[rmOffset + 15] = 1.0f;
        Matrix.translateM(rm, rmOffset, -eyeX, -eyeY, -eyeZ);
    }
}
