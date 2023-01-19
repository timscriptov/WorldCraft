package com.solverlabs.droid.rugl.geom;

import com.solverlabs.droid.rugl.gl.GLUtil;
import com.solverlabs.droid.rugl.gl.State;
import com.solverlabs.droid.rugl.gl.enums.DrawMode;
import com.solverlabs.droid.rugl.util.Trig;
import com.solverlabs.droid.rugl.util.geom.LineUtils;
import com.solverlabs.droid.rugl.util.geom.Vector2f;
import com.solverlabs.droid.rugl.util.geom.Vector3f;
import com.solverlabs.droid.rugl.util.geom.VectorUtils;
import com.solverlabs.worldcraft.math.MathUtils;

import java.util.Arrays;
import java.util.List;


public class ShapeUtil {
    static final /* synthetic */ boolean $assertionsDisabled;
    public static State state;

    static {
        $assertionsDisabled = !ShapeUtil.class.desiredAssertionStatus();
        state = GLUtil.typicalState.with(DrawMode.Triangles);
    }

    public static Shape cuboid(float minx, float miny, float minz, float maxx, float maxy, float maxz) {
        float[] verts = new float[24];
        short[] tris = new short[36];
        int vi = 0 + 1;
        verts[0] = minx;
        int vi2 = vi + 1;
        verts[vi] = miny;
        int vi3 = vi2 + 1;
        verts[vi2] = minz;
        int vi4 = vi3 + 1;
        verts[vi3] = minx;
        int vi5 = vi4 + 1;
        verts[vi4] = maxy;
        int vi6 = vi5 + 1;
        verts[vi5] = minz;
        int vi7 = vi6 + 1;
        verts[vi6] = maxx;
        int vi8 = vi7 + 1;
        verts[vi7] = miny;
        int vi9 = vi8 + 1;
        verts[vi8] = minz;
        int vi10 = vi9 + 1;
        verts[vi9] = maxx;
        int vi11 = vi10 + 1;
        verts[vi10] = maxy;
        int vi12 = vi11 + 1;
        verts[vi11] = minz;
        int vi13 = vi12 + 1;
        verts[vi12] = minx;
        int vi14 = vi13 + 1;
        verts[vi13] = miny;
        int vi15 = vi14 + 1;
        verts[vi14] = maxz;
        int vi16 = vi15 + 1;
        verts[vi15] = minx;
        int vi17 = vi16 + 1;
        verts[vi16] = maxy;
        int vi18 = vi17 + 1;
        verts[vi17] = maxz;
        int vi19 = vi18 + 1;
        verts[vi18] = maxx;
        int vi20 = vi19 + 1;
        verts[vi19] = miny;
        int vi21 = vi20 + 1;
        verts[vi20] = maxz;
        int vi22 = vi21 + 1;
        verts[vi21] = maxx;
        int vi23 = vi22 + 1;
        verts[vi22] = maxy;
        int i = vi23 + 1;
        verts[vi23] = maxz;
        int ti = 0 + 1;
        tris[0] = 0;
        int ti2 = ti + 1;
        tris[ti] = 2;
        int ti3 = ti2 + 1;
        tris[ti2] = 1;
        int ti4 = ti3 + 1;
        tris[ti3] = 2;
        int ti5 = ti4 + 1;
        tris[ti4] = 3;
        int ti6 = ti5 + 1;
        tris[ti5] = 1;
        int ti7 = ti6 + 1;
        tris[ti6] = 0;
        int ti8 = ti7 + 1;
        tris[ti7] = 4;
        int ti9 = ti8 + 1;
        tris[ti8] = 2;
        int ti10 = ti9 + 1;
        tris[ti9] = 4;
        int ti11 = ti10 + 1;
        tris[ti10] = 6;
        int ti12 = ti11 + 1;
        tris[ti11] = 2;
        int ti13 = ti12 + 1;
        tris[ti12] = 2;
        int ti14 = ti13 + 1;
        tris[ti13] = 6;
        int ti15 = ti14 + 1;
        tris[ti14] = 3;
        int ti16 = ti15 + 1;
        tris[ti15] = 7;
        int ti17 = ti16 + 1;
        tris[ti16] = 3;
        int ti18 = ti17 + 1;
        tris[ti17] = 6;
        int ti19 = ti18 + 1;
        tris[ti18] = 1;
        int ti20 = ti19 + 1;
        tris[ti19] = 3;
        int ti21 = ti20 + 1;
        tris[ti20] = 5;
        int ti22 = ti21 + 1;
        tris[ti21] = 3;
        int ti23 = ti22 + 1;
        tris[ti22] = 7;
        int ti24 = ti23 + 1;
        tris[ti23] = 5;
        int ti25 = ti24 + 1;
        tris[ti24] = 4;
        int ti26 = ti25 + 1;
        tris[ti25] = 0;
        int ti27 = ti26 + 1;
        tris[ti26] = 5;
        int ti28 = ti27 + 1;
        tris[ti27] = 0;
        int ti29 = ti28 + 1;
        tris[ti28] = 1;
        int ti30 = ti29 + 1;
        tris[ti29] = 5;
        int ti31 = ti30 + 1;
        tris[ti30] = 6;
        int ti32 = ti31 + 1;
        tris[ti31] = 4;
        int ti33 = ti32 + 1;
        tris[ti32] = 7;
        int ti34 = ti33 + 1;
        tris[ti33] = 4;
        int ti35 = ti34 + 1;
        tris[ti34] = 5;
        int i2 = ti35 + 1;
        tris[ti35] = 7;
        return new Shape(verts, tris);
    }

