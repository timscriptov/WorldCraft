package com.mcal.droid.rugl.geom.line;

import androidx.annotation.NonNull;

import com.mcal.droid.rugl.util.geom.LineUtils;
import com.mcal.droid.rugl.util.geom.Vector2f;

import java.util.List;


public class BevelJoin implements LineJoin {
    @Override
    public void createVerts(Vector2f v1, Vector2f join, Vector2f v2, Vector2f corner, @NonNull List<Vector2f> verts, List<Short> indices) {
        Vector2f e1 = Vector2f.sub(v1, join, null);
        e1.scale(0.5f);
        Vector2f.add(corner, e1, e1);
        Vector2f e2 = Vector2f.sub(v2, join, null);
        e2.scale(0.5f);
        Vector2f.add(corner, e2, e2);
        Short i1 = (short) (verts.size() - 3);
        Short ij = (short) (verts.size() - 2);
        Short i2 = (short) (verts.size() - 1);
        Short b1 = (short) verts.size();
        verts.add(e1);
        Short b2 = (short) verts.size();
        verts.add(e2);
        int ccw = LineUtils.relativeCCW(v1, join, v2);
        Line.addTriangle(ij, i1, b1, ccw, indices);
        Line.addTriangle(ij, b1, b2, ccw, indices);
        Line.addTriangle(ij, b2, i2, ccw, indices);
    }
}
