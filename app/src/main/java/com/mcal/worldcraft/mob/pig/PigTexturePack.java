package com.mcal.worldcraft.mob.pig;

import androidx.annotation.NonNull;

import com.mcal.droid.rugl.geom.ShapeBuilder;
import com.mcal.droid.rugl.geom.TexturedShape;
import com.mcal.droid.rugl.gl.Renderer;
import com.mcal.droid.rugl.gl.State;
import com.mcal.worldcraft.mob.MobTexturePack;
import com.mcal.worldcraft.mob.TexturedBlockProperties;
import com.mcal.worldcraft.skin.geometry.Parallelepiped;

public class PigTexturePack extends MobTexturePack {
    private final Pig mob;
    private TexturedShape snout;

    public PigTexturePack(Pig mob, State state) {
        super(mob, state);
        this.mob = mob;
    }

    public TexturedShape getSnout() {
        return this.snout;
    }

    @Override
    public boolean render(Renderer r) {
        boolean renderNeeds = super.render(r);
        if (renderNeeds && this.snout != null) {
            this.snout.render(r);
        }
        return renderNeeds;
    }

    @Override
    public boolean initShapes(int state) {
        boolean needInitShapes = super.initShapes(state);
        if (needInitShapes) {
            this.snout = createSnoutShape();
        }
        return needInitShapes;
    }

    @NonNull
    private TexturedShape createSnoutShape() {
        PigSize pigSize = (PigSize) this.mob.getSize();
        TexturedBlockProperties snoutBlock = pigSize.getSnoutBlockProperties();
        Parallelepiped snoutParallelepiped = Parallelepiped.createParallelepiped(pigSize.getSnoutWidth(), pigSize.getSnoutHeight(), pigSize.getSnoutDepth(), getStxtn(), snoutBlock.getTc());
        ShapeBuilder handShapeBuilder = createShapeBuilder(snoutParallelepiped, snoutBlock.getWidth(), snoutBlock.getHeight(), snoutBlock.getDepth(), getColor());
        TexturedShape s = handShapeBuilder.compile();
        s.state = getState();
        s.translate(pigSize.getSnoutX(), pigSize.getSnoutY(), pigSize.getSnoutZ());
        s.backup();
        return s;
    }
}
