package com.solverlabs.droid.rugl.util.geom;

import androidx.annotation.NonNull;

import com.solverlabs.worldcraft.math.MathUtils;

import org.jetbrains.annotations.Contract;

import java.nio.FloatBuffer;

public class Quaternion extends Vector implements ReadableVector4f, WritableVector4f {
    private static final long serialVersionUID = 1;
    public float w;
    public float x;
    public float y;
    public float z;

    public Quaternion() {
        setIdentity();
    }

    public Quaternion(ReadableVector4f src) {
        set(src);
    }

    public Quaternion(float x, float y, float z, float w) {
        set(x, y, z, w);
    }

    @NonNull
    @Contract("_ -> param1")
    public static Quaternion setIdentity(@NonNull Quaternion q) {
        q.x = 0.0f;
        q.y = 0.0f;
        q.z = 0.0f;
        q.w = 1.0f;
        return q;
    }

    @NonNull
    public static Quaternion normalise(@NonNull Quaternion src, Quaternion dest) {
        float inv_l = 1.0f / src.length();
        if (dest == null) {
            dest = new Quaternion();
        }
        dest.set(src.x * inv_l, src.y * inv_l, src.z * inv_l, src.w * inv_l);
        return dest;
    }

    @Contract(pure = true)
    public static float dot(@NonNull Quaternion left, @NonNull Quaternion right) {
        return (left.x * right.x) + (left.y * right.y) + (left.z * right.z) + (left.w * right.w);
    }

    @NonNull
    public static Quaternion negate(Quaternion src, Quaternion dest) {
        if (dest == null) {
            dest = new Quaternion();
        }
        dest.x = -src.x;
        dest.y = -src.y;
        dest.z = -src.z;
        dest.w = src.w;
        return dest;
    }

    @NonNull
    public static Quaternion scale(float scale, Quaternion src, Quaternion dest) {
        if (dest == null) {
            dest = new Quaternion();
        }
        dest.x = src.x * scale;
        dest.y = src.y * scale;
        dest.z = src.z * scale;
        dest.w = src.w * scale;
        return dest;
    }

    @NonNull
    public static Quaternion mul(Quaternion left, Quaternion right, Quaternion dest) {
        if (dest == null) {
            dest = new Quaternion();
        }
        dest.set((((left.x * right.w) + (left.w * right.x)) + (left.y * right.z)) - (left.z * right.y), (((left.y * right.w) + (left.w * right.y)) + (left.z * right.x)) - (left.x * right.z), (((left.z * right.w) + (left.w * right.z)) + (left.x * right.y)) - (left.y * right.x), (((left.w * right.w) - (left.x * right.x)) - (left.y * right.y)) - (left.z * right.z));
        return dest;
    }

    @NonNull
    public static Quaternion mulInverse(Quaternion left, @NonNull Quaternion right, Quaternion dest) {
        float n = right.lengthSquared();
        if (n != 0.0d) {
            n = 1.0f / n;
        }
        if (dest == null) {
            dest = new Quaternion();
        }
        dest.set(((((left.x * right.w) - (left.w * right.x)) - (left.y * right.z)) + (left.z * right.y)) * n, ((((left.y * right.w) - (left.w * right.y)) - (left.z * right.x)) + (left.x * right.z)) * n, ((((left.z * right.w) - (left.w * right.z)) - (left.x * right.y)) + (left.y * right.x)) * n, ((left.w * right.w) + (left.x * right.x) + (left.y * right.y) + (left.z * right.z)) * n);
        return dest;
    }

    @Contract("_, _ -> param2")
    public static final Quaternion setFromMatrix(@NonNull Matrix4f m, @NonNull Quaternion q) {
        return q.setFromMat(m.m00, m.m01, m.m02, m.m10, m.m11, m.m12, m.m20, m.m21, m.m22);
    }

    @Contract("_, _ -> param2")
    public static final Quaternion setFromMatrix(@NonNull Matrix3f m, @NonNull Quaternion q) {
        return q.setFromMat(m.m00, m.m01, m.m02, m.m10, m.m11, m.m12, m.m20, m.m21, m.m22);
    }

