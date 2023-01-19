package com.solverlabs.droid.rugl.gl.enums;


public enum DrawMode {
    Points(0),
    LineStrip(3),
    LineLoop(2),
    Lines(1),
    TriangleStrip(5),
    TriangleFan(6),
    Triangles(4);

    public final int glValue;

    DrawMode(int glValue) {
        this.glValue = glValue;
    }
}
