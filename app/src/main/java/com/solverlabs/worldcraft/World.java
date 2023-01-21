package com.solverlabs.worldcraft;

import android.util.Log;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.Game;
import com.solverlabs.droid.rugl.geom.BedBlock;
import com.solverlabs.droid.rugl.geom.BreakingShape;
import com.solverlabs.droid.rugl.geom.ColouredShape;
import com.solverlabs.droid.rugl.geom.DoorBlock;
import com.solverlabs.droid.rugl.geom.LadderBlock;
import com.solverlabs.droid.rugl.geom.ShapeUtil;
import com.solverlabs.droid.rugl.gl.GLUtil;
import com.solverlabs.droid.rugl.gl.StackedRenderer;
import com.solverlabs.droid.rugl.res.ResourceLoader;
import com.solverlabs.droid.rugl.util.Colour;
import com.solverlabs.droid.rugl.util.FPSCamera;
import com.solverlabs.droid.rugl.util.FloatMath;
import com.solverlabs.droid.rugl.util.WorldUtils;
import com.solverlabs.droid.rugl.util.geom.Frustum;
import com.solverlabs.droid.rugl.util.geom.ReadableVector3f;
import com.solverlabs.droid.rugl.util.geom.Vector3f;
import com.solverlabs.droid.rugl.util.geom.Vector3i;
import com.solverlabs.droid.rugl.util.math.Range;
import com.solverlabs.droid.rugl.worldgenerator.BaseTerrainGenerator;
import com.solverlabs.droid.rugl.worldgenerator.LightProcessor;
import com.solverlabs.droid.rugl.worldgenerator.StandardGenerationMethod;
import com.solverlabs.droid.rugl.worldgenerator.TreeDecorator;
import com.solverlabs.worldcraft.blockentity.BlockEntityPainter;
import com.solverlabs.worldcraft.blockentity.TNTBlock;
import com.solverlabs.worldcraft.chunk.Chunk;
import com.solverlabs.worldcraft.chunk.ChunkLoader;
import com.solverlabs.worldcraft.chunk.Chunklet;
import com.solverlabs.worldcraft.chunk.GeometryGenerator;
import com.solverlabs.worldcraft.chunk.tile_entity.Chest;
import com.solverlabs.worldcraft.chunk.tile_entity.Furnace;
import com.solverlabs.worldcraft.chunk.tile_entity.TileEntity;
import com.solverlabs.worldcraft.factories.BlockFactory;
import com.solverlabs.worldcraft.math.MathUtils;
import com.solverlabs.worldcraft.mob.MobFactory;
import com.solverlabs.worldcraft.multiplayer.Multiplayer;
import com.solverlabs.worldcraft.multiplayer.util.WorldCopier;
import com.solverlabs.worldcraft.nbt.Tag;
import com.solverlabs.worldcraft.util.GameTime;
import com.solverlabs.worldcraft.util.WorldGenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

public class World {
    public static final String LEVEL_DAT_FILE_NAME = "level.dat";
    public static final int LOADED_CHUNK_COUNT_FOR_WORLD_READY = 24;
    public static final int MAP_SIZE = 3;
    public static final String REGION_DIR_NAME = "region";
    private static final int DAY_PHASE = 0;
    private static final long DAY_TIME_PERIOD = 600000;
    private static final long ECLIPSE_TIME_PERIOD = 90000;
    private static final long FULL_DAY_TIME_PERIOD = 1200000;
    private static final int LOADING_LIMIT_ON_MAP_CREATE = 148;
    private static final int NIGHT_PHASE = 2;
    private static final long NIGHT_TIME_PERIOD = 420000;
    private static final long PARTICLE_PERIOD = 80;
    private static final int SUN_DOWN_PHASE = 1;
    private static final int SUN_UP_PHASE = 3;
    private static final ChunkSorter cs = new ChunkSorter();
    public static int renderedChunklets = 0;
    public final ReadableVector3f startPosition;
    private final Tag levelTag;
    private final StackedRenderer renderer = new StackedRenderer();
    private final Queue<Chunklet> floodQueue = new ArrayBlockingQueue<>(50);
    private final Vector3i blockPreviewLocation = new Vector3i();
    private final LightProcessor lightProcessor = new LightProcessor(this);
    private final TreeDecorator treeDecorator = new TreeDecorator();
    private final ArrayList<DroppableItem> droppableItems = new ArrayList<>();
    private final ArrayList<BlockParticle> blockParticles = new ArrayList<>();
    private final ArrayList<TileEntity> tileEntitiesList = new ArrayList<>();
    private final Vector3f playerNormPosition = new Vector3f();
    private final Vector3f playerSpawnPosition = new Vector3f();
    private final List<Runnable> onInitedRunnableList = new ArrayList<>();
    private final BaseTerrainGenerator generator = new StandardGenerationMethod();
    public BreakingShape breakingShape;
    public int chunkPosX;
    public int chunkPosZ;
    public File dir;
    public boolean isCancelLoad;
    public Player player;
    public boolean drawOutlines = false;
    public boolean drawEnemies = true;
    public int loadradius = 3;
    public Chunk[][] chunks = (Chunk[][]) Array.newInstance(Chunk.class, 7, 7);
    public boolean isNewGame = false;
    private Furnace activeFurnace;
    private BlockEntityPainter blockEntityPainter;
    private ColouredShape blockPreviewShape;
    private long eclipseTime;
    private boolean isChunksInited;
    private long lastSavedAt;
    private int mapType;
    private Vector3f playerPos;
    private int loadedChunkCount = 0;
    private Chunklet[] renderList = new Chunklet[64];
    private final Game.SurfaceListener surfaceListener = new Game.SurfaceListener() {
        @Override
        public void onSurfaceCreated() {
            loadChunkCache();
        }
    };
    private int renderListSize = 0;
    private int drawFlag = Integer.MIN_VALUE;
    private boolean blockPreview = false;
    private int sunlight = 15;
    private long lastParticleTime = System.currentTimeMillis();

