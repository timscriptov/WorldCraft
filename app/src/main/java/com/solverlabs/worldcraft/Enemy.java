package com.solverlabs.worldcraft;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.solverlabs.droid.rugl.geom.TexturedShape;
import com.solverlabs.droid.rugl.gl.Renderer;
import com.solverlabs.droid.rugl.text.Font;
import com.solverlabs.droid.rugl.text.TextShape;
import com.solverlabs.droid.rugl.util.Colour;
import com.solverlabs.droid.rugl.util.FPSCamera;
import com.solverlabs.droid.rugl.util.FloatMath;
import com.solverlabs.droid.rugl.util.geom.Frustum;
import com.solverlabs.droid.rugl.util.geom.Vector3f;
import com.solverlabs.droid.rugl.util.math.Range;
import com.solverlabs.worldcraft.factories.BlockFactory;
import com.solverlabs.worldcraft.multiplayer.Multiplayer;
import com.solverlabs.worldcraft.skin.geometry.generator.SkinGeometryGenerator;
import com.solverlabs.worldcraft.srv.domain.Camera;

import java.util.Comparator;

public class Enemy implements Comparable<Enemy> {
    private static final float BODY_Y_OFFSET = 0.1875f;
    private static final float CHARACTER_Y_OFFSET = 0.1875f;
    private static final int FAR_AWAY_DISTANCE = 1000;
    private static final float HAND_ACTION_RESET_ANGLE = 0.0f;
    private static final float HAND_ACTION_Y_OFFSET = 0.5625f;
    private static final float HAND_ACTION_Z_OFFSET = 0.09375f;
    private static final float HAND_LEG_WALK_RESET_ANGLE = 0.0f;
    private static final float HAND_LEG_Z_OFFSET = 0.09375f;
    private static final float HAND_Y_OFFSET = -0.5625f;
    private static final float LEFT_HAND_X_OFFSET = -0.375f;
    private static final float LEFT_LEG_X_OFFSET = -0.1875f;
    private static final float LEG_Y_OFFSET = 0.75f;
    private static final float LEY_Y_OFFSET = -0.5625f;
    private static final float MAX_ACTION_ANGLE = 1.5707964f;
    private static final float MAX_LEG_ANGLE = 0.7853982f;
    private static final int MAX_MOVEMENT_DRAW_DISTANCE = 15;
    private static final float MIN_ACTION_ANGLE = 0.5235988f;
    private static final long MIN_ACTION_TIME = 500;
    private static final float MIN_POINT_DIFF = 0.05f;
    private static final float RIGHT_HAND_X_OFFSET = 0.1875f;
    private static final float RIGHT_LEG_X_OFFSET = 0.0f;
    public static Comparator<Enemy> nameComparator = (lhs, rhs) -> {
        if (rhs == null) {
            return 1;
        }
        if (lhs == null) {
            return -1;
        }
        if (lhs != null && rhs != null && lhs.name != null && rhs.name != null) {
            return lhs.name.compareToIgnoreCase(rhs.name);
        }
        return 0;
    };
    private final Vector3f targetEye = new Vector3f();
    private final Vector3f targetAt = new Vector3f();
    public float angle;
    public float handLegWalkAngle;
    public int id;
    public String name;
    public short skin;
    public float actionHandMovementAngle = MIN_ACTION_ANGLE;
    public Vector3f eye = new Vector3f();
    public Vector3f at = new Vector3f();
    private TexturedShape body;
    private boolean currVisible;
    private float distance;
    private TexturedShape head;
    private byte lastActionType;
    private TexturedShape leftHand;
    private TexturedShape leftLeg;
    private long moveEndTime;
    private long moveStartTime;
    private TextShape nameShape;
    private float prevCamHeading;
    private boolean prevVisible;
    private TexturedShape rightHand;
    private TexturedShape rightLeg;
    private boolean targetReached = false;
    private float legMultiplyParam = 1.0f;
    private float actionHandMovementMultiplyParam = 1.0f;
    private long lastMoveTime = 0;
    private long lastActionTime = 0;
    private long avgMoveDiffTime = 200;
    private boolean isGeometryDirty = true;
    private boolean forceUpdateGeometry = false;

    public Enemy() {
        invalidateShapes();
    }

