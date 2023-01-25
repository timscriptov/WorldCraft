package com.mcal.droid.rugl.gl.enums;


public enum ComparisonFunction {
    NEVER(512),
    LESS(513),
    EQUAL(514),
    LEQUAL(515),
    GREATER(516),
    NOTEQUAL(517),
    GEQUAL(518),
    ALWAYS(519);

    public final int value;

    ComparisonFunction(int value) {
        this.value = value;
    }
}
