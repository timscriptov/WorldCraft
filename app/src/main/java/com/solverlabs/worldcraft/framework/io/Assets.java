package com.solverlabs.worldcraft.framework.io;

import com.solverlabs.worldcraft.framework.gl.Texture;
import com.solverlabs.worldcraft.framework.gl.Vertices3;
import com.solverlabs.worldcraft.framework.loader.ObjLoader;


public class Assets {
    public static Texture turkeyTexture;
    private static int currentFrame;
    private static float currentTime;
    private static Vertices3[] turkeyModel;

    public static void load(FileIO fileIO) {
        turkeyTexture = new Texture(fileIO, "turkey.png", true);
        turkeyModel = new Vertices3[42];
        for (int i = 1; i <= 42; i++) {
            turkeyModel[i - 1] = ObjLoader.load(fileIO, "turkey/" + i + ".obj");
        }
    }

    public static Vertices3 getTurkeyModel(long delta) {
        return turkeyModel[currentFrame];
    }

    public static void reload() {
        turkeyTexture.reload();
    }

    public static void updateTurkey(float delta) {
        currentTime += delta;
        currentTime -= (int) currentTime;
        currentFrame = (int) (currentTime * 42.0f);
    }
}
