/*
 * Copyright (c) 2007, Ryan McNally All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. Neither the name of the <ORGANIZATION> nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */

package com.solverlabs.droid.rugl.util.geom;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.Contract;

/**
 * Methods for working with lines and line segments
 */
public class LineUtils {
    private static final float SMALL_NUM = 0.00001f;

    /**
     * Computes the intersection point of two lines
     *
     * @param p0     A point on the first line
     * @param p1     A point on the first line
     * @param p2     A point on the first line
     * @param p3     A point on the second line
     * @param result The {@link Vector2f} in which to store the result, or
     *               null to create a new {@link Vector2f}
     * @return The intersection point of the two lines, or null if the
     * lines are parallel
     */
    @Nullable
    public static Vector2f lineIntersection(@NonNull ReadableVector2f p0, @NonNull ReadableVector2f p1,
                                            @NonNull ReadableVector2f p2, @NonNull ReadableVector2f p3, Vector2f result) {
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

        if (dx1 == 0) {
            float c2 = p2.getY() - m2 * p2.getX();
            result.x = p1.getX();
            result.y = m2 * p1.getX() + c2;

            return result;
        }

        if (dx2 == 0) {
            float c1 = p0.getY() - m1 * p0.getX();
            result.x = p3.getX();
            result.y = m1 * p3.getX() + c1;

            return result;
        }

        float c1 = p0.getY() - m1 * p0.getX();
        float c2 = p2.getY() - m2 * p2.getX();

        result.x = (c1 - c2) / (m2 - m1);
        result.y = m1 * (c2 - c1) / (m1 - m2) + c1;

        return result;
    }

    /**
     * Computes the point(s) of intersection between a line and a
     * circle
     *
     * @param l1 A point on the line
     * @param l2 A different point on the line
     * @param p  The center of the circle
     * @param cr The radius of the circle
     * @return An array of up to two elements, holding the points of
     * intersection, if any
     */
    @NonNull
    public static Vector2f[] lineCircleIntersection(@NonNull Vector2f l1, @NonNull Vector2f l2, @NonNull Vector2f p, float cr) {
        float dx = l2.x - l1.x;
        float dy = l2.y - l1.y;

        float a = dx * dx + dy * dy;
        float b = 2 * (dx * (l1.x - p.x) + dy * (l1.y - p.y));
        float c =
                p.x * p.x + p.y * p.y + l1.x * l1.x + l1.y * l1.y - 2 * (p.x * l1.x + p.y * l1.y)
                        - cr * cr;

        float det = b * b - 4 * a * c;

        if (det < 0) {
            return new Vector2f[0];
        } else if (det == 0) {
            float u = -b / (2 * a);

            return new Vector2f[]{new Vector2f(l1.x + u * dx, l1.y + u * dy)};
        } else {
            float u = (float) ((-b + Math.sqrt(det)) / (2 * a));
            float v = (float) ((-b - Math.sqrt(det)) / (2 * a));

            return new Vector2f[]{new Vector2f(l1.x + u * dx, l1.y + u * dy),
                    new Vector2f(l1.x + v * dx, l1.y + v * dy)};
        }
    }

    /**
     * Finds the points of intersection between a line segment and a
     * circle
     *
     * @param l1 An endpoint of the segment
     * @param l2 The other endpoint of the segment
     * @param p  The center of the circle
     * @param cr The radius of the circle
     * @return An array containing the zero, one or two points of
     * intersection between the line segment and the circle
     */
    public static Vector2f[] segmentCircleIntersection(@NonNull Vector2f l1, @NonNull Vector2f l2, @NonNull Vector2f p,
                                                       float cr) {
        float dx = l2.x - l1.x;
        float dy = l2.y - l1.y;

        float a = dx * dx + dy * dy;
        float b = 2 * (dx * (l1.x - p.x) + dy * (l1.y - p.y));
        float c =
                p.x * p.x + p.y * p.y + l1.x * l1.x + l1.y * l1.y - 2 * (p.x * l1.x + p.y * l1.y)
                        - cr * cr;

        float det = b * b - 4 * a * c;

        Vector2f[] result = new Vector2f[0];
        if (det < 0) {
            // no intersection
        } else if (det == 0) {
            float u = -b / (2 * a);

            if (u >= 0 && u <= 1) {
                result = new Vector2f[]{new Vector2f(l1.x + u * dx, l1.y + u * dy)};
            }
        } else {
            float u = (float) ((-b + Math.sqrt(det)) / (2 * a));
            float v = (float) ((-b - Math.sqrt(det)) / (2 * a));

            if (u >= 0 && u <= 1 && v >= 0 && v <= 1) {
                result =
                        new Vector2f[]{new Vector2f(l1.x + u * dx, l1.y + u * dy),
                                new Vector2f(l1.x + v * dx, l1.y + v * dy)};
            } else if (u >= 0 && u <= 1) {
                result = new Vector2f[]{new Vector2f(l1.x + u * dx, l1.y + u * dy)};
            } else if (v >= 0 && v <= 1) {
                result = new Vector2f[]{new Vector2f(l1.x + v * dx, l1.y + v * dy)};
            } else {
                assert false;
            }
        }

        return result;
    }

