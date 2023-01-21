package com.solverlabs.worldcraft.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;

import com.solverlabs.worldcraft.MyApplication;
import com.solverlabs.worldcraft.R;
import com.solverlabs.worldcraft.dialog.component.MolotButton;
import com.solverlabs.worldcraft.dialog.component.MolotEditText;
import com.solverlabs.worldcraft.dialog.component.MolotRadioButton;
import com.solverlabs.worldcraft.multiplayer.util.TextUtils;
import com.solverlabs.worldcraft.util.GameStarter;
import com.solverlabs.worldcraft.util.KeyboardUtils;
import com.solverlabs.worldcraft.util.WorldGenerator;

import java.util.ArrayList;

public class NewGameSingleplayerActivity extends CommonActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_game_singleplayer);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        final MolotEditText name = findViewById(R.id.world_name_edit_text);
        name.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16), TextUtils.ALPHA_NUMERIC_FILTER});
        KeyboardUtils.hideKeyboardOnEnter(this, name);
        final Spinner mapTypeSpinner = findViewById(R.id.map_type_spinner);
        initMapTypeDropDownMenu(mapTypeSpinner);
        MolotButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());
        final MolotRadioButton worldTypeCreative = findViewById(R.id.world_type_creative);
        worldTypeCreative.setOnClickListener(v -> mapTypeSpinner.setVisibility(View.VISIBLE));
        MolotRadioButton worldTypeSurvival = findViewById(R.id.world_type_survival);
        worldTypeSurvival.setOnClickListener(v -> mapTypeSpinner.setVisibility(View.GONE));
        MolotButton startButton = findViewById(R.id.start_button);
        startButton.setOnClickListener(v -> {
            KeyboardUtils.hideKeyboard(NewGameSingleplayerActivity.this, name);
            GameStarter.startGame((MyApplication) getApplication(), NewGameSingleplayerActivity.this, String.valueOf(name.getText()), true, mapTypeSpinner.getSelectedItemPosition(), worldTypeCreative.isChecked() ? WorldGenerator.Mode.CREATIVE : WorldGenerator.Mode.SURVIVAL);
            finishActivityAndCloseParent();
        });
        getWindow().setSoftInputMode(2);
    }

    public void finishActivityAndCloseParent() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(SingleplayerActivity.SHOULD_FINISH, true);
        setResult(-1, returnIntent);
        finish();
    }

    public void initMapTypeDropDownMenu(@NonNull Spinner dropDownMenu) {
        ArrayList<String> list = new ArrayList<>();
        list.add(getString(R.string.random_map));
        list.add(getString(R.string.flat_map));
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, list);
        dataAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        dropDownMenu.setAdapter(dataAdapter);
    }
}
