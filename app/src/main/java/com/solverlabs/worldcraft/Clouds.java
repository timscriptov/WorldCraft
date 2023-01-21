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
        if (this.texture != null) {
            Shape shape = ShapeUtil.filledQuad(0.0f, 0.0f, 256.0f, 256.0f, 0.0f);
            ColouredShape cs = new ColouredShape(shape, Colour.withAlphai(Colour.white, Interaction.NOISE_NOTIFICATION_DELAY), this.state);
            this.ts = new TexturedShape(cs, ShapeUtil.getQuadTexCoords(1), this.texture);
            this.ts.scale(10.0f, 10.0f, 10.0f);
            setLight(world.getSunlight());
            this.state.apply();
        }
        this.xOffset = 0.0f;
        Vector3f playerPos = world.player.position;
        this.clouds[0] = new CloudPosition(playerPos.x, playerPos.z);
        this.clouds[1] = new CloudPosition(playerPos.x, playerPos.z - 2560.0f);
        this.clouds[2] = new CloudPosition(playerPos.x - 2560.0f, playerPos.z);
        this.clouds[3] = new CloudPosition(playerPos.x - 2560.0f, playerPos.z - 2560.0f);
    }

    public void advance(@NonNull Player player) {
        Vector3f playerPos = player.position;
        for (int i = 0; i < this.clouds.length; i++) {
            CloudPosition pos = this.clouds[i];
            pos.xOffset = this.xOffset;
            if (Math.abs(playerPos.x - pos.getX()) > 2560.0f) {
                if (playerPos.x > pos.getX()) {
                    pos.translateX(5120.0f);
                } else {
                    pos.translateX(-5120.0f);
                }
            }
            if (Math.abs(playerPos.z - pos.getZ()) > 2560.0f) {
                if (playerPos.z > pos.getZ()) {
                    pos.translateZ(5120.0f);
                } else {
                    pos.translateZ(-5120.0f);
                }
            }
        }
        this.xOffset += 0.02f;
    }

    public void draw(StackedRenderer renderer) {
        for (int i = 0; i < this.clouds.length; i++) {
            drawCloud(renderer, this.clouds[i]);
        }
        renderer.render();
    }

    private void drawCloud(@NonNull StackedRenderer renderer, @NonNull CloudPosition pos) {
        renderer.pushMatrix();
        renderer.translate(pos.getX() - 1280.0f, 128.0f, pos.getZ() - 1280.0f);
        renderer.rotate(90.0f, 1.0f, 0.0f, 0.0f);
        this.ts.render(renderer);
        renderer.popMatrix();
    }

    public void loadTexture() {
        ResourceLoader.loadNow(new BitmapLoader("clouds.png") {
            @Override
            public void complete() {
                Clouds.this.texture = TextureFactory.buildTexture(this.resource, true, false);
                if (Clouds.this.texture != null) {
                    Clouds.this.state = Clouds.this.texture.applyTo(Clouds.this.state);
                }
                this.resource.bitmap.recycle();
            }
        });
    }

    public void setLight(int sunLight) {
        float light = (float) Math.pow(0.8d, 15 - sunLight);
        this.ts.colours = ShapeUtil.expand(Colour.packFloat(light, light, light, 1.0f), this.ts.vertexCount());
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
            return this.x + this.xOffset;
        }

        public float getZ() {
            return this.z;
        }

        public void translateX(float offset) {
            this.x += offset;
        }

        public void translateZ(float offset) {
            this.z += offset;
        }
    }
}
