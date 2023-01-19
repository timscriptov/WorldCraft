package com.solverlabs.worldcraft.multiplayer.util;

import com.solverlabs.worldcraft.srv.util.Vector3f;


public class Vector3fUtils {
    public static Vector3f convert(com.solverlabs.droid.rugl.util.geom.Vector3f v) {
        Vector3f e = new Vector3f();
        e.x = v.x;
        e.y = v.y;
        e.z = v.z;
        return e;
    }
}
