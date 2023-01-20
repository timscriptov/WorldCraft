package com.solverlabs.worldcraft.nbt;

import com.solverlabs.droid.rugl.res.ResourceLoader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public abstract class TagLoader extends ResourceLoader.Loader<Tag> {
    private final File f;

    public TagLoader(File file) {
        this.f = file;
    }

    @Override
    public void load() {
        try {
            this.resource = Tag.readFrom(new FileInputStream(this.f), true);
        } catch (IOException e) {
            this.exception = e;
        }
    }
}