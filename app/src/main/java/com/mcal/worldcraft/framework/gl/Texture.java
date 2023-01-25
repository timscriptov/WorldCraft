package com.mcal.worldcraft.framework.gl;

import static org.apache.commons.compress.archivers.tar.TarConstants.DEFAULT_BLKSIZE;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES10;
import android.opengl.GLUtils;

import androidx.annotation.NonNull;

import com.mcal.worldcraft.framework.io.FileIO;

import java.io.IOException;
import java.io.InputStream;

public class Texture {
    public int height;
    public int width;
    FileIO fileIO;
    String fileName;
    int magFilter;
    int minFilter;
    boolean mipmapped;
    int textureId;

    public Texture(FileIO fileIO, String fileName) {
        this(fileIO, fileName, false);
    }

    public Texture(FileIO fileIO, String fileName, boolean mipmapped) {
        this.fileIO = fileIO;
        this.fileName = fileName;
        this.mipmapped = mipmapped;
        load();
    }

    private void load() {
        int[] textureIds = new int[1];
        GLES10.glGenTextures(1, textureIds, 0);
        textureId = textureIds[0];
        System.out.println("load: " + textureId);
        InputStream in = null;
        try {
            try {
                InputStream in2 = fileIO.readAsset(fileName);
                Bitmap bitmap = BitmapFactory.decodeStream(in2);
                if (mipmapped) {
                    createMipmaps(bitmap);
                } else {
                    GLES10.glBindTexture(3553, textureId);
                    GLUtils.texImage2D(3553, 0, bitmap, 0);
                    setFilters(9728, 9728);
                    GLES10.glBindTexture(3553, 0);
                    width = bitmap.getWidth();
                    height = bitmap.getHeight();
                    bitmap.recycle();
                }
                if (in2 != null) {
                    try {
                        in2.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e2) {
                throw new RuntimeException("Couldn't load texture '" + this.fileName + "'", e2);
            }
        } catch (Throwable th) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            throw th;
        }
    }

    private void createMipmaps(@NonNull Bitmap bitmap) {
        GLES10.glBindTexture(3553, this.textureId);
        width = bitmap.getWidth();
        height = bitmap.getHeight();
        setFilters(9984, 9728);
        int level = 0;
        int newWidth = width;
        int newHeight = height;
        while (true) {
            GLUtils.texImage2D(3553, level, bitmap, 0);
            newWidth /= 2;
            newHeight /= 2;
            if (newWidth > 0) {
                Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
                bitmap.recycle();
                bitmap = newBitmap;
                level++;
            } else {
                GLES10.glBindTexture(3553, 0);
                bitmap.recycle();
                return;
            }
        }
    }

    public void reload() {
        load();
        bind();
        setFilters(minFilter, magFilter);
        GLES10.glBindTexture(3553, 0);
    }

    public void setFilters(int minFilter, int magFilter) {
        this.minFilter = minFilter;
        this.magFilter = magFilter;
        GLES10.glTexParameterf(3553, 10241, minFilter);
        GLES10.glTexParameterf(3553, DEFAULT_BLKSIZE, magFilter);
    }

    public void bind() {
        GLES10.glBindTexture(3553, textureId);
    }

    public void dispose() {
        GLES10.glBindTexture(3553, textureId);
        int[] textureIds = {textureId};
        GLES10.glDeleteTextures(1, textureIds, 0);
    }
}
