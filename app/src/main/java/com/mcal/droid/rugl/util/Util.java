package com.mcal.droid.rugl.util;

public class Util {
    public static int extractDigit(float value, int place) {
        float pow = (float) Math.pow(10.0d, place);
        float powp = pow * (place < 0 ? -10 : 10);
        return (int) ((value % powp) / pow);
    }
}