    /**
     * Computes the shortest line segment that connects the two input
     * lines
     *
     * @param p1 A point on the first line
     * @param p2 Another point on the first line
     * @param p3 A point on the second line
     * @param p4 Another point on the second line
     * @return The endpoints of the shortest segment that joins the two
     * input lines
     */
    @NonNull
    public static Vector3f[] lineIntersection(Vector3f p1, Vector3f p2, Vector3f p3, Vector3f p4) {
        Vector3f u = Vector3f.sub(p2, p1, null);
        Vector3f v = Vector3f.sub(p4, p3, null);
        Vector3f w = Vector3f.sub(p1, p3, null);

        float a = Vector3f.dot(u, u); // always >= 0
        float b = Vector3f.dot(u, v);
        float c = Vector3f.dot(v, v); // always >= 0
        float d = Vector3f.dot(u, w);
        float e = Vector3f.dot(v, w);
        float D = a * c - b * b; // always >= 0
        float sc, tc;

        // compute the line parameters of the two closest points
        if (D < SMALL_NUM) { // the lines are almost parallel
            sc = 0.0f;
            tc = b > c ? d / b : e / c; // use the largest
            // denominator
        } else {
            sc = (b * e - c * d) / D;
            tc = (a * e - b * d) / D;
        }

        Vector3f[] intersection = new Vector3f[2];

        u.scale(sc);
        v.scale(tc);

        intersection[0] = new Vector3f(p1);
        Vector3f.add(intersection[0], u, intersection[0]);

        intersection[1] = new Vector3f(p3);
        Vector3f.add(intersection[1], v, intersection[1]);

        return intersection;
    }

    /**
     * Determines if two line segments intersect
     *
     * @param p
     * @param q
     * @param r
     * @param s
     * @return <code>true</code> if the segments p-q and r-s intersect
     */
    public static boolean segmentsIntersect(Vector2f p, Vector2f q, Vector2f r, Vector2f s) {
        int c1 = relativeCCW(p, q, r);
        int c2 = relativeCCW(p, q, s);
        int c3 = relativeCCW(r, s, p);
        int c4 = relativeCCW(r, s, q);

        return c1 * c2 == -1 && c3 * c4 == -1;
    }

