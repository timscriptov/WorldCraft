package com.mcal.worldcraft.ui;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.mcal.droid.rugl.geom.BedBlock;
import com.mcal.droid.rugl.geom.DoorBlock;
import com.mcal.droid.rugl.geom.LadderBlock;
import com.mcal.droid.rugl.input.Touch;
import com.mcal.droid.rugl.util.DestroyBlockSpeed;
import com.mcal.droid.rugl.util.FPSCamera;
import com.mcal.droid.rugl.util.geom.BoundingCuboid;
import com.mcal.droid.rugl.util.geom.GridIterate;
import com.mcal.droid.rugl.util.geom.Vector3f;
import com.mcal.droid.rugl.util.geom.Vector3i;
import com.mcal.droid.rugl.util.math.Range;
import com.mcal.worldcraft.GameMode;
import com.mcal.worldcraft.Player;
import com.mcal.worldcraft.R;
import com.mcal.worldcraft.SoundManager;
import com.mcal.worldcraft.Sounds;
import com.mcal.worldcraft.World;
import com.mcal.worldcraft.blockentity.BlockEntityPainter;
import com.mcal.worldcraft.chunk.Chunk;
import com.mcal.worldcraft.chunk.Chunklet;
import com.mcal.worldcraft.chunk.tile_entity.Chest;
import com.mcal.worldcraft.chunk.tile_entity.Furnace;
import com.mcal.worldcraft.factories.BlockFactory;
import com.mcal.worldcraft.factories.ItemFactory;
import com.mcal.worldcraft.inventory.InventoryItem;
import com.mcal.worldcraft.material.Material;
import com.mcal.worldcraft.mob.Mob;
import com.mcal.worldcraft.mob.MobPainter;
import com.mcal.worldcraft.multiplayer.Multiplayer;
import com.mcal.worldcraft.srv.domain.Room;
import com.mcal.worldcraft.utils.Distance;

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
        creativeModeToolTime = GameMode.isMultiplayerMode() ? 2.4f : 0.6f;
        touch = null;
        stickTouch = null;
        actionDirection = new Vector3f();
        targetValid = false;
        targetBlockLocation = new Vector3i();
        targetBlockSide = BlockFactory.WorldSide.Empty;
        tileEntityLocation = new Vector3i();
        placementTargetBlock = new Vector3i();
        gridIterate = new GridIterate();
        blockBounds = new BoundingCuboid(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
        sweptItem = null;
        breakingLocation = new Vector3i();
        breakingProgress = 0.0f;
        justBroken = false;
        touchSticksHeld = false;
        showCraftingTable = false;
        showFurnaceMenu = false;
        showChestMenu = false;
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
        world.setBlockPlacePreview(false, 0, 0, 0);
        hand.stopStriking();
        if (stickTouch != null) {
            hand.repeatedStrike(false);
            if (touchSticksHeld) {
                held(stickTouch.x, stickTouch.y, delta);
                world.setBlockPlacePreview(targetValid, targetBlockLocation.x, targetBlockLocation.y, targetBlockLocation.z);
            } else {
                hand.stopStriking();
                updateTarget(stickTouch.x, stickTouch.y);
                world.setBlockPlacePreview(targetValid, targetBlockLocation.x, targetBlockLocation.y, targetBlockLocation.z);
            }
        } else {
            breakingProgress = 0.0f;
        }
        if (player != null && player.isReadyToEat()) {
            breakingProgress = 0.0f;
            if (touchSticksHeld) {
                player.eat();
                world.addBlockParticle(player.inHand.getItemID(), player.position, BlockFactory.WorldSide.Empty);
                if (System.currentTimeMillis() - lastNoiseNotificationAt > 200) {
                    lastNoiseNotificationAt = System.currentTimeMillis();
                    SoundManager.playMaterialSound(Material.FOOD, 0.0f);
                }
            }
        }
        if (world.breakingShape != null) {
            world.breakingShape.updateBreakingProgress(breakingProgress);
        }
    }

    private InventoryItem activeItem() {
        return player.inHand;
    }

    public void swipeFromHotBar(ItemFactory.Item item, Touch.Pointer sweptTouch) {
        if (touch == null) {
            sweptItem = item;
            touch = sweptTouch;
        }
    }

    @Override
    public boolean pointerAdded(Touch.Pointer p) {
        if (touch == null) {
            touch = p;
            return true;
        }
        return false;
    }

    @Override
    public void pointerRemoved(Touch.Pointer p) {
        if (touch == p) {
            touch = null;
            if (!justBroken) {
                action(activeItem(), p.x, p.y);
            }
            sweptItem = null;
            justBroken = false;
        }
    }

    @Override
    public void reset() {
        touch = null;
        sweptItem = null;
        justBroken = false;
    }

    public void setBlockType(int x, int y, int z, int chunkX, int chunkZ, byte blockId, byte blockData, boolean multiplayerSend) {
        Chunk chunk = world.getChunk(chunkX, chunkZ);
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
            world.recalculateSkyLight(chunk, 0, 0, 0);
            incLoadingProgress(((currChunk / chunkCount) * 33.0f) + 33.0f);
            currChunk++;
        }
    }

    private void generateChunkletsGeometry(@NonNull Collection<Chunklet> chunklets) {
        int currChunklet = 0;
        int chunkletCount = chunklets.size();
        boolean updateLoadingDialog = world.isLoadingDialogVisible();
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
        world.setLoadingProgressStatus((int) progress, 100);
    }

    public void setBlocks(@NonNull Map<List<Short>, Room.BlockData> blocks) {
        int currChunklet = 0;
        int chunkletCount = blocks.size();
        boolean updateLoadingDialog = world.isLoadingDialogVisible();
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
                Chunk chunk = world.getChunk(chunkX, chunkZ);
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
        Byte targetBlockType = world.getBlockTypeAbsolute(targetBlockLocation.x, targetBlockLocation.y, targetBlockLocation.z);
        if (DoorBlock.isDoor(targetBlockType)) {
            DoorBlock.actionDoor(targetBlockType, world, targetBlockLocation);
            hand.strike(true);
        } else if (LadderBlock.isLadder(targetBlockType)) {
            hand.strike(true);
        } else if (GameMode.isSurvivalMode() && BedBlock.isBed(targetBlockType)) {
            tryToSleep();
            hand.strike(true);
        } else {
            if (targetBlockType != null && GameMode.isSurvivalMode()) {
                if (targetBlockType == BlockFactory.CRAFTING_TABLE_ID) {
                    showCraftingTable = true;
                    hand.strike(true);
                    return;
                } else if (targetBlockType == BlockFactory.FURNACE_ID || targetBlockType == BlockFactory.FURNACE_ACTIVE_ID) {
                    tileEntityLocation = targetBlockLocation;
                    showFurnaceMenu = true;
                    hand.strike(true);
                    return;
                } else if (targetBlockType == BlockFactory.CHEST_ID) {
                    tileEntityLocation = targetBlockLocation;
                    showChestMenu = true;
                    hand.strike(true);
                    return;
                }
            }
            if (!Multiplayer.instance.isInMultiplayerMode && targetBlockType != null && targetBlockType == 46) {
                world.activateTNT(targetBlockLocation);
                hand.strike(true);
                return;
            }
            Mob mob = mobAggregator.getMobOnRay(player.position.x, player.position.y, player.position.z, actionDirection.x, actionDirection.y, actionDirection.z);
            if (mob != null) {
                hand.strike(true);
                mob.tryAttack(player);
            } else if (invItem != null) {
                Chunk chunk = updateTarget(x, y);
                hand.strike(false);
                if (chunk != null && targetValid && invItem.getBlock() != null) {
                    blockBounds.set(placementTargetBlock.x, placementTargetBlock.y, placementTargetBlock.z, placementTargetBlock.x + 1, placementTargetBlock.y + 1, placementTargetBlock.z + 1);
                    if (GameMode.isMultiplayerMode() && Multiplayer.isReadOnly()) {
                        Multiplayer.showReadOnlyRoomModificationDialog();
                    } else if (!player.playerBounds.intersects(blockBounds)) {
                        hand.strike(true);
                        if (DoorBlock.isDoor(invItem.getItemID())) {
                            DoorBlock.placeDoor(chunk, placementTargetBlock, invItem, player);
                        } else if (BedBlock.isBed(invItem.getItemID())) {
                            BedBlock.placeBed(chunk, placementTargetBlock, invItem, player);
                        } else if (LadderBlock.isLadder(invItem.getItemID())) {
                            if (LadderBlock.canSetLadder(targetBlockType)) {
                                LadderBlock.placeLadder(chunk, placementTargetBlock, targetBlockSide, invItem, player);
                            }
                        } else if (targetBlockType != null && targetBlockType == BlockFactory.GRASS_ID) {
                            chunk.setBlockTypeForPosition(targetBlockLocation.x, targetBlockLocation.y, targetBlockLocation.z, invItem.getItemID(), (byte) 0);
                        } else {
                            chunk.setBlockTypeForPosition(placementTargetBlock.x, placementTargetBlock.y, placementTargetBlock.z, invItem.getItemID(), (byte) 0);
                        }
                        SoundManager.playAppeared(invItem.getMaterial(), 0.0f);
                        if (GameMode.isSurvivalMode()) {
                            player.inventory.decItem(invItem);
                        }
                    }
                }
            }
        }
    }

    private void tryToSleep() {
        float distanceToClosestHostileMob = MobPainter.getDistanceToClosestHostileMob(player.position.x, player.position.y, player.position.z);
        if (distanceToClosestHostileMob < MIN_HOSTILE_MOB_DISTANCE_TO_SLEEP) {
            if (chatBox != null) {
                chatBox.addMessage(context.getString(R.string.you_may_not_rest_now_there_are_monsters));
            }
        } else if (!world.isNightNow()) {
            if (chatBox != null) {
                chatBox.addMessage(context.getString(R.string.you_can_sleep_only_at_night));
            }
        } else {
            player.setSpawnPosition(getBedHeadBlock());
            player.setKeptDownAt(System.currentTimeMillis());
        }
    }

    @NonNull
    private Vector3i getBedHeadBlock() {
        Vector3i target = new Vector3i(targetBlockLocation);
        if (BedBlock.isBed(world.getBlockTypeAbsolute(targetBlockLocation.x + 1, targetBlockLocation.y, targetBlockLocation.z))) {
            target.x++;
        }
        target.y++;
        return target;
    }

    public void held(float x, float y, float delta) {
        Vector3i blockLocation;
        Chunk chunk = updateTarget(x, y);
        if (chunk != null && sweptItem == null) {
            BlockFactory.Block b = BlockFactory.getBlock(chunk.blockTypeForPosition(targetBlockLocation.x, targetBlockLocation.y, targetBlockLocation.z));
            if (GameMode.isMultiplayerMode() && Multiplayer.isReadOnly()) {
                Multiplayer.showReadOnlyRoomModificationDialog();
            } else if (breakingLocation.equals(targetBlockLocation) && b != null) {
                InventoryItem activeItem = activeItem();
                byte itemID = activeItem == null ? (byte) 0 : activeItem.getItemID();
                hand.repeatedStrike(true);
                float time = 0.0f;
                if (GameMode.isCreativeMode()) {
                    time = creativeModeToolTime;
                }
                if (GameMode.isSurvivalMode()) {
                    time = ((float) DestroyBlockSpeed.getSpeed(b.id, itemID)) / 1000.0f;
                }
                breakingProgress += delta / time;
                if (!player.isReadyToEat()) {
                    world.addBlockParticle(b.id, new Vector3f(targetBlockLocation), targetBlockSide);
                    if (System.currentTimeMillis() - lastNoiseNotificationAt > 200) {
                        lastNoiseNotificationAt = System.currentTimeMillis();
                        SoundManager.playHit(b.material, 0.0f);
                    }
                }
                if (breakingProgress > 1.0f) {
                    byte blockType = b.id;
                    if (DoorBlock.isDoor(blockType)) {
                        blockLocation = DoorBlock.getDoorDownBlockLocation(chunk, targetBlockLocation);
                    } else if (BedBlock.isBed(blockType)) {
                        blockLocation = BedBlock.getBedLeftBlockLocation(chunk, targetBlockLocation);
                    } else {
                        blockLocation = targetBlockLocation;
                    }
                    world.addDroppableItem(b.id, blockLocation);
                    if ((blockType == BlockFactory.FURNACE_ID || blockType == BlockFactory.FURNACE_ACTIVE_ID) && world.getFurnace(blockLocation) != null) {
                        Furnace furnace = world.getFurnace(blockLocation);
                        if (furnace.getCraftedItem() != null) {
                            world.addDroppableItem(furnace.getCraftedItem().getItemID(), blockLocation, furnace.getCraftedItemCount());
                        }
                        if (furnace.getMaterial() != null) {
                            world.addDroppableItem(furnace.getMaterial().getItemID(), blockLocation, furnace.getMaterialCount());
                        }
                        if (furnace.getFuel() != null) {
                            world.addDroppableItem(furnace.getFuel().getItemID(), blockLocation, furnace.getFuelCount());
                        }
                        world.removeTileEntity(furnace);
                    }
                    if (blockType == BlockFactory.CHEST_ID && world.getChest(blockLocation) != null) {
                        Chest chest = world.getChest(blockLocation);
                        for (int i = 0; i < chest.getChestItems().size(); i++) {
                            InventoryItem chestItem = chest.getChestItems().get(i);
                            world.addDroppableItem(chestItem.getItemID(), blockLocation, chestItem.getCount());
                        }
                        world.removeTileEntity(chest);
                    }
                    if (DoorBlock.isDoor(blockType)) {
                        DoorBlock.breakDoor(chunk, targetBlockLocation);
                    } else if (BedBlock.isBed(blockType)) {
                        BedBlock.breakBed(chunk, targetBlockLocation, player);
                    } else if (LadderBlock.isLadder(blockType)) {
                        LadderBlock.breakLadder(chunk, targetBlockLocation);
                    } else {
                        chunk.setBlockTypeForPosition(targetBlockLocation.x, targetBlockLocation.y, targetBlockLocation.z, (byte) 0, (byte) 0);
                        byte type = chunk.blockTypeForPosition(targetBlockLocation.x, targetBlockLocation.y + 1, targetBlockLocation.z);
                        if (DoorBlock.isDoor(type)) {
                            DoorBlock.breakDoor(chunk, new Vector3i(targetBlockLocation.x, targetBlockLocation.y + 1, targetBlockLocation.z));
                            world.addDroppableItem(type, new Vector3i(targetBlockLocation.x, targetBlockLocation.y + 1, targetBlockLocation.z));
                        }
                        LadderBlock.parentDestroyed(chunk, this, world, targetBlockLocation);
                    }
                    player.decActiveItemDurability();
                    player.increaseExhaustionLevel(0.025f);
                    hand.stopStriking();
                    if (b == BlockFactory.Block.Glass) {
                        SoundManager.playDistancedSound(Sounds.GLASS, 0.0f);
                    } else {
                        SoundManager.playBroke(b.material, 0.0f);
                    }
                    justBroken = true;
                }
            } else {
                breakingLocation.set(targetBlockLocation);
                breakingProgress = 0.0f;
            }
        }
    }

    public Chunk updateTarget(float x, float y) {
        Chunklet chunklet;
        float x2 = (Range.toRatio(x, 0.0f, 800.0f) * 2.0f) - 1.0f;
        float y2 = (Range.toRatio(y, 0.0f, 480.0f) * 2.0f) - 1.0f;
        if (lastInteractionX == x2 && lastInteractionY == y2) {
            return null;
        }
        camera.unProject(x2, y2, actionDirection);
        actionDirection.scale(range);
        if (world.getChunklet(player.position.x + actionDirection.x, player.position.y + actionDirection.y, player.position.z + actionDirection.z) != null) {
            if (mobAggregator.selectMobOnRay(player.position.x, player.position.y, player.position.z, actionDirection.x, actionDirection.y, actionDirection.z)) {
                targetBlockLocation.set(0, 0, 0);
                return null;
            }
            gridIterate.setSeg(player.position.x, player.position.y, player.position.z, player.position.x + actionDirection.x, player.position.y + actionDirection.y, player.position.z + actionDirection.z);
            targetBlockLocation.set(gridIterate.lastGridCoords);
            placementTargetBlock.set(gridIterate.lastGridCoords);
            targetValid = false;
            do {
                chunklet = world.getChunklet(gridIterate.lastGridCoords.x, gridIterate.lastGridCoords.y, gridIterate.lastGridCoords.z);
                if (chunklet != null) {
                    byte bt = chunklet.parent.blockTypeForPosition(gridIterate.lastGridCoords.x, gridIterate.lastGridCoords.y, gridIterate.lastGridCoords.z);
                    if (bt == 0 || bt == BlockFactory.Block.Water.id || bt == BlockFactory.Block.StillWater.id || isPlayerInTransparentBlock(bt) || BlockFactory.getBlock(bt) == null) {
                        placementTargetBlock.set(gridIterate.lastGridCoords);
                        gridIterate.next();
                    } else {
                        targetBlockLocation.set(gridIterate.lastGridCoords);
                        targetBlockSide = findBlockSide(gridIterate.lastGridExit);
                        targetValid = true;
                    }
                } else {
                    targetValid = false;
                    gridIterate.setDone(true);
                }
                if (targetValid) {
                    break;
                }
            } while (!gridIterate.isDone());
            if (gridIterate.isDone()) {
                lastInteractionX = x2;
                lastInteractionY = y2;
            }
            return chunklet == null ? null : chunklet.parent;
        }
        return null;
    }

    private boolean isPlayerInTransparentBlock(byte bt) {
        Vector3f blockCenter = new Vector3f(gridIterate.lastGridCoords);
        blockCenter.x += 0.5f;
        blockCenter.y += 0.5f;
        blockCenter.z += 0.5f;
        float distance = Distance.getDistanceBetweenPoints(world.player.position, blockCenter, 999.0f);
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
