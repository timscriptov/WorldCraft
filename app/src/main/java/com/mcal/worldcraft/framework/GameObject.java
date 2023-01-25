package com.mcal.worldcraft.framework;

import com.mcal.worldcraft.framework.math.Rectangle;
import com.mcal.worldcraft.framework.math.Vector2;

public class GameObject {
    public final Rectangle bounds;
    public final Vector2 position;

    public GameObject(float x, float y, float width, float height) {
        position = new Vector2(x, y);
        bounds = new Rectangle(x - (width / 2.0f), y - (height / 2.0f), width, height);
    }
}
