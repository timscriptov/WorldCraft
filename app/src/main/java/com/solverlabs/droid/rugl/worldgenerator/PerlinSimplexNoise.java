package com.solverlabs.droid.rugl.worldgenerator;

import com.solverlabs.worldcraft.srv.util.ObjectCodec;
import com.solverlabs.worldcraft.ui.Interaction;

import org.apache.commons.compress.archivers.cpio.CpioConstants;
import org.apache.commons.compress.archivers.tar.TarConstants;


public class PerlinSimplexNoise {
    private static int[][] grad3 = {new int[]{1, 1, 0}, new int[]{-1, 1, 0}, new int[]{1, -1, 0}, new int[]{-1, -1, 0}, new int[]{1, 0, 1}, new int[]{-1, 0, 1}, new int[]{1, 0, -1}, new int[]{-1, 0, -1}, new int[]{0, 1, 1}, new int[]{0, -1, 1}, new int[]{0, 1, -1}, new int[]{0, -1, -1}};
    private static int[][] grad4 = {new int[]{0, 1, 1, 1}, new int[]{0, 1, 1, -1}, new int[]{0, 1, -1, 1}, new int[]{0, 1, -1, -1}, new int[]{0, -1, 1, 1}, new int[]{0, -1, 1, -1}, new int[]{0, -1, -1, 1}, new int[]{0, -1, -1, -1}, new int[]{1, 0, 1, 1}, new int[]{1, 0, 1, -1}, new int[]{1, 0, -1, 1}, new int[]{1, 0, -1, -1}, new int[]{-1, 0, 1, 1}, new int[]{-1, 0, 1, -1}, new int[]{-1, 0, -1, 1}, new int[]{-1, 0, -1, -1}, new int[]{1, 1, 0, 1}, new int[]{1, 1, 0, -1}, new int[]{1, -1, 0, 1}, new int[]{1, -1, 0, -1}, new int[]{-1, 1, 0, 1}, new int[]{-1, 1, 0, -1}, new int[]{-1, -1, 0, 1}, new int[]{-1, -1, 0, -1}, new int[]{1, 1, 1, 0}, new int[]{1, 1, -1, 0}, new int[]{1, -1, 1, 0}, new int[]{1, -1, -1, 0}, new int[]{-1, 1, 1, 0}, new int[]{-1, 1, -1, 0}, new int[]{-1, -1, 1, 0}, new int[]{-1, -1, -1, 0}};
    private static int[][] simplex = {new int[]{0, 1, 2, 3}, new int[]{0, 1, 3, 2}, new int[]{0, 0, 0, 0}, new int[]{0, 2, 3, 1}, new int[]{0, 0, 0, 0}, new int[]{0, 0, 0, 0}, new int[]{0, 0, 0, 0}, new int[]{1, 2, 3, 0}, new int[]{0, 2, 1, 3}, new int[]{0, 0, 0, 0}, new int[]{0, 3, 1, 2}, new int[]{0, 3, 2, 1}, new int[]{0, 0, 0, 0}, new int[]{0, 0, 0, 0}, new int[]{0, 0, 0, 0}, new int[]{1, 3, 2, 0}, new int[]{0, 0, 0, 0}, new int[]{0, 0, 0, 0}, new int[]{0, 0, 0, 0}, new int[]{0, 0, 0, 0}, new int[]{0, 0, 0, 0}, new int[]{0, 0, 0, 0}, new int[]{0, 0, 0, 0}, new int[]{0, 0, 0, 0}, new int[]{1, 2, 0, 3}, new int[]{0, 0, 0, 0}, new int[]{1, 3, 0, 2}, new int[]{0, 0, 0, 0}, new int[]{0, 0, 0, 0}, new int[]{0, 0, 0, 0}, new int[]{2, 3, 0, 1}, new int[]{2, 3, 1, 0}, new int[]{1, 0, 2, 3}, new int[]{1, 0, 3, 2}, new int[]{0, 0, 0, 0}, new int[]{0, 0, 0, 0}, new int[]{0, 0, 0, 0}, new int[]{2, 0, 3, 1}, new int[]{0, 0, 0, 0}, new int[]{2, 1, 3, 0}, new int[]{0, 0, 0, 0}, new int[]{0, 0, 0, 0}, new int[]{0, 0, 0, 0}, new int[]{0, 0, 0, 0}, new int[]{0, 0, 0, 0}, new int[]{0, 0, 0, 0}, new int[]{0, 0, 0, 0}, new int[]{0, 0, 0, 0}, new int[]{2, 0, 1, 3}, new int[]{0, 0, 0, 0}, new int[]{0, 0, 0, 0}, new int[]{0, 0, 0, 0}, new int[]{3, 0, 1, 2}, new int[]{3, 0, 2, 1}, new int[]{0, 0, 0, 0}, new int[]{3, 1, 2, 0}, new int[]{2, 1, 0, 3}, new int[]{0, 0, 0, 0}, new int[]{0, 0, 0, 0}, new int[]{0, 0, 0, 0}, new int[]{3, 1, 0, 2}, new int[]{0, 0, 0, 0}, new int[]{3, 2, 0, 1}, new int[]{3, 2, 1, 0}};
    private static int[] p = {151, 160, 137, 91, 90, 15, 131, 13, 201, 95, 96, 53, 194, 233, 7, 225, 140, 36, 103, 30, 69, 142, 8, 99, 37, 240, 21, 10, 23, 190, 6, 148, 247, 120, 234, 75, 0, 26, 197, 62, 94, 252, 219, 203, 117, 35, 11, 32, 57, 177, 33, 88, 237, 149, 56, 87, 174, 20, 125, 136, 171, 168, 68, 175, 74, 165, 71, 134, 139, 48, 27, 166, 77, 146, 158, 231, 83, 111, 229, 122, 60, 211, 133, 230, 220, 105, 92, 41, 55, 46, 245, 40, 244, 102, 143, 54, 65, 25, 63, 161, 1, 216, 80, 73, 209, 76, 132, 187, 208, 89, 18, 169, Interaction.NOISE_NOTIFICATION_DELAY, 196, 135, 130, 116, 188, 159, 86, 164, 100, 109, 198, 173, 186, 3, 64, 52, 217, 226, 250, 124, 123, 5, 202, 38, 147, ObjectCodec.BlockInfo.LAST_BLOCK_ID, 126, 255, 82, 85, 212, 207, 206, 59, 227, 47, 16, 58, 17, 182, 189, 28, 42, 223, 183, 170, 213, 119, 248, 152, 2, 44, 154, 163, 70, 221, 153, 101, TarConstants.PREFIXLEN, 167, 43, 172, 9, 129, 22, 39, 253, 19, 98, 108, 110, 79, 113, 224, 232, 178, 185, 112, 104, 218, 246, 97, 228, 251, 34, 242, 193, 238, 210, 144, 12, 191, 179, 162, 241, 81, 51, 145, 235, 249, 14, 239, 107, 49, 192, 214, 31, 181, 199, 106, 157, 184, 84, 204, 176, 115, 121, 50, 45, 127, 4, 150, 254, 138, 236, 205, 93, 222, 114, 67, 29, 24, 72, 243, 141, CpioConstants.C_IWUSR, 195, 78, 66, 215, 61, 156, 180};
    private static int[] perm = new int[512];

