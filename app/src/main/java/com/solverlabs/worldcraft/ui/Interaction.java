package com.solverlabs.worldcraft.ui;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.geom.BedBlock;
import com.solverlabs.droid.rugl.geom.DoorBlock;
import com.solverlabs.droid.rugl.geom.LadderBlock;
import com.solverlabs.droid.rugl.input.Touch;
import com.solverlabs.droid.rugl.util.DestroyBlockSpeed;
import com.solverlabs.droid.rugl.util.FPSCamera;
import com.solverlabs.droid.rugl.util.geom.BoundingCuboid;
import com.solverlabs.droid.rugl.util.geom.GridIterate;
import com.solverlabs.droid.rugl.util.geom.Vector3f;
import com.solverlabs.droid.rugl.util.geom.Vector3i;
import com.solverlabs.droid.rugl.util.math.Range;
import com.solverlabs.worldcraft.GameMode;
import com.solverlabs.worldcraft.Player;
import com.solverlabs.worldcraft.R;
import com.solverlabs.worldcraft.SoundManager;
import com.solverlabs.worldcraft.Sounds;
import com.solverlabs.worldcraft.World;
import com.solverlabs.worldcraft.blockentity.BlockEntityPainter;
import com.solverlabs.worldcraft.chunk.Chunk;
import com.solverlabs.worldcraft.chunk.Chunklet;
import com.solverlabs.worldcraft.chunk.tile_entity.Chest;
import com.solverlabs.worldcraft.chunk.tile_entity.Furnace;
import com.solverlabs.worldcraft.factories.BlockFactory;
import com.solverlabs.worldcraft.factories.ItemFactory;
import com.solverlabs.worldcraft.inventory.InventoryItem;
import com.solverlabs.worldcraft.material.Material;
import com.solverlabs.worldcraft.mob.Mob;
import com.solverlabs.worldcraft.mob.MobPainter;
import com.solverlabs.worldcraft.multiplayer.Multiplayer;
import com.solverlabs.worldcraft.srv.domain.Room;
import com.solverlabs.worldcraft.util.Distance;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Interaction implements Touch.TouchListener {
    public static final int NOISE_NOTIFICATION_DELAY = 200;
    private static final float MIN_HOSTILE_MOB_DISTANCE_TO_SLEEP = 5.0f;
    private final Vector3f actionDirection;
    private final BoundingCuboid blockBounds;
    private final Vector3i breakingLocation;
    private final FPSCamera camera;
    private final ChatBox chatBox;
    private final Context context;
    private final BlockEntityPainter entityPainter;
    private final GridIterate gridIterate;
    private final Hand hand;
    private final MobPainter mobAggregator;
    private final Vector3i placementTargetBlock;
    private final Player player;
    private final Vector3i targetBlockLocation;
    private final World world;
    public float creativeModeToolTime;
    public float range = 6.0f;
    public boolean showChestMenu;
    public boolean showCraftingTable;
    public boolean showFurnaceMenu;
    public Touch.Pointer stickTouch;
    public BlockFactory.WorldSide targetBlockSide;
    public boolean touchSticksHeld;
    protected Vector3i tileEntityLocation;
    private float breakingProgress;
    private boolean justBroken;
    private float lastInteractionX;
    private float lastInteractionY;
    private long lastNoiseNotificationAt;
    private ItemFactory.Item sweptItem;
    private boolean targetValid;
    private Touch.Pointer touch;

    public Interaction(Player player, World world, FPSCamera camera, Hand hand, MobPainter mobAggregator, BlockEntityPainter entityPainter, ChatBox chatBox, Context context) {
        this.creativeModeToolTime = GameMode.isMultiplayerMode() ? 2.4f : 0.6f;
        this.touch = null;
        this.stickTouch = null;
        this.actionDirection = new Vector3f();
        this.targetValid = false;
        this.targetBlockLocation = new Vector3i();
        this.targetBlockSide = BlockFactory.WorldSide.Empty;
        this.tileEntityLocation = new Vector3i();
        this.placementTargetBlock = new Vector3i();
        this.gridIterate = new GridIterate();
        this.blockBounds = new BoundingCuboid(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
        this.sweptItem = null;
        this.breakingLocation = new Vector3i();
        this.breakingProgress = 0.0f;
        this.justBroken = false;
        this.touchSticksHeld = false;
        this.showCraftingTable = false;
        this.showFurnaceMenu = false;
        this.showChestMenu = false;
        this.player = player;
        this.world = world;
        this.camera = camera;
        this.hand = hand;
        this.mobAggregator = mobAggregator;
        this.entityPainter = entityPainter;
        this.chatBox = chatBox;
        this.context = context;
    }

    public void advance(float delta) {
        this.world.setBlockPlacePreview(false, 0, 0, 0);
        this.hand.stopStriking();
        if (this.stickTouch != null) {
            this.hand.repeatedStrike(false);
            if (this.touchSticksHeld) {
                held(this.stickTouch.x, this.stickTouch.y, delta);
                this.world.setBlockPlacePreview(this.targetValid, this.targetBlockLocation.x, this.targetBlockLocation.y, this.targetBlockLocation.z);
            } else {
                this.hand.stopStriking();
                updateTarget(this.stickTouch.x, this.stickTouch.y);
                this.world.setBlockPlacePreview(this.targetValid, this.targetBlockLocation.x, this.targetBlockLocation.y, this.targetBlockLocation.z);
            }
        } else {
            this.breakingProgress = 0.0f;
        }
        if (this.player != null && this.player.isReadyToEat()) {
            this.breakingProgress = 0.0f;
            if (this.touchSticksHeld) {
                this.player.eat();
                this.world.addBlockParticle(this.player.inHand.getItemID(), this.player.position, BlockFactory.WorldSide.Empty);
                if (System.currentTimeMillis() - this.lastNoiseNotificationAt > 200) {
                    this.lastNoiseNotificationAt = System.currentTimeMillis();
                    SoundManager.playMaterialSound(Material.FOOD, 0.0f);
                }
            }
        }
        if (this.world.breakingShape != null) {
            this.world.breakingShape.updateBreakingProgress(this.breakingProgress);
        }
    }

    private InventoryItem activeItem() {
        return this.player.inHand;
    }

    public void swipeFromHotBar(ItemFactory.Item item, Touch.Pointer sweptTouch) {
        if (this.touch == null) {
            this.sweptItem = item;
            this.touch = sweptTouch;
        }
    }

    @Override
    public boolean pointerAdded(Touch.Pointer p) {
        if (this.touch == null) {
            this.touch = p;
            return true;
        }
        return false;
    }

    @Override
    public void pointerRemoved(Touch.Pointer p) {
        if (this.touch == p) {
            this.touch = null;
            if (!this.justBroken) {
                action(activeItem(), p.x, p.y);
            }
            this.sweptItem = null;
            this.justBroken = false;
        }
    }

    @Override
    public void reset() {
        this.touch = null;
        this.sweptItem = null;
        this.justBroken = false;
    }

    public void setBlockType(int x, int y, int z, int chunkX, int chunkZ, byte blockId, byte blockData, boolean multiplayerSend) {
        Chunk chunk = this.world.getChunk(chunkX, chunkZ);
        if (chunk != null) {
            chunk.setBlockTypeForPosition((chunkX * 16) + x, y, (chunkZ * 16) + z, blockId, blockData, multiplayerSend);
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

    private void recalculateChunksLight(@NonNull Collection<Chunk> chunks) {
        int currChunk = 0;
        int chunkCount = chunks.size();
        for (Chunk chunk : chunks) {
            this.world.recalculateSkyLight(chunk, 0, 0, 0);
            incLoadingProgress(((currChunk / chunkCount) * 33.0f) + 33.0f);
            currChunk++;
        }
    }

    private void generateChunkletsGeometry(@NonNull Collection<Chunklet> chunklets) {
        int currChunklet = 0;
        int chunkletCount = chunklets.size();
        boolean updateLoadingDialog = this.world.isLoadingDialogVisible();
        for (Chunklet chunklet : chunklets) {
            if (chunklet != null) {
                chunklet.geomDirty();
                chunklet.generateGeometry(false);
            }
            if (updateLoadingDialog) {
                incLoadingProgress(64.0f + ((currChunklet / chunkletCount) * 33.0f));
                currChunklet++;
            }
        }
    }

    private void incLoadingProgress(float progress) {
        this.world.setLoadingProgressStatus((int) progress, 100);
    }

    public void setBlocks(@NonNull Map<List<Short>, Room.BlockData> blocks) {
        int currChunklet = 0;
        int chunkletCount = blocks.size();
        boolean updateLoadingDialog = this.world.isLoadingDialogVisible();
        Set<Chunklet> chunkletSet = new HashSet<>();
        for (Map.Entry<List<Short>, Room.BlockData> block : blocks.entrySet()) {
            List<Short> coords = block.getKey();
            short x = coords.get(0);
            short z = coords.get(2);
            if (x != -1 && z != -1) {
                short y = coords.get(1);
                short chunkX = coords.get(3);
                short chunkZ = coords.get(4);
                Room.BlockData blockData = block.getValue();
                Chunk chunk = this.world.getChunk(chunkX, chunkZ);
                if (chunk != null) {
                    try {
                        Set<Chunklet> chs = chunk.setBlockTypeWithoutGeometryRecalculate(x, y, z, blockData.blockType, blockData.blockData);
                        if (chs != null) {
                            chunkletSet.addAll(chs);
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        Log.d("Interaction", "ArrayIndexOutOfBoundsException x=" + ((int) x) + " y=" + ((int) y) + " z=" + ((int) z) + " chunkX=" + ((int) chunkX) + " chunkZ=" + ((int) chunkZ));
                        e.printStackTrace();
                    }
                }
                if (updateLoadingDialog) {
                    incLoadingProgress((currChunklet / chunkletCount) * 33.0f);
                    currChunklet++;
                }
            }
        }
        recalculateChunksLight(getChunks(chunkletSet));
        generateChunkletsGeometry(chunkletSet);
    }

    public void action(InventoryItem invItem, float x, float y) {
        Byte targetBlockType = this.world.getBlockTypeAbsolute(this.targetBlockLocation.x, this.targetBlockLocation.y, this.targetBlockLocation.z);
        if (DoorBlock.isDoor(targetBlockType)) {
            DoorBlock.actionDoor(targetBlockType, this.world, this.targetBlockLocation);
            this.hand.strike(true);
        } else if (LadderBlock.isLadder(targetBlockType)) {
            this.hand.strike(true);
        } else if (GameMode.isSurvivalMode() && BedBlock.isBed(targetBlockType)) {
            tryToSleep();
            this.hand.strike(true);
        } else {
            if (targetBlockType != null && GameMode.isSurvivalMode()) {
                if (targetBlockType == BlockFactory.CRAFTING_TABLE_ID) {
                    this.showCraftingTable = true;
                    this.hand.strike(true);
                    return;
                } else if (targetBlockType == BlockFactory.FURNACE_ID || targetBlockType == BlockFactory.FURNACE_ACTIVE_ID) {
                    this.tileEntityLocation = this.targetBlockLocation;
                    this.showFurnaceMenu = true;
                    this.hand.strike(true);
                    return;
                } else if (targetBlockType == BlockFactory.CHEST_ID) {
                    this.tileEntityLocation = this.targetBlockLocation;
                    this.showChestMenu = true;
                    this.hand.strike(true);
                    return;
                }
            }
            if (!Multiplayer.instance.isInMultiplayerMode && targetBlockType != null && targetBlockType == 46) {
                this.world.activateTNT(this.targetBlockLocation);
                this.hand.strike(true);
                return;
            }
            Mob mob = this.mobAggregator.getMobOnRay(this.player.position.x, this.player.position.y, this.player.position.z, this.actionDirection.x, this.actionDirection.y, this.actionDirection.z);
            if (mob != null) {
                this.hand.strike(true);
                mob.tryAttack(this.player);
            } else if (invItem != null) {
                Chunk chunk = updateTarget(x, y);
                this.hand.strike(false);
                if (chunk != null && this.targetValid && invItem.getBlock() != null) {
                    this.blockBounds.set(this.placementTargetBlock.x, this.placementTargetBlock.y, this.placementTargetBlock.z, this.placementTargetBlock.x + 1, this.placementTargetBlock.y + 1, this.placementTargetBlock.z + 1);
                    if (GameMode.isMultiplayerMode() && Multiplayer.isReadOnly()) {
                        Multiplayer.showReadOnlyRoomModificationDialog();
                    } else if (!this.player.playerBounds.intersects(this.blockBounds)) {
                        this.hand.strike(true);
                        if (DoorBlock.isDoor(invItem.getItemID())) {
                            DoorBlock.placeDoor(chunk, this.placementTargetBlock, invItem, this.player);
                        } else if (BedBlock.isBed(invItem.getItemID())) {
                            BedBlock.placeBed(chunk, this.placementTargetBlock, invItem, this.player);
                        } else if (LadderBlock.isLadder(invItem.getItemID())) {
                            if (LadderBlock.canSetLadder(targetBlockType)) {
                                LadderBlock.placeLadder(chunk, this.placementTargetBlock, this.targetBlockSide, invItem, this.player);
                            }
                        } else if (targetBlockType != null && targetBlockType == BlockFactory.GRASS_ID) {
                            chunk.setBlockTypeForPosition(this.targetBlockLocation.x, this.targetBlockLocation.y, this.targetBlockLocation.z, invItem.getItemID(), (byte) 0);
                        } else {
                            chunk.setBlockTypeForPosition(this.placementTargetBlock.x, this.placementTargetBlock.y, this.placementTargetBlock.z, invItem.getItemID(), (byte) 0);
                        }
                        SoundManager.playAppeared(invItem.getMaterial(), 0.0f);
                        if (GameMode.isSurvivalMode()) {
                            this.player.inventory.decItem(invItem);
                        }
                    }
                }
            }
        }
    }

    private void tryToSleep() {
        float distanceToClosestHostileMob = MobPainter.getDistanceToClosestHostileMob(this.player.position.x, this.player.position.y, this.player.position.z);
        if (distanceToClosestHostileMob < MIN_HOSTILE_MOB_DISTANCE_TO_SLEEP) {
            if (this.chatBox != null) {
                this.chatBox.addMessage(this.context.getString(R.string.you_may_not_rest_now_there_are_monsters));
            }
        } else if (!this.world.isNightNow()) {
            if (this.chatBox != null) {
                this.chatBox.addMessage(this.context.getString(R.string.you_can_sleep_only_at_night));
            }
        } else {
            this.player.setSpawnPosition(getBedHeadBlock());
            this.player.setKeptDownAt(System.currentTimeMillis());
        }
    }

    @NonNull
    private Vector3i getBedHeadBlock() {
        Vector3i target = new Vector3i(this.targetBlockLocation);
        if (BedBlock.isBed(this.world.getBlockTypeAbsolute(this.targetBlockLocation.x + 1, this.targetBlockLocation.y, this.targetBlockLocation.z))) {
            target.x++;
        }
        target.y++;
        return target;
    }

    public void held(float x, float y, float delta) {
        Vector3i blockLocation;
        Chunk chunk = updateTarget(x, y);
        if (chunk != null && this.sweptItem == null) {
            BlockFactory.Block b = BlockFactory.getBlock(chunk.blockTypeForPosition(this.targetBlockLocation.x, this.targetBlockLocation.y, this.targetBlockLocation.z));
            if (GameMode.isMultiplayerMode() && Multiplayer.isReadOnly()) {
                Multiplayer.showReadOnlyRoomModificationDialog();
            } else if (this.breakingLocation.equals(this.targetBlockLocation) && b != null) {
                InventoryItem activeItem = activeItem();
                byte itemID = activeItem == null ? (byte) 0 : activeItem.getItemID();
                this.hand.repeatedStrike(true);
                float time = 0.0f;
                if (GameMode.isCreativeMode()) {
                    time = this.creativeModeToolTime;
                }
                if (GameMode.isSurvivalMode()) {
                    time = ((float) DestroyBlockSpeed.getSpeed(b.id, itemID)) / 1000.0f;
                }
                this.breakingProgress += delta / time;
                if (!this.player.isReadyToEat()) {
                    this.world.addBlockParticle(b.id, new Vector3f(this.targetBlockLocation), this.targetBlockSide);
                    if (System.currentTimeMillis() - this.lastNoiseNotificationAt > 200) {
                        this.lastNoiseNotificationAt = System.currentTimeMillis();
                        SoundManager.playHit(b.material, 0.0f);
                    }
                }
                if (this.breakingProgress > 1.0f) {
                    byte blockType = b.id;
                    if (DoorBlock.isDoor(blockType)) {
                        blockLocation = DoorBlock.getDoorDownBlockLocation(chunk, this.targetBlockLocation);
                    } else if (BedBlock.isBed(blockType)) {
                        blockLocation = BedBlock.getBedLeftBlockLocation(chunk, this.targetBlockLocation);
                    } else {
                        blockLocation = this.targetBlockLocation;
                    }
                    this.world.addDroppableItem(b.id, blockLocation);
                    if ((blockType == BlockFactory.FURNACE_ID || blockType == BlockFactory.FURNACE_ACTIVE_ID) && this.world.getFurnace(blockLocation) != null) {
                        Furnace furnace = this.world.getFurnace(blockLocation);
                        if (furnace.getCraftedItem() != null) {
                            this.world.addDroppableItem(furnace.getCraftedItem().getItemID(), blockLocation, furnace.getCraftedItemCount());
                        }
                        if (furnace.getMaterial() != null) {
                            this.world.addDroppableItem(furnace.getMaterial().getItemID(), blockLocation, furnace.getMaterialCount());
                        }
                        if (furnace.getFuel() != null) {
                            this.world.addDroppableItem(furnace.getFuel().getItemID(), blockLocation, furnace.getFuelCount());
                        }
                        this.world.removeTileEntity(furnace);
                    }
                    if (blockType == BlockFactory.CHEST_ID && this.world.getChest(blockLocation) != null) {
                        Chest chest = this.world.getChest(blockLocation);
                        for (int i = 0; i < chest.getChestItems().size(); i++) {
                            InventoryItem chestItem = chest.getChestItems().get(i);
                            this.world.addDroppableItem(chestItem.getItemID(), blockLocation, chestItem.getCount());
                        }
                        this.world.removeTileEntity(chest);
                    }
                    if (DoorBlock.isDoor(blockType)) {
                        DoorBlock.breakDoor(chunk, this.targetBlockLocation);
                    } else if (BedBlock.isBed(blockType)) {
                        BedBlock.breakBed(chunk, this.targetBlockLocation, this.player);
                    } else if (LadderBlock.isLadder(blockType)) {
                        LadderBlock.breakLadder(chunk, this.targetBlockLocation);
                    } else {
                        chunk.setBlockTypeForPosition(this.targetBlockLocation.x, this.targetBlockLocation.y, this.targetBlockLocation.z, (byte) 0, (byte) 0);
                        byte type = chunk.blockTypeForPosition(this.targetBlockLocation.x, this.targetBlockLocation.y + 1, this.targetBlockLocation.z);
                        if (DoorBlock.isDoor(type)) {
                            DoorBlock.breakDoor(chunk, new Vector3i(this.targetBlockLocation.x, this.targetBlockLocation.y + 1, this.targetBlockLocation.z));
                            this.world.addDroppableItem(type, new Vector3i(this.targetBlockLocation.x, this.targetBlockLocation.y + 1, this.targetBlockLocation.z));
                        }
                        LadderBlock.parentDestroyed(chunk, this, this.world, this.targetBlockLocation);
                    }
                    this.player.decActiveItemDurability();
                    this.player.increaseExhaustionLevel(0.025f);
                    this.hand.stopStriking();
                    if (b == BlockFactory.Block.Glass) {
                        SoundManager.playDistancedSound(Sounds.GLASS, 0.0f);
                    } else {
                        SoundManager.playBroke(b.material, 0.0f);
                    }
                    this.justBroken = true;
                }
            } else {
                this.breakingLocation.set(this.targetBlockLocation);
                this.breakingProgress = 0.0f;
            }
        }
    }

    public Chunk updateTarget(float x, float y) {
        Chunklet chunklet;
        float x2 = (Range.toRatio(x, 0.0f, 800.0f) * 2.0f) - 1.0f;
        float y2 = (Range.toRatio(y, 0.0f, 480.0f) * 2.0f) - 1.0f;
        if (this.lastInteractionX == x2 && this.lastInteractionY == y2) {
            return null;
        }
        this.camera.unProject(x2, y2, this.actionDirection);
        this.actionDirection.scale(this.range);
        if (this.world.getChunklet(this.player.position.x + this.actionDirection.x, this.player.position.y + this.actionDirection.y, this.player.position.z + this.actionDirection.z) != null) {
            if (this.mobAggregator.selectMobOnRay(this.player.position.x, this.player.position.y, this.player.position.z, this.actionDirection.x, this.actionDirection.y, this.actionDirection.z)) {
                this.targetBlockLocation.set(0, 0, 0);
                return null;
            }
            this.gridIterate.setSeg(this.player.position.x, this.player.position.y, this.player.position.z, this.player.position.x + this.actionDirection.x, this.player.position.y + this.actionDirection.y, this.player.position.z + this.actionDirection.z);
            this.targetBlockLocation.set(this.gridIterate.lastGridCoords);
            this.placementTargetBlock.set(this.gridIterate.lastGridCoords);
            this.targetValid = false;
            do {
                chunklet = this.world.getChunklet(this.gridIterate.lastGridCoords.x, this.gridIterate.lastGridCoords.y, this.gridIterate.lastGridCoords.z);
                if (chunklet != null) {
                    byte bt = chunklet.parent.blockTypeForPosition(this.gridIterate.lastGridCoords.x, this.gridIterate.lastGridCoords.y, this.gridIterate.lastGridCoords.z);
                    if (bt == 0 || bt == BlockFactory.Block.Water.id || bt == BlockFactory.Block.StillWater.id || isPlayerInTransparentBlock(bt) || BlockFactory.getBlock(bt) == null) {
                        this.placementTargetBlock.set(this.gridIterate.lastGridCoords);
                        this.gridIterate.next();
                    } else {
                        this.targetBlockLocation.set(this.gridIterate.lastGridCoords);
                        this.targetBlockSide = findBlockSide(this.gridIterate.lastGridExit);
                        this.targetValid = true;
                    }
                } else {
                    this.targetValid = false;
                    this.gridIterate.setDone(true);
                }
                if (this.targetValid) {
                    break;
                }
            } while (!this.gridIterate.isDone());
            if (this.gridIterate.isDone()) {
                this.lastInteractionX = x2;
                this.lastInteractionY = y2;
            }
            return chunklet == null ? null : chunklet.parent;
        }
        return null;
    }

    private boolean isPlayerInTransparentBlock(byte bt) {
        Vector3f blockCenter = new Vector3f(this.gridIterate.lastGridCoords);
        blockCenter.x += 0.5f;
        blockCenter.y += 0.5f;
        blockCenter.z += 0.5f;
        float distance = Distance.getDistanceBetweenPoints(this.world.player.position, blockCenter, 999.0f);
        return distance < 1.0f && isTransparentBlock(bt);
    }

    private boolean isTransparentBlock(byte bt) {
        return bt == BlockFactory.Block.Flower.id || bt == BlockFactory.Block.Grass.id || bt == BlockFactory.Block.Ladder.id;
    }

    private BlockFactory.WorldSide findBlockSide(GridIterate.Move lastExit) {
        BlockFactory.WorldSide res = BlockFactory.WorldSide.Empty;
        if (lastExit != null) {
            switch (lastExit) {
                case Z_HIGH:
                    return BlockFactory.WorldSide.East;
                case Z_LOW:
                    return BlockFactory.WorldSide.West;
                case X_HIGH:
                    return BlockFactory.WorldSide.North;
                case X_LOW:
                    return BlockFactory.WorldSide.South;
                case Y_HIGH:
                    return BlockFactory.WorldSide.Bottom;
                case Y_LOW:
                    return BlockFactory.WorldSide.Top;
                default:
                    return BlockFactory.WorldSide.Empty;
            }
        }
        return res;
    }
}
