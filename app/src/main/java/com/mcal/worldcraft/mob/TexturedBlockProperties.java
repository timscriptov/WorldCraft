package com.mcal.worldcraft.mob;

import androidx.annotation.NonNull;

public class TexturedBlockProperties {
    private int depth;
    private int height;
    private int[] tc;
    private int width;

    public TexturedBlockProperties(@NonNull TexturedBlockProperties block) {
        this.width = block.width;
        this.height = block.height;
        this.depth = block.depth;
        this.tc = new int[block.tc.length];
        System.arraycopy(block.tc, 0, this.tc, 0, this.tc.length);
    }

    public TexturedBlockProperties(int width, int height, int depth, int[] tc) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.tc = tc;
    }

    public int getWidth() {
        return this.width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return this.height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getDepth() {
        return this.depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public int[] getTc() {
        return this.tc;
    }

    public void setTc(int[] tc) {
        this.tc = tc;
    }

    public void shiftTc(int xOffset, int yOffset) {
        for (int i = 0; i < this.tc.length; i += 2) {
            this.tc[i] = this.tc[i] + xOffset;
            this.tc[i + 1] = this.tc[i + 1] + yOffset;
        }
    }
}
