package com.solverlabs.worldcraft.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.solverlabs.droid.rugl.res.ResourceLoader;
import com.solverlabs.worldcraft.GameMode;
import com.solverlabs.worldcraft.MyApplication;
import com.solverlabs.worldcraft.Persistence;
import com.solverlabs.worldcraft.factories.DescriptionFactory;
import com.solverlabs.worldcraft.R;
import com.solverlabs.worldcraft.multiplayer.MultiplayerActivityHelper;
import com.solverlabs.worldcraft.srv.Consts;
import com.solverlabs.worldcraft.util.KeyboardUtils;
import java.util.Hashtable;

public class MainMenuActivity extends CommonActivity {
    public static final int MAP_TYPE_FLAT = 1;
    public static final int MAP_TYPE_PREDEFINED = 2;
    public static final int MAP_TYPE_RANDOM = 0;
    public static int displayHeight;
    public static int displayWidth;
    public static String version = DescriptionFactory.emptyText;
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
        this.activityHelper = new MultiplayerActivityHelper(this);
        ResourceLoader.start(getResources());
        initButtons();
        initVersion();
    }

    private void initVersion() {
        TextView versionTextView = findViewById(R.id.version_text_view);
        versionTextView.setText(getVersionName());
    }

    private String getVersionName() {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
            int i = R.string.version;
            Object[] objArr = new Object[1];
            objArr[0] = pInfo.versionName + (Consts.DEBUG ? " local" : DescriptionFactory.emptyText);
            return getString(i, objArr);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return DescriptionFactory.emptyText;
        }
    }

    private void initButtons() {
        Button singleButton = findViewById(R.id.singlePlyerButton);
        Button multiplayerButton = findViewById(R.id.multiplayerButton);
        Button optionButton = findViewById(R.id.optionButton);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText name = new EditText(this);
        name.setHint(Persistence.getInstance().getPlayerName());
        KeyboardUtils.hideKeyboardOnEnter(this, name);
        builder.setTitle("Please enter your name for multiplayer! You can change your name in option menu");
        builder.setView(name).setPositiveButton("OK", (dialog, id) -> {
            String userName = name.getText().toString();
            Persistence.getInstance().setPlayerName(userName);
            Persistence.getInstance().setFirstTimeStarted(false);
            activityHelper.startMultiplayer();
        });
        AlertDialog alert = builder.create();
        alert.show();
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
        if (!GameMode.isMultiplayerMode() && activityHelper != null) {
            activityHelper.onPause();
        }
        super.onPause();
    }
}
