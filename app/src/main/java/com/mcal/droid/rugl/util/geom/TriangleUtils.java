package com.mcal.droid.rugl.util.geom;

public class TriangleUtils {
    private static final Vector3f v = new Vector3f();
    private static final Vector3f w = new Vector3f();
    private static final Vector3f cross = new Vector3f();

    /**
     * Point-in-triangle test
     *
     * @param x
     * @param y
     * @param ax
     * @param ay
     * @param bx
     * @param by
     * @param cx
     * @param cy
     * @return <code>true</code> if (x,y) lies within the a-b-c
     * triangle, <code>false</code> otherwise
     */
    public static boolean contains(float x, float y, float ax, float ay, float bx, float by,
                                   float cx, float cy) {
        int cc1 = LineUtils.relativeCCW(ax, ay, bx, by, x, y);
        int cc2 = LineUtils.relativeCCW(bx, by, cx, cy, x, y);

        if (cc1 == cc2) {
            int cc3 = LineUtils.relativeCCW(cx, cy, ax, ay, x, y);
            return cc1 == cc3;
        }

        return false;
    }

    /**
     * @param ax
     * @param ay
     * @param az
     * @param bx
     * @param by
     * @param bz
     * @param cx
     * @param cy
     * @param cz
     * @return The area of triangle a-b-c
     */
    public static float area(float ax, float ay, float az, float bx, float by, float bz, float cx,
                             float cy, float cz) {
        v.set(bx - ax, by - ay, bz - az);
        w.set(cx - ax, cy - ay, cz - az);
        Vector3f.cross(v, w, cross);

        return cross.length() * 0.5f;
    }
}
