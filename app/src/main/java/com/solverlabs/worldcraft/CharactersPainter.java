package com.solverlabs.worldcraft;

import com.solverlabs.droid.rugl.gl.GLUtil;
import com.solverlabs.droid.rugl.gl.StackedRenderer;
import com.solverlabs.droid.rugl.res.FontLoader;
import com.solverlabs.droid.rugl.res.ResourceLoader;
import com.solverlabs.droid.rugl.text.Font;
import com.solverlabs.droid.rugl.util.FPSCamera;
import com.solverlabs.droid.rugl.util.geom.Vector3f;
import com.solverlabs.worldcraft.multiplayer.Multiplayer;

import java.util.ArrayList;
import java.util.Collection;

public class CharactersPainter {
    private static final int MAX_ENEMY_COUNT = 100;
    public static Font font;
    private final StackedRenderer renderer = new StackedRenderer();
    private final Collection<Enemy> enemies = new ArrayList<>(MAX_ENEMY_COUNT);
    public boolean shouldDraw = true;

    public static void loadFont() {
        ResourceLoader.loadNow(new FontLoader(R.raw.font, false) {
            @Override
            public void fontLoaded() {
                CharactersPainter.font = this.resource;
            }
        });
    }

    public void advance(float delta, int worldLoadRadius, FPSCamera cam) {
        Collection<Enemy> enemiesCopy = Multiplayer.getEnemiesCopy();
        if (this.shouldDraw && enemiesCopy.size() != 0) {
            updateEnemyList(enemiesCopy);
            for (Enemy enemy : this.enemies) {
                enemy.advance(delta, worldLoadRadius, cam);
            }
            GLUtil.checkGLError();
        }
    }

    private void updateEnemyList(Collection<Enemy> enemiesCopy) {
        this.enemies.clear();
        try {
            this.enemies.addAll(enemiesCopy);
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    public void draw(Vector3f eye, int worldLoadRadius, FPSCamera cam) {
        if (this.shouldDraw) {
            Collection<Enemy> enemiesCopy = Multiplayer.getEnemiesCopy();
            if (enemiesCopy.size() != 0) {
                if (font == null) {
                    loadFont();
                }
                try {
                    updateEnemyList(enemiesCopy);
                    for (Enemy enemy : this.enemies) {
                        if (enemy != null && enemy.isVisible(worldLoadRadius, cam)) {
                            enemy.render(this.renderer, cam);
                        }
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
                this.renderer.render();
                GLUtil.checkGLError();
            }
        }
    }
}