    public static Shape goldenSpiral(float angle, float angleInc, float width) {
        return logSpiral(1.0f, 0.306349f, angle, angleInc, width);
    }

    public static Shape logSpiral(float a, float b, float tRange, float tStep, float width) {
        if ($assertionsDisabled || tRange != 0.0f) {
            int points = ((int) (tRange / tStep)) + 2;
            float tStep2 = tRange / points;
            float[] verts = new float[points * 2];
            int vi = 0;
            for (int i = 0; i < points; i++) {
                float t = i * tStep2;
                float r = (float) (a * Math.pow(2.718281828459045d, b * t));
                int vi2 = vi + 1;
                verts[vi] = Trig.cos(t) * r;
                vi = vi2 + 1;
                verts[vi2] = Trig.sin(t) * r;
            }
            return line(width, verts);
        }
        throw new AssertionError();
    }

    public static Shape chipIcon(int legs, float legLength, float legwidth, float sep) {
        float core = (1.0f - (2.0f * sep)) - (2.0f * legLength);
        Shape coreShape = filledQuad(0.0f, 0.0f, core, core, 0.0f);
        coreShape.translate(legLength + sep, legLength + sep, 0.0f);
        Shape hLeg = filledQuad(0.0f, (-legwidth) / 2.0f, legLength, legwidth / 2.0f, 0.0f);
        Shape vLeg = filledQuad((-legwidth) / 2.0f, 0.0f, legwidth / 2.0f, legLength, 0.0f);
        ShapeWelder<Shape> b = new ShapeWelder<>();
        b.addShape(coreShape);
        float legSep = core / legs;
        float p = legLength + sep + (legSep / 2.0f);
        for (int i = 0; i < legs; i++) {
            b.addShape(hLeg.clone().translate(0.0f, p, 0.0f));
            b.addShape(hLeg.clone().translate((2.0f * sep) + core + legLength, p, 0.0f));
            b.addShape(vLeg.clone().translate(p, 0.0f, 0.0f));
            b.addShape(vLeg.clone().translate(p, (2.0f * sep) + core + legLength, 0.0f));
            p += legSep;
        }
        return b.mo82fuse();
    }

