package com.solverlabs.worldcraft.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.util.WorldUtils;
import com.solverlabs.worldcraft.MyApplication;
import com.solverlabs.worldcraft.R;
import com.solverlabs.worldcraft.dialog.component.MolotButton;
import com.solverlabs.worldcraft.dialog.component.MolotTextView;
import com.solverlabs.worldcraft.dialog.tools.ui.SwipeView;
import com.solverlabs.worldcraft.ui.GUI;
import com.solverlabs.worldcraft.util.GameStarter;
import com.solverlabs.worldcraft.util.WorldGenerator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

public class SingleplayerActivity extends CommonActivity {
    public static final String SHOULD_FINISH = "shouldFinish";
    private RotateAnimation rotateAnimation = null;
    private SwipeView swipeView = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.singleplayer);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        initSwipeView();
        MolotButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());
        MolotButton createButton = findViewById(R.id.create_button);
        createButton.setOnClickListener(v -> {
            Intent intent = new Intent(SingleplayerActivity.this, NewGameSingleplayerActivity.class);
            startActivityForResult(intent, 1);
        });
    }

    @Override
    protected void onResume() {
        updateMapList();
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == -1 && data.getBooleanExtra(SHOULD_FINISH, false)) {
            finish();
        }
    }

    public void onItemClick(@NonNull WorldUtils.WorldInfo worldInfo) {
        GameStarter.startGame((MyApplication) getApplication(), this, worldInfo.file.getAbsolutePath(), false, 0, worldInfo.isCreative ? WorldGenerator.Mode.CREATIVE : WorldGenerator.Mode.SURVIVAL);
        finish();
    }

    public void onDeleteClick(WorldUtils.WorldInfo worldInfo) {
        showDeleteSaveDialog(worldInfo);
    }

    private void initSwipeView() {
        this.swipeView = findViewById(R.id.level_swipe);
        this.swipeView.setPageWidth((getScreenWidth(GUI.HEIGHT) * 7) / 12);
    }

    private int getScreenWidth(int defaultValue) {
        try {
            return getWindowManager().getDefaultDisplay().getWidth();
        } catch (Throwable th) {
            return defaultValue;
        }
    }

    public void updateMapList() {
        if (this.swipeView != null) {
            new Thread() {
                @Override
                public void run() {
                    startLoadingSpinnerAnimation();
                    if (WorldUtils.isStorageAvailable(SingleplayerActivity.this)) {
                        try {
                            updateLevelList(WorldUtils.getWorldListSortedByLastModification(SingleplayerActivity.this));
                        } catch (WorldUtils.StorageNotFoundException e) {
                            WorldUtils.showStorageNotFoundDialog(SingleplayerActivity.this);
                        }
                    } else {
                        WorldUtils.showStorageNotFoundDialog(SingleplayerActivity.this);
                    }
                    stopLoadingSpinnerAnimation();
                }
            }.start();
        }
    }

    public void updateLevelList(Collection<WorldUtils.WorldInfo> worldList) {
        final Collection<View> levelViewList = getLevelViewList(worldList);
        runOnUiThread(() -> swipeView.addAllViews(levelViewList));
    }

    @NonNull
    private Collection<View> getLevelViewList(@NonNull Collection<WorldUtils.WorldInfo> worldList) {
        Collection<View> levelViewList = new ArrayList<>();
        for (WorldUtils.WorldInfo worldInfo : worldList) {
            levelViewList.add(createView(worldInfo));
        }
        return levelViewList;
    }

    public void startLoadingSpinnerAnimation() {
        runOnUiThread(() -> {
            ImageView loadingSpinner = getLoadingSpinner();
            loadingSpinner.setVisibility(View.VISIBLE);
            loadingSpinner.startAnimation(getRotateAnimation());
            swipeView.removeAllViews();
        });
    }

    public void stopLoadingSpinnerAnimation() {
        runOnUiThread(() -> {
            ImageView loadingSpinner = getLoadingSpinner();
            loadingSpinner.clearAnimation();
            loadingSpinner.setVisibility(View.GONE);
        });
    }

    public ImageView getLoadingSpinner() {
        return findViewById(R.id.loading_spinner);
    }

    public RotateAnimation getRotateAnimation() {
        if (this.rotateAnimation == null) {
            this.rotateAnimation = new RotateAnimation(0.0f, 1800.0f, 1, 0.5f, 1, 0.5f);
            this.rotateAnimation.setDuration(7500L);
            this.rotateAnimation.setRepeatCount(-1);
            this.rotateAnimation.setInterpolator(new LinearInterpolator());
        }
        return this.rotateAnimation;
    }

    @NonNull
    private LinearLayout createView(final WorldUtils.WorldInfo worldInfo) {
        LayoutInflater inflater = getLayoutInflater();
        LinearLayout view = (LinearLayout) inflater.inflate(R.layout.level_preview, null);
        view.setTag(worldInfo);
        ImageView icon = view.findViewById(R.id.game_mode_icon);
        MolotButton deleteButton = view.findViewById(R.id.delete_button);
        MolotTextView mapNameView = view.findViewById(R.id.map_name);
        MolotTextView mapModifiedAtView = view.findViewById(R.id.map_modified_at);
        MolotTextView mapModeView = view.findViewById(R.id.map_mode);
        mapNameView.setText(worldInfo.name);
        mapModifiedAtView.setText(DateFormat.format("MM/dd/yyyy hh:mmaa", worldInfo.modifiedAt));
        if (worldInfo.isCreative) {
            mapModeView.setText(R.string.creative);
            icon.setImageResource(R.drawable.world_creative);
        } else {
            mapModeView.setText(R.string.survival);
            icon.setImageResource(R.drawable.world_survival);
        }
        deleteButton.setOnClickListener(v -> onDeleteClick(worldInfo));
        LinearLayout levelView = view.findViewById(R.id.level_view);
        levelView.setOnClickListener(v -> onItemClick(worldInfo));
        return view;
    }

    private void showDeleteSaveDialog(@NonNull final WorldUtils.WorldInfo worldInfo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.are_you_realy_want_to_delete_map, worldInfo.file.getName())).setPositiveButton(R.string.yes, (dialog, id) -> {
            deleteSave(worldInfo.file.getAbsolutePath());
            updateMapList();
        }).setNegativeButton(R.string.no, (dialog, id) -> dialog.dismiss());
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void deleteSave(String absolutePath) {
        File dir = new File(absolutePath);
        String[] list = dir.list();
        if (list != null) {
            for (String str : list) {
                File file = new File(dir, str);
                if (file.isFile()) {
                    file.delete();
                }
                if (file.isDirectory()) {
                    deleteSave(file.getAbsolutePath());
                }
            }
        }
        if (list != null && dir.isDirectory() && list.length == 0) {
            dir.delete();
        }
    }
}
