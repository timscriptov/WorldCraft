package com.solverlabs.droid.rugl.util.math;


public class LowPassFilter {
    private int index = 0;

    private float[] input;

    /**
     * @param length size of the input buffer
     */
    public LowPassFilter(int length) {
        input = new float[length];
    }

    /**
     * @param i
     */
    public void addInput(float i) {
        input[index] = i;
        index++;
        index %= input.length;
    }

    /**
     * @param alpha smoothing value in range 0-1, lower=more inertia
     * @return filtered output value
     */
    public float getOutput(float alpha) {
        float out = input[index];
        for (int i = 0; i < input.length; i++) {
            out = out + alpha * (input[(index + i) % input.length] - out);
        }

        return out;
    }

    /**
     * @return The number of samples held in the filter
     */
    public int getLength() {
        return input.length;
    }
}