    static {
        for (int i = 0; i < 512; i++) {
            perm[i] = p[i & 255];
        }
    }

    private static int fastfloor(float x) {
        return x > 0.0f ? (int) x : ((int) x) - 1;
    }

    private static float dot(int[] g, float x, float y) {
        return (g[0] * x) + (g[1] * y);
    }

    private static float dot(int[] g, float x, float y, float z) {
        return (g[0] * x) + (g[1] * y) + (g[2] * z);
    }

    private static float dot(int[] g, float x, float y, float z, float w) {
        return (g[0] * x) + (g[1] * y) + (g[2] * z) + (g[3] * w);
    }

    public static float noise(float xin, float yin, float zin) {
        int i1;
        int j1;
        int k1;
        int i2;
        int j2;
        int k2;
        float n0;
        float n1;
        float n2;
        float n3;
        float s = (xin + yin + zin) * 0.33333334f;
        int i = fastfloor(xin + s);
        int j = fastfloor(yin + s);
        int k = fastfloor(zin + s);
        float t = (i + j + k) * 0.16666667f;
        float X0 = i - t;
        float Y0 = j - t;
        float Z0 = k - t;
        float x0 = xin - X0;
        float y0 = yin - Y0;
        float z0 = zin - Z0;
        if (x0 >= y0) {
            if (y0 >= z0) {
                i1 = 1;
                j1 = 0;
                k1 = 0;
                i2 = 1;
                j2 = 1;
                k2 = 0;
            } else if (x0 >= z0) {
                i1 = 1;
                j1 = 0;
                k1 = 0;
                i2 = 1;
                j2 = 0;
                k2 = 1;
            } else {
                i1 = 0;
                j1 = 0;
                k1 = 1;
                i2 = 1;
                j2 = 0;
                k2 = 1;
            }
        } else if (y0 < z0) {
            i1 = 0;
            j1 = 0;
            k1 = 1;
            i2 = 0;
            j2 = 1;
            k2 = 1;
        } else if (x0 < z0) {
            i1 = 0;
            j1 = 1;
            k1 = 0;
            i2 = 0;
            j2 = 1;
            k2 = 1;
        } else {
            i1 = 0;
            j1 = 1;
            k1 = 0;
            i2 = 1;
            j2 = 1;
            k2 = 0;
        }
        float x1 = (x0 - i1) + 0.16666667f;
        float y1 = (y0 - j1) + 0.16666667f;
        float z1 = (z0 - k1) + 0.16666667f;
        float x2 = (x0 - i2) + (2.0f * 0.16666667f);
        float y2 = (y0 - j2) + (2.0f * 0.16666667f);
        float z2 = (z0 - k2) + (2.0f * 0.16666667f);
        float x3 = (x0 - 1.0f) + (3.0f * 0.16666667f);
        float y3 = (y0 - 1.0f) + (3.0f * 0.16666667f);
        float z3 = (z0 - 1.0f) + (3.0f * 0.16666667f);
        int ii = i & 255;
        int jj = j & 255;
        int kk = k & 255;
        int gi0 = perm[perm[perm[kk] + jj] + ii] % 12;
        int gi1 = perm[(ii + i1) + perm[(jj + j1) + perm[kk + k1]]] % 12;
        int gi2 = perm[(ii + i2) + perm[(jj + j2) + perm[kk + k2]]] % 12;
        int gi3 = perm[(ii + 1) + perm[(jj + 1) + perm[kk + 1]]] % 12;
        float t0 = ((0.6f - (x0 * x0)) - (y0 * y0)) - (z0 * z0);
        if (t0 < 0.0f) {
            n0 = 0.0f;
        } else {
            float t02 = t0 * t0;
            n0 = t02 * t02 * dot(grad3[gi0], x0, y0, z0);
        }
        float t1 = ((0.6f - (x1 * x1)) - (y1 * y1)) - (z1 * z1);
        if (t1 < 0.0f) {
            n1 = 0.0f;
        } else {
            float t12 = t1 * t1;
            n1 = t12 * t12 * dot(grad3[gi1], x1, y1, z1);
        }
        float t2 = ((0.6f - (x2 * x2)) - (y2 * y2)) - (z2 * z2);
        if (t2 < 0.0f) {
            n2 = 0.0f;
        } else {
            float t22 = t2 * t2;
            n2 = t22 * t22 * dot(grad3[gi2], x2, y2, z2);
        }
        float t3 = ((0.6f - (x3 * x3)) - (y3 * y3)) - (z3 * z3);
        if (t3 < 0.0f) {
            n3 = 0.0f;
        } else {
            float t32 = t3 * t3;
            n3 = t32 * t32 * dot(grad3[gi3], x3, y3, z3);
        }
        return 32.0f * (n0 + n1 + n2 + n3);
    }

