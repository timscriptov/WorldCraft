package com.mcal.droid.rugl.geom;

import androidx.annotation.NonNull;

import com.mcal.droid.rugl.gl.Renderer;
import com.mcal.droid.rugl.texture.Texture;


public class TexturedShape extends ColouredShape {
    public final Texture texture;
    protected final float[] correctedTexCoords;
    public float[] texCoords;
    public boolean texCoordsDirty;

    public TexturedShape(ColouredShape shape, @NonNull float[] texCoords, Texture texture) {
        super(shape);
        this.texCoordsDirty = true;
        this.texCoords = texCoords;
        this.texture = texture;
        this.correctedTexCoords = texCoords.clone();
        if (texture != null) {
            this.state = texture.applyTo(this.state);
        }
        sanity();
    }

    public TexturedShape(TexturedShape ts) {
        super(ts);
        this.texCoordsDirty = true;
        this.texCoords = ts.texCoords;
        this.texture = ts.texture;
        this.correctedTexCoords = ts.correctedTexCoords;
        if (this.texture != null) {
            this.state = this.texture.applyTo(this.state);
        }
    }

    private void sanity() throws IllegalArgumentException, IllegalStateException {
        if (this.texCoords.length != vertexCount() * 2) {
            throw new IllegalArgumentException("Texture coordinate count mismatch\n" + this);
        }
        if (this.texCoords.length != this.correctedTexCoords.length) {
            throw new IllegalStateException("wat");
        }
        if (this.texCoords == this.correctedTexCoords) {
            throw new IllegalStateException("this is a poor idea");
        }
    }

    public float[] getTextureCoords() {
        if (this.texCoordsDirty) {
            System.arraycopy(this.texCoords, 0, this.correctedTexCoords, 0, this.texCoords.length);
            if (this.texture != null) {
                this.texture.correctTexCoords(this.correctedTexCoords);
            }
            this.texCoordsDirty = false;
        }
        return this.correctedTexCoords;
    }

    @Override
    public void render(Renderer r) {
        if (this.texture != null) {
            this.state = this.texture.applyTo(this.state);
        }
        try {
            r.addGeometry(this.vertices, getTextureCoords(), this.colours, this.indices, this.state);
        } catch (Exception e) {
            r.clear();
        }
    }

    @Override
    public int bytes() {
        return super.bytes() + (this.texCoords.length * 4);
    }

    @NonNull
    @Override
    public TexturedShape clone() {
        ColouredShape cs = super.clone();
        float[] ntc = this.texCoords.clone();
        return new TexturedShape(cs, ntc, this.texture);
    }
}
