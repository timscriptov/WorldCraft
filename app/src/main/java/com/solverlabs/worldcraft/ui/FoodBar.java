package com.solverlabs.worldcraft.ui;

import com.solverlabs.droid.rugl.geom.TexturedShape;
import com.solverlabs.droid.rugl.gl.StackedRenderer;
import com.solverlabs.droid.rugl.gl.State;
import com.solverlabs.droid.rugl.util.geom.BoundingRectangle;
import com.solverlabs.worldcraft.Player;
import com.solverlabs.worldcraft.factories.ItemFactory;

public class FoodBar {
    public static final int BAR_ITEMS = 10;
    public static final int MARGIN = 10;
    private static final TexturedShape[] BAR_ITEM_SHAPES = new TexturedShape[3];
    private static final int FOOD_LEVELS_PER_BAR_ITEM = 2;
    private static final float SCALE_VALUE = 25.0f;
    private final BoundingRectangle bounds = new BoundingRectangle(400.0f, 65.0f, 320.0f, 80.0f);
    private final Player player;

    public FoodBar(Player player) {
        this.player = player;
    }

    public static void initShapes(State itemState) {
        for (int i = 0; i < BAR_ITEM_SHAPES.length; i++) {
            BAR_ITEM_SHAPES[i] = ItemFactory.Item.getShape(9 - i, 14);
            BAR_ITEM_SHAPES[i].scale(SCALE_VALUE, SCALE_VALUE, SCALE_VALUE);
        }
    }

    public void advance(float delta) {
    }

    public void draw(StackedRenderer sr) {
        float offset = (-(this.bounds.x.getSpan() - 10.0f)) / 12.0f;
        float yPos = this.bounds.y.toValue(0.5f);
        float xPos = this.bounds.x.getMax() - ((10.0f + offset) / 2.0f);
        int foodLevel = this.player.getFoodLevel();
        for (int i = 0; i < 10; i++) {
            sr.pushMatrix();
            sr.translate(xPos, yPos, 0.0f);
            BAR_ITEM_SHAPES[Math.min(2, foodLevel)].render(sr);
            sr.popMatrix();
            sr.render();
            foodLevel = Math.max(foodLevel - 2, 0);
            xPos += offset;
        }
    }
}
