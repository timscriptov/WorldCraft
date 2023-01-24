package com.solverlabs.worldcraft.framework.io;

import android.content.res.AssetManager;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AndroidFileIO implements FileIO {
    AssetManager assets;
    String externalStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();

    public AndroidFileIO(AssetManager assets) {
        this.assets = assets;
    }

    @Override
    public InputStream readAsset(String fileName) throws IOException {
        return assets.open(fileName);
    }

    @Override
    public InputStream readFile(String fileName) throws IOException {
        return new FileInputStream(new File(externalStoragePath, fileName));
    }

    @Override
    public OutputStream writeFile(String fileName) throws IOException {
        return new FileOutputStream(new File(externalStoragePath, fileName));
    }
}
