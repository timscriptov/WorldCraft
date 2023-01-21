package com.solverlabs.worldcraft.math;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.util.FloatMath;
import com.solverlabs.droid.rugl.util.geom.Vector3f;

import java.util.Random;

public class MathUtils {
    public static final float HALF_OF_PI = 1.5707964f;
    public static final float ONE_AND_HALF_PI = 4.712389f;
    public static final float PI = 3.1415927f;
    public static final float TWO_PI = 6.2831855f;
    public static final float degRad = 0.017453292f;
    public static final float degreesToRadians = 0.017453292f;
    public static final float radDeg = 57.295776f;
    public static final float radiansToDegrees = 57.295776f;
    private static final int ATAN2_BITS = 7;
    private static final int ATAN2_BITS2 = 14;
    private static final int ATAN2_COUNT = 16384;
    private static final int ATAN2_MASK = 16383;
    private static final int BIG_ENOUGH_INT = 16384;
    private static final double BIG_ENOUGH_ROUND = 16384.5d;
    private static final double CEIL = 0.9999999d;
    private static final int SIN_BITS = 13;
    private static final int SIN_COUNT = 8192;
    private static final int SIN_MASK = 8191;
    private static final float degFull = 360.0f;
    private static final float degToIndex = 22.755556f;
    private static final float radFull = 6.2831855f;
    private static final float radToIndex = 1303.7972f;
    private static final double BIG_ENOUGH_FLOOR = 16384.0d;
    static final int ATAN2_DIM = (int) Math.sqrt(BIG_ENOUGH_FLOOR);
    private static final float INV_ATAN2_DIM_MINUS_1 = 1.0f / (ATAN2_DIM - 1);
    private static final double BIG_ENOUGH_CEIL = NumberUtils.longBitsToDouble(NumberUtils.doubleToLongBits(16385.0d) - 1);
    public static Random random = new Random();

    public static final float sin(float radians) {
        return Sin.table[((int) (radToIndex * radians)) & SIN_MASK];
    }

    public static final float cos(float radians) {
        return Cos.table[((int) (radToIndex * radians)) & SIN_MASK];
    }

    public static final float sinDeg(float degrees) {
        return Sin.table[((int) (degToIndex * degrees)) & SIN_MASK];
    }

    public static final float cosDeg(float degrees) {
        return Cos.table[((int) (degToIndex * degrees)) & SIN_MASK];
    }

    public static final float atan2(float y, float x) {
        float mul;
        float add;
        if (x < INV_ATAN2_DIM_MINUS_1) {
            if (y < INV_ATAN2_DIM_MINUS_1) {
                y = -y;
                mul = 1.0f;
            } else {
                mul = -1.0f;
            }
            x = -x;
            add = -3.1415927f;
        } else {
            if (y < INV_ATAN2_DIM_MINUS_1) {
                y = -y;
                mul = -1.0f;
            } else {
                mul = 1.0f;
            }
            add = INV_ATAN2_DIM_MINUS_1;
        }
        float invDiv = 1.0f / ((x < y ? y : x) * INV_ATAN2_DIM_MINUS_1);
        int xi = (int) (x * invDiv);
        int yi = (int) (y * invDiv);
        return (Atan2.table[(ATAN2_DIM * yi) + xi] + add) * mul;
    }

    public static final int random(int range) {
        return random.nextInt(range + 1);
    }

    public static final int random(int start, int end) {
        return random.nextInt((end - start) + 1) + start;
    }

    public static final boolean randomBoolean() {
        return random.nextBoolean();
    }

    public static final float random() {
        return random.nextFloat();
    }

    public static final float random(float range) {
        return random.nextFloat() * range;
    }

    public static final float random(float start, float end) {
        return (random.nextFloat() * (end - start)) + start;
    }

    public static int nextPowerOfTwo(int value) {
        if (value == 0) {
            return 1;
        }
        int value2 = value - 1;
        int value3 = value2 | (value2 >> 1);
        int value4 = value3 | (value3 >> 2);
        int value5 = value4 | (value4 >> 4);
        int value6 = value5 | (value5 >> 8);
        return (value6 | (value6 >> 16)) + 1;
    }

    public static boolean isPowerOfTwo(int value) {
        return value != 0 && ((value + (-1)) & value) == 0;
    }

