package com.solverlabs.worldcraft;

import android.util.Log;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.geom.DoorBlock;
import com.solverlabs.droid.rugl.input.TapPad;
import com.solverlabs.droid.rugl.util.FPSCamera;
import com.solverlabs.droid.rugl.util.geom.BoundingCuboid;
import com.solverlabs.droid.rugl.util.geom.Vector2f;
import com.solverlabs.droid.rugl.util.geom.Vector3f;
import com.solverlabs.droid.rugl.util.geom.Vector3i;
import com.solverlabs.droid.rugl.util.math.Range;
import com.solverlabs.worldcraft.chunk.Chunk;
import com.solverlabs.worldcraft.chunk.tile_entity.Inventory;
import com.solverlabs.worldcraft.domain.Damagable;
import com.solverlabs.worldcraft.etc.Food;
import com.solverlabs.worldcraft.factories.BlockFactory;
import com.solverlabs.worldcraft.factories.DescriptionFactory;
import com.solverlabs.worldcraft.inventory.InventoryItem;
import com.solverlabs.worldcraft.inventory.InventoryTapItem;
import com.solverlabs.worldcraft.material.Material;
import com.solverlabs.worldcraft.math.MathUtils;
import com.solverlabs.worldcraft.multiplayer.Multiplayer;
import com.solverlabs.worldcraft.nbt.Tag;
import com.solverlabs.worldcraft.ui.GUI;
import com.solverlabs.worldcraft.util.FallDetector;
import com.solverlabs.worldcraft.util.GameTime;

import java.util.ArrayList;

import android2.util.FloatMath;


