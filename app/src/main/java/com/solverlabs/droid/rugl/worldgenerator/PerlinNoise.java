package com.solverlabs.droid.rugl.worldgenerator;

import com.solverlabs.worldcraft.math.MathUtils;
import com.solverlabs.worldcraft.srv.util.ObjectCodec;
import com.solverlabs.worldcraft.ui.Interaction;
import java.util.Random;
import org.apache.commons.compress.archivers.cpio.CpioConstants;
import org.apache.commons.compress.archivers.tar.TarConstants;

/* loaded from: classes.dex */
public class PerlinNoise {
    private static final int GRADIENT_MASK_TABLE = 256;
    private static final int mask = 255;
    private final Random _random;
    private int[] _perm = {225, TarConstants.PREFIXLEN, 210, 108, 175, 199, 221, 144, 203, 116, 70, 213, 69, 158, 33, 252, 5, 82, 173, 133, 222, 139, 174, 27, 9, 71, 90, 246, 75, 130, 91, 191, 169, 138, 2, 151, 194, 235, 81, 7, 25, 113, 228, 159, 205, 253, 134, 142, 248, 65, 224, 217, 22, 121, 229, 63, 89, 103, 96, 104, 156, 17, 201, 129, 36, 8, 165, 110, 237, 117, 231, 56, 132, 211, 152, 20, 181, 111, 239, 218, 170, 163, 51, 172, 157, 47, 80, 212, 176, 250, 87, 49, 99, 242, 136, 189, 162, 115, 44, 43, 124, 94, 150, 16, 141, 247, 32, 10, 198, 223, mask, 72, 53, 131, 84, 57, 220, 197, 58, 50, 208, 11, 241, 28, 3, 192, 62, 202, 18, 215, 153, 24, 76, 41, 15, 179, 39, 46, 55, 6, CpioConstants.C_IWUSR, 167, 23, 188, 106, 34, 187, 140, 164, 73, 112, 182, 244, 195, 227, 13, 35, 77, 196, 185, 26, Interaction.NOISE_NOTIFICATION_DELAY, 226, 119, 31, 123, 168, 125, 249, 68, 183, 230, 177, 135, 160, 180, 12, 1, 243, 148, 102, 166, 38, 238, 251, 37, 240, 126, 64, 74, 161, 40, 184, 149, 171, 178, 101, 66, 29, 59, 146, 61, 254, 107, 42, 86, 154, 4, 236, 232, 120, 21, 233, 209, 45, 98, 193, 114, 78, 19, 206, 14, ObjectCodec.BlockInfo.LAST_BLOCK_ID, 127, 48, 79, 147, 85, 30, 207, 219, 54, 88, 234, 190, 122, 95, 67, 143, 109, 137, 214, 145, 93, 92, 100, 245, 0, 216, 186, 60, 83, 105, 97, 204, 52};
    private double[] _gradients = new double[768];

    public PerlinNoise(int seed) {
        this._random = new Random(seed);
        initGradients();
    }

    public double noise(double x, double y, double z) {
        int ix = (int) Math.floor(x);
        double fx0 = x - ix;
        double fx1 = fx0 - 1.0d;
        double wx = smooth(fx0);
        int iy = (int) Math.floor(y);
        double fy0 = y - iy;
        double fy1 = fy0 - 1.0d;
        double wy = smooth(fy0);
        int iz = (int) Math.floor(z);
        double fz0 = z - iz;
        double fz1 = fz0 - 1.0d;
        double wz = smooth(fz0);
        double vx0 = lattice(ix, iy, iz, fx0, fy0, fz0);
        double vx1 = lattice(ix + 1, iy, iz, fx1, fy0, fz0);
        double vy0 = lerp(wx, vx0, vx1);
        double vx02 = lattice(ix, iy + 1, iz, fx0, fy1, fz0);
        double vx12 = lattice(ix + 1, iy + 1, iz, fx1, fy1, fz0);
        double vy1 = lerp(wx, vx02, vx12);
        double vz0 = lerp(wy, vy0, vy1);
        double vx03 = lattice(ix, iy, iz + 1, fx0, fy0, fz1);
        double vx13 = lattice(ix + 1, iy, iz + 1, fx1, fy0, fz1);
        double vy02 = lerp(wx, vx03, vx13);
        double vx04 = lattice(ix, iy + 1, iz + 1, fx0, fy1, fz1);
        double vx14 = lattice(ix + 1, iy + 1, iz + 1, fx1, fy1, fz1);
        double vy12 = lerp(wx, vx04, vx14);
        double vz1 = lerp(wy, vy02, vy12);
        double noise = lerp(wz, vz0, vz1);
        return noise;
    }

    private void initGradients() {
        for (int i = 0; i < 256; i++) {
            double z = 1.0d - (2.0d * this._random.nextDouble());
            double r = Math.sqrt(1.0d - (z * z));
            double theta = 6.283185307179586d * this._random.nextDouble();
            this._gradients[i * 3] = MathUtils.cos((float) theta) * r;
            this._gradients[(i * 3) + 1] = MathUtils.sin((float) theta) * r;
            this._gradients[(i * 3) + 2] = z;
        }
    }

    private int permutate(int x) {
        return this._perm[x & mask];
    }

    private int index(int ix, int iy, int iz) {
        return permutate(permutate(permutate(iz) + iy) + ix);
    }

    private double lattice(int ix, int iy, int iz, double fx, double fy, double fz) {
        int index = index(ix, iy, iz);
        int g = index * 3;
        return (this._gradients[g] * fx) + (this._gradients[g + 1] * fy) + (this._gradients[g + 2] * fz);
    }

    private double lerp(double t, double value0, double value1) {
        return ((value1 - value0) * t) + value0;
    }

    private double smooth(double x) {
        return x * x * (3.0d - (2.0d * x));
    }
}