    public static Shape retArrow(float posx, float posy, float width, float height, float arrowlength, float thickness) {
        float pointy = height / 2.0f;
        float low = pointy - (thickness / 2.0f);
        float high = low + thickness;
        float[] verts = new float[18];
        verts[0] = 0.0f;
        verts[1] = pointy;
        verts[2] = arrowlength;
        verts[3] = height;
        verts[4] = arrowlength;
        verts[5] = high;
        verts[6] = width - thickness;
        verts[7] = high;
        verts[8] = width - thickness;
        verts[9] = height;
        verts[10] = width;
        verts[11] = height;
        verts[12] = width;
        verts[13] = low;
        verts[14] = arrowlength;
        verts[15] = low;
        verts[16] = arrowlength;
        verts[17] = 0.0f;
        for (int i = 0; i < verts.length; i += 2) {
            verts[i] = verts[i] + posx;
            int i2 = i + 1;
            verts[i2] = verts[i2] + posy;
        }
        short[] tris = {0, 2, 1, 0, 7, 2, 0, 8, 7, 7, 6, 2, 2, 6, 3, 6, 4, 3, 6, 5, 4};
        return new Shape(to3D(verts, 0.0f), tris);
    }

    public static Shape filledCircle(float cx, float cy, float radius, float maxSegment, float z) {
        if ($assertionsDisabled || radius >= 0.0f) {
            float c = (float) (2.0f * radius * 3.141592653589793d);
            int segs = Math.max((int) Math.ceil(c / maxSegment), 3);
            float angleIncrement = (float) (6.283185307179586d / segs);
            Vector3f[] verts = new Vector3f[segs];
            short[] tris = new short[(segs - 2) * 3];
            for (int i = 0; i < verts.length; i++) {
                float a = i * angleIncrement;
                verts[i] = new Vector3f(MathUtils.cos(a) * radius, MathUtils.sin(a) * radius, z);
                verts[i].x += cx;
                verts[i].y += cy;
            }
            int ti = 0;
            int i2 = 0;
            while (true) {
                int ti2 = ti;
                if (i2 < segs - 2) {
                    int ti3 = ti2 + 1;
                    tris[ti2] = 0;
                    int ti4 = ti3 + 1;
                    tris[ti3] = (short) (i2 + 1);
                    ti = ti4 + 1;
                    tris[ti4] = (short) (i2 + 2);
                    i2++;
                } else {
                    return new Shape(extract(verts), tris);
                }
            }
        } else {
            throw new AssertionError();
        }
    }

    public static Shape filledCenteredCircle(float cx, float cy, float radius, float maxSegment, float z) {
        if ($assertionsDisabled || radius >= 0.0f) {
            float c = (float) (2.0f * radius * 3.141592653589793d);
            int segs = Math.max((int) Math.ceil(c / maxSegment), 3);
            float angleIncrement = (float) (6.283185307179586d / segs);
            Vector3f[] verts = new Vector3f[segs + 1];
            short[] tris = new short[segs * 3];
            verts[0] = new Vector3f(cx, cy, z);
            for (int i = 1; i < verts.length; i++) {
                float a = i * angleIncrement;
                verts[i] = new Vector3f(MathUtils.cos(a) * radius, MathUtils.sin(a) * radius, z);
                verts[i].x += cx;
                verts[i].y += cy;
            }
            short vi = 1;
            int i2 = 0;
            int ti = 0;
            while (i2 < segs) {
                int ti2 = ti + 1;
                tris[ti] = 0;
                int ti3 = ti2 + 1;
                tris[ti2] = vi;
                vi = (short) (vi + 1);
                tris[ti3] = vi;
                i2++;
                ti = ti3 + 1;
            }
            tris[tris.length - 1] = 1;
            return new Shape(extract(verts), tris);
        }
        throw new AssertionError();
    }

    public static Shape outerCircle(float cx, float cy, float radius, float width, float maxSegment, float z) {
        return innerCircle(cx, cy, radius + width, width, maxSegment, z);
    }

