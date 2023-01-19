package com.solverlabs.droid.rugl.util.geom;

import java.io.Serializable;
import java.nio.FloatBuffer;


public abstract class Matrix implements Serializable {
    private static final long serialVersionUID = 1;

    public abstract float determinant();

    public abstract Matrix invert();

    public abstract Matrix load(FloatBuffer floatBuffer);

    public abstract Matrix loadTranspose(FloatBuffer floatBuffer);

    public abstract Matrix negate();

    public abstract Matrix setIdentity();

    public abstract Matrix setZero();

    public abstract Matrix store(FloatBuffer floatBuffer);

    public abstract Matrix storeTranspose(FloatBuffer floatBuffer);

    public abstract Matrix transpose();
}
