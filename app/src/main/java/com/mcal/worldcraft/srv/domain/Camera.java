package com.mcal.worldcraft.srv.domain;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mcal.worldcraft.srv.util.Vector3f;

import org.jetbrains.annotations.Contract;

public class Camera {
    public Vector3f at;
    public int playerId;
    public Vector3f position;
    public Vector3f up;

    public Camera() {
        this.position = new Vector3f();
        this.at = new Vector3f();
        this.up = new Vector3f();
    }

    public Camera(int i, Vector3f vector3f, Vector3f vector3f2, Vector3f vector3f3) {
        this.playerId = i;
        this.position = vector3f;
        this.at = vector3f2;
        this.up = vector3f3;
    }

    @NonNull
    @Contract(pure = true)
    public static String pack(@NonNull Camera camera) {
        return camera.position.x + "," + camera.position.y + "," + camera.position.z + "," + camera.at.x + "," + camera.at.y + "," + camera.at.z + "," + camera.up.x + "," + camera.up.y + "," + camera.up.z + ",";
    }

    @Nullable
    public static Camera unpack(@NonNull String str) {
        if (str.split(",").length != 9) {
            return null;
        }
        Camera camera = new Camera();
        camera.updateFromPack(str);
        return camera;
    }

    private boolean zeroPosition() {
        return this.position.x == 0.0f && this.position.y == 0.0f && this.position.z == 0.0f;
    }

    public boolean isValid() {
        return this.position != null && !zeroPosition();
    }

    @NonNull
    public String toString() {
        StringBuilder stringBuffer = new StringBuilder();
        stringBuffer.append("player id: ").append(this.playerId);
        if (this.position == null) {
            stringBuffer.append(" eye is null");
        } else {
            stringBuffer.append(" ex: ").append(this.position.x).append(" ey: ").append(this.position.y).append(" ez: ").append(this.position.z);
        }
        if (this.at == null) {
            stringBuffer.append(" at is null");
        } else {
            stringBuffer.append(" ax: ").append(this.at.x).append(" ay: ").append(this.at.y).append(" az: ").append(this.at.z);
        }
        if (this.up == null) {
            stringBuffer.append(" up is null");
        } else {
            stringBuffer.append(" ux: ").append(this.up.x).append(" uy: ").append(this.up.y).append(" uz: ").append(this.up.z);
        }
        return stringBuffer.toString();
    }

    public void updateFromPack(@NonNull String str) {
        String[] split = str.split(",");
        if (split.length != 9) {
            return;
        }
        this.position = new Vector3f();
        this.at = new Vector3f();
        this.up = new Vector3f();
        this.position.x = Float.parseFloat(split[0]);
        this.position.y = Float.parseFloat(split[1]);
        this.position.z = Float.parseFloat(split[2]);
        this.at.x = Float.parseFloat(split[3]);
        this.at.y = Float.parseFloat(split[4]);
        this.at.z = Float.parseFloat(split[5]);
        this.up.x = Float.parseFloat(split[6]);
        this.up.y = Float.parseFloat(split[7]);
        this.up.z = Float.parseFloat(split[8]);
    }
}