    public static Shape innerCircle(float cx, float cy, float radius, float width, float maxSegment, float z) {
        if (width > radius) {
            width = radius;
        }
        float inner = radius - width;
        if (inner == 0.0f) {
            return filledCircle(cx, cy, radius, maxSegment, z);
        }
        int segments = Math.max(3, (int) Math.ceil((6.283185307179586d * radius) / maxSegment));
        float angleIncrement = (float) (6.283185307179586d / segments);
        Vector3f[] verts = new Vector3f[segments * 2];
        short[] indices = new short[segments * 6];
        for (int i = 0; i < segments; i++) {
            float cos = MathUtils.cos(i * angleIncrement);
            float sin = MathUtils.sin(i * angleIncrement);
            verts[i * 2] = new Vector3f((inner * cos) + cx, (inner * sin) + cy, z);
            verts[(i * 2) + 1] = new Vector3f((radius * cos) + cx, (radius * sin) + cy, z);
            short ci = (short) (i * 2);
            short co = (short) ((i * 2) + 1);
            short ni = (short) ((ci + 2) % verts.length);
            short no = (short) ((co + 2) % verts.length);
            indices[i * 6] = ci;
            indices[(i * 6) + 1] = co;
            indices[(i * 6) + 2] = ni;
            indices[(i * 6) + 3] = co;
            indices[(i * 6) + 4] = no;
            indices[(i * 6) + 5] = ni;
        }
        return new Shape(extract(verts), indices);
    }

    public static Shape cross(float width) {
        float n = 0.5f - (width / 2.0f);
        float f = n + width;
        float[] verts = {n, 0.0f, n, n, 0.0f, n, 0.0f, f, n, f, n, 1.0f, f, 1.0f, f, f, 1.0f, f, 1.0f, n, f, n, f, 0.0f};
        short[] tris = {1, 3, 2, 1, 4, 3, 4, 6, 5, 4, 7, 6, 7, 9, 8, 7, 10, 9, 10, 0, 11, 10, 1, 0, 1, 7, 4, 1, 10, 7};
        return new Shape(to3D(verts, 0.0f), tris);
    }

    public static float[] to3D(float[] verts, float z) {
        float[] tdv = new float[(verts.length / 2) * 3];
        int vi = 0;
        int i = 0;
        while (i < verts.length) {
            int vi2 = vi + 1;
            tdv[vi] = verts[i];
            int vi3 = vi2 + 1;
            tdv[vi2] = verts[i + 1];
            tdv[vi3] = z;
            i += 2;
            vi = vi3 + 1;
        }
        return tdv;
    }

    public static float[] to2D(float[] verts) {
        float[] tdv = new float[(verts.length / 3) * 2];
        int vi = 0;
        for (int i = 0; i < verts.length; i += 3) {
            int vi2 = vi + 1;
            tdv[vi] = verts[i];
            vi = vi2 + 1;
            tdv[vi2] = verts[i + 1];
        }
        return tdv;
    }

    public static Shape arrow(float bx, float by, float tx, float ty, float cx, float cy) {
        float dx = tx - bx;
        float dy = ty - by;
        float ix = dx * cx;
        float iy = dy * cy;
        float near = bx + ix;
        float far = tx - ix;
        float peakx = (bx + tx) / 2.0f;
        float[] verts = {near, by, near, iy, bx, iy, peakx, ty, tx, iy, far, iy, far, bx};
        short[] tris = {1, 3, 2, 1, 5, 3, 5, 4, 3, 0, 5, 1, 0, 6, 5};
        return new Shape(to3D(verts, 0.0f), tris);
    }

    public static Shape filledQuad(float px, float py, float qx, float qy, float z) {
        if (px > qx) {
            qx = px;
            px = qx;
        }
        if (py > qy) {
            qy = py;
            py = qy;
        }
        Vector3f[] verts = {new Vector3f(px, py, z), new Vector3f(px, qy, z), new Vector3f(qx, py, z), new Vector3f(qx, qy, z)};
        return new Shape(extract(verts), makeQuads(4, 0, null, 0));
    }

