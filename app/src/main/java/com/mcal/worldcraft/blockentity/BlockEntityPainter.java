package com.mcal.worldcraft.blockentity;

import com.mcal.droid.rugl.gl.GLUtil;
import com.mcal.droid.rugl.gl.StackedRenderer;
import com.mcal.droid.rugl.util.FPSCamera;
import com.mcal.droid.rugl.util.geom.Vector3f;
import com.mcal.worldcraft.Player;
import com.mcal.worldcraft.World;

import java.util.ArrayList;
import java.util.List;

public class BlockEntityPainter {
    private final StackedRenderer renderer = new StackedRenderer();
    private final List<BlockEntity> entityList = new ArrayList<>();

    public static void init() {
        TNTBlock.init();
    }

    public static void loadTexture(World world) {
    }

    public void advance(float delta, World world, FPSCamera cam, Player player) {
        try {
            synchronized (entityList) {
                for (BlockEntity entity : new ArrayList<>(entityList)) {
                    if (entity.isDestroyed()) {
                        entityList.remove(entity);
                    } else {
                        entity.advance(delta);
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void draw(Vector3f eye, int worldLoadRadius, FPSCamera cam) {
        try {
            synchronized (entityList) {
                for (BlockEntity entity : new ArrayList<>(entityList)) {
                    entity.draw(renderer);
                }
            }
            renderer.render();
            GLUtil.checkGLError();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void add(BlockEntity entity) {
        try {
            synchronized (entityList) {
                entityList.add(entity);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
