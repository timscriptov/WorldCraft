package com.solverlabs.worldcraft.mob;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.solverlabs.droid.rugl.gl.GLUtil;
import com.solverlabs.droid.rugl.gl.StackedRenderer;
import com.solverlabs.droid.rugl.gl.State;
import com.solverlabs.droid.rugl.gl.enums.FogMode;
import com.solverlabs.droid.rugl.gl.enums.MagFilter;
import com.solverlabs.droid.rugl.gl.enums.MinFilter;
import com.solverlabs.droid.rugl.gl.facets.Fog;
import com.solverlabs.droid.rugl.res.BitmapLoader;
import com.solverlabs.droid.rugl.res.ResourceLoader;
import com.solverlabs.droid.rugl.texture.Texture;
import com.solverlabs.droid.rugl.texture.TextureFactory;
import com.solverlabs.droid.rugl.util.Colour;
import com.solverlabs.droid.rugl.util.FPSCamera;
import com.solverlabs.droid.rugl.util.geom.BoundingCuboid;
import com.solverlabs.droid.rugl.util.geom.Vector3f;
import com.solverlabs.worldcraft.Player;
import com.solverlabs.worldcraft.World;
import com.solverlabs.worldcraft.chunk.Chunk;
import com.solverlabs.worldcraft.chunk.entity.MobEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class MobFactory implements Mob.MobInteractionListener {
    public static final int DISTANCE_TO_DESPAWN_MOB = 45;
    private static final float DEFAULT_SPAWN_CHANCE = 1.0E-4f;
    private static final int MAX_MOB_COUNT = 100;
    private static final float SHIFT_ON_ATTACKED = 0.75f;
    private static final Collection<Mob> allMobs = new ArrayList<>();
    private final StackedRenderer renderer = new StackedRenderer();
    protected World world;
    protected Collection<Mob> mobs = new ArrayList<>(100);
    protected HashMap<Mob, MobView> mobViews = new HashMap<>(100);
    protected State state = GLUtil.typicalState.with(MinFilter.NEAREST, MagFilter.NEAREST).with(new Fog(FogMode.LINEAR, 0.5f, 30.0f, 40.0f, Colour.packFloat(0.7f, 0.7f, 0.9f, 1.0f)));

    public MobFactory() {
    }

    public MobFactory(int drawableId) {
        loadTexture(drawableId);
    }

    public static void updateAllMobs(@NonNull Collection<Mob>... mobCollections) {
        synchronized (allMobs) {
            allMobs.clear();
            for (Collection<Mob> mobCollection : mobCollections) {
                allMobs.addAll(mobCollection);
            }
        }
    }

    public static boolean noMobs() {
        boolean isEmpty;
        synchronized (allMobs) {
            isEmpty = allMobs.isEmpty();
        }
        return isEmpty;
    }

    public static boolean intersects(Mob sourceMob) {
        if (sourceMob != null) {
            for (Mob mob : getAllMobsCopy()) {
                if (mob != null && !mob.equals(sourceMob) && mob.intersects(sourceMob)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean intersection(Mob sourceMob, BoundingCuboid intersection) {
        if (sourceMob != null) {
            for (Mob mob : getAllMobsCopy()) {
                if (mob != null && !mob.equals(sourceMob) && mob.intersection(sourceMob, intersection)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean intersectionBounds(BoundingCuboid bounds, BoundingCuboid intersection) {
        if (bounds != null) {
            for (Mob mob : getAllMobsCopy()) {
                if (mob != null && mob.intersection(bounds, intersection)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean intersectionBounds(Mob mob, BoundingCuboid bounds, BoundingCuboid intersection) {
        return bounds != null && mob != null && mob.intersection(bounds, intersection);
    }

    public static Mob closestMobOnRay(float x, float y, float z, float directionX, float directionY, float directionZ) {
        return closestMobOnRay(getAllMobsCopy(), x, y, z, directionX, directionY, directionZ);
    }

    public static Mob closestMobOnRay(@NonNull Collection<Mob> mobCollection, float x, float y, float z, float directionX, float directionY, float directionZ) {
        Mob closestMob = null;
        float distanceToMob = -1.0f;
        for (Mob mob : mobCollection) {
            if (mob != null && mob.isOnRay(x, y, z, directionX, directionY, directionZ)) {
                float distance = mob.distance(x, y, z);
                if (distanceToMob == -1.0f || Float.compare(distanceToMob, distance) > 0) {
                    closestMob = mob;
                    distanceToMob = distance;
                }
            }
        }
        return closestMob;
    }

    @Nullable
    public static Collection<Mob> getAllMobsCopy() {
        Collection<Mob> allMobsCopy;
        synchronized (allMobs) {
            try {
                allMobsCopy = new ArrayList<>(allMobs);
                return allMobsCopy;
            } catch (Throwable th) {
            }
        }
        return null;
    }

    @NonNull
    public static List<Mob> getMobs(Chunk chunk) {
        List<Mob> result = new ArrayList<>();
        for (Mob mob : getAllMobsCopy()) {
            if (chunk.contains(mob.getPosition())) {
                result.add(mob);
            }
        }
        return result;
    }

    @NonNull
    public static List<MobEntity> getMobEntities(Chunk chunk) {
        List<MobEntity> result = new ArrayList<>();
        for (Mob mob : getMobs(chunk)) {
            result.add(mob.getEntity());
        }
        return result;
    }

    public static void updateMobsLight() {
        for (Mob mob : getAllMobsCopy()) {
            mob.updateLight();
        }
    }

    public static void printAllMobs() {
        Collection<Mob> allMobsCopy = getAllMobsCopy();
        System.out.println("ALL MOBS: " + allMobsCopy.size());
        for (Mob mob : allMobsCopy) {
            System.out.println(" " + mob);
        }
    }

    public abstract Mob createMob(Vector3f vector3f);

    public abstract int getMaxMobCountAroundPlayer();

    protected abstract MobView getMobView(Mob mob);

    public void loadTexture(int drawableId) {
        ResourceLoader.loadNow(new BitmapLoader(drawableId) {
            @Override
            public void complete() {
                Texture texture = TextureFactory.buildTexture(this.resource, true, false);
                if (texture != null) {
                    MobFactory.this.state = texture.applyTo(MobFactory.this.state);
                }
                this.resource.bitmap.recycle();
            }
        });
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public Mob addMob(Vector3f position) {
        return addMob(createMob(position));
    }

    public Mob addMob(Mob mob) {
        if (mob != null && isSpawnConditionMet(mob.getPosition())) {
            mob.setLight(15.0f);
            mob.setStateChanged();
            mob.setInteractionListener(this);
            synchronized (this.mobs) {
                this.mobs.add(mob);
            }
            synchronized (this.mobViews) {
                this.mobViews.put(mob, getMobView(mob));
            }
            synchronized (allMobs) {
                allMobs.add(mob);
            }
            return mob;
        }
        return null;
    }

    public void removeMob(Mob mob) {
        if (mob != null) {
            synchronized (this.mobs) {
                this.mobs.remove(mob);
            }
            synchronized (this.mobViews) {
                this.mobViews.remove(mob);
            }
            synchronized (allMobs) {
                allMobs.remove(mob);
            }
        }
    }

    public void advance(float delta, World world, FPSCamera cam, Player player) {
        MobView mobView;
        this.world = world;
        for (Mob mob : getMobsCopy()) {
            if (mob != null) {
                advanceMob(delta, world, cam, player, mob);
                synchronized (this.mobViews) {
                    mobView = this.mobViews.get(mob);
                }
                if (mobView != null) {
                    mobView.advance(delta, cam);
                }
            }
        }
        GLUtil.checkGLError();
    }

    public boolean isMobOnRay(float x, float y, float z, float directionX, float directionY, float directionZ) {
        for (Mob mob : getMobsCopy()) {
            if (mob != null && mob.isOnRay(x, y, z, directionX, directionY, directionZ)) {
                return true;
            }
        }
        return false;
    }

    public Mob getClosestMob(float x, float y, float z) {
        Mob closestMob = null;
        float distanceToMob = -1.0f;
        for (Mob mob : this.mobs) {
            if (mob != null) {
                float distance = mob.distance(x, y, z);
                if (distanceToMob == -1.0f || Float.compare(distanceToMob, distance) > 0) {
                    closestMob = mob;
                    distanceToMob = distance;
                }
            }
        }
        return closestMob;
    }

    public void deselectMobs() {
        for (Mob mob : getMobsCopy()) {
            if (mob != null) {
                mob.setSelected(false);
            }
        }
    }

    @Nullable
    private Collection<Mob> getMobsCopy() {
        Collection<Mob> mobsCopy;
        synchronized (this.mobs) {
            try {
                mobsCopy = new ArrayList<>(this.mobs);
                return mobsCopy;
            } catch (Throwable th) {
            }
        }
        return null;
    }

    public void advanceMob(float delta, World world, FPSCamera cam, Player player, @NonNull Mob mob) {
        mob.advance(delta, world, cam, player);
    }

    public void draw(Vector3f eye, int worldLoadRadius, FPSCamera cam) {
        HashMap<Mob, MobView> mobViewsCopy;
        synchronized (this.mobViews) {
            try {
                mobViewsCopy = new HashMap<>(this.mobViews);
                for (MobView mobView : mobViewsCopy.values()) {
                    if (mobView != null) {
                        mobView.render(this.renderer, cam);
                    }
                }
                this.renderer.render();
                GLUtil.checkGLError();
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }

    public List<Mob> despawnMobs() {
        List<Mob> result = new ArrayList<>();
        for (Mob mob : getMobsCopy()) {
            if (mob != null && mob.getDistance() > 45.0f) {
                result.add(despawnMob(mob));
            }
        }
        return result;
    }

    public Mob despawnMob(Mob mob) {
        removeMob(mob);
        return mob;
    }

    public boolean isSpawnConditionMet(Vector3f spawnLocation) {
        return true;
    }

    public int getAllMobCount() {
        int size;
        synchronized (allMobs) {
            size = allMobs != null ? allMobs.size() : 0;
        }
        return size;
    }

    public int getMobCount() {
        int size;
        synchronized (this.mobs) {
            size = this.mobs != null ? this.mobs.size() : 0;
        }
        return size;
    }

    public State getState() {
        return this.state;
    }

    public float getSpawnChange() {
        return DEFAULT_SPAWN_CHANCE;
    }

    public int getMinGroupSize() {
        return 0;
    }

    public int getMaxGroupSize() {
        return 4;
    }

    @Override
    public void mobAttacked(@NonNull Mob mob, @NonNull Player player) {
        mob.takeDamage(player.getWeaponDamage());
        shiftMobOnAttack(mob, player);
        MobAI.mobAttacked(mob, player);
    }

    public void shiftMobOnAttack(@NonNull Mob mob, @NonNull Player player) {
        mob.shift(player.getAngle(), SHIFT_ON_ATTACKED);
    }

    @Override
    public void dead(Mob mob) {
        removeMob(mob);
        dropItems(mob, mob.getDeathDrops());
    }

    public void dropItems(Mob mob, @NonNull HashMap<Byte, Integer> dropItems) {
        for (Map.Entry<Byte, Integer> entry : dropItems.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                this.world.addDroppableItem(entry.getKey(), mob.getPosition().x, mob.getPosition().y + 0.5f, mob.getPosition().z);
            }
        }
    }

    public void printMobs() {
        System.out.println(" MOBS " + this.mobs.size() + ": ");
        for (Mob mob : this.mobs) {
            System.out.println("  " + mob + " distance: " + mob.getDistance());
        }
        System.out.println(" VIEWS " + this.mobViews.size() + ": ");
        for (MobView mobView : this.mobViews.values()) {
            System.out.println("  " + mobView.mob + " distance: " + mobView.mob.getDistance());
        }
    }
}
