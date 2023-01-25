package com.mcal.droid.rugl.gl.enums;


public enum SourceFactor {
    ZERO(0),
    ONE(1),
    DST_COLOR(774),
    ONE_MINUS_DST_COLOR(775),
    SRC_ALPHA(770),
    ONE_MINUS_SRC_ALPHA(771),
    DST_ALPHA(772),
    ONE_MINUS_DST_ALPHA(773),
    SRC_ALPHA_SATURATE(776);

    public final int value;

    SourceFactor(int value) {
        this.value = value;
    }
}
