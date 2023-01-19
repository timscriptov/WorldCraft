package com.solverlabs.droid.rugl.util;

import com.solverlabs.worldcraft.math.MathUtils;

/**
 * Fast trigonometric operations
 *
 * @author Riven
 */
public class Trig {
    /**
     * I'm sick of casting to float
     */
    public static final float PI = (float) Math.PI;

    /**
     * I'm also sick of multiplying by two
     */
    public static final float TWO_PI = 2 * PI;

    /**
     * Honestly, who has the time to divide by two?
     */
    public static final float HALF_PI = PI / 2;

    private static final int ATAN2_BITS = 7;

    private static final int ATAN2_BITS2 = ATAN2_BITS << 1;

    private static final int ATAN2_MASK = ~(-1 << ATAN2_BITS2);

    private static final int ATAN2_COUNT = ATAN2_MASK + 1;

    private static final int ATAN2_DIM = (int) Math.sqrt(ATAN2_COUNT);

    private static final float ATAN2_DIM_MINUS_1 = ATAN2_DIM - 1;

    private static final float[] atan2 = new float[ATAN2_COUNT];

    private static final int SIN_BITS, SIN_MASK, SIN_COUNT;

    private static final float radFull, radToIndex;

    private static final float[] sin, cos;

    static {
        for (int i = 0; i < ATAN2_DIM; i++) {
            for (int j = 0; j < ATAN2_DIM; j++) {
                float x0 = (float) i / ATAN2_DIM;
                float y0 = (float) j / ATAN2_DIM;

                atan2[j * ATAN2_DIM + i] = (float) Math.atan2(y0, x0);
            }
        }

        SIN_BITS = 12;
        SIN_MASK = ~(-1 << SIN_BITS);
        SIN_COUNT = SIN_MASK + 1;

        radFull = (float) (Math.PI * 2.0);
        radToIndex = SIN_COUNT / radFull;

        sin = new float[SIN_COUNT];
        cos = new float[SIN_COUNT];

        for (int i = 0; i < SIN_COUNT; i++) {
            sin[i] = (float) MathUtils.sin((i + 0.5f) / SIN_COUNT * radFull);
            cos[i] = (float) MathUtils.cos((i + 0.5f) / SIN_COUNT * radFull);
        }
    }

    /**
     * Like {@link Math#sin(double)}, but a lot faster and a bit less
     * accurate
     *
     * @param rad
     * @return sin(rad)
     */
    public static final float sin(float rad) {
        return sin[(int) (rad * radToIndex) & SIN_MASK];
    }

    /**
     * Like {@link Math#cos(double)}, but a lot faster and a bit less
     * accurate
     *
     * @param rad
     * @return cos(rad)
     */
    public static final float cos(float rad) {
        return cos[(int) (rad * radToIndex) & SIN_MASK];
    }

    /**
     * @param y
     * @param x
     * @return the angle to (x,y)
     */
    public static final float atan2(float y, float x) {
        float add, mul;

        if (x < 0.0f) {
            if (y < 0.0f) {
                x = -x;
                y = -y;

                mul = 1.0f;
            } else {
                x = -x;
                mul = -1.0f;
            }

            add = -3.141592653f;
        } else {
            if (y < 0.0f) {
                y = -y;
                mul = -1.0f;
            } else {
                mul = 1.0f;
            }

            add = 0.0f;
        }

        float invDiv = ATAN2_DIM_MINUS_1 / (x < y ? y : x);

        int xi = (int) (x * invDiv);
        int yi = (int) (y * invDiv);

        return (atan2[yi * ATAN2_DIM + xi] + add) * mul;
    }

    /**
     * @param degrees
     * @return the radians value
     */
    public static float toRadians(float degrees) {
        return degrees / 180.0f * Trig.PI;
    }

    /**
     * @param radians
     * @return the degrees value
     */
    public static float toDegrees(float radians) {
        return radians * 180.0f / Trig.PI;
    }
}