    public static Shape innerQuad(float px, float py, float qx, float qy, float width, float z) {
        if (px > qx) {
            qx = px;
            px = qx;
        }
        if (py > qy) {
            qy = py;
            py = qy;
        }
        Vector3f[] verts = new Vector3f[8];
        short[] tris = new short[24];
        float xwidth = width;
        float ywidth = width;
        if (Math.abs(qx - px) < 2.0f * width) {
            xwidth = Math.abs(qx - px) / 2.0f;
        }
        if (Math.abs(qy - py) < 2.0f * width) {
            ywidth = Math.abs(qy - py) / 2.0f;
        }
        int index = 0 + 1;
        verts[0] = new Vector3f(px, py, z);
        int index2 = index + 1;
        verts[index] = new Vector3f(px + xwidth, py + ywidth, z);
        int index3 = index2 + 1;
        verts[index2] = new Vector3f(px, qy, z);
        int index4 = index3 + 1;
        verts[index3] = new Vector3f(px + xwidth, qy - ywidth, z);
        int index5 = index4 + 1;
        verts[index4] = new Vector3f(qx, qy, z);
        int index6 = index5 + 1;
        verts[index5] = new Vector3f(qx - xwidth, qy - ywidth, z);
        int index7 = index6 + 1;
        verts[index6] = new Vector3f(qx, py, z);
        int i = index7 + 1;
        verts[index7] = new Vector3f(qx - xwidth, py + ywidth, z);
        int index8 = 0;
        for (short i2 = 0; i2 < 4; i2 = (short) (i2 + 1)) {
            short a = (short) (i2 * 2);
            short b = (short) (a + 1);
            short c = (short) ((b + 1) % verts.length);
            short d = (short) ((c + 1) % verts.length);
            addTriangle(a, b, c, tris, index8);
            int index9 = index8 + 3;
            addTriangle(b, d, c, tris, index9);
            index8 = index9 + 3;
        }
        return new Shape(extract(verts), tris);
    }

    public static Shape outerQuad(float px, float py, float qx, float qy, float width, float z) {
        float minx;
        float maxx;
        float miny;
        float maxy;
        if (px < qx) {
            minx = px;
            maxx = qx;
        } else {
            minx = qx;
            maxx = px;
        }
        if (py < qy) {
            miny = py;
            maxy = qy;
        } else {
            miny = qy;
            maxy = py;
        }
        return innerQuad(minx - width, miny - width, maxx + width, maxy + width, width, z);
    }

    static void addTriangle(short a, short b, short c, short[] array, int index) {
        array[index] = a;
        array[index + 1] = b;
        array[index + 2] = c;
    }

    public static Shape triangle(float ax, float ay, float bx, float by, float cx, float cy) {
        float[] v = {ax, ay, 0.0f, bx, by, 0.0f, cx, cy, 0.0f};
        short[] t = {0, 1, 2};
        return new Shape(v, t);
    }

