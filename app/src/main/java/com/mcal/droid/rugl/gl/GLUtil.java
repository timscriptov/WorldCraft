package com.mcal.droid.rugl.gl;

import android.opengl.GLES10;
import android.opengl.GLException;

import androidx.annotation.NonNull;

import com.mcal.droid.rugl.gl.enums.ComparisonFunction;
import com.mcal.droid.rugl.gl.enums.DestinationFactor;
import com.mcal.droid.rugl.gl.enums.SourceFactor;
import com.mcal.droid.rugl.gl.facets.AlphaTest;
import com.mcal.droid.rugl.gl.facets.Blend;
import com.mcal.droid.rugl.gl.facets.DepthTest;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;


public class GLUtil {
    public static final State typicalState = new State().with(new Blend(SourceFactor.SRC_ALPHA, DestinationFactor.ONE_MINUS_SRC_ALPHA)).with(new DepthTest(ComparisonFunction.LEQUAL)).with(new AlphaTest(ComparisonFunction.GREATER, 0.0f));
    private static final ByteBuffer scratch = BufferUtils.createByteBuffer(64);

    @NonNull
    public static IntBuffer intScratch(int size) {
        scratch.clear();
        scratch.limit(size * 4);
        return scratch.asIntBuffer();
    }

    public static int nextPowerOf2(int n) {
        int x = 1;
        while (x < n) {
            x <<= 1;
        }
        return x;
    }

    public static void scaledOrtho(float desiredWidth, float desiredHeight, int screenWidth, int screenHeight, float near, float far) {
        GLES10.glMatrixMode(5889);
        GLES10.glLoadIdentity();
        GLES10.glOrthof(0.0f, desiredWidth, 0.0f, desiredHeight, near, far);
        GLES10.glMatrixMode(5888);
        GLES10.glLoadIdentity();
        GLES10.glViewport(0, 0, screenWidth, screenHeight);
    }

    public static void checkGLError() throws GLException {
        int err = GLES10.glGetError();
        if (err != 0) {
            throw new GLException(err);
        }
    }

    public static void enableVertexArrays() {
        GLES10.glEnableClientState(32884);
        GLES10.glEnableClientState(32888);
        GLES10.glEnableClientState(32886);
    }

    public static void disableVertexArrays() {
        GLES10.glDisableClientState(32884);
        GLES10.glDisableClientState(32888);
        GLES10.glDisableClientState(32886);
    }
}
