package com.solverlabs.droid.rugl.res;

import android.util.Log;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.Game;
import com.solverlabs.droid.rugl.text.Font;

import java.io.IOException;
import java.io.InputStream;


public abstract class FontLoader extends ResourceLoader.Loader<Font> {
    private final boolean mipmap;
    private final int resourceID;

    public FontLoader(int resourceID, boolean mipmap) {
        this.resourceID = resourceID;
        this.mipmap = mipmap;
    }

    public abstract void fontLoaded();

    @Override
    public void load() {
        InputStream is = ResourceLoader.resources.openRawResource(resourceID);
        try {
            resource = new Font(is);
        } catch (IOException e) {
            exception = e;
            Log.e(Game.RUGL_TAG, "Problem loading font " + resourceID, e);
        }
    }

    @Override
    public final void complete() {
        if (resource != null) {
            resource.init(mipmap);
            fontLoaded();
        }
    }

    @NonNull
    public String toString() {
        return "Font loader " + resourceID + " mipmap = " + mipmap;
    }
}
