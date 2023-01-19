package com.solverlabs.droid.rugl.util.math;


public class LowPassFilter {
    private int index = 0;
    private float[] input;

    public LowPassFilter(int length) {
        this.input = new float[length];
    }

    public void addInput(float i) {
        this.input[this.index] = i;
        this.index++;
        this.index %= this.input.length;
    }

    public float getOutput(float alpha) {
        float out = this.input[this.index];
        for (int i = 0; i < this.input.length; i++) {
            out += (this.input[(this.index + i) % this.input.length] - out) * alpha;
        }
        return out;
    }

    public int getLength() {
        return this.input.length;
    }
}
