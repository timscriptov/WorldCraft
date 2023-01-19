package com.solverlabs.droid.rugl.util;


import androidx.annotation.NonNull;

public class ArrayUtil {
    /**
     * Doubles the size of an array
     *
     * @param in
     * @return The new array
     */
    @NonNull
    public static String[] grow(@NonNull String[] in) {
        String[] na = new String[in.length * 2];
        System.arraycopy(in, 0, na, 0, in.length);
        return na;
    }

    /**
     * Doubles the size of an array
     *
     * @param in
     * @return The new array
     */
    @NonNull
    public static long[] grow(@NonNull long[] in) {
        long[] na = new long[in.length * 2];
        System.arraycopy(in, 0, na, 0, in.length);
        return na;
    }

    /**
     * Doubles the size of an array
     *
     * @param in
     * @return The new array
     */
    @NonNull
    public static int[] grow(@NonNull int[] in) {
        int[] na = new int[in.length * 2];
        System.arraycopy(in, 0, na, 0, in.length);
        return na;
    }
}