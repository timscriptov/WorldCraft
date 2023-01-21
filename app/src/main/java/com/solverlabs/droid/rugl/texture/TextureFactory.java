package com.solverlabs.droid.rugl.texture;

import android.opengl.GLES10;
import android.opengl.GLException;
import android.util.Log;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.Game;
import com.solverlabs.droid.rugl.gl.BufferUtils;
import com.solverlabs.droid.rugl.gl.GLUtil;
import com.solverlabs.droid.rugl.gl.State;
import com.solverlabs.droid.rugl.util.RectanglePacker;
import com.solverlabs.droid.rugl.util.geom.Vector2f;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/* loaded from: classes.dex */
public class TextureFactory {
    private static int textureDimension;
    private static List<GLTexture> textures;
    private static Game.SurfaceListener textureFactoryListener = new Game.SurfaceListener() { // from class: com.solverlabs.droid.rugl.texture.TextureFactory.1
        @Override // com.solverlabs.droid.rugl.Game.SurfaceListener
        public void onSurfaceCreated() {
            TextureFactory.recreateTextures();
        }
    };

    static {
        Game.addSurfaceLIstener(textureFactoryListener);
        textures = new LinkedList();
        textureDimension = 512;
    }

    public static void removeListener() {
        Game.removeSurfaceListener(textureFactoryListener);
    }

    public static void recreateTextures() {
        if (!textures.isEmpty()) {
            IntBuffer texNames = BufferUtils.createIntBuffer(textures.size());
            for (GLTexture glt : textures) {
                texNames.put(glt.id);
            }
            texNames.flip();
            GLES10.glDeleteTextures(textures.size(), texNames);
            for (GLTexture glt2 : textures) {
                glt2.recreate();
            }
        }
        GLUtil.checkGLError();
    }

    public static List<GLTexture> getTextures() {
        return Collections.unmodifiableList(textures);
    }

    public static Texture buildTexture(Image image, boolean lonesome, boolean mipmap) {
        Texture t;
        if (lonesome) {
            try {
                GLTexture parent = new GLTexture(image.width, image.height, image.format, mipmap, lonesome ? 0 : 1);
                textures.add(parent);
                Texture texture = parent.addImage(image);
                Log.i(Game.RUGL_TAG, "Texture uploaded " + texture + " to " + parent);
                return texture;
            } catch (GLException e) {
                Log.e(Game.RUGL_TAG, "Problem creating texture", e);
                return null;
            }
        }
        for (GLTexture tex : textures) {
            if (tex.mipmap == mipmap && (t = tex.addImage(image)) != null) {
                return t;
            }
        }
        try {
            GLTexture parent2 = new GLTexture(textureDimension, textureDimension, image.format, mipmap, 1);
            textures.add(parent2);
            return parent2.addImage(image);
        } catch (GLException e2) {
            Log.e(Game.RUGL_TAG, "Problem creating texture", e2);
            return null;
        }
    }

