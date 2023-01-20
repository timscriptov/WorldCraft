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
    private TextShape chatShape;
    private long createdAt;
    private float currentChatShapeAlpha;
    private float currentStep;
    private Font font;
    private float height;
    private boolean hided;
    private ColouredShape outlineBlack;
    private int position;
    private final String title;
    private float x;
    private float y;

    public ChatMessage(String title, float x, float y, float width, float height, Font font) {
        super(x, y, width, height);
        this.currentChatShapeAlpha = TEXT_COLOR_A;
        this.hided = false;
        this.title = title;
        if (font != null) {
            this.font = font;
            this.x = x;
            this.y = y;
            this.height = height;
            regenerateChatShape(0.0f);
            this.createdAt = System.currentTimeMillis();
        }
    }

    public void advance(float delta) {
        this.currentStep = (delta / FADE_OUT_DURATION) * 255.0f;
    }

    private float convertX(float x) {
        return 10.0f + x;
    }

    public void update(int position) {
        this.position = position;
        regenerateChatShape(0.0f);
        synchronized (this) {
            this.outlineBlack = null;
        }
    }

    private void regenerateChatShape(float step) {
        this.currentChatShapeAlpha -= Math.min(step, this.currentChatShapeAlpha);
        if (this.currentChatShapeAlpha <= 0.0f) {
            this.currentChatShapeAlpha = 0.0f;
            this.hided = true;
        }
        this.chatShape = this.font.buildTextShape(this.title, Colour.packFloat(1.0f, 1.0f, 1.0f, this.currentChatShapeAlpha));
        this.chatShape.scale(TEXT_SCALE, TEXT_SCALE, TEXT_SCALE);
        this.chatShape.translate(convertX(this.x), ((this.y + this.height) - this.font.size) + (this.position * (this.height - 5.0f)), 0.0f);
        float updatedPositionY = this.y + (this.position * (this.height - 5.0f));
        this.pad.y.set(updatedPositionY, (this.pad.y.getMax() - this.pad.y.getMin()) + updatedPositionY);
    }

    @Override
    public void draw(StackedRenderer sr) {
        if (!this.hided && this.chatShape != null) {
            synchronized (this) {
                if (this.outlineBlack == null) {
                    this.outlineBlack = new ColouredShape(ShapeUtil.innerQuad(this.pad.x.getMin() + OUTLINE_BORDER_WIDTH, this.pad.y.getMin() + OUTLINE_BORDER_WIDTH, this.pad.x.getMax() - OUTLINE_BORDER_WIDTH, this.pad.y.getMax() - OUTLINE_BORDER_WIDTH, this.pad.x.getSpan(), 0.0f), Colour.packFloat(0.0f, 0.0f, 0.0f, 0.3f), GLUtil.typicalState);
                }
                if (System.currentTimeMillis() - this.createdAt > TTL) {
                    regenerateChatShape(this.currentStep / 100.0f);
                    for (int i = 0; i < this.outlineBlack.colours.length; i++) {
                        if (this.outlineBlack.colours[i] > 0) {
                            int alpha = (this.outlineBlack.colours[i] & (-16777216)) >> 24;
                            this.outlineBlack.colours[i] = (this.outlineBlack.colours[i] & 16777215) | (((int) (alpha - Math.min(this.currentStep, alpha))) << 24);
                        }
                    }
                }
                this.outlineBlack.render(sr);
                this.chatShape.render(sr);
            }
        }
    }
}
