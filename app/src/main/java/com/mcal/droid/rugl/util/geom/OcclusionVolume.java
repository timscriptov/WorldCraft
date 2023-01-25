package com.mcal.droid.rugl.util.geom;

import androidx.annotation.NonNull;

import com.mcal.droid.rugl.util.geom.Frustum.Result;

/**
 * It's a bit like a frustum, but no far limit
 */
public class OcclusionVolume {
    private static final Vector3f u = new Vector3f();
    private static final Vector3f v = new Vector3f();
    private static final Vector3f w = new Vector3f();
    /**
     * Face normals
     */
    public float[][] faces = new float[5][4];

    /**
     * @param eyex
     * @param eyey
     * @param eyez
     * @param faceCoords
     */
    public void set(float eyex, float eyey, float eyez, @NonNull float... faceCoords) {
        u.set(faceCoords[3] - faceCoords[0], faceCoords[4] - faceCoords[1],
                faceCoords[5] - faceCoords[2]);
        v.set(faceCoords[6] - faceCoords[0], faceCoords[7] - faceCoords[1],
                faceCoords[8] - faceCoords[2]);

        Vector3f.cross(u, v, w);
        w.normalise();
        u.set(faceCoords[0], faceCoords[1], faceCoords[2]);
        float d = Vector3f.dot(w, u);
        faces[0][0] = w.x;
        faces[0][1] = w.y;
        faces[0][2] = w.z;
        faces[0][3] = d;

        for (int i = 0; i < 4; i++) {
            u.set(faceCoords[(3 * i + 0) % faceCoords.length] - eyex,
                    faceCoords[(3 * i + 1) % faceCoords.length] - eyey,
                    faceCoords[(3 * i + 2) % faceCoords.length] - eyez);
            v.set(faceCoords[(3 * i + 3) % faceCoords.length] - eyex,
                    faceCoords[(3 * i + 4) % faceCoords.length] - eyey,
                    faceCoords[(3 * i + 5) % faceCoords.length] - eyez);

            Vector3f.cross(u, v, w);
            w.normalise();
            u.set(faceCoords[0], faceCoords[1], faceCoords[2]);
            d = Vector3f.dot(w, u);

            faces[i + 1][0] = w.x;
            faces[i + 1][1] = w.y;
            faces[i + 1][2] = w.z;
            faces[i + 1][3] = d;
        }
    }

    /**
     * @param x
     * @param y
     * @param z
     * @return <code>true</code> if the point lies within the frustum
     */
    public boolean point(float x, float y, float z) {
        for (int p = 0; p < 5; p++) {
            if (faces[p][0] * x + faces[p][1] * y + faces[p][2] * z
                    + faces[p][3] <= 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param x
     * @param y
     * @param z
     * @param r
     * @return The intersection state between the sphere and the
     * frustum
     */
    public Result sphereIntersects(float x, float y, float z, float r) {
        int c = 0;
        for (int p = 0; p < 5; p++) {
            float d =
                    faces[p][0] * x + faces[p][1] * y + faces[p][2] * z
                            + faces[p][3];
            if (d <= -r) {
                return Result.Miss;
            }
            if (d > r) {
                c++;
            }
        }

        return c == 5 ? Result.Complete : Result.Partial;
    }

    /**
     * @param minx
     * @param miny
     * @param minz
     * @param maxx
     * @param maxy
     * @param maxz
     * @return the intersection state between the cuboid and the
     * frustum
     */
    public Result cuboidIntersects(float minx, float miny, float minz, float maxx,
                                   float maxy, float maxz) {
        int c;
        int c2 = 0;
        for (int p = 0; p < 5; p++) {
            c = 0;
            if (faces[p][0] * minx + faces[p][1] * miny + faces[p][2] * minz
                    + faces[p][3] > 0) {
                c++;
            }
            if (faces[p][0] * maxx + faces[p][1] * miny + faces[p][2] * minz
                    + faces[p][3] > 0) {
                c++;
            }
            if (faces[p][0] * minx + faces[p][1] * maxy + faces[p][2] * minz
                    + faces[p][3] > 0) {
                c++;
            }
            if (faces[p][0] * maxx + faces[p][1] * maxy + faces[p][2] * minz
                    + faces[p][3] > 0) {
                c++;
            }
            if (faces[p][0] * minx + faces[p][1] * miny + faces[p][2] * maxz
                    + faces[p][3] > 0) {
                c++;
            }
            if (faces[p][0] * maxx + faces[p][1] * miny + faces[p][2] * maxz
                    + faces[p][3] > 0) {
                c++;
            }
            if (faces[p][0] * minx + faces[p][1] * maxy + faces[p][2] * maxz
                    + faces[p][3] > 0) {
                c++;
            }
            if (faces[p][0] * maxx + faces[p][1] * maxy + faces[p][2] * maxz
                    + faces[p][3] > 0) {
                c++;
            }
            if (c == 0) {
                return Result.Miss;
            }
            if (c == 8) {
                c2++;
            }
        }
        return c2 == 6 ? Result.Complete : Result.Partial;
    }
}
