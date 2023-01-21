package com.solverlabs.droid.rugl.util.geom;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.Contract;

public class LineUtils {
    static final boolean assertionsDisabled = !LineUtils.class.desiredAssertionStatus();
    private static final float SMALL_NUM = 1.0E-5f;


    @Nullable
    public static Vector2f lineIntersection(@NonNull ReadableVector2f p0, @NonNull ReadableVector2f p1, @NonNull ReadableVector2f p2, @NonNull ReadableVector2f p3, Vector2f result) {
        float dx1 = p1.getX() - p0.getX();
        float dy1 = p1.getY() - p0.getY();
        float m1 = dy1 / dx1;
        float dx2 = p3.getX() - p2.getX();
        float dy2 = p3.getY() - p2.getY();
        float m2 = dy2 / dx2;
        if (m1 == m2) {
            return null;
        }
        if (result == null) {
            result = new Vector2f();
        }
        if (dx1 == 0.0f) {
            float c2 = p2.getY() - (p2.getX() * m2);
            result.x = p1.getX();
            result.y = (p1.getX() * m2) + c2;
            return result;
        } else if (dx2 == 0.0f) {
            float c1 = p0.getY() - (p0.getX() * m1);
            result.x = p3.getX();
            result.y = (p3.getX() * m1) + c1;
            return result;
        } else {
            float c12 = p0.getY() - (p0.getX() * m1);
            float c22 = p2.getY() - (p2.getX() * m2);
            result.x = (c12 - c22) / (m2 - m1);
            result.y = (((c22 - c12) * m1) / (m1 - m2)) + c12;
            return result;
        }
    }

    @NonNull
    @Contract("_, _, _, _ -> new")
    public static Vector2f[] lineCircleIntersection(@NonNull Vector2f l1, @NonNull Vector2f l2, @NonNull Vector2f p, float cr) {
        float dx = l2.x - l1.x;
        float dy = l2.y - l1.y;
        float a = (dx * dx) + (dy * dy);
        float b = 2.0f * (((l1.x - p.x) * dx) + ((l1.y - p.y) * dy));
        float c = (((((p.x * p.x) + (p.y * p.y)) + (l1.x * l1.x)) + (l1.y * l1.y)) - (2.0f * ((p.x * l1.x) + (p.y * l1.y)))) - (cr * cr);
        float det = (b * b) - ((4.0f * a) * c);
        if (det < 0.0f) {
            return new Vector2f[0];
        }
        if (det == 0.0f) {
            float u = (-b) / (2.0f * a);
            return new Vector2f[]{new Vector2f(l1.x + (u * dx), l1.y + (u * dy))};
        }
        float u2 = (float) (((-b) + Math.sqrt(det)) / (2.0f * a));
        float v = (float) (((-b) - Math.sqrt(det)) / (2.0f * a));
        return new Vector2f[]{new Vector2f(l1.x + (u2 * dx), l1.y + (u2 * dy)), new Vector2f(l1.x + (v * dx), l1.y + (v * dy))};
    }

    @NonNull
    public static Vector2f[] segmentCircleIntersection(@NonNull Vector2f l1, @NonNull Vector2f l2, @NonNull Vector2f p, float cr) {
        float dx = l2.x - l1.x;
        float dy = l2.y - l1.y;
        float a = (dx * dx) + (dy * dy);
        float b = 2.0f * (((l1.x - p.x) * dx) + ((l1.y - p.y) * dy));
        float c = (((((p.x * p.x) + (p.y * p.y)) + (l1.x * l1.x)) + (l1.y * l1.y)) - (2.0f * ((p.x * l1.x) + (p.y * l1.y)))) - (cr * cr);
        float det = (b * b) - ((4.0f * a) * c);
        Vector2f[] result = new Vector2f[0];
        if (det >= 0.0f) {
            if (det == 0.0f) {
                float u = (-b) / (2.0f * a);
                return (u < 0.0f || u > 1.0f) ? result : new Vector2f[]{new Vector2f(l1.x + (u * dx), l1.y + (u * dy))};
            }
            float u2 = (float) (((-b) + Math.sqrt(det)) / (2.0f * a));
            float v = (float) (((-b) - Math.sqrt(det)) / (2.0f * a));
            if (u2 < 0.0f || u2 > 1.0f || v < 0.0f || v > 1.0f) {
                if (u2 < 0.0f || u2 > 1.0f) {
                    if (v < 0.0f || v > 1.0f) {
                        if (assertionsDisabled) {
                            return result;
                        }
                        throw new AssertionError();
                    }
                    return new Vector2f[]{new Vector2f(l1.x + (v * dx), l1.y + (v * dy))};
                }
                return new Vector2f[]{new Vector2f(l1.x + (u2 * dx), l1.y + (u2 * dy))};
            }
            return new Vector2f[]{new Vector2f(l1.x + (u2 * dx), l1.y + (u2 * dy)), new Vector2f(l1.x + (v * dx), l1.y + (v * dy))};
        }
        return result;
    }

