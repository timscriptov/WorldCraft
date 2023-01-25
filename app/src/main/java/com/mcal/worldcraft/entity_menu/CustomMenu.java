package com.mcal.worldcraft.entity_menu;

import com.mcal.droid.rugl.Game;
import com.mcal.droid.rugl.geom.ColouredShape;
import com.mcal.droid.rugl.geom.Shape;
import com.mcal.droid.rugl.geom.ShapeUtil;
import com.mcal.droid.rugl.gl.StackedRenderer;
import com.mcal.droid.rugl.input.TapPad;
import com.mcal.droid.rugl.input.Touch;
import com.mcal.droid.rugl.util.Colour;
import com.mcal.droid.rugl.util.geom.BoundingRectangle;
import com.mcal.worldcraft.chunk.tile_entity.Inventory;
import com.mcal.worldcraft.ui.CustomTapPad;
import com.mcal.worldcraft.ui.GUI;

public abstract class CustomMenu implements Touch.TouchListener {
    protected static final float RATIO_Y = Game.screenHeight / Game.gameHeight;
    protected static final float RATIO_X = Game.screenWidth / Game.gameWidth;
    private final int innerColour = Colour.packInt(148, 134, 123, 255);
    private final int boundsColour = Colour.packFloat(0.0f, 0.0f, 0.0f, 0.8f);
    public BoundingRectangle bounds = new BoundingRectangle(0.0f, 0.0f, Game.gameWidth, Game.gameHeight);
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
    protected CustomTapPad exitTap = new CustomTapPad(Game.gameWidth - 68.0f, Game.gameHeight - 68.0f, 60.0f, 60.0f, GUI.getFont(), "X");
    private ColouredShape boundShape;
    private ColouredShape innerShape;

    public CustomMenu(Inventory inventory) {
        this.inventory = inventory;
        exitTap.listener = exitTapListener;
    }

    public void draw(StackedRenderer sr) {
        if (show) {
            drawInnerBound(sr);
            drawBound(sr);
            sr.render();
        }
    }

    protected void drawInnerBound(StackedRenderer sr) {
        if (innerShape == null) {
            Shape is = ShapeUtil.innerQuad(bounds.x.getMin(), bounds.y.getMin(), bounds.x.getMax(), bounds.y.getMax(), bounds.y.getSpan(), 0.0f);
            innerShape = new ColouredShape(is, innerColour, null);
        }
        innerShape.render(sr);
    }

    protected void drawBound(StackedRenderer sr) {
        if (boundShape == null) {
            Shape bs = ShapeUtil.innerQuad(bounds.x.getMin(), bounds.y.getMin(), bounds.x.getMax(), bounds.y.getMax(), 8.0f, 0.0f);
            boundShape = new ColouredShape(bs, boundsColour, null);
        }
        boundShape.render(sr);
    }

    public boolean isVisible() {
        return show;
    }

    public void setShow(boolean isShow) {
        show = isShow;
    }
}
