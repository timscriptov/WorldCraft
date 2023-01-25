package com.mcal.worldcraft.dialog;

import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mcal.worldcraft.Player;
import com.mcal.worldcraft.R;
import com.mcal.worldcraft.activity.WorldCraftActivity;

public class DeathMenuDialog {
    private final WorldCraftActivity activity;
    private final Player player;
    private AlertDialog dialog;

    public DeathMenuDialog(WorldCraftActivity activity, Player player) {
        this.activity = activity;
        this.player = player;
    }

    public void show() {
        final View view = View.inflate(activity, R.layout.death_menu_dialog, null);
        final Button respawnButton = view.findViewById(R.id.respawn_button);
        final Button quitToTitleButton = view.findViewById(R.id.quit_to_title_button);

        final MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity);
        builder.setView(view);
        builder.setTitle(R.string.its_all_over);
        builder.setCancelable(false);
        dialog = builder.create();

        respawnButton.setOnClickListener(v -> {
            dialog.dismiss();
            player.respawn();
        });
        quitToTitleButton.setOnClickListener(v -> {
            player.respawn();
            activity.completeCurrentPhase(true);
        });
        dialog.show();
    }

    public boolean isVisible() {
        return dialog != null && dialog.isShowing();
    }
}
