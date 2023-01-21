package com.solverlabs.worldcraft.chunk.entity;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.util.geom.Vector3f;
import com.solverlabs.worldcraft.factories.DescriptionFactory;
import com.solverlabs.worldcraft.nbt.Tag;
import com.solverlabs.worldcraft.util.Distance;

import java.util.Map;

public class Entity {
    private static final int ENTITY_TAG_COUNT = 4;
    private static final int ID_TAG_INDEX = 0;
    private static final String MOTION = "Motion";
    private static final int MOTION_TAG_INDEX = 2;
    private static final String POS = "Pos";
    private static final int POSITION_TAG_INDEX = 1;
    private static final String ROTATION = "Rotation";
    private static final int ROTATION_TAG_INDEX = 3;
    private static final int TAGS_COUNT = 5;
    private String id;
    private Vector3f motion;
    private float pitch;
    private Vector3f position;
    private Tag tag;
    private Tag[] tags;
    private float yaw;

    public Entity(String id) {
        this.tag = null;
        this.tags = null;
        this.position = new Vector3f();
        this.motion = new Vector3f();
        this.id = id;
    }

    public Entity(String id, Tag tag) {
        this(id);
        if (tag == null) {
            throw new NullPointerException();
        }
        parsePosition(tag);
        parseMotion(tag);
        parseRotation(tag);
    }

    private void parseRotation(@NonNull Tag tag) {
        Tag[] rotation = (Tag[]) tag.findTagByName(ROTATION).getValue();
        this.yaw = (Float) rotation[0].getValue();
        this.pitch = (Float) rotation[1].getValue();
    }

    private void parsePosition(@NonNull Tag tag) {
        Tag[] pos = (Tag[]) tag.findTagByName(POS).getValue();
        this.position.x = ((Double) pos[0].getValue()).floatValue();
        this.position.y = ((Double) pos[1].getValue()).floatValue();
        this.position.z = ((Double) pos[2].getValue()).floatValue();
    }

    private void parseMotion(@NonNull Tag tag) {
        Tag[] motionTag = (Tag[]) tag.findTagByName(MOTION).getValue();
        this.motion.x = ((Double) motionTag[0].getValue()).floatValue();
        this.motion.y = ((Double) motionTag[1].getValue()).floatValue();
        this.motion.z = ((Double) motionTag[2].getValue()).floatValue();
    }

    public Tag getTag() {
        if (this.tags == null) {
            createTag();
        }
        updateTags();
        return this.tag;
    }

    protected Map<String, Tag> getExtraTags() {
        return null;
    }

    private void updateTags() {
        updateIdTag();
        updatePositionTag();
        updateMotionTag();
        updateRotationTag();
        updateExtra();
        updateEndTag();
    }

    private void updateIdTag() {
        if (this.tags[0] == null) {
            this.tags[0] = new Tag(Tag.Type.TAG_String, "id", this.id);
        }
    }

    private void updateEndTag() {
        this.tags[getEndTagIndex()] = new Tag(Tag.Type.TAG_End, (String) null, (Tag[]) null);
    }

    private void updateExtra() {
        Map<String, Tag> extraTags = getExtraTags();
        if (extraTags != null) {
            int i = 0;
            for (Tag tag : extraTags.values()) {
                this.tags[i + 4] = tag;
                i++;
            }
        }
    }

    private void updateRotationTag() {
        if (this.tags[3] == null) {
            this.tags[3] = serializeRotation();
            return;
        }
        Tag[] rotationTag = (Tag[]) this.tags[3].getValue();
        rotationTag[0].setValue(this.yaw);
        rotationTag[1].setValue(this.pitch);
    }

    private void updateMotionTag() {
        if (this.tags[2] == null) {
            this.tags[2] = serializeMotion();
            return;
        }
        Tag[] motionTag = (Tag[]) this.tags[2].getValue();
        motionTag[0].setValue(this.motion.x);
        motionTag[1].setValue(this.motion.y);
        motionTag[2].setValue(this.motion.z);
    }

    private void updatePositionTag() {
        if (this.tags[1] == null) {
            this.tags[1] = serializePosition();
            return;
        }
        Tag[] positionTag = (Tag[]) this.tags[1].getValue();
        positionTag[0].setValue(this.position.x);
        positionTag[1].setValue(this.position.y);
        positionTag[2].setValue(this.position.z);
    }

    private int getEndTagIndex() {
        return getTagsCount() - 1;
    }

    private void createTag() {
        this.tags = new Tag[getTagsCount()];
        this.tag = new Tag(Tag.Type.TAG_Compound, DescriptionFactory.emptyText, this.tags);
    }

    private int getTagsCount() {
        return getExtraTagsCount() + 5;
    }

    private int getExtraTagsCount() {
        Map<String, Tag> extraTags = getExtraTags();
        if (extraTags == null) {
            return 0;
        }
        return extraTags.size();
    }

    @NonNull
    private Tag serializeRotation() {
        Tag rotationList = new Tag(ROTATION, Tag.Type.TAG_Float);
        rotationList.addTag(new Tag(Tag.Type.TAG_Float, DescriptionFactory.emptyText, this.yaw));
        rotationList.addTag(new Tag(Tag.Type.TAG_Float, DescriptionFactory.emptyText, this.pitch));
        return rotationList;
    }

    @NonNull
    private Tag serializeMotion() {
        Tag motionList = new Tag(MOTION, Tag.Type.TAG_Double);
        motionList.addTag(new Tag(Tag.Type.TAG_Double, DescriptionFactory.emptyText, (double) this.motion.x));
        motionList.addTag(new Tag(Tag.Type.TAG_Double, DescriptionFactory.emptyText, (double) this.motion.y));
        motionList.addTag(new Tag(Tag.Type.TAG_Double, DescriptionFactory.emptyText, (double) this.motion.z));
        return motionList;
    }

    @NonNull
    private Tag serializePosition() {
        Tag positionList = new Tag(POS, Tag.Type.TAG_Double);
        positionList.addTag(new Tag(Tag.Type.TAG_Double, DescriptionFactory.emptyText, (double) this.position.x));
        positionList.addTag(new Tag(Tag.Type.TAG_Double, DescriptionFactory.emptyText, (double) this.position.y));
        positionList.addTag(new Tag(Tag.Type.TAG_Double, DescriptionFactory.emptyText, (double) this.position.z));
        return positionList;
    }

    public Vector3f getPosition() {
        return this.position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public Vector3f getMotion() {
        return this.motion;
    }

    public void setMotion(Vector3f motion) {
        this.motion = motion;
    }

    public float getYaw() {
        return this.yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean equals(Object o) {
        if (o instanceof Entity) {
            Entity entity = (Entity) o;
            return this.id.equals(entity.id) && Float.compare(this.position.x, entity.position.x) == 0 && Float.compare(this.position.y, entity.position.y) == 0 && Float.compare(this.position.z, entity.position.z) == 0;
        }
        return false;
    }

    public int hashCode() {
        int x = ((int) this.position.x) + 17;
        int y = ((int) this.position.y) + 19;
        int z = ((int) this.position.z) + 23;
        return ((x & 1023) << 20) | ((y & 1023) << 10) | (z & 1023);
    }

    public float getDistance(Vector3f position) {
        return Distance.getDistanceBetweenPoints(this.position, position, 1000.0f);
    }
}
