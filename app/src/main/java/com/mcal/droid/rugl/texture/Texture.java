package com.mcal.droid.rugl.texture;

import android.graphics.Point;

import com.mcal.droid.rugl.gl.State;
import com.mcal.droid.rugl.gl.enums.MinFilter;
import com.mcal.droid.rugl.gl.facets.TextureState;
import com.mcal.droid.rugl.util.geom.Vector2f;


/**
 * It's a texture, residing inside a larger texture
 */
public class Texture {
    /**
     * The parent texture
     */
    public final TextureFactory.GLTexture parent;
    /**
     * The source of the texture data
     */
    public final Image sourceImage;
    /***/
    public final int width;
    /***/
    public final int height;
    /**
     * The bottom left corner of the texture, in texture coordinates
     */
    private final Vector2f origin;
    /**
     * The vector from the top bottom corner to the top right, in
     * texture coordinates
     */
    private final Vector2f extent;
    /**
     * The bottom left corner, in pixels
     */
    private final Point pixelOrigin;

    /**
     * Constructs a new Texture
     *
     * @param parent
     * @param bottomLeft
     * @param topRight
     * @param source
     */
    Texture(TextureFactory.GLTexture parent, Vector2f bottomLeft, Vector2f topRight, Image source) {
        this.parent = parent;
        origin = bottomLeft;
        extent = Vector2f.sub(topRight, bottomLeft, null);
        sourceImage = source;
        width = sourceImage.width;
        height = sourceImage.height;
        pixelOrigin =
                new Point((int) (origin.x * parent.width),
                        (int) (origin.y * parent.height));
    }

    /**
     * Constructs a new Texture
     *
     * @param parent
     */
    Texture(TextureFactory.GLTexture parent) {
        this.parent = parent;
        origin = new Vector2f(0, 0);
        extent = new Vector2f(1, 1);
        sourceImage = null;
        width = parent.width;
        height = parent.height;

        pixelOrigin =
                new Point((int) (origin.x * parent.width),
                        (int) (origin.y * parent.height));
    }

    /**
     * Applies this texture to a rendering {@link State}
     *
     * @param state
     * @return The altered state
     */
    public State applyTo(State state) {
        if (state.texture.id != parent.id()) {
            state = state.with(state.texture.with(parent.id()));
        }

        if (!parent.mipmap) {
            // no mipmaps, so try for the closest compatible filter mode
            TextureState.Filters f = state.texture.filter;
            if (f.min == MinFilter.LINEAR_MIPMAP_LINEAR
                    || f.min == MinFilter.LINEAR_MIPMAP_NEAREST) {
                f = f.with(MinFilter.LINEAR);
            } else if (f.min == MinFilter.NEAREST_MIPMAP_LINEAR
                    || f.min == MinFilter.NEAREST_MIPMAP_NEAREST) {
                f = f.with(MinFilter.NEAREST);
            }

            if (f != state.texture.filter) {
                state = state.with(state.texture.with(f));
            }
        }

        return state;
    }

    /**
     * Translates s and t coordinates into values that can be passed to
     * openGL.
     *
     * @param s    The desired s coordinate, in range 0 to 1
     * @param t    The desired t coordinate, in range 0 to 1
     * @param dest A vector2f in which to store the result, or null
     * @return The texture coordinates in terms of the containing
     * openGL texture
     */
    public Vector2f getTexCoords(float s, float t, Vector2f dest) {
        if (dest == null) {
            dest = new Vector2f();
        }

        dest.x = origin.x + extent.x * s;
        dest.y = origin.y + extent.y * t;

        return dest;
    }

    /**
     * Translates s and t coordinates into values that can be passed to
     * openGL.
     *
     * @param coords The desired texture coordinates, as if the texture
     *               used up a whole openGL texture
     * @param dest   A vector2f in which to store the result, or null
     * @return The texture coordinates in terms of the containing
     * openGL texture
     */
    public Vector2f getTexCoords(Vector2f coords, Vector2f dest) {
        return getTexCoords(coords.x, coords.y, dest);
    }

    /**
     * Adjusts the supplied texture coordinates (which are in terms of
     * the entire texture object to point to this subtexture
     *
     * @param texCoords
     */
    public void correctTexCoords(float[] texCoords) {
        if (origin.x != 0 || origin.y != 0 || extent.x != 1 || extent.y != 1) {
            for (int i = 0; i < texCoords.length; i += 2) {
                texCoords[i] = origin.x + extent.x * texCoords[i];
                texCoords[i + 1] = origin.y + extent.y * texCoords[i + 1];
            }
        }
    }

    /**
     * @return the x position of this texture within the parent
     * texture, in pixels
     */
    public int getXPosition() {
        return pixelOrigin.x;
    }

    /**
     * @return the y position of this texture within the parent
     * texture, in pixels
     */
    public int getYPosition() {
        return pixelOrigin.y;
    }

    @Override
    public String toString() {
        return width + "x" + height + " @ (" + pixelOrigin.x + ", " + pixelOrigin.y + ")";
    }
}