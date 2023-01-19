package com.solverlabs.droid.rugl.gl;


public enum GLVersion {
    OnePointZero("1.0"),
    OnePointOne("1.1"),
    TwoPointZero("2.0");

    private final String versionString;

    GLVersion(String versionString) {
        this.versionString = versionString;
    }

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
