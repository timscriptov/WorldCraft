package com.solverlabs.droid.rugl.text;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.texture.BufferImage;
import com.solverlabs.droid.rugl.texture.Texture;
import com.solverlabs.droid.rugl.texture.TextureFactory;
import com.solverlabs.droid.rugl.util.geom.BoundingRectangle;
import com.solverlabs.droid.rugl.util.geom.Vector2f;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;


public class GlyphImage {
    public final char[] characters;
    public final BufferImage image;
    private transient Texture texture;
    private transient Vector2f texOrigin = new Vector2f();
    private transient Vector2f texExtent = new Vector2f();

    public GlyphImage(BufferImage image, char... characters) {
        this.image = image;
        this.characters = characters;
    }

    public GlyphImage(@NonNull ByteBuffer data) {
        this.characters = new char[data.getInt()];
        for (int i = 0; i < this.characters.length; i++) {
            this.characters[i] = data.getChar();
        }
        this.image = new BufferImage(data);
    }

    public GlyphImage(InputStream in) throws IOException {
        DataInputStream data = new DataInputStream(in);
        this.characters = new char[data.readInt()];
        for (int i = 0; i < this.characters.length; i++) {
            this.characters[i] = data.readChar();
        }
        this.image = new BufferImage(in);
    }

    public void write(@NonNull ByteBuffer data) {
        data.putInt(this.characters.length);
        for (int i = 0; i < this.characters.length; i++) {
            data.putChar(this.characters[i]);
        }
        this.image.write(data);
    }

    public boolean represents(char c) {
        for (int i = 0; i < this.characters.length; i++) {
            if (c == this.characters[i]) {
                return true;
            }
        }
        return false;
    }

    public boolean init(TextureFactory.GLTexture fontTexture) {
        if (this.texture == null) {
            this.texture = fontTexture.addImage(this.image);
            if (this.texture != null) {
                this.texOrigin.set(0.0f, 0.0f);
                this.texExtent.set(1.0f, 1.0f);
                this.texOrigin = this.texture.getTexCoords(this.texOrigin, this.texOrigin);
                this.texExtent = this.texture.getTexCoords(this.texExtent, this.texExtent);
            }
        }
        return this.texture != null;
    }

    public Vector2f getOrigin(Vector2f dest) {
        if (dest == null) {
            dest = new Vector2f();
        }
        dest.set(this.texOrigin);
        return dest;
    }

    public Vector2f getExtent(Vector2f dest) {
        if (dest == null) {
            dest = new Vector2f();
        }
        dest.set(this.texExtent);
        return dest;
    }

    public int dataSize() {
        return this.image.dataSize() + 4 + (this.characters.length * 2);
    }

    public void getSize(@NonNull BoundingRectangle b) {
        b.set(0.0f, this.image.width, 0.0f, this.image.height);
    }
}
