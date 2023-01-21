package com.solverlabs.worldcraft.mob.pig;

import com.solverlabs.droid.rugl.util.geom.Vector3f;
import com.solverlabs.worldcraft.factories.BlockFactory;
import com.solverlabs.worldcraft.material.Material;
import com.solverlabs.worldcraft.mob.Mob;
import com.solverlabs.worldcraft.mob.MobSize;
import com.solverlabs.worldcraft.mob.TexturedBlockProperties;
import com.solverlabs.worldcraft.util.RandomUtil;

import java.util.HashMap;

public class Pig extends Mob {
    public static final String SAVE_ID = "Pig";
    private static final int BODY_BLOCK_DEPTH = 32;
    private static final int BODY_BLOCK_HEIGHT = 16;
    private static final int BODY_BLOCK_WIDTH = 20;
    private static final float DEFAULT_QUIET_VELOCITY = 0.25f;
    private static final float DEFAULT_RUN_VELOCITY = 0.75f;
    private static final int HAND_BLOCK_DEPTH = 8;
    private static final int HAND_BLOCK_HEIGHT = 12;
    private static final int HAND_BLOCK_WIDTH = 8;
    private static final int HEAD_BLOCK_DEPTH = 16;
    private static final int HEAD_BLOCK_HEIGHT = 16;
    private static final int HEAD_BLOCK_WIDTH = 16;
    private static final int LEG_BLOCK_DEPTH = 8;
    private static final int LEG_BLOCK_HEIGHT = 12;
    private static final int LEG_BLOCK_WIDTH = 8;
    private static final int PORKCHOP_MAX_COUNT = 3;
    private static final int PORKCHOP_MIN_COUNT = 1;
    private static final int SNOUT_BLOCK_DEPTH = 2;
    private static final int SNOUT_BLOCK_HEIGHT = 6;
    private static final int SNOUT_BLOCK_WIDTH = 8;
    private static final int[] HEAD_TC = {32, 16, 0, 16, 16, 16, 48, 16, 16, 0, 32, 0};
    private static final int[] BODY_TC = {90, 48, 90, 32, 50, 32, 70, 32, 70, 48, 50, 48};
    private static final int[] HAND_TC = {16, 40, 0, 40, 8, 40, 24, 40, 8, 32, 16, 32};
    private static final int[] LEG_TC = {16, 40, 0, 40, 8, 40, 24, 40, 8, 32, 16, 32};
    private static final int[] SNOUT_TC = {42, 34, 32, 34, 34, 34, 34, 34, 34, 32, 42, 32};
    private static final TexturedBlockProperties head = new TexturedBlockProperties(16, 16, 16, HEAD_TC);
    private static final TexturedBlockProperties body = new TexturedBlockProperties(20, 16, 32, BODY_TC);
    private static final TexturedBlockProperties hand = new TexturedBlockProperties(8, 12, 8, HAND_TC);
    private static final TexturedBlockProperties leg = new TexturedBlockProperties(8, 12, 8, LEG_TC);
    private static final TexturedBlockProperties snout = new TexturedBlockProperties(8, 6, 2, SNOUT_TC);
    private static final MobSize MOB_SIZE = new PigSize(head, body, hand, leg, snout);

    public Pig() {
        init();
    }

    public Pig(Vector3f position) {
        super(position);
        init();
    }

    private void init() {
        this.mobSize = MOB_SIZE;
        this.healthPoints = (short) 10;
    }

    @Override
    public HashMap<Byte, Integer> getDeathDrops() {
        HashMap<Byte, Integer> result = new HashMap<>();
        result.put(BlockFactory.RAW_PORKCHOP_ID, RandomUtil.getRandomInRangeInclusive(1, 3));
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
        return Material.PIG;
    }
}
