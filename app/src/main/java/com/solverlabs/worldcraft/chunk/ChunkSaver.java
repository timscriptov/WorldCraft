package com.solverlabs.worldcraft.chunk;

import com.solverlabs.worldcraft.World;
import com.solverlabs.worldcraft.nbt.RegionFileCache;
import com.solverlabs.worldcraft.nbt.Tag;

import java.io.DataOutputStream;


public class ChunkSaver {
    private final Chunk chunk;
    private final World world;

    public ChunkSaver(World world, Chunk chunk) {
        this.world = world;
        this.chunk = chunk;
    }

    public void save() {
        if (this.chunk != null && this.chunk.wasChanged) {
            DataOutputStream os = RegionFileCache.getChunkDataOutputStream(this.world.mDir, this.chunk.chunkX, this.chunk.chunkZ);
            Tag tag = this.chunk.ct;
            if (tag != null) {
                tag.findTagByName("Blocks").setValue(this.chunk.blockData);
                tag.findTagByName("BlockLight").setValue(this.chunk.blocklight);
                tag.findTagByName("SkyLight").setValue(this.chunk.skylight);
                addDataTag(tag);
                addEntitiesTag(tag);
                addTileEntitiesTag(tag);
                try {
                    tag.writeTo(os, false);
                    this.chunk.wasChanged = false;
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void addDataTag(Tag tag) {
        Tag dataTag = tag.findTagByName("Data");
        if (dataTag != null) {
            dataTag.setValue(this.chunk.data);
            return;
        }
        Tag[] tags = (Tag[]) tag.getValue();
        Tag[] newTags = new Tag[tags.length + 1];
        System.arraycopy(tags, 0, newTags, 0, tags.length);
        newTags[tags.length - 1] = new Tag(Tag.Type.TAG_Byte_Array, "Data", this.chunk.data);
        newTags[tags.length] = new Tag(Tag.Type.TAG_End, (String) null, (Tag[]) null);
        tag.setValue(newTags);
    }

    private void addEntitiesTag(Tag tag) {
        tag.removeSubTag(tag.findTagByName(Chunk.ENTITIES_TAG_NAME));
        Tag[] tags = (Tag[]) tag.getValue();
        Tag[] newTags = new Tag[tags.length + 1];
        System.arraycopy(tags, 0, newTags, 0, tags.length);
        newTags[tags.length - 1] = this.chunk.serializeEntities();
        newTags[tags.length] = new Tag(Tag.Type.TAG_End, (String) null, (Tag[]) null);
        tag.setValue(newTags);
    }

    private void addTileEntitiesTag(Tag tag) {
        tag.removeSubTag(tag.findTagByName(Chunk.TILE_ENTITIES_TAG_NAME));
        Tag[] tags = (Tag[]) tag.getValue();
        Tag[] newTags = new Tag[tags.length + 1];
        System.arraycopy(tags, 0, newTags, 0, tags.length);
        newTags[tags.length - 1] = this.chunk.serializeTileEntities();
        newTags[tags.length] = new Tag(Tag.Type.TAG_End, (String) null, (Tag[]) null);
        tag.setValue(newTags);
    }
}
