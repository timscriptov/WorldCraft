package com.solverlabs.droid.rugl.texture;

import android.graphics.Bitmap;
import android.opengl.GLES10;
import android.opengl.GLUtils;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.gl.GLUtil;


public class BitmapImage extends Image {
    public final Bitmap bitmap;

    public BitmapImage(@NonNull Bitmap b) {
        super(b.getWidth(), b.getHeight(), Image.Format.RGBA);
        this.bitmap = b;
    }

    @Override
    public void writeToTexture(int x, int y) {
        GLES10.glPixelStorei(3317, this.format.bytes);
        if (!this.bitmap.isRecycled()) {
            GLUtils.texSubImage2D(3553, 0, x, y, this.bitmap);
        }
        GLUtil.checkGLError();
    }
}
