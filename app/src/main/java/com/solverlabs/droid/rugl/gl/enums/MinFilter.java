package com.solverlabs.droid.rugl.gl.enums;


public enum MinFilter {
    NEAREST(9728),
    LINEAR(9729),
    NEAREST_MIPMAP_NEAREST(9984),
    LINEAR_MIPMAP_NEAREST(9985),
    NEAREST_MIPMAP_LINEAR(9986),
    LINEAR_MIPMAP_LINEAR(9987);

    public final int value;

    MinFilter(int value) {
        this.value = value;
    }
}
