package com.mcal.worldcraft.mob;

import androidx.annotation.NonNull;

import com.mcal.droid.rugl.geom.ShapeBuilder;
import com.mcal.droid.rugl.geom.TexturedShape;
import com.mcal.droid.rugl.gl.Renderer;
import com.mcal.droid.rugl.gl.State;
import com.mcal.droid.rugl.util.Colour;
import com.mcal.worldcraft.skin.geometry.Parallelepiped;

public class MobTexturePack {
    protected static final float STXTN = 0.0078125f;
    private static final int DEFAULT_COLOR = Colour.packFloat(0.5f, 0.5f, 0.5f, 1.0f);
    private static final int SELECTED_COLOR = Colour.packFloat(0.8f, 0.8f, 0.8f, 1.0f);
    private static final int ATTACKED_COLOR = Colour.packFloat(1.0f, 0.6f, 0.6f, 1.0f);
    private final Mob mob;
    private final MobSize mobSize;
    private final State state;
    private TexturedShape body;
    private TexturedShape head;
    private TexturedShape leftHand;
    private TexturedShape leftLeg;
    private float light;
    private TexturedShape rightHand;
    private TexturedShape rightLeg;

    public MobTexturePack(@NonNull Mob mob, State state) {
        this.mob = mob;
        this.mobSize = new MobSize(mob.getSize());
        this.state = state;
    }

    private static void addFace(@NonNull Parallelepiped facing, float x, float y, float z, Parallelepiped.Face f, float width, float height, int colour, ShapeBuilder shapBuilder) {
        facing.face(f, x, y, z, width, height, colour, shapBuilder, true);
    }

    public ShapeBuilder createShapeBuilder(Parallelepiped p, int width, int height, int depth, int color) {
        ShapeBuilder shapeBuilder = new ShapeBuilder();
        shapeBuilder.clear();
        float zoom = this.mob.getSize().getZoom();
        addFace(p, width * (-0.03125f) * zoom, 0.0f, 0.0f, p.south, depth, height, color, shapeBuilder);
        addFace(p, width * 0.03125f * zoom, 0.0f, 0.0f, p.north, depth, height, color, shapeBuilder);
        addFace(p, 0.0f, 0.0f, depth * (-0.03125f) * zoom, p.west, width, height, color, shapeBuilder);
        addFace(p, 0.0f, 0.0f, depth * 0.03125f * zoom, p.east, width, height, color, shapeBuilder);
        addFace(p, 0.0f, 0.0f, 0.0f, p.bottom, width, depth, color, shapeBuilder);
        addFace(p, 0.0f, 0.0f, 0.0f, p.top, width, depth, color, shapeBuilder);
        return shapeBuilder;
    }

    public void shiftTc(int xOffset, int yOffset) {
        this.mobSize.shiftTc(xOffset, yOffset);
    }

    public void setLight(float light) {
        if (this.light != light) {
            this.light = light;
            invalidateShapes();
        }
    }

    public boolean initShapes(int state) {
        if (this.head == null && this.mob.isVisible()) {
            this.head = createHeadShape();
            this.body = createBodyShape();
            this.rightHand = createHandShape();
            this.leftHand = createHandShape();
            this.rightLeg = createLegShape();
            this.leftLeg = createLegShape();
            return true;
        }
        return false;
    }

    public boolean render(Renderer r) {
        if (this.head != null) {
            this.head.render(r);
        }
        if (this.body != null) {
            this.body.render(r);
        }
        if (this.leftHand != null) {
            this.leftHand.render(r);
        }
        if (this.rightHand != null) {
            this.rightHand.render(r);
        }
        if (this.leftLeg != null) {
            this.leftLeg.render(r);
        }
        if (this.rightLeg != null) {
            this.rightLeg.render(r);
            return true;
        }
        return true;
    }

    public void invalidateShapes() {
        this.head = null;
        this.body = null;
        this.rightHand = null;
        this.leftHand = null;
        this.rightLeg = null;
        this.leftLeg = null;
    }

    public boolean isShapesInvalidated() {
        return this.head == null;
    }

    public TexturedShape getHead() {
        return this.head;
    }

    public TexturedShape getBody() {
        return this.body;
    }

    public TexturedShape getRightHand() {
        return this.rightHand;
    }

