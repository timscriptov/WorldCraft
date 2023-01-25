package com.mcal.worldcraft.blockentity;

import com.mcal.droid.rugl.gl.StackedRenderer;

public interface BlockEntity {
    void advance(float f);

    void draw(StackedRenderer stackedRenderer);

    boolean isDestroyed();
}
