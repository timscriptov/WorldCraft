package com.solverlabs.worldcraft.skin.geometry.generator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.solverlabs.droid.rugl.geom.ShapeBuilder;
import com.solverlabs.droid.rugl.geom.TexturedShape;
import com.solverlabs.droid.rugl.util.Colour;
import com.solverlabs.worldcraft.CharactersPainter;
import com.solverlabs.worldcraft.SkinFactory;
import com.solverlabs.worldcraft.skin.geometry.Parallelepiped;


public class SkinGeometryGenerator {
    public static final float BLOCK_SIZE = 0.125f;
    public static final float HALF_OF_PI = 1.5707964f;
    public static final int HAND_HEIGHT = 3;
    public static final int LEG_HEIGHT = 3;
    public static final float ONE_AND_HALF_OF_PI = 4.712389f;
    public static final int SKIN_COUNT = 20;
    public static final float ZOOM = 1.5f;
    private static final int BLOCKS_IN_TEXTURE = 16;
    private static final int BODY_DEPTH = 1;
    private static final int BODY_HEIGHT = 3;
    private static final int BODY_WIDTH = 2;
    private static final int HAND_DEPTH = 1;
    private static final int HAND_WIDTH = 1;
    private static final int HEAD_DEPTH = 2;
    private static final int HEAD_HEIGHT = 2;
    private static final int HEAD_WIDTH = 2;
    private static final int LEG_DEPTH = 1;
    private static final int LEG_WIDTH = 1;
    private static final float STXTN = 0.015625f;
    private static final int TEXTURE_COORD_COUNT = 12;
    private static final int COLOR = Colour.packFloat(0.5f, 0.5f, 0.5f, 1.0f);
    private static final int[] headTc = {4, 2, 0, 2, 2, 2, 6, 2, 2, 0, 4, 0};
    private static final int[] bodyTc = {9, 5, 4, 5, 5, 5, 7, 5, 5, 4, 7, 4};
    private static final int[] handTc = {13, 5, 10, 5, 11, 5, 12, 5, 11, 4, 12, 4};
    private static final int[] legTc = {3, 5, 0, 5, 1, 5, 2, 5, 1, 4, 2, 4};
    private static TexturedShape[] bodyShape;
    private static TexturedShape[] handShape;
    private static TexturedShape[] headShape;
    private static TexturedShape[] legShape;
    private static TexturedShape playerHandShape;

    public static void init() {
        headShape = new TexturedShape[20];
        bodyShape = new TexturedShape[20];
        handShape = new TexturedShape[20];
        legShape = new TexturedShape[20];
        for (int i = 0; i < 20; i++) {
            int[] tcTempHead = new int[12];
            int[] tcTempBody = new int[12];
            int[] tcTempHand = new int[12];
            int[] tcTempLeg = new int[legTc.length];
            for (int j = 0; j < headTc.length; j += 2) {
                tcTempHead[j] = headTc[j] + ((i % 4) * 16);
                tcTempHead[j + 1] = headTc[j + 1] + ((i / 4) * 8);
                tcTempBody[j] = bodyTc[j] + ((i % 4) * 16);
                tcTempBody[j + 1] = bodyTc[j + 1] + ((i / 4) * 8);
                tcTempHand[j] = handTc[j] + ((i % 4) * 16);
                tcTempHand[j + 1] = handTc[j + 1] + ((i / 4) * 8);
                tcTempLeg[j] = legTc[j] + ((i % 4) * 16);
                tcTempLeg[j + 1] = legTc[j + 1] + ((i / 4) * 8);
            }
            Parallelepiped headSkin = Parallelepiped.createParallelepiped(0.375f, 0.375f, 0.375f, STXTN, tcTempHead);
            Parallelepiped bodySkin = Parallelepiped.createParallelepiped(0.375f, 0.5625f, 0.1875f, STXTN, tcTempBody);
            Parallelepiped handSkin = Parallelepiped.createParallelepiped(0.1875f, 0.5625f, 0.1875f, STXTN, tcTempHand);
            Parallelepiped legSkin = Parallelepiped.createParallelepiped(0.1875f, 0.5625f, 0.1875f, STXTN, tcTempLeg);
            ShapeBuilder headShapeBuilder = createShapeBuilder(headSkin, 2, 2, 2, COLOR);
            ShapeBuilder bodyShapeBuilder = createShapeBuilder(bodySkin, 2, 3, 1, COLOR);
            ShapeBuilder handShapeBuilder = createShapeBuilder(handSkin, 1, 3, 1, COLOR);
            ShapeBuilder legShapeBuilder = createShapeBuilder(legSkin, 1, 3, 1, COLOR);
            headShape[i] = headShapeBuilder.compile();
            bodyShape[i] = bodyShapeBuilder.compile();
            handShape[i] = handShapeBuilder.compile();
            legShape[i] = legShapeBuilder.compile();
            headShape[i].state = SkinFactory.state;
            bodyShape[i].state = SkinFactory.state;
            handShape[i].state = SkinFactory.state;
            legShape[i].state = SkinFactory.state;
        }
        CharactersPainter.loadFont();
    }

