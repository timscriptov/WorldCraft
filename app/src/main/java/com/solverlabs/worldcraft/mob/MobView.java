package com.solverlabs.worldcraft.mob;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.geom.TexturedShape;
import com.solverlabs.droid.rugl.gl.StackedRenderer;
import com.solverlabs.droid.rugl.gl.State;
import com.solverlabs.droid.rugl.util.FPSCamera;
import com.solverlabs.droid.rugl.util.geom.BoundingCuboid;
import com.solverlabs.droid.rugl.util.geom.Vector3f;


public abstract class MobView {
    private static final float HAND_LEG_WALK_RESET_ANGLE = 0.0f;
    private static final float MAX_LEG_ANGLE = 0.7853982f;
    protected Mob mob;
    protected MobSize mobSize;
    private float handLegWalkAngle;
    private float legMultiplyParam;

    public MobView(Mob mob, State state) {
        this(mob, state, false);
    }

    public MobView(Mob mob, State state, boolean hasSecondTexture) {
        this.legMultiplyParam = 1.0f;
        this.mob = mob;
        this.mobSize = this.mob.getSize();
    }

    public abstract MobTexturePack getTexturePack();

    public void advance(float delta, FPSCamera cam) {
        if (isMobValid()) {
            getTexturePack().setLight(this.mob.getLight());
            if (this.mob.isStateChanged()) {
                invalidate();
            }
            if (getTexturePack().isShapesInvalidated() || this.mob.isPositionChanged() || this.mob.isStateChanged() || this.handLegWalkAngle != HAND_LEG_WALK_RESET_ANGLE) {
                initShapes();
                advanceHandLegDueWalkAngle(delta);
                updateGeometry();
            }
        }
    }

    public void render(StackedRenderer r, FPSCamera cam) {
        if (isMobValid()) {
            Vector3f mobPosition = getMobViewPosition();
            r.pushMatrix();
            r.translate(mobPosition.x, mobPosition.y, mobPosition.z);
            if (this.mob.isDying()) {
                r.rotate(90.0f, HAND_LEG_WALK_RESET_ANGLE, HAND_LEG_WALK_RESET_ANGLE, 1.0f);
            }
            getTexturePack().render(r);
            r.popMatrix();
        }
    }

    private boolean isMobValid() {
        return this.mob != null && this.mob.isVisible() && this.mobSize != null;
    }

    public boolean isVisible() {
        return this.mob != null && this.mob.isVisible();
    }

    @NonNull
    private Vector3f getMobViewPosition() {
        Vector3f result = new Vector3f(this.mob.getPosition());
        if (!this.mob.isDying()) {
            result.y += this.mobSize.getHeight() / 2.0f;
        }
        return result;
    }

    private void advanceHandLegDueWalkAngle(float delta) {
        if (this.mob.isWalking()) {
            updateHandLegWalkAngle(delta);
        } else {
            resetHandLegWalkAngle();
        }
    }

    private void updateHandLegWalkAngle(float delta) {
        this.handLegWalkAngle += this.legMultiplyParam * (delta / 0.2f) * MAX_LEG_ANGLE;
        if (this.legMultiplyParam == 1.0f && this.handLegWalkAngle > MAX_LEG_ANGLE) {
            this.handLegWalkAngle = 1.5707964f - this.handLegWalkAngle;
            this.legMultiplyParam = -1.0f;
        } else if (this.legMultiplyParam == -1.0f && this.handLegWalkAngle < -0.7853982f) {
            this.handLegWalkAngle = (-1.5707964f) - this.handLegWalkAngle;
            this.legMultiplyParam = 1.0f;
        }
    }

    private void resetHandLegWalkAngle() {
        if (this.handLegWalkAngle != HAND_LEG_WALK_RESET_ANGLE) {
            this.handLegWalkAngle = HAND_LEG_WALK_RESET_ANGLE;
        }
    }

    protected void initShapes() {
        getTexturePack().initShapes(this.mob.getState());
    }

