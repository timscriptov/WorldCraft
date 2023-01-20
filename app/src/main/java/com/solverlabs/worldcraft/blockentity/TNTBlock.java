package com.solverlabs.worldcraft.blockentity;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.geom.ShapeBuilder;
import com.solverlabs.droid.rugl.geom.TexturedShape;
import com.solverlabs.droid.rugl.gl.GLUtil;
import com.solverlabs.droid.rugl.gl.StackedRenderer;
import com.solverlabs.droid.rugl.gl.State;
import com.solverlabs.droid.rugl.gl.enums.MagFilter;
import com.solverlabs.droid.rugl.gl.enums.MinFilter;
import com.solverlabs.droid.rugl.util.Colour;
import com.solverlabs.droid.rugl.util.geom.Vector3i;
import com.solverlabs.worldcraft.SoundManager;
import com.solverlabs.worldcraft.Sounds;
import com.solverlabs.worldcraft.World;
import com.solverlabs.worldcraft.etc.Explosion;
import com.solverlabs.worldcraft.factories.BlockFactory;
import com.solverlabs.worldcraft.factories.ItemFactory;
import com.solverlabs.worldcraft.skin.geometry.Parallelepiped;

public class TNTBlock implements BlockEntity {
    private static final float BLOCK_SIZE = 1.0f;
    private static final int COLOR = Colour.packFloat(BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
    private static final int DETONATION_SECONDS = 4;
    private static final float POWER = 4.0f;
    private static State blockState = null;
    private static ItemFactory.Item item = null;
    private static State itemState = null;
    private static Parallelepiped parallelepiped = null;
    private static final float sxtn = 0.0625f;
    private static TexturedShape texShape;
    private static Parallelepiped whiteParallelepiped;
    private static TexturedShape whiteTexShape;
    private long activatedAt;
    private TexturedShape activeTexShape;
    private boolean exploded = false;
    private boolean isActivated;
    private final Vector3i position;
    private final World world;

    public enum DetonationDelayType {
        SHORT_DELAY,
        NORMAL_DELAY
    }

    public static void init() {
        blockState = GLUtil.typicalState.with(MinFilter.NEAREST, MagFilter.NEAREST);
        parallelepiped = Parallelepiped.createParallelepiped(BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE, sxtn);
        whiteParallelepiped = Parallelepiped.createParallelepiped(BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE, sxtn);
        blockState = BlockFactory.texture.applyTo(blockState);
        item = ItemFactory.Item.TNT;
        parallelepiped.setTexCoords(item.block.texCoords);
        whiteParallelepiped.setTexCoords(new int[]{8, 11, 8, 11, 8, 11, 8, 11, 8, 11, 8, 11});
        itemState = GLUtil.typicalState.with(MinFilter.NEAREST, MagFilter.NEAREST);
        if (ItemFactory.itemTexture != null) {
            itemState = ItemFactory.itemTexture.applyTo(itemState);
        }
        texShape = createShapeBuilder(parallelepiped, BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE, COLOR).compile();
        texShape.state = blockState;
        texShape.state.apply();
        whiteTexShape = createShapeBuilder(whiteParallelepiped, BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE, COLOR).compile();
        whiteTexShape.state = blockState;
        whiteTexShape.state.apply();
    }

    public TNTBlock(Vector3i position, World world) {
        this.position = new Vector3i(position);
        this.world = world;
    }

    @NonNull
    private static ShapeBuilder createShapeBuilder(Parallelepiped p, float width, float height, float depth, int color) {
        ShapeBuilder shapeBuilder = new ShapeBuilder();
        shapeBuilder.clear();
        addFace(p, (-1.0f) * width, 0.0f, 0.0f, p.south, depth, height, color, shapeBuilder);
        addFace(p, BLOCK_SIZE * width, 0.0f, 0.0f, p.north, depth, height, color, shapeBuilder);
        addFace(p, 0.0f, 0.0f, (-1.0f) * depth, p.west, width, height, color, shapeBuilder);
        addFace(p, 0.0f, 0.0f, BLOCK_SIZE * depth, p.east, width, height, color, shapeBuilder);
        addFace(p, 0.0f, 0.0f, 0.0f, p.bottom, width, depth, color, shapeBuilder);
        addFace(p, 0.0f, 0.0f, 0.0f, p.top, width, depth, color, shapeBuilder);
        return shapeBuilder;
    }

    private static void addFace(@NonNull Parallelepiped facing, float x, float y, float z, Parallelepiped.Face f, float width, float height, int colour, ShapeBuilder shapBuilder) {
        facing.face(f, x, y, z, width, height, colour, shapBuilder, true);
    }

    public void activate(DetonationDelayType detonationType) {
        activate(detonationType, true);
    }

    public void activate(DetonationDelayType detonationType, boolean removeBlock) {
        if (removeBlock) {
            removeBlock();
        }
        if (detonationType == DetonationDelayType.NORMAL_DELAY) {
            this.activatedAt = System.currentTimeMillis();
        } else if (detonationType == DetonationDelayType.SHORT_DELAY) {
            this.activatedAt = System.currentTimeMillis() - 3800;
        }
        SoundManager.playDistancedSound(Sounds.FUSE, 0.0f);
    }

    private void removeBlock() {
        this.world.setBlockType(this.position.x, this.position.y, this.position.z, (byte) 0);
    }

    @Override
    public void advance(float delta) {
        if (!hasToBeExploded()) {
            this.activeTexShape = getActiveTexShape();
        } else if (!this.isActivated) {
            this.isActivated = true;
            Explosion.explode(this.world, this.position, POWER, this);
        }
    }

    private boolean hasToBeExploded() {
        long activeTime = System.currentTimeMillis() - this.activatedAt;
        return activeTime / 1000 > 4;
    }

    private TexturedShape getActiveTexShape() {
        int state;
        long activeTime = System.currentTimeMillis() - this.activatedAt;
        switch ((int) (activeTime / 1000)) {
            case 0:
            case 1:
                state = (int) (activeTime / 500);
                break;
            case 2:
            case 3:
                state = (int) (activeTime / 250);
                break;
            default:
                state = (int) (activeTime / 100);
                break;
        }
        if (state % 2 == 0) {
            return whiteTexShape;
        }
        return texShape;
    }

    @Override
    public void draw(StackedRenderer renderer) {
        if (!this.exploded && this.activeTexShape != null) {
            renderer.pushMatrix();
            renderer.translate(this.position.x, this.position.y, this.position.z);
            this.activeTexShape.render(renderer);
            renderer.popMatrix();
            renderer.render();
        }
    }

    @Override
    public boolean isDestroyed() {
        return this.exploded;
    }

    public void setExploded(boolean exploded) {
        this.exploded = exploded;
    }
}
