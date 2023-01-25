package com.mcal.worldcraft.chunk.entity;

import androidx.annotation.NonNull;

import com.mcal.droid.rugl.util.geom.Vector3f;
import com.mcal.worldcraft.factories.DescriptionFactory;
import com.mcal.worldcraft.nbt.Tag;
import com.mcal.worldcraft.util.Distance;

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
        tag = null;
        tags = null;
        position = new Vector3f();
        motion = new Vector3f();
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
        yaw = (Float) rotation[0].getValue();
        pitch = (Float) rotation[1].getValue();
    }

    private void parsePosition(@NonNull Tag tag) {
        Tag[] pos = (Tag[]) tag.findTagByName(POS).getValue();
        position.x = ((Double) pos[0].getValue()).floatValue();
        position.y = ((Double) pos[1].getValue()).floatValue();
        position.z = ((Double) pos[2].getValue()).floatValue();
    }

    private void parseMotion(@NonNull Tag tag) {
        Tag[] motionTag = (Tag[]) tag.findTagByName(MOTION).getValue();
        motion.x = ((Double) motionTag[0].getValue()).floatValue();
        motion.y = ((Double) motionTag[1].getValue()).floatValue();
        motion.z = ((Double) motionTag[2].getValue()).floatValue();
    }

    public Tag getTag() {
        if (tags == null) {
            createTag();
        }
        updateTags();
        return tag;
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
        if (tags[0] == null) {
            tags[0] = new Tag(Tag.Type.TAG_String, "id", id);
        }
    }

    private void updateEndTag() {
        tags[getEndTagIndex()] = new Tag(Tag.Type.TAG_End, null, (Tag[]) null);
    }

    private void updateExtra() {
        Map<String, Tag> extraTags = getExtraTags();
        if (extraTags != null) {
            int i = 0;
            for (Tag tag : extraTags.values()) {
                tags[i + 4] = tag;
                i++;
            }
        }
    }

    private void updateRotationTag() {
        if (tags[3] == null) {
            tags[3] = serializeRotation();
            return;
        }
        Tag[] rotationTag = (Tag[]) tags[3].getValue();
        rotationTag[0].setValue(yaw);
        rotationTag[1].setValue(pitch);
    }

    private void updateMotionTag() {
        if (tags[2] == null) {
            tags[2] = serializeMotion();
            return;
        }
        Tag[] motionTag = (Tag[]) tags[2].getValue();
        motionTag[0].setValue(motion.x);
        motionTag[1].setValue(motion.y);
        motionTag[2].setValue(motion.z);
    }

    private void updatePositionTag() {
        if (tags[1] == null) {
            tags[1] = serializePosition();
            return;
        }
        Tag[] positionTag = (Tag[]) tags[1].getValue();
        positionTag[0].setValue(position.x);
        positionTag[1].setValue(position.y);
        positionTag[2].setValue(position.z);
    }

    private int getEndTagIndex() {
        return getTagsCount() - 1;
    }

    private void createTag() {
        tags = new Tag[getTagsCount()];
        tag = new Tag(Tag.Type.TAG_Compound, DescriptionFactory.emptyText, tags);
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
        rotationList.addTag(new Tag(Tag.Type.TAG_Float, DescriptionFactory.emptyText, yaw));
        rotationList.addTag(new Tag(Tag.Type.TAG_Float, DescriptionFactory.emptyText, pitch));
        return rotationList;
    }

    @NonNull
    private Tag serializeMotion() {
        Tag motionList = new Tag(MOTION, Tag.Type.TAG_Double);
        motionList.addTag(new Tag(Tag.Type.TAG_Double, DescriptionFactory.emptyText, (double) motion.x));
        motionList.addTag(new Tag(Tag.Type.TAG_Double, DescriptionFactory.emptyText, (double) motion.y));
        motionList.addTag(new Tag(Tag.Type.TAG_Double, DescriptionFactory.emptyText, (double) motion.z));
        return motionList;
    }

    @NonNull
    private Tag serializePosition() {
        Tag positionList = new Tag(POS, Tag.Type.TAG_Double);
        positionList.addTag(new Tag(Tag.Type.TAG_Double, DescriptionFactory.emptyText, (double) position.x));
        positionList.addTag(new Tag(Tag.Type.TAG_Double, DescriptionFactory.emptyText, (double) position.y));
        positionList.addTag(new Tag(Tag.Type.TAG_Double, DescriptionFactory.emptyText, (double) position.z));
        return positionList;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public Vector3f getMotion() {
        return motion;
    }

    public void setMotion(Vector3f motion) {
        this.motion = motion;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean equals(Object o) {
        if (o instanceof Entity) {
            Entity entity = (Entity) o;
            return id.equals(entity.id) && Float.compare(position.x, entity.position.x) == 0 && Float.compare(position.y, entity.position.y) == 0 && Float.compare(position.z, entity.position.z) == 0;
        }
        return false;
    }

    public int hashCode() {
        int x = ((int) position.x) + 17;
        int y = ((int) position.y) + 19;
        int z = ((int) position.z) + 23;
        return ((x & 1023) << 20) | ((y & 1023) << 10) | (z & 1023);
    }

    public float getDistance(Vector3f position) {
        return Distance.getDistanceBetweenPoints(position, position, 1000.0f);
    }
}
