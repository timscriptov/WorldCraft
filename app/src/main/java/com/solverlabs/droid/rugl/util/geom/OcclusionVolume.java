package com.solverlabs.droid.rugl.util.geom;

import java.lang.reflect.Array;


public class OcclusionVolume {
    private static Vector3f u = new Vector3f();
    private static Vector3f v = new Vector3f();
    private static Vector3f w = new Vector3f();
    public float[][] faces = (float[][]) Array.newInstance(Float.TYPE, 5, 4);

    public void set(float eyex, float eyey, float eyez, float... faceCoords) {
        u.set(faceCoords[3] - faceCoords[0], faceCoords[4] - faceCoords[1], faceCoords[5] - faceCoords[2]);
        v.set(faceCoords[6] - faceCoords[0], faceCoords[7] - faceCoords[1], faceCoords[8] - faceCoords[2]);
        Vector3f.cross(u, v, w);
        w.normalise();
        u.set(faceCoords[0], faceCoords[1], faceCoords[2]);
        float d = Vector3f.dot(w, u);
        this.faces[0][0] = w.x;
        this.faces[0][1] = w.y;
        this.faces[0][2] = w.z;
        this.faces[0][3] = d;
        for (int i = 0; i < 4; i++) {
            u.set(faceCoords[((i * 3) + 0) % faceCoords.length] - eyex, faceCoords[((i * 3) + 1) % faceCoords.length] - eyey, faceCoords[((i * 3) + 2) % faceCoords.length] - eyez);
            v.set(faceCoords[((i * 3) + 3) % faceCoords.length] - eyex, faceCoords[((i * 3) + 4) % faceCoords.length] - eyey, faceCoords[((i * 3) + 5) % faceCoords.length] - eyez);
            Vector3f.cross(u, v, w);
            w.normalise();
            u.set(faceCoords[0], faceCoords[1], faceCoords[2]);
            float d2 = Vector3f.dot(w, u);
            this.faces[i + 1][0] = w.x;
            this.faces[i + 1][1] = w.y;
            this.faces[i + 1][2] = w.z;
            this.faces[i + 1][3] = d2;
        }
    }

    public boolean point(float x, float y, float z) {
        for (int p = 0; p < 5; p++) {
            if ((this.faces[p][0] * x) + (this.faces[p][1] * y) + (this.faces[p][2] * z) + this.faces[p][3] <= 0.0f) {
                return false;
            }
        }
        return true;
    }

    public Frustum.Result sphereIntersects(float x, float y, float z, float r) {
        int c = 0;
        for (int p = 0; p < 5; p++) {
            float d = (this.faces[p][0] * x) + (this.faces[p][1] * y) + (this.faces[p][2] * z) + this.faces[p][3];
            if (d <= (-r)) {
                return Frustum.Result.Miss;
            }
            if (d > r) {
                c++;
            }
        }
        return c == 5 ? Frustum.Result.Complete : Frustum.Result.Partial;
    }

    public Frustum.Result cuboidIntersects(float minx, float miny, float minz, float maxx, float maxy, float maxz) {
        int c2 = 0;
        for (int p = 0; p < 5; p++) {
            int c = 0;
            if ((this.faces[p][0] * minx) + (this.faces[p][1] * miny) + (this.faces[p][2] * minz) + this.faces[p][3] > 0.0f) {
                c = 0 + 1;
            }
            if ((this.faces[p][0] * maxx) + (this.faces[p][1] * miny) + (this.faces[p][2] * minz) + this.faces[p][3] > 0.0f) {
                c++;
            }
            if ((this.faces[p][0] * minx) + (this.faces[p][1] * maxy) + (this.faces[p][2] * minz) + this.faces[p][3] > 0.0f) {
                c++;
            }
            if ((this.faces[p][0] * maxx) + (this.faces[p][1] * maxy) + (this.faces[p][2] * minz) + this.faces[p][3] > 0.0f) {
                c++;
            }
            if ((this.faces[p][0] * minx) + (this.faces[p][1] * miny) + (this.faces[p][2] * maxz) + this.faces[p][3] > 0.0f) {
                c++;
            }
            if ((this.faces[p][0] * maxx) + (this.faces[p][1] * miny) + (this.faces[p][2] * maxz) + this.faces[p][3] > 0.0f) {
                c++;
            }
            if ((this.faces[p][0] * minx) + (this.faces[p][1] * maxy) + (this.faces[p][2] * maxz) + this.faces[p][3] > 0.0f) {
                c++;
            }
            if ((this.faces[p][0] * maxx) + (this.faces[p][1] * maxy) + (this.faces[p][2] * maxz) + this.faces[p][3] > 0.0f) {
                c++;
            }
            if (c == 0) {
                return Frustum.Result.Miss;
            }
            if (c == 8) {
                c2++;
            }
        }
        return c2 == 6 ? Frustum.Result.Complete : Frustum.Result.Partial;
    }
}
