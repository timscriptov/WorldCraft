package com.solverlabs.droid.rugl.geom.line;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.util.geom.LineUtils;
import com.solverlabs.droid.rugl.util.geom.Vector2f;
import com.solverlabs.droid.rugl.util.geom.VectorUtils;

import java.util.List;


public class MiterDecoration implements LineJoin, LineCap {
    static final boolean assertionsDisabled = !MiterDecoration.class.desiredAssertionStatus();
    public float bevelLimit = 0.0f;
    public float capLength = 1.0f;

    private static Vector2f getFarthestIntersection(Vector2f lp1, Vector2f lp2, Vector2f circle, float radius, Vector2f pt) {
        Vector2f[] intersections = LineUtils.lineCircleIntersection(lp1, lp2, circle, radius);
        if (assertionsDisabled || intersections.length > 0) {
            Vector2f i = LineUtils.closestPointOnLine(circle, lp1, lp2);
            float md = VectorUtils.distanceSquared(i, pt);
            for (int j = 0; j < intersections.length; j++) {
                float d = VectorUtils.distanceSquared(intersections[j], pt);
                if (d > md) {
                    md = d;
                    i = intersections[j];
                }
            }
            return i;
        }
        throw new AssertionError();
    }

    @Override
    public void createVerts(Vector2f v1, Vector2f join, Vector2f v2, Vector2f corner, List<Vector2f> verts, List<Short> indices) {
        Vector2f e1 = Vector2f.sub(v1, join, null);
        float bl = Math.max(this.bevelLimit, e1.length());
        VectorUtils.rotate90(e1);
        Vector2f.add(v1, e1, e1);
        Vector2f e2 = Vector2f.sub(v2, join, null);
        VectorUtils.rotate90(e2);
        Vector2f.add(v2, e2, e2);
        Vector2f intersection = LineUtils.lineIntersection(v1, e1, v2, e2, null);
        int ccw = LineUtils.relativeCCW(v1, join, v2);
        if (!assertionsDisabled && intersection == null) {
            throw new AssertionError();
        }
        Short i1 = (short) (verts.size() - 3);
        Short ij = (short) (verts.size() - 2);
        Short i2 = (short) (verts.size() - 1);
        if (VectorUtils.distanceSquared(corner, intersection) > bl * bl) {
            Vector2f b1 = getFarthestIntersection(v1, e1, corner, bl, join);
            Vector2f b2 = getFarthestIntersection(v2, e2, corner, bl, join);
            Short bi1 = (short) verts.size();
            verts.add(b1);
            Short bi2 = (short) verts.size();
            verts.add(b2);
            Line.addTriangle(ij, i1, bi1, ccw, indices);
            Line.addTriangle(ij, bi1, bi2, ccw, indices);
            Line.addTriangle(ij, bi2, i2, ccw, indices);
            return;
        }
        Short ii = (short) verts.size();
        verts.add(intersection);
        Line.addTriangle(ij, i1, ii, ccw, indices);
        Line.addTriangle(ii, i2, ij, ccw, indices);
    }

    @Override
    public void createVerts(Vector2f endPoint, @NonNull Vector2f lineDirection, short leftIndex, short rightIndex, float width, List<Vector2f> verts, List<Short> indices) {
        float bl = Math.max(this.bevelLimit, width);
        float pl = 0.5f * width * this.capLength;
        lineDirection.scale(-pl);
        Vector2f p = new Vector2f();
        Vector2f.add(endPoint, lineDirection, p);
        if (pl < bl) {
            verts.add(p);
            indices.add(rightIndex);
            indices.add(leftIndex);
            indices.add((short) (verts.size() - 1));
            return;
        }
        Vector2f li = LineUtils.segmentCircleIntersection(verts.get(leftIndex), p, endPoint, bl)[0];
        Vector2f ri = LineUtils.segmentCircleIntersection(verts.get(rightIndex), p, endPoint, bl)[0];
        int lii = verts.size();
        verts.add(li);
        int rii = verts.size();
        verts.add(ri);
        indices.add(leftIndex);
        indices.add((short) lii);
        indices.add((short) rii);
        indices.add(leftIndex);
        indices.add((short) rii);
        indices.add(rightIndex);
    }
}
