package com.solverlabs.worldcraft.mob.skeleton;

import com.solverlabs.droid.rugl.gl.State;
import com.solverlabs.worldcraft.mob.MobTexturePack;
import com.solverlabs.worldcraft.mob.MobView;

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