    public static float getAngle(float atX, float atY, float atZ) {
        float a = 0.0f;
        float b = 0.0f;
        float anglePlus = 0.0f;
        float angleMinus = 0.0f;
        if (atZ == 0.0f) {
            atZ = -0.01f;
        }
        if (atX >= -1.0f && atX < 0.0f && atZ >= 0.0f && atZ <= 1.0f) {
            a = atZ;
            b = atX;
            angleMinus = 1.5707964f;
        } else if (atX >= -1.0f && atX < 0.0f && atZ >= -1.0f && atZ <= 0.0f) {
            a = atZ;
            b = atX;
            anglePlus = 1.5707964f;
        } else if (atX >= 0.0f && atX <= 1.0f && atZ >= -1.0f && atZ <= 0.0f) {
            a = atX;
            b = atZ;
            anglePlus = 3.1415927f;
        } else if (atX >= 0.0f && atX <= 1.0f && atZ > 0.0f && atZ <= 1.0f) {
            a = atZ;
            b = atX;
            anglePlus = 4.712389f;
            angleMinus = 3.1415927f;
        }
        float c = (float) Math.pow((a * a) + (b * b), 0.5d);
        float cos = (-b) / c;
        float angle = (float) (angleMinus + ((angleMinus != 0.0f ? -1 : 1) * Math.acos(cos)) + anglePlus);
        if (Float.compare(angle, Float.NaN) == 0) {
            angle = 0.0f;
        }
        return (-1.0f) * angle;
    }

    public void onMovement(Camera camera) {
        long timeDiff = System.currentTimeMillis() - this.lastMoveTime;
        this.lastMoveTime = System.currentTimeMillis();
        if (timeDiff < 50) {
            this.moveEndTime += timeDiff;
            setTarget(camera);
            return;
        }
        if (timeDiff > MIN_ACTION_TIME) {
            timeDiff = this.avgMoveDiffTime;
        } else {
            this.avgMoveDiffTime = (this.avgMoveDiffTime + timeDiff) / 2;
        }
        this.moveStartTime = System.currentTimeMillis();
        this.moveEndTime = this.moveStartTime + timeDiff + (3 * this.avgMoveDiffTime);
        setTarget(camera);
    }

    public void onAction(byte actionType) {
        this.lastActionType = actionType;
        this.lastActionTime = System.currentTimeMillis();
        invalidateGeometry();
    }

    public void advance(float delta, int worldLoadRadius, FPSCamera cam) {
        updateDistance();
        updateVisibility(worldLoadRadius, cam);
        if (visibilityChanged()) {
            advanceGeometry(delta, cam);
        } else if (this.currVisible) {
            if (!this.isGeometryDirty) {
                rotateNameShape(cam);
                advancePosition();
                return;
            }
            advanceGeometry(delta, cam);
        } else {
            advancePosition();
        }
    }

    private void advanceGeometry(float delta, FPSCamera cam) {
        this.isGeometryDirty = false;
        advancePosition();
        advanceHandLegDueWalkAngle(delta);
        advanceActionHandMovement(delta);
        if (this.isGeometryDirty || this.forceUpdateGeometry) {
            updateGeometry(cam, this.forceUpdateGeometry);
            this.forceUpdateGeometry = false;
        }
    }

    private boolean visibilityChanged() {
        return this.prevVisible != this.currVisible;
    }

    private void updateVisibility(int worldLoadRadius, FPSCamera cam) {
        this.prevVisible = this.currVisible;
        this.currVisible = isVisible(worldLoadRadius, cam);
    }

    public void render(Renderer r, FPSCamera cam) {
        if (this.eye != null && this.at != null) {
            if (this.head != null) {
                this.head.render(r);
            }
            if (this.body != null) {
                this.body.render(r);
            }
            if (this.leftHand != null) {
                this.leftHand.render(r);
            }
            if (this.rightHand != null) {
                this.rightHand.render(r);
            }
            if (this.leftLeg != null) {
                this.leftLeg.render(r);
            }
            if (this.rightLeg != null) {
                this.rightLeg.render(r);
            }
            if (this.nameShape != null) {
                this.nameShape.render(r);
            }
        }
    }

