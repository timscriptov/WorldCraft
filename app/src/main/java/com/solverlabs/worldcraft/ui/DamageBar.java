package com.solverlabs.worldcraft.ui;

import com.solverlabs.droid.rugl.util.Colour;
import com.solverlabs.worldcraft.Player;

public class DamageBar extends FadeInOutBar {
    private static final int DAMAGE_BAR_COLOR = Colour.packFloat(1.0f, 0.0f, 0.0f, 0.3f);
    private static final long FADE_OUT_DURATION = 400;
    private final Player player;

    public DamageBar(Player player) {
        super(DAMAGE_BAR_COLOR, FADE_OUT_DURATION, 0.5f, FadeInOutBar.FadingType.FadeIn);
        this.player = player;
    }

    @Override
    public void advance(float delta) {
        setFadingStartedAt(player.getLastDamagedAt());
        super.advance(delta);
    }
}
