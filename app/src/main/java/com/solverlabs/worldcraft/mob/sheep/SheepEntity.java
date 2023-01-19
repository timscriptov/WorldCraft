package com.solverlabs.worldcraft.mob.sheep;

import com.solverlabs.worldcraft.chunk.entity.MobEntity;
import com.solverlabs.worldcraft.mob.Mob;
import com.solverlabs.worldcraft.nbt.Tag;


public class SheepEntity extends MobEntity {
    private static final String SHEARED = "Sheared";
    private boolean isSheared;

    public SheepEntity(String id, Tag tag) {
        super(id, tag);
        Tag shearedTag;
        this.isSheared = false;
        if (tag != null && (shearedTag = tag.findTagByName(SHEARED)) != null) {
            this.isSheared = (Short) shearedTag.getValue() == 1;
        }
    }

    public SheepEntity(String id) {
        super(id);
        this.isSheared = false;
    }

    public boolean isSheared() {
        return this.isSheared;
    }

    public void setSheared(boolean isSheared) {
        this.isSheared = isSheared;
    }

    @Override
    public Tag getTag() {
        short sheared = (short) (this.isSheared ? 1 : 0);
        Tag result = super.getTag();
        Tag[] tags = (Tag[]) result.getValue();
        if (tags.length > 0) {
            tags[tags.length - 1] = new Tag(Tag.Type.TAG_Short, SHEARED, sheared);
            Tag[] newResult = new Tag[tags.length + 1];
            System.arraycopy(tags, 0, newResult, 0, tags.length);
            newResult[tags.length] = new Tag(Tag.Type.TAG_End, (String) null, (Tag[]) null);
            result.setValue(newResult);
        }
        return result;
    }

    @Override
    public Mob extractMob() {
        Sheep sheep = (Sheep) super.extractMob();
        sheep.setSheared(this.isSheared);
        return sheep;
    }
}
