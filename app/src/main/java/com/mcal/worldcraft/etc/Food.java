package com.mcal.worldcraft.etc;

import com.mcal.worldcraft.factories.BlockFactory;

public enum Food {
    COOKED_PORKCHOP(BlockFactory.COOKED_PORKCHOP_ID, 8, 12.8f, 0.0f),
    STEAK(BlockFactory.STEAK_ID, 8, 12.8f, 0.0f),
    RAW_PORKCHOP(BlockFactory.RAW_PORKCHOP_ID, 3, 1.8f, 0.0f),
    RAW_BEEF(BlockFactory.RAW_BEEF_ID, 3, 1.8f, 0.0f),
    ROTTEN_FLESH(BlockFactory.ROTTEN_FLESH_ID, 4, 0.8f, 0.8f);

    private final int foodPoints;
    private final byte id;
    private final float poisoningChance;
    private final float saturationPoints;

    Food(byte id, int foodPoints, float saturationPoints, float poisoningChance) {
        this.id = id;
        this.foodPoints = foodPoints;
        this.saturationPoints = saturationPoints;
        this.poisoningChance = poisoningChance;
    }

    public byte getId() {
        return id;
    }

    public int getFoodPoints() {
        return foodPoints;
    }

    public float getSaturationPoints() {
        return saturationPoints;
    }

    public float getPoisoningChance() {
        return poisoningChance;
    }
}
