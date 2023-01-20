package com.solverlabs.worldcraft.mob;

import androidx.annotation.NonNull;

public class MobSize {
    public static final float BLOCK_SIZE = 0.03125f;
    private static final float BODY_COLLIDE_HEAD_Y = 0.3f;
    private static final float BODY_COLLIDE_HEAD_Z = 0.3f;
    private static final float ZOOM = 0.85f;
    private final TexturedBlockProperties bodyBox;
    protected float bodyCollideHeadY;
    protected float bodyCollideHeadZ;
    protected float bodyDepth;
    protected float bodyHeight;
    protected float bodyWidth;
    protected float bodyX;
    protected float bodyY;
    protected float bodyZ;
    protected float handActionYOffset;
    private final TexturedBlockProperties handBox;
    protected float handDepth;
    protected float handHeight;
    protected float handLegZOffset;
    protected float handWidth;
    protected float handY;
    private final TexturedBlockProperties headBox;
    protected float headDepth;
    protected float headHeight;
    protected float headWidth;
    protected float headX;
    protected float headY;
    protected float headZ;
    protected float leftHandX;
    protected float leftHandZ;
    protected float leftLegX;
    protected float leftLegZ;
    protected float legActionYOffset;
    private final TexturedBlockProperties legBox;
    protected float legDepth;
    protected float legHeight;
    protected float legWidth;
    protected float legY;
    protected float maxSize;
    protected float objectDepth;
    protected float objectHeight;
    protected float objectWidth;
    protected float objectX;
    protected float objectY;
    protected float objectZ;
    protected float rightHandX;
    protected float rightHandZ;
    protected float rightLegX;
    protected float rightLegZ;
    protected float startHandsAngle;
    protected float zoom;

    public MobSize(TexturedBlockProperties head, TexturedBlockProperties body, TexturedBlockProperties hand, TexturedBlockProperties leg, float bodyCollideHeadY, float bodyCollideHeadZ) {
        this(head, body, hand, leg);
        this.bodyCollideHeadY = bodyCollideHeadY;
        this.bodyCollideHeadZ = bodyCollideHeadZ;
        init();
    }

    public MobSize(TexturedBlockProperties head, TexturedBlockProperties body, TexturedBlockProperties hand, TexturedBlockProperties leg) {
        this.bodyCollideHeadY = 0.3f;
        this.bodyCollideHeadZ = 0.3f;
        this.startHandsAngle = 0.0f;
        this.zoom = ZOOM;
        this.maxSize = -1.0f;
        this.headBox = head;
        this.bodyBox = body;
        this.handBox = hand;
        this.legBox = leg;
        init();
    }

