package com.mcal.worldcraft.utils;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;

public class KeyboardUtils {
    public static void hideKeyboard(@NonNull Context context, @NonNull View v) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public static void hideKeyboardOnEnter(final Context context, @NonNull final EditText roomNameEditText) {
        roomNameEditText.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == 0 && keyCode == 66) {
                KeyboardUtils.hideKeyboard(context, roomNameEditText);
                return true;
            }
            return false;
        });
    }
}