    public static Shape outline(float width, float pos, float... vin) {
        if ($assertionsDisabled || vin.length >= 6) {
            Vector2f prevDir = new Vector2f();
            Vector2f nextDir = new Vector2f();
            Vector2f point = new Vector2f();
            Vector2f ta = new Vector2f();
            Vector2f tb = new Vector2f();
            Vector2f tc = new Vector2f();
            Vector2f td = new Vector2f();
            float[] vout = new float[vin.length * 2];
            int vi = 0;
            for (int i = 1; i <= vin.length / 2; i++) {
                int c = (i * 2) % vin.length;
                int p = ((i - 1) * 2) % vin.length;
                int n = ((i + 1) * 2) % vin.length;
                prevDir.set(vin[c] - vin[p], vin[c + 1] - vin[p + 1]);
                prevDir.normalise();
                nextDir.set(vin[n] - vin[c], vin[n + 1] - vin[c + 1]);
                nextDir.normalise();
                prevDir.scale(width);
                nextDir.scale(width);
                VectorUtils.rotate90(prevDir);
                VectorUtils.rotate90(nextDir);
                ta.set(vin[p] - (prevDir.x * pos), vin[p + 1] - (prevDir.y * pos));
                tb.set(vin[c] - (prevDir.x * pos), vin[c + 1] - (prevDir.y * pos));
                tc.set(vin[c] - (nextDir.x * pos), vin[c + 1] - (nextDir.y * pos));
                td.set(vin[n] - (nextDir.x * pos), vin[n + 1] - (nextDir.y * pos));
                LineUtils.lineIntersection(ta, tb, tc, td, point);
                int vi2 = vi + 1;
                vout[vi] = point.x;
                int vi3 = vi2 + 1;
                vout[vi2] = point.y;
                ta.set(vin[p] + ((1.0f - pos) * prevDir.x), vin[p + 1] + ((1.0f - pos) * prevDir.y));
                tb.set(vin[c] + ((1.0f - pos) * prevDir.x), vin[c + 1] + ((1.0f - pos) * prevDir.y));
                tc.set(vin[c] + ((1.0f - pos) * nextDir.x), vin[c + 1] + ((1.0f - pos) * nextDir.y));
                td.set(vin[n] + ((1.0f - pos) * nextDir.x), vin[n + 1] + ((1.0f - pos) * nextDir.y));
                LineUtils.lineIntersection(ta, tb, tc, td, point);
                int vi4 = vi3 + 1;
                vout[vi3] = point.x;
                vi = vi4 + 1;
                vout[vi4] = point.y;
            }
            if (!$assertionsDisabled) {
                if (vi != vout.length) {
                    throw new AssertionError(vi + " " + vout.length);
                }
            }
            short[] tris = new short[vin.length * 3];
            int ti = 0;
            for (int i2 = 0; i2 < vin.length / 2; i2++) {
                int ti2 = ti + 1;
                tris[ti] = (short) (i2 * 2);
                int ti3 = ti2 + 1;
                tris[ti2] = (short) ((i2 * 2) + 2);
                int ti4 = ti3 + 1;
                tris[ti3] = (short) ((i2 * 2) + 1);
                int ti5 = ti4 + 1;
                tris[ti4] = (short) ((i2 * 2) + 2);
                int ti6 = ti5 + 1;
                tris[ti5] = (short) ((i2 * 2) + 3);
                ti = ti6 + 1;
                tris[ti6] = (short) ((i2 * 2) + 1);
            }
            for (int i3 = 0; i3 < tris.length; i3++) {
                tris[i3] = (short) (tris[i3] % (vout.length / 2));
            }
            return new Shape(to3D(vout, 0.0f), tris);
        }
        throw new AssertionError();
    }

