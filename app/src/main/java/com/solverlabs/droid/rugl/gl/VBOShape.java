package com.solverlabs.droid.rugl.gl;

import android.opengl.GLES11;
import android.opengl.GLException;

import com.solverlabs.droid.rugl.Game;
import com.solverlabs.droid.rugl.geom.ColouredShape;
import com.solverlabs.droid.rugl.geom.ShapeUtil;
import com.solverlabs.droid.rugl.geom.TexturedShape;
import com.solverlabs.droid.rugl.util.Colour;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;


public class VBOShape {
    private static int contextID;
    private static int vertexBytes;

    static {
        Game.addSurfaceLIstener(new Game.SurfaceListener() {
            @Override
            public void onSurfaceCreated() {
                VBOShape.access$008();
            }
        });
        contextID = 0;
        vertexBytes = 24;
    }

    private final ShortBuffer indexBuffer;
    private final int indexCount;
    private final int vertexCount;
    public State state;
    private ByteBuffer dataBuffer;
    private int uploadedContextID;
    private int dataVBOID = -1;
    private int indexVBOID = -1;

    public VBOShape(TexturedShape ts) {
        this.state = ts.state;
        this.vertexCount = ts.vertexCount();
        this.dataBuffer = BufferUtils.createByteBuffer(this.vertexCount * vertexBytes);
        for (int i = 0; i < this.vertexCount; i++) {
            this.dataBuffer.putFloat(ts.vertices[(i * 3) + 0]);
            this.dataBuffer.putFloat(ts.vertices[(i * 3) + 1]);
            this.dataBuffer.putFloat(ts.vertices[(i * 3) + 2]);
            this.dataBuffer.putInt(ts.colours[i]);
            this.dataBuffer.putFloat(ts.mTexCoords[(i * 2) + 0]);
            this.dataBuffer.putFloat(ts.mTexCoords[(i * 2) + 1]);
        }
        this.dataBuffer.flip();
        this.indexCount = ts.indices.length;
        this.indexBuffer = BufferUtils.createShortBuffer(this.indexCount);
        this.indexBuffer.put(ts.indices);
        this.indexBuffer.flip();
    }

    public VBOShape(ColouredShape cs) {
        this.state = cs.state;
        this.vertexCount = cs.vertexCount();
        this.dataBuffer = BufferUtils.createByteBuffer(this.vertexCount * vertexBytes);
        for (int i = 0; i < this.vertexCount; i++) {
            this.dataBuffer.putFloat(cs.vertices[(i * 3) + 0]);
            this.dataBuffer.putFloat(cs.vertices[(i * 3) + 1]);
            this.dataBuffer.putFloat(cs.vertices[(i * 3) + 2]);
            this.dataBuffer.putInt(cs.colours[i]);
            this.dataBuffer.putFloat(0.0f);
            this.dataBuffer.putFloat(0.0f);
        }
        this.dataBuffer.flip();
        this.indexCount = cs.indices.length;
        this.indexBuffer = BufferUtils.createShortBuffer(this.indexCount);
        this.indexBuffer.put(cs.indices);
        this.indexBuffer.flip();
    }

    static /* synthetic */ int access$008() {
        int i = contextID;
        contextID = i + 1;
        return i;
    }

    public void draw() {
        GLUtil.checkGLError();
        if (this.uploadedContextID != contextID) {
            delete();
        }
        if (this.dataVBOID == -1) {
            try {
                IntBuffer ib = GLUtil.intScratch(2);
                GLES11.glGenBuffers(2, ib);
                this.dataVBOID = ib.get(0);
                this.indexVBOID = ib.get(1);
                if (this.dataVBOID == 0 || this.indexVBOID == 0) {
                    throw new GLException(1282, "Attempted to bind null buffer name : " + this.dataVBOID + " or " + this.indexVBOID);
                }
                GLES11.glBindBuffer(34962, this.dataVBOID);
                GLES11.glBufferData(34962, this.vertexCount * vertexBytes, this.dataBuffer, 35044);
                GLES11.glBindBuffer(34963, this.indexVBOID);
                GLES11.glBufferData(34963, this.indexCount * 2, this.indexBuffer, 35044);
                this.uploadedContextID = contextID;
                GLUtil.checkGLError();
            } catch (Exception e) {
                delete();
            }
        }
        this.state.apply();
        GLES11.glBindBuffer(34962, this.dataVBOID);
        GLES11.glVertexPointer(3, 5126, vertexBytes, 0);
        GLES11.glColorPointer(4, 5121, vertexBytes, 12);
        GLES11.glTexCoordPointer(2, 5126, vertexBytes, 16);
        GLES11.glBindBuffer(34963, this.indexVBOID);
        GLES11.glDrawElements(this.state.drawMode.glValue, this.indexCount, 5123, 0);
        GLES11.glBindBuffer(34962, 0);
        GLES11.glBindBuffer(34963, 0);
        GLUtil.checkGLError();
    }

    public void delete() {
        GLUtil.checkGLError();
        IntBuffer ib = GLUtil.intScratch(2);
        ib.put(0, this.dataVBOID);
        ib.put(1, this.indexVBOID);
        GLES11.glDeleteBuffers(2, ib);
        this.dataVBOID = -1;
        this.indexVBOID = -1;
        GLUtil.checkGLError();
    }

    public void setColour(float colour) {
        float resColour = Colour.packFloat(colour, colour, colour, 1.0f);
        int[] colours = ShapeUtil.expand((int) resColour, 4);
        for (int i = 0; i < 4; i++) {
            this.dataBuffer.position(this.dataBuffer.position() + 12);
            this.dataBuffer.putInt(colours[i]);
            this.dataBuffer.position(this.dataBuffer.position() + 8);
        }
    }

    public ByteBuffer getDataBuffer() {
        return this.dataBuffer;
    }

    public int getVertexCount() {
        return this.vertexCount;
    }
}
