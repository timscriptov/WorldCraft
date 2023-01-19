package com.solverlabs.worldcraft;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.Game;
import com.solverlabs.droid.rugl.geom.ColouredShape;
import com.solverlabs.droid.rugl.geom.Shape;
import com.solverlabs.droid.rugl.geom.ShapeUtil;
import com.solverlabs.droid.rugl.geom.TexturedShape;
import com.solverlabs.droid.rugl.gl.GLUtil;
import com.solverlabs.droid.rugl.gl.StackedRenderer;
import com.solverlabs.droid.rugl.gl.State;
import com.solverlabs.droid.rugl.gl.enums.MagFilter;
import com.solverlabs.droid.rugl.gl.enums.MinFilter;
import com.solverlabs.droid.rugl.util.Colour;
import com.solverlabs.droid.rugl.util.FPSCamera;
import com.solverlabs.droid.rugl.util.Trig;
import com.solverlabs.droid.rugl.util.math.Range;
import com.solverlabs.worldcraft.factories.BlockFactory;
import com.solverlabs.worldcraft.factories.ItemFactory;
import com.solverlabs.worldcraft.util.RandomUtil;

import java.util.Random;


public class BlockParticle {
    private static final float sxtn = 0.0625f;
    private static final long ACTIVE_PERIOD = 400;
    private static final int MAX_TIME_DELAY = 500;
    private static final int[] EXPLOSION_SHAPE_COORDS = {2, 11, 3, 11, 4, 11, 5, 11, 2, 12, 3, 12, 4, 12, 5, 12, 2, 13, 3, 13, 4, 13, 5, 13, 2, 14, 3, 14, 4, 14, 5, 14};
    private static final Random random = new Random(System.currentTimeMillis());
    private static ColouredShape blockColouredShape = null;
    private static State blockState = null;
    private static ColouredShape itemColouredShape = null;
    private static State itemState = null;
    private final BlockFactory.WorldSide mBlockSide;
    private final long createTime;
    private final int horizontalOffsetSign;
    float[] itc;
    private boolean mIsExplosion;
    private boolean isOrtho2D;
    private boolean moveUp;
    private float scale;
    private long timeDelay;
    private TexturedShape ts;
    private float mX;
    private float xOffset;
    private float mY;
    private float yOffset;
    private float mZ;
    private float zOffset;

    public BlockParticle(byte blockID, float x, float y, float z, BlockFactory.WorldSide blockSide) {
        this(blockID, x, y, z, blockSide, false);
    }

    public BlockParticle(byte blockID, float x, float y, float z, BlockFactory.WorldSide blockSide, boolean isExplosion) {
        int i = 1;
        itc = ShapeUtil.getQuadTexCoords(1);
        moveUp = true;
        isOrtho2D = false;
        mIsExplosion = false;
        blockColouredShape.reset();
        itemColouredShape.reset();
        mIsExplosion = isExplosion;
        createTime = System.currentTimeMillis();
        mX = x + 0.5f;
        mY = y + 0.5f + Range.toValue(random.nextFloat(), -0.4f, 0.4f);
        mZ = z + 0.5f;
        mBlockSide = blockSide;
        initPosition(blockSide);
        initOffsets(blockID);
        isOrtho2D = blockSide == BlockFactory.WorldSide.Empty;
        BlockFactory.Block b = BlockFactory.getBlock(blockID);
        ItemFactory.Item item = ItemFactory.Item.getItemByID(blockID);
        if (item != null) {
            faceTexCoords(itc, item);
        }
        if (b != null && !isExplosion) {
            ts = new TexturedShape(blockColouredShape, itc, BlockFactory.texture);
        } else {
            ts = new TexturedShape(itemColouredShape, itc, ItemFactory.itemTexture);
        }
        horizontalOffsetSign = !random.nextBoolean() ? -1 : i;
    }

    public static void init() {
        blockState = GLUtil.typicalState.with(MinFilter.NEAREST, MagFilter.NEAREST);
        blockState = BlockFactory.texture.applyTo(blockState);
        itemState = GLUtil.typicalState.with(MinFilter.NEAREST, MagFilter.NEAREST);
        itemState = BlockFactory.texture.applyTo(itemState);
        Shape shape = ShapeUtil.filledQuad(0.0f, 0.0f, 1.0f, 1.0f, 0.0f);
        blockColouredShape = new ColouredShape(shape, Colour.packFloat(0.9f, 0.9f, 0.9f, 0.9f), blockState);
        itemColouredShape = new ColouredShape(shape, Colour.packFloat(0.9f, 0.9f, 0.9f, 0.9f), itemState);
    }

    private void initOffsets(byte blockID) {
        if (!mIsExplosion) {
            scale = RandomUtil.getRandomInRangeExclusive(0.05f, 0.15f);
            zOffset = RandomUtil.getRandomInRangeExclusive(-0.5f, 0.5f);
            xOffset = RandomUtil.getRandomInRangeExclusive(-0.5f, 0.5f);
            return;
        }
        scale = RandomUtil.getRandomInRangeExclusive(0.75f, 3.0f);
        zOffset = RandomUtil.getRandomInRangeExclusive(-3.0f, 3.0f);
        xOffset = RandomUtil.getRandomInRangeExclusive(-3.0f, 3.0f);
        yOffset = RandomUtil.getRandomInRangeExclusive(-3.0f, 2.0f);
        timeDelay = RandomUtil.getRandomInRangeInclusive(0, MAX_TIME_DELAY);
    }

