package com.solverlabs.worldcraft.mob.turkey;

import android.opengl.GLES10;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.gl.GLUtil;
import com.solverlabs.worldcraft.framework.gl.Vertices3;
import com.solverlabs.worldcraft.framework.io.Assets;

public class TurkeyView {
    static float[] ambient = {0.2f, 0.2f, 0.2f, 1.0f};
    static float[] diffuse = {1.0f, 1.0f, 1.0f, 1.0f};
    static float[] specular = {0.0f, 0.0f, 0.0f, 0.0f};
    static float[] direction = {0.0f, 0.0f, -1.0f, 0.0f};

    public static void advance(float delta) {
        Assets.updateTurkey(delta);
    }

    public static void render(@NonNull Turkey turkey) {
        GLES10.glDisableClientState(32886);
        GLES10.glEnable(3553);
        GLES10.glEnable(2903);
        Vertices3 turkeyModel = Assets.getTurkeyModel(0L);
        Assets.turkeyTexture.setFilters(9728, 9728);
        Assets.turkeyTexture.bind();
        turkeyModel.bind();
        GLES10.glPushMatrix();
        GLES10.glTranslatef(turkey.position.x, turkey.position.y, turkey.position.z);
        GLES10.glScalef(0.5f, 0.5f, 0.5f);
        GLES10.glRotatef(-90.0f, 1.0f, 0.0f, 0.0f);
        turkeyModel.draw(4, 0, turkeyModel.getNumVertices());
        GLES10.glPopMatrix();
        turkeyModel.unbind();
        GLES10.glDisable(3553);
        GLES10.glDisable(2903);
        GLES10.glEnableClientState(32886);
        GLUtil.checkGLError();
    }

    private static void enable(int lightId) {
        GLES10.glEnable(lightId);
        GLES10.glLightfv(lightId, 4608, ambient, 0);
        GLES10.glLightfv(lightId, 4609, diffuse, 0);
        GLES10.glLightfv(lightId, 4610, specular, 0);
        GLES10.glLightfv(lightId, 4611, direction, 0);
    }
}
