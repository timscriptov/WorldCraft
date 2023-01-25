package com.mcal.worldcraft;

import com.mcal.droid.rugl.gl.GLUtil;
import com.mcal.droid.rugl.gl.State;
import com.mcal.droid.rugl.gl.enums.FogMode;
import com.mcal.droid.rugl.gl.enums.MagFilter;
import com.mcal.droid.rugl.gl.enums.MinFilter;
import com.mcal.droid.rugl.gl.facets.Fog;
import com.mcal.droid.rugl.res.BitmapLoader;
import com.mcal.droid.rugl.res.ResourceLoader;
import com.mcal.droid.rugl.texture.Texture;
import com.mcal.droid.rugl.texture.TextureFactory;
import com.mcal.droid.rugl.util.Colour;
import com.mcal.worldcraft.skin.geometry.generator.SkinGeometryGenerator;

public class SkinFactory {
    public static State state = GLUtil.typicalState.with(MinFilter.NEAREST, MagFilter.NEAREST).with(new Fog(FogMode.LINEAR, 0.5f, 30.0f, 40.0f, Colour.packFloat(0.7f, 0.7f, 0.9f, 1.0f)));
    public static Texture texture;

    public static void loadTexture() {
        ResourceLoader.loadNow(new BitmapLoader("player_skins.png") {
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
