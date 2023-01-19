package com.solverlabs.worldcraft.mob;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.util.FPSCamera;
import com.solverlabs.droid.rugl.util.geom.BoundingCuboid;
import com.solverlabs.droid.rugl.util.geom.Frustum;
import com.solverlabs.droid.rugl.util.geom.Vector3f;
import com.solverlabs.droid.rugl.util.math.Range;
import com.solverlabs.worldcraft.Player;
import com.solverlabs.worldcraft.SoundManager;
import com.solverlabs.worldcraft.World;
import com.solverlabs.worldcraft.chunk.Chunk;
import com.solverlabs.worldcraft.chunk.entity.MobEntity;
import com.solverlabs.worldcraft.domain.Damagable;
import com.solverlabs.worldcraft.factories.BlockFactory;
import com.solverlabs.worldcraft.material.Material;
import com.solverlabs.worldcraft.math.MathUtils;
import com.solverlabs.worldcraft.util.Distance;
import com.solverlabs.worldcraft.util.FallDetector;
import com.solverlabs.worldcraft.util.RandomUtil;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.vecmath.Point3d;

import android2.util.FloatMath;


public abstract class Mob implements Damagable {
    public static final byte AGGRESSION_TYPE_HOSTILE = 2;
    public static final byte AGGRESSION_TYPE_PASSIVE = 1;
    public static final int FAR_AWAY_DISTANCE = 1000;
    private static final float DEFAULT_ATTACK_DISTANCE = 3.0f;
    private static final long DEFAULT_DYING_TIMEOUT = 2500;
    private static final float DEFAULT_JUMP_SPEED = 5.0f;
    private static final int GRAVITY = -10;
    private static final long IS_RECENTLY_ATTACKED_TIMEOUT = 800;
    private static final int MAX_TALK_INTERVAL = 30000;
    private static final int MIN_TALK_INTERVAL = 5000;
    private static final int STATE_ATTACKED = 3;
    private static final int STATE_NORMAL = 1;
    private static final int STATE_SELECTED = 2;
    private static final long SUNLIGHT_DAMAGE_TIMEOUT = 1000;
    private static final Vector3f ZERO_VELOCITY_VECTOR = new Vector3f(0.0f, 0.0f, 0.0f);
    private static final AtomicInteger nextId = new AtomicInteger();
    private final BoundingCuboid bounds;
    private final FallDetector fallDetector;
    private final Vector3f outerVelocityVector;
    private final BoundingCuboid smallBounds;
    private final Vector3f velocityVector;
    protected Byte downBlock;
    protected short healthPoints;
    protected MobSize mobSize;
    private byte aggressionType;
    private float angle;
    private boolean currVisible;
    private long damagedAt;
    private float distance;
    private int id;
    private MobInteractionListener interactionListener;
    private boolean isRunning;
    private boolean isSelected;
    private boolean isStateChanged;
    private long killedAt;
    private long lastAttackAt;
    private long lastJumpAt;
    private long lastSunlightDamageAt;
    private float light;
    private long nextTalkAt;
    private long outerVelocityVectorEndsAt;
    private Vector3f position;
    private boolean positionChanged;
    private boolean prevAttackedRecently;
    private boolean prevIsDying;
    private boolean prevIsSelected;
    private boolean prevIsVisible;
    private boolean prevVisible;
    private Object tag;
    private float targetAngle;
    private float velocity;
    private World world;

    public Mob(Vector3f position) {
        this();
        this.position.set(position);
    }

