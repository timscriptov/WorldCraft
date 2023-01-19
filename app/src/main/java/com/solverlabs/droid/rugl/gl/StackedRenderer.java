package com.solverlabs.droid.rugl.gl;

import android.opengl.Matrix;


public class StackedRenderer extends Renderer {
    private int pushOffset = 0;
    private float[] stack = new float[80];
    private int pushCount = 0;
    private int popCount = 0;

    public void popMatrix() {
        this.pushOffset -= 16;
        System.arraycopy(this.stack, this.pushOffset, this.transform, 0, 16);
        this.popCount++;
    }

    public void pushMatrix() {
        if (this.pushOffset + 16 >= this.stack.length) {
            float[] ns = new float[this.stack.length * 2];
            System.arraycopy(this.stack, 0, ns, 0, this.pushOffset);
        }
        System.arraycopy(this.transform, 0, this.stack, this.pushOffset, 16);
        this.pushOffset += 16;
        this.pushCount++;
    }

    public void loadIdentity() {
        Matrix.setIdentityM(this.transform, 0);
    }

    public void translate(float x, float y, float z) {
        Matrix.translateM(this.transform, 0, x, y, z);
    }

    public void rotate(float angle, float x, float y, float z) {
        Matrix.rotateM(this.transform, 0, angle, x, y, z);
    }

    public void scale(float x, float y, float z) {
        Matrix.scaleM(this.transform, 0, x, y, z);
    }

    @Override
    public void render() {
        super.render();
        if (this.pushCount != this.popCount) {
            throw new RuntimeException("pushed " + this.pushCount + " and popped " + this.popCount + " matrices between calls to render()");
        }
        this.pushCount = 0;
        this.popCount = 0;
    }
}
