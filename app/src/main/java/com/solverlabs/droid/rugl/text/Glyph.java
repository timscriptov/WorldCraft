package com.solverlabs.droid.rugl.text;

import com.solverlabs.droid.rugl.util.geom.Vector2f;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;


public final class Glyph {
    static final /* synthetic */ boolean $assertionsDisabled;

    static {
        $assertionsDisabled = !Glyph.class.desiredAssertionStatus();
    }

    public final float advance;
    public final char character;
    public final GlyphImage image;
    private final Vector2f glyphOffset;
    private float[] kerning;
    private char[] kernsWith;

    public Glyph(char character, GlyphImage image, Vector2f origin, float advance, char[] kernsWith, float[] kerning) {
        this.character = character;
        this.image = image;
        this.glyphOffset = origin;
        this.advance = advance;
        this.kernsWith = kernsWith;
        this.kerning = kerning;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Glyph(ByteBuffer data, GlyphImage[] images) {
        this.character = data.getChar();
        GlyphImage im = null;
        int i = 0;
        while (true) {
            if (i < images.length) {
                if (!images[i].represents(this.character)) {
                    i++;
                } else {
                    im = images[i];
                    break;
                }
            } else {
                break;
            }
        }
        if ($assertionsDisabled || im != null) {
            this.image = im;
            this.advance = data.getFloat();
            this.glyphOffset = new Vector2f(data.getFloat(), data.getFloat());
            this.kernsWith = new char[data.getInt()];
            this.kerning = new float[this.kernsWith.length];
            for (int i2 = 0; i2 < this.kernsWith.length; i2++) {
                this.kernsWith[i2] = data.getChar();
                this.kerning[i2] = data.getFloat();
            }
            return;
        }
        throw new AssertionError();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Glyph(InputStream is, GlyphImage[] images) throws IOException {
        DataInputStream dis = new DataInputStream(is);
        this.character = dis.readChar();
        GlyphImage im = null;
        int i = 0;
        while (true) {
            if (i < images.length) {
                if (!images[i].represents(this.character)) {
                    i++;
                } else {
                    im = images[i];
                    break;
                }
            } else {
                break;
            }
        }
        if ($assertionsDisabled || im != null) {
            this.image = im;
            this.advance = dis.readFloat();
            this.glyphOffset = new Vector2f(dis.readFloat(), dis.readFloat());
            int kc = dis.readInt();
            this.kernsWith = new char[kc];
            this.kerning = new float[kc];
            for (int i2 = 0; i2 < kc; i2++) {
                this.kernsWith[i2] = dis.readChar();
                this.kerning[i2] = dis.readFloat();
            }
            return;
        }
        throw new AssertionError();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void write(ByteBuffer data) {
        data.putChar(this.character);
        data.putFloat(this.advance);
        data.putFloat(this.glyphOffset.x);
        data.putFloat(this.glyphOffset.y);
        if (this.kernsWith == null) {
            data.putInt(0);
            return;
        }
        data.putInt(this.kernsWith.length);
        for (int i = 0; i < this.kernsWith.length; i++) {
            data.putChar(this.kernsWith[i]);
            data.putFloat(this.kerning[i]);
        }
    }

    public int dataSize() {
        int size = 0 + 2;
        int size2 = size + 4 + 8 + 4;
        if (this.kernsWith != null) {
            int size3 = (this.kernsWith.length * 6) + 18;
            return size3;
        }
        return size2;
    }

    public float getKerningAfter(char g) {
        int i;
        if (this.kernsWith.length != 0 && (i = Arrays.binarySearch(this.kernsWith, g)) >= 0 && i < this.kerning.length && this.kernsWith[i] == g) {
            return this.kerning[i];
        }
        return 0.0f;
    }

    public Vector2f getGlyphOffset(Vector2f dest) {
        if (dest == null) {
            dest = new Vector2f();
        }
        dest.set(this.glyphOffset);
        return dest;
    }

    public void updateKerning(char c, float k) {
        if (k != 0.0f) {
            int insertion = Arrays.binarySearch(this.kernsWith, c);
            if (!$assertionsDisabled && insertion >= 0) {
                throw new AssertionError();
            }
            int insertion2 = -(insertion + 1);
            char[] newKernsWith = new char[this.kernsWith.length + 1];
            float[] newKerning = new float[this.kerning.length + 1];
            System.arraycopy(this.kernsWith, 0, newKernsWith, 0, insertion2);
            System.arraycopy(this.kerning, 0, newKerning, 0, insertion2);
            newKernsWith[insertion2] = c;
            newKerning[insertion2] = k;
            if (insertion2 < newKernsWith.length) {
                System.arraycopy(this.kernsWith, insertion2, newKernsWith, insertion2 + 1, this.kernsWith.length - insertion2);
                System.arraycopy(this.kerning, insertion2, newKerning, insertion2 + 1, this.kerning.length - insertion2);
            }
            this.kernsWith = newKernsWith;
            this.kerning = newKerning;
        }
    }

    public String toString() {
        StringBuilder buff = new StringBuilder("Glyph '" + this.character + "' adv = " + this.advance + " origin = " + this.glyphOffset + " size = " + this.image.image.width + "x" + this.image.image.height);
        if (this.kernsWith.length > 0) {
            buff.append("\n\t\tKerning : ");
            for (int i = 0; i < this.kernsWith.length; i++) {
                buff.append(" ");
                buff.append(this.kernsWith[i]);
                buff.append("=");
                buff.append(this.kerning[i]);
            }
        }
        return buff.toString();
    }
}
