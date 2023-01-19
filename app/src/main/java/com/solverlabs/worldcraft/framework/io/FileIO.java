package com.solverlabs.worldcraft.framework.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public interface FileIO {
    InputStream readAsset(String str) throws IOException;

    InputStream readFile(String str) throws IOException;

    OutputStream writeFile(String str) throws IOException;
}