    public static int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        return value > max ? max : value;
    }

    public static short clamp(short value, short min, short max) {
        if (value < min) {
            return min;
        }
        return value > max ? max : value;
    }

    public static float clamp(float value, float min, float max) {
        if (value < min) {
            return min;
        }
        return value > max ? max : value;
    }

    public static int floor(float x) {
        return ((int) (x + BIG_ENOUGH_FLOOR)) - 16384;
    }

    public static int floorPositive(float x) {
        return (int) x;
    }

    public static int ceil(float x) {
        return ((int) (x + BIG_ENOUGH_CEIL)) - 16384;
    }

    public static int ceilPositive(float x) {
        return (int) (x + CEIL);
    }

    public static int round(float x) {
        return ((int) (x + BIG_ENOUGH_ROUND)) - 16384;
    }

    public static int roundPositive(float x) {
        return (int) (0.5f + x);
    }

    public static int roundUp(float x) {
        float delta = x - ((int) x);
        if (delta != INV_ATAN2_DIM_MINUS_1) {
            x += 1.0f;
        }
        return (int) x;
    }

    public static float randomizeAngleCorrection(float angle, float maxCorrection) {
        float randomCorrection = (float) (Math.random() * maxCorrection);
        float result = angle + (getRandomSign() * randomCorrection);
        return normalizeAngle(result);
    }

    public static int getRandomSign() {
        return Math.random() < 0.5d ? 1 : -1;
    }

    public static float normalizeAngle(float angle) {
        int mult;
        if (angle < INV_ATAN2_DIM_MINUS_1 || angle > 6.2831855f) {
            int mult2 = (int) (Math.abs(angle) / 6.2831855f);
            if (angle > INV_ATAN2_DIM_MINUS_1) {
                mult = mult2 * (-1);
            } else {
                mult = mult2 + 1;
            }
            return angle + (mult * 6.2831855f);
        }
        return angle;
    }

    @NonNull
    public static Vector3f getVelocityVector(float angle, float velocity) {
        Vector3f result = new Vector3f();
        if (Float.compare(angle, INV_ATAN2_DIM_MINUS_1) == 0) {
            result.x = INV_ATAN2_DIM_MINUS_1;
            result.z = velocity;
        } else if (Float.compare(angle, 1.5707964f) == 0) {
            result.x = velocity;
            result.z = INV_ATAN2_DIM_MINUS_1;
        } else if (Float.compare(angle, 3.1415927f) == 0) {
            result.x = INV_ATAN2_DIM_MINUS_1;
            result.z = -velocity;
        } else if (Float.compare(angle, 4.712389f) == 0) {
            result.x = -velocity;
            result.z = INV_ATAN2_DIM_MINUS_1;
        } else if (angleInRange(angle, INV_ATAN2_DIM_MINUS_1, 1.5707964f)) {
            result.x = 2.0f * velocity * sin(angle);
            result.z = 2.0f * velocity * sin(1.5707964f - angle);
        } else if (angleInRange(angle, 1.5707964f, 3.1415927f)) {
            float tempAngle = angle - 1.5707964f;
            result.z = (-2.0f) * velocity * sin(tempAngle);
            result.x = 2.0f * velocity * sin(1.5707964f - tempAngle);
        } else if (angleInRange(angle, 3.1415927f, 4.712389f)) {
            float tempAngle2 = angle - 3.1415927f;
            result.x = (-2.0f) * velocity * sin(tempAngle2);
            result.z = (-2.0f) * velocity * sin(1.5707964f - tempAngle2);
        } else if (angleInRange(angle, 4.712389f, 6.2831855f)) {
            float tempAngle3 = angle - 4.712389f;
            result.z = 2.0f * velocity * sin(tempAngle3);
            result.x = (-2.0f) * velocity * sin(1.5707964f - tempAngle3);
        }
        return result;
    }

    private static boolean angleInRange(float angle, float min, float max) {
        return Double.compare((double) angle, (double) min) == 1 && Double.compare((double) angle, (double) max) == -1;
    }

    public static float getAngleToFollowPoint(float sourceX, float sourceZ, float targetX, float targetZ) {
        float a = targetX - sourceX;
        float b = targetZ - sourceZ;
        float c = getHypotenuse(a, b);
        if (a > INV_ATAN2_DIM_MINUS_1 && b >= INV_ATAN2_DIM_MINUS_1) {
            return getAngleUsingCosTheorem(a, b, c);
        }
        if (a > INV_ATAN2_DIM_MINUS_1 && b < INV_ATAN2_DIM_MINUS_1) {
            return 3.1415927f - getAngleUsingCosTheorem(a, -b, c);
        }
        if (a <= INV_ATAN2_DIM_MINUS_1 && b < INV_ATAN2_DIM_MINUS_1) {
            return getAngleUsingCosTheorem(-a, -b, c) + 3.1415927f;
        }
        return 6.2831855f - getAngleUsingCosTheorem(-a, b, c);
    }

    private static float getHypotenuse(float xDiff, float zDiff) {
        return (float) Math.pow((xDiff * xDiff) + (zDiff * zDiff), 0.5d);
    }

    private static float getAngleUsingCosTheorem(float a, float b, float c) {
        return (float) Math.acos((((b * b) + (c * c)) - (a * a)) / ((2.0f * b) * c));
    }

    public static class Sin {
        static final float[] table = new float[8192];

        static {
            for (int i = 0; i < 8192; i++) {
                table[i] = FloatMath.sin(((i + 0.5f) / 8192.0f) * 6.2831855f);
            }
            for (int i2 = 0; i2 < 360; i2 += 90) {
                table[((int) (i2 * MathUtils.degToIndex)) & MathUtils.SIN_MASK] = FloatMath.sin(i2 * 0.017453292f);
            }
        }

        private Sin() {
        }
    }

    /* loaded from: classes.dex */
    public static class Cos {
        static final float[] table = new float[8192];

        static {
            for (int i = 0; i < 8192; i++) {
                table[i] = FloatMath.cos(((i + 0.5f) / 8192.0f) * 6.2831855f);
            }
            for (int i2 = 0; i2 < 360; i2 += 90) {
                table[((int) (i2 * MathUtils.degToIndex)) & MathUtils.SIN_MASK] = FloatMath.cos(i2 * 0.017453292f);
            }
        }

        private Cos() {
        }
    }

    private static class Atan2 {
        static final float[] table = new float[16384];

        static {
            for (int i = 0; i < MathUtils.ATAN2_DIM; i++) {
                for (int j = 0; j < MathUtils.ATAN2_DIM; j++) {
                    float x0 = i / MathUtils.ATAN2_DIM;
                    float y0 = j / MathUtils.ATAN2_DIM;
                    table[(MathUtils.ATAN2_DIM * j) + i] = (float) Math.atan2(y0, x0);
                }
            }
        }

        private Atan2() {
        }
    }
}