    public static Shape line(float width, float... vin) {
        int vi;
        if ($assertionsDisabled || vin.length >= 4) {
            Vector2f prevDir = new Vector2f();
            Vector2f nextDir = new Vector2f();
            Vector2f point = new Vector2f();
            Vector2f ta = new Vector2f();
            Vector2f tb = new Vector2f();
            Vector2f tc = new Vector2f();
            Vector2f td = new Vector2f();
            float[] vout = new float[vin.length * 2];
            prevDir.set(vin[2] - vin[0], vin[3] - vin[1]);
            prevDir.normalise();
            prevDir.scale(width);
            VectorUtils.rotate90(prevDir);
            int vi2 = 0 + 1;
            vout[0] = vin[0] - (prevDir.x * 0.5f);
            int vi3 = vi2 + 1;
            vout[vi2] = vin[1] - (prevDir.y * 0.5f);
            int vi4 = vi3 + 1;
            vout[vi3] = vin[0] + ((1.0f - 0.5f) * prevDir.x);
            int vi5 = vi4 + 1;
            vout[vi4] = vin[1] + ((1.0f - 0.5f) * prevDir.y);
            nextDir.set(prevDir);
            for (int i = 1; i < (vin.length / 2) - 1; i++) {
                int p = (i - 1) * 2;
                int c = i * 2;
                int n = (i + 1) * 2;
                prevDir.set(vin[c] - vin[p], vin[c + 1] - vin[p + 1]);
                prevDir.normalise();
                nextDir.set(vin[n] - vin[c], vin[n + 1] - vin[c + 1]);
                nextDir.normalise();
                prevDir.scale(width);
                nextDir.scale(width);
                VectorUtils.rotate90(prevDir);
                VectorUtils.rotate90(nextDir);
                ta.set(vin[p] - (prevDir.x * 0.5f), vin[p + 1] - (prevDir.y * 0.5f));
                tb.set(vin[c] - (prevDir.x * 0.5f), vin[c + 1] - (prevDir.y * 0.5f));
                tc.set(vin[c] - (nextDir.x * 0.5f), vin[c + 1] - (nextDir.y * 0.5f));
                td.set(vin[n] - (nextDir.x * 0.5f), vin[n + 1] - (nextDir.y * 0.5f));
                if (LineUtils.lineIntersection(ta, tb, tc, td, point) == null) {
                    int vi6 = vi5 + 1;
                    vout[vi5] = tb.x;
                    vi = vi6 + 1;
                    vout[vi6] = tb.y;
                } else {
                    int vi7 = vi5 + 1;
                    vout[vi5] = point.x;
                    vi = vi7 + 1;
                    vout[vi7] = point.y;
                }
                ta.set(vin[p] + ((1.0f - 0.5f) * prevDir.x), vin[p + 1] + ((1.0f - 0.5f) * prevDir.y));
                tb.set(vin[c] + ((1.0f - 0.5f) * prevDir.x), vin[c + 1] + ((1.0f - 0.5f) * prevDir.y));
                tc.set(vin[c] + ((1.0f - 0.5f) * nextDir.x), vin[c + 1] + ((1.0f - 0.5f) * nextDir.y));
                td.set(vin[n] + ((1.0f - 0.5f) * nextDir.x), vin[n + 1] + ((1.0f - 0.5f) * nextDir.y));
                if (LineUtils.lineIntersection(ta, tb, tc, td, point) == null) {
                    int vi8 = vi + 1;
                    vout[vi] = tb.x;
                    vi5 = vi8 + 1;
                    vout[vi8] = tb.y;
                } else {
                    int vi9 = vi + 1;
                    vout[vi] = point.x;
                    vi5 = vi9 + 1;
                    vout[vi9] = point.y;
                }
            }
            int vi10 = vi5 + 1;
            vout[vi5] = vin[vin.length - 2] - (nextDir.x * 0.5f);
            int vi11 = vi10 + 1;
            vout[vi10] = vin[vin.length - 1] - (nextDir.y * 0.5f);
            int vi12 = vi11 + 1;
            vout[vi11] = vin[vin.length - 2] + ((1.0f - 0.5f) * nextDir.x);
            int vi13 = vi12 + 1;
            vout[vi12] = vin[vin.length - 1] + ((1.0f - 0.5f) * nextDir.y);
            if (!$assertionsDisabled && vi13 != vout.length) {
                throw new AssertionError(vi13 + " " + vout.length);
            }
            short[] tris = new short[(vin.length - 2) * 3];
            int ti = 0;
            for (int i2 = 0; i2 < (vin.length / 2) - 1; i2++) {
                int ti2 = ti + 1;
                tris[ti] = (short) (i2 * 2);
                int ti3 = ti2 + 1;
                tris[ti2] = (short) ((i2 * 2) + 2);
                int ti4 = ti3 + 1;
                tris[ti3] = (short) ((i2 * 2) + 1);
                int ti5 = ti4 + 1;
                tris[ti4] = (short) ((i2 * 2) + 2);
                int ti6 = ti5 + 1;
                tris[ti5] = (short) ((i2 * 2) + 3);
                ti = ti6 + 1;
                tris[ti6] = (short) ((i2 * 2) + 1);
            }
            return new Shape(to3D(vout, 0.0f), tris);
        }
        throw new AssertionError();
    }

