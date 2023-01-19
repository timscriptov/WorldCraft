package com.solverlabs.droid.rugl.res;

import android.util.Log;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.Game;
import com.solverlabs.droid.rugl.text.Font;

import java.io.IOException;
import java.io.InputStream;


public abstract class FontLoader extends ResourceLoader.Loader<Font> {
    private final boolean mMipmap;
    private final int mResourceID;

    public FontLoader(int resourceID, boolean mipmap) {
        mResourceID = resourceID;
        mMipmap = mipmap;
    }

    public abstract void fontLoaded();

    @Override
    public void load() {
        InputStream is = ResourceLoader.mResources.openRawResource(mResourceID);
        try {
            resource = new Font(is);
        } catch (IOException e) {
            exception = e;
            Log.e(Game.RUGL_TAG, "Problem loading font " + mResourceID, e);
        }
    }

    @Override
    public final void complete() {
        if (resource != null) {
            ((Font) resource).init(mMipmap);
            fontLoaded();
        }
    }

    @NonNull
    public String toString() {
        return "Font loader " + mResourceID + " mipmap = " + mMipmap;
    }
}
