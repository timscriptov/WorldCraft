package com.solverlabs.worldcraft.chunk;

import com.solverlabs.droid.rugl.util.geom.Vector3f;
import com.solverlabs.droid.rugl.util.geom.Vector3i;
import com.solverlabs.worldcraft.GameMode;
import com.solverlabs.worldcraft.World;
import com.solverlabs.worldcraft.chunk.entity.EntityFactory;
import com.solverlabs.worldcraft.chunk.tile_entity.TileEntity;
import com.solverlabs.worldcraft.chunk.tile_entity.TileEntityFactory;
import com.solverlabs.worldcraft.factories.BlockFactory;
import com.solverlabs.worldcraft.factories.DescriptionFactory;
import com.solverlabs.worldcraft.mob.Mob;
import com.solverlabs.worldcraft.mob.MobFactory;
import com.solverlabs.worldcraft.mob.MobPainter;
import com.solverlabs.worldcraft.multiplayer.Multiplayer;
import com.solverlabs.worldcraft.nbt.Tag;
import com.solverlabs.worldcraft.util.RandomUtil;

import org.apache.commons.compress.archivers.cpio.CpioConstants;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android2.util.FloatMath;


public class Chunk {
    public static final String ENTITIES_TAG_NAME = "Entities";
    public static final String TILE_ENTITIES_TAG_NAME = "TileEntities";
    private static final float CHANCE_TO_SPAWN_HOSTILE_MOBS = 0.25f;
    private static final float CHANCE_TO_SPAWN_PASSIVE_MOBS = 0.2f;
    public final Chunklet[] chunklets;
    public byte[] blockData;
    public byte[] blocklight;
    public int chunkX;
    public int chunkZ;
    public Tag ct;
    public byte[] data;
    public byte[] skylight;
    public List<TileEntity> tileEntities;
    public ArrayList<Vector3i> topLayer;
    public boolean wasChanged;
    public World world;
    private MobFactory hostileMobFactory;
    private List<Mob> hostileMobs;
    private Boolean isBlocksArrayInited;
    private boolean isLightCalculate;
    private MobFactory passiveMobFactory;
    private List<Mob> passiveMobs;
    private List<Runnable> runnableList;

    public Chunk(World world, InputStream is) throws IOException {
        this.tileEntities = new ArrayList();
        this.wasChanged = false;
        this.topLayer = new ArrayList<>();
        this.isLightCalculate = false;
        this.passiveMobs = new ArrayList();
        this.hostileMobs = new ArrayList();
        this.isBlocksArrayInited = false;
        this.runnableList = new ArrayList();
        this.world = world;
        this.ct = Tag.readFrom(is, false);
        this.chunkX = ((Integer) this.ct.findTagByName("xPos").getValue()).intValue();
        this.chunkZ = ((Integer) this.ct.findTagByName("zPos").getValue()).intValue();
        this.blockData = (byte[]) this.ct.findTagByName("Blocks").getValue();
        Tag dataTag = this.ct.findTagByName("Data");
        if (dataTag != null) {
            this.data = (byte[]) dataTag.getValue();
            if (this.data == null || this.data.length != 32768) {
                this.data = new byte[32768];
            }
        } else {
            this.data = new byte[32768];
            this.wasChanged = true;
        }
        this.skylight = (byte[]) this.ct.findTagByName("SkyLight").getValue();
        this.blocklight = (byte[]) this.ct.findTagByName("BlockLight").getValue();
        if (GameMode.isSurvivalMode()) {
            parseEntities();
            parseTileEntities();
        }
        this.chunklets = new Chunklet[8];
        for (int i = 0; i < this.chunklets.length; i++) {
            this.chunklets[i] = new Chunklet(this, i);
        }
        this.isLightCalculate = true;
        this.isBlocksArrayInited = true;
    }

    public Chunk(World world, int chunkX, int chunkZ) throws IOException {
        this.tileEntities = new ArrayList();
        this.wasChanged = false;
        this.topLayer = new ArrayList<>();
        this.isLightCalculate = false;
        this.passiveMobs = new ArrayList();
        this.hostileMobs = new ArrayList();
        this.isBlocksArrayInited = false;
        this.runnableList = new ArrayList();
        this.world = world;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.blockData = new byte[32768];
        this.data = new byte[32768];
        this.blocklight = new byte[16384];
        this.skylight = new byte[16384];
        this.ct = createTag();
        this.chunklets = new Chunklet[8];
        for (int i = 0; i < this.chunklets.length; i++) {
            this.chunklets[i] = new Chunklet(this, i);
        }
        this.wasChanged = true;
        this.isLightCalculate = false;
        this.isBlocksArrayInited = false;
        if (GameMode.isSurvivalMode()) {
            executeOnInited(new AnonymousClass1(world));
        }
    }

