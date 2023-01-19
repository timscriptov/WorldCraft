package com.solverlabs.worldcraft.mob.cow;

import com.solverlabs.droid.rugl.gl.State;
import com.solverlabs.worldcraft.mob.MobTexturePack;
import com.solverlabs.worldcraft.mob.MobView;


public class CowView extends MobView {
    private final MobTexturePack mobTexturePack;

    public CowView(Cow cow, State state) {
        super(cow, state);
        this.mobTexturePack = new MobTexturePack(cow, state);
    }

    @Override
    public MobTexturePack getTexturePack() {
        return this.mobTexturePack;
    }
}
