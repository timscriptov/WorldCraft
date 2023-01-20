package com.solverlabs.droid.rugl.res;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.texture.BitmapImage;
import com.solverlabs.worldcraft.MyApplication;
import com.solverlabs.worldcraft.factories.DescriptionFactory;

import java.io.IOException;
import java.io.InputStream;


public abstract class BitmapLoader extends ResourceLoader.Loader<BitmapImage> {
    private static final BitmapFactory.Options opts = new BitmapFactory.Options();

    static {
        opts.inDither = false;
        opts.inPurgeable = true;
        opts.inInputShareable = true;
        opts.inTempStorage = new byte[32768];
    }

    private final int id;


    public BitmapLoader(int id) {
        this.id = id;
    }


    /**
     * Retrieve a bitmap from assets.
     *
     * @param mgr  The {@link AssetManager} obtained via {@link Context#getAssets()}
     * @param path The path to the asset.
     * @return The {@link Bitmap} or {@code null} if we failed to decode the file.
     */
    public static Bitmap getBitmapFromAsset(@NonNull AssetManager mgr, String path) {
        InputStream is = null;
        Bitmap bitmap = null;
        try {
            is = mgr.open(path);
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
        resource = new BitmapImage(BitmapFactory.decodeResource(ResourceLoader.resources, this.id, opts));

    }

    @NonNull
    public String toString() {
        return "Bitmap id = " + id + (resource != null ? " " + resource.width + "x" + resource.height : DescriptionFactory.emptyText);
    }
}
