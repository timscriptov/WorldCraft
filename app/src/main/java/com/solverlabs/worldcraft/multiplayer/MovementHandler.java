package com.solverlabs.worldcraft.multiplayer;

import com.solverlabs.droid.rugl.util.geom.Vector3f;
import com.solverlabs.worldcraft.GameMode;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MovementHandler {
    public static final byte ACTION_CLICK = 1;
    public static final byte ACTION_CLICK_HOLD_FINISH = 3;
    public static final byte ACTION_CLICK_HOLD_START = 2;
    private static final long DELAY = 25;
    private static final float MIN_MOVE_DISTANCE = 0.1f;
    private static final long RESPONSE_WAIT_TIME = 5000;
    private final Vector3f eye = new Vector3f();
    private final Vector3f at = new Vector3f();
    private final Vector3f up = new Vector3f();
    public boolean responseReceived = true;
    private long lastAccessMillis;
    private MovementHandlerListener listener;

    public static boolean coordsEqual(float v1, float v2) {
        return roundDownScale((double) v1, 2) == roundDownScale((double) v2, 2);
    }

    public static float roundDownScale(double aValue, int scale) {
        BigDecimal decimal = new BigDecimal(aValue);
        return decimal.setScale(scale, RoundingMode.HALF_UP).floatValue();
    }

    public Vector3f getEye() {
        return this.eye;
    }

    public void setListener(MovementHandlerListener listener) {
        this.listener = listener;
    }

    private boolean isMovedEnough(float eyeX, float eyeY, float eyeZ, float atX, float atY, float atZ, float upX, float upY, float upZ) {
        return Math.abs(eyeX - this.eye.x) > MIN_MOVE_DISTANCE || Math.abs(eyeY - this.eye.y) > MIN_MOVE_DISTANCE || Math.abs(eyeZ - this.eye.z) > MIN_MOVE_DISTANCE || Math.abs(atX - this.at.x) > MIN_MOVE_DISTANCE || Math.abs(atY - this.at.y) > MIN_MOVE_DISTANCE || Math.abs(atZ - this.at.z) > MIN_MOVE_DISTANCE || Math.abs(upX - this.up.x) > MIN_MOVE_DISTANCE || Math.abs(upY - this.up.y) > MIN_MOVE_DISTANCE || Math.abs(upZ - this.up.z) > MIN_MOVE_DISTANCE;
    }

    public void set(float eyeX, float eyeY, float eyeZ, float atX, float atY, float atZ, float upX, float upY, float upZ) {
        if (GameMode.isMultiplayerMode()) {
            if ((this.responseReceived || System.currentTimeMillis() - this.lastAccessMillis >= RESPONSE_WAIT_TIME) && isMovedEnough(eyeX, eyeY, eyeZ, atX, atY, atZ, upX, upY, upZ) && System.currentTimeMillis() - this.lastAccessMillis > DELAY) {
                this.eye.x = roundDownScale(eyeX, 2);
                this.eye.y = roundDownScale(eyeY, 2);
                this.eye.z = roundDownScale(eyeZ, 2);
                this.at.x = roundDownScale(atX, 2);
                this.at.y = roundDownScale(atY, 2);
                this.at.z = roundDownScale(atZ, 2);
                this.up.x = roundDownScale(upX, 2);
                this.up.y = roundDownScale(upY, 2);
                this.up.z = roundDownScale(upZ, 2);
                this.lastAccessMillis = System.currentTimeMillis();
                if (Multiplayer.instance.isClientGraphicInited && this.listener != null) {
                    this.responseReceived = false;
                    this.listener.myLocationChanged(this.eye, this.at, this.up);
                }
            }
        }
    }

    public void action(byte action) {
        if (this.listener != null) {
            this.listener.myAction(action);
        }
    }

    public void informMyLocation() {
        if (Multiplayer.instance.isClientGraphicInited && this.listener != null) {
            this.listener.myLocationChanged(this.eye, this.at, this.up);
        }
    }

    public void clientGraphicsInited() {
        if (this.listener != null) {
            this.listener.myGraphicsInited(this.eye, this.at, this.up);
        }
    }

    public interface MovementHandlerListener {
        void myAction(byte b);

        void myGraphicsInited(Vector3f vector3f, Vector3f vector3f2, Vector3f vector3f3);

        void myLocationChanged(Vector3f vector3f, Vector3f vector3f2, Vector3f vector3f3);
    }
}
