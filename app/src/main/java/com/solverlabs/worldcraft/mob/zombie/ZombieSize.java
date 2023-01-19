package com.solverlabs.worldcraft.mob.zombie;

import com.solverlabs.worldcraft.mob.MobSize;
import com.solverlabs.worldcraft.mob.TexturedBlockProperties;


public class ZombieSize extends MobSize {
    public ZombieSize(TexturedBlockProperties head, TexturedBlockProperties body, TexturedBlockProperties hand, TexturedBlockProperties leg) {
        super(head, body, hand, leg);
        init();
    }

    private void init() {
        setZoom(0.75f);
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
        this.leftHandX = this.bodyX - (this.bodyWidth / 2.0f);
        this.leftHandZ = this.bodyZ;
        this.rightHandX = this.bodyX + (this.bodyWidth / 2.0f) + this.handWidth;
        this.rightHandZ = this.leftHandZ;
        this.legY = (this.bodyY - (this.bodyHeight / 2.0f)) - (this.legHeight / 2.0f);
        this.leftLegX = this.leftHandX + this.legWidth;
        this.leftLegZ = this.leftHandZ;
        this.rightLegX = this.rightHandX - this.legWidth;
        this.rightLegZ = this.leftLegZ;
        this.handActionYOffset = this.handHeight / 4.0f;
        this.legActionYOffset = this.legY + this.legHeight;
        this.startHandsAngle = -1.5707964f;
    }
}
