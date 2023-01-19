package com.solverlabs.worldcraft.mob;

import androidx.annotation.NonNull;

import com.solverlabs.worldcraft.Player;
import com.solverlabs.worldcraft.math.MathUtils;
import com.solverlabs.worldcraft.mob.zombie.Zombie;
import com.solverlabs.worldcraft.util.RandomUtil;


public class MobAI {
    private static final int ANGLE_TO_FOLLOW_PLAYER_UPDATE_TIMEOUT = 700;
    private static final long FOLLOWING_PLAYER_TIMEOUT = 1000;
    private static final float MAX_MOB_ANGLE = 6.2831855f;
    private static final long MAX_UPDATE_TIMEOUT = 10000;
    private static final long MIN_UPDATE_TIMEOUT = 2000;
    private static final long RUNNING_ON_ATTACK_TIMEOUT = 5000;
    private static final int STATE_FOLLOWING_PLAYER = 4;
    private static final int STATE_MOVING = 1;
    private static final int STATE_RUNNING = 3;
    private static final int STATE_STANDS_STILL = 2;

    public static void advance(@NonNull Mob mob, Player player) {
        MobTag tag = (MobTag) mob.getTag();
        if (tag == null || tag.nextUpdateAt < System.currentTimeMillis() || (tag.state == 3 && RandomUtil.getChance(0.025f))) {
            updateMob(mob);
        }
        if ((mob instanceof Zombie) && mob.getDistance() < 16.0f && !player.isDead()) {
            followPlayer(mob, player);
        }
        if (mob.hasNeedToTalk()) {
            mob.talk();
        }
    }

    public static void updateMob(@NonNull Mob mob) {
        MobTag tag = (MobTag) mob.getTag();
        if (tag == null) {
            new MobTag();
            moveMob(mob, getRandomAngle());
            return;
        }
        switch (tag.state) {
            case 1:
                stopMob(mob);
                return;
            case 2:
            default:
                moveMob(mob, getRandomAngle());
                return;
            case 3:
                if (tag.nextUpdateAt > System.currentTimeMillis()) {
                    changeMobAngle(mob, getRandomAngle());
                    return;
                } else {
                    stopMob(mob);
                    return;
                }
            case 4:
                stopMob(mob);
                return;
        }
    }

    public static void mobCollision(Mob mob) {
    }

    public static void mobAttacked(@NonNull Mob mob, Player player) {
        if (!mob.isHostile() && mob.isPassive()) {
            runMob(mob, MathUtils.randomizeAngleCorrection(player.getAngle(), 1.0f));
            mob.talk();
        }
    }

    private static void changeMobAngle(@NonNull Mob mob, float angle) {
        mob.setAngle(angle, false);
    }

    private static void stopMob(Mob mob) {
        updateMob(mob, 2, mob.getAngle(), getRandomUpdateTimeout());
        mob.stop();
    }

    private static void moveMob(Mob mob, float angle) {
        updateMob(mob, 1, angle, getRandomUpdateTimeout());
        mob.move();
    }

    private static void runMob(Mob mob, float angle) {
        updateMob(mob, 3, angle, RUNNING_ON_ATTACK_TIMEOUT);
        mob.run();
    }

    private static void followPlayer(Mob mob, Player player) {
        updateMob(mob, 4, getAngleToFollowPlayer(mob, player), 1000L);
        mob.run();
    }

    private static float getAngleToFollowPlayer(@NonNull Mob mob, Player player) {
        MobTag tag = (MobTag) mob.getTag();
        if (System.currentTimeMillis() - tag.angleToFollowPlayerCalculatedAt > 700) {
            tag.angleToFollowPlayer = MathUtils.getAngleToFollowPoint(mob.getPosition().x, mob.getPosition().z, player.position.x, player.position.z);
            tag.angleToFollowPlayerCalculatedAt = System.currentTimeMillis();
        }
        return tag.angleToFollowPlayer;
    }

    private static void updateMob(@NonNull Mob mob, int state, float angle, long timeout) {
        MobTag tag = (MobTag) mob.getTag();
        if (tag == null) {
            tag = new MobTag();
        }
        tag.nextUpdateAt = System.currentTimeMillis() + timeout;
        tag.state = state;
        mob.setAngle(angle, state != 3 && state != 4);
        mob.setTag(tag);
    }

    private static long getRandomUpdateTimeout() {
        return MIN_UPDATE_TIMEOUT + ((long) (Math.random() * 8000.0d));
    }

    private static float getRandomAngle() {
        return (float) (Math.random() * 6.2831854820251465d);
    }

    public static class MobTag {
        float angleToFollowPlayer;
        long angleToFollowPlayerCalculatedAt;
        long nextUpdateAt;
        int state;

        private MobTag() {
            this.state = 1;
        }
    }
}
