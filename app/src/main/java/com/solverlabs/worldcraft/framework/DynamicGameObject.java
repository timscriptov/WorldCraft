package com.solverlabs.worldcraft.framework;

import com.solverlabs.worldcraft.framework.math.Vector2;


public class DynamicGameObject extends GameObject {
    public final Vector2 accel;
    public final Vector2 velocity;

    public DynamicGameObject(float x, float y, float width, float height) {
        super(x, y, width, height);
        this.velocity = new Vector2();
        this.accel = new Vector2();
    }
}
