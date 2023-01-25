package com.mcal.droid.rugl.util;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;

public class ArrayUtil {
    @NonNull
    public static String[] grow(@NonNull String[] in) {
        String[] na = new String[in.length * 2];
        System.arraycopy(in, 0, na, 0, in.length);
        return na;
    }

    @NonNull
    public static long[] grow(@NonNull long[] in) {
        long[] na = new long[in.length * 2];
        System.arraycopy(in, 0, na, 0, in.length);
        return na;
    }

    @NonNull
    public static int[] grow(@NonNull int[] in) {
        int[] na = new int[in.length * 2];
        System.arraycopy(in, 0, na, 0, in.length);
        return na;
    }

    @NonNull
    @Contract(pure = true)
    public static float[] toFloatArray(@NonNull int[] array) {
        float[] res = new float[array.length];
        for (int i = 0; i < array.length; i++) {
            res[i] = array[i];
        }
        return res;
    }
}
