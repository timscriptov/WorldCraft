package com.solverlabs.worldcraft.ui;

import com.solverlabs.droid.rugl.geom.TexturedShape;
import com.solverlabs.droid.rugl.gl.GLUtil;
import com.solverlabs.droid.rugl.gl.StackedRenderer;
import com.solverlabs.droid.rugl.gl.State;
import com.solverlabs.droid.rugl.gl.enums.MagFilter;
import com.solverlabs.droid.rugl.gl.enums.MinFilter;
import com.solverlabs.droid.rugl.res.BitmapLoader;
import com.solverlabs.droid.rugl.res.ResourceLoader;
import com.solverlabs.droid.rugl.texture.BitmapImage;
import com.solverlabs.droid.rugl.texture.Image;
import com.solverlabs.droid.rugl.texture.Texture;
import com.solverlabs.droid.rugl.texture.TextureFactory;
import com.solverlabs.droid.rugl.util.Trig;
import com.solverlabs.droid.rugl.util.geom.Vector2f;
import com.solverlabs.droid.rugl.util.math.Range;
import com.solverlabs.worldcraft.GameMode;
import com.solverlabs.worldcraft.Player;
import com.solverlabs.worldcraft.R;
import com.solverlabs.worldcraft.skin.geometry.generator.SkinGeometryGenerator;

public class Hand {
    public static final float DEFAULT_SIZE = 300.0f;
    private static final int FOOD_SIZE_REST = 200;
    private static final int FOOD_SIZE_STRIKE = 300;
    private static State state = GLUtil.typicalState.with(MinFilter.NEAREST, MagFilter.NEAREST);
    private final Player player;
    public Vector2f restPos = new Vector2f(700.0f, 60.0f);
    public Vector2f strikePos = new Vector2f(510.0f, 40.0f);
    public Vector2f foodPosRest = new Vector2f(400.0f, 70.0f);
    public Vector2f foodPosStrike = new Vector2f(375.0f, 40.0f);
    public float restRotation = 10.0f;
    public float strikeRotation = 80.0f;
    public float missTime = 0.6f;
    public float hitTime = 0.3f;
    private float strikeCycle = 0.0f;
    private boolean swing = false;
    private float currentStrikeTime = this.missTime;

    public Hand(Player player) {
        this.player = player;
    }

    public void strike(boolean fast) {
        if (this.strikeCycle == 0.0f) {
            this.currentStrikeTime = fast ? this.hitTime : this.missTime;
            this.strikeCycle = Float.MIN_VALUE;
        }
    }

    public void repeatedStrike(boolean fast) {
        this.swing = true;
        this.currentStrikeTime = (!fast || this.player.isReadyToEat()) ? this.missTime : this.hitTime;
    }

    public void stopStriking() {
        this.swing = false;
    }

    public void advance(float delta) {
        if (this.swing || this.strikeCycle != 0.0f) {
            this.strikeCycle += (6.2831855f * delta) / this.currentStrikeTime;
        }
        if (this.strikeCycle > 6.2831855f) {
            this.strikeCycle = 0.0f;
        }
    }

    public void draw(StackedRenderer r) {
        float rot;
        float x;
        float y;
        try {
            r.pushMatrix();
            float size = DEFAULT_SIZE;
            if (this.swing && this.player != null && this.player.isReadyToEat()) {
                float swing = (-Math.abs(Trig.cos(this.strikeCycle))) + 1.0f;
                rot = 0.0f;
                size = Range.toValue(swing, FOOD_SIZE_REST, FOOD_SIZE_STRIKE);
                x = Range.toValue(swing, this.foodPosRest.x, this.foodPosStrike.x);
                y = Range.toValue(swing, this.foodPosRest.y, this.foodPosStrike.y);
            } else {
                float swing2 = (-Math.abs(Trig.cos(0.5f * this.strikeCycle))) + 1.0f;
                rot = Range.toValue(swing2, this.restRotation, this.strikeRotation);
                x = Range.toValue(swing2, this.restPos.x, this.strikePos.x);
                y = Range.toValue(swing2, this.restPos.y, this.strikePos.y);
            }
            r.translate(x, y, 0.0f);
            r.rotate(rot, 0.0f, 0.0f, 1.0f);
            r.scale(size, size, 1.0f);
            if (this.player.inHand != null) {
                this.player.inHand.getItemShape().render(r);
            } else if (state != null && (!GameMode.isMultiplayerMode() || this.player.getWorld().isReady())) {
                TexturedShape hand = SkinGeometryGenerator.getHandShape();
                hand.state = state;
                hand.render(r);
            }
            r.popMatrix();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void loadTexture() {
        ResourceLoader.loadNow(new BitmapLoader(R.drawable.player_skins) {
            @Override
            public void complete() {
                Texture skin = TextureFactory.buildTexture(this.resource, true, false);
                if (skin != null) {
                    Hand.state = skin.applyTo(Hand.state);
                }
                this.resource.bitmap.recycle();
            }
        });
    }
}
