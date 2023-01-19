package com.solverlabs.droid.rugl.util;

import android.graphics.Bitmap;
import android.opengl.GLES10;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.IntBuffer;


public class ScreenShot {
    private static Bitmap savePixels(int x, int y, int w, int h) {
        int[] b = new int[w * h];
        int[] bt = new int[w * h];
        IntBuffer ib = IntBuffer.wrap(b);
        ib.position(0);
        GLES10.glReadPixels(x, y, w, h, 6408, 5121, ib);
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                int pix = b[(i * w) + j];
                int pb = (pix >> 16) & 255;
                int pr = (pix << 16) & 16711680;
                int pix1 = ((-16711936) & pix) | pr | pb;
                bt[(((h - i) - 1) * w) + j] = pix1;
            }
        }
        Bitmap sb = Bitmap.createBitmap(bt, w, h, Bitmap.Config.ARGB_8888);
        return sb;
    }

    public static void savePNG(int x, int y, int w, int h, File output) {
        Bitmap bmp = savePixels(x, y, w, h);
        try {
            FileOutputStream fos = new FileOutputStream(output);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
