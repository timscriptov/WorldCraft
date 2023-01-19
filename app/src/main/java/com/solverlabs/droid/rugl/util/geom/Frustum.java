package com.solverlabs.droid.rugl.util.geom;

import android.opengl.GLES11;

import java.lang.reflect.Array;

import android2.util.FloatMath;


public class Frustum {
    private static final float[] proj = new float[16];
    private static final float[] modl = new float[16];
    private static final float[] clip = new float[16];
    private final float[][] frustum = (float[][]) Array.newInstance(Float.TYPE, 6, 4);

    public Frustum() {
    }

    public Frustum(Frustum f) {
        for (int i = 0; i < this.frustum.length; i++) {
            for (int j = 0; j < this.frustum[i].length; j++) {
                this.frustum[i][j] = f.frustum[i][j];
            }
        }
    }

    public void extractFromOGL() {
        GLES11.glGetFloatv(2983, proj, 0);
        GLES11.glGetFloatv(2982, modl, 0);
        update(proj, modl);
    }

    public void update(float[] projection, float[] modelView) {
        try {
            clip[0] = (modelView[0] * projection[0]) + (modelView[1] * projection[4]) + (modelView[2] * projection[8]) + (modelView[3] * projection[12]);
            clip[1] = (modelView[0] * projection[1]) + (modelView[1] * projection[5]) + (modelView[2] * projection[9]) + (modelView[3] * projection[13]);
            clip[2] = (modelView[0] * projection[2]) + (modelView[1] * projection[6]) + (modelView[2] * projection[10]) + (modelView[3] * projection[14]);
            clip[3] = (modelView[0] * projection[3]) + (modelView[1] * projection[7]) + (modelView[2] * projection[11]) + (modelView[3] * projection[15]);
            clip[4] = (modelView[4] * projection[0]) + (modelView[5] * projection[4]) + (modelView[6] * projection[8]) + (modelView[7] * projection[12]);
            clip[5] = (modelView[4] * projection[1]) + (modelView[5] * projection[5]) + (modelView[6] * projection[9]) + (modelView[7] * projection[13]);
            clip[6] = (modelView[4] * projection[2]) + (modelView[5] * projection[6]) + (modelView[6] * projection[10]) + (modelView[7] * projection[14]);
            clip[7] = (modelView[4] * projection[3]) + (modelView[5] * projection[7]) + (modelView[6] * projection[11]) + (modelView[7] * projection[15]);
            clip[8] = (modelView[8] * projection[0]) + (modelView[9] * projection[4]) + (modelView[10] * projection[8]) + (modelView[11] * projection[12]);
            clip[9] = (modelView[8] * projection[1]) + (modelView[9] * projection[5]) + (modelView[10] * projection[9]) + (modelView[11] * projection[13]);
            clip[10] = (modelView[8] * projection[2]) + (modelView[9] * projection[6]) + (modelView[10] * projection[10]) + (modelView[11] * projection[14]);
            clip[11] = (modelView[8] * projection[3]) + (modelView[9] * projection[7]) + (modelView[10] * projection[11]) + (modelView[11] * projection[15]);
            clip[12] = (modelView[12] * projection[0]) + (modelView[13] * projection[4]) + (modelView[14] * projection[8]) + (modelView[15] * projection[12]);
            clip[13] = (modelView[12] * projection[1]) + (modelView[13] * projection[5]) + (modelView[14] * projection[9]) + (modelView[15] * projection[13]);
            clip[14] = (modelView[12] * projection[2]) + (modelView[13] * projection[6]) + (modelView[14] * projection[10]) + (modelView[15] * projection[14]);
            clip[15] = (modelView[12] * projection[3]) + (modelView[13] * projection[7]) + (modelView[14] * projection[11]) + (modelView[15] * projection[15]);
            this.frustum[0][0] = clip[3] - clip[0];
            this.frustum[0][1] = clip[7] - clip[4];
            this.frustum[0][2] = clip[11] - clip[8];
            this.frustum[0][3] = clip[15] - clip[12];
            float t = 1.0f / FloatMath.sqrt(((this.frustum[0][0] * this.frustum[0][0]) + (this.frustum[0][1] * this.frustum[0][1])) + (this.frustum[0][2] * this.frustum[0][2]));
            float[] fArr = this.frustum[0];
            fArr[0] = fArr[0] * t;
            float[] fArr2 = this.frustum[0];
            fArr2[1] = fArr2[1] * t;
            float[] fArr3 = this.frustum[0];
            fArr3[2] = fArr3[2] * t;
            float[] fArr4 = this.frustum[0];
            fArr4[3] = fArr4[3] * t;
            this.frustum[1][0] = clip[3] + clip[0];
            this.frustum[1][1] = clip[7] + clip[4];
            this.frustum[1][2] = clip[11] + clip[8];
            this.frustum[1][3] = clip[15] + clip[12];
            float t2 = 1.0f / FloatMath.sqrt(((this.frustum[1][0] * this.frustum[1][0]) + (this.frustum[1][1] * this.frustum[1][1])) + (this.frustum[1][2] * this.frustum[1][2]));
            float[] fArr5 = this.frustum[1];
            fArr5[0] = fArr5[0] * t2;
            float[] fArr6 = this.frustum[1];
            fArr6[1] = fArr6[1] * t2;
            float[] fArr7 = this.frustum[1];
            fArr7[2] = fArr7[2] * t2;
            float[] fArr8 = this.frustum[1];
            fArr8[3] = fArr8[3] * t2;
            this.frustum[2][0] = clip[3] + clip[1];
            this.frustum[2][1] = clip[7] + clip[5];
            this.frustum[2][2] = clip[11] + clip[9];
            this.frustum[2][3] = clip[15] + clip[13];
            float t3 = 1.0f / FloatMath.sqrt(((this.frustum[2][0] * this.frustum[2][0]) + (this.frustum[2][1] * this.frustum[2][1])) + (this.frustum[2][2] * this.frustum[2][2]));
            float[] fArr9 = this.frustum[2];
            fArr9[0] = fArr9[0] * t3;
            float[] fArr10 = this.frustum[2];
            fArr10[1] = fArr10[1] * t3;
            float[] fArr11 = this.frustum[2];
            fArr11[2] = fArr11[2] * t3;
            float[] fArr12 = this.frustum[2];
            fArr12[3] = fArr12[3] * t3;
            this.frustum[3][0] = clip[3] - clip[1];
            this.frustum[3][1] = clip[7] - clip[5];
            this.frustum[3][2] = clip[11] - clip[9];
            this.frustum[3][3] = clip[15] - clip[13];
            float t4 = 1.0f / FloatMath.sqrt(((this.frustum[3][0] * this.frustum[3][0]) + (this.frustum[3][1] * this.frustum[3][1])) + (this.frustum[3][2] * this.frustum[3][2]));
            float[] fArr13 = this.frustum[3];
            fArr13[0] = fArr13[0] * t4;
            float[] fArr14 = this.frustum[3];
            fArr14[1] = fArr14[1] * t4;
            float[] fArr15 = this.frustum[3];
            fArr15[2] = fArr15[2] * t4;
            float[] fArr16 = this.frustum[3];
            fArr16[3] = fArr16[3] * t4;
            this.frustum[4][0] = clip[3] - clip[2];
            this.frustum[4][1] = clip[7] - clip[6];
            this.frustum[4][2] = clip[11] - clip[10];
            this.frustum[4][3] = clip[15] - clip[14];
            float t5 = 1.0f / FloatMath.sqrt(((this.frustum[4][0] * this.frustum[4][0]) + (this.frustum[4][1] * this.frustum[4][1])) + (this.frustum[4][2] * this.frustum[4][2]));
            float[] fArr17 = this.frustum[4];
            fArr17[0] = fArr17[0] * t5;
            float[] fArr18 = this.frustum[4];
            fArr18[1] = fArr18[1] * t5;
            float[] fArr19 = this.frustum[4];
            fArr19[2] = fArr19[2] * t5;
            float[] fArr20 = this.frustum[4];
            fArr20[3] = fArr20[3] * t5;
            this.frustum[5][0] = clip[3] + clip[2];
            this.frustum[5][1] = clip[7] + clip[6];
            this.frustum[5][2] = clip[11] + clip[10];
            this.frustum[5][3] = clip[15] + clip[14];
            float t6 = 1.0f / FloatMath.sqrt(((this.frustum[5][0] * this.frustum[5][0]) + (this.frustum[5][1] * this.frustum[5][1])) + (this.frustum[5][2] * this.frustum[5][2]));
            float[] fArr21 = this.frustum[5];
            fArr21[0] = fArr21[0] * t6;
            float[] fArr22 = this.frustum[5];
            fArr22[1] = fArr22[1] * t6;
            float[] fArr23 = this.frustum[5];
            fArr23[2] = fArr23[2] * t6;
            float[] fArr24 = this.frustum[5];
            fArr24[3] = fArr24[3] * t6;
        } catch (Exception e) {
            StringBuilder b = new StringBuilder();
            b.append("modl.length = ").append(modelView.length);
            b.append("\nproj.length = ").append(projection.length);
            b.append("\nfrustum.length = ").append(this.frustum.length);
            for (int i = 0; i < this.frustum.length; i++) {
                try {
                    b.append("\n\tfrustum[ ").append(i).append(" ].length = ");
                    if (this.frustum[i] != null) {
                        b.append(this.frustum[i].length);
                    } else {
                        b.append("null");
                    }
                } catch (Exception omg) {
                    throw new RuntimeException("Sweet jesus what is going on here? Exception on index " + i, omg);
                }
            }
            throw new RuntimeException(b.toString(), e);
        }
    }

