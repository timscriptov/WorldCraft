package com.mcal.worldcraft.mob.skeleton;

import com.mcal.droid.rugl.gl.State;
import com.mcal.worldcraft.mob.MobTexturePack;
import com.mcal.worldcraft.mob.MobView;

public class SkeletonView extends MobView {
    private final MobTexturePack texturePack;

    public SkeletonView(Skeleton zombie, State state) {
        super(zombie, state);
        this.texturePack = new MobTexturePack(zombie, state);
    }

    @Override
    public MobTexturePack getTexturePack() {
        return this.texturePack;
    }
}