    /**
     * Computes the shortest line segment that connects the two input
     * segments
     *
     * @param p1 The start of the first segment
     * @param p2 The end of the first segment
     * @param p3 The start of the second segment
     * @param p4 The end of the second segment
     * @return The two endpoints of the shortest segment that joins the
     * two input segments
     */
    @NonNull
    public static Vector3f[] segmentIntersection(Vector3f p1, Vector3f p2, Vector3f p3, Vector3f p4) {
        Vector3f u = Vector3f.sub(p2, p1, null);
        Vector3f v = Vector3f.sub(p4, p3, null);
        Vector3f w = Vector3f.sub(p1, p3, null);

        float a = Vector3f.dot(u, u); // always >= 0
        float b = Vector3f.dot(u, v);
        float c = Vector3f.dot(v, v); // always >= 0
        float d = Vector3f.dot(u, w);
        float e = Vector3f.dot(v, w);
        float D = a * c - b * b; // always >= 0
        float sc, sN, sD = D; // sc = sN / sD, default sD = D >= 0
        float tc, tN, tD = D; // tc = tN / tD, default tD = D >= 0

        // compute the line parameters of the two closest points
        if (D < SMALL_NUM) { // the lines are almost parallel
            sN = 0.0f; // force using point P0 on segment S1
            sD = 1.0f; // to prevent possible division by 0.0 later
            tN = e;
            tD = c;
        } else { // get the closest points on the infinite lines
            sN = b * e - c * d;
            tN = a * e - b * d;
            if (sN < 0.0) { // sc < 0 => the s=0 edge is visible
                sN = 0.0f;
                tN = e;
                tD = c;
            } else if (sN > sD) { // sc > 1 => the s=1 edge is visible
                sN = sD;
                tN = e + b;
                tD = c;
            }
        }

        if (tN < 0.0) { // tc < 0 => the t=0 edge is visible
            tN = 0.0f;
            // recompute sc for this edge
            if (-d < 0.0) {
                sN = 0.0f;
            } else if (-d > a) {
                sN = sD;
            } else {
                sN = -d;
                sD = a;
            }
        } else if (tN > tD) { // tc > 1 => the t=1 edge is visible
            tN = tD;
            // recompute sc for this edge
            if (-d + b < 0.0) {
                sN = 0;
            } else if (-d + b > a) {
                sN = sD;
            } else {
                sN = -d + b;
                sD = a;
            }
        }
        // finally do the division to get sc and tc
        sc = Math.abs(sN) < SMALL_NUM ? 0.0f : sN / sD;
        tc = Math.abs(tN) < SMALL_NUM ? 0.0f : tN / tD;

        Vector3f[] intersection = new Vector3f[2];

        u.scale(sc);
        v.scale(tc);

        intersection[0] = new Vector3f(p1);
        Vector3f.add(intersection[0], u, intersection[0]);

        intersection[1] = new Vector3f(p3);
        Vector3f.add(intersection[1], v, intersection[1]);

        return intersection;
    }

    /**
     * Computes the closest point on a line segment to a point
     *
     * @param point    The point
     * @param segStart The start of the segment
     * @param segEnd   The end of the segement
     * @return The point on the segment that is closer to point than
     * any other
     */
    public static Vector3f closestPointOnSegment(Vector3f point, Vector3f segStart, Vector3f segEnd) {
        Vector3f v = Vector3f.sub(segEnd, segStart, null);
        Vector3f w = Vector3f.sub(point, segStart, null);

        double c1 = Vector3f.dot(w, v);
        double c2 = Vector3f.dot(v, v);

        if (c1 <= 0) {
            return segStart;
        }
        if (c2 <= c1) {
            return segEnd;
        }

        double b = c1 / c2;
        v.scale((float) b);
        Vector3f Pb = new Vector3f(segStart);
        Vector3f.add(Pb, v, Pb);

        return Pb;
    }

    /**
     * Computes the closest point on a line to a point
     *
     * @param point The point
     * @param p1    A point on the line
     * @param p2    A different point on the line
     * @return The point on the line that is closer to point than any
     * other
     */
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

    /**
     * Determines which side of a line a point lies on
     *
     * @param lp1   The first point on the line
     * @param lp2   The second point on the line
     * @param point The point to test
     * @return -1 if the point lies on the left of the line, 1 if the
     * point is on the right, 0 if the point lies on the line
     */
    @Contract(pure = true)
    public static int relativeCCW(@NonNull Vector2f lp1, @NonNull Vector2f lp2, @NonNull Vector2f point) {
        return relativeCCW(lp1.x, lp1.y, lp2.x, lp2.y, point.x, point.y);
    }

    /**
     * return -1 if point is on left, 1 if on right
     *
     * @param ax
     * @param ay
     * @param bx
     * @param by
     * @param px
     * @param py
     * @return -1 if the point is on the left of the line, 1 if the
     * point is on the right of the line, 0 if the point lies
     * on the line
     */
    public static int relativeCCW(float ax, float ay, float bx, float by, float px, float py) {
        bx -= ax;
        by -= ay;
        px -= ax;
        py -= ay;
        float ccw = px * by - py * bx;

        return ccw < 0.0 ? -1 : ccw > 0.0 ? 1 : 0;
    }
}
