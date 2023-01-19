package com.solverlabs.droid.rugl.util.geom;


public class TriangleUtils {
    private static final Vector3f v = new Vector3f();
    private static final Vector3f w = new Vector3f();
    private static final Vector3f cross = new Vector3f();

    public static boolean contains(float x, float y, float ax, float ay, float bx, float by, float cx, float cy) {
        int cc1 = LineUtils.relativeCCW(ax, ay, bx, by, x, y);
        int cc2 = LineUtils.relativeCCW(bx, by, cx, cy, x, y);
        if (cc1 == cc2) {
            int cc3 = LineUtils.relativeCCW(cx, cy, ax, ay, x, y);
            return cc1 == cc3;
        }
        return false;
    }

    public static float area(float ax, float ay, float az, float bx, float by, float bz, float cx, float cy, float cz) {
        v.set(bx - ax, by - ay, bz - az);
        w.set(cx - ax, cy - ay, cz - az);
        Vector3f.cross(v, w, cross);
        return cross.length() * 0.5f;
    }
}
