package com.solverlabs.worldcraft.blockentity;

import com.solverlabs.droid.rugl.gl.StackedRenderer;

public interface BlockEntity {
    void advance(float f);

    void draw(StackedRenderer stackedRenderer);

    boolean isDestroyed();
}
