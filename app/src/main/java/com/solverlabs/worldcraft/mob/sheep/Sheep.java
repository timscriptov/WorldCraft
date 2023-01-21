package com.solverlabs.worldcraft.mob.sheep;

import com.solverlabs.droid.rugl.util.FPSCamera;
import com.solverlabs.droid.rugl.util.geom.Vector3f;
import com.solverlabs.worldcraft.Player;
import com.solverlabs.worldcraft.World;
import com.solverlabs.worldcraft.chunk.entity.MobEntity;
import com.solverlabs.worldcraft.material.Material;
import com.solverlabs.worldcraft.mob.Mob;
import com.solverlabs.worldcraft.mob.MobSize;
import com.solverlabs.worldcraft.mob.TexturedBlockProperties;
import com.solverlabs.worldcraft.srv.domain.PlayerDefault;
import com.solverlabs.worldcraft.util.RandomUtil;

import java.util.HashMap;

public class Sheep extends Mob {
    public static final String SAVE_ID = "Sheep";
    private static final int BODY_BLOCK_DEPTH = 32;
    private static final int BODY_BLOCK_WIDTH = 20;
    private static final float DEFAULT_QUIET_VELOCITY = 0.3f;
    private static final float DEFAULT_RUN_VELOCITY = 0.90000004f;
    private static final int HAND_BLOCK_DEPTH = 8;
    private static final int HAND_BLOCK_HEIGHT = 16;
    private static final int HAND_BLOCK_WIDTH = 8;
    private static final int HEAD_BLOCK_DEPTH = 16;
    private static final int HEAD_BLOCK_HEIGHT = 12;
    private static final int HEAD_BLOCK_WIDTH = 12;
    private static final int LEG_BLOCK_DEPTH = 8;
    private static final int LEG_BLOCK_HEIGHT = 16;
    private static final int LEG_BLOCK_WIDTH = 8;
    private static final int MAX_WOOL_COUNT_ON_SHEAR = 3;
    private static final int MIN_WOOL_COUNT_ON_SHEAR = 1;
    private static final int SHEARED_TIMEOUT = 120000;
    private static final float SHEEP_BODY_COLLIDE_HEAD_Y = 0.5f;
    private static final float SHEEP_BODY_COLLIDE_HEAD_Z = 0.5f;
    private static final int[] HEAD_TC = {28, 16, 0, 16, 16, 16, 44, 16, 16, 0, 28, 0};
    private static final int BODY_BLOCK_HEIGHT = 18;
    private static final int[] BODY_TC = {96, 0, 96, BODY_BLOCK_HEIGHT, 56, 0, 76, 0, 56, BODY_BLOCK_HEIGHT, 76, BODY_BLOCK_HEIGHT};
    private static final TexturedBlockProperties body = new TexturedBlockProperties(20, BODY_BLOCK_HEIGHT, 32, BODY_TC);
    private static final int[] HAND_TC = {16, 40, 0, 40, 8, 40, 24, 40, 8, 32, 16, 32};
    private static final int[] LEG_TC = {16, 40, 0, 40, 8, 40, 24, 40, 8, 32, 16, 32};
    private static final TexturedBlockProperties head = new TexturedBlockProperties(12, 12, 16, HEAD_TC);
    private static final TexturedBlockProperties hand = new TexturedBlockProperties(8, 16, 8, HAND_TC);
    private static final TexturedBlockProperties leg = new TexturedBlockProperties(8, 16, 8, LEG_TC);
    private static final MobSize MOB_SIZE = new MobSize(head, body, hand, leg, 0.5f, 0.5f);
    private boolean sheared;
    private long shearedAt;

    public Sheep() {
        this.sheared = false;
        init();
    }

    public Sheep(Vector3f position) {
        super(position);
        this.sheared = false;
        init();
    }

    private void init() {
        this.mobSize = MOB_SIZE;
        this.healthPoints = (short) 10;
    }

    @Override
    public void advance(float delta, World world, FPSCamera cam, Player player) {
        super.advance(delta, world, cam, player);
        if (this.downBlock != null && System.currentTimeMillis() - this.shearedAt > PlayerDefault.MAX_IDLE_TIME && this.downBlock == 2) {
            this.sheared = false;
        }
    }

    @Override
    public boolean useSecondTexture() {
        if (isSheared()) {
            return super.useSecondTexture();
        }
        return true;
    }

    @Override
    public HashMap<Byte, Integer> getDeathDrops() {
        HashMap<Byte, Integer> result = new HashMap<>();
        result.put((byte) 35, 1);
        return result;
    }

    public HashMap<Byte, Integer> getShearDrops() {
        HashMap<Byte, Integer> result = new HashMap<>();
        result.put((byte) 35, RandomUtil.getRandomInRangeInclusive(1, 3));
        return result;
    }

    @Override
    public String getSaveId() {
        return SAVE_ID;
    }

    @Override
    public float getQuietVelocity() {
        return 0.3f;
    }

    @Override
    public float getRunVelocity() {
        return DEFAULT_RUN_VELOCITY;
    }

    public boolean isSheared() {
        return this.sheared;
    }

    public void setSheared(boolean sheared) {
        this.sheared = sheared;
    }

    public void shear() {
        setSheared(true);
        this.shearedAt = System.currentTimeMillis();
    }

    @Override
    public MobEntity getEntity() {
        SheepEntity sheepEntity = (SheepEntity) super.getEntity();
        sheepEntity.setSheared(this.sheared);
        return sheepEntity;
    }

    @Override
    protected MobEntity createMobEntity() {
        return new SheepEntity(getSaveId());
    }

    @Override
    public Material getMaterial() {
        return Material.SHEEP;
    }
}
