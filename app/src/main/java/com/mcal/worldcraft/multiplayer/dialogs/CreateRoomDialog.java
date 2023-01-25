package com.mcal.worldcraft.multiplayer.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.mcal.worldcraft.R;
import com.mcal.worldcraft.factories.DescriptionFactory;
import com.mcal.worldcraft.multiplayer.util.TextUtils;
import com.mcal.worldcraft.util.KeyboardUtils;

import java.io.File;
import java.util.Collection;

public class CreateRoomDialog extends Dialog {
    public static final int MAX_ROOM_NAME_LENGTH = 16;
    public static final int MAX_ROOM_PASSWORD_LENGTH = 16;
    private final CheckBox readOnlyCheckBox;
    private final EditText roomNameEditText;
    private final EditText roomPasswordEditText;
    private OnCancelClickListener onCancelClickListener;
    private OnCreateRoomClickListener onCreateRoomClickListener;
    private String worldName;

    public CreateRoomDialog(Context context, Collection<File> worlds) {
        super(context);
        requestWindowFeature(1);
        setContentView(R.layout.create_room);
        getWindow().setFlags(1024, 1024);
        this.roomNameEditText = findViewById(R.id.room_name_edit_text);
        this.roomNameEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16), TextUtils.ALPHA_NUMERIC_FILTER});
        KeyboardUtils.hideKeyboardOnEnter(context, this.roomNameEditText);
        this.roomPasswordEditText = findViewById(R.id.room_password_edit_text);
        this.roomPasswordEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16), TextUtils.ALPHA_NUMERIC_FILTER});
        KeyboardUtils.hideKeyboardOnEnter(context, this.roomPasswordEditText);
        this.readOnlyCheckBox = findViewById(R.id.read_only_checkbox);
        Button okButton = findViewById(R.id.start_button);
        okButton.setOnClickListener(v -> {
            String roomName = String.valueOf(roomNameEditText.getText());
            if (roomName != null && !DescriptionFactory.emptyText.equals(roomName.trim())) {
                hide();
                String roomPassword = String.valueOf(roomPasswordEditText.getText());
                if (roomPassword == null || "null".equals(roomPassword)) {
                    roomPassword = DescriptionFactory.emptyText;
                }
                if (onCreateRoomClickListener != null) {
                    onCreateRoomClickListener.onCreateRoomClick(worldName, roomName, roomPassword, readOnlyCheckBox.isChecked());
                    return;
                }
                return;
            }
            Toast.makeText(getContext(), getContext().getString(R.string.please_enter_room_name), Toast.LENGTH_LONG).show();
        });
        Button cancelButton = findViewById(R.id.back_button);
        cancelButton.setOnClickListener(v -> onCancel());
        initRoomSpinner(worlds);
        getWindow().setSoftInputMode(2);
    }

    @Override
    public void show() {
        this.roomNameEditText.setText(DescriptionFactory.emptyText);
        this.roomPasswordEditText.setText(DescriptionFactory.emptyText);
        this.readOnlyCheckBox.setChecked(false);
        super.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 4) {
            onCancel();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void setOnCancelClickListener(OnCancelClickListener onCancelClickListener) {
        this.onCancelClickListener = onCancelClickListener;
    }

    public void setOnCreateRoomClickListener(OnCreateRoomClickListener onCreateRoomClickListener) {
        this.onCreateRoomClickListener = onCreateRoomClickListener;
    }

    private void initRoomSpinner(@NonNull Collection<File> worlds) {
        int i = 0;
        final String[] worldNames = new String[worlds.size()];
        for (File file : worlds) {
            worldNames[i] = file.getName();
            i++;
        }
        Spinner worldChooser = findViewById(R.id.world_spinner);
        ArrayAdapter<String> worldChooserAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, worldNames);
        worldChooserAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        worldChooser.setAdapter(worldChooserAdapter);
        worldChooser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                worldName = worldNames[arg2];
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    public void onCancel() {
        dismiss();
        if (this.onCancelClickListener != null) {
            this.onCancelClickListener.onCancelClick();
        }
    }

    public interface OnCancelClickListener {
        void onCancelClick();
    }

    public interface OnCreateRoomClickListener {
        void onCreateRoomClick(String str, String str2, String str3, boolean z);
    }
}
