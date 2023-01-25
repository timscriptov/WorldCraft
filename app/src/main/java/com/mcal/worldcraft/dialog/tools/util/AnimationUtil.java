package com.mcal.worldcraft.dialog.tools.util;

public class AnimationUtil {
    public static int quadraticOutEase(float currentTime, float startValue, float changeInValue, float duration) {
        float currentTime2 = currentTime / duration;
        return (int) (((-changeInValue) * currentTime2 * (currentTime2 - 2.0f)) + startValue);
    }
}
