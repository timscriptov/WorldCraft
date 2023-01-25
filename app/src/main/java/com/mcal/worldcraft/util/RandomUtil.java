package com.mcal.worldcraft.util;

import com.mcal.worldcraft.math.MathUtils;

public class RandomUtil {
    public static int getRandomInRangeInclusive(int min, int max) {
        return ((int) (Math.random() * ((max - min) + 1))) + min;
    }

    public static int getRandomSignedInRangeInclusive(int min, int max) {
        return getRandomInRangeInclusive(min, max) * MathUtils.getRandomSign();
    }

    public static float getRandomInRangeExclusive(float min, float max) {
        return ((float) (Math.random() * (max - min))) + min;
    }

    public static float getRandomSignedInRangeExclusive(float min, float max) {
        return getRandomInRangeExclusive(min, max) * MathUtils.getRandomSign();
    }

    public static boolean getChance(float value) {
        return Math.random() <= ((double) value);
    }
}
