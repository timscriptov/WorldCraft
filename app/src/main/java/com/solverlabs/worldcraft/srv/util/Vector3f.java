package com.solverlabs.worldcraft.srv.util;

import androidx.annotation.NonNull;

public class Vector3f {
    public float x;
    public float y;
    public float z;

    @NonNull
    public String toString() {
        return "Vector3f@" + this.x + "," + this.y + "," + this.z;
    }
}