    private void faceTexCoords(float[] tc, @NonNull ItemFactory.Item item) {
        int[] blockTexCoords;
        BlockFactory.Block b = BlockFactory.getBlock(item.id);
        if (mIsExplosion) {
            blockTexCoords = EXPLOSION_SHAPE_COORDS;
        } else if (b != null) {
            blockTexCoords = b.texCoords;
            if (b == BlockFactory.Block.DirtWithGrass) {
                blockTexCoords = BlockFactory.Block.Dirt.texCoords;
            }
            if (b == BlockFactory.Block.Bed || b == BlockFactory.Block.Ladder || b == BlockFactory.Block.CraftingTable || b == BlockFactory.Block.Chest) {
                blockTexCoords = BlockFactory.Block.WoodenPlanks.texCoords;
            }
            if (b == BlockFactory.Block.Furnace || b == BlockFactory.Block.FurnaceActive) {
                blockTexCoords = BlockFactory.Block.Stone.texCoords;
            }
        } else {
            blockTexCoords = item.getTexCoords();
        }
        calcTextureOffset(tc, blockTexCoords);
    }

    private void calcTextureOffset(@NonNull float[] tc, @NonNull int[] blockTexCoords) {
        float bu = sxtn * blockTexCoords[0];
        float bv = sxtn * (blockTexCoords[1] + 1);
        float tu = sxtn * (blockTexCoords[0] + 1);
        float tv = sxtn * blockTexCoords[1];
        int index = 1;
        tc[0] = bu;
        int index2 = index + 1;
        tc[index] = bv;
        int index3 = index2 + 1;
        tc[index2] = bu;
        int index4 = index3 + 1;
        tc[index3] = tv;
        int index5 = index4 + 1;
        tc[index4] = tu;
        int index6 = index5 + 1;
        tc[index5] = bv;
        int index7 = index6 + 1;
        tc[index6] = tu;
        int i = index7 + 1;
        tc[index7] = tv;
    }

    private void initPosition(@NonNull BlockFactory.WorldSide blockSide) {
        switch (blockSide) {
            case Bottom:
                mY -= 0.55f;
                return;
            case Top:
                mY += 0.7f;
                return;
            case North:
                mX -= 0.55f;
                return;
            case South:
                mX += 0.55f;
                return;
            case West:
                mZ += 0.55f;
                return;
            case East:
                mZ -= 0.55f;
                return;
            default:
                return;
        }
    }

    public void draw(StackedRenderer sr, FPSCamera camera) {
        if (isActive() && System.currentTimeMillis() - createTime >= timeDelay) {
            if (!mIsExplosion) {
                if (yOffset <= 0.12f && moveUp) {
                    yOffset += 0.03f;
                } else {
                    moveUp = false;
                    yOffset -= 0.03f;
                }
                zOffset += horizontalOffsetSign * 0.01f;
                xOffset += horizontalOffsetSign * 0.01f;
            } else {
                setExplosionShape();
            }
            if (!isOrtho2D) {
                float angleHeading = Range.wrap(camera.getHeading(), 0.0f, 6.2831855f) + 3.1415927f;
                sr.pushMatrix();
                if (mBlockSide == BlockFactory.WorldSide.South || mBlockSide == BlockFactory.WorldSide.North) {
                    sr.translate(mX, mY + yOffset, mZ + zOffset);
                } else if (mBlockSide == BlockFactory.WorldSide.West || mBlockSide == BlockFactory.WorldSide.East) {
                    sr.translate(mX + xOffset, mY + yOffset, mZ);
                } else if (mBlockSide == BlockFactory.WorldSide.Top || mBlockSide == BlockFactory.WorldSide.Bottom) {
                    sr.translate(mX + xOffset, mY + yOffset, mZ + zOffset);
                }
                sr.scale(scale, scale, scale);
                sr.rotate(Trig.toDegrees(angleHeading), 0.0f, 1.0f, 0.0f);
                if ((mBlockSide == BlockFactory.WorldSide.Bottom || mBlockSide == BlockFactory.WorldSide.Top) && !mIsExplosion) {
                    float angleElevation = Range.wrap(camera.getElevation(), 0.0f, 1.5707964f) + 3.1415927f;
                    sr.rotate(Trig.toDegrees(angleElevation), 1.0f, 0.0f, 0.0f);
                }
                if (ts != null) {
                    ts.render(sr);
                }
                sr.popMatrix();
                sr.render();
                return;
            }
            GLUtil.scaledOrtho(Game.mGameWidth, Game.mGameHeight, Game.mScreenWidth, Game.mScreenHeight, -1.0f, 1.0f);
            sr.pushMatrix();
            sr.translate(375.0f + (zOffset * 300.0f), 80.0f + (yOffset * 500.0f), 1.0f);
            sr.scale(scale * 400.0f, scale * 400.0f, 1.0f);
            ts.render(sr);
            sr.popMatrix();
            sr.render();
            camera.resetGluPerspective();
        }
    }

    private void setExplosionShape() {
        long delta = System.currentTimeMillis() - createTime;
        int step = (int) (ACTIVE_PERIOD / (EXPLOSION_SHAPE_COORDS.length / 2));
        int index = ((int) (delta / step)) * 2;
        if (index >= EXPLOSION_SHAPE_COORDS.length || index + 1 >= EXPLOSION_SHAPE_COORDS.length) {
            ts = null;
            return;
        }
        int s = EXPLOSION_SHAPE_COORDS[index];
        int t = EXPLOSION_SHAPE_COORDS[index + 1];
        calcTextureOffset(itc, new int[]{s, t});
        ts = new TexturedShape(itemColouredShape, itc, ItemFactory.itemTexture);
    }

    public boolean isActive() {
        return System.currentTimeMillis() - createTime <= ACTIVE_PERIOD + timeDelay && ts != null;
    }
}
