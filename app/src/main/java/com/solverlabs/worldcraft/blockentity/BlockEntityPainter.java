package com.solverlabs.worldcraft.blockentity;

import com.solverlabs.droid.rugl.gl.GLUtil;
import com.solverlabs.droid.rugl.gl.StackedRenderer;
import com.solverlabs.droid.rugl.util.FPSCamera;
import com.solverlabs.droid.rugl.util.geom.Vector3f;
import com.solverlabs.worldcraft.Player;
import com.solverlabs.worldcraft.World;

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
            synchronized (this.entityList) {
                for (BlockEntity entity : new ArrayList<>(this.entityList)) {
                    if (entity.isDestroyed()) {
                        this.entityList.remove(entity);
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
            synchronized (this.entityList) {
                for (BlockEntity entity : new ArrayList<>(this.entityList)) {
                    entity.draw(this.renderer);
                }
            }
            this.renderer.render();
            GLUtil.checkGLError();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void add(BlockEntity entity) {
        try {
            synchronized (this.entityList) {
                this.entityList.add(entity);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
