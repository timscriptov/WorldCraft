package com.solverlabs.droid.rugl.geom;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.gl.Renderer;
import com.solverlabs.droid.rugl.texture.Texture;


public class TexturedShape extends ColouredShape {
    public final Texture mTexture;
    protected final float[] mCorrectedTexCoords;
    public float[] mTexCoords;
    public boolean mTexCoordsDirty;

    public TexturedShape(ColouredShape shape, @NonNull float[] texCoords, Texture texture) {
        super(shape);
        mTexCoordsDirty = true;
        mTexCoords = texCoords;
        mTexture = texture;
        mCorrectedTexCoords = (float[]) texCoords.clone();
        if (texture != null) {
            state = texture.applyTo(state);
        }
        sanity();
    }

    public TexturedShape(TexturedShape ts) {
        super(ts);
        mTexCoordsDirty = true;
        mTexCoords = ts.mTexCoords;
        mTexture = ts.mTexture;
        mCorrectedTexCoords = ts.mCorrectedTexCoords;
        if (mTexture != null) {
            state = mTexture.applyTo(state);
        }
    }

    private void sanity() throws IllegalArgumentException, IllegalStateException {
        if (mTexCoords.length != vertexCount() * 2) {
            throw new IllegalArgumentException("Texture coordinate count mismatch\n" + toString());
        }
        if (mTexCoords.length != mCorrectedTexCoords.length) {
            throw new IllegalStateException("wat");
        }
        if (mTexCoords == mCorrectedTexCoords) {
            throw new IllegalStateException("this is a poor idea");
        }
    }

    public float[] getTextureCoords() {
        if (mTexCoordsDirty) {
            System.arraycopy(mTexCoords, 0, mCorrectedTexCoords, 0, mTexCoords.length);
            if (mTexture != null) {
                mTexture.correctTexCoords(mCorrectedTexCoords);
            }
            mTexCoordsDirty = false;
        }
        return mCorrectedTexCoords;
    }

    @Override
    public void render(Renderer r) {
        if (mTexture != null) {
            state = mTexture.applyTo(state);
        }
        try {
            r.addGeometry(vertices, getTextureCoords(), colours, indices, state);
        } catch (Exception e) {
            r.clear();
        }
    }

    @Override
    public int bytes() {
        return super.bytes() + (mTexCoords.length * 4);
    }

    @NonNull
    @Override
    public TexturedShape clone() {
        ColouredShape cs = super.mo81clone();
        float[] ntc = (float[]) mTexCoords.clone();
        return new TexturedShape(cs, ntc, mTexture);
    }
}
