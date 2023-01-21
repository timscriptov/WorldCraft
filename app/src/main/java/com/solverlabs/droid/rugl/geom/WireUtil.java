package com.solverlabs.droid.rugl.geom;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.gl.GLUtil;
import com.solverlabs.droid.rugl.gl.State;
import com.solverlabs.droid.rugl.gl.enums.DrawMode;

import org.jetbrains.annotations.Contract;


public class WireUtil {
    public static State state = GLUtil.typicalState.with(DrawMode.Lines);

    @NonNull
    @Contract(" -> new")
    public static Shape unitCube() {
        float[] verts = {0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f};
        short[] li = {0, 2, 1, 3, 4, 6, 5, 7, 0, 1, 2, 3, 4, 5, 6, 7, 0, 4, 1, 5, 2, 6, 3, 7};
        return new Shape(verts, li);
    }
}
