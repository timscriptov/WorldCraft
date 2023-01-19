package com.solverlabs.droid.rugl.gl.enums;


public enum FogMode {
    LINEAR(9729),
    EXP(2048),
    EXP2(2049);

    public final int mode;

    FogMode(int mode) {
        this.mode = mode;
    }
}