    public boolean point(float x, float y, float z) {
        for (int p = 0; p < 6; p++) {
            if ((this.frustum[p][0] * x) + (this.frustum[p][1] * y) + (this.frustum[p][2] * z) + this.frustum[p][3] <= 0.0f) {
                return false;
            }
        }
        return true;
    }

    public Result sphereIntersects(float x, float y, float z, float r) {
        int c = 0;
        for (int p = 0; p < 6; p++) {
            float d = (this.frustum[p][0] * x) + (this.frustum[p][1] * y) + (this.frustum[p][2] * z) + this.frustum[p][3];
            if (d <= (-r)) {
                return Result.Miss;
            }
            if (d > r) {
                c++;
            }
        }
        return c == 6 ? Result.Complete : Result.Partial;
    }

    public float sphereDistance(float x, float y, float z, float r) {
        float d = 0.0f;
        for (int p = 0; p < 6; p++) {
            d = (this.frustum[p][0] * x) + (this.frustum[p][1] * y) + (this.frustum[p][2] * z) + this.frustum[p][3];
            if (d <= (-r)) {
                return 0.0f;
            }
        }
        return d + r;
    }

    public Result cuboidIntersects(float minx, float miny, float minz, float maxx, float maxy, float maxz) {
        int c2 = 0;
        for (int p = 0; p < 6; p++) {
            int c = 0;
            if ((this.frustum[p][0] * minx) + (this.frustum[p][1] * miny) + (this.frustum[p][2] * minz) + this.frustum[p][3] > 0.0f) {
                c = 0 + 1;
            }
            if ((this.frustum[p][0] * maxx) + (this.frustum[p][1] * miny) + (this.frustum[p][2] * minz) + this.frustum[p][3] > 0.0f) {
                c++;
            }
            if ((this.frustum[p][0] * minx) + (this.frustum[p][1] * maxy) + (this.frustum[p][2] * minz) + this.frustum[p][3] > 0.0f) {
                c++;
            }
            if ((this.frustum[p][0] * maxx) + (this.frustum[p][1] * maxy) + (this.frustum[p][2] * minz) + this.frustum[p][3] > 0.0f) {
                c++;
            }
            if ((this.frustum[p][0] * minx) + (this.frustum[p][1] * miny) + (this.frustum[p][2] * maxz) + this.frustum[p][3] > 0.0f) {
                c++;
            }
            if ((this.frustum[p][0] * maxx) + (this.frustum[p][1] * miny) + (this.frustum[p][2] * maxz) + this.frustum[p][3] > 0.0f) {
                c++;
            }
            if ((this.frustum[p][0] * minx) + (this.frustum[p][1] * maxy) + (this.frustum[p][2] * maxz) + this.frustum[p][3] > 0.0f) {
                c++;
            }
            if ((this.frustum[p][0] * maxx) + (this.frustum[p][1] * maxy) + (this.frustum[p][2] * maxz) + this.frustum[p][3] > 0.0f) {
                c++;
            }
            if (c == 0) {
                return Result.Miss;
            }
            if (c == 8) {
                c2++;
            }
        }
        return c2 == 6 ? Result.Complete : Result.Partial;
    }


    public enum Result {
        Miss,
        Partial,
        Complete
    }
}
