package com.mcal.worldcraft.mob.zombie;

import com.mcal.droid.rugl.util.geom.Vector3f;
import com.mcal.worldcraft.SoundManager;
import com.mcal.worldcraft.Sounds;
import com.mcal.worldcraft.factories.BlockFactory;
import com.mcal.worldcraft.material.Material;
import com.mcal.worldcraft.mob.Mob;
import com.mcal.worldcraft.mob.MobSize;
import com.mcal.worldcraft.mob.TexturedBlockProperties;
import com.mcal.worldcraft.utils.RandomUtil;

import java.util.HashMap;

public class Zombie extends Mob {
    public static final float DISTANCE_TO_DETECT_PLAYER = 16.0f;
    public static final String SAVE_ID = "Zombie";
    private static final long ATTACK_TIMEOUT = 2000;
    private static final int BODY_BLOCK_DEPTH = 8;
    private static final int BODY_BLOCK_HEIGHT = 24;
    private static final int BODY_BLOCK_WIDTH = 16;
    private static final int DAMAGE_POWER = 3;
    private static final float DEFAULT_QUIET_VELOCITY = 0.25f;
    private static final float DEFAULT_RUN_VELOCITY = 0.5f;
    private static final int HAND_BLOCK_DEPTH = 8;
    private static final int HAND_BLOCK_HEIGHT = 24;
    private static final int HAND_BLOCK_WIDTH = 8;
    private static final int HEAD_BLOCK_DEPTH = 16;
    private static final int HEAD_BLOCK_HEIGHT = 16;
    private static final int HEAD_BLOCK_WIDTH = 16;
    private static final int LEG_BLOCK_DEPTH = 8;
    private static final int LEG_BLOCK_HEIGHT = 24;
    private static final int LEG_BLOCK_WIDTH = 8;
    private static final int ROTTEN_FLESH_MAX_COUNT = 2;
    private static final int ROTTEN_FLESH_MIN_COUNT = 0;
    private static final int[] HEAD_TC = {32, 16, 0, 16, 16, 16, 48, 16, 16, 0, 32, 0};
    private static final int[] BODY_TC = {72, 40, 32, 40, 40, 40, 56, 40, 40, 32, 56, 32};
    private static final int[] HAND_TC = {104, 40, 80, 40, 88, 40, 96, 40, 88, 32, 96, 32};
    private static final int[] LEG_TC = {24, 40, 0, 40, 8, 40, 16, 40, 8, 32, 16, 32};
    private static final TexturedBlockProperties head = new TexturedBlockProperties(16, 16, 16, HEAD_TC);
    private static final TexturedBlockProperties body = new TexturedBlockProperties(16, 24, 8, BODY_TC);
    private static final TexturedBlockProperties hand = new TexturedBlockProperties(8, 24, 8, HAND_TC);
    private static final TexturedBlockProperties leg = new TexturedBlockProperties(8, 24, 8, LEG_TC);
    private static final MobSize MOB_SIZE = new ZombieSize(head, body, hand, leg);

    public Zombie() {
        init();
    }

    public Zombie(Vector3f position) {
        super(position);
        init();
    }

    private void init() {
        setAggressionType((byte) 2);
        this.mobSize = MOB_SIZE;
        this.healthPoints = (short) 22;
    }

    @Override
    public boolean isHandsMoving() {
        return false;
    }

    @Override
    public HashMap<Byte, Integer> getDeathDrops() {
        HashMap<Byte, Integer> result = new HashMap<>();
        result.put(BlockFactory.ROTTEN_FLESH_ID, RandomUtil.getRandomInRangeInclusive(0, 2));
        if (RandomUtil.getChance(0.05f)) {
            result.put(BlockFactory.IRON_INGOT_ID, 1);
        }
        return result;
    }

    @Override
    protected long getAttackTimeout() {
        return ATTACK_TIMEOUT;
    }

    @Override
    public String getSaveId() {
        return SAVE_ID;
    }

    @Override
    public float getQuietVelocity() {
        return DEFAULT_QUIET_VELOCITY;
    }

    @Override
    public float getRunVelocity() {
        return 0.5f;
    }

    @Override
    public boolean isAfraidSunlight() {
        return true;
    }

    @Override
    public int getDamagePower() {
        return 3;
    }

    @Override
    public Material getMaterial() {
        return Material.ZOMBIE;
    }

    @Override
    public void takeDamage(int damage) {
        boolean wasAlive = getHealthPoints() > 0;
        super.takeDamage(damage);
        if (wasAlive) {
            if (getHealthPoints() > 0) {
                SoundManager.playDistancedSound(Sounds.ZOMBIE_HIT, getDistance());
            } else {
                SoundManager.playDistancedSound(Sounds.ZOMBIE_DEATH, getDistance());
            }
            onTalk();
        }
    }
}
