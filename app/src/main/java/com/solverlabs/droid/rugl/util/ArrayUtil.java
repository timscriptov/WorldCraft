package com.solverlabs.droid.rugl.util;


public class ArrayUtil {
    public static String[] grow(String[] in) {
        String[] na = new String[in.length * 2];
        System.arraycopy(in, 0, na, 0, in.length);
        return na;
    }

    public static long[] grow(long[] in) {
        long[] na = new long[in.length * 2];
        System.arraycopy(in, 0, na, 0, in.length);
        return na;
    }

    public static int[] grow(int[] in) {
        int[] na = new int[in.length * 2];
        System.arraycopy(in, 0, na, 0, in.length);
        return na;
    }

    public static float[] toFloatArray(int[] array) {
        float[] res = new float[array.length];
        for (int i = 0; i < array.length; i++) {
            res[i] = array[i];
        }
        return res;
    }
}
