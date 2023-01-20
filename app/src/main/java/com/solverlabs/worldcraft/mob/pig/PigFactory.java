package com.solverlabs.worldcraft.mob.pig;

import com.solverlabs.droid.rugl.util.FPSCamera;
import com.solverlabs.droid.rugl.util.geom.Vector3f;
import com.solverlabs.worldcraft.Player;
import com.solverlabs.worldcraft.World;
import com.solverlabs.worldcraft.mob.Mob;
import com.solverlabs.worldcraft.mob.MobAI;
import com.solverlabs.worldcraft.mob.MobFactory;
import com.solverlabs.worldcraft.mob.MobView;

public class PigFactory extends MobFactory {
    @Override 
    protected MobView getMobView(Mob mob) {
        return new PigView((Pig) mob, this.state);
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
    public void collisionWithPlayer(Mob mob, Player player) {
    }

    @Override 
    public int getMaxMobCountAroundPlayer() {
        return 2;
    }

    @Override 
    public Mob createMob(Vector3f location) {
        return new Pig(location);
    }

    @Override 
    public int getMinGroupSize() {
        return 2;
    }

    @Override 
    public int getMaxGroupSize() {
        return 5;
    }
}
