package com.solverlabs.worldcraft.mob.zombie;

import com.solverlabs.droid.rugl.gl.State;
import com.solverlabs.worldcraft.mob.MobTexturePack;
import com.solverlabs.worldcraft.mob.MobView;


public class ZombieView extends MobView {
    private final MobTexturePack texturePack;

    public ZombieView(Zombie zombie, State state) {
        super(zombie, state);
        this.texturePack = new MobTexturePack(zombie, state);
    }

    @Override
    public MobTexturePack getTexturePack() {
        return this.texturePack;
    }
}