    private void executeOnInited(Runnable runnable) {
        if (runnable != null) {
            synchronized (this.isBlocksArrayInited) {
                if (this.isBlocksArrayInited.booleanValue()) {
                    runnable.run();
                } else {
                    this.runnableList.add(runnable);
                }
            }
        }
    }

    public void onPostGenerated() {
        synchronized (this.isBlocksArrayInited) {
            this.isBlocksArrayInited = true;
            Iterator<Runnable> iterator = this.runnableList.iterator();
            while (iterator.hasNext()) {
                Runnable runnable = iterator.next();
                runnable.run();
                iterator.remove();
            }
        }
    }

    private void parseEntities() {
        try {
            Tag entitiesTag = this.ct.findTagByName(ENTITIES_TAG_NAME);
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
            MobPainter.executeOnInited(new Runnable() {
                @Override
                public void run() {
                    Chunk.this.world.executeOnInited(new Runnable() {
                        @Override
                        public void run() {
                            if (mob.isPassive()) {
                                if (Chunk.this.passiveMobFactory == null) {
                                    Chunk.this.passiveMobFactory = MobPainter.getFactory(mob);
                                }
                                if (Chunk.this.passiveMobFactory != null && Chunk.this.getPassiveMobCountCanBeSpawned() > 0) {
                                    Chunk.this.passiveMobs.add(MobPainter.getFactory(mob).addMob(mob));
                                }
                            } else if (mob.isHostile()) {
                                if (Chunk.this.hostileMobFactory == null) {
                                    Chunk.this.hostileMobFactory = MobPainter.getFactory(mob);
                                }
                                if (Chunk.this.hostileMobFactory != null && Chunk.this.getHostileMobCountCanBeSpawned() > 0) {
                                    Chunk.this.hostileMobs.add(MobPainter.getFactory(mob).addMob(mob));
                                }
                            }
                        }
                    });
                }
            });
        }
    }

