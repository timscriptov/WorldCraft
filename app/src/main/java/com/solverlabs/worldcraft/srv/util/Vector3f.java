package com.solverlabs.worldcraft.srv.util;


public class Vector3f {
    public float x;
    public float y;
    public float z;

    public String toString() {
        return new StringBuffer("Vector3f@").append(this.x).append(",").append(this.y).append(",").append(this.z).toString();
    }
}
