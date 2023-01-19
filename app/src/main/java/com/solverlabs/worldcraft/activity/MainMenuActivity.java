package com.solverlabs.worldcraft.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.solverlabs.droid.rugl.res.ResourceLoader;
import com.solverlabs.worldcraft.GameMode;
import com.solverlabs.worldcraft.Persistence;
import com.solverlabs.worldcraft.R;
import com.solverlabs.worldcraft.factories.DescriptionFactory;
import com.solverlabs.worldcraft.multiplayer.MultiplayerActivityHelper;
import com.solverlabs.worldcraft.util.KeyboardUtils;

public class MainMenuActivity extends CommonActivity {
    public static int displayHeight;
    public static int displayWidth;
    public static String version = DescriptionFactory.emptyText;
    int enterCount;
    private MultiplayerActivityHelper activityHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        displayWidth = displaymetrics.widthPixels;
        displayHeight = displaymetrics.heightPixels;
        setContentView(R.layout.main_menu_view);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setTitle("WorldCraft");
        Persistence.initPersistence(this);
        activityHelper = new MultiplayerActivityHelper(this);
        ResourceLoader.start(getResources());
        initButtons();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void initButtons() {
        Button singleButton = (Button) findViewById(R.id.singlePlyerButton);
        Button multiplayerButton = (Button) findViewById(R.id.multiplayerButton);
        Button optionButton = (Button) findViewById(R.id.optionButton);
        singleButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, SingleplayerActivity.class);
            startActivity(intent);
        });
        multiplayerButton.setOnClickListener(v -> {
            if (Persistence.getInstance().isFirstTimeStarted()) {
                showChangeNameDialog();
            } else {
                activityHelper.startMultiplayer();
            }
        });
        optionButton.setOnClickListener(v -> optionClick());
    }

    public void showChangeNameDialog() {
        final EditText name = new EditText(this);
        name.setHint(Persistence.getInstance().getPlayerName());
        KeyboardUtils.hideKeyboardOnEnter(this, name);

        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(this);
        materialAlertDialogBuilder.setTitle("Please enter your name for multiplayer! You can change your name in option menu");
        materialAlertDialogBuilder.setView(name);
        materialAlertDialogBuilder.setPositiveButton("OK", (dialog, id) -> {
            String userName = name.getText().toString();
            Persistence.getInstance().setPlayerName(userName);
            Persistence.getInstance().setFirstTimeStarted(false);
            activityHelper.startMultiplayer();
        });
        materialAlertDialogBuilder.show();
    }

    public void optionClick() {
        Persistence.getInstance().setFirstTimeStarted(false);
        Intent i = new Intent(this, OptionActivity.class);
        startActivity(i);
    }

    @Override
    protected void onResume() {
        if (activityHelper != null) {
            activityHelper.onResume(this);
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        enterCount = Persistence.getInstance().getEnterCount();
        if (enterCount < 10 && enterCount >= 0) {
            enterCount++;
            Persistence.getInstance().setEnterCount(enterCount);
        }
        if (!GameMode.isMultiplayerMode() && activityHelper != null) {
            activityHelper.onPause();
        }
        super.onPause();
    }
}
