package com.solverlabs.worldcraft.ui;

import com.solverlabs.droid.rugl.geom.TexturedShape;
import com.solverlabs.droid.rugl.gl.StackedRenderer;
import com.solverlabs.droid.rugl.gl.State;
import com.solverlabs.droid.rugl.util.geom.BoundingRectangle;
import com.solverlabs.worldcraft.Player;
import com.solverlabs.worldcraft.factories.ItemFactory;

public class HealthBar {
    private static final int EMPTY_HEART = 0;
    private static final int FULL_HEART = 2;
    private static final int HALF_HEART = 1;
    private static final int HEART_COUNT = 10;
    private static final float SCALE_VALUE = 50.0f;
    private static TexturedShape emptyHeartActiveShape;
    private static TexturedShape emptyHeartShape;
    private static TexturedShape fullHeartActiveShape;
    private static TexturedShape fullHeartShape;
    private static TexturedShape halfHeartActiveShape;
    private static TexturedShape halfHeartShape;
    private final BoundingRectangle bounds = new BoundingRectangle(150.0f, 70.0f, 300.0f, 80.0f);
    private final Player player;

    public HealthBar(Player player) {
        this.player = player;
    }

    public static void initShapes(State itemState) {
        fullHeartActiveShape = ItemFactory.Item.getShape(10, 14);
        halfHeartActiveShape = ItemFactory.Item.getShape(11, 14);
        emptyHeartActiveShape = ItemFactory.Item.getShape(12, 14);
        fullHeartShape = ItemFactory.Item.getShape(13, 14);
        halfHeartShape = ItemFactory.Item.getShape(14, 14);
        emptyHeartShape = ItemFactory.Item.getShape(15, 14);
        fullHeartActiveShape.scale(SCALE_VALUE, SCALE_VALUE, SCALE_VALUE);
        halfHeartActiveShape.scale(SCALE_VALUE, SCALE_VALUE, SCALE_VALUE);
        emptyHeartActiveShape.scale(SCALE_VALUE, SCALE_VALUE, SCALE_VALUE);
        fullHeartShape.scale(SCALE_VALUE, SCALE_VALUE, SCALE_VALUE);
        halfHeartShape.scale(SCALE_VALUE, SCALE_VALUE, SCALE_VALUE);
        emptyHeartShape.scale(SCALE_VALUE, SCALE_VALUE, SCALE_VALUE);
    }

    public void advance(float delta) {
    }

    public void draw(StackedRenderer sr) {
        byte[] playerHearts = HeartTypesProcessor.getHeartTypes(this.player.getHealthPoints());
        float offset = (bounds.x.getSpan() - 10.0f) / 12.0f;
        float yPos = bounds.y.toValue(0.5f);
        float xPos = (bounds.x.getMin() + 5.0f) - (offset / 2.0f);
        for (byte b : playerHearts) {
            xPos += offset;
            sr.pushMatrix();
            sr.translate(xPos, yPos, 0.0f);
            getHeartTexture(b).render(sr);
            sr.popMatrix();
            sr.render();
        }
    }

    private TexturedShape getHeartTexture(byte heartType) {
        if (this.player.isHealthJustUpdated()) {
            return getHeartTextureActive(heartType);
        }
        return getHeartTextureRest(heartType);
    }

    private TexturedShape getHeartTextureRest(byte heartType) {
        switch (heartType) {
            case HALF_HEART:
                return halfHeartShape;
            case FULL_HEART:
                return fullHeartShape;
            default: // EMPTY_HEART
                return emptyHeartShape;
        }
    }

    private TexturedShape getHeartTextureActive(byte heartType) {
        switch (heartType) {
            case HALF_HEART:
                return halfHeartActiveShape;
            case FULL_HEART:
                return fullHeartActiveShape;
            default: // EMPTY_HEART
                return emptyHeartActiveShape;
        }
    }

    public static class HeartTypesProcessor {
        private static byte[] heartTypes;
        private static int prevHealthPoints = -1;

        private HeartTypesProcessor() {
        }

        public static byte[] getHeartTypes(int healthPoints) {
            if (prevHealthPoints == healthPoints) {
                return heartTypes;
            }
            heartTypes = new byte[10];
            int tempHealth = healthPoints;
            for (int i = 0; i < 10; i++) {
                if (tempHealth >= 2) {
                    heartTypes[i] = 2;
                    tempHealth -= 2;
                } else if (tempHealth == 1) {
                    heartTypes[i] = 1;
                    tempHealth--;
                } else {
                    heartTypes[i] = 0;
                }
            }
            prevHealthPoints = healthPoints;
            return heartTypes;
        }
    }
}
