package com.solverlabs.droid.rugl.text;

import com.solverlabs.droid.rugl.util.geom.Vector2f;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;


/**
 * A single glyph in a Font. This class started life in the SPGL, but
 * has been extensively refactored - converted to floating point etc
 */
public final class Glyph {
    /**
     * The character that this glyph represents
     */
    public final char character;

    /**
     * The glyph image
     */
    public final GlyphImage image;

    /**
     * Glyph advance. The distance from the start of this character to
     * the start of the next character, disregarding kerning
     */
    public final float advance;

    /**
     * The offset from the baseline of the character to where to draw
     * the glyph quad
     */
    private final Vector2f glyphOffset;

    /**
     * The characters that we kern with when placed after
     */
    private char[] kernsWith;

    /**
     * The corresponding kerning values
     */
    private float[] kerning;

    /**
     * @param character The character that this glyph represents
     * @param image     the glyph image
     * @param origin    The offset from the glyph's baseline to the origin of
     *                  the glyph
     * @param advance
     * @param kernsWith
     * @param kerning
     */
    public Glyph(char character, GlyphImage image, Vector2f origin, float advance,
                 char[] kernsWith, float[] kerning) {
        this.character = character;
        this.image = image;

        glyphOffset = origin;

        this.advance = advance;
        this.kernsWith = kernsWith;
        this.kerning = kerning;
    }

    /**
     * Reads a {@link Glyph} from a buffer
     *
     * @param data   the buffer
     * @param images An array of {@link GlyphImage}s to choose from
     */
    Glyph(ByteBuffer data, GlyphImage[] images) {
        character = data.getChar();

        GlyphImage im = null;
        for (int i = 0; i < images.length; i++) {
            if (images[i].represents(character)) {
                im = images[i];
                break;
            }
        }
        assert im != null;
        image = im;

        advance = data.getFloat();
        glyphOffset = new Vector2f(data.getFloat(), data.getFloat());

        kernsWith = new char[data.getInt()];
        kerning = new float[kernsWith.length];
        for (int i = 0; i < kernsWith.length; i++) {
            kernsWith[i] = data.getChar();
            kerning[i] = data.getFloat();
        }
    }

    /**
     * Reads a {@link Glyph} from a stream
     *
     * @param is
     * @param images An array of {@link GlyphImage}s to choose from
     * @throws IOException
     */
    Glyph(InputStream is, GlyphImage[] images) throws IOException {
        DataInputStream dis = new DataInputStream(is);

        character = dis.readChar();

        GlyphImage im = null;
        for (int i = 0; i < images.length; i++) {
            if (images[i].represents(character)) {
                im = images[i];
                break;
            }
        }
        assert im != null;
        image = im;

        advance = dis.readFloat();
        glyphOffset = new Vector2f(dis.readFloat(), dis.readFloat());

        int kc = dis.readInt();
        kernsWith = new char[kc];
        kerning = new float[kc];

        for (int i = 0; i < kc; i++) {
            kernsWith[i] = dis.readChar();
            kerning[i] = dis.readFloat();
        }
    }

    /**
     * Stores a glyph in a buffer
     *
     * @param data the buffer
     */
    void write(ByteBuffer data) {
        data.putChar(character);

        data.putFloat(advance);
        data.putFloat(glyphOffset.x);
        data.putFloat(glyphOffset.y);

        if (kernsWith == null) {
            data.putInt(0);
        } else {
            data.putInt(kernsWith.length);
            for (int i = 0; i < kernsWith.length; i++) {
                data.putChar(kernsWith[i]);
                data.putFloat(kerning[i]);
            }
        }
    }

    /**
     * Calculates the size of the buffer needed to store this glyph, in
     * bytes
     *
     * @return The number of bytes needed to store this glyph
     */
    public int dataSize() {
        int size = 0;

        // char
        size += 2;

        // advance
        size += 4;

        // origin
        size += 2 * 4;

        // kerns size
        size += 4;

        // kerns table
        if (kernsWith != null) {
            size += kernsWith.length * (2 + 4);
        }

        return size;
    }

    /**
     * If we have just laid out a glyph, <code>g</code>, and we want to
     * lay out this glyph next to it, this function will return the
     * required kerning to do so. For example, the kerning value for
     * laying a 'A' after a 'W' is likely to be some negative number.
     * Add it to 'W''s advance to get the origin of the 'A' glyph
     *
     * @param g The glyph immediately preceding this one
     * @return The horizontal offset to apply to the advance.
     */
    public float getKerningAfter(char g) {
        if (kernsWith.length == 0) {
            return 0;
        }

        int i = Arrays.binarySearch(kernsWith, g);
        if (i < 0 || i >= kerning.length) {
            return 0;
        } else {
            if (kernsWith[i] != g) {
                return 0;
            } else {
                return kerning[i];
            }
        }
    }

    /**
     * Gets the offset from the character's baseline to the bottom-left
     * corner of the textured quad used to render the glyph
     *
     * @param dest The {@link Vector2f} in which to store the results, or
     *             null to construct a new {@link Vector2f}
     * @return The baseline-quad offset
     */
    public Vector2f getGlyphOffset(Vector2f dest) {
        if (dest == null) {
            dest = new Vector2f();
        }

        dest.set(glyphOffset);

        return dest;
    }

    /**
     * Updates this {@link Glyph}'s kerning table
     *
     * @param c The preceding character
     * @param k The kerning value when this glyph succeeds c
     */
    public void updateKerning(char c, float k) {
        if (k != 0) {
            int insertion = Arrays.binarySearch(kernsWith, c);

            assert insertion < 0;

            insertion += 1;
            insertion = -insertion;

            char[] newKernsWith = new char[kernsWith.length + 1];
            float[] newKerning = new float[kerning.length + 1];

            System.arraycopy(kernsWith, 0, newKernsWith, 0, insertion);
            System.arraycopy(kerning, 0, newKerning, 0, insertion);

            newKernsWith[insertion] = c;
            newKerning[insertion] = k;

            if (insertion < newKernsWith.length) {
                System.arraycopy(kernsWith, insertion, newKernsWith, insertion + 1,
                        kernsWith.length - insertion);
                System.arraycopy(kerning, insertion, newKerning, insertion + 1,
                        kerning.length - insertion);
            }

            kernsWith = newKernsWith;
            kerning = newKerning;
        }
    }

    @Override
    public String toString() {
        StringBuilder buff =
                new StringBuilder("Glyph \'" + character + "\' adv = " + advance
                        + " origin = " + glyphOffset + " size = " + image.image.width + "x"
                        + image.image.height);
        if (kernsWith.length > 0) {
            buff.append("\n\t\tKerning : ");
            for (int i = 0; i < kernsWith.length; i++) {
                buff.append(" ");
                buff.append(kernsWith[i]);
                buff.append("=");
                buff.append(kerning[i]);
            }
        }

        return buff.toString();
    }
}