    private void updateGeometry(FPSCamera cam, boolean requireMovementDrawing, boolean forceDraw) {
        float y = this.eye.y + 0.1875f;
        generateHead(this.head, y, requireMovementDrawing);
        generateBody(this.body, y - 0.1875f, requireMovementDrawing);
        generateHand(this.leftHand, y - 0.1875f, LEFT_HAND_X_OFFSET, true, this.handLegWalkAngle, requireMovementDrawing);
        generateHand(this.rightHand, y - 0.1875f, 0.1875f, false, -this.handLegWalkAngle, requireMovementDrawing);
        generateLeg(this.leftLeg, y - LEG_Y_OFFSET, LEFT_LEG_X_OFFSET, -this.handLegWalkAngle, requireMovementDrawing);
        generateLeg(this.rightLeg, y - LEG_Y_OFFSET, 0.0f, this.handLegWalkAngle, requireMovementDrawing);
        updateNameShape(cam, requireMovementDrawing || forceDraw);
    }

    private void rotateNameShape(@NonNull FPSCamera cam) {
        if (cam.getHeading() != this.prevCamHeading) {
            updateNameShape(cam, requireMovementDrawing());
            this.prevCamHeading = cam.getHeading();
        }
    }

    private void updateNameShape(FPSCamera cam, boolean requireMovementDrawing) {
        try {
            this.nameShape.reset();
            if (requireMovementDrawing) {
                float angle = Range.wrap(cam.getHeading(), 0.0f, 6.2831855f) + 3.1415927f;
                if (this.nameShape.getLastRotateAngle() == angle) {
                    this.nameShape.restoreCheckpoint();
                } else {
                    this.nameShape.rotateYByOne(angle);
                    this.nameShape.checkpoint();
                }
            }
            this.nameShape.translate(this.eye.x, this.eye.y + 0.6f, this.eye.z);
        } catch (NullPointerException e) {
            initNameShape();
        }
    }

    private void generateHead(TexturedShape s, float y, boolean requireMovementDrawing) {
        if (s != null) {
            if (s.getLastRotateAngle() == this.angle) {
                s.restoreCheckpoint();
            } else {
                s.reset();
                if (requireMovementDrawing && this.angle != 0.0f) {
                    s.rotateYByOne(this.angle);
                    s.checkpoint();
                }
            }
            s.translate(this.eye.x, y, this.eye.z);
        }
    }

    private void generateBody(TexturedShape s, float y, boolean requireMovementDrawing) {
        if (s != null) {
            if (s.getLastRotateAngle() == this.angle) {
                s.restoreCheckpoint();
            } else {
                s.reset();
                if (requireMovementDrawing && this.angle != 0.0f) {
                    s.rotateYByOne(this.angle);
                    s.checkpoint();
                }
            }
            s.translate(this.eye.x, y, this.eye.z);
        }
    }

    private void generateHand(TexturedShape s, float y, float xOffset, boolean isActionHand, float moveBodyAngle, boolean requireMovementDrawing) {
        if (s != null) {
            s.reset();
            if (requireMovementDrawing) {
                if ((isActionHand && this.actionHandMovementAngle == 0.0f && moveBodyAngle != 0.0f) || (!isActionHand && moveBodyAngle != 0.0f)) {
                    moveHandLeg(s, moveBodyAngle);
                } else if (isActionHand && this.actionHandMovementAngle != 0.0f) {
                    s.translate(0.0f, -0.5625f, -0.09375f);
                    s.rotateXByOne(-this.actionHandMovementAngle);
                    s.translate(0.0f, HAND_ACTION_Y_OFFSET, 0.09375f);
                }
            }
            s.translate(xOffset, -0.5625f, -0.09375f);
            if (requireMovementDrawing) {
                s.rotateYByOne(this.angle);
            }
            s.translate(this.eye.x, y, this.eye.z);
        }
    }

    private void generateLeg(TexturedShape s, float y, float xOffset, float moveBodyAngle, boolean requireMovementDrawing) {
        if (s != null) {
            s.reset();
            if (requireMovementDrawing && moveBodyAngle != 0.0f) {
                moveHandLeg(s, moveBodyAngle);
            }
            s.translate(xOffset, -0.5625f, -0.09375f);
            if (requireMovementDrawing) {
                s.rotateYByOne(this.angle);
            }
            s.translate(this.eye.x, y, this.eye.z);
        }
    }

