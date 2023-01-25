package com.mcal.worldcraft.framework;

import com.mcal.worldcraft.framework.math.Sphere;
import com.mcal.worldcraft.framework.math.Vector3;

public class GameObject3D {
    public final Sphere bounds;
    public final Vector3 position;

    public GameObject3D(float x, float y, float z, float radius) {
        position = new Vector3(x, y, z);
        bounds = new Sphere(x, y, z, radius);
    }
}
