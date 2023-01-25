package com.mcal.worldcraft.mob.skeleton;

import androidx.annotation.NonNull;

import com.mcal.droid.rugl.util.FPSCamera;
import com.mcal.droid.rugl.util.geom.Vector3f;
import com.mcal.worldcraft.Player;
import com.mcal.worldcraft.World;
import com.mcal.worldcraft.mob.Mob;
import com.mcal.worldcraft.mob.MobAI;
import com.mcal.worldcraft.mob.MobFactory;
import com.mcal.worldcraft.mob.MobView;

public class SkeletonFactory extends MobFactory {
    private static final float MAX_BLOCK_LIGHT_TO_SPAWN = 0.5f;
    private static final float ZOMBIE_SPAWN_CHANGE = 0.001f;

    @Override
    protected MobView getMobView(Mob mob) {
        return new SkeletonView((Skeleton) mob, this.state);
    }

    @Override
    public void advanceMob(float delta, World world, FPSCamera cam, Player player, Mob mob) {
        MobAI.advance(mob, player);
        super.advanceMob(delta, world, cam, player, mob);
    }

    @Override
    public void collisionWithBlock(Mob mob, boolean isJumpPossible) {
        if (isJumpPossible) {
            mob.jump();
        } else {
            MobAI.updateMob(mob);
        }
    }

    @Override
    public void collisionWithMob(Mob mob) {
        MobAI.mobCollision(mob);
    }

    @Override
    public void collisionWithPlayer(Mob mob, @NonNull Player player) {
        if (!player.isDead()) {
            mob.attack(player);
        }
    }

    @Override
    public int getMaxMobCountAroundPlayer() {
        return 6;
    }

    @Override
    public boolean isSpawnConditionMet(Vector3f spawnLocation) {
        return true;
    }

    @Override
    public float getSpawnChange() {
        return ZOMBIE_SPAWN_CHANGE;
    }

    @Override
    public Mob createMob(Vector3f location) {
        return new Skeleton(location);
    }

    @Override
    public int getMinGroupSize() {
        return 1;
    }

    @Override
    public int getMaxGroupSize() {
        return 2;
    }
}