    public MobSize(@NonNull MobSize mobSize) {
        this.bodyCollideHeadY = 0.3f;
        this.bodyCollideHeadZ = 0.3f;
        this.startHandsAngle = 0.0f;
        this.zoom = ZOOM;
        this.maxSize = -1.0f;
        this.headBox = new TexturedBlockProperties(mobSize.getHeadBlockProperties());
        this.bodyBox = new TexturedBlockProperties(mobSize.getBodyBlockProperties());
        this.handBox = new TexturedBlockProperties(mobSize.getHandBlockProperties());
        this.legBox = new TexturedBlockProperties(mobSize.getLegBlockProperties());
        this.bodyCollideHeadY = mobSize.bodyCollideHeadY;
        this.bodyCollideHeadZ = mobSize.bodyCollideHeadZ;
        this.headWidth = mobSize.headWidth;
        this.headHeight = mobSize.headHeight;
        this.headDepth = mobSize.headDepth;
        this.bodyWidth = mobSize.bodyWidth;
        this.bodyHeight = mobSize.bodyHeight;
        this.bodyDepth = mobSize.bodyDepth;
        this.handWidth = mobSize.handWidth;
        this.handHeight = mobSize.handHeight;
        this.handDepth = mobSize.handDepth;
        this.legWidth = mobSize.legWidth;
        this.legHeight = mobSize.legHeight;
        this.legDepth = mobSize.legDepth;
        this.objectWidth = mobSize.objectWidth;
        this.objectHeight = mobSize.objectHeight;
        this.objectDepth = mobSize.objectDepth;
        this.objectX = mobSize.objectX;
        this.objectY = mobSize.objectY;
        this.objectZ = mobSize.objectZ;
        this.headX = mobSize.headX;
        this.headY = mobSize.headY;
        this.headZ = mobSize.headZ;
        this.bodyX = mobSize.bodyX;
        this.bodyY = mobSize.bodyY;
        this.bodyZ = mobSize.bodyZ;
        this.handY = mobSize.handY;
        this.legY = mobSize.legY;
        this.leftHandX = mobSize.leftHandX;
        this.leftHandZ = mobSize.leftHandZ;
        this.rightHandX = mobSize.rightHandX;
        this.rightHandZ = mobSize.rightHandZ;
        this.leftLegX = mobSize.leftLegX;
        this.leftLegZ = mobSize.leftLegZ;
        this.rightLegX = mobSize.rightLegX;
        this.rightLegZ = mobSize.rightLegZ;
        this.handLegZOffset = mobSize.handLegZOffset;
        this.handActionYOffset = mobSize.handActionYOffset;
        this.legActionYOffset = mobSize.legActionYOffset;
        this.maxSize = mobSize.maxSize;
        this.startHandsAngle = mobSize.startHandsAngle;
        this.zoom = mobSize.zoom;
    }

    public float getMaxSize() {
        return this.maxSize;
    }

    public TexturedBlockProperties getHeadBlockProperties() {
        return this.headBox;
    }

    public TexturedBlockProperties getBodyBlockProperties() {
        return this.bodyBox;
    }

    public TexturedBlockProperties getHandBlockProperties() {
        return this.handBox;
    }

    public TexturedBlockProperties getLegBlockProperties() {
        return this.legBox;
    }

    public float getHeadWidth() {
        return this.headWidth;
    }

    public float getHeadHeight() {
        return this.headHeight;
    }

    public float getHeadDepth() {
        return this.headDepth;
    }

    public float getBodyWidth() {
        return this.bodyWidth;
    }

    public float getBodyHeight() {
        return this.bodyHeight;
    }

    public float getBodyDepth() {
        return this.bodyDepth;
    }

    public float getHandWidth() {
        return this.handWidth;
    }

    public float getHandHeight() {
        return this.handHeight;
    }

    public float getHandDepth() {
        return this.handDepth;
    }

    public float getLegWidth() {
        return this.legWidth;
    }

    public float getLegHeight() {
        return this.legHeight;
    }

    public float getLegDepth() {
        return this.legDepth;
    }

    public float getWidth() {
        return this.objectWidth;
    }

    public float getHeight() {
        return this.objectHeight;
    }

    public float getDepth() {
        return this.objectDepth;
    }

    public float getObjectX() {
        return this.objectX;
    }

    public float getObjectY() {
        return this.objectY;
    }

    public float getObjectZ() {
        return this.objectZ;
    }

    public float getHeadX() {
        return this.headX;
    }

    public float getHeadY() {
        return this.headY;
    }

    public float getHeadZ() {
        return this.headZ;
    }

    public float getBodyX() {
        return this.bodyX;
    }

    public float getBodyY() {
        return this.bodyY;
    }

    public float getBodyZ() {
        return this.bodyZ;
    }

    public float getHandY() {
        return this.handY;
    }

    public float getLegY() {
        return this.legY;
    }

    public float getLeftHandX() {
        return this.leftHandX;
    }

    public float getLeftHandZ() {
        return this.leftHandZ;
    }

    public float getRightHandX() {
        return this.rightHandX;
    }

    public float getRightHandZ() {
        return this.rightHandZ;
    }

