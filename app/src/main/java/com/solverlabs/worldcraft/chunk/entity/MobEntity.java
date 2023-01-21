package com.solverlabs.worldcraft.chunk.entity;

import androidx.annotation.Nullable;

import com.solverlabs.worldcraft.mob.Mob;
import com.solverlabs.worldcraft.mob.cow.Cow;
import com.solverlabs.worldcraft.mob.pig.Pig;
import com.solverlabs.worldcraft.mob.sheep.Sheep;
import com.solverlabs.worldcraft.mob.zombie.Zombie;
import com.solverlabs.worldcraft.nbt.Tag;

import java.util.HashMap;
import java.util.Map;

public class MobEntity extends Entity {
    private static final String HEALTH = "Health";
    private Map<String, Tag> extraTags;
    private short health;

    public MobEntity(String id) {
        super(id);
    }

    public MobEntity(String id, Tag tag) {
        super(id, tag);
        this.health = (Short) tag.findTagByName(HEALTH).getValue();
    }

    @Nullable
    private static Mob createMob(String id) {
        if (Cow.SAVE_ID.equals(id)) {
            return new Cow();
        }
        if (Pig.SAVE_ID.equals(id)) {
            return new Pig();
        }
        if (Sheep.SAVE_ID.equals(id)) {
            return new Sheep();
        }
        if (Zombie.SAVE_ID.equals(id)) {
            return new Zombie();
        }
        return null;
    }

    @Override
    protected Map<String, Tag> getExtraTags() {
        if (this.extraTags == null) {
            createExtraTags();
        } else {
            updateExtraTags();
        }
        return this.extraTags;
    }

    private void updateExtraTags() {
        Tag healthTag = this.extraTags.get(HEALTH);
        healthTag.setValue(this.health);
    }

    private void createExtraTags() {
        this.extraTags = new HashMap<>();
        this.extraTags.put(HEALTH, new Tag(Tag.Type.TAG_Short, HEALTH, this.health));
    }

    public short getHealth() {
        return this.health;
    }

    public void setHealth(short health) {
        this.health = health;
    }

    public Mob extractMob() {
        Mob mob = createMob(getId());
        mob.setPosition(getPosition());
        mob.setAngle(getYaw());
        mob.setHealthPoints(getHealth());
        return mob;
    }
}
