package com.solverlabs.worldcraft.ui;

import com.solverlabs.droid.rugl.geom.ColouredShape;
import com.solverlabs.droid.rugl.geom.Shape;
import com.solverlabs.droid.rugl.geom.ShapeUtil;
import com.solverlabs.droid.rugl.gl.StackedRenderer;
import com.solverlabs.droid.rugl.input.Touch;
import com.solverlabs.droid.rugl.util.Colour;
import com.solverlabs.droid.rugl.util.geom.BoundingRectangle;
import com.solverlabs.worldcraft.Player;
import com.solverlabs.worldcraft.inventory.InventoryTapItem;

/**
 * On-screen inventory
 */
public class Hotbar implements Touch.TouchListener {
    private final Player player;
    public BoundingRectangle bounds = new BoundingRectangle(150.0f, 0.0f, 420.0f, 90.0f);
    public int boundsColour = Colour.packFloat(1.0f, 1.0f, 1.0f, 0.8f);
    /**
     * How much to zoom roll-overed items
     */
    public float maxZoom = 2.0f;
    public float zoomTime = 0.15f;
    private ColouredShape boundsShape;
    private Touch.Pointer touch;

    /**
     * @param player
     */
    public Hotbar(Player player) {
        this.player = player;
    }

    /**
     * @param delta
     */
    public void advance(float delta) {
        for (int i = 0; i < player.hotbar.size(); i++) {
            player.hotbar.get(i).advance();
            if (player.hotbar.get(i).getInventoryItem().isEmpty()) {
                player.hotbar.remove(i);
            }
        }
    }

    public void draw(StackedRenderer sr) {
        if (boundsShape == null) {
            Shape bs = ShapeUtil.innerQuad(bounds.x.getMin(), bounds.y.getMin(), bounds.x.getMax(), bounds.y.getMax(), 5.0f, 0.0f);
            boundsShape = new ColouredShape(bs, boundsColour, null);
        }
        this.boundsShape.render(sr);
        for (int i = 0; i < 5; i++) {
            if (i < player.hotbar.size() && player.hotbar.get(i) != null) {
                float x = bounds.x.getMin() + (84.0f / 2.0f) + (i * 84.0f);
                float y = bounds.y.toValue(0.5f);
                player.hotbar.get(i).setPosition(x, y);
                player.hotbar.get(i).draw(sr, 0.0f);
            }
        }
    }

    public void boundsDirty() {
        this.boundsShape = null;
    }

    @Override
    public boolean pointerAdded(Touch.Pointer p) {
        if (touch == null && bounds.contains(p.x, p.y)) {
            touch = p;
            for (InventoryTapItem item : player.hotbar) {
                item.pointerAdded(p);
            }
            return true;
        }
        return false;
    }

    @Override
    public void pointerRemoved(Touch.Pointer p) {
        if (touch == p) {
            touch = null;
            for (int i = 0; i < player.hotbar.size(); i++) {
                player.hotbar.get(i).pointerRemoved(p);
            }
        }
    }

    @Override
    public void reset() {
        touch = null;
    }
}
