package com.mcal.worldcraft;

import androidx.annotation.NonNull;

import com.mcal.droid.rugl.Game;
import com.mcal.droid.rugl.geom.ColouredShape;
import com.mcal.droid.rugl.geom.Shape;
import com.mcal.droid.rugl.geom.ShapeUtil;
import com.mcal.droid.rugl.geom.TexturedShape;
import com.mcal.droid.rugl.gl.GLUtil;
import com.mcal.droid.rugl.gl.StackedRenderer;
import com.mcal.droid.rugl.gl.State;
import com.mcal.droid.rugl.gl.enums.MagFilter;
import com.mcal.droid.rugl.gl.enums.MinFilter;
import com.mcal.droid.rugl.util.Colour;
import com.mcal.droid.rugl.util.FPSCamera;
import com.mcal.droid.rugl.util.Trig;
import com.mcal.droid.rugl.util.math.Range;
import com.mcal.worldcraft.factories.BlockFactory;
import com.mcal.worldcraft.factories.ItemFactory;
import com.mcal.worldcraft.util.RandomUtil;

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
    private final BlockFactory.WorldSide blockSide;
    private final long createTime;
    private final int horizontalOffsetSign;
    float[] itc;
    private boolean isExplosion;
    private boolean isOrtho2D;
    private boolean moveUp;
    private float scale;
    private long timeDelay;
    private TexturedShape ts;
    private float x;
    private float xOffset;
    private float y;
    private float yOffset;
    private float z;
    private float zOffset;

    public BlockParticle(byte blockID, float x, float y, float z, BlockFactory.WorldSide blockSide) {
        this(blockID, x, y, z, blockSide, false);
    }

    public BlockParticle(byte blockID, float x, float y, float z, BlockFactory.WorldSide blockSide, boolean isExplosion) {
        this.itc = ShapeUtil.getQuadTexCoords(1);
        this.moveUp = true;
        this.isOrtho2D = false;
        this.isExplosion = false;
        blockColouredShape.reset();
        itemColouredShape.reset();
        this.isExplosion = isExplosion;
        this.createTime = System.currentTimeMillis();
        this.x = x + 0.5f;
        this.y = y + 0.5f + Range.toValue(random.nextFloat(), -0.4f, 0.4f);
        this.z = z + 0.5f;
        this.blockSide = blockSide;
        initPosition(blockSide);
        initOffsets(blockID);
        this.isOrtho2D = blockSide == BlockFactory.WorldSide.Empty;
        ItemFactory.Item item = ItemFactory.Item.getItemByID(blockID);
        BlockFactory.Block b = BlockFactory.getBlock(blockID);
        faceTexCoords(this.itc, item);
        if (b != null && !isExplosion) {
            this.ts = new TexturedShape(blockColouredShape, this.itc, BlockFactory.texture);
        } else {
            this.ts = new TexturedShape(itemColouredShape, this.itc, ItemFactory.itemTexture);
        }
        this.horizontalOffsetSign = random.nextBoolean() ? 1 : -1;
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
        if (!this.isExplosion) {
            this.scale = RandomUtil.getRandomInRangeExclusive(0.05f, 0.15f);
            this.zOffset = RandomUtil.getRandomInRangeExclusive(-0.5f, 0.5f);
            this.xOffset = RandomUtil.getRandomInRangeExclusive(-0.5f, 0.5f);
            return;
        }
        this.scale = RandomUtil.getRandomInRangeExclusive(0.75f, 3.0f);
        this.zOffset = RandomUtil.getRandomInRangeExclusive(-3.0f, 3.0f);
        this.xOffset = RandomUtil.getRandomInRangeExclusive(-3.0f, 3.0f);
        this.yOffset = RandomUtil.getRandomInRangeExclusive(-3.0f, 2.0f);
        this.timeDelay = RandomUtil.getRandomInRangeInclusive(0, MAX_TIME_DELAY);
    }

    private void faceTexCoords(float[] tc, @NonNull ItemFactory.Item item) {
        int[] blockTexCoords;
        BlockFactory.Block b = BlockFactory.getBlock(item.id);
        if (this.isExplosion) {
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
        int index = 0 + 1;
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
                this.y -= 0.55f;
                return;
            case Top:
                this.y += 0.7f;
                return;
            case North:
                this.x -= 0.55f;
                return;
            case South:
                this.x += 0.55f;
                return;
            case West:
                this.z += 0.55f;
                return;
            case East:
                this.z -= 0.55f;
                return;
            default:
                return;
        }
    }

    public void draw(StackedRenderer sr, FPSCamera camera) {
        if (isActive() && System.currentTimeMillis() - this.createTime >= this.timeDelay) {
            if (!this.isExplosion) {
                if (this.yOffset <= 0.12f && this.moveUp) {
                    this.yOffset += 0.03f;
                } else {
                    this.moveUp = false;
                    this.yOffset -= 0.03f;
                }
                this.zOffset += this.horizontalOffsetSign * 0.01f;
                this.xOffset += this.horizontalOffsetSign * 0.01f;
            } else {
                setExplosionShape();
            }
            if (!this.isOrtho2D) {
                float angleHeading = Range.wrap(camera.getHeading(), 0.0f, 6.2831855f) + 3.1415927f;
                sr.pushMatrix();
                if (this.blockSide == BlockFactory.WorldSide.South || this.blockSide == BlockFactory.WorldSide.North) {
                    sr.translate(this.x, this.y + this.yOffset, this.z + this.zOffset);
                } else if (this.blockSide == BlockFactory.WorldSide.West || this.blockSide == BlockFactory.WorldSide.East) {
                    sr.translate(this.x + this.xOffset, this.y + this.yOffset, this.z);
                } else if (this.blockSide == BlockFactory.WorldSide.Top || this.blockSide == BlockFactory.WorldSide.Bottom) {
                    sr.translate(this.x + this.xOffset, this.y + this.yOffset, this.z + this.zOffset);
                }
                sr.scale(this.scale, this.scale, this.scale);
                sr.rotate(Trig.toDegrees(angleHeading), 0.0f, 1.0f, 0.0f);
                if ((this.blockSide == BlockFactory.WorldSide.Bottom || this.blockSide == BlockFactory.WorldSide.Top) && !this.isExplosion) {
                    float angleElevation = Range.wrap(camera.getElevation(), 0.0f, 1.5707964f) + 3.1415927f;
                    sr.rotate(Trig.toDegrees(angleElevation), 1.0f, 0.0f, 0.0f);
                }
                if (this.ts != null) {
                    this.ts.render(sr);
                }
                sr.popMatrix();
                sr.render();
                return;
            }
            GLUtil.scaledOrtho(Game.gameWidth, Game.gameHeight, Game.screenWidth, Game.screenHeight, -1.0f, 1.0f);
            sr.pushMatrix();
            sr.translate(375.0f + (this.zOffset * 300.0f), 80.0f + (this.yOffset * 500.0f), 1.0f);
            sr.scale(this.scale * 400.0f, this.scale * 400.0f, 1.0f);
            this.ts.render(sr);
            sr.popMatrix();
            sr.render();
            camera.resetGluPerspective();
        }
    }

    private void setExplosionShape() {
        long delta = System.currentTimeMillis() - this.createTime;
        int step = (int) (ACTIVE_PERIOD / (EXPLOSION_SHAPE_COORDS.length / 2));
        int index = ((int) (delta / step)) * 2;
        if (index >= EXPLOSION_SHAPE_COORDS.length || index + 1 >= EXPLOSION_SHAPE_COORDS.length) {
            this.ts = null;
            return;
        }
        int s = EXPLOSION_SHAPE_COORDS[index];
        int t = EXPLOSION_SHAPE_COORDS[index + 1];
        calcTextureOffset(this.itc, new int[]{s, t});
        this.ts = new TexturedShape(itemColouredShape, this.itc, ItemFactory.itemTexture);
    }

    public boolean isActive() {
        return System.currentTimeMillis() - this.createTime <= ACTIVE_PERIOD + this.timeDelay && this.ts != null;
    }
}
