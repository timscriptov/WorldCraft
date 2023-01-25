package com.mcal.worldcraft.chunk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mcal.droid.rugl.util.FloatMath;
import com.mcal.droid.rugl.util.geom.Vector3f;
import com.mcal.droid.rugl.util.geom.Vector3i;
import com.mcal.worldcraft.GameMode;
import com.mcal.worldcraft.World;
import com.mcal.worldcraft.chunk.entity.EntityFactory;
import com.mcal.worldcraft.chunk.tile_entity.TileEntity;
import com.mcal.worldcraft.chunk.tile_entity.TileEntityFactory;
import com.mcal.worldcraft.factories.BlockFactory;
import com.mcal.worldcraft.factories.DescriptionFactory;
import com.mcal.worldcraft.mob.Mob;
import com.mcal.worldcraft.mob.MobFactory;
import com.mcal.worldcraft.mob.MobPainter;
import com.mcal.worldcraft.multiplayer.Multiplayer;
import com.mcal.worldcraft.nbt.Tag;
import com.mcal.worldcraft.util.RandomUtil;

import org.apache.commons.compress.archivers.cpio.CpioConstants;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A 16x16x128 chunk of blocks
 */
public class Chunk {
    public static final String ENTITIES_TAG_NAME = "Entities";
    public static final String TILE_ENTITIES_TAG_NAME = "TileEntities";
    private static final float CHANCE_TO_SPAWN_HOSTILE_MOBS = 0.25f;
    private static final float CHANCE_TO_SPAWN_PASSIVE_MOBS = 0.2f;
    /**
     * The child chunklets
     */
    public final Chunklet[] chunklets;
    private final List<Mob> hostileMobs;
    private final List<Mob> passiveMobs;
    private final List<Runnable> runnableList;
    /**
     * Block data
     */
    public byte[] blockData;
    /**
     * Blocklight data, remember it's only 4 bits per block
     */
    public byte[] blocklight;
    /**
     * World chunk x coordinate
     */
    public int chunkX;
    /**
     * World chunk z coordinate
     */
    public int chunkZ;
    public Tag ct;
    public byte[] data;
    /**
     * Skylight data, remember it's only 4 bits per block
     */
    public byte[] skylight;
    public List<TileEntity> tileEntities;
    public ArrayList<Vector3i> topLayer;
    public boolean wasChanged;
    /**
     * The parent world
     */
    public World world;
    private MobFactory hostileMobFactory;
    private Boolean isBlocksArrayInited;
    private boolean isLightCalculate;
    private MobFactory passiveMobFactory;

    /**
     * @param world
     * @param is
     * @throws IOException
     */
    public Chunk(World world, InputStream is) throws IOException {
        tileEntities = new ArrayList<>();
        wasChanged = false;
        topLayer = new ArrayList<>();
        isLightCalculate = false;
        passiveMobs = new ArrayList<>();
        hostileMobs = new ArrayList<>();
        isBlocksArrayInited = false;
        runnableList = new ArrayList<>();
        this.world = world;
        ct = Tag.readFrom(is, false);
        chunkX = (Integer) ct.findTagByName("xPos").getValue();
        chunkZ = (Integer) ct.findTagByName("zPos").getValue();
        blockData = (byte[]) ct.findTagByName("Blocks").getValue();
        Tag dataTag = ct.findTagByName("Data");
        if (dataTag != null) {
            data = (byte[]) dataTag.getValue();
            if (data == null || data.length != 32768) {
                data = new byte[32768];
            }
        } else {
            data = new byte[32768];
            wasChanged = true;
        }
        skylight = (byte[]) ct.findTagByName("SkyLight").getValue();
        blocklight = (byte[]) ct.findTagByName("BlockLight").getValue();
        if (GameMode.isSurvivalMode()) {
            parseEntities();
            parseTileEntities();
        }
        chunklets = new Chunklet[8];
        for (int i = 0; i < chunklets.length; i++) {
            chunklets[i] = new Chunklet(this, i);
        }
        isLightCalculate = true;
        isBlocksArrayInited = true;
    }

    public Chunk(World world, int chunkX, int chunkZ) throws IOException {
        tileEntities = new ArrayList<>();
        wasChanged = false;
        topLayer = new ArrayList<>();
        isLightCalculate = false;
        passiveMobs = new ArrayList<>();
        hostileMobs = new ArrayList<>();
        isBlocksArrayInited = false;
        this.runnableList = new ArrayList<>();
        this.world = world;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        blockData = new byte[32768];
        data = new byte[32768];
        blocklight = new byte[16384];
        skylight = new byte[16384];
        ct = createTag();
        chunklets = new Chunklet[8];
        for (int i = 0; i < chunklets.length; i++) {
            chunklets[i] = new Chunklet(this, i);
        }
        wasChanged = true;
        isLightCalculate = false;
        isBlocksArrayInited = false;
        if (GameMode.isSurvivalMode()) {
            executeOnInited(() -> world.executeOnInited(() -> MobPainter.executeOnInited(() -> {
                spawnPassiveMobs(true);
                if (world.isNightNow()) {
                    spawnHostileMobs(true);
                }
            })));
        }
    }


