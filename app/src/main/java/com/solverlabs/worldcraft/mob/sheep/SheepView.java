package com.solverlabs.worldcraft.mob.sheep;

import com.solverlabs.droid.rugl.gl.State;
import com.solverlabs.worldcraft.mob.MobTexturePack;
import com.solverlabs.worldcraft.mob.MobView;

public class SheepView extends MobView {
    private final MobTexturePack furryTexturePack;
    private final Sheep mob;
    private final MobTexturePack mobTexturePack;

    public SheepView(Sheep mob, State state, boolean hasSecondTexture) {
        super(mob, state, true);
        this.mob = mob;
        this.mobTexturePack = new MobTexturePack(mob, state);
        this.furryTexturePack = new MobTexturePack(mob, state);
        this.furryTexturePack.shiftTc(0, 64);
    }

    @Override
    public MobTexturePack getTexturePack() {
        return this.mob.useSecondTexture() ? this.furryTexturePack : this.mobTexturePack;
    }
}