    @Override
    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void set(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Quaternion set(@NonNull ReadableVector4f src) {
        this.x = src.getX();
        this.y = src.getY();
        this.z = src.getZ();
        this.w = src.getW();
        return this;
    }

    public Quaternion setIdentity() {
        return setIdentity(this);
    }

    @Override
    public float lengthSquared() {
        return (this.x * this.x) + (this.y * this.y) + (this.z * this.z) + (this.w * this.w);
    }

    public Quaternion normalise(Quaternion dest) {
        return normalise(this, dest);
    }

    public Quaternion negate(Quaternion dest) {
        return negate(this, dest);
    }

    @Override
    public Vector negate() {
        return negate(this, this);
    }

    @Override
    public Vector load(@NonNull FloatBuffer buf) {
        this.x = buf.get();
        this.y = buf.get();
        this.z = buf.get();
        this.w = buf.get();
        return this;
    }

    @Override
    public Vector scale(float scale) {
        return scale(scale, this, this);
    }

    @Override
    public Vector store(@NonNull FloatBuffer buf) {
        buf.put(this.x);
        buf.put(this.y);
        buf.put(this.z);
        buf.put(this.w);
        return this;
    }

    @Override
    public final float getX() {
        return this.x;
    }

    @Override
    public final void setX(float x) {
        this.x = x;
    }

    @Override
    public final float getY() {
        return this.y;
    }

    @Override
    public final void setY(float y) {
        this.y = y;
    }

    @Override
    public float getZ() {
        return this.z;
    }

    @Override
    public void setZ(float z) {
        this.z = z;
    }

    @Override
    public float getW() {
        return this.w;
    }

    @Override
    public void setW(float w) {
        this.w = w;
    }

    @NonNull
    public String toString() {
        return "Quaternion: " + this.x + " " + this.y + " " + this.z + " " + this.w;
    }

    public final void setFromAxisAngle(@NonNull Vector4f a1) {
        this.x = a1.x;
        this.y = a1.y;
        this.z = a1.z;
        float n = (float) Math.sqrt((this.x * this.x) + (this.y * this.y) + (this.z * this.z));
        float s = MathUtils.sin((float) (a1.w * 0.5d)) / n;
        this.x *= s;
        this.y *= s;
        this.z *= s;
        this.w = MathUtils.cos((float) (a1.w * 0.5d));
    }

    public final Quaternion setFromMatrix(Matrix4f m) {
        return setFromMatrix(m, this);
    }

    public final Quaternion setFromMatrix(Matrix3f m) {
        return setFromMatrix(m, this);
    }

    private Quaternion setFromMat(float m00, float m01, float m02, float m10, float m11, float m12, float m20, float m21, float m22) {
        float tr = m00 + m11 + m22;
        if (tr >= 0.0d) {
            float s = (float) Math.sqrt(tr + 1.0d);
            this.w = 0.5f * s;
            float s2 = 0.5f / s;
            this.x = (m21 - m12) * s2;
            this.y = (m02 - m20) * s2;
            this.z = (m10 - m01) * s2;
        } else {
            float max = Math.max(Math.max(m00, m11), m22);
            if (max == m00) {
                float s3 = (float) Math.sqrt((m00 - (m11 + m22)) + 1.0d);
                this.x = 0.5f * s3;
                float s4 = 0.5f / s3;
                this.y = (m01 + m10) * s4;
                this.z = (m20 + m02) * s4;
                this.w = (m21 - m12) * s4;
            } else if (max == m11) {
                float s5 = (float) Math.sqrt((m11 - (m22 + m00)) + 1.0d);
                this.y = 0.5f * s5;
                float s6 = 0.5f / s5;
                this.z = (m12 + m21) * s6;
                this.x = (m01 + m10) * s6;
                this.w = (m02 - m20) * s6;
            } else {
                float s7 = (float) Math.sqrt((m22 - (m00 + m11)) + 1.0d);
                this.z = 0.5f * s7;
                float s8 = 0.5f / s7;
                this.x = (m20 + m02) * s8;
                this.y = (m12 + m21) * s8;
                this.w = (m10 - m01) * s8;
            }
        }
        return this;
    }
}