    public TexturedShape getLeftHand() {
        return this.leftHand;
    }

    public TexturedShape getRightLeg() {
        return this.rightLeg;
    }

    public TexturedShape getLeftLeg() {
        return this.leftLeg;
    }

    public float getStxtn() {
        return STXTN;
    }

    public State getState() {
        return this.state;
    }

    public int getColor() {
        if (this.mob != null) {
            if (this.mob.isAttackedRecently()) {
                return Colour.withLightf(ATTACKED_COLOR, this.light);
            }
            if (this.mob.isSelected()) {
                return Colour.withLightf(SELECTED_COLOR, this.light);
            }
        }
        return Colour.withLightf(DEFAULT_COLOR, this.light);
    }

    @NonNull
    private TexturedShape createHeadShape() {
        TexturedBlockProperties headBlock = this.mobSize.getHeadBlockProperties();
        Parallelepiped headSkin = Parallelepiped.createParallelepiped(this.mobSize.getHeadWidth(), this.mobSize.getHeadHeight(), this.mobSize.getHeadDepth(), STXTN, headBlock.getTc());
        ShapeBuilder headShapeBuilder = createShapeBuilder(headSkin, headBlock.getWidth(), headBlock.getHeight(), headBlock.getDepth(), getColor());
        TexturedShape s = headShapeBuilder.compile();
        s.state = this.state;
        s.translate(this.mobSize.getHeadX(), this.mobSize.getHeadY(), this.mobSize.getHeadZ());
        s.backup();
        return s;
    }

    @NonNull
    private TexturedShape createBodyShape() {
        TexturedBlockProperties bodyBlock = this.mobSize.getBodyBlockProperties();
        Parallelepiped bodySkin = Parallelepiped.createParallelepiped(this.mobSize.getBodyWidth(), this.mobSize.getBodyHeight(), this.mobSize.getBodyDepth(), STXTN, bodyBlock.getTc());
        ShapeBuilder bodyShapeBuilder = createShapeBuilder(bodySkin, bodyBlock.getWidth(), bodyBlock.getHeight(), bodyBlock.getDepth(), getColor());
        TexturedShape s = bodyShapeBuilder.compile();
        s.state = this.state;
        s.translate(this.mobSize.getBodyX(), this.mobSize.getBodyY(), this.mobSize.getBodyZ());
        s.backup();
        return s;
    }

    @NonNull
    private TexturedShape createHandShape() {
        TexturedBlockProperties handBlock = this.mobSize.getHandBlockProperties();
        Parallelepiped handSkin = Parallelepiped.createParallelepiped(this.mobSize.getHandWidth(), this.mobSize.getHandHeight(), this.mobSize.getHandDepth(), STXTN, handBlock.getTc());
        ShapeBuilder handShapeBuilder = createShapeBuilder(handSkin, handBlock.getWidth(), handBlock.getHeight(), handBlock.getDepth(), getColor());
        TexturedShape s = handShapeBuilder.compile();
        s.state = this.state;
        s.translate(0.0f, this.mobSize.getHandY(), 0.0f);
        if (this.mob.getSize().getStartHandsAngle() != 0.0f) {
            s.translate(0.0f, -this.mobSize.getHandActionYOffset(), -this.mobSize.getHandLegZOffset());
            s.rotateXByOne(this.mob.getSize().getStartHandsAngle());
            s.translate(0.0f, this.mobSize.getHandActionYOffset(), this.mobSize.getHandLegZOffset());
            s.translate(0.0f, 0.05f, 0.125f);
        }
        s.backup();
        return s;
    }

    @NonNull
    private TexturedShape createLegShape() {
        TexturedBlockProperties legBlock = this.mobSize.getLegBlockProperties();
        Parallelepiped legSkin = Parallelepiped.createParallelepiped(this.mobSize.getLegWidth(), this.mobSize.getLegHeight(), this.mobSize.getLegDepth(), STXTN, legBlock.getTc());
        ShapeBuilder legShapeBuilder = createShapeBuilder(legSkin, legBlock.getWidth(), legBlock.getHeight(), legBlock.getDepth(), getColor());
        TexturedShape s = legShapeBuilder.compile();
        s.state = this.state;
        s.translate(0.0f, this.mobSize.getLegY(), 0.0f);
        s.backup();
        return s;
    }
}
