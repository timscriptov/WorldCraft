package com.solverlabs.worldcraft.mob;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.geom.DoorBlock;
import com.solverlabs.droid.rugl.util.FloatMath;
import com.solverlabs.droid.rugl.util.geom.BoundingCuboid;
import com.solverlabs.droid.rugl.util.geom.Vector3f;
import com.solverlabs.droid.rugl.util.math.Range;
import com.solverlabs.worldcraft.World;
import com.solverlabs.worldcraft.factories.BlockFactory;

import org.jetbrains.annotations.Contract;

public class MobCollider {
    private static final Vector3f collideCorrection = new Vector3f();
    private static final BoundingCuboid blockBounds = new BoundingCuboid(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
    private static final BoundingCuboid intersection = new BoundingCuboid(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);

    public static boolean isCollided(@NonNull BoundingCuboid objectBounds, World world) {
        for (float x = FloatMath.floor(objectBounds.x.getMin()); x < objectBounds.x.getMax(); x += 1.0f) {
            for (float z = FloatMath.floor(objectBounds.z.getMin()); z < objectBounds.z.getMax(); z += 1.0f) {
                for (float y = FloatMath.floor(objectBounds.y.getMin()); y < objectBounds.y.getMax(); y += 1.0f) {
                    collideCorrection.set(0.0f, 0.0f, 0.0f);
                    if (isIntersects(x, y, z, objectBounds, world)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean collideWithAnotherMob(Mob mob) {
        if (MobFactory.intersection(mob, intersection)) {
            BoundingCuboid mobBounds = mob.getBounds();
            Vector3f mobPosition = mob.getPosition();
            collideCorrection.set(0.0f, 0.0f, 0.0f);
            correction(intersection, collideCorrection, mobBounds, mobPosition, null);
            mobBounds.translate(collideCorrection.x, collideCorrection.y, collideCorrection.z);
            Vector3f.add(mobPosition, collideCorrection, mobPosition);
            return true;
        }
        return false;
    }

    public static boolean collidePlayerAndMob(BoundingCuboid playerBounds, Mob mob) {
        if (MobFactory.intersectionBounds(mob, playerBounds, intersection)) {
            collideCorrection.set(0.0f, 0.0f, 0.0f);
            correction(intersection, collideCorrection, playerBounds, mob.getPosition(), null);
            playerBounds.translate(collideCorrection.x, collideCorrection.y, collideCorrection.z);
            Vector3f.add(mob.getPosition(), collideCorrection, mob.getPosition());
            return true;
        }
        return false;
    }

    public static boolean collideWithBlock(Mob mob, @NonNull BoundingCuboid objectBounds, Vector3f position, Vector3f velocity, World world) {
        Vector3f originalPosition = new Vector3f(position);
        for (float x = FloatMath.floor(objectBounds.x.getMin()); x < objectBounds.x.getMax(); x += 1.0f) {
            for (float z = FloatMath.floor(objectBounds.z.getMin()); z < objectBounds.z.getMax(); z += 1.0f) {
                for (float y = FloatMath.floor(objectBounds.y.getMin()); y < objectBounds.y.getMax(); y += 1.0f) {
                    collideCorrection.set(0.0f, 0.0f, 0.0f);
                    collide(x, y, z, objectBounds, position, velocity, world);
                    objectBounds.translate(collideCorrection.x, collideCorrection.y, collideCorrection.z);
                    Vector3f.add(position, collideCorrection, position);
                    if (collideCorrection.y != 0.0f && Math.signum(collideCorrection.y) != Math.signum(velocity.y)) {
                        velocity.y = 0.0f;
                    }
                }
            }
        }
        position.y = Range.limit(position.y, 1.0f, 127.0f);
        return !originalPosition.equals(position) && !isFalling(originalPosition, position);
    }

    @Contract(pure = true)
    private static boolean isFalling(@NonNull Vector3f originalPosition, @NonNull Vector3f position) {
        return originalPosition.y > position.y;
    }

    private static boolean isIntersects(float x, float y, float z, BoundingCuboid objectBounds, @NonNull World world) {
        byte upperBlockType;
        byte bt = world.blockType(x, y, z);
        if (y < 128.0f) {
            upperBlockType = world.blockType(x, y + 1.0f, z);
        } else {
            upperBlockType = 0;
        }
        BlockFactory.Block b = BlockFactory.getBlock(bt);
        if (b != null && b != BlockFactory.Block.Water && b != BlockFactory.Block.StillWater && b != BlockFactory.Block.Torch && b != BlockFactory.Block.Grass) {
            float x2 = FloatMath.floor(x);
            float y2 = FloatMath.floor(y);
            float z2 = FloatMath.floor(z);
            if (DoorBlock.isDoor(b)) {
                DoorBlock.updateBlockBounds(blockBounds, x2, y2, z2, world);
            } else {
                blockBounds.set(x2, y2, z2, x2 + 1.0f, y2 + 1.0f, z2 + 1.0f);
                BlockFactory.Block upperBlock = BlockFactory.getBlock(upperBlockType);
                if (b == BlockFactory.Block.Slab || upperBlockType == 0 || (upperBlock != null && !upperBlock.isCuboid)) {
                    blockBounds.y.set(y2, 0.5f + y2);
                }
            }
            return objectBounds.intersection(blockBounds, intersection);
        }
        return false;
    }

    private static boolean collide(float x, float y, float z, BoundingCuboid objectBounds, Vector3f position, Vector3f velocity, World world) {
        if (isIntersects(x, y, z, objectBounds, world)) {
            correction(intersection, collideCorrection, objectBounds, position, velocity);
            return true;
        }
        return false;
    }

    private static void correction(@NonNull BoundingCuboid intersection2, Vector3f correction, @NonNull BoundingCuboid objectBounds, Vector3f position, Vector3f velocity) {
        float mx = intersection2.x.getSpan();
        float my = intersection2.y.getSpan();
        float mz = intersection2.z.getSpan();
        float midpoint = objectBounds.y.toValue(0.5f);
        if (my >= 0.51f || intersection2.y.toValue(0.5f) >= midpoint) {
            if (mx < my && mx < mz) {
                if (intersection2.x.toValue(0.5f) >= position.x) {
                    mx = -mx;
                }
                correction.set(mx, 0.0f, 0.0f);
            } else if (my < mz) {
                correction.set(0.0f, 0.0f, 0.0f);
            } else {
                if (intersection2.z.toValue(0.5f) >= position.z) {
                    mz = -mz;
                }
                correction.set(0.0f, 0.0f, mz);
            }
        }
    }
}