public class Player implements Damagable {
    public static final float ATTACKING_ENEMY_EXHAUSTION = 0.3f;
    public static final float DESTROY_BLOCK_EXHAUSTION = 0.025f;
    public static final float FOOD_POISONING_EXHAUSTION = 0.5f;
    public static final float JUMP_EXHAUSTION = 0.2f;
    public static final int MAX_FOOD_LEVEL = 20;
    public static final int MIN_HUNGER_DAMAGE_HEALTH_POINTS = 2;
    public static final float RECEIVING_DAMAGE_EXHAUSTION = 0.3f;
    public static final float WALK_EXHAUSTION = 0.01f;
    private static final int EAT_TIMEOUT = 2000;
    private static final String FOOD_EXHAUSTION_LEVEL_TAG = "foodExhaustionLevel";
    private static final String FOOD_LEVEL_TAG = "foodLevel";
    private static final String FOOD_SATURATION_LEVEL_TAG = "foodSaturationLevel";
    private static final String FOOD_TIMER_TAG = "foodTimer";
    private static final int FOOD_TIMER_TIMEOUT = 4000;
    private static final int HEALTHY_FOOD_LEVEL = 17;
    private static final int HEALTH_IS_JUST_UPDATED_TIMEOUT = 200;
    private static final int HUNGRY_FOOD_LEVEL = 0;
    private static final float MAX_FOOD_EXHAUSTION = 4.0f;
    private static final int MAX_HEALTH_POINTS = 20;
    private static final String PLAYER_HEALTH_TAG = "Health";
    private static final int STEP_NOTIFICATION_DELAY = 400;
    private static final long TIMEOUT_BETWEEN_DAMAGE = 2000;
    private final World mWorld;
    private final Vector3f collideCorrection = new Vector3f();
    private final BoundingCuboid blockBounds = new BoundingCuboid(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
    private final BoundingCuboid intersection = new BoundingCuboid(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
    private final Vector3f forward = new Vector3f();
    private final FallDetector fallDetector = new FallDetector(this);
    public float speed = MAX_FOOD_EXHAUSTION;
    public float crouchedSpeed = 2.0f;
    public float jumpSpeed = 6.0f;
    public float gravity = -10.0f;
    public boolean ghost = false;
    public float width = 0.2f;
    public float height = 1.8f;
    public float eyeLevel = 0.9f;
    public float crouchedEyeLevel = 0.65f;
    public boolean onGround = false;
    public Vector2f rotation = new Vector2f();
    public Vector3f position = new Vector3f();
    public Vector3f spawnPosition = new Vector3f();
    public Vector3f velocity = new Vector3f();
    public BoundingCuboid playerBounds = new BoundingCuboid(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
    public ArrayList<InventoryTapItem> hotbar = new ArrayList<>();
    public InventoryItem inHand = null;
    public Inventory inventory = new Inventory(this);
    private long eatingStartedAt;
    private float exhaustionWalkDistance;
    private float foodExhaustionLevel;
    private short foodLevel;
    private float foodSaturationLevel;
    private long foodTimer;
    private long healthUpdatedAt;
    private long mKeptDownAt;
    private long lastAdvanceAt;
    private long lastAdvanceInterval;
    private long lastStepNotificationAt;
    private boolean crouched = false;
    public TapPad.Listener jumpCrouchListener = new TapPad.Listener() {
        @Override
        public void onTap(TapPad pad) {
            if (ghost) {
                ghost = false;
            }
            doJump();
        }

        @Override
        public void onFlick(TapPad pad, int horizontal, int vertical) {
            if (vertical == 1) {
                onTap(pad);
            } else if (vertical == -1) {
                crouched = true;
            }
        }

        @Override
        public void onLongPress(TapPad pad) {
            crouched = true;
        }

        @Override
        public void onDoubleTap(TapPad pad) {
            if (GameMode.isCreativeMode()) {
                ghost = true;
            }
        }
    };
    private BlockFactory.WorldSide currentWorldSide = BlockFactory.WorldSide.North;
    private short healthPoints = 20;
    private Long damagedAt = System.currentTimeMillis();

    public Player(World world) {
        mWorld = world;
        resetSavedPosition();
    }

    public void doJump() {
        if (crouched) {
            crouched = false;
        } else if (onGround) {
            velocity.y = jumpSpeed;
            if (GameMode.isSurvivalMode()) {
                increaseExhaustionLevel(0.2f);
            }
        }
    }

    public void init(Tag levelTag) {
        if (GameMode.isSurvivalMode()) {
            if (levelTag != null) {
                initInventory(levelTag.findTagByName("Inventory"));
                initHealth(levelTag);
                initFoodLevel(levelTag);
                initSpawnPos(levelTag);
                return;
            }
            return;
        }
        for (int i = 0; i < 5; i++) {
            addItemToHotBar(new InventoryTapItem(this, new InventoryItem(BlockFactory.Block.values()[i], i)));
        }
    }

    public void save(Tag playerTag) {
        saveInventory(playerTag);
        saveHealth(playerTag);
        saveFoodLevel(playerTag);
    }

    private void initSpawnPos(Tag levelTag) {
        try {
            float x = (Integer) levelTag.findTagByName("SpawnX").getValue();
            float y = (Integer) levelTag.findTagByName("SpawnY").getValue();
            float z = (Integer) levelTag.findTagByName("SpawnZ").getValue();
            spawnPosition.set(x, y, z);
        } catch (Exception e) {
            spawnPosition.set(50.0f, 80.0f, 50.0f);
        }
    }

    private void initInventory(@NonNull Tag inventoryTag) {
        Tag[] items = (Tag[]) inventoryTag.getValue();
        for (Tag itemTag : items) {
            int slot = (Integer) itemTag.findTagByName("Slot").getValue();
            byte id = (Byte) itemTag.findTagByName("id").getValue();
            int damage = (Integer) itemTag.findTagByName("Damage").getValue();
            int count = (Integer) itemTag.findTagByName("Count").getValue();
            boolean isInHotbar = (Integer) itemTag.findTagByName("Hotbar").getValue() == 1;
            if (count > 0) {
                InventoryItem invItem = new InventoryItem(id, slot, damage, count, isInHotbar);
                inventory.insertItem(invItem);
                if (isInHotbar) {
                    addItemToHotBar(new InventoryTapItem(this, invItem));
                }
            }
        }
    }

    private void saveInventory(@NonNull Tag playerTag) {
        playerTag.removeSubTag(playerTag.findTagByName("Inventory"));
        Tag inventoryTag = new Tag("Inventory", Tag.Type.TAG_Compound);
        for (int i = 0; i < inventory.getSize(); i++) {
            InventoryItem element = inventory.getElement(i);
            if (element.getItemID() != 0) {
                Tag[] tags = new Tag[6];
                tags[0] = new Tag(Tag.Type.TAG_Int, "Slot", element.getSlot());
                tags[1] = new Tag(Tag.Type.TAG_Byte, "id", element.getItemID());
                tags[2] = new Tag(Tag.Type.TAG_Int, "Damage", element.getCurrentDurability());
                tags[3] = new Tag(Tag.Type.TAG_Int, "Count", element.getCount());
                tags[4] = new Tag(Tag.Type.TAG_Int, "Hotbar", element.isInHotbar ? 1 : 0);
                tags[5] = new Tag(Tag.Type.TAG_End, (String) null, (Tag[]) null);
                Tag item = new Tag(Tag.Type.TAG_Compound, DescriptionFactory.emptyText, tags);
                inventoryTag.addTag(item);
            }
        }
        playerTag.addTag(inventoryTag);
    }

    private void initHealth(@NonNull Tag levelTag) {
        Object healthValue = levelTag.getTagValue(PLAYER_HEALTH_TAG);
        if (healthValue != null) {
            healthPoints = (Short) healthValue;
        }
    }

    private void saveHealth(@NonNull Tag playerTag) {
        playerTag.saveTagValue(Tag.Type.TAG_Short, PLAYER_HEALTH_TAG, healthPoints);
    }

    private void initFoodLevel(@NonNull Tag levelTag) {
        Object foodLevelValue = levelTag.getTagValue(FOOD_LEVEL_TAG);
        Object foodSaturationLevelValue = levelTag.getTagValue(FOOD_SATURATION_LEVEL_TAG);
        Object foodTimerValue = levelTag.getTagValue(FOOD_TIMER_TAG);
        Object foodExhaustionLevelValue = levelTag.getTagValue(FOOD_EXHAUSTION_LEVEL_TAG);
        foodLevel = foodLevelValue != null ? (Short) foodLevelValue : (short) 20;
        foodSaturationLevel = foodSaturationLevelValue != null ? (Float) foodSaturationLevelValue : foodLevel;
        foodTimer = foodTimerValue != null ? (Long) foodTimerValue : 0L;
        foodExhaustionLevel = foodExhaustionLevelValue != null ? (Float) foodExhaustionLevelValue : 0.0f;
    }

    private void saveFoodLevel(@NonNull Tag playerTag) {
        playerTag.saveTagValue(Tag.Type.TAG_Short, FOOD_LEVEL_TAG, foodLevel);
        playerTag.saveTagValue(Tag.Type.TAG_Float, FOOD_SATURATION_LEVEL_TAG, foodSaturationLevel);
        playerTag.saveTagValue(Tag.Type.TAG_Long, FOOD_TIMER_TAG, foodTimer);
        playerTag.saveTagValue(Tag.Type.TAG_Float, FOOD_EXHAUSTION_LEVEL_TAG, foodExhaustionLevel);
    }

    public void setSpawnPosition(Vector3i target) {
        spawnPosition.set(target);
    }

    public void reSpawnPosition() {
        position.set(spawnPosition);
        fallDetector.reset(position);
        velocity.set(0.0f, 0.0f, 0.0f);
    }

    public void resetSavedPosition() {
        position.set(mWorld.mStartPosition);
        fallDetector.reset(position);
        velocity.set(0.0f, 0.0f, 0.0f);
    }

    public void setWorldStartPosAsSpawnPos() {
        spawnPosition.set(mWorld.mStartPosition);
    }

    public boolean spawnBedExists(int x, int y, int z) {
        return spawnPosition.y - 1.0f == ((float) y) && spawnPosition.z == ((float) z) && spawnPosition.x >= ((float) (x + (-1))) && spawnPosition.x <= ((float) (x + 1));
    }

    public void advance(float delta, FPSCamera cam, GUI gui) {
        if (mWorld.getChunklet(position.x, position.y, position.z) != null) {
            updateLastAdvanceInterval();
            rotation.set(cam.getHeading(), cam.getElevation());
            forward.set(cam.forward);
            float s = crouched ? crouchedSpeed : speed;
            float diffX = delta * s * ((gui.left.y * forward.x) - (gui.left.x * cam.right.x));
            float diffZ = delta * s * ((gui.left.y * forward.z) - (gui.left.x * cam.right.z));
            float nextPosX = position.x + diffX;
            float nextPosZ = position.z + diffZ;
            if (inHand != null && inHand.isEmpty()) {
                inHand = null;
            }
            float headingAngle = Range.wrap(cam.getHeading(), 0.0f, 6.2831855f);
            setWorldSide(headingAngle);
            if (ghost) {
                if (mWorld.getChunklet(nextPosX, position.y, nextPosZ) != null) {
                    position.x = nextPosX;
                    position.z = nextPosZ;
                    position.y += gui.left.y * delta * cam.forward.y * s;
                    position.y += (-gui.left.x) * delta * cam.right.y * s;
                    position.y = Range.limit(position.y, 1.0f, 127.0f);
                    velocity.y = 0.0f;
                    float w = width / 2.0f;
                    float feet = height * (crouched ? crouchedEyeLevel : eyeLevel);
                    float head = height - feet;
                    playerBounds.set(position.x - w, position.y - feet, position.z - w, position.x + w, position.y + head, position.z + w);
                    for (float x = FloatMath.floor(playerBounds.x.getMin()); x < playerBounds.x.getMax(); x += 1.0f) {
                        for (float z = FloatMath.floor(playerBounds.z.getMin()); z < playerBounds.z.getMax(); z += 1.0f) {
                            for (float y = FloatMath.floor(playerBounds.y.getMin()); y < playerBounds.y.getMax(); y += 1.0f) {
                                collideCorrection.set(0.0f, 0.0f, 0.0f);
                                collide(x, y, z, collideCorrection);
                                playerBounds.translate(collideCorrection.x, collideCorrection.y, collideCorrection.z);
                                Vector3f.add(position, collideCorrection, position);
                            }
                        }
                    }
                }
            } else if (mWorld.getChunklet(nextPosX, position.y, nextPosZ) != null) {
                forward.y = 0.0f;
                forward.normalise();
                position.x = nextPosX;
                position.z = nextPosZ;
                if (mWorld.blockType(position.x, position.y, position.z) == 76) {
                    velocity.y = 0.0f;
                    position.y += gui.left.y * delta * cam.forward.y * s;
                    position.y += (-gui.left.x) * delta * cam.right.y * s;
                } else {
                    velocity.y += gravity * delta;
                }
                position.y += velocity.y * delta;
                float w2 = width / 2.0f;
                float feet2 = height * (crouched ? crouchedEyeLevel : eyeLevel);
                float head2 = height - feet2;
                playerBounds.set(position.x - w2, position.y - feet2, position.z - w2, position.x + w2, position.y + head2, position.z + w2);
                boolean groundHit = false;
                BlockFactory.Block groundBlock = null;
                for (float x2 = FloatMath.floor(playerBounds.x.getMin()); x2 < playerBounds.x.getMax(); x2 += 1.0f) {
                    for (float z2 = FloatMath.floor(playerBounds.z.getMin()); z2 < playerBounds.z.getMax(); z2 += 1.0f) {
                        for (float y2 = FloatMath.floor(playerBounds.y.getMin()); y2 < playerBounds.y.getMax(); y2 += 1.0f) {
                            collideCorrection.set(0.0f, 0.0f, 0.0f);
                            BlockFactory.Block collidedBlock = collide(x2, y2, z2, collideCorrection);
                            playerBounds.translate(collideCorrection.x, collideCorrection.y, collideCorrection.z);
                            Vector3f.add(position, collideCorrection, position);
                            if (collideCorrection.y != 0.0f && Math.signum(collideCorrection.y) != Math.signum(velocity.y)) {
                                velocity.y = 0.0f;
                            }
                            groundHit |= collideCorrection.y > 0.0f;
                            if (collideCorrection.y > 0.0f) {
                                groundBlock = collidedBlock;
                            }
                        }
                    }
                }
                onGround = groundHit;
                if (groundBlock != null) {
                    if (Math.abs(diffX) + Math.abs(diffZ) > 0.0f && System.currentTimeMillis() - lastStepNotificationAt > 400) {
                        lastStepNotificationAt = System.currentTimeMillis();
                        SoundManager.playStep(groundBlock.material, 0.0f);
                    }
                    exhaustionWalkDistance += FloatMath.sqrt((diffX * diffX) + (diffZ * diffZ));
                }
                if (GameMode.isSurvivalMode()) {
                    fallDetector.set(position, onGround);
                    updateFoodLevel();
                }
                position.y = Range.limit(position.y, 1.0f, 127.0f);
                if (GameMode.isMultiplayerMode() && Multiplayer.instance.movementHandler != null) {
                    Multiplayer.instance.movementHandler.set(position.x, position.y, position.z, cam.forward.x, cam.forward.y, cam.forward.z, cam.up.x, cam.up.y, cam.up.z);
                }
            }
        }
    }

    private void setWorldSide(float heading) {
        int side = (int) (((heading / 1.5707964f) - 0.7853982f) + 1.0f);
        if (side > 3) {
            side = 0;
        }
        switch (side) {
            case 0:
                currentWorldSide = BlockFactory.WorldSide.North;
                return;
            case 1:
                currentWorldSide = BlockFactory.WorldSide.West;
                return;
            case 2:
                currentWorldSide = BlockFactory.WorldSide.South;
                return;
            case 3:
                currentWorldSide = BlockFactory.WorldSide.East;
                return;
            default:
                return;
        }
    }

    public BlockFactory.WorldSide getCurrentWorldSide() {
        return currentWorldSide;
    }

    private void updateLastAdvanceInterval() {
        long currentGameTime = GameTime.getTime();
        if (lastAdvanceAt > 0) {
            lastAdvanceInterval = currentGameTime - lastAdvanceAt;
        }
        lastAdvanceAt = currentGameTime;
    }

    public float getAngle() {
        return MathUtils.normalizeAngle(Enemy.getAngle(forward.x, forward.y, forward.z));
    }

    private BlockFactory.Block collide(float x, float y, float z, Vector3f correction) {
        byte upperBlockType;
        byte bt = mWorld.blockType(x, y, z);
        if (y < 128.0f) {
            upperBlockType = mWorld.blockType(x, y + 1.0f, z);
        } else {
            upperBlockType = 0;
        }
        BlockFactory.Block b = BlockFactory.getBlock(bt);
        if (b != null && b != BlockFactory.Block.Water && b != BlockFactory.Block.StillWater && b.isCuboid) {
            float x2 = FloatMath.floor(x);
            float y2 = FloatMath.floor(y);
            float z2 = FloatMath.floor(z);
            if (DoorBlock.isDoor(b)) {
                DoorBlock.updateBlockBounds(blockBounds, x2, y2, z2, mWorld);
            } else {
                blockBounds.set(x2, y2, z2, x2 + 1.0f, y2 + 1.0f, z2 + 1.0f);
                BlockFactory.Block upperBlock = BlockFactory.getBlock(upperBlockType);
                if (b == BlockFactory.Block.Slab || upperBlockType == 0 || (upperBlock != null && !upperBlock.isCuboid)) {
                    blockBounds.y.set(y2, 0.5f + y2);
                }
            }
            if (playerBounds.intersection(blockBounds, intersection)) {
                correction(intersection, collideCorrection);
            }
        }
        return b;
    }

    private void correction(@NonNull BoundingCuboid intersection, Vector3f correction) {
        float mx = intersection.x.getSpan();
        float my = intersection.y.getSpan();
        float mz = intersection.z.getSpan();
        float midpoint = playerBounds.y.toValue(0.5f);
        if (my < 0.51f && intersection.y.toValue(0.5f) < midpoint) {
            correction.set(0.0f, my, 0.0f);
            correction.y *= 0.3f;
        } else if (mx < my && mx < mz) {
            if (intersection.x.toValue(0.5f) >= position.x) {
                mx = -mx;
            }
            correction.set(mx, 0.0f, 0.0f);
        } else if (my < mz) {
            if (intersection.y.toValue(0.5f) >= midpoint) {
                my = -my;
            }
            correction.set(0.0f, my, 0.0f);
        } else {
            if (intersection.z.toValue(0.5f) >= position.z) {
                mz = -mz;
            }
            correction.set(0.0f, 0.0f, mz);
        }
    }

    public void addItemToHotBar(InventoryTapItem invTapItem) {
        addItemToHotBar(invTapItem, false);
    }

    public void addItemToHotBar(InventoryTapItem invTapItem, boolean toTheEnd) {
        for (int i = 0; i < hotbar.size(); i++) {
            if (hotbar.get(i).getInventoryItem().getSlot() == invTapItem.getInventoryItem().getSlot()) {
                hotbar.remove(i);
                invTapItem.getInventoryItem().isInHotbar = false;
            }
        }
        if (toTheEnd) {
            if (hotbar.isEmpty()) {
                inHand = invTapItem.getInventoryItem();
            }
            hotbar.add(invTapItem);
            invTapItem.getInventoryItem().isInHotbar = true;
            return;
        }
        hotbar.add(0, invTapItem);
        inHand = invTapItem.getInventoryItem();
        invTapItem.getInventoryItem().isInHotbar = true;
    }

    public boolean isHotBarContainsItem(InventoryItem invItem) {
        for (int i = 0; i < hotbar.size(); i++) {
            if (hotbar.get(i).getInventoryItem().getSlot() == invItem.getSlot()) {
                return true;
            }
        }
        return false;
    }

    public short getHealthPoints() {
        return healthPoints;
    }

    public BoundingCuboid getBounds() {
        return playerBounds;
    }

    public boolean justAttacked() {
        return System.currentTimeMillis() - damagedAt < 1000;
    }

    public void attacked(int attackPoints) {
        takeDamage(attackPoints);
    }

    @Override
    public void takeDamage(int healthPoints) {
        if (healthPoints > 0) {
            synchronized (damagedAt) {
                if (damagedAt + TIMEOUT_BETWEEN_DAMAGE < System.currentTimeMillis()) {
                    damagedAt = System.currentTimeMillis();
                    updateHealth(-healthPoints);
                    increaseExhaustionLevel(0.3f);
                    SoundManager.playMaterialSound(Material.HUMAN, 0.0f);
                }
            }
        }
    }

    public long getLastDamagedAt() {
        return damagedAt;
    }

    private void updateHealth(int deltaPoints) {
        if (!isDead()) {
            short newValue = (short) Math.max(0, Math.min(healthPoints + deltaPoints, 20));
            if (healthPoints != newValue) {
                healthPoints = newValue;
                healthUpdatedAt = System.currentTimeMillis();
            }
            if (isDead()) {
                die();
            }
        }
    }

    @Override
    public boolean isDead() {
        return healthPoints <= 0;
    }

    public void respawn() {
        if (inventory != null) {
            inventory.clear();
        }
        if (hotbar != null) {
            hotbar.clear();
        }
        inHand = null;
        reSpawnPosition();
        healthPoints = (short) 20;
        foodLevel = (short) 20;
        foodSaturationLevel = foodLevel;
        foodExhaustionLevel = 0.0f;
        foodTimer = 0L;
    }

    private void die() {
        dropInventoryItems();
        mWorld.showDeathMenu(this);
    }

    private void dropInventoryItems() {
        for (InventoryItem item : inventory.getAllInventoryItems()) {
            mWorld.addDroppableItem(item.getItemID(), position.x, position.y, position.z, item.getCount());
        }
        inventory.clear();
    }

    public boolean isFullInventory() {
        return inventory.getSize() == 32;
    }

    public Chunk getChunk() {
        return mWorld.getChunk(((int) position.x) / 16, ((int) position.z) / 16);
    }

    public int getWeaponDamage() {
        if (inHand != null) {
            return inHand.getDamage();
        }
        return 1;
    }

    public boolean isReadyToEat() {
        return isHungry() && hasFoodInHand();
    }

    private boolean isHungry() {
        return foodLevel < 20;
    }

    private boolean hasFoodInHand() {
        return inHand != null && inHand.isFood();
    }

    public void decActiveItemDurability() {
        if (inHand != null && inHand.isTool()) {
            inHand.decDurability();
            if (inHand.isEmpty()) {
                inventory.remove(inHand);
            }
        }
    }

    public void dropItemFronHotbar(@NonNull InventoryItem inventoryItem) {
        mWorld.addDroppableItem(inventoryItem.getItemID(), position.x, position.y, position.z, inventoryItem.getCount(), true);
    }

    public boolean isHealthJustUpdated() {
        return System.currentTimeMillis() - healthUpdatedAt < 200;
    }

    public World getmWorld() {
        return mWorld;
    }

    public long getmKeptDownAt() {
        return mKeptDownAt;
    }

    public void setmKeptDownAt(long keptDownAt) {
        mKeptDownAt = keptDownAt;
    }

    private void updateFoodLevel() {
        foodExhaustionLevel += exhaustionWalkDistance * 0.01f;
        exhaustionWalkDistance = 0.0f;
        int foodLevelDecrement = (int) (foodExhaustionLevel / MAX_FOOD_EXHAUSTION);
        foodExhaustionLevel -= foodLevelDecrement * MAX_FOOD_EXHAUSTION;
        if (foodSaturationLevel > 0.0f) {
            foodSaturationLevel = Math.max(foodSaturationLevel - foodLevelDecrement, 0.0f);
        } else {
            foodLevel = (short) Math.max(foodLevel - foodLevelDecrement, 0);
        }
        boolean isHealthy = foodLevel >= 17;
        boolean isHungry = foodLevel <= 0 && getHealthPoints() >= 2;
        foodTimer = (isHealthy || isHungry) ? foodTimer + lastAdvanceInterval : 0L;
        int healthInfluence = (int) (foodTimer / 4000);
        foodTimer %= 4000;
        if (healthInfluence > 0) {
            if (isHungry) {
                takeDamage(healthInfluence);
            } else {
                updateHealth(healthInfluence);
            }
        }
    }

    public short getFoodLevel() {
        return foodLevel;
    }

    public void eat() {
        if (!isEatingStarted()) {
            eatingStartedAt = System.currentTimeMillis();
        }
        if (isEatTimeoutElapsed()) {
            Food food = inHand.getFood();
            if (food == null) {
                Log.e("Player", "eat() failed: food is null");
                return;
            }
            foodLevel = (short) Math.min(foodLevel + food.getFoodPoints(), 20);
            foodSaturationLevel = Math.min(foodSaturationLevel + food.getSaturationPoints(), foodLevel);
            eatingStartedAt = 0L;
            inventory.decItem(inHand);
            if (!isHungry()) {
                SoundManager.playDistancedSound(Sounds.BURP, 0.0f);
            }
        }
    }

    private boolean isEatingStarted() {
        return System.currentTimeMillis() - eatingStartedAt <= 4000;
    }

    private boolean isEatTimeoutElapsed() {
        return System.currentTimeMillis() - eatingStartedAt > TIMEOUT_BETWEEN_DAMAGE;
    }

    public void increaseExhaustionLevel(float increment) {
        foodExhaustionLevel += increment;
    }
}
