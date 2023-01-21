package com.solverlabs.droid.rugl.util;

import android.opengl.GLES10;

import com.solverlabs.droid.rugl.Game;
import com.solverlabs.droid.rugl.gl.GLU;
import com.solverlabs.droid.rugl.util.geom.Frustum;
import com.solverlabs.droid.rugl.util.geom.Matrix4f;
import com.solverlabs.droid.rugl.util.geom.Vector3f;
import com.solverlabs.droid.rugl.util.geom.Vector4f;
import com.solverlabs.droid.rugl.util.math.Range;
import com.solverlabs.worldcraft.factories.BlockFactory;


public class FPSCamera {
    public final Vector3f forward = new Vector3f();
    public final Vector3f up = new Vector3f();
    public final Vector3f right = new Vector3f();
    private final Vector3f position = new Vector3f();
    private final Matrix4f m = new Matrix4f();
    private final Frustum frustum = new Frustum();
    public boolean invert = false;
    public float headingSpeed = 90.0f;
    public float pitchSpeed = 90.0f;
    public BlockFactory.WorldSide currentWorldSide = BlockFactory.WorldSide.North;
    public float aspect = -1.0f;
    public float fov = 70.0f;
    public float near = 0.01f;
    public float far = 500.0f;
    private Vector4f v4f = new Vector4f();
    private float elevation = 0.0f;
    private float heading = 0.0f;
    private boolean frustumDirty = true;
    private float[] projectionMatrix = new float[16];
    private float[] modelViewMatrix = new float[16];
    private float lastHeadingX = 0.0f;
    private float lastElevationY = 0.0f;

    public void advance(float headingX, float elevationY) {
        float dx = headingX - this.lastHeadingX;
        float dy = elevationY - this.lastElevationY;
        this.lastHeadingX = headingX;
        this.lastElevationY = elevationY;
        if (Math.abs(dx) > 0.01f && headingX != 0.0f) {
            this.heading += (float) ((-dx) * Math.toRadians(this.headingSpeed));
            this.frustumDirty = true;
        }
        if (Math.abs(dy) > 0.01f && elevationY != 0.0f) {
            this.elevation = ((float) ((this.invert ? 1 : -1) * dy * Math.toRadians(this.pitchSpeed))) + this.elevation;
            this.frustumDirty = true;
        }
        updateVectors();
    }

    public void updateVectors() {
        this.m.setIdentity();
        float headingAngle = Range.wrap(this.heading, 0.0f, 6.2831855f);
        this.m.rotate(headingAngle, 0.0f, 1.0f, 0.0f);
        this.m.rotate(Range.limit(this.elevation, -1.5550884f, 1.5550884f), 1.0f, 0.0f, 0.0f);
        this.v4f.set(0.0f, 0.0f, 1.0f, 0.0f);
        Matrix4f.transform(this.m, this.v4f, this.v4f);
        this.forward.set(this.v4f.x, this.v4f.y, this.v4f.z);
        this.right.set(this.forward.z, 0.0f, -this.forward.x);
        this.right.normalise();
        Vector3f.cross(this.forward, this.right, this.up);
    }

    public float getHeading() {
        return this.heading;
    }

    public void setHeading(float heading) {
        this.heading = heading;
        this.frustumDirty = true;
    }

    public float getElevation() {
        return this.elevation;
    }

    public void setElevation(float elevation) {
        this.elevation = elevation;
        this.frustumDirty = true;
    }

    public void setPosition(float eyeX, float eyeY, float eyeZ) {
        if (this.position.x != eyeX || this.position.y != eyeY || this.position.z != eyeZ) {
            this.frustumDirty = true;
            this.position.x = eyeX;
            this.position.y = eyeY;
            this.position.z = eyeZ;
        }
        if (this.aspect == -1.0f) {
            this.aspect = Game.screenWidth / Game.screenHeight;
        }
        GLES10.glMatrixMode(5889);
        GLES10.glLoadIdentity();
        GLU.gluPerspective(this.fov, this.aspect, this.near, this.far, this.projectionMatrix);
        GLES10.glMatrixMode(5888);
        GLES10.glLoadIdentity();
        GLU.gluLookAt(eyeX, eyeY, eyeZ, eyeX + this.forward.x, eyeY + this.forward.y, eyeZ + this.forward.z, this.up.x, this.up.y, this.up.z, this.modelViewMatrix);
    }

    public void resetGluPerspective() {
        GLES10.glMatrixMode(5889);
        GLES10.glLoadIdentity();
        GLU.gluPerspective(this.fov, this.aspect, this.near, this.far, this.projectionMatrix);
        GLES10.glMatrixMode(5888);
        GLES10.glLoadIdentity();
        GLU.gluLookAt(this.position.x, this.position.y, this.position.z, this.position.x + this.forward.x, this.position.y + this.forward.y, this.position.z + this.forward.z, this.up.x, this.up.y, this.up.z, this.modelViewMatrix);
    }

    public Frustum getFrustum() {
        if (this.frustumDirty) {
            this.frustum.update(this.projectionMatrix, this.modelViewMatrix);
            this.frustumDirty = false;
        }
        return this.frustum;
    }

    public Vector3f unProject(float x, float y, Vector3f dest) {
        this.v4f.set(this.forward.x, this.forward.y, this.forward.z, 0.0f);
        float yAngle = ((-y) * this.fov) / 2.0f;
        float xAngle = (((-x) * this.aspect) * this.fov) / 2.0f;
        this.m.setIdentity();
        this.m.rotate(Trig.toRadians(yAngle), this.right.x, this.right.y, this.right.z);
        this.m.rotate(Trig.toRadians(xAngle), 0.0f, 1.0f, 0.0f);
        Matrix4f.transform(this.m, this.v4f, this.v4f);
        if (dest == null) {
            dest = new Vector3f();
        }
        dest.set(this.v4f);
        return dest;
    }

    public String toString() {
        return "e = " + this.elevation + "\nh = " + this.heading + "\nf = " + this.forward + "\nu = " + this.up + "\nr = " + this.right;
    }
}
