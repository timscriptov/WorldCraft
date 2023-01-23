package com.solverlabs.worldcraft.chunk;

import androidx.annotation.NonNull;

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
        if (chunk != null && chunk.wasChanged) {
            DataOutputStream os = RegionFileCache.getChunkDataOutputStream(world.dir, chunk.chunkX, chunk.chunkZ);
            Tag tag = chunk.ct;
            if (tag != null) {
                tag.findTagByName("Blocks").setValue(chunk.blockData);
                tag.findTagByName("BlockLight").setValue(chunk.blocklight);
                tag.findTagByName("SkyLight").setValue(chunk.skylight);
                addDataTag(tag);
                addEntitiesTag(tag);
                addTileEntitiesTag(tag);
                try {
                    tag.writeTo(os, false);
                    chunk.wasChanged = false;
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void addDataTag(@NonNull Tag tag) {
        Tag dataTag = tag.findTagByName("Data");
        if (dataTag != null) {
            dataTag.setValue(chunk.data);
            return;
        }
        Tag[] tags = (Tag[]) tag.getValue();
        Tag[] newTags = new Tag[tags.length + 1];
        System.arraycopy(tags, 0, newTags, 0, tags.length);
        newTags[tags.length - 1] = new Tag(Tag.Type.TAG_Byte_Array, "Data", chunk.data);
        newTags[tags.length] = new Tag(Tag.Type.TAG_End, null, null);
        tag.setValue(newTags);
    }

    private void addEntitiesTag(@NonNull Tag tag) {
        tag.removeSubTag(tag.findTagByName(Chunk.ENTITIES_TAG_NAME));
        Tag[] tags = (Tag[]) tag.getValue();
        Tag[] newTags = new Tag[tags.length + 1];
        System.arraycopy(tags, 0, newTags, 0, tags.length);
        newTags[tags.length - 1] = chunk.serializeEntities();
        newTags[tags.length] = new Tag(Tag.Type.TAG_End, null, null);
        tag.setValue(newTags);
    }

    private void addTileEntitiesTag(@NonNull Tag tag) {
        tag.removeSubTag(tag.findTagByName(Chunk.TILE_ENTITIES_TAG_NAME));
        Tag[] tags = (Tag[]) tag.getValue();
        Tag[] newTags = new Tag[tags.length + 1];
        System.arraycopy(tags, 0, newTags, 0, tags.length);
        newTags[tags.length - 1] = chunk.serializeTileEntities();
        newTags[tags.length] = new Tag(Tag.Type.TAG_End, null, null);
        tag.setValue(newTags);
    }
}
