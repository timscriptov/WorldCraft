package com.solverlabs.worldcraft.framework;

import com.solverlabs.worldcraft.framework.math.Vector3;

public class DynamicGameObject3D extends GameObject3D {
    public final Vector3 accel;
    public final Vector3 velocity;

    public DynamicGameObject3D(float x, float y, float z, float radius) {
        super(x, y, z, radius);
        velocity = new Vector3();
        accel = new Vector3();
    }
}
