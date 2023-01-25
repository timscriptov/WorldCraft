package com.mcal.worldcraft.framework.math;

public class Circle {
    public final Vector2 center = new Vector2();
    public float radius;

    public Circle(float x, float y, float radius) {
        center.set(x, y);
        this.radius = radius;
    }
}
