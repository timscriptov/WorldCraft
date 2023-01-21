package com.solverlabs.droid.rugl.res;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.GameApp;
import com.solverlabs.droid.rugl.texture.BitmapImage;

import java.io.IOException;
import java.io.InputStream;

/**
 * Loads a {@link BitmapImage}
 */
public abstract class BitmapLoader extends ResourceLoader.Loader<BitmapImage> {
    private final String imagePath;

    /**
     * @param path
     */
    public BitmapLoader(String path) {
        this.imagePath = path;
    }

    /**
     * Retrieve a bitmap from assets.
     *
     * @param path The path to the asset.
     * @return The {@link Bitmap} or {@code null} if we failed to decode the file.
     */
    public static Bitmap getBitmapFromAsset(String path) {
        InputStream is = null;
        Bitmap bitmap = null;
        try {
            is = GameApp.getContext().getAssets().open(path);
            bitmap = BitmapFactory.decodeStream(is);
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bitmap;
    }

    @Override
    public void load() {
        resource = new BitmapImage(getBitmapFromAsset(imagePath));
    }

    @NonNull
    @Override
    public String toString() {
        return "Image path = " + imagePath
                + (resource != null ? " " + resource.width + "x" + resource.height : "");
    }
}