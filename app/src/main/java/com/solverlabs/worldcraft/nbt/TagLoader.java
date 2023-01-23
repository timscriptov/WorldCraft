package com.solverlabs.worldcraft.nbt;

import android.util.Log;

import com.solverlabs.droid.rugl.res.ResourceLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Loads a {@link Tag} file
 */
public abstract class TagLoader extends ResourceLoader.Loader<Tag> {
    private final File f;

    /**
     * @param file
     */
    public TagLoader(File file) {
        f = file;
    }

    @Override
    public void load() {
        try {
            resource = Tag.readFrom(new FileInputStream(f), true);
        } catch (IOException e) {
            Log.e(ResourceLoader.LOG_TAG, "Problem loading tag file", e);
            exception = e;
        }
    }
}