    public static GLTexture createTexture(int minWidth, int minHeight, Image.Format format, boolean mipmap, int border) {
        try {
            GLTexture parent = new GLTexture(minWidth, minHeight, format, mipmap, border);
            textures.add(parent);
            return parent;
        } catch (GLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean deleteTexture(Texture t) {
        for (GLTexture tex : textures) {
            if (tex.release(t)) {
                return true;
            }
        }
        return false;
    }

    public static boolean deleteAllTextures() {
        return !textures.isEmpty() && textures.get(0).releaseAll();
    }

    public static void clear() {
        textures.clear();
    }

    @NonNull
    public static String getStateString() {
        StringBuilder buff = new StringBuilder("TextureFactory state");
        buff.append("\n\t").append(textures.size()).append(" GL texture objects");
        for (GLTexture glt : textures) {
            buff.append("\n").append(glt.toString());
        }
        return buff.toString();
    }

    /* loaded from: classes.dex */
    public static class GLTexture {
        public final Image.Format format;
        public final int height;
        public final boolean mipmap;
        public final int width;
        private final RectanglePacker<Image> packer;
        private final List<Texture> residentTextures;
        private int id;
        private Texture pan;

        private GLTexture(int width, int height, Image.Format format, boolean mipmap, int border) {
            this.residentTextures = new LinkedList();
            this.pan = null;
            if (width < 1 || height < 1) {
                this.width = TextureFactory.textureDimension;
                this.height = TextureFactory.textureDimension;
            } else {
                this.width = GLUtil.nextPowerOf2(width);
                this.height = GLUtil.nextPowerOf2(height);
            }
            this.format = format;
            this.mipmap = mipmap;
            this.packer = new RectanglePacker<>(width, height, border);
            recreate();
        }

        public void recreate() {
            try {
                int[] ib = new int[1];
                GLES10.glGenTextures(1, ib, 0);
                this.id = ib[0];
                State.getCurrentState().withTexture(this.id).apply();
                ByteBuffer data = BufferUtils.createByteBuffer(this.width * this.height * this.format.bytes);
                GLES10.glPixelStorei(3317, this.format.bytes);
                if (this.mipmap) {
                    GLES10.glTexParameterf(3553, 33169, 1.0f);
                }
                GLES10.glTexImage2D(3553, 0, this.format.glFormat, this.width, this.height, 0, this.format.glFormat, 5121, data);
                GLES10.glDeleteTextures(1, data.asIntBuffer());
                for (Texture t : this.residentTextures) {
                    RectanglePacker.Rectangle rpr = this.packer.findRectangle(t.sourceImage);
                    if (rpr != null) {
                        writeToTexture(rpr, t.sourceImage);
                    }
                }
                GLUtil.checkGLError();
            } catch (OutOfMemoryError e) {
                Log.d("OutOfMemory", "width:  " + this.width + "  height:  " + this.height);
            }
        }

        public int id() {
            return this.id;
        }

        public List<Texture> getResidents() {
            return Collections.unmodifiableList(this.residentTextures);
        }

        public Texture getTexture() {
            if (this.pan == null) {
                this.pan = new Texture(this);
            }
            return this.pan;
        }

        public Texture addImage(Image image) {
            RectanglePacker.Rectangle rpr = this.packer.insert(image.width, image.height, image);
            if (rpr != null) {
                writeToTexture(rpr, image);
                Vector2f bottomleft = new Vector2f(rpr.x, rpr.y);
                Vector2f topRight = new Vector2f(rpr.x + rpr.width, rpr.y + rpr.height);
                bottomleft.x /= this.width;
                bottomleft.y /= this.height;
                topRight.x /= this.width;
                topRight.y /= this.height;
                Texture t = new Texture(this, bottomleft, topRight, image);
                this.residentTextures.add(t);
                return t;
            }
            return null;
        }

        public boolean release(Texture t) {
            if (this.residentTextures.remove(t)) {
                this.packer.remove(t.sourceImage);
                return true;
            }
            return false;
        }

        public boolean releaseAll() {
            Iterator<Texture> i$ = this.residentTextures.iterator();
            if (i$.hasNext()) {
                Texture tex = i$.next();
                if (this.residentTextures.remove(tex)) {
                    this.packer.remove(tex.sourceImage);
                    return true;
                }
                return false;
            }
            return false;
        }

        private void writeToTexture(@NonNull RectanglePacker.Rectangle r, @NonNull Image image) {
            State.getCurrentState().withTexture(this.id).apply();
            image.writeToTexture(r.x, r.y);
            GLUtil.checkGLError();
        }

        @NonNull
        public String toString() {
            StringBuilder buff = new StringBuilder("GLTexture id = ");
            buff.append(this.id);
            buff.append(" format = ");
            buff.append(this.format);
            buff.append(" mimap = ");
            buff.append(this.mipmap);
            buff.append(" size = [");
            buff.append(this.width);
            buff.append(",");
            buff.append(this.height);
            buff.append("]");
            buff.append(" residents: ");
            buff.append(this.residentTextures.size());
            if (this.residentTextures.size() < 10) {
                for (Texture t : this.residentTextures) {
                    buff.append("\n\t");
                    buff.append(t.toString());
                }
            }
            return buff.toString();
        }
    }
}