    private void moveHandLeg(@NonNull TexturedShape s, float moveBodyAngle) {
        if (s.getLastRotateAngle() == moveBodyAngle) {
            s.restoreCheckpoint();
            return;
        }
        s.translate(0.0f, -0.5625f, -0.09375f);
        s.rotateXByOne(moveBodyAngle);
        s.translate(0.0f, HAND_ACTION_Y_OFFSET, 0.09375f);
        s.checkpoint();
    }

    private void advancePosition() {
        if (isWalking() && requireMovementDrawing()) {
            updatePosition();
        } else if (!this.targetReached) {
            jumpToTarget();
        }
    }

    private void advanceActionHandMovement(float delta) {
        if (requireMovementDrawing()) {
            if (isActionHandMoving()) {
                updateActionHandMovementAngle(delta);
            } else {
                resetActionHandAngle();
            }
        }
    }

    private void advanceHandLegDueWalkAngle(float delta) {
        if (requireMovementDrawing()) {
            if (isWalking()) {
                updateHandLegWalkAngle(delta);
            } else {
                resetHandLegWalkAngle();
            }
        }
    }

    private void updateActionHandMovementAngle(float delta) {
        this.actionHandMovementAngle += this.actionHandMovementMultiplyParam * 3.0f * delta * 1.5707964f;
        if (this.actionHandMovementMultiplyParam == 1.0f && this.actionHandMovementAngle > 1.5707964f) {
            this.actionHandMovementAngle = 3.1415927f - this.actionHandMovementAngle;
            this.actionHandMovementMultiplyParam = -1.0f;
        } else if (this.actionHandMovementMultiplyParam == -1.0f && this.actionHandMovementAngle < MIN_ACTION_ANGLE) {
            this.actionHandMovementAngle = 1.0471976f - this.actionHandMovementAngle;
            this.actionHandMovementMultiplyParam = 1.0f;
        }
        invalidateGeometry();
    }

    private void updateHandLegWalkAngle(float delta) {
        this.handLegWalkAngle += this.legMultiplyParam * 2.0f * delta * MAX_LEG_ANGLE;
        if (this.legMultiplyParam == 1.0f && this.handLegWalkAngle > MAX_LEG_ANGLE) {
            this.handLegWalkAngle = 1.5707964f - this.handLegWalkAngle;
            this.legMultiplyParam = -1.0f;
        } else if (this.legMultiplyParam == -1.0f && this.handLegWalkAngle < -0.7853982f) {
            this.handLegWalkAngle = (-1.5707964f) - this.handLegWalkAngle;
            this.legMultiplyParam = 1.0f;
        }
        invalidateGeometry();
    }

    private void updatePosition() {
        float timeDiff = ((float) (System.currentTimeMillis() - this.moveStartTime)) / ((float) (this.moveEndTime - this.moveStartTime));
        setPosition(((this.targetEye.x - this.eye.x) * timeDiff) + this.eye.x, ((this.targetEye.y - this.eye.y) * timeDiff) + this.eye.y, ((this.targetEye.z - this.eye.z) * timeDiff) + this.eye.z, ((this.targetAt.x - this.at.x) * timeDiff) + this.at.x, ((this.targetAt.y - this.at.y) * timeDiff) + this.at.y, ((this.targetAt.z - this.at.z) * timeDiff) + this.at.z);
    }

    private synchronized void setPosition(float eyeX, float eyeY, float eyeZ, float atX, float atY, float atZ) {
        this.eye.x = eyeX;
        this.eye.y = eyeY;
        this.eye.z = eyeZ;
        this.at.x = atX;
        this.at.y = atY;
        this.at.z = atZ;
        this.angle = getAngle(this.at.x, this.at.y, this.at.z);
        updateTargetReached();
        invalidateGeometry();
    }

    private synchronized void setTarget(@NonNull Camera camera) {
        this.targetEye.x = camera.position.x;
        this.targetEye.y = camera.position.y;
        this.targetEye.z = camera.position.z;
        this.targetAt.x = camera.at.x;
        this.targetAt.y = camera.at.y;
        this.targetAt.z = camera.at.z;
        updateTargetReached();
        invalidateGeometry();
    }

    private void updateTargetReached() {
        this.targetReached = Math.abs(this.eye.x - this.targetEye.x) < MIN_POINT_DIFF && Math.abs(this.eye.y - this.targetEye.y) < MIN_POINT_DIFF && Math.abs(this.eye.z - this.targetEye.z) < MIN_POINT_DIFF && Math.abs(this.at.x - this.targetAt.x) < MIN_POINT_DIFF && Math.abs(this.at.y - this.targetAt.y) < MIN_POINT_DIFF && Math.abs(this.at.z - this.targetAt.z) < MIN_POINT_DIFF;
    }

