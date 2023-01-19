package com.solverlabs.worldcraft;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.geom.BedBlock;
import com.solverlabs.droid.rugl.geom.DoorBlock;
import com.solverlabs.droid.rugl.geom.ShapeBuilder;
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
import com.solverlabs.worldcraft.chunk.Chunk;
import com.solverlabs.worldcraft.chunk.Chunklet;
import com.solverlabs.worldcraft.factories.BlockFactory;
import com.solverlabs.worldcraft.factories.ItemFactory;
import com.solverlabs.worldcraft.skin.geometry.Parallelepiped;
import com.solverlabs.worldcraft.util.GameTime;
import com.solverlabs.worldcraft.util.RandomUtil;


public class DroppableItem {
    private static final float BLOCK_SIZE = 0.2f;
    private static final int COLOR = Colour.packFloat(1.0f, 1.0f, 1.0f, 1.0f);
    private static final long INACTIVE_PERIOD = 500;
    private static final long LIVE_PERIOD = 180000;
    private static final float sxtn = 0.0625f;
    private static State blockState = null;
    private static State itemState = null;
    private static Parallelepiped parallelepiped = null;
    private final long createTime;
    public float dx;
    public float dy;
    public float dz;
    public ItemFactory.Item item;
    public float x;
    public float y;
    public float z;
    public int angle = 0;
    private int count;
    private boolean isItem;
    private TexturedShape texShape;
    private float xOffset;
    private float zOffset;
    private float yOffset = 0.0f;
    private boolean moveUp = false;
    private int currentStep = 10;

    public DroppableItem(byte itemID, float x, float y, float z, int count, boolean dropItemFromHotBar) {
        this.xOffset = 0.0f;
        this.zOffset = 0.0f;
        this.isItem = false;
        this.item = ItemFactory.Item.getItemByID(itemID);
        this.x = x;
        this.y = y;
        this.z = z;
        if (!dropItemFromHotBar) {
            this.xOffset = RandomUtil.getRandomInRangeExclusive(-0.2f, 0.2f);
            this.zOffset = RandomUtil.getRandomInRangeExclusive(-0.2f, 0.2f);
        }
        if (this.item != null) {
            if (this.item.block != null && !DoorBlock.isDoor(this.item.block) && !BedBlock.isBed(this.item.block)) {
                if (this.item.id == 90 || this.item.id == 122 || this.item.id == 76) {
                    this.texShape = (TexturedShape) this.item.itemShape.clone();
                } else {
                    parallelepiped.setTexCoords((Object) this.item.block.texCoords);
                    this.texShape = createShapeBuilder(parallelepiped, 1.0f, 1.0f, 1.0f, COLOR).compile();
                }
                this.texShape.state = blockState;
            } else {
                this.texShape = (TexturedShape) this.item.itemShape.clone();
                this.texShape.state = itemState;
                this.isItem = true;
            }
        }
        this.createTime = GameTime.getTime();
        this.count = count;
    }

    public static void init() {
        blockState = GLUtil.typicalState.with(MinFilter.NEAREST, MagFilter.NEAREST);
        parallelepiped = Parallelepiped.createParallelepiped(0.2f, 0.2f, 0.2f, sxtn);
        blockState = BlockFactory.texture.applyTo(blockState);
        itemState = GLUtil.typicalState.with(MinFilter.NEAREST, MagFilter.NEAREST);
        itemState = ItemFactory.itemTexture.applyTo(itemState);
    }

    @NonNull
    private static ShapeBuilder createShapeBuilder(Parallelepiped p, float width, float height, float depth, int color) {
        ShapeBuilder shapeBuilder = new ShapeBuilder();
        shapeBuilder.clear();
        addFace(p, (-0.2f) * width, 0.0f, 0.0f, p.south, depth, height, color, shapeBuilder);
        addFace(p, 0.2f * width, 0.0f, 0.0f, p.north, depth, height, color, shapeBuilder);
        addFace(p, 0.0f, 0.0f, (-0.2f) * depth, p.west, width, height, color, shapeBuilder);
        addFace(p, 0.0f, 0.0f, 0.2f * depth, p.east, width, height, color, shapeBuilder);
        addFace(p, 0.0f, 0.0f, 0.0f, p.bottom, width, depth, color, shapeBuilder);
        addFace(p, 0.0f, 0.0f, 0.0f, p.top, width, depth, color, shapeBuilder);
        return shapeBuilder;
    }

