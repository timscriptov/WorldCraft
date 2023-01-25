package com.mcal.droid.rugl.util.geom;

import androidx.annotation.NonNull;

import com.mcal.droid.rugl.geom.Shape;

import org.jetbrains.annotations.Contract;

import java.util.Arrays;

/**
 * Builds shapes with a simple ear-pruning technique
 */
public class Tesselator {
    /**
     * Builds a shape from a sequence of points. Note that the polygon described
     * by the points must not be self-intersecting
     *
     * @param verts The vertices of the shape, in x1,y1,x2,y2 order
     * @return the {@link Shape} of the polygon, with a z-coordinate of zero
     */
    @NonNull
    @Contract("_ -> new")
    public static Shape tesselate(@NonNull final float... verts) {
        // slightly more convenient to have the points as vector objects
        // rejigger to work directly on the input float array if
        // performance is a concern
        Vector3f[] v = new Vector3f[verts.length / 2];
        int vi = 0;
        for (int i = 0; i < verts.length; i += 2) {
            v[vi++] = new Vector3f(verts[i], verts[i + 1], 0);
        }

        // make sure the vertices are in anti-clockwise order - needed
        // for vertex concavity test and to produce front-facing
        // triangles
        final Vector3f[] vList = buildCounterList(v);

        // set to true when a vertex has been clipped from the shape
        final boolean[] used = new boolean[vList.length];
        Arrays.fill(used, false);

        v = null;

        // we know beforehand how many triangles there are
        final short[] tris = new short[(vList.length - 2) * 3];
        int ti = 0;

        short previous = 0;
        short current = 1;
        short next = 2;

        while (ti < tris.length - 1) {
            // if the internal angle is acute...
            boolean isEar =
                    isConcave(vList[previous], vList[current], vList[next]);

            // and the triangle does not contain any other vertices...
            for (int i = 0; i < vList.length && isEar; i++) {
                if (!used[i] && i != previous && i != current && i != next) {
                    isEar &=
                            !contains(vList[previous], vList[current],
                                    vList[next], vList[i]);
                }
            }

            if (isEar) { // we have found an ear!
                // record the vertex indices
                tris[ti++] = previous;
                tris[ti++] = current;
                tris[ti++] = next;

                // snip!
                used[current] = true;

                // consider the next vertex
                current = next;
                next = next(used, next);
            } else { // consider the next vertex
                previous = current;
                current = next;
                next = next(used, next);
            }
        }

        // extract vertex coordinates into an array
        final float[] vertexArray = new float[3 * vList.length];
        for (int i = 0; i < vList.length; i++) {
            vertexArray[3 * i] = vList[i].x;
            vertexArray[3 * i + 1] = vList[i].y;
            vertexArray[3 * i + 2] = vList[i].z;
        }

        return new Shape(vertexArray, tris);
    }

    @Contract(pure = true)
    private static short next(@NonNull final boolean[] used, short index) {
        do {
            index = (short) ((index + 1) % used.length);
        }
        while (used[index]);

        return index;
    }

    /**
     * Relies on abc being in anti-clockwise order
     *
     * @param a
     * @param b
     * @param c
     * @param p
     * @return <code>true</code> if the triangle abc contains the point p
     */
    private static boolean contains(@NonNull final Vector3f a, @NonNull final Vector3f b,
                                    final Vector3f c, @NonNull final Vector3f p) {
        if (LineUtils.relativeCCW(a.x, a.y, b.x, b.y, p.x, p.y) == -1) {
            if (LineUtils.relativeCCW(b.x, b.y, c.x, c.y, p.x, p.y) == -1) {
                if (LineUtils.relativeCCW(c.x, c.y, a.x, a.y, p.x, p.y) == -1) {
                    return true;
                }
            }
        }
        return false;
    }

    @Contract(pure = true)
    private static boolean isConcave(@NonNull final Vector3f p, @NonNull final Vector3f c,
                                     @NonNull final Vector3f n) {
        final int ccw = LineUtils.relativeCCW(p.x, p.y, c.x, c.y, n.x, n.y);

        return ccw <= 0;
    }

    @NonNull
    @Contract(pure = true)
    private static Vector3f[] buildCounterList(@NonNull final Vector3f[] v) {
        final Vector3f[] vList = new Vector3f[v.length];

        // determine winding order
        final boolean counter = traverseOrder(v);

        if (counter) {
            for (int i = 0; i < v.length; i++) {
                vList[i] = v[i];
            }
        } else {
            int li = 0;
            for (int i = v.length - 1; i >= 0; i--) {
                vList[li++] = v[i];
            }
        }
        return vList;
    }

    /**
     * @param v
     * @return true if vertices are anti-clockwise
     */
    @Contract(pure = true)
    private static boolean traverseOrder(@NonNull final Vector3f[] v) {
        float area =
                v[v.length - 1].x * v[0].y - v[0].x * v[v.length - 1].y;

        for (int i = 0; i < v.length - 1; i++) {
            area += v[i].x * v[i + 1].y - v[i + 1].x * v[i].y;
        }

        return area >= 0.0;
    }
}
