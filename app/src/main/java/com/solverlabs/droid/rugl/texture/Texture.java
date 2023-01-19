package com.solverlabs.droid.rugl.texture;

import android.graphics.Point;

import com.solverlabs.droid.rugl.gl.State;
import com.solverlabs.droid.rugl.gl.enums.MinFilter;
import com.solverlabs.droid.rugl.gl.facets.TextureState;
import com.solverlabs.droid.rugl.util.geom.Vector2f;


public class Texture {
    public final int height;
    public final TextureFactory.GLTexture parent;
    public final Image sourceImage;
    public final int width;
    private final Vector2f extent;
    private final Vector2f origin;
    private final Point pixelOrigin;

    /* JADX INFO: Access modifiers changed from: package-private */
    public Texture(TextureFactory.GLTexture parent, Vector2f bottomLeft, Vector2f topRight, Image source) {
        this.parent = parent;
        this.origin = bottomLeft;
        this.extent = Vector2f.sub(topRight, bottomLeft, null);
        this.sourceImage = source;
        this.width = this.sourceImage.width;
        this.height = this.sourceImage.height;
        this.pixelOrigin = new Point((int) (this.origin.x * parent.width), (int) (this.origin.y * parent.height));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Texture(TextureFactory.GLTexture parent) {
        this.parent = parent;
        this.origin = new Vector2f(0.0f, 0.0f);
        this.extent = new Vector2f(1.0f, 1.0f);
        this.sourceImage = null;
        this.width = parent.width;
        this.height = parent.height;
        this.pixelOrigin = new Point((int) (this.origin.x * parent.width), (int) (this.origin.y * parent.height));
    }

    public State applyTo(State state) {
        if (state.texture.id != this.parent.id()) {
            state = state.with(state.texture.with(this.parent.id()));
        }
        if (!this.parent.mipmap) {
            TextureState.Filters f = state.texture.filter;
            if (f.min == MinFilter.LINEAR_MIPMAP_LINEAR || f.min == MinFilter.LINEAR_MIPMAP_NEAREST) {
                f = f.with(MinFilter.LINEAR);
            } else if (f.min == MinFilter.NEAREST_MIPMAP_LINEAR || f.min == MinFilter.NEAREST_MIPMAP_NEAREST) {
                f = f.with(MinFilter.NEAREST);
            }
            if (f != state.texture.filter) {
                return state.with(state.texture.with(f));
            }
            return state;
        }
        return state;
    }

    public Vector2f getTexCoords(float s, float t, Vector2f dest) {
        if (dest == null) {
            dest = new Vector2f();
        }
        dest.x = this.origin.x + (this.extent.x * s);
        dest.y = this.origin.y + (this.extent.y * t);
        return dest;
    }

    public Vector2f getTexCoords(Vector2f coords, Vector2f dest) {
        return getTexCoords(coords.x, coords.y, dest);
    }

    public void correctTexCoords(float[] texCoords) {
        if (this.origin.x != 0.0f || this.origin.y != 0.0f || this.extent.x != 1.0f || this.extent.y != 1.0f) {
            for (int i = 0; i < texCoords.length; i += 2) {
                texCoords[i] = this.origin.x + (this.extent.x * texCoords[i]);
                texCoords[i + 1] = this.origin.y + (this.extent.y * texCoords[i + 1]);
            }
        }
    }

    public int getXPosition() {
        return this.pixelOrigin.x;
    }

    public int getYPosition() {
        return this.pixelOrigin.y;
    }

    public String toString() {
        return this.width + "x" + this.height + " @ (" + this.pixelOrigin.x + ", " + this.pixelOrigin.y + ")";
    }
}
