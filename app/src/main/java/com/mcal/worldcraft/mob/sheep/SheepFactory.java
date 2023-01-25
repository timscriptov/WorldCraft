package com.mcal.worldcraft.mob.sheep;

import com.mcal.droid.rugl.util.FPSCamera;
import com.mcal.droid.rugl.util.geom.Vector3f;
import com.mcal.worldcraft.Player;
import com.mcal.worldcraft.World;
import com.mcal.worldcraft.mob.Mob;
import com.mcal.worldcraft.mob.MobAI;
import com.mcal.worldcraft.mob.MobFactory;
import com.mcal.worldcraft.mob.MobView;

public class SheepFactory extends MobFactory {
    @Override
    protected MobView getMobView(Mob mob) {
        return new SheepView((Sheep) mob, this.state, true);
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
        return 1;
    }

    @Override
    public void mobAttacked(Mob mob, Player player) {
        Sheep sheep = (Sheep) mob;
        if (player != null && player.inHand != null && player.inHand.getItemID() == 71 && !sheep.isSheared()) {
            sheep.shear();
            dropItems(mob, sheep.getShearDrops());
            shiftMobOnAttack(mob, player);
            return;
        }
        super.mobAttacked(mob, player);
    }

    @Override
    public Mob createMob(Vector3f location) {
        return new Sheep(location);
    }

    @Override
    public int getMinGroupSize() {
        return 1;
    }

    @Override
    public int getMaxGroupSize() {
        return 3;
    }
}