    public static short[] makeQuads(int vertexCount, int startVertex, short[] dest, int start) {
        if (dest == null) {
            dest = new short[(vertexCount / 4) * 6];
            start = 0;
        }
        int index = start;
        int index2 = index;
        for (int i = startVertex; i < vertexCount + startVertex; i += 4) {
            int index3 = index2 + 1;
            dest[index2] = (short) i;
            int index4 = index3 + 1;
            dest[index3] = (short) (i + 3);
            int index5 = index4 + 1;
            dest[index4] = (short) (i + 1);
            int index6 = index5 + 1;
            dest[index5] = (short) i;
            int index7 = index6 + 1;
            dest[index6] = (short) (i + 2);
            index2 = index7 + 1;
            dest[index7] = (short) (i + 3);
        }
        return dest;
    }

    public static float[] getQuadTexCoords(int quads) {
        float[] tc = new float[quads * 8];
        for (int i = 0; i < quads; i++) {
            int b = i * 8;
            int o = 0 + 1;
            tc[b + 0] = 0.0f;
            tc[b + 1] = 0.0f;
            tc[b + 2] = 0.0f;
            tc[b + 3] = 1.0f;
            tc[b + 4] = 1.0f;
            tc[b + 5] = 0.0f;
            tc[b + 6] = 1.0f;
            int i2 = o + 1 + 1 + 1 + 1 + 1 + 1 + 1;
            tc[b + 7] = 1.0f;
        }
        return tc;
    }

    public static float[] vertFlipQuadTexCoords(float[] tc) {
        for (int i = 1; i < tc.length - 2; i += 4) {
            float swap = tc[i];
            tc[i] = tc[i + 2];
            tc[i + 2] = swap;
        }
        return tc;
    }

    public static float[] extract(Vector3f[] verts) {
        float[] v = new float[verts.length * 3];
        for (int i = 0; i < verts.length; i++) {
            v[i * 3] = verts[i].x;
            v[(i * 3) + 1] = verts[i].y;
            v[(i * 3) + 2] = verts[i].z;
        }
        return v;
    }

    public static float[] extract(Vector2f[] verts, float z) {
        float[] v = new float[verts.length * 3];
        for (int i = 0; i < verts.length; i++) {
            v[i * 3] = verts[i].x;
            v[(i * 3) + 1] = verts[i].y;
            v[(i * 3) + 2] = z;
        }
        return v;
    }

    public static float[] extract(Vector2f[] verts) {
        float[] v = new float[verts.length * 2];
        for (int i = 0; i < verts.length; i++) {
            v[i * 2] = verts[i].x;
            v[(i * 2) + 1] = verts[i].y;
        }
        return v;
    }

    public static float[] extract(List<Vector3f> verts) {
        float[] va = new float[verts.size() * 3];
        int vi = 0;
        for (Vector3f v : verts) {
            int vi2 = vi + 1;
            va[vi] = v.x;
            int vi3 = vi2 + 1;
            va[vi2] = v.y;
            va[vi3] = v.z;
            vi = vi3 + 1;
        }
        return va;
    }

    public static float[] extractVerts(List<Vector2f> verts, float z) {
        float[] va = new float[verts.size() * 3];
        int vi = 0;
        for (Vector2f v : verts) {
            int vi2 = vi + 1;
            va[vi] = v.x;
            int vi3 = vi2 + 1;
            va[vi2] = v.y;
            va[vi3] = z;
            vi = vi3 + 1;
        }
        return va;
    }

    public static short[] extractIndices(List<Short> indexes) {
        short[] ia = new short[indexes.size()];
        int j = 0;
        for (Short i : indexes) {
            ia[j] = i.shortValue();
            j++;
        }
        return ia;
    }

    public static int[] expand(int value, int size) {
        int[] a = new int[size];
        Arrays.fill(a, value);
        return a;
    }
}
