package com.solverlabs.worldcraft.mob.zombie;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.util.FPSCamera;
import com.solverlabs.droid.rugl.util.geom.Vector3f;
import com.solverlabs.worldcraft.Player;
import com.solverlabs.worldcraft.World;
import com.solverlabs.worldcraft.mob.Mob;
import com.solverlabs.worldcraft.mob.MobAI;
import com.solverlabs.worldcraft.mob.MobFactory;
import com.solverlabs.worldcraft.mob.MobView;

public class ZombieFactory extends MobFactory {
    private static final float MAX_BLOCK_LIGHT_TO_SPAWN = 0.5f;
    private static final float ZOMBIE_SPAWN_CHANGE = 0.001f;

    @Override
    protected MobView getMobView(Mob mob) {
        return new ZombieView((Zombie) mob, this.state);
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
        if (this.world == null) {
            return false;
        }
        spawnLocation.y += 1.0f;
        Float blockLight = this.world.blockLight(spawnLocation);
        spawnLocation.y -= 1.0f;
        return blockLight != null && blockLight < 0.5f;
    }

    @Override
    public float getSpawnChange() {
        return ZOMBIE_SPAWN_CHANGE;
    }

    @Override
    public Mob createMob(Vector3f location) {
        return new Zombie(location);
    }

    @Override
    public int getMinGroupSize() {
        return 0;
    }

    @Override
    public int getMaxGroupSize() {
        return 1;
    }
}
