package com.solverlabs.worldcraft.framework;

import com.solverlabs.worldcraft.framework.math.Rectangle;
import com.solverlabs.worldcraft.framework.math.Vector2;


public class GameObject {
    public final Rectangle bounds;
    public final Vector2 position;

    public GameObject(float x, float y, float width, float height) {
        this.position = new Vector2(x, y);
        this.bounds = new Rectangle(x - (width / 2.0f), y - (height / 2.0f), width, height);
    }
}
