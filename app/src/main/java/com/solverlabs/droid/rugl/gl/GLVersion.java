package com.solverlabs.droid.rugl.gl;


import androidx.annotation.Nullable;

public enum GLVersion {
    OnePointZero("1.0"),
    OnePointOne("1.1"),
    TwoPointZero("2.0");

    private final String versionString;

    GLVersion(String versionString) {
        this.versionString = versionString;
    }

    @Nullable
    public static GLVersion findVersion(String glVersionString) {
        GLVersion[] arr$ = values();
        for (GLVersion glv : arr$) {
            if (glVersionString.contains(glv.versionString)) {
                return glv;
            }
        }
        return null;
    }
}