    private static void addFace(@NonNull Parallelepiped facing, float x, float y, float z, Parallelepiped.Face f, float width, float height, int colour, ShapeBuilder shapBuilder) {
        facing.face(f, x, y, z, width, height, colour, shapBuilder);
    }

    public TexturedShape getTexturedShape() {
        return this.texShape;
    }

    public void advance(World world) {
        if (!isAlive()) {
            world.removeDroppableBlock(this);
            return;
        }
        Chunklet chunklet = world.getChunklet(this.x, this.y, this.z);
        if (chunklet == null) {
            world.removeDroppableBlock(this);
            return;
        }
        Chunk c = chunklet.parent;
        float light = chunklet.light(((int) this.x) - chunklet.x, ((int) this.y) - chunklet.y, ((int) this.z) - chunklet.z);
        int colour = Colour.packFloat(light, light, light, 1.0f);
        this.texShape.colours = ShapeUtil.expand(colour, this.texShape.vertexCount());
        byte blockType = c.blockTypeForPosition(this.x, this.y - 0.2f, this.z);
        if (blockType == 0 || blockType == 8) {
            this.y = (float) (this.y - 0.05d);
        }
        Player player = world.mPlayer;
        float posX = player.position.x;
        float posZ = player.position.z;
        float posY = player.position.y;
        float dx = posX - (this.x + this.xOffset);
        float dy = posY - this.y;
        float dz = posZ - (this.z + this.zOffset);
        float max = Math.max(Math.max(Math.abs(dx), Math.abs(dy)), Math.abs(dz));
        if (max < 1.8f && isActive() && !player.isDead()) {
            moveToPlayer(dx, dy, dz);
            if (needToRemove()) {
                boolean addedSuccessfully = player.inventory.add(this);
                if (addedSuccessfully) {
                    SoundManager.playDistancedSound(Sounds.POP, 0.0f);
                }
                if (this.count == 0) {
                    world.removeDroppableBlock(this);
                }
            }
        }
    }

    public void draw(@NonNull StackedRenderer renderer, FPSCamera cam) {
        renderer.pushMatrix();
        if (this.isItem || (this.item.block != null && !this.item.block.isCuboid)) {
            renderer.translate(this.x + this.dx + this.xOffset + 0.5f, this.y + this.dy + 0.4f + getyOffset(), this.z + this.dz + this.zOffset + 0.5f);
            renderer.scale(0.35f, 0.35f, 0.35f);
            float angle = Range.wrap(cam.getHeading(), 0.0f, 6.2831855f) + 3.1415927f;
            renderer.rotate(Trig.toDegrees(angle), 0.0f, 1.0f, 0.0f);
        } else {
            renderer.translate(this.x + this.xOffset + 0.575f, this.y, this.z + this.zOffset + 0.575f);
            this.angle += 3;
            renderer.rotate(this.angle, 0.0f, 1.0f, 0.0f);
            renderer.translate(this.dx - 0.075f, this.dy + getyOffset(), this.dz - 0.075f);
        }
        getTexturedShape().render(renderer);
        renderer.popMatrix();
        renderer.render();
    }

    public boolean isItem() {
        return this.isItem;
    }

    public float getyOffset() {
        int i = 1;
        if (this.yOffset <= 0.0f) {
            this.moveUp = true;
        }
        if (this.yOffset >= 0.15f) {
            this.moveUp = false;
        }
        float f = this.yOffset;
        if (!this.moveUp) {
            i = -1;
        }
        this.yOffset = (i * 0.005f) + f;
        return this.yOffset;
    }

    public void moveToPlayer(float dx, float dy, float dz) {
        this.dx = dx / this.currentStep;
        this.dy = dy / this.currentStep;
        this.dz = dz / this.currentStep;
        this.currentStep--;
    }

    public boolean needToRemove() {
        return this.currentStep == 0;
    }

    public boolean isActive() {
        return GameTime.getTime() - this.createTime >= INACTIVE_PERIOD;
    }

    public boolean isAlive() {
        return GameTime.getTime() - this.createTime < LIVE_PERIOD;
    }

    public byte getBlockID() {
        return this.item.id;
    }

    public int getCount() {
        return this.count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void decCount() {
        this.count--;
    }
}
