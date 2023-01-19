package com.solverlabs.droid.rugl.util.geom;

import com.solverlabs.droid.rugl.geom.Shape;

import java.util.Arrays;


public class Tesselator {
    public static Shape tesselate(float... verts) {
        Vector3f[] v = new Vector3f[verts.length / 2];
        int vi = 0;
        int i = 0;
        while (i < verts.length) {
            v[vi] = new Vector3f(verts[i], verts[i + 1], 0.0f);
            i += 2;
            vi++;
        }
        Vector3f[] vList = buildCounterList(v);
        boolean[] used = new boolean[vList.length];
        Arrays.fill(used, false);
        short[] tris = new short[(vList.length - 2) * 3];
        int ti = 0;
        short previous = 0;
        short current = 1;
        short next = 2;
        while (ti < tris.length - 1) {
            boolean isEar = isConcave(vList[previous], vList[current], vList[next]);
            for (int i2 = 0; i2 < vList.length && isEar; i2++) {
                if (!used[i2] && i2 != previous && i2 != current && i2 != next) {
                    isEar &= !contains(vList[previous], vList[current], vList[next], vList[i2]);
                }
            }
            if (isEar) {
                int ti2 = ti + 1;
                tris[ti] = previous;
                int ti3 = ti2 + 1;
                tris[ti2] = current;
                tris[ti3] = next;
                used[current] = true;
                current = next;
                next = next(used, next);
                ti = ti3 + 1;
            } else {
                previous = current;
                current = next;
                next = next(used, next);
            }
        }
        float[] vertexArray = new float[vList.length * 3];
        for (int i3 = 0; i3 < vList.length; i3++) {
            vertexArray[i3 * 3] = vList[i3].x;
            vertexArray[(i3 * 3) + 1] = vList[i3].y;
            vertexArray[(i3 * 3) + 2] = vList[i3].z;
        }
        return new Shape(vertexArray, tris);
    }

    private static short next(boolean[] used, short index) {
        do {
            index = (short) ((index + 1) % used.length);
        } while (used[index]);
        return index;
    }

    private static boolean contains(Vector3f a, Vector3f b, Vector3f c, Vector3f p) {
        return LineUtils.relativeCCW(a.x, a.y, b.x, b.y, p.x, p.y) == -1 && LineUtils.relativeCCW(b.x, b.y, c.x, c.y, p.x, p.y) == -1 && LineUtils.relativeCCW(c.x, c.y, a.x, a.y, p.x, p.y) == -1;
    }

    private static boolean isConcave(Vector3f p, Vector3f c, Vector3f n) {
        int ccw = LineUtils.relativeCCW(p.x, p.y, c.x, c.y, n.x, n.y);
        return ccw <= 0;
    }

    private static Vector3f[] buildCounterList(Vector3f[] v) {
        Vector3f[] vList = new Vector3f[v.length];
        boolean counter = traverseOrder(v);
        if (counter) {
            for (int i = 0; i < v.length; i++) {
                vList[i] = v[i];
            }
        } else {
            int i2 = v.length - 1;
            int li = 0;
            while (i2 >= 0) {
                vList[li] = v[i2];
                i2--;
                li++;
            }
        }
        return vList;
    }

    private static boolean traverseOrder(Vector3f[] v) {
        float area = (v[v.length - 1].x * v[0].y) - (v[0].x * v[v.length - 1].y);
        for (int i = 0; i < v.length - 1; i++) {
            area += (v[i].x * v[i + 1].y) - (v[i + 1].x * v[i].y);
        }
        return ((double) area) >= 0.0d;
    }
}
