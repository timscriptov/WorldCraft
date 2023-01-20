package com.solverlabs.worldcraft.dialog;

import android.app.Dialog;
import android.view.View;
import android.widget.Button;
import com.solverlabs.worldcraft.Player;
import com.solverlabs.worldcraft.activity.WorldCraftActivity;
import com.solverlabs.worldcraft.R;

public class DeathMenuDialog {
    private final WorldCraftActivity activity;
    private Dialog dialog;
    private final Player player;

    public DeathMenuDialog(WorldCraftActivity activity, Player player) {
        this.activity = activity;
        this.player = player;
    }

    public void show() {
        this.dialog = new Dialog(this.activity);
        this.dialog.setContentView(R.layout.death_menu_dialog);
        this.dialog.setTitle(R.string.its_all_over);
        this.dialog.setCancelable(false);
        Button respawnButton = this.dialog.findViewById(R.id.respawn_button);
        Button quitToTitleButton = this.dialog.findViewById(R.id.quit_to_title_button);
        respawnButton.setOnClickListener(v -> {
            dialog.dismiss();
            player.respawn();
        });
        quitToTitleButton.setOnClickListener(v -> {
            player.respawn();
            activity.completeCurrentPhase(true);
        });
        this.dialog.show();
    }

    public boolean isVisible() {
        return this.dialog != null && this.dialog.isShowing();
    }
}
