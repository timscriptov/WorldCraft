package com.mcal.droid.rugl.geom;

import androidx.annotation.NonNull;

import com.mcal.droid.rugl.gl.GLUtil;
import com.mcal.droid.rugl.gl.StackedRenderer;
import com.mcal.droid.rugl.gl.State;
import com.mcal.droid.rugl.gl.enums.FogMode;
import com.mcal.droid.rugl.gl.enums.MagFilter;
import com.mcal.droid.rugl.gl.enums.MinFilter;
import com.mcal.droid.rugl.gl.facets.Fog;
import com.mcal.droid.rugl.util.Colour;
import com.mcal.worldcraft.factories.BlockFactory;
import com.mcal.worldcraft.skin.geometry.Parallelepiped;


public class BreakingShape {
    private static final int COLOR = Colour.packFloat(1.0f, 1.0f, 1.0f, 1.0f);
    private static final int HIGHTLIGHT_COLOR = Colour.packFloat(1.0f, 1.0f, 1.0f, 0.1f);
    private static final float BLOCK_SIDE_SIZE = 1.01f;
    private static final int[][] BREAKING_TEXT_COORDS = {new int[]{0, 15}, new int[]{1, 15}, new int[]{2, 15}, new int[]{3, 15}, new int[]{4, 15}, new int[]{5, 15}, new int[]{6, 15}, new int[]{7, 15}, new int[]{8, 15}, new int[]{9, 15}};
    private static final int MAX_BREAKING_TEXT_COORDS_INDEX = BREAKING_TEXT_COORDS.length - 1;
    private static final TexturedShape[] BREAKING_BLOCKS = new TexturedShape[BREAKING_TEXT_COORDS.length];
    private static final ColouredShape HIGHLIGHT_SHAPE = new ColouredShape(ShapeUtil.cuboid(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f), HIGHTLIGHT_COLOR, ShapeUtil.state);
    public static State state = GLUtil.typicalState.with(MinFilter.NEAREST, MagFilter.NEAREST).with(new Fog(FogMode.LINEAR, 0.5f, 30.0f, 40.0f, Colour.packFloat(0.7f, 0.7f, 0.9f, 1.0f)));
    private TexturedShape block;
    private float breakingProgress;
    private boolean inBreakingProcess;

    public BreakingShape() {
        int[][] arr$ = BREAKING_TEXT_COORDS;
        int len$ = arr$.length;
        int i$ = 0;
        int i = 0;
        while (i$ < len$) {
            int[] tc = arr$[i$];
            Parallelepiped block = Parallelepiped.createParallelepiped(1.0f, 1.0f, 1.0f, 0.0625f, tc);
            ShapeBuilder shapeBuilder = createShapeBuilder(block, 1, 1, 1, COLOR);
            TexturedShape ts = shapeBuilder.compile();
            state = BlockFactory.texture.applyTo(state);
            if (ts != null) {
                ts.state = state;
            }
            BREAKING_BLOCKS[i] = ts;
            i$++;
            i++;
        }
    }

    @NonNull
    private static ShapeBuilder createShapeBuilder(Parallelepiped p, int width, int height, int depth, int color) {
        ShapeBuilder shapeBuilder = new ShapeBuilder();
        shapeBuilder.clear();
        addFace(p, -BLOCK_SIDE_SIZE, 0.0f, 0.0f, p.south, depth, height, color, shapeBuilder);
        addFace(p, BLOCK_SIDE_SIZE, 0.0f, 0.0f, p.north, depth, height, color, shapeBuilder);
        addFace(p, 0.0f, 0.0f, -BLOCK_SIDE_SIZE, p.west, width, height, color, shapeBuilder);
        addFace(p, 0.0f, 0.0f, BLOCK_SIDE_SIZE, p.east, width, height, color, shapeBuilder);
        addFace(p, 0.0f, 0.0f, 0.0f, p.bottom, width, depth, color, shapeBuilder);
        addFace(p, 0.0f, 0.0f, 0.0f, p.top, width, depth, color, shapeBuilder);
        return shapeBuilder;
    }

    private static void addFace(@NonNull Parallelepiped facing, float x, float y, float z, Parallelepiped.Face f, float width, float height, int colour, ShapeBuilder shapBuilder) {
        facing.face(f, x, y, z, width, height, colour, shapBuilder);
    }

    public void updateBreakingProgress(float breakingProgress) {
        if (breakingProgress == 0.0f || this.breakingProgress == breakingProgress) {
            stopBreakingProgress();
            return;
        }
        int index = (int) (10.0f * breakingProgress);
        if (index < 0) {
            index = 0;
        } else if (index > MAX_BREAKING_TEXT_COORDS_INDEX) {
            index = 0;
        }
        this.block = BREAKING_BLOCKS[index];
        this.breakingProgress = breakingProgress;
        this.inBreakingProcess = true;
    }

    public void render(StackedRenderer renderer) {
        if (this.block != null) {
            this.block.render(renderer);
            HIGHLIGHT_SHAPE.render(renderer);
        }
    }

    public boolean isInBreakingProcess() {
        return this.inBreakingProcess;
    }

    private void stopBreakingProgress() {
        this.inBreakingProcess = false;
    }
}
