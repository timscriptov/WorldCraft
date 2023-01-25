package com.mcal.droid.rugl.geom.line;

import com.mcal.droid.rugl.util.geom.Vector2f;

import java.util.List;


public interface LineCap {
    void createVerts(Vector2f vector2f, Vector2f vector2f2, short s, short s2, float f, List<Vector2f> list, List<Short> list2);
}
