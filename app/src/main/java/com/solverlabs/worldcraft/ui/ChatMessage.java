package com.solverlabs.worldcraft.ui;

import com.solverlabs.droid.rugl.geom.ColouredShape;
import com.solverlabs.droid.rugl.geom.ShapeUtil;
import com.solverlabs.droid.rugl.gl.GLUtil;
import com.solverlabs.droid.rugl.gl.StackedRenderer;
import com.solverlabs.droid.rugl.input.TapPad;
import com.solverlabs.droid.rugl.text.Font;
import com.solverlabs.droid.rugl.text.TextShape;
import com.solverlabs.droid.rugl.util.Colour;

public class ChatMessage extends TapPad {
    private static final float FADE_OUT_DURATION = 0.6f;
    private static final float OUTLINE_BORDER_WIDTH = 2.5f;
    private static final float TEXT_COLOR_A = 0.8f;
    private static final float TEXT_COLOR_B = 1.0f;
    private static final float TEXT_COLOR_G = 1.0f;
    private static final float TEXT_COLOR_R = 1.0f;
    private static final float TEXT_SCALE = 0.65f;
    private static final long TTL = 10000;
    private final String title;
    private TextShape chatShape;
    private long createdAt;
    private float currentChatShapeAlpha;
    private float currentStep;
    private Font font;
    private float height;
    private boolean hided;
    private ColouredShape outlineBlack;
    private int position;
    private float x;
    private float y;

    public ChatMessage(String title, float x, float y, float width, float height, Font font) {
        super(x, y, width, height);
        currentChatShapeAlpha = TEXT_COLOR_A;
        this.hided = false;
        this.title = title;
        if (font != null) {
            this.font = font;
            this.x = x;
            this.y = y;
            this.height = height;
            regenerateChatShape(0.0f);
            createdAt = System.currentTimeMillis();
        }
    }

    public void advance(float delta) {
        currentStep = (delta / FADE_OUT_DURATION) * 255.0f;
    }

    private float convertX(float x) {
        return 10.0f + x;
    }

    public void update(int position) {
        this.position = position;
        regenerateChatShape(0.0f);
        synchronized (this) {
            outlineBlack = null;
        }
    }

    private void regenerateChatShape(float step) {
        currentChatShapeAlpha -= Math.min(step, currentChatShapeAlpha);
        if (currentChatShapeAlpha <= 0.0f) {
            currentChatShapeAlpha = 0.0f;
            hided = true;
        }
        chatShape = font.buildTextShape(title, Colour.packFloat(1.0f, 1.0f, 1.0f, currentChatShapeAlpha));
        chatShape.scale(TEXT_SCALE, TEXT_SCALE, TEXT_SCALE);
        chatShape.translate(convertX(x), ((y + height) - font.size) + (position * (height - 5.0f)), 0.0f);
        float updatedPositionY = y + (position * (height - 5.0f));
        pad.y.set(updatedPositionY, (pad.y.getMax() - pad.y.getMin()) + updatedPositionY);
    }

    @Override
    public void draw(StackedRenderer sr) {
        if (!hided && chatShape != null) {
            synchronized (this) {
                if (outlineBlack == null) {
                    outlineBlack = new ColouredShape(ShapeUtil.innerQuad(pad.x.getMin() + OUTLINE_BORDER_WIDTH, pad.y.getMin() + OUTLINE_BORDER_WIDTH, pad.x.getMax() - OUTLINE_BORDER_WIDTH, pad.y.getMax() - OUTLINE_BORDER_WIDTH, pad.x.getSpan(), 0.0f), Colour.packFloat(0.0f, 0.0f, 0.0f, 0.3f), GLUtil.typicalState);
                }
                if (System.currentTimeMillis() - createdAt > TTL) {
                    regenerateChatShape(currentStep / 100.0f);
                    for (int i = 0; i < outlineBlack.colours.length; i++) {
                        if (outlineBlack.colours[i] > 0) {
                            int alpha = (outlineBlack.colours[i] & (-16777216)) >> 24;
                            outlineBlack.colours[i] = (outlineBlack.colours[i] & 16777215) | (((int) (alpha - Math.min(currentStep, alpha))) << 24);
                        }
                    }
                }
                outlineBlack.render(sr);
                chatShape.render(sr);
            }
        }
    }
}
