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

/**
 * Manages loading chunks, decides which chunklets to render
 */
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
    /**
     * Number of chunklets rendered in the last draw call
     */
    public static int renderedChunklets = 0;
    /**
     * The player position, as read from level.dat
     */
    public final ReadableVector3f startPosition;
    private final Tag levelTag;
    /**
     * For drawing the wireframes
     */
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
    /**
     * Coordinates of the currently-occupied chunk
     */
    public int chunkPosZ;
    /**
     * The world save directory
     */
    public File dir;
    public boolean isCancelLoad;
    public Player player;
    public boolean drawOutlines = false;
    public boolean drawEnemies = true;
    public int loadradius = 3;
    /**
     * 1st index = x, 2nd = z
     */
    private final Chunk[][] chunks = new Chunk[2 * getLoadRadius() + 1][2 * getLoadRadius() + 1];
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

    /**
     * @param dir
     * @param startPosition
     * @param levelTag
     */
    public World(File dir, @NonNull Vector3f startPosition, Tag levelTag) {
        isCancelLoad = false;
        this.dir = dir;
        this.startPosition = startPosition;
        this.levelTag = levelTag;
        chunkPosX = (int) Math.floor(startPosition.getX() / 16.0f);
        chunkPosZ = (int) Math.floor(startPosition.getZ() / 16.0f);
        Game.addSurfaceLIstener(surfaceListener);
    }

    public static int getLoadingLimit(boolean isNewGame) {
        if (isNewGame) {
            return LOADING_LIMIT_ON_MAP_CREATE;
        }
        return 24;
    }

    protected void setInited(boolean value) {
        isChunksInited = true;
        if (isChunksInited) {
            executeInitRelatedTasks();
        }
    }

    private void executeInitRelatedTasks() {
        synchronized (onInitedRunnableList) {
            if (onInitedRunnableList.size() > 0) {
                Iterator<Runnable> iterator = onInitedRunnableList.iterator();
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
        if (isNewGame) {
            player.position.set(playerNormPosition);
            player.spawnPosition.set(playerSpawnPosition);
        }
        blockPreviewShape = new ColouredShape(ShapeUtil.cuboid(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f), Colour.packFloat(1.0f, 1.0f, 1.0f, 0.3f), ShapeUtil.state);
        breakingShape = new BreakingShape();
        save();
    }

    public void loadChunkCache() {
        try {
            clearCache();
            if (isNewGame) {
                switch (mapType) {
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
                float x = startPosition.getX();
                float z = startPosition.getZ();
                boolean positionNormalize = false;
                if (c != null) {
                    for (int y = 127; y >= 0 && !positionNormalize; y--) {
                        byte blockType = c.blockTypeForPosition(x, y, z);
                        if (blockType != 0 && blockType != 18) {
                            playerNormPosition.set(x, y + 3.0f, z);
                            playerSpawnPosition.set(playerNormPosition);
                            positionNormalize = true;
                        }
                    }
                    return;
                }
                return;
            }
            fillChunks();
        } catch (OutOfMemoryError e) {
            isCancelLoad = true;
        }
    }

    private void fillChunks() {
        for (int i = 0; i < chunks.length; i++) {
            for (int j = 0; j < chunks[i].length; j++) {
                final int caix = i;
                final int caiz = j;
                int x = (chunkPosX + i) - getLoadRadius();
                int z = (chunkPosZ + j) - getLoadRadius();
                if (getChunk(x, z) == null) {
                    ResourceLoader.load(new ChunkLoader(this, x, z) {
                        @Override
                        public void complete() {
                            if (resource != null && Range.inRange(caix, 0.0f, chunks.length - 1) && Range.inRange(caiz, 0.0f, chunks[caix].length - 1)) {
                                if (chunks[caix][caiz] == null) {
                                    resource.geomDirty();
                                    chunks[caix][caiz] = resource;
                                    incLoadingProgressStatus(1);

                                    // need to re-evaluate the geometry of
                                    // neighbouring chunks
                                    Chunk c = getChunk(x - 1, z);
                                    if (c != null) {
                                        c.geomDirty();
                                    }
                                    c = getChunk(x + 1, z);
                                    if (c != null) {
                                        c.geomDirty();
                                    }
                                    c = getChunk(x, z - 1);
                                    if (c != null) {
                                        c.geomDirty();
                                    }
                                    c = getChunk(x, z + 1);
                                    if (c != null) {
                                        c.geomDirty();
                                    }
                                }
                                resource.onPostGenerated();
                            }
                            loadedChunkCount += 1;
                        }
                    });
                }
            }
        }
    }

    public boolean isReady() {
        return loadedChunkCount >= 24;
    }

    public void generateTerrain(Chunk c, boolean isFlat) {
        if (isFlat) {
            generator.generateFlatTerrain(c);
            lightProcessor.lightSunlitBlocksInChunk(c);
            return;
        }
        generator.generateTerrain(c, 2);
        for (int k = 0; k < c.topLayer.size(); k++) {
            Vector3i top = c.topLayer.get(k);
            treeDecorator.decorate(top.x, top.y, top.z, c);
        }
        recalculateLightForChunk(c);
    }

    /**
     * Генерация рандомной карты
     */
    private void generateRandomMap() {
        for (int i = 0; i < chunks.length; i++) {
            for (int j = 0; j < chunks[i].length; j++) {
                int caix = i;
                int caiz = j;
                int x = (chunkPosX + i) - getLoadRadius();
                int z = (chunkPosZ + j) - getLoadRadius();
                if (getChunk(x, z) == null) {
                    try {
                        Chunk c = new Chunk(this, x, z);
                        generator.generateTerrain(c, 2135413);
                        chunks[caix][caiz] = c;
                        chunks[caix][caiz].onPostGenerated();
                        incLoadingProgressStatus(1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                loadedChunkCount++;
            }
        }
    }

    /**
     * Генерация плоской карты
     */
    private void generateFlatMap() {
        for (int i = 0; i < chunks.length; i++) {
            for (int j = 0; j < chunks[i].length; j++) {
                int caix = i;
                int caiz = j;
                int x = (chunkPosX + i) - getLoadRadius();
                int z = (chunkPosZ + j) - getLoadRadius();
                if (getChunk(x, z) == null) {
                    try {
                        Chunk c = new Chunk(this, x, z);
                        generator.generateFlatTerrain(c);
                        chunks[caix][caiz] = c;
                        chunks[caix][caiz].onPostGenerated();
                        incLoadingProgressStatus(1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                loadedChunkCount++;
            }
        }
    }

    /**
     * Генерация освещения
     */
    private void generateLight() {
        for (int i = 0; i < chunks.length && !isCancelLoad; i++) {
            for (int j = 0; j < chunks[i].length && !isCancelLoad; j++) {
                Chunk c = chunks[i][j];
                if (c != null) {
                    lightProcessor.lightSunlitBlocksInChunk(c);
                    incLoadingProgressStatus(1);
                }
            }
        }
        for (int x = 0; x < chunks.length && !isCancelLoad; x++) {
            for (int z = 0; z < chunks[x].length && !isCancelLoad; z++) {
                Chunk c = chunks[x][z];
                if (c != null) {
                    lightProcessor.lightChunk(c);
                    incLoadingProgressStatus(1);
                    c.geomDirty();
                }
            }
        }
    }

    public void generateLight(Chunk c) {
        if (c != null) {
            lightProcessor.lightChunk(c);
        }
    }

    /**
     * Генерация деревьев
     */
    private void generateDecoration() {
        for (int i = 0; i < chunks.length && !isCancelLoad; i++) {
            for (int j = 0; j < chunks[i].length && !isCancelLoad; j++) {
                Chunk c = chunks[i][j];
                if (c != null) {
                    for (int k = 0; k < c.topLayer.size(); k++) {
                        Vector3i top = c.topLayer.get(k);
                        treeDecorator.decorate(top.x, top.y, top.z, c);
                    }
                }
            }
        }
    }

    /**
     * @param active
     * @param x
     * @param y
     * @param z
     */
    public void setBlockPlacePreview(boolean active, int x, int y, int z) {
        blockPreview = active;
        blockPreviewLocation.set(x, y, z);
    }

    /**
     * Tries to ensure we have the right chunks loaded
     */
    public void advance() {
        if (!isChunksInited) {
            setInited(true);
        }
        if (isChunksInited) {
            playerPos = player.position;
            float posX = playerPos.x;
            float posZ = playerPos.z;
            boolean chunksDirty = false;
            int cx = (int) Math.floor(posX / 16.0f);
            int cz = (int) Math.floor(posZ / 16.0f);
            for (int i = 0; i < droppableItems.size(); i++) {
                droppableItems.get(i).advance(this);
            }
            for (int i = 0; i < blockParticles.size(); i++) {
                BlockParticle bp = blockParticles.get(i);
                if (bp != null && !bp.isActive()) {
                    blockParticles.remove(bp);
                }
            }
            Clouds.getInstance().advance(player);
            advanceFurnace();
            if (checkChunks()) {
                if (Math.abs(cx - chunkPosX) > getLoadRadius() || Math.abs(cz - chunkPosZ) > getLoadRadius()) {
                    chunkPosX = cx;
                    chunkPosZ = cz;
                    unloadAllChunks();
                    chunksDirty = true;
                }
                if (cx < chunkPosX) {
                    chunkPosX--;
                    moveRight();
                    chunksDirty = true;
                } else if (cx > chunkPosX) {
                    chunkPosX++;
                    moveLeft();
                    chunksDirty = true;
                } else if (cz < chunkPosZ) {
                    chunkPosZ--;
                    moveDown();
                    chunksDirty = true;
                } else if (cz > chunkPosZ) {
                    chunkPosZ++;
                    moveUp();
                    chunksDirty = true;
                }
                // load new chunks
                if (chunksDirty) {
                    Log.i(Game.RUGL_TAG, "Entered chunk " + chunkPosX + ", " + chunkPosZ);
                    fillChunks();
                }
            }
        }
    }

    private void unloadAllChunks() {
        for (int i = 0; i < chunks.length; i++) {
            for (int j = 0; j < chunks[i].length; j++) {
                if (chunks[i][j] != null) {
                    chunks[i][j].unload();
                    chunks[i][j].save(this);
                    chunks[i][j] = null;
                }
            }
        }
    }

    private void advanceFurnace() {
        for (int i = 0; i < tileEntitiesList.size(); i++) {
            if (tileEntitiesList.get(i).isFurnace()) {
                Furnace furnace = (Furnace) tileEntitiesList.get(i);
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
        Chunk[] swap = chunks[0];
        if (swap != null) {
            for (int i = 0; i < swap.length; i++) {
                if (swap[i] != null) {
                    swap[i].save(this);
                    swap[i].unload();
                    swap[i] = null;
                }
            }
            for (int i2 = 0; i2 < chunks.length - 1; i2++) {
                chunks[i2] = chunks[i2 + 1];
            }
            chunks[chunks.length - 1] = swap;
        }
    }

    private void moveRight() {
        Chunk[] swap = chunks[chunks.length - 1];
        for (int i = 0; i < swap.length; i++) {
            if (swap[i] != null) {
                swap[i].save(this);
                swap[i].unload();
                swap[i] = null;
            }
        }
        for (int i2 = chunks.length - 1; i2 > 0; i2--) {
            chunks[i2] = chunks[i2 - 1];
        }
        chunks[0] = swap;
    }

    private void moveUp() {
        for (int i = 0; i < chunks.length; i++) {
            if (chunks[i][0] != null) {
                chunks[i][0].save(this);
                chunks[i][0].unload();
            }
            for (int j = 0; j < chunks[i].length - 1; j++) {
                chunks[i][j] = chunks[i][j + 1];
            }
            chunks[i][chunks[i].length - 1] = null;
        }
    }

    private void moveDown() {
        for (int i = 0; i < chunks.length; i++) {
            if (chunks[i][chunks[i].length - 1] != null) {
                chunks[i][chunks[i].length - 1].save(this);
                chunks[i][chunks[i].length - 1].unload();
            }
            for (int j = chunks[i].length - 1; j > 0; j--) {
                chunks[i][j] = chunks[i][j - 1];
            }
            chunks[i][0] = null;
        }
    }

    public void saveAs(String worldName) {
        save();
        File dest = new File(WorldUtils.WORLD_DIR, worldName);
        try {
            WorldCopier.copyDirectory(dir, dest);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        if (System.currentTimeMillis() - lastSavedAt >= 5000) {
            lastSavedAt = System.currentTimeMillis();
            for (int i = 0; i < chunks.length && !isCancelLoad; i++) {
                for (int j = 0; j < chunks[i].length && !isCancelLoad; j++) {
                    Chunk chunk = chunks[i][j];
                    if (chunk != null) {
                        chunk.save(this);
                    }
                }
            }
            if (levelTag != null && !isCancelLoad) {
                try {
                    FileOutputStream os = new FileOutputStream(new File(dir, LEVEL_DAT_FILE_NAME));
                    Tag playerTag = levelTag.findTagByName("Player");
                    Tag pos = playerTag.findTagByName("Pos");
                    Tag[] tl = (Tag[]) pos.getValue();
                    if (playerPos != null) {
                        tl[0].setValue(Float.valueOf(playerPos.x).doubleValue());
                        tl[1].setValue(Float.valueOf(playerPos.y).doubleValue());
                        tl[2].setValue(Float.valueOf(playerPos.z).doubleValue());
                    }
                    if (player != null && GameMode.isSurvivalMode()) {
                        Tag rot = playerTag.findTagByName("Rotation");
                        Tag[] tr = (Tag[]) rot.getValue();
                        tr[0].setValue(player.rotation.x);
                        tr[1].setValue(player.rotation.y);
                        levelTag.findTagByName("SpawnX").setValue(Float.valueOf(player.spawnPosition.x).intValue());
                        levelTag.findTagByName("SpawnY").setValue(Float.valueOf(player.spawnPosition.y).intValue());
                        levelTag.findTagByName("SpawnZ").setValue(Float.valueOf(player.spawnPosition.z).intValue());
                        player.save(playerTag);
                    }
                    Tag time = levelTag.findTagByName(WorldGenerator.LAST_PLAYED);
                    time.setValue(GameTime.getTime());
                    levelTag.writeTo(os, true);
                } catch (IOException | ClassCastException e) {
                    e.printStackTrace();
                }
            }
            System.gc();
            Runtime.getRuntime().gc();
            System.runFinalization();
        }
    }

    /**
     * @param eye
     * @param cam
     */
    public void draw(Vector3f eye, FPSCamera cam) {
        if (isChunksInited) {
            isNewGame = false;
            if (GameMode.isMultiplayerMode() && !Multiplayer.instance.isWorldReady && isReady()) {
                Multiplayer.instance.clientGraphicInited();
            }
            if (!isCancelLoad) {
                Chunklet c = getChunklet(eye.x, eye.y, eye.z);
                if (c != null) {
                    final Chunklet origin = c;
                    c.drawFlag = drawFlag;
                    floodQueue.offer(c);
                    while (!floodQueue.isEmpty()) {
                        c = floodQueue.poll();

                        renderList[renderListSize++] = c;

                        // grow
                        if (renderListSize >= renderList.length) {
                            Chunklet[] nrl = new Chunklet[renderList.length * 2];
                            System.arraycopy(renderList, 0, nrl, 0, renderList.length);
                            renderList = nrl;
                        }

                        // floodfill - for each neighbouring chunklet...
                        // we are not reversing flood direction and we can see
                        // through that face of this chunk
                        if (c.x <= origin.x && !c.northSheet) {
                            Chunklet north = getChunklet(c.x - 16, c.y, c.z);
                            if (north != null) {
                                // neighbouring chunk exists
                                if (!north.southSheet)
                                // we can see through the traversal face
                                {
                                    if (north.drawFlag != drawFlag) {
                                        // we haven't already visited it in this frame
                                        // it intersects the frustum
                                        if (north.intersection(cam.getFrustum()) != Frustum.Result.Miss) {
                                            north.drawFlag = drawFlag;
                                            floodQueue.offer(north);
                                        }
                                    }
                                }
                            }
                        }
                        if (c.x >= origin.x && !c.southSheet) {
                            Chunklet south = getChunklet(c.x + 16, c.y, c.z);
                            if (south != null && !south.northSheet && south.drawFlag != drawFlag && south.intersection(cam.getFrustum()) != Frustum.Result.Miss) {
                                south.drawFlag = drawFlag;
                                floodQueue.offer(south);
                            }
                        }
                        if (c.z <= origin.z && !c.eastSheet) {
                            Chunklet east = getChunklet(c.x, c.y, c.z - 16);
                            if (east != null && !east.westSheet && east.drawFlag != drawFlag && east.intersection(cam.getFrustum()) != Frustum.Result.Miss) {
                                east.drawFlag = drawFlag;
                                floodQueue.offer(east);
                            }
                        }
                        if (c.z >= origin.z && !c.westSheet) {
                            Chunklet west = getChunklet(c.x, c.y, c.z + 16);
                            if (west != null && !west.eastSheet && west.drawFlag != drawFlag && west.intersection(cam.getFrustum()) != Frustum.Result.Miss) {
                                west.drawFlag = drawFlag;
                                floodQueue.offer(west);
                            }
                        }
                        if (c.y <= origin.y && !c.bottomSheet) {
                            Chunklet bottom = getChunklet(c.x, c.y - 16, c.z);
                            if (bottom != null && !bottom.topSheet && bottom.drawFlag != drawFlag && bottom.intersection(cam.getFrustum()) != Frustum.Result.Miss) {
                                bottom.drawFlag = drawFlag;
                                floodQueue.offer(bottom);
                            }
                        }
                        if (c.y >= origin.y && !c.topSheet) {
                            Chunklet top = getChunklet(c.x, c.y + 16, c.z);
                            if (top != null && !top.bottomSheet && top.drawFlag != drawFlag && top.intersection(cam.getFrustum()) != Frustum.Result.Miss) {
                                top.drawFlag = drawFlag;
                                floodQueue.offer(top);
                            }
                        }
                    }
                }
                renderedChunklets = 0;
                // sort chunklets into ascending order of distance from the
                // eye
                cs.eye.set(eye);
                GLUtil.checkGLError();

                // solid stuff from near to far
                for (int i = 0; i < renderListSize; i++) {
                    renderList[i].generateGeometry(false);
                }
                for (int i = 0; i < renderListSize; i++) {
                    renderList[i].drawSolid(renderer);
                    if (!renderList[i].isEmpty()) {
                        renderedChunklets++;
                    }
                }
                GLUtil.checkGLError();

                // translucent stuff from far to near
                for (int i = renderListSize - 1; i >= 0; i--) {
                    renderList[i].drawTransparent(renderer);
                }
                GLUtil.checkGLError();
                if (drawOutlines) {
                    for (int i = 0; i < renderListSize; i++) {
                        renderList[i].drawOutline(renderer);
                    }
                    GLUtil.checkGLError();
                }
                for (int i = 0; i < renderListSize; i++) {
                    renderList[i].drawSolid(renderer);
                    if (!renderList[i].isEmpty()) {
                        renderedChunklets++;
                    }
                }
                Clouds.getInstance().draw(renderer);
                if (GameMode.isSurvivalMode() && !GameMode.isMultiplayerMode()) {
                    dayNightCircle();
                }
                if (blockPreview) {
                    renderer.pushMatrix();
                    renderer.translate(blockPreviewLocation.x, blockPreviewLocation.y, blockPreviewLocation.z);
                    Byte previewBlockType = getPreviewBlockType();
                    if (DoorBlock.isDoor(previewBlockType)) {
                        DoorBlock.renderPreview(this, renderer);
                    } else if (BedBlock.isBed(previewBlockType)) {
                        BedBlock.renderPreview(this, renderer);
                    } else if (LadderBlock.isLadder(previewBlockType)) {
                        LadderBlock.renderPreview(this, renderer, blockPreviewLocation);
                    } else if (breakingShape != null && breakingShape.isInBreakingProcess()) {
                        breakingShape.render(renderer);
                    } else {
                        blockPreviewShape.render(renderer);
                    }
                    renderer.popMatrix();
                }
                renderer.render();
                for (int i = 0; i < droppableItems.size(); i++) {
                    DroppableItem b = droppableItems.get(i);
                    b.draw(renderer, cam);
                }
                for (int i = 0; i < blockParticles.size(); i++) {
                    BlockParticle bp = blockParticles.get(i);
                    bp.draw(renderer, cam);
                }
                Arrays.fill(renderList, null);
                renderListSize = 0;
                Arrays.fill(renderList, null);
                renderList = new Chunklet[32];
                drawFlag++;
                renderer.render();
            }
        }
    }

    public boolean checkChunks() {
        boolean res = true;
        for (int i = 0; i < chunks.length; i++) {
            for (int j = 0; j < chunks[i].length; j++) {
                if (chunks[i][j] == null) {
                    res = false;
                }
            }
        }
        return res | (mapType == -1) | GameMode.isMultiplayerMode();
    }

    public void initSunLight() {
        long time = GameTime.getTime();
        int currentDayPeriod = (int) (time % FULL_DAY_TIME_PERIOD);
        switch (getDayPhase(currentDayPeriod)) {
            case 0:
                sunlight = 15;
                Log.d("SUNLIGHT", "INNITED DAY");
                return;
            case 1:
                sunlight = 15;
                sunlight = (int) (sunlight - ((currentDayPeriod - DAY_TIME_PERIOD) / 10000));
                Log.d("SUNLIGHT", "INNITED SUN DOWN" + sunlight);
                if (sunlight < 6) {
                    sunlight = 6;
                    return;
                }
                return;
            case 2:
                sunlight = 6;
                Log.d("SUNLIGHT", "INNITED NIGHT");
                return;
            case 3:
                sunlight = 15;
                sunlight = (int) (sunlight - ((FULL_DAY_TIME_PERIOD - currentDayPeriod) / 10000));
                Log.d("SUNLIGHT", "INNITED SUN UP" + sunlight);
                if (sunlight > 15) {
                    sunlight = 15;
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
                if (sunlight != 15) {
                    sunlight = 15;
                    geomDirty = true;
                    onDayBegins();
                }
                eclipseTime = time;
                break;
            case 1:
                if (time - eclipseTime >= 10000 && sunlight > 6) {
                    eclipseTime = time;
                    sunlight--;
                    geomDirty = true;
                    break;
                }
                break;
            case 2:
                if (sunlight != 6) {
                    sunlight = 6;
                    geomDirty = true;
                    onNightBegins();
                }
                eclipseTime = time;
                break;
            case 3:
                if (time - eclipseTime >= 10000 && sunlight < 15) {
                    eclipseTime = time;
                    sunlight++;
                    geomDirty = true;
                    break;
                }
                break;
        }
        if (geomDirty) {
            Runnable r = () -> {
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
            };
            GeometryGenerator.addTask(r);
            MobFactory.updateMobsLight();
            Clouds.getInstance().setLight(sunlight);
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
        for (Chunk[] chunkArray : chunks) {
            for (Chunk chunk : chunkArray) {
                if (chunk != null) {
                    chunk.spawnPassiveMobs(force);
                }
            }
        }
    }

    private void spawnHostileMobs(boolean force) {
        updateChunkMobs();
        for (Chunk[] chunkArray : chunks) {
            for (Chunk chunk : chunkArray) {
                if (chunk != null) {
                    chunk.spawnHostileMobs(force);
                }
            }
        }
    }

    private void updateChunkMobs() {
        for (Chunk[] chunkArray : chunks) {
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
        float ratio = sunlight / 15.0f;
        if (sunlight <= 10) {
            ratio = (float) (ratio * 0.6d);
        }
        if (sunlight == 4) {
            ratio = (float) (ratio * 0.1d);
        }
        float r = 0.7f * ratio;
        float g = 0.7f * ratio;
        float b = 0.9f * ratio;
        return Colour.packFloat(r, g, b, 1.0f);
    }

    public Vector3i getBlockPreviewLocation() {
        return blockPreviewLocation;
    }

    public Byte getPreviewBlockType() {
        return getBlockTypeAbsolute(blockPreviewLocation.x, blockPreviewLocation.y, blockPreviewLocation.z);
    }

    public Byte getPreviewBlockData() {
        return getBlockDataAbsolute(blockPreviewLocation.x, blockPreviewLocation.y, blockPreviewLocation.z);
    }

    public Byte getDownPreviewBlockType() {
        return getBlockTypeAbsolute(blockPreviewLocation.x, blockPreviewLocation.y - 1, blockPreviewLocation.z);
    }

    public Byte getLeftPreviewBlockType() {
        return getBlockTypeAbsolute(blockPreviewLocation.x - 1, blockPreviewLocation.y, blockPreviewLocation.z);
    }

    public Byte getBlockTypeAbsolute(int x, int y, int z) {
        Chunk chunk = getChunk(x / 16, z / 16);
        if (chunk != null) {
            return chunk.blockType(x % 16, y, z % 16);
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

    /**
     * Gets a loaded chunk
     *
     * @param x
     * @param z
     * @return the so-indexed chunk, or <code>null</code> if it does not exist
     */
    public Chunk getChunk(int x, int z) {
        int dx = x - chunkPosX;
        int dz = z - chunkPosZ;
        int caix = getLoadRadius() + dx;
        int caiz = getLoadRadius() + dz;
        if (caix < 0 || caix >= chunks.length || caiz < 0 || caiz >= chunks[caix].length) {
            return null;
        }
        return chunks[caix][caiz];
    }

    public Chunk getChunkByPos(int chunkX, int chunkZ) {
        for (Chunk[] chunkArray : chunks) {
            for (Chunk chunk : chunkArray) {
                if (chunk != null && chunk.chunkX == chunkX && chunk.chunkZ == chunkZ) {
                    return chunk;
                }
            }
        }
        return null;
    }

    /**
     * @param x
     * @param y
     * @param z
     * @return The chunklet that contains that point
     */
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

    /**
     * @param x
     * @param y
     * @param z
     * @return The type of the block that contains the point, or 0 if the chunk
     * is not loaded yet
     */
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

    /**
     * @return chunk load radius
     */
    public int getLoadRadius() {
        return loadradius;
    }

    public void clearCache() {
        for (int i = 0; i < chunks.length; i++) {
            for (int j = 0; j < chunks[i].length; j++) {
                Chunk chunk = chunks[i][j];
                if (chunk != null) {
                    chunk.unload();
                    chunks[i][j] = null;
                }
            }
        }
        for (int i2 = 0; i2 < renderList.length; i2++) {
            Chunklet c = renderList[i2];
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
        lightProcessor.recalculateSkyLightingAround(chunk, bx, by, bz);
    }

    public void recalculateBlockLight(final Chunk chunk, final int bx, final int by, final int bz) {
        Runnable r = () -> {
            lightProcessor.recalculateBlockLightAround(chunk, bx, by, bz);
            List<Chunklet> chunkletsLightRecalc = new ArrayList<>(chunkletsForRecalculateLight());
            for (Chunklet chunklet : chunkletsLightRecalc) {
                chunklet.geomDirty = true;
                chunklet.generateGeometry(false);
            }
            chunkletsForRecalculateLight().clear();
        };
        GeometryGenerator.addTask(r);
    }

    public ArrayList<Chunklet> chunkletsForRecalculateLight() {
        return lightProcessor.getDirtyChunklet();
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
        lightProcessor.lightSunlitBlocksInChunk(c);
        lightProcessor.lightChunk(c);
    }

    public boolean isCancelLoad() {
        return isCancelLoad;
    }

    public void setCancel(boolean isCancel) {
        isCancelLoad = isCancel;
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
        Game.removeSurfaceListener(surfaceListener);
    }

    public int getSunlight() {
        return sunlight;
    }

    public boolean isChunksInited() {
        return isChunksInited;
    }

    public int getMapType() {
        return mapType;
    }

    public void setMapType(int mapType) {
        this.mapType = mapType;
    }

    public Furnace getFurnace(@NonNull Vector3i vec) {
        return getFurnace(vec.x, vec.y, vec.z);
    }

    public Furnace getFurnace(int x, int y, int z) {
        for (int i = 0; i < tileEntitiesList.size(); i++) {
            if (tileEntitiesList.get(i) != null && tileEntitiesList.get(i).isFurnace()) {
                Furnace furnace = (Furnace) tileEntitiesList.get(i);
                if (furnace.isSelectedEntity(x, y, z)) {
                    return furnace;
                }
            }
        }
        Furnace furnace2 = new Furnace(x, y, z);
        tileEntitiesList.add(furnace2);
        return furnace2;
    }

    public Chest getChest(@NonNull Vector3i vec) {
        return getChest(vec.x, vec.y, vec.z);
    }

    public Chest getChest(int x, int y, int z) {
        for (int i = 0; i < tileEntitiesList.size(); i++) {
            if (tileEntitiesList.get(i) != null && tileEntitiesList.get(i).isChest()) {
                Chest chest = (Chest) tileEntitiesList.get(i);
                if (chest.isSelectedEntity(x, y, z)) {
                    return chest;
                }
            }
        }
        Chest chest2 = new Chest(x, y, z);
        tileEntitiesList.add(chest2);
        return chest2;
    }

    public void addTileEntity(TileEntity tileEntity) {
        tileEntitiesList.add(tileEntity);
    }

    public Furnace getActiveFurnace() {
        return activeFurnace;
    }

    public void setActiveFurnace(Furnace activeFurnace) {
        this.activeFurnace = activeFurnace;
    }

    public Tag getLevelTag() {
        return levelTag;
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
                Vector3f vec = MathUtils.getVelocityVector(player.getAngle(), 0.75f);
                droppableItems.add(new DroppableItem(itemId, x + (vec.x * 1.5f), y, z + (vec.z * 1.5f), count, true));
            } else if (itemId == 82) {
                for (int i = 0; i < 4; i++) {
                    droppableItems.add(new DroppableItem(itemId, x, y, z, count, false));
                }
            } else if (itemId != 121) {
                droppableItems.add(new DroppableItem(itemId, x, y, z, count, false));
            }
        }
    }

    public void removeDroppableBlock(DroppableItem droppableItem) {
        droppableItems.remove(droppableItem);
    }

    public void addBlockParticle(byte itemID, Vector3f location, BlockFactory.WorldSide blockSide) {
        addBlockParticle(itemID, location, blockSide, false);
    }

    public void addBlockParticle(byte itemID, Vector3f location, BlockFactory.WorldSide blockSide, boolean isExplosion) {
        if (isExplosion) {
            for (int i = 0; i < 50; i++) {
                blockParticles.add(new BlockParticle(itemID, location.x, location.y, location.z, blockSide, isExplosion));
            }
        } else if (System.currentTimeMillis() - lastParticleTime > 80) {
            blockParticles.add(new BlockParticle(itemID, location.x, location.y, location.z, blockSide));
            lastParticleTime = System.currentTimeMillis();
        }
    }

    public void removeTileEntity(TileEntity tileEntity) {
        tileEntitiesList.remove(tileEntity);
    }

    public List<? extends TileEntity> getTileEntities(Chunk chunk) {
        List<TileEntity> result = new ArrayList<>();
        for (int i = 0; i < tileEntitiesList.size(); i++) {
            TileEntity item = tileEntitiesList.get(i);
            if (chunk.contains(item.getX(), item.getZ())) {
                result.add(item);
            }
        }
        tileEntitiesList.removeAll(result);
        return result;
    }

    public void updateMobChunks() {
        for (Chunk[] chunkArray : chunks) {
            for (Chunk chunk : chunkArray) {
                if (chunk != null) {
                    chunk.updateMobs();
                }
            }
        }
    }

    public void executeOnInited(Runnable runnable) {
        if (runnable != null) {
            if (isChunksInited) {
                runnable.run();
                return;
            }
            synchronized (onInitedRunnableList) {
                onInitedRunnableList.add(runnable);
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
        blockEntityPainter.add(tnt);
        tnt.activate(detonationType, removeBlock);
    }

    public void activateTNT(Vector3f targetBlockLocation, TNTBlock.DetonationDelayType detonationType, boolean removeBlock) {
        activateTNT(new Vector3i(targetBlockLocation), detonationType, removeBlock);
    }

    public static class ChunkSorter implements Comparator<Chunklet> {
        private final Vector3f eye = new Vector3f();

        @Override
        public int compare(@NonNull Chunklet a, @NonNull Chunklet b) {
            float ad = a.distanceSq(eye.x, eye.y, eye.z);
            float bd = b.distanceSq(eye.x, eye.y, eye.z);
            return (int) Math.signum(ad - bd);
        }
    }
}
