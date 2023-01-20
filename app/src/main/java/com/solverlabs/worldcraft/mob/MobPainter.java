package com.solverlabs.worldcraft.mob;

import androidx.annotation.Nullable;

import com.solverlabs.droid.rugl.util.FPSCamera;
import com.solverlabs.droid.rugl.util.geom.Vector3f;
import com.solverlabs.worldcraft.Player;
import com.solverlabs.worldcraft.World;
import com.solverlabs.worldcraft.R;
import com.solverlabs.worldcraft.mob.cow.Cow;
import com.solverlabs.worldcraft.mob.cow.CowFactory;
import com.solverlabs.worldcraft.mob.pig.Pig;
import com.solverlabs.worldcraft.mob.pig.PigFactory;
import com.solverlabs.worldcraft.mob.sheep.Sheep;
import com.solverlabs.worldcraft.mob.sheep.SheepFactory;
import com.solverlabs.worldcraft.mob.zombie.Zombie;
import com.solverlabs.worldcraft.mob.zombie.ZombieFactory;
import com.solverlabs.worldcraft.util.RandomUtil;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MobPainter {
    private static final int MOB_CHUNK_UPDATE_TIMEOUT = 2000;
    private static CowFactory cowFactory;
    private static final List<Runnable> onInitedTaskList = new ArrayList();
    private static PigFactory pigFactory;
    private static SheepFactory sheepFactory;
    private static ZombieFactory zombieFactory;
    private long mobChunksUpdatedAt;

    public static void loadTexture(World world) {
        cowFactory = new CowFactory();
        pigFactory = new PigFactory();
        sheepFactory = new SheepFactory();
        zombieFactory = new ZombieFactory();
        cowFactory.loadTexture(R.drawable.cow);
        pigFactory.loadTexture(R.drawable.pig);
        sheepFactory.loadTexture(R.drawable.sheep);
        zombieFactory.loadTexture(R.drawable.zombie);
        cowFactory.setWorld(world);
        pigFactory.setWorld(world);
        sheepFactory.setWorld(world);
        zombieFactory.setWorld(world);
        onInited();
    }

    public static MobFactory getFactory(Mob mob) {
        if (mob instanceof Cow) {
            return cowFactory;
        }
        if (mob instanceof Pig) {
            return pigFactory;
        }
        if (mob instanceof Sheep) {
            return sheepFactory;
        }
        if (mob instanceof Zombie) {
            return zombieFactory;
        }
        return null;
    }

    @Nullable
    public static MobFactory getRandomPassiveMobFactory() {
        int mobType = RandomUtil.getRandomInRangeInclusive(0, 2);
        switch (mobType) {
            case 0:
                return cowFactory;
            case 1:
                return pigFactory;
            case 2:
                return sheepFactory;
            default:
                return null;
        }
    }

    public static MobFactory getRandomHostileMobFactory() {
        return zombieFactory;
    }

    public void advance(float delta, World world, FPSCamera cam, Player player) {
        if (System.currentTimeMillis() - this.mobChunksUpdatedAt > 2000) {
            world.updateMobChunks();
            this.mobChunksUpdatedAt = System.currentTimeMillis();
        }
        cowFactory.advance(delta, world, cam, player);
        pigFactory.advance(delta, world, cam, player);
        sheepFactory.advance(delta, world, cam, player);
        zombieFactory.advance(delta, world, cam, player);
    }

    public void draw(Vector3f eye, int worldLoadRadius, FPSCamera cam) {
        cowFactory.draw(eye, worldLoadRadius, cam);
        pigFactory.draw(eye, worldLoadRadius, cam);
        sheepFactory.draw(eye, worldLoadRadius, cam);
        zombieFactory.draw(eye, worldLoadRadius, cam);
    }

    public boolean selectMobOnRay(float x, float y, float z, float directionX, float directionY, float directionZ) {
        deselectMobs();
        Mob closestMob = getClosestMob(x, y, z, directionX, directionY, directionZ);
        if (closestMob != null) {
            closestMob.setSelected(true);
            return true;
        }
        return false;
    }

    public Mob getMobOnRay(float x, float y, float z, float directionX, float directionY, float directionZ) {
        return getClosestMob(x, y, z, directionX, directionY, directionZ);
    }

    private void deselectMobs() {
        pigFactory.deselectMobs();
        cowFactory.deselectMobs();
        sheepFactory.deselectMobs();
        zombieFactory.deselectMobs();
    }

    private Mob getClosestMob(float x, float y, float z, float directionX, float directionY, float directionZ) {
        MobFactory.updateAllMobs(pigFactory.mobs, cowFactory.mobs, sheepFactory.mobs, zombieFactory.mobs);
        return MobFactory.closestMobOnRay(x, y, z, directionX, directionY, directionZ);
    }

    public static void onInited() {
        synchronized (onInitedTaskList) {
            Iterator<Runnable> iterator = onInitedTaskList.iterator();
            while (iterator.hasNext()) {
                Runnable runnable = iterator.next();
                runnable.run();
                iterator.remove();
            }
        }
    }

    public static void executeOnInited(Runnable runnable) {
        if (runnable != null) {
            if (cowFactory != null && pigFactory != null && sheepFactory != null && zombieFactory != null) {
                runnable.run();
                return;
            }
            synchronized (onInitedTaskList) {
                onInitedTaskList.add(runnable);
            }
        }
    }

    public static float getDistanceToClosestHostileMob(float x, float y, float z) {
        Mob closestHostileMob = zombieFactory.getClosestMob(x, y, z);
        if (closestHostileMob == null) {
            return 1000.0f;
        }
        return closestHostileMob.distance(x, y, z);
    }

    public static void debug() {
        MobFactory.printAllMobs();
        System.out.println("_PIG FACTORY: ");
        pigFactory.printMobs();
        System.out.println("_COW FACTORY: ");
        cowFactory.printMobs();
        System.out.println("_SHEEP FACTORY: ");
        sheepFactory.printMobs();
    }
}
