package com.solverlabs.worldcraft.framework;

import com.solverlabs.worldcraft.framework.math.Sphere;
import com.solverlabs.worldcraft.framework.math.Vector3;


public class GameObject3D {
    public final Sphere bounds;
    public final Vector3 position;

    public GameObject3D(float x, float y, float z, float radius) {
        this.position = new Vector3(x, y, z);
        this.bounds = new Sphere(x, y, z, radius);
    }
}
