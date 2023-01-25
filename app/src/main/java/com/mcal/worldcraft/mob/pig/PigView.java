package com.mcal.worldcraft.mob.pig;

import com.mcal.droid.rugl.geom.TexturedShape;
import com.mcal.droid.rugl.gl.State;
import com.mcal.worldcraft.mob.MobTexturePack;
import com.mcal.worldcraft.mob.MobView;

public class PigView extends MobView {
    private final PigTexturePack texturePack;

    public PigView(Pig pig, State state) {
        super(pig, state);
        this.texturePack = new PigTexturePack(pig, state);
    }

    @Override
    public MobTexturePack getTexturePack() {
        return this.texturePack;
    }

    @Override
    public float updateGeometry() {
        float y = super.updateGeometry();
        generateSnout(y);
        return y;
    }

    private void generateSnout(float y) {
        TexturedShape s = this.texturePack.getSnout();
        if (s != null) {
            if (s.getLastRotateAngle() == this.mob.getAngle()) {
                s.restoreCheckpoint();
                return;
            }
            s.reset();
            s.rotateYByOne(this.mob.getAngle());
            s.checkpoint();
        }
    }
}
