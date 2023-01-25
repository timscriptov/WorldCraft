package com.mcal.droid.rugl.util.geom;

import static android.opengl.GLES11.GL_MODELVIEW_MATRIX;
import static android.opengl.GLES11.GL_PROJECTION_MATRIX;
import static android.opengl.GLES11.glGetFloatv;

import com.mcal.droid.rugl.util.FloatMath;

/**
 * Taken from http://www.racer.nl/reference/vfc_markmorley.htm
 */
public class Frustum {
    private static final float[] proj = new float[16];
    private static final float[] modl = new float[16];
    private static final float[] clip = new float[16];
    /**
     * Frustum planes, in right, left, bottom, top, far, near order.
     * Each plane is defined by normalX, normalY, normalZ,
     * distanceFromOrigin values
     */
    private final float[][] frustum = new float[6][4];

    /**
     *
     */
    public Frustum() {

    }

    /**
     * Copy constructor
     *
     * @param f
     */
    public Frustum(Frustum f) {
        for (int i = 0; i < frustum.length; i++) {
            for (int j = 0; j < frustum[i].length; j++) {
                frustum[i][j] = f.frustum[i][j];
            }
        }
    }

    /**
     * Extracts a frustum from OpenGL. Beware PixelFlinger! It does not
     * implement glGetFloat!
     */
    public void extractFromOGL() {
        glGetFloatv(GL_PROJECTION_MATRIX, proj, 0);
        glGetFloatv(GL_MODELVIEW_MATRIX, modl, 0);

        update(proj, modl);
    }

    /**
     * Updates the frustum planes from the supplied projection and
     * modelView matrices
     *
     * @param projection
     * @param modelView
     */
    public void update(float[] projection, float[] modelView) {
        float[] proj = projection;
        float[] modl = modelView;
        try {

            // Combine the two matrices (multiply projection by
            // modelview)
            clip[0] =
                    modl[0] * proj[0] + modl[1] * proj[4] + modl[2] * proj[8]
                            + modl[3] * proj[12];
            clip[1] =
                    modl[0] * proj[1] + modl[1] * proj[5] + modl[2] * proj[9]
                            + modl[3] * proj[13];
            clip[2] =
                    modl[0] * proj[2] + modl[1] * proj[6] + modl[2] * proj[10]
                            + modl[3] * proj[14];
            clip[3] =
                    modl[0] * proj[3] + modl[1] * proj[7] + modl[2] * proj[11]
                            + modl[3] * proj[15];
            clip[4] =
                    modl[4] * proj[0] + modl[5] * proj[4] + modl[6] * proj[8]
                            + modl[7] * proj[12];
            clip[5] =
                    modl[4] * proj[1] + modl[5] * proj[5] + modl[6] * proj[9]
                            + modl[7] * proj[13];
            clip[6] =
                    modl[4] * proj[2] + modl[5] * proj[6] + modl[6] * proj[10]
                            + modl[7] * proj[14];
            clip[7] =
                    modl[4] * proj[3] + modl[5] * proj[7] + modl[6] * proj[11]
                            + modl[7] * proj[15];
            clip[8] =
                    modl[8] * proj[0] + modl[9] * proj[4] + modl[10] * proj[8]
                            + modl[11] * proj[12];
            clip[9] =
                    modl[8] * proj[1] + modl[9] * proj[5] + modl[10] * proj[9]
                            + modl[11] * proj[13];
            clip[10] =
                    modl[8] * proj[2] + modl[9] * proj[6] + modl[10] * proj[10]
                            + modl[11] * proj[14];
            clip[11] =
                    modl[8] * proj[3] + modl[9] * proj[7] + modl[10] * proj[11]
                            + modl[11] * proj[15];
            clip[12] =
                    modl[12] * proj[0] + modl[13] * proj[4] + modl[14] * proj[8]
                            + modl[15] * proj[12];
            clip[13] =
                    modl[12] * proj[1] + modl[13] * proj[5] + modl[14] * proj[9]
                            + modl[15] * proj[13];
            clip[14] =
                    modl[12] * proj[2] + modl[13] * proj[6] + modl[14] * proj[10]
                            + modl[15] * proj[14];
            clip[15] =
                    modl[12] * proj[3] + modl[13] * proj[7] + modl[14] * proj[11]
                            + modl[15] * proj[15];

            // right
            frustum[0][0] = clip[3] - clip[0];
            frustum[0][1] = clip[7] - clip[4];
            frustum[0][2] = clip[11] - clip[8];
            frustum[0][3] = clip[15] - clip[12];
            // normalise
            float t =
                    1.0f / FloatMath.sqrt(frustum[0][0] * frustum[0][0] + frustum[0][1]
                            * frustum[0][1] + frustum[0][2] * frustum[0][2]);
            frustum[0][0] *= t;
            frustum[0][1] *= t;
            frustum[0][2] *= t;
            frustum[0][3] *= t;
            // left
            frustum[1][0] = clip[3] + clip[0];
            frustum[1][1] = clip[7] + clip[4];
            frustum[1][2] = clip[11] + clip[8];
            frustum[1][3] = clip[15] + clip[12];
            // normalise
            t =
                    1.0f / FloatMath.sqrt(frustum[1][0] * frustum[1][0] + frustum[1][1]
                            * frustum[1][1] + frustum[1][2] * frustum[1][2]);
            frustum[1][0] *= t;
            frustum[1][1] *= t;
            frustum[1][2] *= t;
            frustum[1][3] *= t;
            // bottom
            frustum[2][0] = clip[3] + clip[1];
            frustum[2][1] = clip[7] + clip[5];
            frustum[2][2] = clip[11] + clip[9];
            frustum[2][3] = clip[15] + clip[13];
            // normalise
            t =
                    1.0f / FloatMath.sqrt(frustum[2][0] * frustum[2][0] + frustum[2][1]
                            * frustum[2][1] + frustum[2][2] * frustum[2][2]);
            frustum[2][0] *= t;
            frustum[2][1] *= t;
            frustum[2][2] *= t;
            frustum[2][3] *= t;
            // top
            frustum[3][0] = clip[3] - clip[1];
            frustum[3][1] = clip[7] - clip[5];
            frustum[3][2] = clip[11] - clip[9];
            frustum[3][3] = clip[15] - clip[13];
            // normalise
            t =
                    1.0f / FloatMath.sqrt(frustum[3][0] * frustum[3][0] + frustum[3][1]
                            * frustum[3][1] + frustum[3][2] * frustum[3][2]);
            frustum[3][0] *= t;
            frustum[3][1] *= t;
            frustum[3][2] *= t;
            frustum[3][3] *= t;
            // far
            frustum[4][0] = clip[3] - clip[2];
            frustum[4][1] = clip[7] - clip[6];
            frustum[4][2] = clip[11] - clip[10];
            frustum[4][3] = clip[15] - clip[14];
            // normalise
            t =
                    1.0f / FloatMath.sqrt(frustum[4][0] * frustum[4][0] + frustum[4][1]
                            * frustum[4][1] + frustum[4][2] * frustum[4][2]);
            frustum[4][0] *= t;
            frustum[4][1] *= t;
            frustum[4][2] *= t;
            frustum[4][3] *= t;
            // near
            frustum[5][0] = clip[3] + clip[2];
            frustum[5][1] = clip[7] + clip[6];
            frustum[5][2] = clip[11] + clip[10];
            frustum[5][3] = clip[15] + clip[14];
            // normalise
            t =
                    1.0f / FloatMath.sqrt(frustum[5][0] * frustum[5][0] + frustum[5][1]
                            * frustum[5][1] + frustum[5][2] * frustum[5][2]);
            frustum[5][0] *= t;
            frustum[5][1] *= t;
            frustum[5][2] *= t;
            frustum[5][3] *= t;
        } catch (Exception e) {
            StringBuilder b = new StringBuilder();
            b.append("modl.length = ").append(modl.length);
            b.append("\nproj.length = ").append(proj.length);
            b.append("\nfrustum.length = ").append(frustum.length);
            for (int i = 0; i < frustum.length; i++) {
                try {
                    b.append("\n\tfrustum[ ").append(i).append(" ].length = ");
                    if (frustum[i] != null) {
                        b.append(frustum[i].length);
                    } else {
                        b.append("null");
                    }
                } catch (Exception omg) {
                    throw new RuntimeException(
                            "Sweet jesus what is going on here? Exception on index " + i, omg);
                }
            }

            throw new RuntimeException(b.toString(), e);
        }
    }

