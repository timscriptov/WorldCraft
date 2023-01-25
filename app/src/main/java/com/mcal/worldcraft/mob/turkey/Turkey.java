package com.mcal.worldcraft.mob.turkey;

import com.mcal.worldcraft.framework.DynamicGameObject3D;

public class Turkey extends DynamicGameObject3D {
    private static final int ALIVE = 0;
    private static final float RADIUS = 0.5f;
    public int state;
    public float stateTime;
    int lives;

    public Turkey(float x, float y, float z) {
        super(x, y, z, RADIUS);
        this.stateTime = 0.0f;
        this.lives = 10;
        this.state = ALIVE;
    }

    public void update(float deltaTime) {
        this.position.add(this.velocity.x * deltaTime, 0.0f, 0.0f);
    }
}
