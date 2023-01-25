package com.mcal.droid.rugl.gl.enums;


public enum DestinationFactor {
    ZERO(0),
    ONE(1),
    SRC_COLOR(768),
    ONE_MINUS_SRC_COLOR(769),
    SRC_ALPHA(770),
    ONE_MINUS_SRC_ALPHA(771),
    DST_ALPHA(772),
    ONE_MINUS_DST_ALPHA(773);

    public final int value;

    DestinationFactor(int value) {
        this.value = value;
    }
}
