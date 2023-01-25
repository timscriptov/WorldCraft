package com.mcal.worldcraft.mob.pig;

import com.mcal.worldcraft.mob.MobSize;
import com.mcal.worldcraft.mob.TexturedBlockProperties;

public class PigSize extends MobSize {
    private final TexturedBlockProperties snoutBox;
    private final float snoutDepth;
    private final float snoutHeight;
    private final float snoutWidth;
    private final float snoutX;
    private final float snoutY;
    private final float snoutZ;

    public PigSize(TexturedBlockProperties head, TexturedBlockProperties body, TexturedBlockProperties hand, TexturedBlockProperties leg, TexturedBlockProperties snoutBox) {
        super(head, body, hand, leg);
        this.snoutBox = snoutBox;
        this.snoutWidth = getZoom() * 0.03125f * this.snoutBox.getWidth();
        this.snoutHeight = getZoom() * 0.03125f * this.snoutBox.getHeight();
        this.snoutDepth = getZoom() * 0.03125f * this.snoutBox.getDepth();
        this.snoutX = getHeadX() + ((getHeadWidth() - this.snoutWidth) / 2.0f);
        this.snoutY = getHeadY() + (this.snoutHeight / 4.0f);
        this.snoutZ = getHeadZ() + getHeadDepth();
    }

    public TexturedBlockProperties getSnoutBlockProperties() {
        return this.snoutBox;
    }

    public float getSnoutX() {
        return this.snoutX;
    }

    public float getSnoutY() {
        return this.snoutY;
    }

    public float getSnoutZ() {
        return this.snoutZ;
    }

    public float getSnoutWidth() {
        return this.snoutWidth;
    }

    public float getSnoutHeight() {
        return this.snoutHeight;
    }

    public float getSnoutDepth() {
        return this.snoutDepth;
    }
}
