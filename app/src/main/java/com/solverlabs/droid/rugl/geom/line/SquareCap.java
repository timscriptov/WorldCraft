package com.solverlabs.droid.rugl.geom.line;

import com.solverlabs.droid.rugl.util.geom.Vector2f;
import com.solverlabs.droid.rugl.util.geom.VectorUtils;

import java.util.List;


public class SquareCap implements LineCap {
    @Override
    public void createVerts(Vector2f endPoint, Vector2f lineDirection, short leftIndex, short rightIndex, float width, List<Vector2f> verts, List<Short> indices) {
        lineDirection.scale(0.5f * width);
        VectorUtils.rotate90(lineDirection);
        Vector2f l = Vector2f.add(endPoint, lineDirection, null);
        VectorUtils.rotate90(lineDirection);
        Vector2f l2 = Vector2f.add(l, lineDirection, l);
        VectorUtils.rotate90(lineDirection);
        lineDirection.scale(2.0f);
        Vector2f r = Vector2f.add(l2, lineDirection, null);
        Short li = new Short((short) verts.size());
        verts.add(l2);
        Short ri = new Short((short) verts.size());
        verts.add(r);
        Short eli = new Short(leftIndex);
        Short eri = new Short(rightIndex);
        indices.add(eli);
        indices.add(li);
        indices.add(eri);
        indices.add(eri);
        indices.add(li);
        indices.add(ri);
    }
}
