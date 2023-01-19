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

    private final Bitmap mBitmap;

    public BitmapLoader(String name) {
        mBitmap = getBitmapFromAsset(MyApplication.getContext().getAssets(), name);
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
        resource = new BitmapImage(mBitmap);
    }

    @NonNull
    public String toString() {
        return "Bitmap id = " + mBitmap + (resource != null ? " " + ((BitmapImage) resource).width + "x" + ((BitmapImage) resource).height : DescriptionFactory.emptyText);
    }
}
