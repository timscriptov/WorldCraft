package com.solverlabs.worldcraft.entity_menu;

import com.solverlabs.droid.rugl.Game;
import com.solverlabs.droid.rugl.geom.ColouredShape;
import com.solverlabs.droid.rugl.geom.Shape;
import com.solverlabs.droid.rugl.geom.ShapeUtil;
import com.solverlabs.droid.rugl.gl.StackedRenderer;
import com.solverlabs.droid.rugl.gl.State;
import com.solverlabs.droid.rugl.input.TapPad;
import com.solverlabs.droid.rugl.input.Touch;
import com.solverlabs.droid.rugl.util.Colour;
import com.solverlabs.droid.rugl.util.geom.BoundingRectangle;
import com.solverlabs.worldcraft.chunk.tile_entity.Inventory;
import com.solverlabs.worldcraft.ui.CustomTapPad;
import com.solverlabs.worldcraft.ui.GUI;


public abstract class CustomMenu implements Touch.TouchListener {
    protected static final float RATIO_Y = Game.mScreenHeight / Game.mGameHeight;
    protected static final float RATIO_X = Game.mScreenWidth / Game.mGameWidth;
    private final int innerColour = Colour.packInt(148, 134, 123, 255);
    private final int boundsColour = Colour.packFloat(0.0f, 0.0f, 0.0f, 0.8f);
    public BoundingRectangle bounds = new BoundingRectangle(0.0f, 0.0f, Game.mGameWidth, Game.mGameHeight);
    protected Inventory inventory;
    protected boolean show;
    private final TapPad.Listener exitTapListener = new TapPad.Listener() {
        @Override
        public void onTap(TapPad pad) {
            CustomMenu.this.setShow(false);
        }

        @Override
        public void onLongPress(TapPad pad) {
        }

        @Override
        public void onFlick(TapPad pad, int horizontal, int vertical) {
        }

        @Override
        public void onDoubleTap(TapPad pad) {
        }
    };
    protected Touch.Pointer touch;
    protected CustomTapPad exitTap = new CustomTapPad(Game.mGameWidth - 68.0f, Game.mGameHeight - 68.0f, 60.0f, 60.0f, GUI.getFont(), "X");
    private ColouredShape boundShape;
    private ColouredShape innerShape;

    public CustomMenu(Inventory inventory) {
        this.inventory = inventory;
        this.exitTap.listener = this.exitTapListener;
    }

    public void draw(StackedRenderer sr) {
        if (this.show) {
            drawInnerBound(sr);
            drawBound(sr);
            sr.render();
        }
    }

    protected void drawInnerBound(StackedRenderer sr) {
        if (this.innerShape == null) {
            Shape is = ShapeUtil.innerQuad(this.bounds.x.getMin(), this.bounds.y.getMin(), this.bounds.x.getMax(), this.bounds.y.getMax(), this.bounds.y.getSpan(), 0.0f);
            this.innerShape = new ColouredShape(is, this.innerColour, (State) null);
        }
        this.innerShape.render(sr);
    }

    protected void drawBound(StackedRenderer sr) {
        if (this.boundShape == null) {
            Shape bs = ShapeUtil.innerQuad(this.bounds.x.getMin(), this.bounds.y.getMin(), this.bounds.x.getMax(), this.bounds.y.getMax(), 8.0f, 0.0f);
            this.boundShape = new ColouredShape(bs, this.boundsColour, (State) null);
        }
        this.boundShape.render(sr);
    }

    public boolean isVisible() {
        return this.show;
    }

    public void setShow(boolean isShow) {
        this.show = isShow;
    }
}
