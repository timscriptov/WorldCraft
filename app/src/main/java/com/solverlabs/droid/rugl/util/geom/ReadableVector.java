package com.solverlabs.droid.rugl.util.geom;

import java.nio.FloatBuffer;


public interface ReadableVector {
    float length();

    float lengthSquared();

    Vector store(FloatBuffer floatBuffer);
}
