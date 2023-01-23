package com.solverlabs.worldcraft.chunk.entity;

import androidx.annotation.NonNull;

import com.solverlabs.worldcraft.mob.Mob;
import com.solverlabs.worldcraft.mob.cow.Cow;
import com.solverlabs.worldcraft.mob.pig.Pig;
import com.solverlabs.worldcraft.mob.sheep.Sheep;
import com.solverlabs.worldcraft.mob.sheep.SheepEntity;
import com.solverlabs.worldcraft.mob.zombie.Zombie;
import com.solverlabs.worldcraft.nbt.Tag;

import org.jetbrains.annotations.Contract;

public class EntityFactory {
    public static Mob getMob(Tag tag) {
        return getMob(parse(tag));
    }

    public static Mob getMob(Entity entity) {
        if (entity instanceof MobEntity) {
            return ((MobEntity) entity).extractMob();
        }
        return null;
    }

    @NonNull
    @Contract("_ -> new")
    public static Entity parse(@NonNull Tag tag) {
        String id = (String) tag.findTagByName("id").getValue();
        if (isMob(id)) {
            if (isSheep(id)) {
                return new SheepEntity(id, tag);
            }
            return new MobEntity(id, tag);
        }
        return new Entity(id, tag);
    }

    private static boolean isMob(String id) {
        return isZombie(id) || isPig(id) || isCow(id) || isSheep(id);
    }

    private static boolean isZombie(String id) {
        return Zombie.SAVE_ID.equals(id);
    }

    private static boolean isPig(String id) {
        return Pig.SAVE_ID.equals(id);
    }

    private static boolean isCow(String id) {
        return Cow.SAVE_ID.equals(id);
    }

    private static boolean isSheep(String id) {
        return Sheep.SAVE_ID.equals(id);
    }
}
