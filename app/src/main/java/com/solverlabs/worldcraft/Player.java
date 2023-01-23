package com.solverlabs.worldcraft;

import android.util.Log;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.geom.DoorBlock;
import com.solverlabs.droid.rugl.input.TapPad;
import com.solverlabs.droid.rugl.util.FPSCamera;
import com.solverlabs.droid.rugl.util.FloatMath;
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
    private final World world;
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
    /**
     * Высота игрока
     */
    public float height = 1.8f;
    /**
     * Высота глаз
     */
    public float eyeLevel = 1.1f; // 0.9f
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
    private long keptDownAt;
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
        this.world = world;
        resetSavedPosition();
    }

    public void doJump() {
        if (this.crouched) {
            this.crouched = false;
        } else if (this.onGround) {
            this.velocity.y = this.jumpSpeed;
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
            this.spawnPosition.set(x, y, z);
        } catch (Exception e) {
            this.spawnPosition.set(50.0f, 80.0f, 50.0f);
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
                this.inventory.insertItem(invItem);
                if (isInHotbar) {
                    addItemToHotBar(new InventoryTapItem(this, invItem));
                }
            }
        }
    }

    private void saveInventory(@NonNull Tag playerTag) {
        playerTag.removeSubTag(playerTag.findTagByName("Inventory"));
        Tag inventoryTag = new Tag("Inventory", Tag.Type.TAG_Compound);
        for (int i = 0; i < this.inventory.getSize(); i++) {
            InventoryItem element = this.inventory.getElement(i);
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
            this.healthPoints = (Short) healthValue;
        }
    }

    private void saveHealth(@NonNull Tag playerTag) {
        playerTag.saveTagValue(Tag.Type.TAG_Short, PLAYER_HEALTH_TAG, this.healthPoints);
    }

    private void initFoodLevel(@NonNull Tag levelTag) {
        Object foodLevelValue = levelTag.getTagValue(FOOD_LEVEL_TAG);
        Object foodSaturationLevelValue = levelTag.getTagValue(FOOD_SATURATION_LEVEL_TAG);
        Object foodTimerValue = levelTag.getTagValue(FOOD_TIMER_TAG);
        Object foodExhaustionLevelValue = levelTag.getTagValue(FOOD_EXHAUSTION_LEVEL_TAG);
        this.foodLevel = foodLevelValue != null ? (Short) foodLevelValue : (short) 20;
        this.foodSaturationLevel = foodSaturationLevelValue != null ? (Float) foodSaturationLevelValue : this.foodLevel;
        this.foodTimer = foodTimerValue != null ? (Long) foodTimerValue : 0L;
        this.foodExhaustionLevel = foodExhaustionLevelValue != null ? (Float) foodExhaustionLevelValue : 0.0f;
    }

    private void saveFoodLevel(@NonNull Tag playerTag) {
        playerTag.saveTagValue(Tag.Type.TAG_Short, FOOD_LEVEL_TAG, this.foodLevel);
        playerTag.saveTagValue(Tag.Type.TAG_Float, FOOD_SATURATION_LEVEL_TAG, this.foodSaturationLevel);
        playerTag.saveTagValue(Tag.Type.TAG_Long, FOOD_TIMER_TAG, this.foodTimer);
        playerTag.saveTagValue(Tag.Type.TAG_Float, FOOD_EXHAUSTION_LEVEL_TAG, this.foodExhaustionLevel);
    }

    public void setSpawnPosition(Vector3i target) {
        this.spawnPosition.set(target);
    }

    public void reSpawnPosition() {
        this.position.set(this.spawnPosition);
        this.fallDetector.reset(this.position);
        this.velocity.set(0.0f, 0.0f, 0.0f);
    }

    public void resetSavedPosition() {
        this.position.set(this.world.startPosition);
        this.fallDetector.reset(this.position);
        this.velocity.set(0.0f, 0.0f, 0.0f);
    }

    public void setWorldStartPosAsSpawnPos() {
        this.spawnPosition.set(this.world.startPosition);
    }

    public boolean spawnBedExists(int x, int y, int z) {
        return this.spawnPosition.y - 1.0f == ((float) y) && this.spawnPosition.z == ((float) z) && this.spawnPosition.x >= ((float) (x + (-1))) && this.spawnPosition.x <= ((float) (x + 1));
    }

    public void advance(float delta, FPSCamera cam, GUI gui) {
        if (this.world.getChunklet(this.position.x, this.position.y, this.position.z) != null) {
            updateLastAdvanceInterval();
            this.rotation.set(cam.getHeading(), cam.getElevation());
            this.forward.set(cam.forward);
            float s = this.crouched ? this.crouchedSpeed : this.speed;
            float diffX = delta * s * ((gui.left.y * this.forward.x) - (gui.left.x * cam.right.x));
            float diffZ = delta * s * ((gui.left.y * this.forward.z) - (gui.left.x * cam.right.z));
            float nextPosX = this.position.x + diffX;
            float nextPosZ = this.position.z + diffZ;
            if (this.inHand != null && this.inHand.isEmpty()) {
                this.inHand = null;
            }
            float headingAngle = Range.wrap(cam.getHeading(), 0.0f, 6.2831855f);
            setWorldSide(headingAngle);
            if (this.ghost) {
                if (this.world.getChunklet(nextPosX, this.position.y, nextPosZ) != null) {
                    this.position.x = nextPosX;
                    this.position.z = nextPosZ;
                    this.position.y += gui.left.y * delta * cam.forward.y * s;
                    this.position.y += (-gui.left.x) * delta * cam.right.y * s;
                    this.position.y = Range.limit(this.position.y, 1.0f, 127.0f);
                    this.velocity.y = 0.0f;
                    float w = this.width / 2.0f;
                    float feet = this.height * (this.crouched ? this.crouchedEyeLevel : this.eyeLevel);
                    float head = this.height - feet;
                    this.playerBounds.set(this.position.x - w, this.position.y - feet, this.position.z - w, this.position.x + w, this.position.y + head, this.position.z + w);
                    for (float x = FloatMath.floor(this.playerBounds.x.getMin()); x < this.playerBounds.x.getMax(); x += 1.0f) {
                        for (float z = FloatMath.floor(this.playerBounds.z.getMin()); z < this.playerBounds.z.getMax(); z += 1.0f) {
                            for (float y = FloatMath.floor(this.playerBounds.y.getMin()); y < this.playerBounds.y.getMax(); y += 1.0f) {
                                this.collideCorrection.set(0.0f, 0.0f, 0.0f);
                                collide(x, y, z, this.collideCorrection);
                                this.playerBounds.translate(this.collideCorrection.x, this.collideCorrection.y, this.collideCorrection.z);
                                Vector3f.add(this.position, this.collideCorrection, this.position);
                            }
                        }
                    }
                }
            } else if (this.world.getChunklet(nextPosX, this.position.y, nextPosZ) != null) {
                this.forward.y = 0.0f;
                this.forward.normalise();
                this.position.x = nextPosX;
                this.position.z = nextPosZ;
                if (this.world.blockType(this.position.x, this.position.y, this.position.z) == 76) {
                    this.velocity.y = 0.0f;
                    this.position.y += gui.left.y * delta * cam.forward.y * s;
                    this.position.y += (-gui.left.x) * delta * cam.right.y * s;
                } else {
                    this.velocity.y += this.gravity * delta;
                }
                this.position.y += this.velocity.y * delta;
                float w2 = this.width / 2.0f;
                float feet2 = this.height * (this.crouched ? this.crouchedEyeLevel : this.eyeLevel);
                float head2 = this.height - feet2;
                this.playerBounds.set(this.position.x - w2, this.position.y - feet2, this.position.z - w2, this.position.x + w2, this.position.y + head2, this.position.z + w2);
                boolean groundHit = false;
                BlockFactory.Block groundBlock = null;
                for (float x2 = FloatMath.floor(this.playerBounds.x.getMin()); x2 < this.playerBounds.x.getMax(); x2 += 1.0f) {
                    for (float z2 = FloatMath.floor(this.playerBounds.z.getMin()); z2 < this.playerBounds.z.getMax(); z2 += 1.0f) {
                        for (float y2 = FloatMath.floor(this.playerBounds.y.getMin()); y2 < this.playerBounds.y.getMax(); y2 += 1.0f) {
                            this.collideCorrection.set(0.0f, 0.0f, 0.0f);
                            BlockFactory.Block collidedBlock = collide(x2, y2, z2, this.collideCorrection);
                            this.playerBounds.translate(this.collideCorrection.x, this.collideCorrection.y, this.collideCorrection.z);
                            Vector3f.add(this.position, this.collideCorrection, this.position);
                            if (this.collideCorrection.y != 0.0f && Math.signum(this.collideCorrection.y) != Math.signum(this.velocity.y)) {
                                this.velocity.y = 0.0f;
                            }
                            groundHit |= this.collideCorrection.y > 0.0f;
                            if (this.collideCorrection.y > 0.0f) {
                                groundBlock = collidedBlock;
                            }
                        }
                    }
                }
                this.onGround = groundHit;
                if (groundBlock != null) {
                    if (Math.abs(diffX) + Math.abs(diffZ) > 0.0f && System.currentTimeMillis() - this.lastStepNotificationAt > 400) {
                        this.lastStepNotificationAt = System.currentTimeMillis();
                        SoundManager.playStep(groundBlock.material, 0.0f);
                    }
                    this.exhaustionWalkDistance += FloatMath.sqrt((diffX * diffX) + (diffZ * diffZ));
                }
                if (GameMode.isSurvivalMode()) {
                    this.fallDetector.set(this.position, this.onGround);
                    updateFoodLevel();
                }
                this.position.y = Range.limit(this.position.y, 1.0f, 127.0f);
                if (GameMode.isMultiplayerMode() && Multiplayer.instance.movementHandler != null) {
                    Multiplayer.instance.movementHandler.set(this.position.x, this.position.y, this.position.z, cam.forward.x, cam.forward.y, cam.forward.z, cam.up.x, cam.up.y, cam.up.z);
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
                this.currentWorldSide = BlockFactory.WorldSide.North;
                return;
            case 1:
                this.currentWorldSide = BlockFactory.WorldSide.West;
                return;
            case 2:
                this.currentWorldSide = BlockFactory.WorldSide.South;
                return;
            case 3:
                this.currentWorldSide = BlockFactory.WorldSide.East;
                return;
            default:
                return;
        }
    }

    public BlockFactory.WorldSide getCurrentWorldSide() {
        return this.currentWorldSide;
    }

    private void updateLastAdvanceInterval() {
        long currentGameTime = GameTime.getTime();
        if (this.lastAdvanceAt > 0) {
            this.lastAdvanceInterval = currentGameTime - this.lastAdvanceAt;
        }
        this.lastAdvanceAt = currentGameTime;
    }

    public float getAngle() {
        return MathUtils.normalizeAngle(Enemy.getAngle(this.forward.x, this.forward.y, this.forward.z));
    }

    private BlockFactory.Block collide(float x, float y, float z, Vector3f correction) {
        byte upperBlockType;
        byte bt = this.world.blockType(x, y, z);
        if (y < 128.0f) {
            upperBlockType = this.world.blockType(x, y + 1.0f, z);
        } else {
            upperBlockType = 0;
        }
        BlockFactory.Block b = BlockFactory.getBlock(bt);
        if (b != null && b != BlockFactory.Block.Water && b != BlockFactory.Block.StillWater && b.isCuboid) {
            float x2 = FloatMath.floor(x);
            float y2 = FloatMath.floor(y);
            float z2 = FloatMath.floor(z);
            if (DoorBlock.isDoor(b)) {
                DoorBlock.updateBlockBounds(this.blockBounds, x2, y2, z2, this.world);
            } else {
                this.blockBounds.set(x2, y2, z2, x2 + 1.0f, y2 + 1.0f, z2 + 1.0f);
                BlockFactory.Block upperBlock = BlockFactory.getBlock(upperBlockType);
                if (b == BlockFactory.Block.Slab || upperBlockType == 0 || (upperBlock != null && !upperBlock.isCuboid)) {
                    this.blockBounds.y.set(y2, 0.5f + y2);
                }
            }
            if (this.playerBounds.intersection(this.blockBounds, this.intersection)) {
                correction(this.intersection, this.collideCorrection);
            }
        }
        return b;
    }

    private void correction(@NonNull BoundingCuboid intersection, Vector3f correction) {
        float mx = intersection.x.getSpan();
        float my = intersection.y.getSpan();
        float mz = intersection.z.getSpan();
        float midpoint = this.playerBounds.y.toValue(0.5f);
        if (my < 0.51f && intersection.y.toValue(0.5f) < midpoint) {
            correction.set(0.0f, my, 0.0f);
            correction.y *= 0.3f;
        } else if (mx < my && mx < mz) {
            if (intersection.x.toValue(0.5f) >= this.position.x) {
                mx = -mx;
            }
            correction.set(mx, 0.0f, 0.0f);
        } else if (my < mz) {
            if (intersection.y.toValue(0.5f) >= midpoint) {
                my = -my;
            }
            correction.set(0.0f, my, 0.0f);
        } else {
            if (intersection.z.toValue(0.5f) >= this.position.z) {
                mz = -mz;
            }
            correction.set(0.0f, 0.0f, mz);
        }
    }

    public void addItemToHotBar(InventoryTapItem invTapItem) {
        addItemToHotBar(invTapItem, false);
    }

    public void addItemToHotBar(InventoryTapItem invTapItem, boolean toTheEnd) {
        for (int i = 0; i < this.hotbar.size(); i++) {
            if (this.hotbar.get(i).getInventoryItem().getSlot() == invTapItem.getInventoryItem().getSlot()) {
                this.hotbar.remove(i);
                invTapItem.getInventoryItem().isInHotbar = false;
            }
        }
        if (toTheEnd) {
            if (this.hotbar.isEmpty()) {
                this.inHand = invTapItem.getInventoryItem();
            }
            this.hotbar.add(invTapItem);
            invTapItem.getInventoryItem().isInHotbar = true;
            return;
        }
        this.hotbar.add(0, invTapItem);
        this.inHand = invTapItem.getInventoryItem();
        invTapItem.getInventoryItem().isInHotbar = true;
    }

    public boolean isHotBarContainsItem(InventoryItem invItem) {
        for (int i = 0; i < this.hotbar.size(); i++) {
            if (this.hotbar.get(i).getInventoryItem().getSlot() == invItem.getSlot()) {
                return true;
            }
        }
        return false;
    }

    public short getHealthPoints() {
        return this.healthPoints;
    }

    public BoundingCuboid getBounds() {
        return this.playerBounds;
    }

    public boolean justAttacked() {
        return System.currentTimeMillis() - this.damagedAt < 1000;
    }

    public void attacked(int attackPoints) {
        takeDamage(attackPoints);
    }

    @Override
    public void takeDamage(int healthPoints) {
        if (healthPoints > 0) {
            synchronized (this.damagedAt) {
                if (this.damagedAt + TIMEOUT_BETWEEN_DAMAGE < System.currentTimeMillis()) {
                    this.damagedAt = System.currentTimeMillis();
                    updateHealth(-healthPoints);
                    increaseExhaustionLevel(0.3f);
                    SoundManager.playMaterialSound(Material.HUMAN, 0.0f);
                }
            }
        }
    }

    public long getLastDamagedAt() {
        return this.damagedAt;
    }

    private void updateHealth(int deltaPoints) {
        if (!isDead()) {
            short newValue = (short) Math.max(0, Math.min(this.healthPoints + deltaPoints, 20));
            if (this.healthPoints != newValue) {
                this.healthPoints = newValue;
                this.healthUpdatedAt = System.currentTimeMillis();
            }
            if (isDead()) {
                die();
            }
        }
    }

    @Override
    public boolean isDead() {
        return this.healthPoints <= 0;
    }

    public void respawn() {
        if (this.inventory != null) {
            this.inventory.clear();
        }
        if (this.hotbar != null) {
            this.hotbar.clear();
        }
        this.inHand = null;
        reSpawnPosition();
        this.healthPoints = (short) 20;
        this.foodLevel = (short) 20;
        this.foodSaturationLevel = this.foodLevel;
        this.foodExhaustionLevel = 0.0f;
        this.foodTimer = 0L;
    }

    private void die() {
        dropInventoryItems();
        this.world.showDeathMenu(this);
    }

    private void dropInventoryItems() {
        for (InventoryItem item : this.inventory.getAllInventoryItems()) {
            this.world.addDroppableItem(item.getItemID(), this.position.x, this.position.y, this.position.z, item.getCount());
        }
        this.inventory.clear();
    }

    public boolean isFullInventory() {
        return this.inventory.getSize() == 32;
    }

    public Chunk getChunk() {
        return this.world.getChunk(((int) this.position.x) / 16, ((int) this.position.z) / 16);
    }

    public int getWeaponDamage() {
        if (this.inHand != null) {
            return this.inHand.getDamage();
        }
        return 1;
    }

    public boolean isReadyToEat() {
        return isHungry() && hasFoodInHand();
    }

    private boolean isHungry() {
        return this.foodLevel < 20;
    }

    private boolean hasFoodInHand() {
        return this.inHand != null && this.inHand.isFood();
    }

    public void decActiveItemDurability() {
        if (this.inHand != null && this.inHand.isTool()) {
            this.inHand.decDurability();
            if (this.inHand.isEmpty()) {
                this.inventory.remove(this.inHand);
            }
        }
    }

    public void dropItemFronHotbar(@NonNull InventoryItem inventoryItem) {
        this.world.addDroppableItem(inventoryItem.getItemID(), this.position.x, this.position.y, this.position.z, inventoryItem.getCount(), true);
    }

    public boolean isHealthJustUpdated() {
        return System.currentTimeMillis() - this.healthUpdatedAt < 200;
    }

    public World getWorld() {
        return this.world;
    }

    public long getKeptDownAt() {
        return this.keptDownAt;
    }

    public void setKeptDownAt(long keptDownAt) {
        this.keptDownAt = keptDownAt;
    }

    private void updateFoodLevel() {
        this.foodExhaustionLevel += this.exhaustionWalkDistance * 0.01f;
        this.exhaustionWalkDistance = 0.0f;
        int foodLevelDecrement = (int) (this.foodExhaustionLevel / MAX_FOOD_EXHAUSTION);
        this.foodExhaustionLevel -= foodLevelDecrement * MAX_FOOD_EXHAUSTION;
        if (this.foodSaturationLevel > 0.0f) {
            this.foodSaturationLevel = Math.max(this.foodSaturationLevel - foodLevelDecrement, 0.0f);
        } else {
            this.foodLevel = (short) Math.max(this.foodLevel - foodLevelDecrement, 0);
        }
        boolean isHealthy = this.foodLevel >= 17;
        boolean isHungry = this.foodLevel <= 0 && getHealthPoints() >= 2;
        this.foodTimer = (isHealthy || isHungry) ? this.foodTimer + this.lastAdvanceInterval : 0L;
        int healthInfluence = (int) (this.foodTimer / 4000);
        this.foodTimer %= 4000;
        if (healthInfluence > 0) {
            if (isHungry) {
                takeDamage(healthInfluence);
            } else {
                updateHealth(healthInfluence);
            }
        }
    }

    public short getFoodLevel() {
        return this.foodLevel;
    }

    public void eat() {
        if (!isEatingStarted()) {
            this.eatingStartedAt = System.currentTimeMillis();
        }
        if (isEatTimeoutElapsed()) {
            Food food = this.inHand.getFood();
            if (food == null) {
                Log.e("Player", "eat() failed: food is null");
                return;
            }
            this.foodLevel = (short) Math.min(this.foodLevel + food.getFoodPoints(), 20);
            this.foodSaturationLevel = Math.min(this.foodSaturationLevel + food.getSaturationPoints(), this.foodLevel);
            this.eatingStartedAt = 0L;
            this.inventory.decItem(this.inHand);
            if (!isHungry()) {
                SoundManager.playDistancedSound(Sounds.BURP, 0.0f);
            }
        }
    }

    private boolean isEatingStarted() {
        return System.currentTimeMillis() - this.eatingStartedAt <= 4000;
    }

    private boolean isEatTimeoutElapsed() {
        return System.currentTimeMillis() - this.eatingStartedAt > TIMEOUT_BETWEEN_DAMAGE;
    }

    public void increaseExhaustionLevel(float increment) {
        this.foodExhaustionLevel += increment;
    }
}
