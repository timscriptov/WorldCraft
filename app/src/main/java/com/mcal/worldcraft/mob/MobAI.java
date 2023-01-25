package com.mcal.worldcraft.mob;

import androidx.annotation.NonNull;

import com.mcal.worldcraft.Player;
import com.mcal.worldcraft.math.MathUtils;
import com.mcal.worldcraft.mob.zombie.Zombie;
import com.mcal.worldcraft.util.RandomUtil;

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
            case STATE_MOVING:
                stopMob(mob);
                return;
            case STATE_STANDS_STILL:
            default:
                moveMob(mob, getRandomAngle());
                return;
            case STATE_RUNNING:
                if (tag.nextUpdateAt > System.currentTimeMillis()) {
                    changeMobAngle(mob, getRandomAngle());
                    return;
                } else {
                    stopMob(mob);
                    return;
                }
            case STATE_FOLLOWING_PLAYER:
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
        updateMob(mob, STATE_STANDS_STILL, mob.getAngle(), getRandomUpdateTimeout());
        mob.stop();
    }

    private static void moveMob(Mob mob, float angle) {
        updateMob(mob, STATE_MOVING, angle, getRandomUpdateTimeout());
        mob.move();
    }

    private static void runMob(Mob mob, float angle) {
        updateMob(mob, STATE_RUNNING, angle, RUNNING_ON_ATTACK_TIMEOUT);
        mob.run();
    }

    private static void followPlayer(Mob mob, Player player) {
        updateMob(mob, STATE_FOLLOWING_PLAYER, getAngleToFollowPlayer(mob, player), FOLLOWING_PLAYER_TIMEOUT);
        mob.run();
    }

    private static float getAngleToFollowPlayer(@NonNull Mob mob, Player player) {
        MobTag tag = (MobTag) mob.getTag();
        if (System.currentTimeMillis() - tag.angleToFollowPlayerCalculatedAt > ANGLE_TO_FOLLOW_PLAYER_UPDATE_TIMEOUT) {
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
        mob.setAngle(angle, state != STATE_RUNNING && state != STATE_FOLLOWING_PLAYER);
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