    public static float noise(float xin, float yin) {
        int i1;
        int j1;
        float n0;
        float n1;
        float n2;
        float F2 = (float) (0.5d * (Math.sqrt(3.0d) - 1.0d));
        float s = (xin + yin) * F2;
        int i = fastfloor(xin + s);
        int j = fastfloor(yin + s);
        float g2 = (float) ((3.0d - Math.sqrt(3.0d)) / 6.0d);
        float t = (i + j) * g2;
        float X0 = i - t;
        float Y0 = j - t;
        float x0 = xin - X0;
        float y0 = yin - Y0;
        if (x0 > y0) {
            i1 = 1;
            j1 = 0;
        } else {
            i1 = 0;
            j1 = 1;
        }
        float x1 = (x0 - i1) + g2;
        float y1 = (y0 - j1) + g2;
        float x2 = (x0 - 1.0f) + (2.0f * g2);
        float y2 = (y0 - 1.0f) + (2.0f * g2);
        int ii = i & 255;
        int jj = j & 255;
        int gi0 = perm[perm[jj] + ii] % 12;
        int gi1 = perm[(ii + i1) + perm[jj + j1]] % 12;
        int gi2 = perm[(ii + 1) + perm[jj + 1]] % 12;
        float t0 = (0.5f - (x0 * x0)) - (y0 * y0);
        if (t0 < 0.0f) {
            n0 = 0.0f;
        } else {
            float t02 = t0 * t0;
            n0 = t02 * t02 * dot(grad3[gi0], x0, y0);
        }
        float t1 = (0.5f - (x1 * x1)) - (y1 * y1);
        if (t1 < 0.0f) {
            n1 = 0.0f;
        } else {
            float t12 = t1 * t1;
            n1 = t12 * t12 * dot(grad3[gi1], x1, y1);
        }
        float t2 = (0.5f - (x2 * x2)) - (y2 * y2);
        if (t2 < 0.0f) {
            n2 = 0.0f;
        } else {
            float t22 = t2 * t2;
            n2 = t22 * t22 * dot(grad3[gi2], x2, y2);
        }
        float returnNoise = 70.0f * (n0 + n1 + n2);
        return (1.0f + returnNoise) * 0.5f;
    }
}