    /**
     * @param x
     * @param y
     * @param z
     * @return <code>true</code> if the point lies within the frustum
     */
    public boolean point(float x, float y, float z) {
        for (int p = 0; p < 6; p++) {
            if (frustum[p][0] * x + frustum[p][1] * y + frustum[p][2] * z
                    + frustum[p][3] <= 0) {
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
        for (int p = 0; p < 6; p++) {
            float d =
                    frustum[p][0] * x + frustum[p][1] * y + frustum[p][2] * z
                            + frustum[p][3];
            if (d <= -r) {
                return Result.Miss;
            }
            if (d > r) {
                c++;
            }
        }

        return c == 6 ? Result.Complete : Result.Partial;
    }

    /**
     * Checks if the sphere intersects the frustum, and calculates the
     * distance to the near plane if so
     *
     * @param x
     * @param y
     * @param z
     * @param r
     * @return the distance from the camera to the sphere's center plus
     * the radius, or 0 if the sphere does not intersect the
     * frustum
     */
    public float sphereDistance(float x, float y, float z, float r) {
        float d = 0;
        for (int p = 0; p < 6; p++) {
            d =
                    frustum[p][0] * x + frustum[p][1] * y + frustum[p][2] * z
                            + frustum[p][3];
            if (d <= -r) {
                return 0;
            }
        }

        return d + r;
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
        for (int p = 0; p < 6; p++) {
            c = 0;
            if (frustum[p][0] * minx + frustum[p][1] * miny + frustum[p][2]
                    * minz + frustum[p][3] > 0) {
                c++;
            }
            if (frustum[p][0] * maxx + frustum[p][1] * miny + frustum[p][2]
                    * minz + frustum[p][3] > 0) {
                c++;
            }
            if (frustum[p][0] * minx + frustum[p][1] * maxy + frustum[p][2]
                    * minz + frustum[p][3] > 0) {
                c++;
            }
            if (frustum[p][0] * maxx + frustum[p][1] * maxy + frustum[p][2]
                    * minz + frustum[p][3] > 0) {
                c++;
            }
            if (frustum[p][0] * minx + frustum[p][1] * miny + frustum[p][2]
                    * maxz + frustum[p][3] > 0) {
                c++;
            }
            if (frustum[p][0] * maxx + frustum[p][1] * miny + frustum[p][2]
                    * maxz + frustum[p][3] > 0) {
                c++;
            }
            if (frustum[p][0] * minx + frustum[p][1] * maxy + frustum[p][2]
                    * maxz + frustum[p][3] > 0) {
                c++;
            }
            if (frustum[p][0] * maxx + frustum[p][1] * maxy + frustum[p][2]
                    * maxz + frustum[p][3] > 0) {
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

    /**
     * Result of an intersection test
     */
    public static enum Result {
        /**
         * Subject lies outside of the frustum
         */
        Miss,
        /**
         * Subject intersects the frustum
         */
        Partial,
        /**
         * Subject lies within the frustum
         */
        Complete;
    }
}