    public static TexturedShape getHandShape() {
        if (playerHandShape == null) {
            Parallelepiped handSkin = Parallelepiped.createParallelepiped(0.1875f, 0.5625f, 0.1875f, STXTN, handTc);
            ShapeBuilder handShapeBuilder = createShapeBuilder(handSkin, 1, 3, 1, COLOR);
            playerHandShape = handShapeBuilder.compile();
            playerHandShape.scale(1.0f, 2.0f, 1.0f);
            playerHandShape.rotate(3.8f, 1.0f, 0.0f, 0.0f);
            playerHandShape.rotate(40.4f, 0.0f, 1.0f, 0.0f);
            playerHandShape.rotate(0.6f, 0.0f, 0.0f, 1.0f);
            playerHandShape.scale(1.3f, 1.3f, 1.3f);
            playerHandShape.translate(0.0f, 0.2f, -1.0f);
        }
        return playerHandShape;
    }

    public static TexturedShape getHead(int skin) {
        return headShape[skin % 20];
    }

    public static TexturedShape createHeadShape(int skin) {
        if (headShape == null) {
            return null;
        }
        TexturedShape s = (TexturedShape) headShape[skin % 20].clone();
        s.translate(-0.1875f, -0.1875f, -0.1875f);
        s.backup();
        return s;
    }

    public static TexturedShape createBodyShape(int skin) {
        if (bodyShape == null) {
            return null;
        }
        TexturedShape s = (TexturedShape) bodyShape[skin % 20].clone();
        s.translate(-0.1875f, -0.5625f, -0.09375f);
        s.backup();
        return s;
    }

    @Nullable
    public static TexturedShape createHandShape(int skin) {
        if (handShape == null) {
            return null;
        }
        TexturedShape s = (TexturedShape) handShape[skin % 20].clone();
        s.backup();
        return s;
    }

    @Nullable
    public static TexturedShape createLegShape(int skin) {
        if (legShape == null) {
            return null;
        }
        TexturedShape s = (TexturedShape) legShape[skin % 20].clone();
        s.backup();
        return s;
    }

    @NonNull
    private static ShapeBuilder createShapeBuilder(Parallelepiped p, int width, int height, int depth, int color) {
        ShapeBuilder shapeBuilder = new ShapeBuilder();
        shapeBuilder.clear();
        addFace(p, (-0.1875f) * width, 0.0f, 0.0f, p.south, depth, height, color, shapeBuilder);
        addFace(p, 0.1875f * width, 0.0f, 0.0f, p.north, depth, height, color, shapeBuilder);
        addFace(p, 0.0f, 0.0f, (-0.1875f) * depth, p.west, width, height, color, shapeBuilder);
        addFace(p, 0.0f, 0.0f, 0.1875f * depth, p.east, width, height, color, shapeBuilder);
        addFace(p, 0.0f, 0.0f, 0.0f, p.bottom, width, depth, color, shapeBuilder);
        addFace(p, 0.0f, 0.0f, 0.0f, p.top, width, depth, color, shapeBuilder);
        return shapeBuilder;
    }

    private static void addFace(Parallelepiped facing, float x, float y, float z, Parallelepiped.Face f, float width, float height, int colour, ShapeBuilder shapBuilder) {
        facing.face(f, x, y, z, width, height, colour, shapBuilder);
    }
}