    public float getLeftLegX() {
        return this.leftLegX;
    }

    public float getLeftLegZ() {
        return this.leftLegZ;
    }

    public float getRightLegX() {
        return this.rightLegX;
    }

    public float getRightLegZ() {
        return this.rightLegZ;
    }

    public float getHandLegZOffset() {
        return this.handLegZOffset;
    }

    public float getHandActionYOffset() {
        return this.handActionYOffset;
    }

    public float getLegActionYOffset() {
        return this.legActionYOffset;
    }

    public float getStartHandsAngle() {
        return this.startHandsAngle;
    }

    public float getZoom() {
        return this.zoom;
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
        recalculateBlocksDimensions();
    }

    private void init() {
        recalculateBlocksDimensions();
        this.objectWidth = this.bodyWidth;
        this.objectHeight = this.handHeight + this.bodyHeight + (this.headHeight * this.bodyCollideHeadY);
        this.objectDepth = (this.headDepth * (1.0f - this.bodyCollideHeadZ)) + this.bodyDepth;
        this.objectX = (-this.objectWidth) / 2.0f;
        this.objectY = (-this.objectHeight) / 2.0f;
        this.objectZ = (-this.objectDepth) / 2.0f;
        this.headX = (-this.headWidth) / 2.0f;
        this.headY = (this.objectHeight - this.headHeight) / 4.0f;
        this.headZ = (this.objectDepth - this.headDepth) / 2.0f;
        this.bodyX = (-this.bodyWidth) / 2.0f;
        this.bodyY = -((this.objectHeight / 2.0f) - this.handHeight);
        this.bodyZ = -((this.objectDepth / 2.0f) - (this.headDepth * this.bodyCollideHeadZ));
        this.handY = this.bodyY - this.handHeight;
        this.legY = this.handY;
        this.leftHandX = (this.bodyX + this.bodyWidth) - this.handWidth;
        this.leftHandZ = (this.bodyZ + this.bodyDepth) - this.handDepth;
        this.rightHandX = this.bodyX;
        this.rightHandZ = this.leftHandZ;
        this.leftLegX = this.leftHandX;
        this.leftLegZ = (this.leftHandZ - this.bodyDepth) + this.legDepth;
        this.rightLegX = this.rightHandX;
        this.rightLegZ = this.leftLegZ;
        this.handLegZOffset = this.handDepth / 4.0f;
        this.handActionYOffset = this.handHeight / 4.0f;
        this.legActionYOffset = this.handActionYOffset;
        this.maxSize = Math.max(this.objectWidth, Math.max(this.objectHeight, this.objectDepth));
    }

    private void recalculateBlocksDimensions() {
        this.headWidth = this.zoom * 0.03125f * this.headBox.getWidth();
        this.headHeight = this.zoom * 0.03125f * this.headBox.getHeight();
        this.headDepth = this.zoom * 0.03125f * this.headBox.getDepth();
        this.bodyWidth = this.zoom * 0.03125f * this.bodyBox.getWidth();
        this.bodyHeight = this.zoom * 0.03125f * this.bodyBox.getHeight();
        this.bodyDepth = this.zoom * 0.03125f * this.bodyBox.getDepth();
        this.handWidth = this.zoom * 0.03125f * this.handBox.getWidth();
        this.handHeight = this.zoom * 0.03125f * this.handBox.getHeight();
        this.handDepth = this.zoom * 0.03125f * this.handBox.getDepth();
        this.legWidth = this.handWidth;
        this.legHeight = this.handHeight;
        this.legDepth = this.handDepth;
    }

    public void shiftTc(int xOffset, int yOffset) {
        this.headBox.shiftTc(xOffset, yOffset);
        this.bodyBox.shiftTc(xOffset, yOffset);
        this.handBox.shiftTc(xOffset, yOffset);
        this.legBox.shiftTc(xOffset, yOffset);
    }
}