    @NonNull
    public static Vector3f[] lineIntersection(Vector3f p1, Vector3f p2, Vector3f p3, Vector3f p4) {
        float sc;
        float tc;
        Vector3f u = Vector3f.sub(p2, p1, null);
        Vector3f v = Vector3f.sub(p4, p3, null);
        Vector3f w = Vector3f.sub(p1, p3, null);
        float a = Vector3f.dot(u, u);
        float b = Vector3f.dot(u, v);
        float c = Vector3f.dot(v, v);
        float d = Vector3f.dot(u, w);
        float e = Vector3f.dot(v, w);
        float D = (a * c) - (b * b);
        if (D < SMALL_NUM) {
            sc = 0.0f;
            tc = b > c ? d / b : e / c;
        } else {
            sc = ((b * e) - (c * d)) / D;
            tc = ((a * e) - (b * d)) / D;
        }
        u.scale(sc);
        v.scale(tc);
        Vector3f[] intersection = {new Vector3f(p1), new Vector3f(p3)};
        Vector3f.add(intersection[0], u, intersection[0]);
        Vector3f.add(intersection[1], v, intersection[1]);
        return intersection;
    }

    public static boolean segmentsIntersect(Vector2f p, Vector2f q, Vector2f r, Vector2f s) {
        int c1 = relativeCCW(p, q, r);
        int c2 = relativeCCW(p, q, s);
        int c3 = relativeCCW(r, s, p);
        int c4 = relativeCCW(r, s, q);
        return c1 * c2 == -1 && c3 * c4 == -1;
    }

    @NonNull
    public static Vector3f[] segmentIntersection(Vector3f p1, Vector3f p2, Vector3f p3, Vector3f p4) {
        float sN;
        float tN;
        Vector3f u = Vector3f.sub(p2, p1, null);
        Vector3f v = Vector3f.sub(p4, p3, null);
        Vector3f w = Vector3f.sub(p1, p3, null);
        float a = Vector3f.dot(u, u);
        float b = Vector3f.dot(u, v);
        float c = Vector3f.dot(v, v);
        float d = Vector3f.dot(u, w);
        float e = Vector3f.dot(v, w);
        float D = (a * c) - (b * b);
        float sD = D;
        float tD = D;
        if (D < SMALL_NUM) {
            sN = 0.0f;
            sD = 1.0f;
            tN = e;
            tD = c;
        } else {
            sN = (b * e) - (c * d);
            tN = (a * e) - (b * d);
            if (sN < 0.0d) {
                sN = 0.0f;
                tN = e;
                tD = c;
            } else if (sN > sD) {
                sN = sD;
                tN = e + b;
                tD = c;
            }
        }
        if (tN < 0.0d) {
            tN = 0.0f;
            if ((-d) < 0.0d) {
                sN = 0.0f;
            } else if ((-d) > a) {
                sN = sD;
            } else {
                sN = -d;
                sD = a;
            }
        } else if (tN > tD) {
            tN = tD;
            if ((-d) + b < 0.0d) {
                sN = 0.0f;
            } else if ((-d) + b > a) {
                sN = sD;
            } else {
                sN = (-d) + b;
                sD = a;
            }
        }
        float sc = Math.abs(sN) < SMALL_NUM ? 0.0f : sN / sD;
        float tc = Math.abs(tN) < SMALL_NUM ? 0.0f : tN / tD;
        u.scale(sc);
        v.scale(tc);
        Vector3f[] intersection = {new Vector3f(p1), new Vector3f(p3)};
        Vector3f.add(intersection[0], u, intersection[0]);
        Vector3f.add(intersection[1], v, intersection[1]);
        return intersection;
    }

    public static Vector3f closestPointOnSegment(Vector3f point, Vector3f segStart, Vector3f segEnd) {
        Vector3f v = Vector3f.sub(segEnd, segStart, null);
        Vector3f w = Vector3f.sub(point, segStart, null);
        double c1 = Vector3f.dot(w, v);
        double c2 = Vector3f.dot(v, v);
        if (c1 > 0.0d) {
            if (c2 <= c1) {
                return segEnd;
            }
            double b = c1 / c2;
            v.scale((float) b);
            Vector3f Pb = new Vector3f(segStart);
            Vector3f.add(Pb, v, Pb);
            return Pb;
        }
        return segStart;
    }

    @NonNull
    public static Vector2f closestPointOnLine(Vector2f point, Vector2f p1, Vector2f p2) {
        Vector2f v = Vector2f.sub(p2, p1, null);
        Vector2f w = Vector2f.sub(point, p1, null);
        double c1 = Vector2f.dot(w, v);
        double c2 = Vector2f.dot(v, v);
        double b = c1 / c2;
        v.scale((float) b);
        Vector2f Pb = new Vector2f(p1);
        Vector2f.add(Pb, v, Pb);
        return Pb;
    }

    @Contract(pure = true)
    public static int relativeCCW(@NonNull Vector2f lp1, @NonNull Vector2f lp2, @NonNull Vector2f point) {
        return relativeCCW(lp1.x, lp1.y, lp2.x, lp2.y, point.x, point.y);
    }

    public static int relativeCCW(float ax, float ay, float bx, float by, float px, float py) {
        float ccw = ((px - ax) * (by - ay)) - ((py - ay) * (bx - ax));
        if (ccw < 0.0d) {
            return -1;
        }
        return ((double) ccw) > 0.0d ? 1 : 0;
    }
}
