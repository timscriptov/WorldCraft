package com.mcal.worldcraft.multiplayer.util;

import androidx.annotation.NonNull;

import com.mcal.worldcraft.srv.util.Vector3f;

public class Vector3fUtils {
    @NonNull
    public static Vector3f convert(@NonNull com.mcal.droid.rugl.util.geom.Vector3f v) {
        Vector3f e = new Vector3f();
        e.x = v.x;
        e.y = v.y;
        e.z = v.z;
        return e;
    }
}