    public Mob() {
        this.position = new Vector3f();
        this.velocityVector = new Vector3f();
        this.outerVelocityVector = new Vector3f();
        this.targetAngle = 0.0f;
        this.aggressionType = (byte) 1;
        this.bounds = new BoundingCuboid(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
        this.smallBounds = new BoundingCuboid(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
        this.id = nextId.incrementAndGet();
        this.fallDetector = new FallDetector(this);
        onTalk();
    }

    public abstract Material getMaterial();

    public abstract float getQuietVelocity();

    public abstract float getRunVelocity();

    public abstract String getSaveId();

    public void advance(float delta, World world, FPSCamera cam, Player player) {
        this.world = world;
        if (this.position.x == 0.0f) {
            this.position.x = player.position.x + 1.0f;
            this.position.y = player.position.y;
            this.position.z = player.position.z + 1.0f;
        }
        if (this.prevIsSelected != this.isSelected || this.prevAttackedRecently != isAttackedRecently() || this.prevIsDying != isDying()) {
            this.isStateChanged = true;
        } else {
            this.isStateChanged = false;
        }
        if (isDead()) {
            resetVelocity();
            this.interactionListener.dead(this);
        }
        this.fallDetector.set(this.position);
        this.prevAttackedRecently = isAttackedRecently();
        this.prevIsSelected = this.isSelected;
        this.prevIsDying = isDying();
        this.prevIsVisible = isVisible();
        advancePosition(delta, player);
        updateDistance(player);
        updateVisibility(world.loadradius, cam);
        if (this.prevIsVisible != isVisible()) {
            this.isStateChanged = true;
        }
    }

    private void resetVelocity() {
        this.velocity = 0.0f;
        this.velocityVector.x = 0.0f;
        this.velocityVector.z = 0.0f;
    }

    public void advancePosition(float delta, Player player) {
        this.positionChanged = false;
        Vector3f prevPosition = new Vector3f(this.position);
        updatePosition(delta);
        updateLight();
        if (!prevPosition.equals(this.position)) {
            this.positionChanged = true;
            updateBounds();
            if (!isDying() && this.interactionListener != null && MobCollider.collideWithAnotherMob(this)) {
                this.interactionListener.collisionWithMob(this);
            }
            if (!isDying() && this.interactionListener != null && MobCollider.collideWithBlock(this, getBounds(), this.position, this.velocityVector, this.world)) {
                this.interactionListener.collisionWithBlock(this, isJumpPossible());
            }
            if (!isDying() && this.interactionListener != null && MobCollider.collidePlayerAndMob(player.getBounds(), this)) {
                this.interactionListener.collisionWithPlayer(this, player);
            }
        }
        if (!this.prevIsVisible && isVisible()) {
            this.positionChanged = true;
        }
    }

    public void updateLight() {
        float prevLight = this.light;
        if (this.world != null) {
            this.light = Math.max(this.world.blockLight(this.position.x + (getSize().getMaxSize() / 2.0f), this.position.y + 1.0f, this.position.z + (getSize().getMaxSize() / 2.0f)), this.world.blockLight(this.position.x - (getSize().getMaxSize() / 2.0f), this.position.y + 1.0f, this.position.z - (getSize().getMaxSize() / 2.0f)));
        } else {
            this.light = 0.0f;
        }
        if (Float.compare(prevLight, this.light) != 0) {
            this.isStateChanged = true;
        }
        if (!isDying() && isAfraidSunlight() && isUnderSunLight()) {
            takeSunlightDamage();
        }
    }

    private void takeSunlightDamage() {
        if (System.currentTimeMillis() - this.lastSunlightDamageAt > 1000) {
            this.lastSunlightDamageAt = System.currentTimeMillis();
            takeDamage(1);
        }
    }

    private boolean isUnderSunLight() {
        return this.light == 1.0f;
    }

    public float getLight() {
        return this.light;
    }

    public void setLight(float light) {
        this.light = light;
    }

    public void stop() {
        setRunning(false);
        setVelocity(0.0f);
    }

    public void move() {
        setRunning(false);
        setVelocity(getQuietVelocity());
    }

    public void run() {
        setRunning(true);
        setVelocity(getRunVelocity());
    }

    public int getDamagePower() {
        return 0;
    }

    private boolean isJumpPossible() {
        Vector3f velocityVector = MathUtils.getVelocityVector(this.angle, (this.mobSize.getMaxSize() / 2.0f) + 0.25f);
        if (System.currentTimeMillis() - this.lastJumpAt > 2000 && this.world.blockType((int) this.position.x, ((int) this.position.y) + 2, (int) this.position.z) == 0 && this.world.blockType((int) (this.position.x + velocityVector.x), (int) (this.position.y + 2.0f), (int) (this.position.z + velocityVector.z)) == 0) {
            this.lastJumpAt = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    private void updateBounds() {
        updateBound();
        updateSmallBound();
    }

    private void updateSmallBound() {
        float halfHeight = this.mobSize.getHeight() * 0.4f;
        float halfWidth = this.mobSize.getMaxSize() / 4.0f;
        this.smallBounds.set(this.position.x - halfWidth, this.position.y - halfHeight, this.position.z - halfWidth, this.position.x + halfWidth, this.position.y + halfHeight, this.position.z + halfWidth);
    }

    private void updateBound() {
        float halfHeight = this.mobSize.getHeight() * 0.7f;
        float halfWidth = this.mobSize.getMaxSize() / 2.0f;
        float frontHalfWidth = halfWidth * 1.2f;
        float backHalfWidth = halfWidth * 0.8f;
        this.bounds.set(this.position.x - frontHalfWidth, this.position.y - halfHeight, this.position.z - frontHalfWidth, this.position.x + backHalfWidth, this.position.y + halfHeight, this.position.z + backHalfWidth);
    }

    public BoundingCuboid getBounds() {
        return this.bounds;
    }

    public void setAngle(float angle, boolean animateRotation) {
        if (!isFrozen()) {
            setTargetAngle(angle);
            if (!animateRotation) {
                this.angle = angle;
            }
            setVelocity(this.velocity);
        }
    }

    private void setTargetAngle(float angle) {
        this.targetAngle = angle;
        if (Math.abs(this.targetAngle - this.angle) > 3.141592653589793d) {
            if (Float.compare(this.angle, this.targetAngle) == -1) {
                this.angle = (float) (this.angle + 6.283185307179586d);
            } else {
                this.angle = (float) (this.angle - 6.283185307179586d);
            }
        }
    }

    public void shift(float angle, float distance) {
        Vector3f velocityVector = MathUtils.getVelocityVector(angle, distance);
        this.outerVelocityVector.x = velocityVector.x * 4.0f;
        this.outerVelocityVector.z = velocityVector.z * 4.0f;
        this.outerVelocityVectorEndsAt = System.currentTimeMillis() + 300;
        lowJump();
    }

    public boolean isOnRay(float x, float y, float z, float directionX, float directionY, float directionZ) {
        if (!isDying() && this.mobSize != null) {
            float distance = getDistanceToSegment(x, y, z, directionX, directionY, directionZ);
            float mobMaxSize = this.mobSize.getMaxSize() / 2.0f;
            if (Float.compare(distance, mobMaxSize) == -1) {
                return true;
            }
        }
        return false;
    }

    public float distance(float x, float y, float z) {
        return FloatMath.sqrt((float) (Math.pow(this.position.x - x, 2.0d) + Math.pow(this.position.y - y, 2.0d) + Math.pow(this.position.z - z, 2.0d)));
    }

    public boolean intersects(@NonNull Mob mob) {
        return getBounds().intersects(mob.getBounds());
    }

    public boolean intersection(@NonNull Mob mob, BoundingCuboid intersection) {
        return intersection(mob.getBounds(), intersection);
    }

    public boolean intersection(BoundingCuboid bounds, BoundingCuboid intersection) {
        return getBounds().intersection(bounds, intersection);
    }

    public void setInteractionListener(MobInteractionListener moveListener) {
        this.interactionListener = moveListener;
    }

    private boolean isVisible(int worldLoadRadius, FPSCamera cam) {
        return isRenderable(worldLoadRadius) && isInFrustrum(cam);
    }

    public boolean isWalking() {
        return !isDying() && !(this.velocityVector.x == 0.0f && this.velocityVector.z == 0.0f && this.velocityVector.y == 0.0f && Float.compare(this.angle, this.targetAngle) == 0);
    }

    public void tryAttack(Player player) {
        if (!isDying() && distance(player.position.x, player.position.y, player.position.z) < DEFAULT_ATTACK_DISTANCE) {
            mobAttacked(player);
        }
    }

    public boolean isVisible() {
        return this.currVisible;
    }

    public boolean isPositionChanged() {
        return this.positionChanged;
    }

    public boolean isStateChanged() {
        return this.isStateChanged;
    }

    public void setStateChanged() {
        this.isStateChanged = true;
    }

    public int getState() {
        if (isAttackedRecently()) {
            return 3;
        }
        if (this.isSelected) {
        }
        return 1;
    }

    public boolean isDying() {
        return isDyingTimeout() || isDead();
    }

    public boolean isAttackedRecently() {
        return System.currentTimeMillis() - IS_RECENTLY_ATTACKED_TIMEOUT < this.damagedAt;
    }

    public HashMap<Byte, Integer> getDeathDrops() {
        return null;
    }

    public void jump() {
        this.velocityVector.y = DEFAULT_JUMP_SPEED;
    }

    public void lowJump() {
        this.velocityVector.y = 2.5f;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Vector3f getPosition() {
        return this.position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public float getAngle() {
        return this.angle;
    }

    public void setAngle(float angle) {
        setAngle(angle, true);
    }

    public Vector3f getVelocity() {
        return this.velocityVector;
    }

    public void setVelocity(float velocity) {
        if (!isFrozen()) {
            this.velocity = velocity;
            Vector3f velocityVector = MathUtils.getVelocityVector(this.targetAngle, this.velocity);
            this.velocityVector.x = velocityVector.x;
            this.velocityVector.z = velocityVector.z;
        }
    }

    public Object getTag() {
        return this.tag;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }

    public MobSize getSize() {
        return this.mobSize;
    }

    public boolean isSelected() {
        return this.isSelected;
    }

    public void setSelected(boolean value) {
        this.isSelected = false;
    }

    public float getDistance() {
        return this.distance;
    }

    public boolean useSecondTexture() {
        return false;
    }

    public boolean isHandsMoving() {
        return true;
    }

    public boolean equals(Object o) {
        return (o instanceof Mob) && this.id == ((Mob) o).id;
    }

    public boolean isHostile() {
        return this.aggressionType == 2;
    }

    public boolean isPassive() {
        return this.aggressionType == 1;
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    public short getHealthPoints() {
        return this.healthPoints;
    }

    public void setHealthPoints(short healthPoints) {
        this.healthPoints = healthPoints;
    }

    public void attack(Player player) {
        if (isAttackAvailable()) {
            this.lastAttackAt = System.currentTimeMillis();
            player.attacked(getDamagePower());
        }
    }

    public MobEntity getEntity() {
        MobEntity mobEntity = createMobEntity();
        mobEntity.setPosition(this.position);
        mobEntity.setMotion(this.velocityVector);
        mobEntity.setYaw(this.angle);
        mobEntity.setPitch(0.0f);
        mobEntity.setHealth(this.healthPoints);
        return mobEntity;
    }

    protected MobEntity createMobEntity() {
        return new MobEntity(getSaveId());
    }

    public boolean isAfraidSunlight() {
        return false;
    }

    public int hashCode() {
        return this.id;
    }

    @NonNull
    public String toString() {
        return getClass().getSimpleName() + " id: " + this.id;
    }

    protected byte getAggressionType() {
        return this.aggressionType;
    }

    public void setAggressionType(byte type) {
        this.aggressionType = type;
    }

    protected long getAttackTimeout() {
        return 0L;
    }

    private void updatePosition(float delta) {
        Vector3f curVelocity = getCurrentVelocityVector();
        if (canFallDown() || this.velocityVector.y > 0.0f) {
            this.velocityVector.y += (-10.0f) * delta;
            this.position.y += this.velocityVector.y * delta;
            this.position.y = Range.limit(this.position.y, 1.0f, 127.0f);
            this.position.x += curVelocity.x * delta;
            this.position.z += curVelocity.z * delta;
            updateAngle(delta);
        } else if (!isDying()) {
            if (Float.compare(this.angle, this.targetAngle) == 0) {
                this.position.x += curVelocity.x * delta;
                this.position.z += curVelocity.z * delta;
            } else {
                updateAngle(delta);
            }
            this.velocityVector.y = 0.0f;
        } else if (isDying() && Float.compare(this.angle, this.targetAngle) != 0) {
            updateAngle(delta);
            this.positionChanged = true;
        }
    }

    private Vector3f getCurrentVelocityVector() {
        if (this.outerVelocityVectorEndsAt > System.currentTimeMillis()) {
            return this.outerVelocityVector;
        }
        if (!isDying()) {
            return this.velocityVector;
        }
        return ZERO_VELOCITY_VECTOR;
    }

    private void updateAngle(float delta) {
        if (Float.compare(this.angle, this.targetAngle) == -1) {
            this.angle = (float) (this.angle + (getRotationSpeed() * delta));
            if (Float.compare(this.angle, 6.2831855f) == 1) {
                this.angle = 0.0f;
            }
            if (Float.compare(this.angle, this.targetAngle) == 1) {
                this.angle = this.targetAngle;
            }
        } else if (Float.compare(this.angle, this.targetAngle) == 1) {
            this.angle = (float) (this.angle - (getRotationSpeed() * delta));
            if (Float.compare(this.angle, this.targetAngle) == -1) {
                this.angle = this.targetAngle;
            }
        }
    }

    private double getRotationSpeed() {
        return isDying() ? 6.283185307179586d : 1.5707963267948966d;
    }

    protected void mobAttacked(@NonNull Player player) {
        player.decActiveItemDurability();
        player.increaseExhaustionLevel(0.3f);
        if (this.interactionListener != null) {
            this.interactionListener.mobAttacked(this, player);
        }
    }

    private boolean isDyingTimeout() {
        return System.currentTimeMillis() - DEFAULT_DYING_TIMEOUT < this.killedAt;
    }

    @Override
    public boolean isDead() {
        return this.healthPoints <= 0 && !isDyingTimeout();
    }

    private boolean isAttackAvailable() {
        return isHostile() && isAttackTimeoutElapsed() && !isDead();
    }

    private boolean isAttackTimeoutElapsed() {
        return this.lastAttackAt + getAttackTimeout() < System.currentTimeMillis();
    }

    public Chunk getChunk(@NonNull World world2) {
        return world2.getChunk(((int) this.position.x) / 16, ((int) this.position.z) / 16);
    }

    public int getChunkX() {
        return (int) FloatMath.floor(this.position.x / 16.0f);
    }

    public int getChunkZ() {
        return (int) FloatMath.floor(this.position.x / 16.0f);
    }

    @Override
    public void takeDamage(int damage) {
        this.damagedAt = System.currentTimeMillis();
        this.healthPoints = (short) (this.healthPoints - damage);
        if (this.healthPoints <= 0) {
            this.killedAt = System.currentTimeMillis();
            setTargetAngle(0.0f);
            jump();
        }
    }

    private boolean isFrozen() {
        return isDying();
    }

    private BoundingCuboid getSmallBounds() {
        return this.smallBounds;
    }

    private boolean canFallDown() {
        this.downBlock = this.world.getBlockTypeAbsolute((int) this.position.x, (int) this.position.y, (int) this.position.z);
        if (this.downBlock != null) {
            return (this.downBlock == 0 || this.downBlock == 8 || this.downBlock == 9) && !MobCollider.isCollided(getSmallBounds(), this.world);
        }
        return false;
    }

    private void updateVisibility(int worldLoadRadius, FPSCamera cam) {
        this.prevVisible = this.currVisible;
        this.currVisible = isVisible(worldLoadRadius, cam);
    }

    private boolean isInFrustrum(FPSCamera cam) {
        try {
            return cam.getFrustum().sphereIntersects(this.position.x, this.position.y, this.position.z, 1.0f) != Frustum.Result.Miss;
        } catch (NullPointerException e) {
            return false;
        }
    }

    private void updateDistance(Player player) {
        if (player != null) {
            this.distance = Distance.getDistanceBetweenPoints(this.position, player.position, 1000.0f);
        } else {
            this.distance = 1000.0f;
        }
    }

    private boolean isRenderable(int loadradius) {
        float visibleRadius = Math.min(loadradius << 4, BlockFactory.state.fog.end);
        return this.distance < visibleRadius;
    }

    private boolean visibilityChanged() {
        return this.prevVisible != this.currVisible;
    }

    private float getDistanceToSegment(float x, float y, float z, float directionX, float directionY, float directionZ) {
        Point3d segstart = new Point3d(x, y, z);
        Point3d segend = new Point3d(x + directionX, y + directionY, z + directionZ);
        Point3d pt = new Point3d(this.position.x, this.position.y + (getSize().getHeight() / 2.0f), this.position.z);
        return (float) Distance.pointToSegment(pt, segstart, segend);
    }

    public void talk() {
        SoundManager.playMaterialSound(getMaterial(), getDistance());
        onTalk();
    }

    public void onTalk() {
        this.nextTalkAt = System.currentTimeMillis() + getNextTalkInterval();
    }

    protected long getNextTalkInterval() {
        return RandomUtil.getRandomInRangeInclusive(5000, MAX_TALK_INTERVAL);
    }

    public boolean hasNeedToTalk() {
        return System.currentTimeMillis() > this.nextTalkAt && getHealthPoints() > 0;
    }


    public interface MobInteractionListener {
        void collisionWithBlock(Mob mob, boolean z);

        void collisionWithMob(Mob mob);

        void collisionWithPlayer(Mob mob, Player player);

        void dead(Mob mob);

        void mobAttacked(Mob mob, Player player);
    }
}