    private void jumpToTarget() {
        setPosition(this.targetEye.x, this.targetEye.y, this.targetEye.z, this.targetAt.x, this.targetAt.y, this.targetAt.z);
    }

    private boolean isWalking() {
        return !this.targetReached && System.currentTimeMillis() < this.moveEndTime;
    }

    private boolean isActionHandMoving() {
        return this.lastActionType == 2 || System.currentTimeMillis() - this.lastActionTime < MIN_ACTION_TIME;
    }

    private void resetActionHandAngle() {
        if (this.actionHandMovementAngle != 0.0f) {
            this.actionHandMovementAngle = 0.0f;
            invalidateGeometry();
        }
    }

    private void resetHandLegWalkAngle() {
        if (this.handLegWalkAngle != 0.0f) {
            this.handLegWalkAngle = 0.0f;
            invalidateGeometry();
        }
    }

    private void updateGeometry(FPSCamera cam, boolean forceDraw) {
        if (this.head == null) {
            this.head = SkinGeometryGenerator.createHeadShape(this.skin);
            this.body = SkinGeometryGenerator.createBodyShape(this.skin);
            this.rightHand = SkinGeometryGenerator.createHandShape(this.skin);
            this.leftHand = SkinGeometryGenerator.createHandShape(this.skin);
            this.rightLeg = SkinGeometryGenerator.createLegShape(this.skin);
            this.leftLeg = SkinGeometryGenerator.createLegShape(this.skin);
        }
        if (this.nameShape == null) {
            initNameShape();
        }
        updateGeometry(cam, requireMovementDrawing(), forceDraw);
    }

    private void initNameShape() {
        this.nameShape = createPlayerNameShape();
    }

    @Nullable
    private TextShape createPlayerNameShape() {
        Font font = CharactersPainter.font;
        if (this.name == null || font == null) {
            return null;
        }
        TextShape shape = font.buildTextShape(this.name, Colour.packFloat(1.0f, 1.0f, 1.0f, 1.0f));
        shape.scale(0.008f, 0.008f, 0.008f);
        shape.translate((-(font.getStringLength(this.name) / 2.0f)) * 0.008f, 0.0f, 0.0f);
        shape.backup();
        return shape;
    }

    public boolean isVisible(int worldLoadRadius, FPSCamera cam) {
        return isRenderable(worldLoadRadius) && isInFrustrum(cam);
    }

    private boolean isInFrustrum(FPSCamera cam) {
        try {
            return cam.getFrustum().sphereIntersects(this.eye.x, this.eye.y, this.eye.z, 1.0f) != Frustum.Result.Miss;
        } catch (NullPointerException e) {
            return false;
        }
    }

    private void updateDistance() {
        try {
            Vector3f playerEye = Multiplayer.instance.movementHandler.getEye();
            this.distance = FloatMath.sqrt((float) (Math.pow(this.eye.x - playerEye.x, 2.0d) + Math.pow(this.eye.y - playerEye.y, 2.0d) + Math.pow(this.eye.z - playerEye.z, 2.0d)));
        } catch (NullPointerException e) {
            this.distance = 1000.0f;
        }
    }

    private boolean isRenderable(int loadradius) {
        float visibleRadius = Math.min(loadradius << 4, BlockFactory.state.fog.end);
        return this.distance < visibleRadius;
    }

    public boolean requireMovementDrawing() {
        return true;
    }

    @Override
    public int compareTo(Enemy another) {
        if (this.name != null) {
            return this.name.compareTo(another.name);
        }
        return 0;
    }

    @NonNull
    public String toString() {
        return "Enemy[id: " + this.id + ", name: " + this.name + ", skin: " + (int) this.skin;
    }

    public void invalidate() {
        invalidateShapes();
        invalidateGeometry();
        this.forceUpdateGeometry = true;
    }

    private void invalidateGeometry() {
        this.isGeometryDirty = true;
    }

    private void invalidateShapes() {
        this.head = null;
        this.body = null;
        this.rightHand = null;
        this.leftHand = null;
        this.rightLeg = null;
        this.leftLeg = null;
        this.nameShape = null;
    }
}
