package com.solverlabs.worldcraft.mob.cow;

import com.solverlabs.droid.rugl.util.geom.Vector3f;
import com.solverlabs.worldcraft.factories.BlockFactory;
import com.solverlabs.worldcraft.material.Material;
import com.solverlabs.worldcraft.mob.Mob;
import com.solverlabs.worldcraft.mob.MobSize;
import com.solverlabs.worldcraft.mob.TexturedBlockProperties;
import com.solverlabs.worldcraft.util.RandomUtil;

import java.util.HashMap;

public class Cow extends Mob {
    public static final String SAVE_ID = "Cow";
    private static final int BEEF_MAX_COUNT = 3;
    private static final int BEEF_MIN_COUNT = 1;
    private static final int BODY_BLOCK_HEIGHT = 20;
    private static final int BODY_BLOCK_WIDTH = 24;
    private static final float DEFAULT_QUIET_VELOCITY = 0.4f;
    private static final float DEFAULT_RUN_VELOCITY = 1.2f;
    private static final int HAND_BLOCK_DEPTH = 8;
    private static final int HAND_BLOCK_HEIGHT = 24;
    private static final int HAND_BLOCK_WIDTH = 8;
    private static final int HEAD_BLOCK_DEPTH = 12;
    private static final int HEAD_BLOCK_HEIGHT = 16;
    private static final int HEAD_BLOCK_WIDTH = 16;
    private static final int LEG_BLOCK_DEPTH = 8;
    private static final int LEG_BLOCK_HEIGHT = 24;
    private static final int LEG_BLOCK_WIDTH = 8;
    private static final int[] HEAD_TC = {28, 12, 0, 12, 12, 12, 40, 12, 12, 0, 28, 0};
    private static final int[] BODY_TC = {92, 48, 92, 28, 44, 28, 68, 28, 68, 48, 44, 48};
    private static final int[] HAND_TC = {16, 40, 0, 40, 8, 40, 24, 40, 8, 32, 16, 32};
    private static final int[] LEG_TC = {16, 40, 0, 40, 8, 40, 24, 40, 8, 32, 16, 32};
    private static final TexturedBlockProperties head = new TexturedBlockProperties(16, 16, 12, HEAD_TC);
    private static final int BODY_BLOCK_DEPTH = 36;
    private static final TexturedBlockProperties body = new TexturedBlockProperties(24, 20, BODY_BLOCK_DEPTH, BODY_TC);
    private static final TexturedBlockProperties hand = new TexturedBlockProperties(8, 24, 8, HAND_TC);
    private static final TexturedBlockProperties leg = new TexturedBlockProperties(8, 24, 8, LEG_TC);
    private static final MobSize MOB_SIZE = new MobSize(head, body, hand, leg);

    public Cow(Vector3f position) {
        super(position);
        init();
    }

    public Cow() {
        init();
    }

    private void init() {
        this.mobSize = MOB_SIZE;
        this.healthPoints = (short) 10;
    }

    @Override
    public HashMap<Byte, Integer> getDeathDrops() {
        HashMap<Byte, Integer> result = new HashMap<>();
        result.put(BlockFactory.RAW_BEEF_ID, RandomUtil.getRandomInRangeInclusive(1, 3));
        return result;
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
        return DEFAULT_RUN_VELOCITY;
    }

    @Override
    public Material getMaterial() {
        return Material.COW;
    }
}