    public World(File dir, @NonNull Vector3f startPosition, Tag levelTag) {
        this.isCancelLoad = false;
        this.isCancelLoad = false;
        this.dir = dir;
        this.startPosition = startPosition;
        this.levelTag = levelTag;
        this.chunkPosX = (int) Math.floor(startPosition.getX() / 16.0f);
        this.chunkPosZ = (int) Math.floor(startPosition.getZ() / 16.0f);
        Game.addSurfaceLIstener(this.surfaceListener);
    }

    public static int getLoadingLimit(boolean isNewGame) {
        if (isNewGame) {
            return LOADING_LIMIT_ON_MAP_CREATE;
        }
        return 24;
    }

    protected void setInited(boolean value) {
        this.isChunksInited = true;
        if (this.isChunksInited) {
            executeInitRelatedTasks();
        }
    }

    private void executeInitRelatedTasks() {
        synchronized (this.onInitedRunnableList) {
            if (this.onInitedRunnableList.size() > 0) {
                Iterator<Runnable> iterator = this.onInitedRunnableList.iterator();
                while (iterator.hasNext()) {
                    Runnable runnable = iterator.next();
                    runnable.run();
                    iterator.remove();
                }
            }
        }
    }

    public void init(Player player, BlockEntityPainter blockEntityPainter) {
        this.player = player;
        this.blockEntityPainter = blockEntityPainter;
        if (this.isNewGame) {
            player.position.set(this.playerNormPosition);
            player.spawnPosition.set(this.playerSpawnPosition);
        }
        this.blockPreviewShape = new ColouredShape(ShapeUtil.cuboid(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f), Colour.packFloat(1.0f, 1.0f, 1.0f, 0.3f), ShapeUtil.state);
        this.breakingShape = new BreakingShape();
        save();
    }

    public void loadChunkCache() {
        try {
            clearCache();
            if (this.isNewGame) {
                switch (this.mapType) {
                    case 0:
                        generateRandomMap();
                        generateDecoration();
                        generateLight();
                        break;
                    case 1:
                        generateFlatMap();
                        generateLight();
                        break;
                }
                Chunk c = getChunk(0, 0);
                float x = this.startPosition.getX();
                float z = this.startPosition.getZ();
                boolean positionNormilize = false;
                if (c != null) {
                    for (int y = 127; y >= 0 && !positionNormilize; y--) {
                        byte blockType = c.blockTypeForPosition(x, y, z);
                        if (blockType != 0 && blockType != 18) {
                            this.playerNormPosition.set(x, y + 3.0f, z);
                            this.playerSpawnPosition.set(this.playerNormPosition);
                            positionNormilize = true;
                        }
                    }
                    return;
                }
                return;
            }
            fillChunks();
        } catch (OutOfMemoryError e) {
            this.isCancelLoad = true;
        }
    }

    private void fillChunks() {
        for (int i = 0; i < this.chunks.length; i++) {
            for (int j = 0; j < this.chunks[i].length; j++) {
                final int caix = i;
                final int caiz = j;
                int x = (this.chunkPosX + i) - getLoadRadius();
                int z = (this.chunkPosZ + j) - getLoadRadius();
                if (getChunk(x, z) == null) {
                    ResourceLoader.load(new ChunkLoader(this, x, z) {
                        @Override
                        public void complete() {
                            if (this.resource != null && Range.inRange(caix, 0.0f, chunks.length - 1) && Range.inRange(caiz, 0.0f, chunks[caix].length - 1)) {
                                if (World.this.chunks[caix][caiz] == null) {
                                    this.resource.geomDirty();
                                    chunks[caix][caiz] = this.resource;
                                    incLoadingProgressStatus(1);
                                    Chunk c = getChunk(this.x - 1, this.z);
                                    if (c != null) {
                                        c.geomDirty();
                                    }
                                    Chunk c2 = getChunk(this.x + 1, this.z);
                                    if (c2 != null) {
                                        c2.geomDirty();
                                    }
                                    Chunk c3 = getChunk(this.x, this.z - 1);
                                    if (c3 != null) {
                                        c3.geomDirty();
                                    }
                                    Chunk c4 = getChunk(this.x, this.z + 1);
                                    if (c4 != null) {
                                        c4.geomDirty();
                                    }
                                }
                                this.resource.onPostGenerated();
                            }
                            loadedChunkCount += 1;
                        }
                    });
                }
            }
        }
    }

    public boolean isReady() {
        return this.loadedChunkCount >= 24;
    }

    public void generateTerrain(Chunk c, boolean isFlat) {
        if (isFlat) {
            this.generator.generateFlatTerrain(c);
            this.lightProcessor.lightSunlitBlocksInChunk(c);
            return;
        }
        this.generator.generateTerrain(c, 2);
        for (int k = 0; k < c.topLayer.size(); k++) {
            Vector3i top = c.topLayer.get(k);
            this.treeDecorator.decorate(top.x, top.y, top.z, c);
        }
        recalculateLightForChunk(c);
    }

