package com.solverlabs.worldcraft.dialog.tools.util;


public class AnimationUtil {
    public static int quadraticOutEase(float currentTime, float startValue, float changeInValue, float duration) {
        float currentTime2 = currentTime / duration;
        int returnValue = (int) (((-changeInValue) * currentTime2 * (currentTime2 - 2.0f)) + startValue);
        return returnValue;
    }
}