    private void parseTileEntities() {
        Tag[] values;
        try {
            Tag tileEntityTag = this.ct.findTagByName(TILE_ENTITIES_TAG_NAME);
            if (tileEntityTag != null && (values = (Tag[]) tileEntityTag.getValue()) != null) {
                for (Tag tag : values) {
                    TileEntity tileEntity = TileEntityFactory.parse(tag);
                    this.tileEntities.add(tileEntity);
                    this.world.addTileEntity(tileEntity);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public boolean isOldMap() {
        if (this.skylight == null) {
            return true;
        }
        for (int i = 0; i < this.skylight.length; i++) {
            if (this.skylight[i] != 0) {
                return false;
            }
        }
        return true;
    }

    public void clearBlockLight() {
        this.blocklight = new byte[16384];
    }

    private Tag createTag() {
        Tag[] tags = {new Tag(Tag.Type.TAG_Int, "xPos", Integer.valueOf(this.chunkX)), new Tag(Tag.Type.TAG_Int, "zPos", Integer.valueOf(this.chunkZ)), new Tag(Tag.Type.TAG_Byte_Array, "Blocks", this.blockData), new Tag(Tag.Type.TAG_Byte_Array, "Data", this.data), new Tag(Tag.Type.TAG_Byte_Array, "SkyLight", this.skylight), new Tag(Tag.Type.TAG_Byte_Array, "BlockLight", this.blocklight), serializeEntities(), serializeTileEntities(), new Tag(Tag.Type.TAG_End, (String) null, (Tag[]) null)};
        Tag level = new Tag(Tag.Type.TAG_Compound, "Level", tags);
        Tag res = new Tag(Tag.Type.TAG_Compound, DescriptionFactory.emptyText, new Tag[]{level, new Tag(Tag.Type.TAG_End, (String) null, (Tag[]) null)});
        return res;
    }

    public Tag serializeEntities() {
        Tag list = new Tag(ENTITIES_TAG_NAME, Tag.Type.TAG_Compound);
        for (Mob mob : this.passiveMobs) {
            list.addTag(mob.getEntity().getTag());
        }
        for (Mob mob2 : this.hostileMobs) {
            if (mob2 != null && mob2.getEntity() != null) {
                list.addTag(mob2.getEntity().getTag());
            }
        }
        return list;
    }

    public Tag serializeTileEntities() {
        Tag list = new Tag(TILE_ENTITIES_TAG_NAME, Tag.Type.TAG_Compound);
        for (TileEntity tileEntity : this.tileEntities) {
            if (tileEntity != null) {
                list.addTag(tileEntity.getTag());
                this.world.removeTileEntity(tileEntity);
            }
        }
        return list;
    }

    public byte blockType(int bx, int by, int bz) {
        if (bx < 0) {
            Chunk north = this.world.getChunk(this.chunkX - 1, this.chunkZ);
            if (north != null) {
                return north.blockType(bx + 16, by, bz);
            }
            return (byte) 0;
        } else if (bx >= 16) {
            Chunk south = this.world.getChunk(this.chunkX + 1, this.chunkZ);
            if (south == null) {
                return (byte) 0;
            }
            return south.blockType(bx - 16, by, bz);
        } else if (bz < 0) {
            Chunk east = this.world.getChunk(this.chunkX, this.chunkZ - 1);
            if (east == null) {
                return (byte) 0;
            }
            return east.blockType(bx, by, bz + 16);
        } else if (bz >= 16) {
            Chunk west = this.world.getChunk(this.chunkX, this.chunkZ + 1);
            if (west == null) {
                return (byte) 0;
            }
            return west.blockType(bx, by, bz - 16);
        } else if (by >= 0 && by < 128) {
            return this.blockData[(bz * CpioConstants.C_IWUSR) + by + (bx * 2048)];
        } else {
            return (byte) 0;
        }
    }

    public byte blockData(int bx, int by, int bz) {
        if (bx < 0) {
            Chunk north = this.world.getChunk(this.chunkX - 1, this.chunkZ);
            if (north != null) {
                return north.blockData(bx + 16, by, bz);
            }
            return (byte) 0;
        } else if (bx >= 16) {
            Chunk south = this.world.getChunk(this.chunkX + 1, this.chunkZ);
            if (south == null) {
                return (byte) 0;
            }
            return south.blockData(bx - 16, by, bz);
        } else if (bz < 0) {
            Chunk east = this.world.getChunk(this.chunkX, this.chunkZ - 1);
            if (east == null) {
                return (byte) 0;
            }
            return east.blockData(bx, by, bz + 16);
        } else if (bz >= 16) {
            Chunk west = this.world.getChunk(this.chunkX, this.chunkZ + 1);
            if (west == null) {
                return (byte) 0;
            }
            return west.blockData(bx, by, bz - 16);
        } else if (by >= 0 && by < 128) {
            return this.data[(bz * CpioConstants.C_IWUSR) + by + (bx * 2048)];
        } else {
            return (byte) 0;
        }
    }

    public Chunk blockChunk(int bx, int by, int bz) {
        if (bx < 0) {
            Chunk north = this.world.getChunk(this.chunkX - 1, this.chunkZ);
            if (north != null) {
                return north.blockChunk(bx + 16, by, bz);
            }
            return null;
        } else if (bx >= 16) {
            Chunk south = this.world.getChunk(this.chunkX + 1, this.chunkZ);
            if (south == null) {
                return null;
            }
            return south.blockChunk(bx - 16, by, bz);
        } else if (bz < 0) {
            Chunk east = this.world.getChunk(this.chunkX, this.chunkZ - 1);
            if (east == null) {
                return null;
            }
            return east.blockChunk(bx, by, bz + 16);
        } else if (bz < 16) {
            return this;
        } else {
            Chunk west = this.world.getChunk(this.chunkX, this.chunkZ + 1);
            if (west == null) {
                return null;
            }
            return west.blockChunk(bx, by, bz - 16);
        }
    }

    private int getBlockDataIndex(int bx, int by, int bz) {
        return (bz * CpioConstants.C_IWUSR) + by + (bx * 2048);
    }

    public Set<Chunklet> setBlockTypeWithoutGeometryRecalculate(int bx, int by, int bz, byte blockType, byte blData) {
        Chunk south;
        Chunk west;
        if (bx < 0) {
            Chunk north = this.world.getChunk(this.chunkX - 1, this.chunkZ);
            if (north != null) {
                return north.setBlockTypeWithoutGeometryRecalculate(bx + 16, by, bz, blockType, blData);
            }
            return null;
        } else if (bx >= 16) {
            Chunk south2 = this.world.getChunk(this.chunkX + 1, this.chunkZ);
            if (south2 != null) {
                return south2.setBlockTypeWithoutGeometryRecalculate(bx - 16, by, bz, blockType, blData);
            }
            return null;
        } else if (bz < 0) {
            Chunk east = this.world.getChunk(this.chunkX, this.chunkZ - 1);
            if (east != null) {
                return east.setBlockTypeWithoutGeometryRecalculate(bx, by, bz + 16, blockType, blData);
            }
            return null;
        } else if (bz >= 16) {
            Chunk west2 = this.world.getChunk(this.chunkX, this.chunkZ + 1);
            if (west2 != null) {
                return west2.setBlockTypeWithoutGeometryRecalculate(bx, by, bz - 16, blockType, blData);
            }
            return null;
        } else if (by < 0 || by >= 128) {
            return null;
        } else {
            int index = getBlockDataIndex(bx, by, bz);
            this.blockData[index] = blockType;
            this.data[index] = blData;
            Set<Chunklet> chunkletSet = new HashSet<>();
            int cyi = by / 16;
            chunkletSet.add(this.chunklets[cyi]);
            if (bx == 0) {
                Chunk north2 = this.world.getChunk(this.chunkX - 1, this.chunkZ);
                if (north2 != null) {
                    chunkletSet.add(north2.chunklets[cyi]);
                }
            } else if (bx == 15 && (south = this.world.getChunk(this.chunkX + 1, this.chunkZ)) != null) {
                chunkletSet.add(south.chunklets[cyi]);
            }
            if (bz == 0) {
                Chunk east2 = this.world.getChunk(this.chunkX, this.chunkZ - 1);
                if (east2 != null) {
                    chunkletSet.add(east2.chunklets[cyi]);
                }
            } else if (bz == 15 && (west = this.world.getChunk(this.chunkX, this.chunkZ + 1)) != null) {
                chunkletSet.add(west.chunklets[cyi]);
            }
            if (by % 16 == 0 && cyi >= 1) {
                chunkletSet.add(this.chunklets[cyi - 1]);
            }
            if (by % 16 == 15 && cyi < 6) {
                chunkletSet.add(this.chunklets[cyi + 1]);
                return chunkletSet;
            }
            return chunkletSet;
        }
    }

    private void setBlockType(int bx, int by, int bz, byte blockType, byte blockData, boolean multiplayerSend) {
        Chunk south;
        Chunk west;
        if (GameMode.isMultiplayerMode() && multiplayerSend) {
            Multiplayer.setBlockType(bx, by, bz, this.chunkX, this.chunkZ, blockType, blockData, blockType(bx, by, bz), blockData(bx, by, bz));
        }
        int index = getBlockDataIndex(bx, by, bz);
        if (bx < 0) {
            Chunk north = this.world.getChunk(this.chunkX - 1, this.chunkZ);
            if (north != null) {
                north.setBlockType(bx + 16, by, bz, blockType, blockData, multiplayerSend);
            }
        } else if (bx >= 16) {
            Chunk south2 = this.world.getChunk(this.chunkX + 1, this.chunkZ);
            if (south2 != null) {
                south2.setBlockType(bx - 16, by, bz, blockType, blockData, multiplayerSend);
            }
        } else if (bz < 0) {
            Chunk east = this.world.getChunk(this.chunkX, this.chunkZ - 1);
            if (east != null) {
                east.setBlockType(bx, by, bz + 16, blockType, blockData, multiplayerSend);
            }
        } else if (bz >= 16) {
            Chunk west2 = this.world.getChunk(this.chunkX, this.chunkZ + 1);
            if (west2 != null) {
                west2.setBlockType(bx, by, bz - 16, blockType, blockData, multiplayerSend);
            }
        } else if (by >= 0 && by < 128) {
            this.blockData[index] = blockType;
            this.data[index] = blockData;
            if (blockType == BlockFactory.Block.Torch.id) {
                this.world.recalculateBlockLight(this, bx, by, bz);
            } else {
                this.world.recalculateBlockLight(this, bx, by, bz);
                this.world.recalculateSkyLight(this, bx, by, bz);
            }
            int cyi = by / 16;
            this.chunklets[cyi].geomDirty();
            this.chunklets[cyi].generateGeometry(true);
            if (bx == 0) {
                Chunk north2 = this.world.getChunk(this.chunkX - 1, this.chunkZ);
                if (north2 != null) {
                    north2.chunklets[cyi].geomDirty();
                    north2.chunklets[cyi].generateGeometry(true);
                }
            } else if (bx == 15 && (south = this.world.getChunk(this.chunkX + 1, this.chunkZ)) != null) {
                south.chunklets[cyi].geomDirty();
                south.chunklets[cyi].generateGeometry(true);
            }
            if (bz == 0) {
                Chunk east2 = this.world.getChunk(this.chunkX, this.chunkZ - 1);
                if (east2 != null) {
                    east2.chunklets[cyi].geomDirty();
                    east2.chunklets[cyi].generateGeometry(true);
                }
            } else if (bz == 15 && (west = this.world.getChunk(this.chunkX, this.chunkZ + 1)) != null) {
                west.chunklets[cyi].geomDirty();
                west.chunklets[cyi].generateGeometry(true);
            }
            if (by % 16 == 0 && cyi >= 1) {
                Chunklet below = this.chunklets[cyi - 1];
                below.geomDirty();
                below.generateGeometry(true);
            }
            if (by % 16 == 15 && cyi < 6) {
                Chunklet above = this.chunklets[cyi + 1];
                above.geomDirty();
                above.generateGeometry(true);
            }
            updateMobsLight();
        }
    }

    private void updateMobsLight() {
        MobFactory.updateMobsLight();
    }

    public byte blockTypeForPosition(float x, float y, float z) {
        return blockType((int) FloatMath.floor(x - (this.chunkX * 16)), (int) FloatMath.floor(y), (int) FloatMath.floor(z - (this.chunkZ * 16)));
    }

    public byte blockDataForPosition(float x, float y, float z) {
        return blockData((int) FloatMath.floor(x - (this.chunkX * 16)), (int) FloatMath.floor(y), (int) FloatMath.floor(z - (this.chunkZ * 16)));
    }

    public Chunk blockChunkForPosition(float x, float y, float z) {
        return blockChunk((int) FloatMath.floor(x - (this.chunkX * 16)), (int) FloatMath.floor(y), (int) FloatMath.floor(z - (this.chunkZ * 16)));
    }

    public Set<Chunklet> setBlockTypeForPositionWithoutGeometryRecalculate(float x, float y, float z, byte blockType, byte blockData) {
        Set<Chunklet> result = setBlockTypeWithoutGeometryRecalculate((int) FloatMath.floor(x - (this.chunkX * 16)), (int) FloatMath.floor(y), (int) FloatMath.floor(z - (this.chunkZ * 16)), blockType, blockData);
        this.wasChanged = true;
        return result;
    }

    public void setBlockTypeForPosition(float x, float y, float z, byte blockType, byte blockData) {
        setBlockTypeForPosition(x, y, z, blockType, blockData, true);
    }

    public void setBlockTypeForPosition(float x, float y, float z, byte blockType, byte blockData, boolean multiplayerSend) {
        setBlockType((int) FloatMath.floor(x - (this.chunkX * 16)), (int) FloatMath.floor(y), (int) FloatMath.floor(z - (this.chunkZ * 16)), blockType, blockData, multiplayerSend);
        this.wasChanged = true;
    }

    public void setBlockDataForPosition(float x, float y, float z, byte blockType, byte blockData) {
        setBlockDataForPosition(x, y, z, blockType, blockData, true);
    }

    public void setBlockDataForPosition(float x, float y, float z, byte blockType, byte blockData, boolean multiplayerSend) {
        setBlockDataType((int) FloatMath.floor(x - (this.chunkX * 16)), (int) FloatMath.floor(y), (int) FloatMath.floor(z - (this.chunkZ * 16)), blockType, blockData, multiplayerSend);
        this.wasChanged = true;
    }

    private void setBlockDataType(int bx, int by, int bz, byte blockType, byte blockData, boolean multiplayerSend) {
        int index = getBlockDataIndex(bx, by, bz);
        if (bx < 0) {
            Chunk north = this.world.getChunk(this.chunkX - 1, this.chunkZ);
            if (north != null) {
                north.setBlockDataType(bx + 16, by, bz, blockType, blockData, multiplayerSend);
            }
        } else if (bx >= 16) {
            Chunk south = this.world.getChunk(this.chunkX + 1, this.chunkZ);
            if (south != null) {
                south.setBlockDataType(bx - 16, by, bz, blockType, blockData, multiplayerSend);
            }
        } else if (bz < 0) {
            Chunk east = this.world.getChunk(this.chunkX, this.chunkZ - 1);
            if (east != null) {
                east.setBlockDataType(bx, by, bz + 16, blockType, blockData, multiplayerSend);
            }
        } else if (bz >= 16) {
            Chunk west = this.world.getChunk(this.chunkX, this.chunkZ + 1);
            if (west != null) {
                west.setBlockDataType(bx, by, bz - 16, blockType, blockData, multiplayerSend);
            }
        } else if (by >= 0 && by < 128) {
            this.data[index] = blockData;
        }
    }

    public int blockLight(int bx, int by, int bz) {
        if (bx < 0) {
            Chunk north = this.world.getChunk(this.chunkX - 1, this.chunkZ);
            if (north != null) {
                return north.blockLight(bx + 16, by, bz);
            }
            return 0;
        } else if (bx >= 16) {
            Chunk south = this.world.getChunk(this.chunkX + 1, this.chunkZ);
            if (south == null) {
                return 0;
            }
            return south.blockLight(bx - 16, by, bz);
        } else if (bz < 0) {
            Chunk east = this.world.getChunk(this.chunkX, this.chunkZ - 1);
            if (east == null) {
                return 0;
            }
            return east.blockLight(bx, by, bz + 16);
        } else if (bz >= 16) {
            Chunk west = this.world.getChunk(this.chunkX, this.chunkZ + 1);
            if (west == null) {
                return 0;
            }
            return west.blockLight(bx, by, bz - 16);
        } else if (by < 0 || by >= 128) {
            return 0;
        } else {
            int index = getBlockDataIndex(bx, by, bz);
            int hi = index / 2;
            boolean odd = (index & 1) != 0;
            if (odd) {
                return (this.blocklight[hi] & 240) >> 4;
            }
            return this.blocklight[hi] & 15;
        }
    }

    public void setLightForBlock(int x, int y, int z, int light) {
        if (x < 0) {
            Chunk north = this.world.getChunk(this.chunkX - 1, this.chunkZ);
            if (north != null) {
                north.setLightForBlock(x + 16, y, z, light);
            }
        } else if (x >= 16) {
            Chunk south = this.world.getChunk(this.chunkX + 1, this.chunkZ);
            if (south != null) {
                south.setLightForBlock(x - 16, y, z, light);
            }
        } else if (z < 0) {
            Chunk east = this.world.getChunk(this.chunkX, this.chunkZ - 1);
            if (east != null) {
                east.setLightForBlock(x, y, z + 16, light);
            }
        } else if (z >= 16) {
            Chunk west = this.world.getChunk(this.chunkX, this.chunkZ + 1);
            if (west != null) {
                west.setLightForBlock(x, y, z - 16, light);
            }
        } else if (y >= 0 && y < 128) {
            int index = getBlockDataIndex(x, y, z);
            int hi = index / 2;
            boolean odd = (index & 1) != 0;
            if (odd) {
                byte[] bArr = this.blocklight;
                bArr[hi] = (byte) (bArr[hi] & 15);
                byte[] bArr2 = this.blocklight;
                bArr2[hi] = (byte) (bArr2[hi] | (light << 4));
                return;
            }
            byte[] bArr3 = this.blocklight;
            bArr3[hi] = (byte) (bArr3[hi] & 240);
            byte[] bArr4 = this.blocklight;
            bArr4[hi] = (byte) (bArr4[hi] | light);
        }
    }

    public int skyLight(int bx, int by, int bz) {
        if (bx < 0) {
            Chunk north = this.world.getChunk(this.chunkX - 1, this.chunkZ);
            if (north != null) {
                return north.skyLight(bx + 16, by, bz);
            }
            return 0;
        } else if (bx >= 16) {
            Chunk south = this.world.getChunk(this.chunkX + 1, this.chunkZ);
            if (south == null) {
                return 0;
            }
            return south.skyLight(bx - 16, by, bz);
        } else if (bz < 0) {
            Chunk east = this.world.getChunk(this.chunkX, this.chunkZ - 1);
            if (east == null) {
                return 0;
            }
            return east.skyLight(bx, by, bz + 16);
        } else if (bz >= 16) {
            Chunk west = this.world.getChunk(this.chunkX, this.chunkZ + 1);
            if (west == null) {
                return 0;
            }
            return west.skyLight(bx, by, bz - 16);
        } else if (by < 0 || by >= 128) {
            return 15;
        } else {
            int index = getBlockDataIndex(bx, by, bz);
            int hi = index / 2;
            boolean odd = (index & 1) != 0;
            if (odd) {
                return (this.skylight[hi] & 240) >> 4;
            }
            return this.skylight[hi] & 15;
        }
    }

    public void setSkyLightForBlock(int x, int y, int z, int light) {
        if (x < 0) {
            Chunk north = this.world.getChunk(this.chunkX - 1, this.chunkZ);
            if (north != null) {
                north.setSkyLightForBlock(x + 16, y, z, light);
            }
        } else if (x >= 16) {
            Chunk south = this.world.getChunk(this.chunkX + 1, this.chunkZ);
            if (south != null) {
                south.setSkyLightForBlock(x - 16, y, z, light);
            }
        } else if (z < 0) {
            Chunk east = this.world.getChunk(this.chunkX, this.chunkZ - 1);
            if (east != null) {
                east.setSkyLightForBlock(x, y, z + 16, light);
            }
        } else if (z >= 16) {
            Chunk west = this.world.getChunk(this.chunkX, this.chunkZ + 1);
            if (west != null) {
                west.setSkyLightForBlock(x, y, z - 16, light);
            }
        } else if (y >= 0 && y < 128) {
            int index = getBlockDataIndex(x, y, z);
            int hi = index / 2;
            boolean odd = (index & 1) != 0;
            if (odd) {
                byte[] bArr = this.skylight;
                bArr[hi] = (byte) (bArr[hi] & 15);
                byte[] bArr2 = this.skylight;
                bArr2[hi] = (byte) (bArr2[hi] | (light << 4));
                return;
            }
            byte[] bArr3 = this.skylight;
            bArr3[hi] = (byte) (bArr3[hi] & 240);
            byte[] bArr4 = this.skylight;
            bArr4[hi] = (byte) (bArr4[hi] | light);
        }
    }

    public void generateGeometry(boolean synchronous) {
        for (int i = 0; i < this.chunklets.length; i++) {
            this.chunklets[i].generateGeometry(synchronous);
        }
    }

    public void geomDirty() {
        for (int i = 0; i < this.chunklets.length; i++) {
            this.chunklets[i].geomDirty();
        }
    }

    public void unload() {
        for (int i = 0; i < this.chunklets.length; i++) {
            this.chunklets[i].unload();
        }
        despawnMobs();
    }

    public String toString() {
        return "Chunk ( " + this.chunkX + ", " + this.chunkZ + " )";
    }

    public boolean isGeomDirty() {
        for (int i = 0; i < this.chunklets.length; i++) {
            if (this.chunklets[i].geomDirty) {
                return true;
            }
        }
        return false;
    }

    public boolean isLightCalculate() {
        return this.isLightCalculate;
    }

    public void setLightCalculate(boolean isLightCalculate) {
        this.isLightCalculate = isLightCalculate;
    }

    public void calculateLight() {
        this.world.generateLight(this);
    }

    public boolean contains(float x, float z) {
        return (((int) x) >> 4) == this.chunkX && (((int) z) >> 4) == this.chunkZ;
    }

    public boolean contains(Vector3f position) {
        return position != null && contains(position.x, position.z);
    }

    public void addTileEntity(TileEntity tileEntity) {
        this.tileEntities.add(tileEntity);
    }

    public void addTileEntity(List<? extends TileEntity> tileEntities) {
        if (tileEntities != null && tileEntities.size() > 0) {
            this.tileEntities.addAll(tileEntities);
            this.wasChanged = true;
        }
    }

    private void setTileEntities(List<? extends TileEntity> tileEntities) {
        this.tileEntities.clear();
        addTileEntity(tileEntities);
    }

    public void save(World world) {
        if (this.wasChanged) {
            if (GameMode.isSurvivalMode()) {
                updateMobs();
                setTileEntities(world.getTileEntities(this));
            }
            new ChunkSaver(world, this).save();
        }
    }

    public void spawnPassiveMobs(boolean force) {
        if (allowsPassiveMobSpawn()) {
            if (force || RandomUtil.getChance(0.2f)) {
                if (this.passiveMobFactory == null) {
                    this.passiveMobFactory = MobPainter.getRandomPassiveMobFactory();
                }
                int passiveMobCountCanBeSpawned = getPassiveMobCountCanBeSpawned();
                if (this.passiveMobFactory != null && passiveMobCountCanBeSpawned > 0) {
                    int spawnMobCount = RandomUtil.getRandomInRangeInclusive(this.passiveMobFactory.getMinGroupSize(), passiveMobCountCanBeSpawned);
                    Vector3f spawnCenter = getRandomSpawnCenter();
                    for (Vector3f location : generateSpawnLocationList(spawnCenter, spawnMobCount)) {
                        addMob(this.passiveMobFactory.createMob(location));
                    }
                }
            }
        }
    }

    public void spawnHostileMobs(boolean force) {
        if (allowsHostileMobSpawn()) {
            if (force || RandomUtil.getChance(CHANCE_TO_SPAWN_HOSTILE_MOBS)) {
                if (this.hostileMobFactory == null) {
                    this.hostileMobFactory = MobPainter.getRandomHostileMobFactory();
                }
                if (this.hostileMobFactory != null && getHostileMobCountCanBeSpawned() > 0) {
                    Vector3f spawnCenter = getRandomSpawnCenter();
                    int mobCountToSpawn = RandomUtil.getRandomInRangeInclusive(this.hostileMobFactory.getMinGroupSize(), this.hostileMobFactory.getMaxGroupSize());
                    for (Vector3f location : generateSpawnLocationList(spawnCenter, mobCountToSpawn)) {
                        addMob(this.hostileMobFactory.createMob(location));
                    }
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int getHostileMobCountCanBeSpawned() {
        if (this.hostileMobFactory == null) {
            return 0;
        }
        return this.hostileMobFactory.getMaxGroupSize() - this.hostileMobs.size();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int getPassiveMobCountCanBeSpawned() {
        if (this.passiveMobFactory == null) {
            return 0;
        }
        return this.passiveMobFactory.getMaxGroupSize() - getPassiveMobCountAround();
    }

    private int getPassiveMobCountAround() {
        int mobCount = 0;
        for (int xPos = this.chunkX - 2; xPos <= this.chunkX + 2; xPos++) {
            for (int zPos = this.chunkZ - 2; zPos <= this.chunkZ + 2; zPos++) {
                Chunk chunk = this.world.getChunkByPos(xPos, zPos);
                if (chunk != null) {
                    mobCount += chunk.passiveMobs.size();
                }
            }
        }
        return mobCount;
    }

    private boolean allowsPassiveMobSpawn() {
        return this.chunkX % 3 == 0 && this.chunkZ % 3 == 0;
    }

    private boolean allowsHostileMobSpawn() {
        return true;
    }

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

    private Vector3f generatePassiveMobSpawnLocation(Vector3f spawnCenter, List<Vector3f> spawnList) {
        Float yPos;
        Vector3f spawnLocation = new Vector3f();
        boolean isSpawnLocationIntersectsAnotherOne = false;
        int tryCount = 0;
        do {
            spawnLocation.x = spawnCenter.x + RandomUtil.getRandomSignedInRangeInclusive(0, 5);
            spawnLocation.z = spawnCenter.z + RandomUtil.getRandomSignedInRangeInclusive(0, 5);
            Iterator<Vector3f> vector3fIterator = spawnList.iterator();
            while (true) {
                if (!vector3fIterator.hasNext()) {
                    break;
                }
                Vector3f pos = vector3fIterator.next();
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
            spawnLocation.y = yPos.floatValue();
            return spawnLocation;
        }
        return null;
    }

    private Float getPassiveMobSpawnLocationY(Vector3f spawnLocation) {
        float y = 126.0f;
        while (y > 0.0f && this.world.blockType(spawnLocation.x, y, spawnLocation.z) == 0) {
            y -= 1.0f;
        }
        if (y <= 0.0f) {
            return null;
        }
        return Float.valueOf(y + 1.0f);
    }

    private Vector3f getRandomSpawnCenter() {
        Vector3f spawnCenter = new Vector3f();
        spawnCenter.x = (this.chunkX * 16) + RandomUtil.getRandomInRangeInclusive(5, 10);
        spawnCenter.z = (this.chunkZ * 16) + RandomUtil.getRandomInRangeInclusive(5, 10);
        return spawnCenter;
    }

    public void updateMobs() {
        boolean z = false;
        boolean changed = false | removeDeadPassiveMobs();
        boolean changed2 = changed | removeDeadHostileMobs() | updatePassiveMobChunks() | updateHostileMobChunks() | (this.passiveMobs.size() > 0);
        if (this.hostileMobs.size() > 0) {
            z = true;
        }
        if (changed2 | z) {
            this.wasChanged = true;
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
        Iterator<Mob> iterator = (updatePassiveMobs ? this.passiveMobs : this.hostileMobs).iterator();
        while (iterator.hasNext()) {
            Mob mob = iterator.next();
            if (mob == null) {
                iterator.remove();
                modificated = true;
            } else {
                Chunk chunk = this.world.getChunk(mob.getChunkX(), mob.getChunkZ());
                if (chunk != null && (chunk.chunkX != this.chunkX || chunk.chunkZ != this.chunkZ)) {
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
        return removeDeadMobs(this.passiveMobs);
    }

    private boolean removeDeadHostileMobs() {
        return removeDeadMobs(this.hostileMobs);
    }

    private boolean removeDeadMobs(List<Mob> mobList) {
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
        for (Mob mob : this.passiveMobs) {
            if (this.passiveMobFactory != null) {
                this.passiveMobFactory.despawnMob(mob);
            }
        }
    }

    private void despawnHostileMobs() {
        for (Mob mob : this.hostileMobs) {
            if (this.hostileMobFactory != null) {
                this.hostileMobFactory.despawnMob(mob);
            }
        }
    }

    public boolean equals(Object o) {
        if (!(o instanceof Chunk)) {
            return false;
        }
        Chunk c = (Chunk) o;
        return c.chunkX == this.chunkX && c.chunkZ == this.chunkZ;
    }

    /* renamed from: com.solverlabs.worldcraft.chunk.Chunk$1  reason: invalid class name */

    class AnonymousClass1 implements Runnable {
        final /* synthetic */ World val$world;

        AnonymousClass1(World world) {
            this.val$world = world;
        }

        @Override
        public void run() {
            this.val$world.executeOnInited(new Runnable() {
                @Override
                public void run() {
                    MobPainter.executeOnInited(new Runnable() {
                        @Override
                        public void run() {
                            Chunk.this.spawnPassiveMobs(true);
                            if (AnonymousClass1.this.val$world.isNightNow()) {
                                Chunk.this.spawnHostileMobs(true);
                            }
                        }
                    });
                }
            });
        }
    }
}
