package com.solverlabs.worldcraft.mob.skeleton;

import com.solverlabs.worldcraft.mob.MobSize;
import com.solverlabs.worldcraft.mob.TexturedBlockProperties;


public class SkeletonSize extends MobSize {
    public SkeletonSize(TexturedBlockProperties head, TexturedBlockProperties body, TexturedBlockProperties hand, TexturedBlockProperties leg) {
        super(head, body, hand, leg);
        init();
    }

    private void init() {
        setZoom(0.7f);
        this.objectWidth = this.bodyWidth;
        this.objectHeight = this.headHeight + this.bodyHeight + this.legHeight;
        this.objectDepth = this.bodyDepth;
        this.objectX = (-this.objectWidth) / 2.0f;
        this.objectY = (-this.objectHeight) / 6.0f;
        this.objectZ = (-this.objectDepth) / 2.0f;
        this.headX = (-this.headWidth) / 2.0f;
        this.headY = this.objectY + this.bodyHeight;
        this.headZ = (-this.headDepth) / 2.0f;
        this.bodyX = (-this.bodyWidth) / 2.0f;
        this.bodyY = ((this.headY - (this.headHeight / 2.0f)) - (this.bodyHeight / 2.0f)) - (this.bodyHeight / 6.0f);
        this.bodyZ = (-this.objectDepth) / 2.0f;
        this.handY = this.bodyY;
        this.leftHandX = (this.bodyX - (this.bodyWidth / 2.0f)) + this.handWidth;
        this.leftHandZ = this.bodyZ + (this.handDepth / 2.0f);
        this.rightHandX = this.bodyX + this.bodyWidth;
        this.rightHandZ = this.leftHandZ;
        this.legY = (this.bodyY - (this.bodyHeight / 2.0f)) - (this.legHeight / 2.0f);
        this.leftLegX = this.leftHandX + (this.legWidth * 1.5f);
        this.leftLegZ = this.leftHandZ;
        this.rightLegX = this.rightHandX - (this.legWidth * 1.5f);
        this.rightLegZ = this.leftLegZ;
        this.handActionYOffset = this.handY + this.handHeight;
        this.legActionYOffset = this.legY + this.legHeight;
    }
}
