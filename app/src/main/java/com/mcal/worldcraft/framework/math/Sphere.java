package com.mcal.worldcraft.framework.math;

public class Sphere {
    public final Vector3 center = new Vector3();
    public float radius;

    public Sphere(float x, float y, float z, float radius) {
        center.set(x, y, z);
        this.radius = radius;
    }
}
