package com.solverlabs.worldcraft.etc;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.util.geom.Vector3f;
import com.solverlabs.droid.rugl.util.geom.Vector3i;
import com.solverlabs.droid.rugl.util.math.Range;
import com.solverlabs.worldcraft.GameMode;
import com.solverlabs.worldcraft.SoundManager;
import com.solverlabs.worldcraft.World;
import com.solverlabs.worldcraft.blockentity.TNTBlock;
import com.solverlabs.worldcraft.chunk.Chunklet;
import com.solverlabs.worldcraft.chunk.GeometryGenerator;
import com.solverlabs.worldcraft.domain.Damagable;
import com.solverlabs.worldcraft.factories.BlockFactory;
import com.solverlabs.worldcraft.material.Material;
import com.solverlabs.worldcraft.mob.Mob;
import com.solverlabs.worldcraft.mob.MobFactory;
import com.solverlabs.worldcraft.util.Distance;
import com.solverlabs.worldcraft.util.RandomUtil;

import org.jetbrains.annotations.Contract;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Explosion {
    private static final float BLAST_DECREASED_PER_STEP = 0.22500001f;
    private static final float EXPLOSION_STEP = 0.3f;
    private static final float MAX_BLAST_FACTOR = 1.3f;
    private static final float MIN_BLAST_FACTOR = 0.7f;
    private static Set<Vector3f> RAY_VECTORS = null;
    private static final float STEP = 0.125f;
    private static final int STEP_COUNT_PER_AXIS = 16;

    public enum CoordOrder {
        XYZ,
        YXZ,
        ZXY
    }

    static {
        initRayVectors();
    }

    public static void explode(final World world, final Vector3i position, final float power) {
        GeometryGenerator.addTask(() -> Explosion.explode(world, new Vector3f(position), power, null));
    }

    public static void explode(final World world, final Vector3i position, final float power, final TNTBlock tnt) {
        GeometryGenerator.addTask(() -> Explosion.explode(world, new Vector3f(position), power, tnt));
    }

    public static void explode(World world, Vector3f position, float power, TNTBlock tnt) {
        int explodeRadius = getExplodeRadius(power);
        Vector3f normalizedPosition = getNormalizedExplosionPosition(position);
        ExplosionCube explosionCube = new ExplosionCube(world, normalizedPosition, explodeRadius);
        world.addBlockParticle(BlockFactory.TNT_ID, new Vector3f(position), BlockFactory.WorldSide.Top, true);
        if (tnt != null) {
            tnt.setExploded(true);
        }
        Set<Chunklet> modifiedChunklets = new HashSet<>();
        for (Vector3f rayVector : RAY_VECTORS) {
            modifiedChunklets.addAll(blast(world, explosionCube, rayVector, explodeRadius, power, normalizedPosition));
        }
        if (GameMode.isSurvivalMode()) {
            explosionCube.applyDamageLive();
        }
        world.recalculateChunklets(modifiedChunklets);
        float distance = Distance.getDistanceBetweenPoints(position, world.player.position, Float.MAX_VALUE);
        SoundManager.playMaterialSound(Material.EXPLOSIVE, distance);
    }

    @NonNull
    private static Vector3f getNormalizedExplosionPosition(Vector3f position) {
        Vector3f pos = new Vector3f(position);
        pos.x += 0.5f;
        pos.y += 0.5f;
        pos.z += 0.5f;
        return pos;
    }

    private static int getExplodeRadius(float power) {
        return (int) Math.ceil(((MAX_BLAST_FACTOR * power) / BLAST_DECREASED_PER_STEP) * EXPLOSION_STEP);
    }

    @NonNull
    private static Set<Chunklet> blast(@NonNull World world, @NonNull ExplosionCube explosionCube, Vector3f rayVector, int explodeRadius, float power, Vector3f position) {
        float blastForce = RandomUtil.getRandomInRangeExclusive(MIN_BLAST_FACTOR, MAX_BLAST_FACTOR) * power;
        float currentStep = 0.0f;
        Set<Chunklet> modifiedChunklets = new HashSet<>();
        do {
            currentStep += EXPLOSION_STEP;
            explosionCube.damageLive(rayVector, blastForce, currentStep);
            Vector3f blockPosition = getBlockPosition(rayVector, currentStep, position);
            byte blockType = world.blockType(blockPosition);
            if (blockType != 0) {
                if (blockType == BlockFactory.TNT_ID) {
                    removeBlock(world, modifiedChunklets, blockPosition, blockType);
                    world.activateTNT(blockPosition, TNTBlock.DetonationDelayType.SHORT_DELAY, false);
                } else {
                    blastForce -= getAbsorbedBlastForce(currentStep, blockType);
                    if (blastForce >= 0.0f) {
                        removeBlock(world, modifiedChunklets, blockPosition, blockType);
                    }
                }
            }
            blastForce -= BLAST_DECREASED_PER_STEP;
        } while (blastForce > 0.0f);
        return modifiedChunklets;
    }

    private static float getAbsorbedBlastForce(float currentStep, Byte blockType) {
        return ((BlockBlastResistance.getBlastResistance(blockType) / 5.0f) + EXPLOSION_STEP) * currentStep;
    }

    @NonNull
    @Contract("_, _, _ -> new")
    private static Vector3f getBlockPosition(@NonNull Vector3f rayVector, float currentStep, @NonNull Vector3f position) {
        return new Vector3f((int) ((rayVector.x * currentStep) + position.x), (int) ((rayVector.y * currentStep) + position.y), (int) ((rayVector.z * currentStep) + position.z));
    }

    private static void removeBlock(@NonNull World world, @NonNull Set<Chunklet> result, @NonNull Vector3f position, byte blockId) {
        result.addAll(world.setBlockTypeWithoutGeometryRecalculate(position.x, position.y, position.z, (byte) 0, (byte) 0));
        if (GameMode.isSurvivalMode() && blockId != BlockFactory.TNT_ID && RandomUtil.getChance(EXPLOSION_STEP)) {
            world.addDroppableItem(blockId, position.x, position.y, position.z);
        }
    }

    private static void initRayVectors() {
        RAY_VECTORS = new HashSet<>();
        fillRayVectors();
    }

    private static void fillRayVectors() {
        fillRayVectors(getRays(CoordOrder.XYZ), getRays(CoordOrder.YXZ), getRays(CoordOrder.ZXY));
    }

    private static void fillRayVectors(@NonNull Collection<Vector3f>... rayVectorCollectionList) {
        for (Collection<Vector3f> rayVectorList : rayVectorCollectionList) {
            RAY_VECTORS.addAll(rayVectorList);
        }
    }

    @NonNull
    private static Collection<Vector3f> getRays(CoordOrder coordOrder) {
        Vector3f vector3f;
        Collection<Vector3f> result = new ArrayList<>();
        float[] iArray = {-1.0f, 1.0f};
        for (float i : iArray) {
            for (float j = -1.0f; j + STEP <= 1.0f; j += STEP) {
                for (float k = -1.0f; k + STEP <= 1.0f; k += STEP) {
                    if (coordOrder == CoordOrder.XYZ) {
                        vector3f = new Vector3f(i, j, k);
                    } else if (coordOrder == CoordOrder.YXZ) {
                        vector3f = new Vector3f(j, i, k);
                    } else {
                        vector3f = new Vector3f(j, k, i);
                    }
                    result.add(vector3f);
                }
            }
        }
        return result;
    }

    public static class ExplosionCube {
        DamagableLoss[][][] damagables;
        int explodeRadius;

        public ExplosionCube(World world, Vector3f position, int explodeRadius) {
            this.explodeRadius = explodeRadius;
            int size = (explodeRadius * 2) + 1;
            this.damagables = (DamagableLoss[][][]) Array.newInstance(DamagableLoss.class, size, size, size);
            fillDamagablesInExplodeRadius(world, position, explodeRadius);
        }

        private void fillDamagablesInExplodeRadius(World world, Vector3f position, int explodeRadius) {
            for (Mob mob : MobFactory.getAllMobsCopy()) {
                Vector3f mobPosition = mob.getPosition();
                if (belongsExplosionCube(position, explodeRadius, mobPosition)) {
                    this.damagables[(int) ((position.x - mobPosition.x) + explodeRadius)][(int) ((position.y - mobPosition.y) + explodeRadius)][(int) ((position.z - mobPosition.z) + explodeRadius)] = new DamagableLoss(mob);
                }
            }
            Vector3f playerPosition = world.player.position;
            if (belongsExplosionCube(position, explodeRadius, playerPosition)) {
                this.damagables[(int) ((position.x - playerPosition.x) + explodeRadius)][(int) ((position.y - playerPosition.y) + explodeRadius)][(int) ((position.z - playerPosition.z) + explodeRadius)] = new DamagableLoss(world.player);
            }
        }

        private boolean belongsExplosionCube(@NonNull Vector3f position, int explodeRadius, @NonNull Vector3f mobPosition) {
            return Range.inRange(mobPosition.x, ((float) (-explodeRadius)) + position.x, ((float) explodeRadius) + position.x) && Range.inRange(mobPosition.y, ((float) (-explodeRadius)) + position.y, ((float) explodeRadius) + position.y) && Range.inRange(mobPosition.z, ((float) (-explodeRadius)) + position.z, ((float) explodeRadius) + position.z);
        }

        public void damageLive(@NonNull Vector3f rayVector, float blastForce, float currentStep) {
            int yPos;
            int zPos;
            DamagableLoss damagable;
            int damage;
            int xPos = ((int) (rayVector.x * currentStep)) + this.explodeRadius;
            if (xPos >= 0 && xPos <= this.damagables.length && (yPos = ((int) (rayVector.y * currentStep)) + this.explodeRadius) >= 0 && yPos <= this.damagables.length && (zPos = ((int) (rayVector.z * currentStep)) + this.explodeRadius) >= 0 && zPos <= this.damagables.length && (damagable = this.damagables[xPos][yPos][zPos]) != null && (damage = (int) (7.0f * blastForce)) > 0) {
                damagable.loss = Math.max(damage, damagable.loss);
            }
        }

        public void applyDamageLive() {
            for (int i = 0; i < this.damagables.length; i++) {
                for (int j = 0; j < this.damagables[i].length; j++) {
                    for (int k = 0; k < this.damagables[i][j].length; k++) {
                        DamagableLoss damagableLoss = this.damagables[i][j][k];
                        if (damagableLoss != null && damagableLoss.loss > 0) {
                            damagableLoss.damagable.takeDamage(damagableLoss.loss);
                        }
                    }
                }
            }
        }
    }

    public static class DamagableLoss {
        Damagable damagable;
        int loss;

        public DamagableLoss(Damagable damagable) {
            this.damagable = damagable;
        }
    }
}
