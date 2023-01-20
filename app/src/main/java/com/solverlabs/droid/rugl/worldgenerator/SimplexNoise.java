package com.solverlabs.droid.rugl.worldgenerator;


public class SimplexNoise {
    private static final float onesixth = 0.16666667f;
    private static final float onethird = 0.33333334f;
    private static final int[] A = new int[3];
    private static final int[] T = {21, 56, 50, 44, 13, 19, 7, 42};
    private static int i = 0;
    private static int j = 0;
    private static int k = 0;
    private static float s;
    private static float u;
    private static float v;
    private static float w;

    public static float noise(float x, float y, float z) {
        s = (x + y + z) * onethird;
        i = fastfloor(s + x);
        j = fastfloor(s + y);
        k = fastfloor(s + z);
        s = (i + j + k) * onesixth;
        u = (x - i) + s;
        v = (y - j) + s;
        w = (z - k) + s;
        A[2] = 0;
        A[1] = 0;
        A[0] = 0;
        int hi = u >= w ? u >= v ? 0 : 1 : v >= w ? 1 : 2;
        int lo = u < w ? u < v ? 0 : 1 : v < w ? 1 : 2;
        return K(0) + K(hi) + K((3 - hi) - lo) + K(lo);
    }

    private static int fastfloor(float n) {
        return n > 0.0f ? (int) n : ((int) n) - 1;
    }

    private static float K(int a) {
        float p;
        float q;
        float r;
        s = (A[0] + A[1] + A[2]) * onesixth;
        float x = (u - A[0]) + s;
        float y = (v - A[1]) + s;
        float z = (w - A[2]) + s;
        float t = ((0.6f - (x * x)) - (y * y)) - (z * z);
        int h = shuffle(i + A[0], j + A[1], k + A[2]);
        int[] iArr = A;
        iArr[a] = iArr[a] + 1;
        if (t < 0.0f) {
            return 0.0f;
        }
        int b5 = (h >> 5) & 1;
        int b4 = (h >> 4) & 1;
        int b3 = (h >> 3) & 1;
        int b2 = (h >> 2) & 1;
        int b = h & 3;
        if (b == 1) {
            p = x;
        } else {
            p = b == 2 ? y : z;
        }
        if (b == 1) {
            q = y;
        } else {
            q = b == 2 ? z : x;
        }
        if (b == 1) {
            r = z;
        } else {
            r = b == 2 ? x : y;
        }
        if (b5 == b3) {
            p = -p;
        }
        if (b5 == b4) {
            q = -q;
        }
        if (b5 != (b4 ^ b3)) {
            r = -r;
        }
        float t2 = t * t;
        float f = 8.0f * t2 * t2;
        if (b == 0) {
            q += r;
        } else if (b2 != 0) {
            q = r;
        }
        return f * (p + q);
    }

    private static int shuffle(int i2, int j2, int k2) {
        return b(i2, j2, k2, 0) + b(j2, k2, i2, 1) + b(k2, i2, j2, 2) + b(i2, j2, k2, 3) + b(j2, k2, i2, 4) + b(k2, i2, j2, 5) + b(i2, j2, k2, 6) + b(j2, k2, i2, 7);
    }

    private static int b(int i2, int j2, int k2, int B) {
        return T[(b(i2, B) << 2) | (b(j2, B) << 1) | b(k2, B)];
    }

    private static int b(int N, int B) {
        return (N >> B) & 1;
    }
}
