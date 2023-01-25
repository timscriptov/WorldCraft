package com.mcal.worldcraft.framework;

import com.mcal.worldcraft.framework.math.Vector2;

public class DynamicGameObject extends GameObject {
    public final Vector2 accel;
    public final Vector2 velocity;

    public DynamicGameObject(float x, float y, float width, float height) {
        super(x, y, width, height);
        velocity = new Vector2();
        accel = new Vector2();
    }
}