    private void executeOnInited(Runnable runnable) {
        if (runnable != null) {
            synchronized (isBlocksArrayInited) {
                if (isBlocksArrayInited) {
                    runnable.run();
                } else {
                    runnableList.add(runnable);
                }
            }
        }
    }

    public void onPostGenerated() {
        synchronized (isBlocksArrayInited) {
            isBlocksArrayInited = true;
            Iterator<Runnable> iterator = runnableList.iterator();
            while (iterator.hasNext()) {
                Runnable runnable = iterator.next();
                runnable.run();
                iterator.remove();
            }
        }
    }

    private void parseEntities() {
        try {
            Tag entitiesTag = ct.findTagByName(ENTITIES_TAG_NAME);
            if (entitiesTag != null) {
                Tag[] arr$ = (Tag[]) entitiesTag.getValue();
                for (Tag tag : arr$) {
                    addMob(EntityFactory.getMob(tag));
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void addMob(final Mob mob) {
        if (mob != null) {
            MobPainter.executeOnInited(() -> world.executeOnInited(() -> {
                if (mob.isPassive()) {
                    if (passiveMobFactory == null) {
                        passiveMobFactory = MobPainter.getFactory(mob);
                    }
                    if (passiveMobFactory != null && getPassiveMobCountCanBeSpawned() > 0) {
                        passiveMobs.add(MobPainter.getFactory(mob).addMob(mob));
                    }
                } else if (mob.isHostile()) {
                    if (hostileMobFactory == null) {
                        hostileMobFactory = MobPainter.getFactory(mob);
                    }
                    if (hostileMobFactory != null && getHostileMobCountCanBeSpawned() > 0) {
                        hostileMobs.add(MobPainter.getFactory(mob).addMob(mob));
                    }
                }
            }));
        }
    }

    private void parseTileEntities() {
        Tag[] values;
        try {
            Tag tileEntityTag = ct.findTagByName(TILE_ENTITIES_TAG_NAME);
            if (tileEntityTag != null && (values = (Tag[]) tileEntityTag.getValue()) != null) {
                for (Tag tag : values) {
                    TileEntity tileEntity = TileEntityFactory.parse(tag);
                    tileEntities.add(tileEntity);
                    world.addTileEntity(tileEntity);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public boolean isOldMap() {
        if (skylight == null) {
            return true;
        }
        for (int i = 0; i < skylight.length; i++) {
            if (skylight[i] != 0) {
                return false;
            }
        }
        return true;
    }

    public void clearBlockLight() {
        blocklight = new byte[16384];
    }

    @NonNull
    private Tag createTag() {
        Tag[] tags = {new Tag(Tag.Type.TAG_Int, "xPos", chunkX), new Tag(Tag.Type.TAG_Int, "zPos", chunkZ), new Tag(Tag.Type.TAG_Byte_Array, "Blocks", blockData), new Tag(Tag.Type.TAG_Byte_Array, "Data", data), new Tag(Tag.Type.TAG_Byte_Array, "SkyLight", skylight), new Tag(Tag.Type.TAG_Byte_Array, "BlockLight", blocklight), serializeEntities(), serializeTileEntities(), new Tag(Tag.Type.TAG_End, null, null)};
        Tag level = new Tag(Tag.Type.TAG_Compound, "Level", tags);
        return new Tag(Tag.Type.TAG_Compound, DescriptionFactory.emptyText, new Tag[]{level, new Tag(Tag.Type.TAG_End, null, null)});
    }

    public Tag serializeEntities() {
        Tag list = new Tag(ENTITIES_TAG_NAME, Tag.Type.TAG_Compound);
        for (Mob mob : passiveMobs) {
            list.addTag(mob.getEntity().getTag());
        }
        for (Mob mob2 : hostileMobs) {
            if (mob2 != null && mob2.getEntity() != null) {
                list.addTag(mob2.getEntity().getTag());
            }
        }
        return list;
    }

    public Tag serializeTileEntities() {
        Tag list = new Tag(TILE_ENTITIES_TAG_NAME, Tag.Type.TAG_Compound);
        for (TileEntity tileEntity : tileEntities) {
            if (tileEntity != null) {
                list.addTag(tileEntity.getTag());
                world.removeTileEntity(tileEntity);
            }
        }
        return list;
    }

    /**
     * @param bx
     * @param by
     * @param bz
     * @return the type of the so-indexed block in this chunk
     */
    public byte blockType(int bx, int by, int bz) {
        if (bx < 0) {
            Chunk north = world.getChunk(chunkX - 1, chunkZ);
            if (north == null) {
                return (byte) 0;
            }
            return north.blockType(bx + 16, by, bz);
        } else if (bx >= 16) {
            Chunk south = world.getChunk(chunkX + 1, chunkZ);
            if (south != null) {
                return south.blockType(bx - 16, by, bz);
            }
            return (byte) 0;
        } else if (bz < 0) {
            Chunk east = world.getChunk(chunkX, chunkZ - 1);
            if (east != null) {
                return east.blockType(bx, by, bz + 16);
            }
            return (byte) 0;
        } else if (bz >= 16) {
            Chunk west = world.getChunk(chunkX, chunkZ + 1);
            if (west != null) {
                return west.blockType(bx, by, bz - 16);
            }
            return (byte) 0;
        } else if (by < 0 || by >= 128) {
            return (byte) 0;
        } else {
            return blockData[(bz * CpioConstants.C_IWUSR) + by + (bx * 2048)];
        }
    }

    public byte blockData(int bx, int by, int bz) {
        if (bx < 0) {
            Chunk north = world.getChunk(chunkX - 1, chunkZ);
            if (north == null) {
                return (byte) 0;
            }
            return north.blockData(bx + 16, by, bz);
        } else if (bx >= 16) {
            Chunk south = world.getChunk(chunkX + 1, chunkZ);
            if (south != null) {
                return south.blockData(bx - 16, by, bz);
            }
            return (byte) 0;
        } else if (bz < 0) {
            Chunk east = world.getChunk(chunkX, chunkZ - 1);
            if (east != null) {
                return east.blockData(bx, by, bz + 16);
            }
            return (byte) 0;
        } else if (bz >= 16) {
            Chunk west = world.getChunk(chunkX, chunkZ + 1);
            if (west != null) {
                return west.blockData(bx, by, bz - 16);
            }
            return (byte) 0;
        } else if (by < 0 || by >= 128) {
            return (byte) 0;
        } else {
            return data[(bz * CpioConstants.C_IWUSR) + by + (bx * 2048)];
        }
    }

    public Chunk blockChunk(int bx, int by, int bz) {
        if (bx < 0) {
            Chunk north = world.getChunk(chunkX - 1, chunkZ);
            if (north == null) {
                return null;
            }
            return north.blockChunk(bx + 16, by, bz);
        } else if (bx >= 16) {
            Chunk south = world.getChunk(chunkX + 1, chunkZ);
            if (south != null) {
                return south.blockChunk(bx - 16, by, bz);
            }
            return null;
        } else if (bz < 0) {
            Chunk east = world.getChunk(chunkX, chunkZ - 1);
            if (east != null) {
                return east.blockChunk(bx, by, bz + 16);
            }
            return null;
        } else if (bz >= 16) {
            Chunk west = world.getChunk(chunkX, chunkZ + 1);
            if (west != null) {
                return west.blockChunk(bx, by, bz - 16);
            }
            return null;
        } else {
            return this;
        }
    }

    private int getBlockDataIndex(int bx, int by, int bz) {
        return (bz * CpioConstants.C_IWUSR) + by + (bx * 2048);
    }

    public Set<Chunklet> setBlockTypeWithoutGeometryRecalculate(int bx, int by, int bz, byte blockType, byte blData) {
        Chunk south;
        Chunk west;
        if (bx < 0) {
            Chunk north = world.getChunk(chunkX - 1, chunkZ);
            if (north != null) {
                return north.setBlockTypeWithoutGeometryRecalculate(bx + 16, by, bz, blockType, blData);
            }
            return null;
        } else if (bx >= 16) {
            Chunk south2 = world.getChunk(chunkX + 1, chunkZ);
            if (south2 != null) {
                return south2.setBlockTypeWithoutGeometryRecalculate(bx - 16, by, bz, blockType, blData);
            }
            return null;
        } else if (bz < 0) {
            Chunk east = world.getChunk(chunkX, chunkZ - 1);
            if (east != null) {
                return east.setBlockTypeWithoutGeometryRecalculate(bx, by, bz + 16, blockType, blData);
            }
            return null;
        } else if (bz >= 16) {
            Chunk west2 = world.getChunk(chunkX, chunkZ + 1);
            if (west2 != null) {
                return west2.setBlockTypeWithoutGeometryRecalculate(bx, by, bz - 16, blockType, blData);
            }
            return null;
        } else if (by < 0 || by >= 128) {
            return null;
        } else {
            int index = getBlockDataIndex(bx, by, bz);
            blockData[index] = blockType;
            data[index] = blData;
            Set<Chunklet> chunkletSet = new HashSet<>();
            int cyi = by / 16;
            chunkletSet.add(chunklets[cyi]);
            if (bx == 0) {
                Chunk north2 = world.getChunk(chunkX - 1, chunkZ);
                if (north2 != null) {
                    chunkletSet.add(north2.chunklets[cyi]);
                }
            } else if (bx == 15 && (south = world.getChunk(chunkX + 1, chunkZ)) != null) {
                chunkletSet.add(south.chunklets[cyi]);
            }
            if (bz == 0) {
                Chunk east2 = world.getChunk(chunkX, chunkZ - 1);
                if (east2 != null) {
                    chunkletSet.add(east2.chunklets[cyi]);
                }
            } else if (bz == 15 && (west = world.getChunk(chunkX, chunkZ + 1)) != null) {
                chunkletSet.add(west.chunklets[cyi]);
            }
            if (by % 16 == 0 && cyi >= 1) {
                chunkletSet.add(chunklets[cyi - 1]);
            }
            if (by % 16 == 15 && cyi < 6) {
                chunkletSet.add(chunklets[cyi + 1]);
                return chunkletSet;
            }
            return chunkletSet;
        }
    }

    private void setBlockType(int bx, int by, int bz, byte blockType, byte blockData, boolean multiplayerSend) {
        Chunk south;
        Chunk west;
        if (GameMode.isMultiplayerMode() && multiplayerSend) {
            Multiplayer.setBlockType(bx, by, bz, chunkX, chunkZ, blockType, blockData, blockType(bx, by, bz), blockData(bx, by, bz));
        }
        int index = getBlockDataIndex(bx, by, bz);
        if (bx < 0) {
            Chunk north = world.getChunk(chunkX - 1, chunkZ);
            if (north != null) {
                north.setBlockType(bx + 16, by, bz, blockType, blockData, multiplayerSend);
            }
        } else if (bx >= 16) {
            Chunk south2 = world.getChunk(chunkX + 1, chunkZ);
            if (south2 != null) {
                south2.setBlockType(bx - 16, by, bz, blockType, blockData, multiplayerSend);
            }
        } else if (bz < 0) {
            Chunk east = world.getChunk(chunkX, chunkZ - 1);
            if (east != null) {
                east.setBlockType(bx, by, bz + 16, blockType, blockData, multiplayerSend);
            }
        } else if (bz >= 16) {
            Chunk west2 = world.getChunk(chunkX, this.chunkZ + 1);
            if (west2 != null) {
                west2.setBlockType(bx, by, bz - 16, blockType, blockData, multiplayerSend);
            }
        } else if (by >= 0 && by < 128) {
            this.blockData[index] = blockType;
            data[index] = blockData;
            if (blockType == BlockFactory.Block.Torch.id) {
                world.recalculateBlockLight(this, bx, by, bz);
            } else {
                world.recalculateBlockLight(this, bx, by, bz);
                world.recalculateSkyLight(this, bx, by, bz);
            }
            int cyi = by / 16;
            chunklets[cyi].geomDirty();
            chunklets[cyi].generateGeometry(true);
            // neighbours also dirty?
            if (bx == 0) {
                Chunk north2 = world.getChunk(chunkX - 1, chunkZ);
                if (north2 != null) {
                    north2.chunklets[cyi].geomDirty();
                    north2.chunklets[cyi].generateGeometry(true);
                }
            } else if (bx == 15 && (south = world.getChunk(chunkX + 1, chunkZ)) != null) {
                south.chunklets[cyi].geomDirty();
                south.chunklets[cyi].generateGeometry(true);
            }
            if (bz == 0) {
                Chunk east2 = world.getChunk(chunkX, chunkZ - 1);
                if (east2 != null) {
                    east2.chunklets[cyi].geomDirty();
                    east2.chunklets[cyi].generateGeometry(true);
                }
            } else if (bz == 15 && (west = world.getChunk(chunkX, chunkZ + 1)) != null) {
                west.chunklets[cyi].geomDirty();
                west.chunklets[cyi].generateGeometry(true);
            }
            if (by % 16 == 0 && cyi >= 1) {
                Chunklet below = chunklets[cyi - 1];
                below.geomDirty();
                below.generateGeometry(true);
            }
            if (by % 16 == 15 && cyi < 6) {
                Chunklet above = chunklets[cyi + 1];
                above.geomDirty();
                above.generateGeometry(true);
            }
            updateMobsLight();
        }
    }

    private void updateMobsLight() {
        MobFactory.updateMobsLight();
    }

    /**
     * @param x
     * @param y
     * @param z
     * @return The type of the block that contains the specified point
     */
    public byte blockTypeForPosition(float x, float y, float z) {
        return blockType((int) FloatMath.floor(x - (chunkX * 16)), (int) FloatMath.floor(y), (int) FloatMath.floor(z - (chunkZ * 16)));
    }

    public byte blockDataForPosition(float x, float y, float z) {
        return blockData((int) FloatMath.floor(x - (chunkX * 16)), (int) FloatMath.floor(y), (int) FloatMath.floor(z - (chunkZ * 16)));
    }

    public Chunk blockChunkForPosition(float x, float y, float z) {
        return blockChunk((int) FloatMath.floor(x - (chunkX * 16)), (int) FloatMath.floor(y), (int) FloatMath.floor(z - (chunkZ * 16)));
    }

    /**
     * @param x
     * @param y
     * @param z
     * @param blockType
     * @param blockData
     */
    public Set<Chunklet> setBlockTypeForPositionWithoutGeometryRecalculate(float x, float y, float z, byte blockType, byte blockData) {
        Set<Chunklet> result = setBlockTypeWithoutGeometryRecalculate((int) FloatMath.floor(x - (chunkX * 16)), (int) FloatMath.floor(y), (int) FloatMath.floor(z - (chunkZ * 16)), blockType, blockData);
        wasChanged = true;
        return result;
    }

    /**
     * @param x
     * @param y
     * @param z
     * @param blockType
     * @param blockData
     */
    public void setBlockTypeForPosition(float x, float y, float z, byte blockType, byte blockData) {
        setBlockTypeForPosition(x, y, z, blockType, blockData, true);
    }

    /**
     * @param x
     * @param y
     * @param z
     * @param blockType
     * @param blockData
     * @param multiplayerSend
     */
    public void setBlockTypeForPosition(float x, float y, float z, byte blockType, byte blockData, boolean multiplayerSend) {
        setBlockType((int) FloatMath.floor(x - (chunkX * 16)), (int) FloatMath.floor(y), (int) FloatMath.floor(z - (chunkZ * 16)), blockType, blockData, multiplayerSend);
        wasChanged = true;
    }

    /**
     * @param x
     * @param y
     * @param z
     * @param blockType
     * @param blockData
     */
    public void setBlockDataForPosition(float x, float y, float z, byte blockType, byte blockData) {
        setBlockDataForPosition(x, y, z, blockType, blockData, true);
    }

    /**
     * @param x
     * @param y
     * @param z
     * @param blockType
     * @param blockData
     * @param multiplayerSend
     */
    public void setBlockDataForPosition(float x, float y, float z, byte blockType, byte blockData, boolean multiplayerSend) {
        setBlockDataType((int) FloatMath.floor(x - (chunkX * 16)), (int) FloatMath.floor(y), (int) FloatMath.floor(z - (chunkZ * 16)), blockType, blockData, multiplayerSend);
        wasChanged = true;
    }

    /**
     * @param bx
     * @param by
     * @param bz
     * @param blockType
     * @param blockData
     * @param multiplayerSend
     */
    private void setBlockDataType(int bx, int by, int bz, byte blockType, byte blockData, boolean multiplayerSend) {
        int index = getBlockDataIndex(bx, by, bz);
        if (bx < 0) {
            Chunk north = world.getChunk(chunkX - 1, chunkZ);
            if (north != null) {
                north.setBlockDataType(bx + 16, by, bz, blockType, blockData, multiplayerSend);
            }
        } else if (bx >= 16) {
            Chunk south = world.getChunk(chunkX + 1, chunkZ);
            if (south != null) {
                south.setBlockDataType(bx - 16, by, bz, blockType, blockData, multiplayerSend);
            }
        } else if (bz < 0) {
            Chunk east = world.getChunk(chunkX, chunkZ - 1);
            if (east != null) {
                east.setBlockDataType(bx, by, bz + 16, blockType, blockData, multiplayerSend);
            }
        } else if (bz >= 16) {
            Chunk west = world.getChunk(chunkX, chunkZ + 1);
            if (west != null) {
                west.setBlockDataType(bx, by, bz - 16, blockType, blockData, multiplayerSend);
            }
        } else if (by >= 0 && by < 128) {
            data[index] = blockData;
        }
    }

    /**
     * @param bx
     * @param by
     * @param bz
     * @return The light contribution from torches, lava etc, in range 0-15
     */
    public int blockLight(int bx, int by, int bz) {
        if (bx < 0) {
            Chunk north = world.getChunk(chunkX - 1, chunkZ);
            return north == null ? 0 : north.blockLight(bx + 16, by, bz);
        } else if (bx >= 16) {
            Chunk south = world.getChunk(chunkX + 1, chunkZ);
            return south == null ? 0 : south.blockLight(bx - 16, by, bz);
        } else if (bz < 0) {
            Chunk east = world.getChunk(chunkX, chunkZ - 1);
            return east == null ? 0 : east.blockLight(bx, by, bz + 16);
        } else if (bz >= 16) {
            Chunk west = world.getChunk(chunkX, chunkZ + 1);
            return west == null ? 0 : west.blockLight(bx, by, bz - 16);
        } else if (by < 0 || by >= 128) {
            return 0;
        }

        int index = by + bz * 128 + bx * 2048;
        int hi = index / 2;
        boolean odd = (index & 1) != 0;
        if (odd) {
            return (blocklight[hi] & 0xf0) >> 4;
        } else {
            return blocklight[hi] & 0xf;
        }
    }

    public void setLightForBlock(int x, int y, int z, int light) {
        if (x < 0) {
            Chunk north = world.getChunk(chunkX - 1, chunkZ);
            if (north != null) {
                north.setLightForBlock(x + 16, y, z, light);
            }
        } else if (x >= 16) {
            Chunk south = world.getChunk(chunkX + 1, chunkZ);
            if (south != null) {
                south.setLightForBlock(x - 16, y, z, light);
            }
        } else if (z < 0) {
            Chunk east = world.getChunk(chunkX, chunkZ - 1);
            if (east != null) {
                east.setLightForBlock(x, y, z + 16, light);
            }
        } else if (z >= 16) {
            Chunk west = world.getChunk(chunkX, chunkZ + 1);
            if (west != null) {
                west.setLightForBlock(x, y, z - 16, light);
            }
        } else if (y >= 0 && y < 128) {
            int index = getBlockDataIndex(x, y, z);
            int hi = index / 2;
            boolean odd = (index & 1) != 0;
            if (odd) {
                byte[] bArr = blocklight;
                bArr[hi] = (byte) (bArr[hi] & 15);
                byte[] bArr2 = blocklight;
                bArr2[hi] = (byte) (bArr2[hi] | (light << 4));
                return;
            }
            byte[] bArr3 = blocklight;
            bArr3[hi] = (byte) (bArr3[hi] & 240);
            byte[] bArr4 = blocklight;
            bArr4[hi] = (byte) (bArr4[hi] | light);
        }
    }

    /**
     * @param bx
     * @param by
     * @param bz
     * @return The light contribution from the sky, in range 0-15
     */
    public int skyLight(int bx, int by, int bz) {
        if (bx < 0) {
            Chunk north = world.getChunk(chunkX - 1, chunkZ);
            return north == null ? 0 : north.skyLight(bx + 16, by, bz);
        } else if (bx >= 16) {
            Chunk south = world.getChunk(chunkX + 1, chunkZ);
            return south == null ? 0 : south.skyLight(bx - 16, by, bz);
        } else if (bz < 0) {
            Chunk east = world.getChunk(chunkX, chunkZ - 1);
            return east == null ? 0 : east.skyLight(bx, by, bz + 16);
        } else if (bz >= 16) {
            Chunk west = world.getChunk(chunkX, chunkZ + 1);
            return west == null ? 0 : west.skyLight(bx, by, bz - 16);
        } else if (by < 0 || by >= 128)
            return 0;

        int index = by + bz * 128 + bx * 2048;
        int hi = index / 2;
        boolean odd = (index & 1) != 0;

        if (odd) {
            return (skylight[hi] & 0xf0) >> 4;
        } else {
            return skylight[hi] & 0xf;
        }
    }

    public void setSkyLightForBlock(int x, int y, int z, int light) {
        if (x < 0) {
            Chunk north = world.getChunk(chunkX - 1, chunkZ);
            if (north != null) {
                north.setSkyLightForBlock(x + 16, y, z, light);
            }
        } else if (x >= 16) {
            Chunk south = world.getChunk(chunkX + 1, chunkZ);
            if (south != null) {
                south.setSkyLightForBlock(x - 16, y, z, light);
            }
        } else if (z < 0) {
            Chunk east = world.getChunk(chunkX, chunkZ - 1);
            if (east != null) {
                east.setSkyLightForBlock(x, y, z + 16, light);
            }
        } else if (z >= 16) {
            Chunk west = world.getChunk(chunkX, chunkZ + 1);
            if (west != null) {
                west.setSkyLightForBlock(x, y, z - 16, light);
            }
        } else if (y >= 0 && y < 128) {
            int index = getBlockDataIndex(x, y, z);
            int hi = index / 2;
            boolean odd = (index & 1) != 0;
            if (odd) {
                skylight[hi] = (byte) (skylight[hi] & 15);
                skylight[hi] = (byte) (skylight[hi] | (light << 4));
                return;
            }
            skylight[hi] = (byte) (skylight[hi] & 0xf0);
            skylight[hi] = (byte) (skylight[hi] | light);
        }
    }

    public void generateGeometry(boolean synchronous) {
        for (int i = 0; i < chunklets.length; i++) {
            chunklets[i].generateGeometry(synchronous);
        }
    }

    /**
     * Call this to refresh the geometry of the chunk
     */
    public void geomDirty() {
        for (int i = 0; i < chunklets.length; i++) {
            chunklets[i].geomDirty();
        }
    }

    /**
     * Destroys VBO handles
     */
    public void unload() {
        for (int i = 0; i < chunklets.length; i++) {
            chunklets[i].unload();
        }
        despawnMobs();
    }

    @NonNull
    public String toString() {
        return "Chunk ( " + chunkX + ", " + chunkZ + " )";
    }

    public boolean isGeomDirty() {
        for (int i = 0; i < chunklets.length; i++) {
            if (chunklets[i].geomDirty) {
                return true;
            }
        }
        return false;
    }

    public boolean isLightCalculate() {
        return isLightCalculate;
    }

    public void setLightCalculate(boolean isLightCalculate) {
        this.isLightCalculate = isLightCalculate;
    }

    public void calculateLight() {
        world.generateLight(this);
    }

    public boolean contains(float x, float z) {
        return (((int) x) >> 4) == chunkX && (((int) z) >> 4) == chunkZ;
    }

    public boolean contains(Vector3f position) {
        return position != null && contains(position.x, position.z);
    }

    public void addTileEntity(TileEntity tileEntity) {
        tileEntities.add(tileEntity);
    }

    public void addTileEntity(List<? extends TileEntity> tileEntities) {
        if (tileEntities != null && tileEntities.size() > 0) {
            this.tileEntities.addAll(tileEntities);
            wasChanged = true;
        }
    }

    private void setTileEntities(List<? extends TileEntity> tileEntities) {
        this.tileEntities.clear();
        addTileEntity(tileEntities);
    }

    public void save(World world) {
        if (wasChanged) {
            if (GameMode.isSurvivalMode()) {
                updateMobs();
                setTileEntities(world.getTileEntities(this));
            }
            new ChunkSaver(world, this).save();
        }
    }

    public void spawnPassiveMobs(boolean force) {
        if (allowsPassiveMobSpawn()) {
            if (force || RandomUtil.getChance(CHANCE_TO_SPAWN_PASSIVE_MOBS)) {
                if (passiveMobFactory == null) {
                    passiveMobFactory = MobPainter.getRandomPassiveMobFactory();
                }
                int passiveMobCountCanBeSpawned = getPassiveMobCountCanBeSpawned();
                if (passiveMobFactory != null && passiveMobCountCanBeSpawned > 0) {
                    int spawnMobCount = RandomUtil.getRandomInRangeInclusive(passiveMobFactory.getMinGroupSize(), passiveMobCountCanBeSpawned);
                    Vector3f spawnCenter = getRandomSpawnCenter();
                    for (Vector3f location : generateSpawnLocationList(spawnCenter, spawnMobCount)) {
                        addMob(passiveMobFactory.createMob(location));
                    }
                }
            }
        }
    }

    public void spawnHostileMobs(boolean force) {
        if (allowsHostileMobSpawn()) {
            if (force || RandomUtil.getChance(CHANCE_TO_SPAWN_HOSTILE_MOBS)) {
                if (hostileMobFactory == null) {
                    hostileMobFactory = MobPainter.getRandomHostileMobFactory();
                }
                if (hostileMobFactory != null && getHostileMobCountCanBeSpawned() > 0) {
                    Vector3f spawnCenter = getRandomSpawnCenter();
                    int mobCountToSpawn = RandomUtil.getRandomInRangeInclusive(hostileMobFactory.getMinGroupSize(), hostileMobFactory.getMaxGroupSize());
                    for (Vector3f location : generateSpawnLocationList(spawnCenter, mobCountToSpawn)) {
                        addMob(hostileMobFactory.createMob(location));
                    }
                }
            }
        }
    }

    public int getHostileMobCountCanBeSpawned() {
        if (hostileMobFactory == null) {
            return 0;
        }
        return hostileMobFactory.getMaxGroupSize() - hostileMobs.size();
    }

    public int getPassiveMobCountCanBeSpawned() {
        if (passiveMobFactory == null) {
            return 0;
        }
        return passiveMobFactory.getMaxGroupSize() - getPassiveMobCountAround();
    }

    private int getPassiveMobCountAround() {
        int mobCount = 0;
        for (int xPos = chunkX - 2; xPos <= chunkX + 2; xPos++) {
            for (int zPos = chunkZ - 2; zPos <= chunkZ + 2; zPos++) {
                Chunk chunk = world.getChunkByPos(xPos, zPos);
                if (chunk != null) {
                    mobCount += chunk.passiveMobs.size();
                }
            }
        }
        return mobCount;
    }

    private boolean allowsPassiveMobSpawn() {
        return chunkX % 3 == 0 && chunkZ % 3 == 0;
    }

    private boolean allowsHostileMobSpawn() {
        return true;
    }

    @NonNull
    private List<Vector3f> generateSpawnLocationList(Vector3f spawnCenter, int mobCountAllowedToSpawn) {
        List<Vector3f> spawnList = new ArrayList<>();
        for (int i = 0; i < mobCountAllowedToSpawn; i++) {
            Vector3f spawnLocation = generatePassiveMobSpawnLocation(spawnCenter, spawnList);
            if (spawnLocation != null) {
                spawnList.add(spawnLocation);
            }
        }
        return spawnList;
    }

    @Nullable
    private Vector3f generatePassiveMobSpawnLocation(@NonNull Vector3f spawnCenter, @NonNull List<Vector3f> spawnList) {
        Float yPos;
        Vector3f spawnLocation = new Vector3f();
        boolean isSpawnLocationIntersectsAnotherOne = false;
        int tryCount = 0;
        do {
            spawnLocation.x = spawnCenter.x + RandomUtil.getRandomSignedInRangeInclusive(0, 5);
            spawnLocation.z = spawnCenter.z + RandomUtil.getRandomSignedInRangeInclusive(0, 5);
            Iterator<Vector3f> i$ = spawnList.iterator();
            while (true) {
                if (!i$.hasNext()) {
                    break;
                }
                Vector3f pos = i$.next();
                if (Float.compare(spawnLocation.x, pos.x) == 0 && Float.compare(spawnLocation.z, pos.z) == 0) {
                    isSpawnLocationIntersectsAnotherOne = true;
                    break;
                }
            }
            tryCount++;
            if (!isSpawnLocationIntersectsAnotherOne) {
                break;
            }
        } while (tryCount < 10);
        if (!spawnLocation.isZeroVector() && (yPos = getPassiveMobSpawnLocationY(spawnLocation)) != null) {
            spawnLocation.y = yPos;
            return spawnLocation;
        }
        return null;
    }

    @Nullable
    private Float getPassiveMobSpawnLocationY(Vector3f spawnLocation) {
        float y = 126.0f;
        while (y > 0.0f && world.blockType(spawnLocation.x, y, spawnLocation.z) == 0) {
            y -= 1.0f;
        }
        if (y <= 0.0f) {
            return null;
        }
        return y + 1.0f;
    }

    @NonNull
    private Vector3f getRandomSpawnCenter() {
        Vector3f spawnCenter = new Vector3f();
        spawnCenter.x = (chunkX * 16) + RandomUtil.getRandomInRangeInclusive(5, 10);
        spawnCenter.z = (chunkZ * 16) + RandomUtil.getRandomInRangeInclusive(5, 10);
        return spawnCenter;
    }

    public void updateMobs() {
        boolean changed = removeDeadPassiveMobs();
        if (changed | removeDeadHostileMobs() | updatePassiveMobChunks() | updateHostileMobChunks() | (passiveMobs.size() > 0) | (hostileMobs.size() > 0)) {
            wasChanged = true;
        }
    }

    private boolean updatePassiveMobChunks() {
        return updateMobChunks(true);
    }

    private boolean updateHostileMobChunks() {
        return updateMobChunks(false);
    }

    private boolean updateMobChunks(boolean updatePassiveMobs) {
        boolean modificated = false;
        Iterator<Mob> iterator = (updatePassiveMobs ? passiveMobs : hostileMobs).iterator();
        while (iterator.hasNext()) {
            Mob mob = iterator.next();
            if (mob == null) {
                iterator.remove();
                modificated = true;
            } else {
                Chunk chunk = world.getChunk(mob.getChunkX(), mob.getChunkZ());
                if (chunk != null && (chunk.chunkX != chunkX || chunk.chunkZ != chunkZ)) {
                    if (updatePassiveMobs) {
                        chunk.passiveMobs.add(mob);
                    } else {
                        chunk.hostileMobs.add(mob);
                    }
                    iterator.remove();
                    modificated = true;
                }
            }
        }
        return modificated;
    }

    private boolean removeDeadPassiveMobs() {
        return removeDeadMobs(passiveMobs);
    }

    private boolean removeDeadHostileMobs() {
        return removeDeadMobs(hostileMobs);
    }

    private boolean removeDeadMobs(@NonNull List<Mob> mobList) {
        boolean modificated = false;
        Iterator<Mob> iterator = mobList.iterator();
        while (iterator.hasNext()) {
            Mob mob = iterator.next();
            if (mob != null && mob.isDead()) {
                iterator.remove();
                modificated = true;
            }
        }
        return modificated;
    }

    private void despawnMobs() {
        despawnPassiveMobs();
        despawnHostileMobs();
    }

    private void despawnPassiveMobs() {
        for (Mob mob : passiveMobs) {
            if (passiveMobFactory != null) {
                passiveMobFactory.despawnMob(mob);
            }
        }
    }

    private void despawnHostileMobs() {
        for (Mob mob : hostileMobs) {
            if (hostileMobFactory != null) {
                hostileMobFactory.despawnMob(mob);
            }
        }
    }

    public boolean equals(Object o) {
        if (o instanceof Chunk) {
            Chunk c = (Chunk) o;
            return c.chunkX == chunkX && c.chunkZ == chunkZ;
        }
        return false;
    }
}
