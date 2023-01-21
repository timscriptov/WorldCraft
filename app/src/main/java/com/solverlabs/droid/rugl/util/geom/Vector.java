package com.solverlabs.droid.rugl.util.geom;

import java.io.Serializable;
import java.nio.FloatBuffer;

public abstract class Vector implements Serializable, ReadableVector {
    private static final long serialVersionUID = 1;

    public abstract float lengthSquared();

    public abstract Vector load(FloatBuffer floatBuffer);

    public abstract Vector negate();

    public abstract Vector scale(float f);

    public abstract Vector store(FloatBuffer floatBuffer);

    @Override
    public final float length() {
        return (float) Math.sqrt(lengthSquared());
    }

    public final Vector normalise() {
        float len = length();
        if (len != 0.0f) {
            float l = 1.0f / len;
            return scale(l);
        }
        throw new IllegalStateException("Zero length vector");
    }
}
