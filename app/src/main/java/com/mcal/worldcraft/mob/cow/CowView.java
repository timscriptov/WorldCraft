package com.mcal.worldcraft.mob.cow;

import com.mcal.droid.rugl.gl.State;
import com.mcal.worldcraft.mob.MobTexturePack;
import com.mcal.worldcraft.mob.MobView;

public class CowView extends MobView {
    private final MobTexturePack mobTexturePack;

    public CowView(Cow cow, State state) {
        super(cow, state);
        mobTexturePack = new MobTexturePack(cow, state);
    }

    @Override
    public MobTexturePack getTexturePack() {
        return this.mobTexturePack;
    }
}
