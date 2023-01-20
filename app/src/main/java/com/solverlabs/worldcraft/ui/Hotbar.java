package com.solverlabs.worldcraft.ui;

import com.solverlabs.droid.rugl.geom.ColouredShape;
import com.solverlabs.droid.rugl.geom.Shape;
import com.solverlabs.droid.rugl.geom.ShapeUtil;
import com.solverlabs.droid.rugl.gl.StackedRenderer;
import com.solverlabs.droid.rugl.gl.State;
import com.solverlabs.droid.rugl.input.Touch;
import com.solverlabs.droid.rugl.util.Colour;
import com.solverlabs.droid.rugl.util.geom.BoundingRectangle;
import com.solverlabs.worldcraft.Player;
import com.solverlabs.worldcraft.inventory.InventoryTapItem;
import java.util.Iterator;

public class Hotbar implements Touch.TouchListener {
    private ColouredShape boundsShape;
    private final Player player;
    private Touch.Pointer touch;
    public BoundingRectangle bounds = new BoundingRectangle(150.0f, 0.0f, 420.0f, 90.0f);
    public int boundsColour = Colour.packFloat(1.0f, 1.0f, 1.0f, 0.8f);
    public float maxZoom = 2.0f;
    public float zoomTime = 0.15f;

    public Hotbar(Player player) {
        this.player = player;
    }

    public void advance(float delta) {
        for (int i = 0; i < this.player.hotbar.size(); i++) {
            this.player.hotbar.get(i).advance();
            if (this.player.hotbar.get(i).getInventoryItem().isEmpty()) {
                this.player.hotbar.remove(i);
            }
        }
    }

    public void draw(StackedRenderer sr) {
        if (this.boundsShape == null) {
            Shape bs = ShapeUtil.innerQuad(this.bounds.x.getMin(), this.bounds.y.getMin(), this.bounds.x.getMax(), this.bounds.y.getMax(), 5.0f, 0.0f);
            this.boundsShape = new ColouredShape(bs, this.boundsColour, null);
        }
        this.boundsShape.render(sr);
        for (int i = 0; i < 5; i++) {
            if (i < this.player.hotbar.size() && this.player.hotbar.get(i) != null) {
                float x = this.bounds.x.getMin() + (84.0f / 2.0f) + (i * 84.0f);
                float y = this.bounds.y.toValue(0.5f);
                this.player.hotbar.get(i).setPosition(x, y);
                this.player.hotbar.get(i).draw(sr, 0.0f);
            }
        }
    }

    public void boundsDirty() {
        this.boundsShape = null;
    }

    @Override
    public boolean pointerAdded(Touch.Pointer p) {
        if (this.touch == null && this.bounds.contains(p.x, p.y)) {
            this.touch = p;
            for (InventoryTapItem item : this.player.hotbar) {
                item.pointerAdded(p);
            }
            return true;
        }
        return false;
    }

    @Override
    public void pointerRemoved(Touch.Pointer p) {
        if (this.touch == p) {
            this.touch = null;
            for (int i = 0; i < this.player.hotbar.size(); i++) {
                this.player.hotbar.get(i).pointerRemoved(p);
            }
        }
    }

    @Override
    public void reset() {
        this.touch = null;
    }
}
