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
        long timeDiff = System.currentTimeMillis() - lastMoveTime;
        lastMoveTime = System.currentTimeMillis();
        if (timeDiff < 50) {
            moveEndTime += timeDiff;
            setTarget(camera);
            return;
        }
        if (timeDiff > MIN_ACTION_TIME) {
            timeDiff = avgMoveDiffTime;
        } else {
            avgMoveDiffTime = (avgMoveDiffTime + timeDiff) / 2;
        }
        moveStartTime = System.currentTimeMillis();
        moveEndTime = moveStartTime + timeDiff + (3 * avgMoveDiffTime);
        setTarget(camera);
    }

    public void onAction(byte actionType) {
        lastActionType = actionType;
        lastActionTime = System.currentTimeMillis();
        invalidateGeometry();
    }

    public void advance(float delta, int worldLoadRadius, FPSCamera cam) {
        updateDistance();
        updateVisibility(worldLoadRadius, cam);
        if (visibilityChanged()) {
            advanceGeometry(delta, cam);
        } else if (currVisible) {
            if (!isGeometryDirty) {
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
        isGeometryDirty = false;
        advancePosition();
        advanceHandLegDueWalkAngle(delta);
        advanceActionHandMovement(delta);
        if (isGeometryDirty || forceUpdateGeometry) {
            updateGeometry(cam, forceUpdateGeometry);
            forceUpdateGeometry = false;
        }
    }

    private boolean visibilityChanged() {
        return prevVisible != currVisible;
    }

    private void updateVisibility(int worldLoadRadius, FPSCamera cam) {
        prevVisible = currVisible;
        currVisible = isVisible(worldLoadRadius, cam);
    }

    public void render(Renderer r, FPSCamera cam) {
        if (eye != null && at != null) {
            if (head != null) {
                head.render(r);
            }
            if (body != null) {
                body.render(r);
            }
            if (leftHand != null) {
                leftHand.render(r);
            }
            if (rightHand != null) {
                rightHand.render(r);
            }
            if (leftLeg != null) {
                leftLeg.render(r);
            }
            if (rightLeg != null) {
                rightLeg.render(r);
            }
            if (nameShape != null) {
                nameShape.render(r);
            }
        }
    }

    private void updateGeometry(FPSCamera cam, boolean requireMovementDrawing, boolean forceDraw) {
        float y = eye.y + 0.1875f;
        generateHead(head, y, requireMovementDrawing);
        generateBody(body, y - 0.1875f, requireMovementDrawing);
        generateHand(leftHand, y - 0.1875f, LEFT_HAND_X_OFFSET, true, handLegWalkAngle, requireMovementDrawing);
        generateHand(rightHand, y - 0.1875f, 0.1875f, false, -handLegWalkAngle, requireMovementDrawing);
        generateLeg(leftLeg, y - LEG_Y_OFFSET, LEFT_LEG_X_OFFSET, -handLegWalkAngle, requireMovementDrawing);
        generateLeg(rightLeg, y - LEG_Y_OFFSET, 0.0f, handLegWalkAngle, requireMovementDrawing);
        updateNameShape(cam, requireMovementDrawing || forceDraw);
    }

    private void rotateNameShape(@NonNull FPSCamera cam) {
        if (cam.getHeading() != prevCamHeading) {
            updateNameShape(cam, requireMovementDrawing());
            prevCamHeading = cam.getHeading();
        }
    }

    private void updateNameShape(FPSCamera cam, boolean requireMovementDrawing) {
        try {
            nameShape.reset();
            if (requireMovementDrawing) {
                float angle = Range.wrap(cam.getHeading(), 0.0f, 6.2831855f) + 3.1415927f;
                if (nameShape.getLastRotateAngle() == angle) {
                    nameShape.restoreCheckpoint();
                } else {
                    nameShape.rotateYByOne(angle);
                    nameShape.checkpoint();
                }
            }
            nameShape.translate(eye.x, eye.y + 0.6f, eye.z);
        } catch (NullPointerException e) {
            initNameShape();
        }
    }

    private void generateHead(TexturedShape s, float y, boolean requireMovementDrawing) {
        if (s != null) {
            if (s.getLastRotateAngle() == angle) {
                s.restoreCheckpoint();
            } else {
                s.reset();
                if (requireMovementDrawing && angle != 0.0f) {
                    s.rotateYByOne(angle);
                    s.checkpoint();
                }
            }
            s.translate(eye.x, y, eye.z);
        }
    }

    private void generateBody(TexturedShape s, float y, boolean requireMovementDrawing) {
        if (s != null) {
            if (s.getLastRotateAngle() == angle) {
                s.restoreCheckpoint();
            } else {
                s.reset();
                if (requireMovementDrawing && angle != 0.0f) {
                    s.rotateYByOne(angle);
                    s.checkpoint();
                }
            }
            s.translate(eye.x, y, eye.z);
        }
    }

    private void generateHand(TexturedShape s, float y, float xOffset, boolean isActionHand, float moveBodyAngle, boolean requireMovementDrawing) {
        if (s != null) {
            s.reset();
            if (requireMovementDrawing) {
                if ((isActionHand && actionHandMovementAngle == 0.0f && moveBodyAngle != 0.0f) || (!isActionHand && moveBodyAngle != 0.0f)) {
                    moveHandLeg(s, moveBodyAngle);
                } else if (isActionHand && actionHandMovementAngle != 0.0f) {
                    s.translate(0.0f, -0.5625f, -0.09375f);
                    s.rotateXByOne(-actionHandMovementAngle);
                    s.translate(0.0f, HAND_ACTION_Y_OFFSET, 0.09375f);
                }
            }
            s.translate(xOffset, -0.5625f, -0.09375f);
            if (requireMovementDrawing) {
                s.rotateYByOne(angle);
            }
            s.translate(eye.x, y, eye.z);
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
                s.rotateYByOne(angle);
            }
            s.translate(eye.x, y, eye.z);
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
        } else if (!targetReached) {
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
        actionHandMovementAngle += actionHandMovementMultiplyParam * 3.0f * delta * 1.5707964f;
        if (actionHandMovementMultiplyParam == 1.0f && actionHandMovementAngle > 1.5707964f) {
            actionHandMovementAngle = 3.1415927f - actionHandMovementAngle;
            actionHandMovementMultiplyParam = -1.0f;
        } else if (actionHandMovementMultiplyParam == -1.0f && actionHandMovementAngle < MIN_ACTION_ANGLE) {
            actionHandMovementAngle = 1.0471976f - actionHandMovementAngle;
            actionHandMovementMultiplyParam = 1.0f;
        }
        invalidateGeometry();
    }

    private void updateHandLegWalkAngle(float delta) {
        handLegWalkAngle += legMultiplyParam * 2.0f * delta * MAX_LEG_ANGLE;
        if (legMultiplyParam == 1.0f && handLegWalkAngle > MAX_LEG_ANGLE) {
            handLegWalkAngle = 1.5707964f - handLegWalkAngle;
            legMultiplyParam = -1.0f;
        } else if (legMultiplyParam == -1.0f && handLegWalkAngle < -0.7853982f) {
            handLegWalkAngle = (-1.5707964f) - handLegWalkAngle;
            legMultiplyParam = 1.0f;
        }
        invalidateGeometry();
    }

    private void updatePosition() {
        float timeDiff = ((float) (System.currentTimeMillis() - moveStartTime)) / ((float) (moveEndTime - moveStartTime));
        setPosition(((targetEye.x - eye.x) * timeDiff) + eye.x, ((targetEye.y - eye.y) * timeDiff) + eye.y, ((targetEye.z - eye.z) * timeDiff) + eye.z, ((targetAt.x - at.x) * timeDiff) + at.x, ((targetAt.y - at.y) * timeDiff) + at.y, ((targetAt.z - at.z) * timeDiff) + at.z);
    }

    private synchronized void setPosition(float eyeX, float eyeY, float eyeZ, float atX, float atY, float atZ) {
        eye.x = eyeX;
        eye.y = eyeY;
        eye.z = eyeZ;
        at.x = atX;
        at.y = atY;
        at.z = atZ;
        angle = getAngle(at.x, at.y, at.z);
        updateTargetReached();
        invalidateGeometry();
    }

    private synchronized void setTarget(@NonNull Camera camera) {
        targetEye.x = camera.position.x;
        targetEye.y = camera.position.y;
        targetEye.z = camera.position.z;
        targetAt.x = camera.at.x;
        targetAt.y = camera.at.y;
        targetAt.z = camera.at.z;
        updateTargetReached();
        invalidateGeometry();
    }

    private void updateTargetReached() {
        targetReached = Math.abs(eye.x - targetEye.x) < MIN_POINT_DIFF && Math.abs(eye.y - targetEye.y) < MIN_POINT_DIFF && Math.abs(eye.z - targetEye.z) < MIN_POINT_DIFF && Math.abs(at.x - targetAt.x) < MIN_POINT_DIFF && Math.abs(at.y - targetAt.y) < MIN_POINT_DIFF && Math.abs(at.z - targetAt.z) < MIN_POINT_DIFF;
    }

    private void jumpToTarget() {
        setPosition(targetEye.x, targetEye.y, targetEye.z, targetAt.x, targetAt.y, targetAt.z);
    }

    private boolean isWalking() {
        return !targetReached && System.currentTimeMillis() < moveEndTime;
    }

    private boolean isActionHandMoving() {
        return lastActionType == 2 || System.currentTimeMillis() - lastActionTime < MIN_ACTION_TIME;
    }

    private void resetActionHandAngle() {
        if (actionHandMovementAngle != 0.0f) {
            actionHandMovementAngle = 0.0f;
            invalidateGeometry();
        }
    }

    private void resetHandLegWalkAngle() {
        if (handLegWalkAngle != 0.0f) {
            handLegWalkAngle = 0.0f;
            invalidateGeometry();
        }
    }

    private void updateGeometry(FPSCamera cam, boolean forceDraw) {
        if (head == null) {
            head = SkinGeometryGenerator.createHeadShape(skin);
            body = SkinGeometryGenerator.createBodyShape(skin);
            rightHand = SkinGeometryGenerator.createHandShape(skin);
            leftHand = SkinGeometryGenerator.createHandShape(skin);
            rightLeg = SkinGeometryGenerator.createLegShape(skin);
            leftLeg = SkinGeometryGenerator.createLegShape(skin);
        }
        if (nameShape == null) {
            initNameShape();
        }
        updateGeometry(cam, requireMovementDrawing(), forceDraw);
    }

    private void initNameShape() {
        nameShape = createPlayerNameShape();
    }

    @Nullable
    private TextShape createPlayerNameShape() {
        Font font = CharactersPainter.font;
        if (name == null || font == null) {
            return null;
        }
        TextShape shape = font.buildTextShape(name, Colour.packFloat(1.0f, 1.0f, 1.0f, 1.0f));
        shape.scale(0.008f, 0.008f, 0.008f);
        shape.translate((-(font.getStringLength(name) / 2.0f)) * 0.008f, 0.0f, 0.0f);
        shape.backup();
        return shape;
    }

    public boolean isVisible(int worldLoadRadius, FPSCamera cam) {
        return isRenderable(worldLoadRadius) && isInFrustrum(cam);
    }

    private boolean isInFrustrum(FPSCamera cam) {
        try {
            return cam.getFrustum().sphereIntersects(eye.x, eye.y, eye.z, 1.0f) != Frustum.Result.Miss;
        } catch (NullPointerException e) {
            return false;
        }
    }

    private void updateDistance() {
        try {
            Vector3f playerEye = Multiplayer.instance.movementHandler.getEye();
            distance = FloatMath.sqrt((float) (Math.pow(eye.x - playerEye.x, 2.0d) + Math.pow(eye.y - playerEye.y, 2.0d) + Math.pow(eye.z - playerEye.z, 2.0d)));
        } catch (NullPointerException e) {
            distance = 1000.0f;
        }
    }

    private boolean isRenderable(int loadradius) {
        float visibleRadius = Math.min(loadradius << 4, BlockFactory.state.fog.end);
        return distance < visibleRadius;
    }

    public boolean requireMovementDrawing() {
        return true;
    }

    @Override
    public int compareTo(Enemy another) {
        if (name != null) {
            return name.compareTo(another.name);
        }
        return 0;
    }

    @NonNull
    public String toString() {
        return "Enemy[id: " + id + ", name: " + name + ", skin: " + (int) skin;
    }

    public void invalidate() {
        invalidateShapes();
        invalidateGeometry();
        forceUpdateGeometry = true;
    }

    private void invalidateGeometry() {
        isGeometryDirty = true;
    }

    private void invalidateShapes() {
        head = null;
        body = null;
        rightHand = null;
        leftHand = null;
        rightLeg = null;
        leftLeg = null;
        nameShape = null;
    }
}