    public void invalidate() {
        getTexturePack().invalidateShapes();
    }

    public void getBounds(@NonNull Vector3f position, @NonNull BoundingCuboid mobBounds) {
        float h = this.mobSize.getHeight() * 1.4f;
        float d = this.mobSize.getDepth() * 1.4f;
        mobBounds.set(position.x - (d / 2.0f), position.y - (h / 2.0f), position.z - (d / 2.0f), (d / 2.0f) + position.x, (h / 2.0f) + position.y, (d / 2.0f) + position.z);
    }

    public float updateGeometry() {
        float y = getMobViewPosition().y;
        MobTexturePack texturePack = getTexturePack();
        generateHead(texturePack.getHead(), y);
        generateBody(texturePack.getBody(), y);
        generateHand(texturePack.getLeftHand(), y, this.mobSize.getLeftHandX(), this.mobSize.getLeftHandZ(), this.handLegWalkAngle);
        generateHand(texturePack.getRightHand(), y, this.mobSize.getRightHandX(), this.mobSize.getRightHandZ(), -this.handLegWalkAngle);
        generateLeg(texturePack.getLeftLeg(), y, this.mobSize.getLeftLegX(), this.mobSize.getLeftLegZ(), -this.handLegWalkAngle);
        generateLeg(texturePack.getRightLeg(), y, this.mobSize.getRightLegX(), this.mobSize.getRightLegZ(), this.handLegWalkAngle);
        return y;
    }

    private void generateHead(TexturedShape s, float y) {
        if (s != null) {
            if (s.getLastRotateAngle() == this.mob.getAngle()) {
                s.restoreCheckpoint();
                return;
            }
            s.reset();
            s.rotateYByOne(this.mob.getAngle());
            s.checkpoint();
        }
    }

    private void generateBody(TexturedShape s, float y) {
        if (s != null) {
            if (s.getLastRotateAngle() == this.mob.getAngle()) {
                s.restoreCheckpoint();
                return;
            }
            s.reset();
            s.rotateYByOne(this.mob.getAngle());
            s.checkpoint();
        }
    }

    private void generateHand(TexturedShape s, float y, float xOffset, float zOffset, float moveBodyAngle) {
        if (s != null) {
            s.reset();
            if (this.mob.isHandsMoving() && moveBodyAngle != HAND_LEG_WALK_RESET_ANGLE) {
                moveHandLeg(s, moveBodyAngle, true);
            }
            s.translate(xOffset, HAND_LEG_WALK_RESET_ANGLE, zOffset);
            s.rotateYByOne(this.mob.getAngle());
        }
    }

    private void generateLeg(TexturedShape s, float y, float xOffset, float zOffset, float moveBodyAngle) {
        if (s != null) {
            s.reset();
            if (moveBodyAngle != HAND_LEG_WALK_RESET_ANGLE) {
                moveHandLeg(s, moveBodyAngle, false);
            }
            s.translate(xOffset, HAND_LEG_WALK_RESET_ANGLE, zOffset);
            s.rotateYByOne(this.mob.getAngle());
        }
    }

    private void moveHandLeg(@NonNull TexturedShape s, float moveBodyAngle, boolean isHand) {
        if (s.getLastRotateAngle() == moveBodyAngle) {
            s.restoreCheckpoint();
            return;
        }
        float offset = isHand ? this.mobSize.getHandActionYOffset() : this.mobSize.getLegActionYOffset();
        s.translate(HAND_LEG_WALK_RESET_ANGLE, -offset, -this.mobSize.getHandLegZOffset());
        s.rotateXByOne((this.mob.isRunning() ? 0.9f : 0.3f) * moveBodyAngle);
        s.translate(HAND_LEG_WALK_RESET_ANGLE, offset, this.mobSize.getHandLegZOffset());
        s.checkpoint();
    }

    @NonNull
    public String toString() {
        return "Mob[id: " + this.mob.getId() + "]";
    }
}
