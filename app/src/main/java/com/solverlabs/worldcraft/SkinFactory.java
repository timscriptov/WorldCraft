package com.solverlabs.worldcraft;

import com.solverlabs.droid.rugl.gl.GLUtil;
import com.solverlabs.droid.rugl.gl.State;
import com.solverlabs.droid.rugl.gl.enums.FogMode;
import com.solverlabs.droid.rugl.gl.enums.MagFilter;
import com.solverlabs.droid.rugl.gl.enums.MinFilter;
import com.solverlabs.droid.rugl.gl.facets.Fog;
import com.solverlabs.droid.rugl.res.BitmapLoader;
import com.solverlabs.droid.rugl.res.ResourceLoader;
import com.solverlabs.droid.rugl.texture.BitmapImage;
import com.solverlabs.droid.rugl.texture.Image;
import com.solverlabs.droid.rugl.texture.Texture;
import com.solverlabs.droid.rugl.texture.TextureFactory;
import com.solverlabs.droid.rugl.util.Colour;
import com.solverlabs.worldcraft.skin.geometry.generator.SkinGeometryGenerator;

public class SkinFactory {
    public static State state = GLUtil.typicalState.with(MinFilter.NEAREST, MagFilter.NEAREST).with(new Fog(FogMode.LINEAR, 0.5f, 30.0f, 40.0f, Colour.packFloat(0.7f, 0.7f, 0.9f, 1.0f)));
    public static Texture texture;

    public static void loadTexture() {
        ResourceLoader.loadNow(new BitmapLoader(R.drawable.player_skins) {
            @Override
            public void complete() {
                SkinFactory.texture = TextureFactory.buildTexture(this.resource, true, false);
                if (SkinFactory.texture != null) {
                    SkinFactory.state = SkinFactory.texture.applyTo(SkinFactory.state);
                }
                this.resource.bitmap.recycle();
                SkinGeometryGenerator.init();
            }
        });
    }
}
