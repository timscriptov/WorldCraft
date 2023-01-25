package com.mcal.worldcraft.mob.zombie;

import com.mcal.droid.rugl.gl.State;
import com.mcal.worldcraft.mob.MobTexturePack;
import com.mcal.worldcraft.mob.MobView;

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