    private void generateRandomMap() {
        for (int i = 0; i < this.chunks.length; i++) {
            for (int j = 0; j < this.chunks[i].length; j++) {
                int caix = i;
                int caiz = j;
                int x = (this.chunkPosX + i) - getLoadRadius();
                int z = (this.chunkPosZ + j) - getLoadRadius();
                if (getChunk(x, z) == null) {
                    try {
                        Chunk c = new Chunk(this, x, z);
                        this.generator.generateTerrain(c, 2135413);
                        this.chunks[caix][caiz] = c;
                        this.chunks[caix][caiz].onPostGenerated();
                        incLoadingProgressStatus(1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                this.loadedChunkCount++;
            }
        }
    }

    private void generateFlatMap() {
        for (int i = 0; i < this.chunks.length; i++) {
            for (int j = 0; j < this.chunks[i].length; j++) {
                int caix = i;
                int caiz = j;
                int x = (this.chunkPosX + i) - getLoadRadius();
                int z = (this.chunkPosZ + j) - getLoadRadius();
                if (getChunk(x, z) == null) {
                    try {
                        Chunk c = new Chunk(this, x, z);
                        this.generator.generateFlatTerrain(c);
                        this.chunks[caix][caiz] = c;
                        this.chunks[caix][caiz].onPostGenerated();
                        incLoadingProgressStatus(1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                this.loadedChunkCount++;
            }
        }
    }

    private void generateLight() {
        for (int i = 0; i < this.chunks.length && !this.isCancelLoad; i++) {
            for (int j = 0; j < this.chunks[i].length && !this.isCancelLoad; j++) {
                Chunk c = this.chunks[i][j];
                if (c != null) {
                    this.lightProcessor.lightSunlitBlocksInChunk(c);
                    incLoadingProgressStatus(1);
                }
            }
        }
        for (int x = 0; x < this.chunks.length && !this.isCancelLoad; x++) {
            for (int z = 0; z < this.chunks[x].length && !this.isCancelLoad; z++) {
                Chunk c2 = this.chunks[x][z];
                if (c2 != null) {
                    this.lightProcessor.lightChunk(c2);
                    incLoadingProgressStatus(1);
                    c2.geomDirty();
                }
            }
        }
    }

    public void generateLight(Chunk c) {
        if (c != null) {
            this.lightProcessor.lightChunk(c);
        }
    }

    private void generateDecoration() {
        for (int i = 0; i < this.chunks.length && !this.isCancelLoad; i++) {
            for (int j = 0; j < this.chunks[i].length && !this.isCancelLoad; j++) {
                Chunk c = this.chunks[i][j];
                if (c != null) {
                    for (int k = 0; k < c.topLayer.size(); k++) {
                        Vector3i top = c.topLayer.get(k);
                        this.treeDecorator.decorate(top.x, top.y, top.z, c);
                    }
                }
            }
        }
    }

    public void setBlockPlacePreview(boolean active, int x, int y, int z) {
        this.blockPreview = active;
        this.blockPreviewLocation.set(x, y, z);
    }

    public void advance() {
        if (!this.isChunksInited) {
            setInited(true);
        }
        if (this.isChunksInited) {
            this.playerPos = this.player.position;
            float posX = this.playerPos.x;
            float posZ = this.playerPos.z;
            boolean chunksDirty = false;
            int cx = (int) Math.floor(posX / 16.0f);
            int cz = (int) Math.floor(posZ / 16.0f);
            for (int i = 0; i < this.droppableItems.size(); i++) {
                this.droppableItems.get(i).advance(this);
            }
            for (int i2 = 0; i2 < this.blockParticles.size(); i2++) {
                BlockParticle bp = this.blockParticles.get(i2);
                if (bp != null && !bp.isActive()) {
                    this.blockParticles.remove(bp);
                }
            }
            Clouds.getInstance().advance(this.player);
            advanceFurnace();
            if (checkChunks()) {
                if (Math.abs(cx - this.chunkPosX) > getLoadRadius() || Math.abs(cz - this.chunkPosZ) > getLoadRadius()) {
                    this.chunkPosX = cx;
                    this.chunkPosZ = cz;
                    unloadAllChunks();
                    chunksDirty = true;
                }
                if (cx < this.chunkPosX) {
                    this.chunkPosX--;
                    moveRight();
                    chunksDirty = true;
                } else if (cx > this.chunkPosX) {
                    this.chunkPosX++;
                    moveLeft();
                    chunksDirty = true;
                } else if (cz < this.chunkPosZ) {
                    this.chunkPosZ--;
                    moveDown();
                    chunksDirty = true;
                } else if (cz > this.chunkPosZ) {
                    this.chunkPosZ++;
                    moveUp();
                    chunksDirty = true;
                }
                if (chunksDirty) {
                    Log.i(Game.RUGL_TAG, "Entered chunk " + this.chunkPosX + ", " + this.chunkPosZ);
                    fillChunks();
                }
            }
        }
    }

    private void unloadAllChunks() {
        for (int i = 0; i < this.chunks.length; i++) {
            for (int j = 0; j < this.chunks[i].length; j++) {
                if (this.chunks[i][j] != null) {
                    this.chunks[i][j].unload();
                    this.chunks[i][j].save(this);
                    this.chunks[i][j] = null;
                }
            }
        }
    }

    private void advanceFurnace() {
        for (int i = 0; i < this.tileEntitiesList.size(); i++) {
            if (this.tileEntitiesList.get(i).isFurnace()) {
                Furnace furnace = (Furnace) this.tileEntitiesList.get(i);
                if (furnace.needRecalcLight) {
                    if (furnace.isInProgress()) {
                        setBlockType(furnace.getX(), furnace.getY(), furnace.getZ(), BlockFactory.FURNACE_ACTIVE_ID);
                        Chunklet chunklet = getChunklet(furnace.getX(), furnace.getY(), furnace.getZ());
                        if (chunklet != null) {
                            Chunk c = chunklet.parent;
                            if (blockLight(furnace.getX(), furnace.getY(), furnace.getZ()) < 13.0f) {
                                recalculateBlockLight(c, (int) FloatMath.floor(furnace.getX() - (c.chunkX * 16)), (int) FloatMath.floor(furnace.getY()), (int) FloatMath.floor(furnace.getZ() - (c.chunkZ * 16)));
                            }
                        }
                    } else {
                        setBlockType(furnace.getX(), furnace.getY(), furnace.getZ(), (byte) 61);
                    }
                    furnace.needRecalcLight = false;
                }
                furnace.advance();
            }
        }
    }

    private void moveLeft() {
        Chunk[] swap = this.chunks[0];
        if (swap != null) {
            for (int i = 0; i < swap.length; i++) {
                if (swap[i] != null) {
                    swap[i].save(this);
                    swap[i].unload();
                    swap[i] = null;
                }
            }
            for (int i2 = 0; i2 < this.chunks.length - 1; i2++) {
                this.chunks[i2] = this.chunks[i2 + 1];
            }
            this.chunks[this.chunks.length - 1] = swap;
        }
    }

    private void moveRight() {
        Chunk[] swap = this.chunks[this.chunks.length - 1];
        for (int i = 0; i < swap.length; i++) {
            if (swap[i] != null) {
                swap[i].save(this);
                swap[i].unload();
                swap[i] = null;
            }
        }
        for (int i2 = this.chunks.length - 1; i2 > 0; i2--) {
            this.chunks[i2] = this.chunks[i2 - 1];
        }
        this.chunks[0] = swap;
    }

    private void moveUp() {
        for (int i = 0; i < this.chunks.length; i++) {
            if (this.chunks[i][0] != null) {
                this.chunks[i][0].save(this);
                this.chunks[i][0].unload();
            }
            for (int j = 0; j < this.chunks[i].length - 1; j++) {
                this.chunks[i][j] = this.chunks[i][j + 1];
            }
            this.chunks[i][this.chunks[i].length - 1] = null;
        }
    }

    private void moveDown() {
        for (int i = 0; i < this.chunks.length; i++) {
            if (this.chunks[i][this.chunks[i].length - 1] != null) {
                this.chunks[i][this.chunks[i].length - 1].save(this);
                this.chunks[i][this.chunks[i].length - 1].unload();
            }
            for (int j = this.chunks[i].length - 1; j > 0; j--) {
                this.chunks[i][j] = this.chunks[i][j - 1];
            }
            this.chunks[i][0] = null;
        }
    }

    public void saveAs(String worldName) {
        save();
        File dest = new File(WorldUtils.WORLD_DIR, worldName);
        try {
            WorldCopier.copyDirectory(this.dir, dest);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        if (System.currentTimeMillis() - this.lastSavedAt >= 5000) {
            this.lastSavedAt = System.currentTimeMillis();
            for (int i = 0; i < this.chunks.length && !this.isCancelLoad; i++) {
                for (int j = 0; j < this.chunks[i].length && !this.isCancelLoad; j++) {
                    Chunk chunk = this.chunks[i][j];
                    if (chunk != null) {
                        chunk.save(this);
                    }
                }
            }
            if (this.levelTag != null && !this.isCancelLoad) {
                try {
                    FileOutputStream os = new FileOutputStream(new File(this.dir, LEVEL_DAT_FILE_NAME));
                    Tag playerTag = this.levelTag.findTagByName("Player");
                    Tag pos = playerTag.findTagByName("Pos");
                    Tag[] tl = (Tag[]) pos.getValue();
                    if (this.playerPos != null) {
                        tl[0].setValue(Float.valueOf(this.playerPos.x).doubleValue());
                        tl[1].setValue(Float.valueOf(this.playerPos.y).doubleValue());
                        tl[2].setValue(Float.valueOf(this.playerPos.z).doubleValue());
                    }
                    if (this.player != null && GameMode.isSurvivalMode()) {
                        Tag rot = playerTag.findTagByName("Rotation");
                        Tag[] tr = (Tag[]) rot.getValue();
                        tr[0].setValue(this.player.rotation.x);
                        tr[1].setValue(this.player.rotation.y);
                        this.levelTag.findTagByName("SpawnX").setValue(Float.valueOf(this.player.spawnPosition.x).intValue());
                        this.levelTag.findTagByName("SpawnY").setValue(Float.valueOf(this.player.spawnPosition.y).intValue());
                        this.levelTag.findTagByName("SpawnZ").setValue(Float.valueOf(this.player.spawnPosition.z).intValue());
                        this.player.save(playerTag);
                    }
                    Tag time = this.levelTag.findTagByName(WorldGenerator.LAST_PLAYED);
                    time.setValue(GameTime.getTime());
                    this.levelTag.writeTo(os, true);
                } catch (IOException | ClassCastException e) {
                    e.printStackTrace();
                }
            }
            System.gc();
            Runtime.getRuntime().gc();
            System.runFinalization();
        }
    }

    public void draw(Vector3f eye, FPSCamera cam) {
        Chunklet top;
        Chunklet bottom;
        Chunklet west;
        Chunklet east;
        Chunklet south;
        Chunklet north;
        if (this.isChunksInited) {
            this.isNewGame = false;
            if (GameMode.isMultiplayerMode() && !Multiplayer.instance.isWorldReady && isReady()) {
                Multiplayer.instance.clientGraphicInited();
            }
            if (!this.isCancelLoad) {
                Chunklet c = getChunklet(eye.x, eye.y, eye.z);
                if (c != null) {
                    c.drawFlag = this.drawFlag;
                    this.floodQueue.offer(c);
                    while (!this.floodQueue.isEmpty()) {
                        Chunklet c2 = this.floodQueue.poll();
                        Chunklet[] chunkletArr = this.renderList;
                        int i = this.renderListSize;
                        this.renderListSize = i + 1;
                        chunkletArr[i] = c2;
                        if (this.renderListSize >= this.renderList.length) {
                            Chunklet[] nrl = new Chunklet[this.renderList.length * 2];
                            System.arraycopy(this.renderList, 0, nrl, 0, this.renderList.length);
                            this.renderList = nrl;
                        }
                        if (c2.x <= c.x && !c2.northSheet && (north = getChunklet(c2.x - 16, c2.y, c2.z)) != null && !north.southSheet && north.drawFlag != this.drawFlag && north.intersection(cam.getFrustum()) != Frustum.Result.Miss) {
                            north.drawFlag = this.drawFlag;
                            this.floodQueue.offer(north);
                        }
                        if (c2.x >= c.x && !c2.southSheet && (south = getChunklet(c2.x + 16, c2.y, c2.z)) != null && !south.northSheet && south.drawFlag != this.drawFlag && south.intersection(cam.getFrustum()) != Frustum.Result.Miss) {
                            south.drawFlag = this.drawFlag;
                            this.floodQueue.offer(south);
                        }
                        if (c2.z <= c.z && !c2.eastSheet && (east = getChunklet(c2.x, c2.y, c2.z - 16)) != null && !east.westSheet && east.drawFlag != this.drawFlag && east.intersection(cam.getFrustum()) != Frustum.Result.Miss) {
                            east.drawFlag = this.drawFlag;
                            this.floodQueue.offer(east);
                        }
                        if (c2.z >= c.z && !c2.westSheet && (west = getChunklet(c2.x, c2.y, c2.z + 16)) != null && !west.eastSheet && west.drawFlag != this.drawFlag && west.intersection(cam.getFrustum()) != Frustum.Result.Miss) {
                            west.drawFlag = this.drawFlag;
                            this.floodQueue.offer(west);
                        }
                        if (c2.y <= c.y && !c2.bottomSheet && (bottom = getChunklet(c2.x, c2.y - 16, c2.z)) != null && !bottom.topSheet && bottom.drawFlag != this.drawFlag && bottom.intersection(cam.getFrustum()) != Frustum.Result.Miss) {
                            bottom.drawFlag = this.drawFlag;
                            this.floodQueue.offer(bottom);
                        }
                        if (c2.y >= c.y && !c2.topSheet && (top = getChunklet(c2.x, c2.y + 16, c2.z)) != null && !top.bottomSheet && top.drawFlag != this.drawFlag && top.intersection(cam.getFrustum()) != Frustum.Result.Miss) {
                            top.drawFlag = this.drawFlag;
                            this.floodQueue.offer(top);
                        }
                    }
                }
                renderedChunklets = 0;
                cs.eye.set(eye);
                GLUtil.checkGLError();
                for (int i2 = 0; i2 < this.renderListSize; i2++) {
                    this.renderList[i2].generateGeometry(false);
                }
                for (int i3 = 0; i3 < this.renderListSize; i3++) {
                    this.renderList[i3].drawSolid(this.renderer);
                    if (!this.renderList[i3].isEmpty()) {
                        renderedChunklets++;
                    }
                }
                GLUtil.checkGLError();
                for (int i4 = this.renderListSize - 1; i4 >= 0; i4--) {
                    this.renderList[i4].drawTransparent(this.renderer);
                }
                GLUtil.checkGLError();
                if (this.drawOutlines) {
                    for (int i5 = 0; i5 < this.renderListSize; i5++) {
                        this.renderList[i5].drawOutline(this.renderer);
                    }
                    GLUtil.checkGLError();
                }
                for (int i6 = 0; i6 < this.renderListSize; i6++) {
                    this.renderList[i6].drawSolid(this.renderer);
                    if (!this.renderList[i6].isEmpty()) {
                        renderedChunklets++;
                    }
                }
                Clouds.getInstance().draw(this.renderer);
                if (GameMode.isSurvivalMode() && !GameMode.isMultiplayerMode()) {
                    dayNightCircle();
                }
                if (this.blockPreview) {
                    this.renderer.pushMatrix();
                    this.renderer.translate(this.blockPreviewLocation.x, this.blockPreviewLocation.y, this.blockPreviewLocation.z);
                    Byte previewBlockType = getPreviewBlockType();
                    if (DoorBlock.isDoor(previewBlockType)) {
                        DoorBlock.renderPreview(this, this.renderer);
                    } else if (BedBlock.isBed(previewBlockType)) {
                        BedBlock.renderPreview(this, this.renderer);
                    } else if (LadderBlock.isLadder(previewBlockType)) {
                        LadderBlock.renderPreview(this, this.renderer, this.blockPreviewLocation);
                    } else if (this.breakingShape != null && this.breakingShape.isInBreakingProcess()) {
                        this.breakingShape.render(this.renderer);
                    } else {
                        this.blockPreviewShape.render(this.renderer);
                    }
                    this.renderer.popMatrix();
                }
                this.renderer.render();
                for (int i7 = 0; i7 < this.droppableItems.size(); i7++) {
                    DroppableItem b = this.droppableItems.get(i7);
                    b.draw(this.renderer, cam);
                }
                for (int i8 = 0; i8 < this.blockParticles.size(); i8++) {
                    BlockParticle bp = this.blockParticles.get(i8);
                    bp.draw(this.renderer, cam);
                }
                Arrays.fill(this.renderList, null);
                this.renderListSize = 0;
                for (int i9 = 0; i9 < this.renderList.length; i9++) {
                    this.renderList[i9] = null;
                }
                this.renderList = new Chunklet[32];
                this.drawFlag++;
                this.renderer.render();
            }
        }
    }

    public boolean checkChunks() {
        boolean res = true;
        for (int i = 0; i < this.chunks.length; i++) {
            for (int j = 0; j < this.chunks[i].length; j++) {
                if (this.chunks[i][j] == null) {
                    res = false;
                }
            }
        }
        return res | (this.mapType == -1) | GameMode.isMultiplayerMode();
    }

    public void initSunLight() {
        long time = GameTime.getTime();
        int currentDayPeriod = (int) (time % FULL_DAY_TIME_PERIOD);
        switch (getDayPhase(currentDayPeriod)) {
            case 0:
                this.sunlight = 15;
                Log.d("SUNLIGHT", "INNITED DAY");
                return;
            case 1:
                this.sunlight = 15;
                this.sunlight = (int) (this.sunlight - ((currentDayPeriod - DAY_TIME_PERIOD) / 10000));
                Log.d("SUNLIGHT", "INNITED SUN DOWN" + this.sunlight);
                if (this.sunlight < 6) {
                    this.sunlight = 6;
                    return;
                }
                return;
            case 2:
                this.sunlight = 6;
                Log.d("SUNLIGHT", "INNITED NIGHT");
                return;
            case 3:
                this.sunlight = 15;
                this.sunlight = (int) (this.sunlight - ((FULL_DAY_TIME_PERIOD - currentDayPeriod) / 10000));
                Log.d("SUNLIGHT", "INNITED SUN UP" + this.sunlight);
                if (this.sunlight > 15) {
                    this.sunlight = 15;
                    return;
                }
                return;
            default:
                return;
        }
    }

    private int getCurrentPhase() {
        long time = GameTime.getTime();
        int dayPeriod = (int) (time % FULL_DAY_TIME_PERIOD);
        return getDayPhase(dayPeriod);
    }

    private int getDayPhase(int dayPeriod) {
        if (dayPeriod <= 0 || dayPeriod > DAY_TIME_PERIOD) {
            if (dayPeriod > DAY_TIME_PERIOD && dayPeriod <= 690000) {
                return 1;
            }
            if (dayPeriod <= 690000 || dayPeriod > 1110000) {
                return (((long) dayPeriod) <= 1110000 || ((long) dayPeriod) > FULL_DAY_TIME_PERIOD) ? 0 : 3;
            }
            return 2;
        }
        return 0;
    }

    public void setDayTimePeriod() {
        long time = GameTime.getTime();
        GameTime.incTime(FULL_DAY_TIME_PERIOD - ((int) (time % FULL_DAY_TIME_PERIOD)));
    }

    private void dayNightCircle() {
        boolean geomDirty = false;
        long time = GameTime.getTime();
        switch (getCurrentPhase()) {
            case 0:
                if (this.sunlight != 15) {
                    this.sunlight = 15;
                    geomDirty = true;
                    onDayBegins();
                }
                this.eclipseTime = time;
                break;
            case 1:
                if (time - this.eclipseTime >= 10000 && this.sunlight > 6) {
                    this.eclipseTime = time;
                    this.sunlight--;
                    geomDirty = true;
                    break;
                }
                break;
            case 2:
                if (this.sunlight != 6) {
                    this.sunlight = 6;
                    geomDirty = true;
                    onNightBegins();
                }
                this.eclipseTime = time;
                break;
            case 3:
                if (time - this.eclipseTime >= 10000 && this.sunlight < 15) {
                    this.eclipseTime = time;
                    this.sunlight++;
                    geomDirty = true;
                    break;
                }
                break;
        }
        if (geomDirty) {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < chunks.length; i++) {
                        for (int j = 0; j < chunks[i].length; j++) {
                            Chunk c = chunks[i][j];
                            if (c != null) {
                                Chunklet[] arr$ = c.chunklets;
                                for (Chunklet chunklet : arr$) {
                                    chunklet.changeSunLight();
                                }
                            }
                        }
                    }
                }
            };
            GeometryGenerator.addTask(r);
            MobFactory.updateMobsLight();
            Clouds.getInstance().setLight(this.sunlight);
        }
    }

    private void onDayBegins() {
        spawnPassiveMobs(false);
    }

    private void onNightBegins() {
        spawnHostileMobs(false);
    }

    private void spawnPassiveMobs(boolean force) {
        updateChunkMobs();
        Chunk[][] arr$ = this.chunks;
        for (Chunk[] chunkArray : arr$) {
            for (Chunk chunk : chunkArray) {
                if (chunk != null) {
                    chunk.spawnPassiveMobs(force);
                }
            }
        }
    }

    private void spawnHostileMobs(boolean force) {
        updateChunkMobs();
        Chunk[][] arr$ = this.chunks;
        for (Chunk[] chunkArray : arr$) {
            for (Chunk chunk : chunkArray) {
                if (chunk != null) {
                    chunk.spawnHostileMobs(force);
                }
            }
        }
    }

    private void updateChunkMobs() {
        Chunk[][] arr$ = this.chunks;
        for (Chunk[] chunkArray : arr$) {
            for (Chunk chunk : chunkArray) {
                if (chunk != null) {
                    chunk.updateMobs();
                }
            }
        }
    }

    public boolean isNightNow() {
        return getCurrentPhase() == 2;
    }

    public int getSkyColour() {
        float ratio = this.sunlight / 15.0f;
        if (this.sunlight <= 10) {
            ratio = (float) (ratio * 0.6d);
        }
        if (this.sunlight == 4) {
            ratio = (float) (ratio * 0.1d);
        }
        float r = 0.7f * ratio;
        float g = 0.7f * ratio;
        float b = 0.9f * ratio;
        return Colour.packFloat(r, g, b, 1.0f);
    }

    public Vector3i getBlockPreviewLocation() {
        return this.blockPreviewLocation;
    }

    public Byte getPreviewBlockType() {
        return getBlockTypeAbsolute(this.blockPreviewLocation.x, this.blockPreviewLocation.y, this.blockPreviewLocation.z);
    }

    public Byte getPreviewBlockData() {
        return getBlockDataAbsolute(this.blockPreviewLocation.x, this.blockPreviewLocation.y, this.blockPreviewLocation.z);
    }

    public Byte getDownPreviewBlockType() {
        return getBlockTypeAbsolute(this.blockPreviewLocation.x, this.blockPreviewLocation.y - 1, this.blockPreviewLocation.z);
    }

    public Byte getLeftPreviewBlockType() {
        return getBlockTypeAbsolute(this.blockPreviewLocation.x - 1, this.blockPreviewLocation.y, this.blockPreviewLocation.z);
    }

    public Byte getBlockTypeAbsolute(int x, int y, int z) {
        Chunk chunk = getChunk(x / 16, z / 16);
        if (chunk != null) {
            return Byte.valueOf(chunk.blockType(x % 16, y, z % 16));
        }
        return null;
    }

    public Byte getBlockDataAbsolute(int x, int y, int z) {
        Chunk chunk = getChunk(x / 16, z / 16);
        if (chunk != null) {
            return chunk.blockData(x % 16, y, z % 16);
        }
        return null;
    }

    public byte getBlockTypeAbsolute(Vector3i position) {
        if (position != null) {
            return getBlockTypeAbsolute(position.x, position.y, position.z);
        }
        return (byte) 0;
    }

    public Chunk getChunk(int x, int z) {
        int dx = x - this.chunkPosX;
        int dz = z - this.chunkPosZ;
        int caix = getLoadRadius() + dx;
        int caiz = getLoadRadius() + dz;
        if (caix < 0 || caix >= this.chunks.length || caiz < 0 || caiz >= this.chunks[caix].length) {
            return null;
        }
        return this.chunks[caix][caiz];
    }

    public Chunk getChunkByPos(int chunkX, int chunkZ) {
        Chunk[][] arr$ = this.chunks;
        for (Chunk[] chunkArray : arr$) {
            for (Chunk chunk : chunkArray) {
                if (chunk != null && chunk.chunkX == chunkX && chunk.chunkZ == chunkZ) {
                    return chunk;
                }
            }
        }
        return null;
    }

    public Chunklet getChunklet(float x, float y, float z) {
        Chunk chunk = getChunk((int) FloatMath.floor(x / 16.0f), (int) FloatMath.floor(z / 16.0f));
        if (chunk == null || y < 0.0f || y >= 128.0f) {
            return null;
        }
        int yi = (int) FloatMath.floor(y / 16.0f);
        return chunk.chunklets[yi];
    }

    public Float blockLight(@NonNull Vector3f pos) {
        return blockLightExt(pos.x, pos.y, pos.z);
    }

    public Float blockLightExt(float x, float y, float z) {
        Chunklet c = getChunklet(x, y, z);
        if (c != null) {
            return c.light(((int) x) - c.x, ((int) y) - c.y, ((int) z) - c.z);
        }
        return null;
    }

    public float blockLight(float x, float y, float z) {
        Chunklet c = getChunklet(x, y, z);
        if (c != null) {
            return c.light(((int) x) - c.x, ((int) y) - c.y, ((int) z) - c.z);
        }
        return 0.0f;
    }

    public byte blockType(@NonNull Vector3f pos) {
        return blockType(pos.x, pos.y, pos.z);
    }

    public byte blockType(float x, float y, float z) {
        Chunklet c = getChunklet(x, y, z);
        if (c != null) {
            return c.parent.blockTypeForPosition(x, y, z);
        }
        return (byte) 0;
    }

    public byte blockData(float x, float y, float z) {
        Chunklet c = getChunklet(x, y, z);
        if (c != null) {
            return c.parent.blockDataForPosition(x, y, z);
        }
        return (byte) 0;
    }

    public void setBlockType(float x, float y, float z, byte blockType) {
        Chunklet c = getChunklet(x, y, z);
        if (c != null) {
            c.parent.setBlockTypeForPosition(x, y, z, blockType, (byte) 0);
        }
    }

    public Set<Chunklet> setBlockTypeWithoutGeometryRecalculate(float x, float y, float z, byte blockType, byte blockData) {
        Set<Chunklet> result = new HashSet<>();
        Chunklet c = getChunklet(x, y, z);
        result.add(c);
        if (c != null) {
            result.addAll(c.parent.setBlockTypeForPositionWithoutGeometryRecalculate(x, y, z, blockType, blockData));
        }
        return result;
    }

    public int getLoadRadius() {
        return this.loadradius;
    }

    public void clearCache() {
        for (int i = 0; i < this.chunks.length; i++) {
            for (int j = 0; j < this.chunks[i].length; j++) {
                Chunk chunk = this.chunks[i][j];
                if (chunk != null) {
                    chunk.unload();
                    this.chunks[i][j] = null;
                }
            }
        }
        for (int i2 = 0; i2 < this.renderList.length; i2++) {
            Chunklet c = this.renderList[i2];
            if (c != null) {
                c.unload();
            }
        }
        System.runFinalization();
        Runtime.getRuntime().gc();
        System.gc();
    }

    public void setNewGame(boolean isNewGame) {
        this.isNewGame = isNewGame;
    }

    public void recalculateSkyLight(Chunk chunk, int bx, int by, int bz) {
        this.lightProcessor.recalculateSkyLightingAround(chunk, bx, by, bz);
    }

    public void recalculateBlockLight(final Chunk chunk, final int bx, final int by, final int bz) {
        Runnable r = () -> {
            lightProcessor.recalculateBlockLightAround(chunk, bx, by, bz);
            List<Chunklet> chunkletsLightRecalc = new ArrayList<>(World.this.chunkletsForRecalculateLight());
            for (Chunklet chunklet : chunkletsLightRecalc) {
                chunklet.geomDirty = true;
                chunklet.generateGeometry(false);
            }
            chunkletsForRecalculateLight().clear();
        };
        GeometryGenerator.addTask(r);
    }

    public ArrayList<Chunklet> chunkletsForRecalculateLight() {
        return this.lightProcessor.getDirtyChunklet();
    }

    public void recalculateChunklets(Set<Chunklet> modifiedChunks) {
        recalculateChunksLight(getChunks(modifiedChunks));
        generateChunkletsGeometry(modifiedChunks);
    }

    private void recalculateChunksLight(@NonNull Collection<Chunk> chunks) {
        for (Chunk chunk : chunks) {
            recalculateLightForChunk(chunk);
        }
    }

    private void generateChunkletsGeometry(@NonNull Collection<Chunklet> chunklets) {
        for (Chunklet chunklet : chunklets) {
            if (chunklet != null) {
                chunklet.geomDirty();
                chunklet.generateGeometry(true);
            }
        }
    }

    @NonNull
    private Set<Chunk> getChunks(@NonNull Collection<Chunklet> chunklets) {
        Set<Chunk> chunkSet = new HashSet<>();
        for (Chunklet chunklet : chunklets) {
            if (chunklet != null) {
                chunkSet.add(chunklet.parent);
            }
        }
        return chunkSet;
    }

    public void recalculateLightForChunk(Chunk c) {
        this.lightProcessor.lightSunlitBlocksInChunk(c);
        this.lightProcessor.lightChunk(c);
    }

    public boolean isCancelLoad() {
        return this.isCancelLoad;
    }

    public void setCancel(boolean isCancel) {
        this.isCancelLoad = isCancel;
    }

    public int getMapSize() {
        return 3;
    }

    public boolean isLoadingDialogVisible() {
        return false;
    }

    public void incLoadingProgressStatus(int diff) {
    }

    public void setLoadingProgressStatus(int progress, int max) {
    }

    public void dismissLoadingDialog() {
    }

    public void dismissLoadingDialogAndWait() {
    }

    public void showGameMenu() {
    }

    public void showChat() {
    }

    public void showReportAbuse() {
    }

    public void showDeathMenu(Player player) {
    }

    public void quit() {
    }

    public void restart() {
    }

    public void destroy() {
        Game.removeSurfaceListener(this.surfaceListener);
    }

    public int getSunlight() {
        return this.sunlight;
    }

    public boolean isChunksInited() {
        return this.isChunksInited;
    }

    public int getMapType() {
        return this.mapType;
    }

    public void setMapType(int mapType) {
        this.mapType = mapType;
    }

    public Furnace getFurnace(@NonNull Vector3i vec) {
        return getFurnace(vec.x, vec.y, vec.z);
    }

    public Furnace getFurnace(int x, int y, int z) {
        for (int i = 0; i < this.tileEntitiesList.size(); i++) {
            if (this.tileEntitiesList.get(i) != null && this.tileEntitiesList.get(i).isFurnace()) {
                Furnace furnace = (Furnace) this.tileEntitiesList.get(i);
                if (furnace.isSelectedEntity(x, y, z)) {
                    return furnace;
                }
            }
        }
        Furnace furnace2 = new Furnace(x, y, z);
        this.tileEntitiesList.add(furnace2);
        return furnace2;
    }

    public Chest getChest(@NonNull Vector3i vec) {
        return getChest(vec.x, vec.y, vec.z);
    }

    public Chest getChest(int x, int y, int z) {
        for (int i = 0; i < this.tileEntitiesList.size(); i++) {
            if (this.tileEntitiesList.get(i) != null && this.tileEntitiesList.get(i).isChest()) {
                Chest chest = (Chest) this.tileEntitiesList.get(i);
                if (chest.isSelectedEntity(x, y, z)) {
                    return chest;
                }
            }
        }
        Chest chest2 = new Chest(x, y, z);
        this.tileEntitiesList.add(chest2);
        return chest2;
    }

    public void addTileEntity(TileEntity tileEntity) {
        this.tileEntitiesList.add(tileEntity);
    }

    public Furnace getActiveFurnace() {
        return this.activeFurnace;
    }

    public void setActiveFurnace(Furnace activeFurnace) {
        this.activeFurnace = activeFurnace;
    }

    public Tag getLevelTag() {
        return this.levelTag;
    }

    public void addDroppableItem(byte itemId, @NonNull Vector3i pos) {
        addDroppableItem(itemId, pos.x, pos.y, pos.z);
    }

    public void addDroppableItem(byte itemId, @NonNull Vector3i pos, int count) {
        addDroppableItem(itemId, pos.x, pos.y, pos.z, count);
    }

    public void addDroppableItem(byte itemId, float x, float y, float z) {
        addDroppableItem(itemId, x, y, z, 1);
    }

    public void addDroppableItem(byte itemId, float x, float y, float z, int count) {
        addDroppableItem(itemId, x, y, z, count, false);
    }

    public void addDroppableItem(byte itemId, float x, float y, float z, int count, boolean dropFromHotbar) {
        if (!GameMode.isCreativeMode()) {
            if (dropFromHotbar) {
                Vector3f vec = MathUtils.getVelocityVector(this.player.getAngle(), 0.75f);
                this.droppableItems.add(new DroppableItem(itemId, x + (vec.x * 1.5f), y, z + (vec.z * 1.5f), count, true));
            } else if (itemId == 82) {
                for (int i = 0; i < 4; i++) {
                    this.droppableItems.add(new DroppableItem(itemId, x, y, z, count, false));
                }
            } else if (itemId != 121) {
                this.droppableItems.add(new DroppableItem(itemId, x, y, z, count, false));
            }
        }
    }

    public void removeDroppableBlock(DroppableItem droppableItem) {
        this.droppableItems.remove(droppableItem);
    }

    public void addBlockParticle(byte itemID, Vector3f location, BlockFactory.WorldSide blockSide) {
        addBlockParticle(itemID, location, blockSide, false);
    }

    public void addBlockParticle(byte itemID, Vector3f location, BlockFactory.WorldSide blockSide, boolean isExplosion) {
        if (isExplosion) {
            for (int i = 0; i < 50; i++) {
                this.blockParticles.add(new BlockParticle(itemID, location.x, location.y, location.z, blockSide, isExplosion));
            }
        } else if (System.currentTimeMillis() - this.lastParticleTime > 80) {
            this.blockParticles.add(new BlockParticle(itemID, location.x, location.y, location.z, blockSide));
            this.lastParticleTime = System.currentTimeMillis();
        }
    }

    public void removeTileEntity(TileEntity tileEntity) {
        this.tileEntitiesList.remove(tileEntity);
    }

    public List<? extends TileEntity> getTileEntities(Chunk chunk) {
        List<TileEntity> result = new ArrayList<>();
        for (int i = 0; i < this.tileEntitiesList.size(); i++) {
            TileEntity item = this.tileEntitiesList.get(i);
            if (chunk.contains(item.getX(), item.getZ())) {
                result.add(item);
            }
        }
        this.tileEntitiesList.removeAll(result);
        return result;
    }

    public void updateMobChunks() {
        Chunk[][] arr$ = this.chunks;
        for (Chunk[] chunkArray : arr$) {
            for (Chunk chunk : chunkArray) {
                if (chunk != null) {
                    chunk.updateMobs();
                }
            }
        }
    }

    public void executeOnInited(Runnable runnable) {
        if (runnable != null) {
            if (this.isChunksInited) {
                runnable.run();
                return;
            }
            synchronized (this.onInitedRunnableList) {
                this.onInitedRunnableList.add(runnable);
            }
        }
    }

    public void activateTNT(Vector3i targetBlockLocation) {
        activateTNT(targetBlockLocation, TNTBlock.DetonationDelayType.NORMAL_DELAY);
    }

    public void activateTNT(Vector3i targetBlockLocation, TNTBlock.DetonationDelayType detonationType) {
        activateTNT(targetBlockLocation, detonationType, true);
    }

    public void activateTNT(Vector3i targetBlockLocation, TNTBlock.DetonationDelayType detonationType, boolean removeBlock) {
        TNTBlock tnt = new TNTBlock(targetBlockLocation, this);
        this.blockEntityPainter.add(tnt);
        tnt.activate(detonationType, removeBlock);
    }

    public void activateTNT(Vector3f targetBlockLocation, TNTBlock.DetonationDelayType detonationType, boolean removeBlock) {
        activateTNT(new Vector3i(targetBlockLocation), detonationType, removeBlock);
    }

    public static class ChunkSorter implements Comparator<Chunklet> {
        private final Vector3f eye;

        private ChunkSorter() {
            this.eye = new Vector3f();
        }

        @Override
        public int compare(@NonNull Chunklet a, @NonNull Chunklet b) {
            float ad = a.distanceSq(this.eye.x, this.eye.y, this.eye.z);
            float bd = b.distanceSq(this.eye.x, this.eye.y, this.eye.z);
            return (int) Math.signum(ad - bd);
        }
    }
}
