package com.solverlabs.worldcraft;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.geom.ColouredShape;
import com.solverlabs.droid.rugl.geom.Shape;
import com.solverlabs.droid.rugl.geom.ShapeUtil;
import com.solverlabs.droid.rugl.geom.TexturedShape;
import com.solverlabs.droid.rugl.gl.GLUtil;
import com.solverlabs.droid.rugl.gl.StackedRenderer;
import com.solverlabs.droid.rugl.gl.State;
import com.solverlabs.droid.rugl.gl.enums.MagFilter;
import com.solverlabs.droid.rugl.gl.enums.MinFilter;
import com.solverlabs.droid.rugl.res.BitmapLoader;
import com.solverlabs.droid.rugl.res.ResourceLoader;
import com.solverlabs.droid.rugl.texture.Texture;
import com.solverlabs.droid.rugl.texture.TextureFactory;
import com.solverlabs.droid.rugl.util.Colour;
import com.solverlabs.droid.rugl.util.geom.Vector3f;
import com.solverlabs.worldcraft.ui.Interaction;

public class Clouds {
    private static final int CLOUD_HEIGHT = 128;
    private static final int CLOUD_SIZE = 2560;
    private static Clouds instance;
    private final CloudPosition[] clouds = new CloudPosition[4];
    protected State state = GLUtil.typicalState.with(MinFilter.NEAREST, MagFilter.NEAREST);
    private Texture texture;
    private TexturedShape ts;
    private float xOffset;

    public static Clouds getInstance() {
        if (instance == null) {
            instance = new Clouds();
        }
        return instance;
    }

    public void init(World world) {
        loadTexture();
        if (texture != null) {
            Shape shape = ShapeUtil.filledQuad(0.0f, 0.0f, 256.0f, 256.0f, 0.0f);
            ColouredShape cs = new ColouredShape(shape, Colour.withAlphai(Colour.white, Interaction.NOISE_NOTIFICATION_DELAY), state);
            ts = new TexturedShape(cs, ShapeUtil.getQuadTexCoords(1), texture);
            ts.scale(10.0f, 10.0f, 10.0f);
            setLight(world.getSunlight());
            state.apply();
        }
        xOffset = 0.0f;
        Vector3f playerPos = world.player.position;
        clouds[0] = new CloudPosition(playerPos.x, playerPos.z);
        clouds[1] = new CloudPosition(playerPos.x, playerPos.z - CLOUD_SIZE);
        clouds[2] = new CloudPosition(playerPos.x - CLOUD_SIZE, playerPos.z);
        clouds[3] = new CloudPosition(playerPos.x - CLOUD_SIZE, playerPos.z - CLOUD_SIZE);
    }

    public void advance(@NonNull Player player) {
        Vector3f playerPos = player.position;
        for (int i = 0; i < clouds.length; i++) {
            CloudPosition pos = clouds[i];
            pos.xOffset = xOffset;
            if (Math.abs(playerPos.x - pos.getX()) > CLOUD_SIZE) {
                if (playerPos.x > pos.getX()) {
                    pos.translateX(5120.0f);
                } else {
                    pos.translateX(-5120.0f);
                }
            }
            if (Math.abs(playerPos.z - pos.getZ()) > CLOUD_SIZE) {
                if (playerPos.z > pos.getZ()) {
                    pos.translateZ(5120.0f);
                } else {
                    pos.translateZ(-5120.0f);
                }
            }
        }
        // Скорость движения облаков
        xOffset += 0.02f;
    }

    public void draw(StackedRenderer renderer) {
        for (int i = 0; i < clouds.length; i++) {
            drawCloud(renderer, clouds[i]);
        }
        renderer.render();
    }

    private void drawCloud(@NonNull StackedRenderer renderer, @NonNull CloudPosition pos) {
        renderer.pushMatrix();
        renderer.translate(pos.getX() - 1280.0f, CLOUD_HEIGHT, pos.getZ() - 1280.0f);
        renderer.rotate(90.0f, 1.0f, 0.0f, 0.0f);
        ts.render(renderer);
        renderer.popMatrix();
    }

    public void loadTexture() {
        ResourceLoader.loadNow(new BitmapLoader("clouds.png") {
            @Override
            public void complete() {
                texture = TextureFactory.buildTexture(resource, true, false);
                if (texture != null) {
                    state = texture.applyTo(state);
                }
                resource.bitmap.recycle();
            }
        });
    }

    public void setLight(int sunLight) {
        float light = (float) Math.pow(0.8d, 15 - sunLight);
        ts.colours = ShapeUtil.expand(Colour.packFloat(light, light, light, 1.0f), ts.vertexCount());
    }

    public static class CloudPosition {
        public float xOffset;
        private float x;
        private float z;

        public CloudPosition(float x, float z) {
            this.x = x;
            this.z = z;
        }

        public float getX() {
            return x + xOffset;
        }

        public float getZ() {
            return z;
        }

        public void translateX(float offset) {
            x += offset;
        }

        public void translateZ(float offset) {
            z += offset;
        }
    }
}
