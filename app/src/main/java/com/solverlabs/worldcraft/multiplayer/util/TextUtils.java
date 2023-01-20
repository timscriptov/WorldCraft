package com.solverlabs.worldcraft.multiplayer.util;

import android.text.InputFilter;
import android.text.Spanned;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.solverlabs.worldcraft.factories.DescriptionFactory;

public class TextUtils {
    public static final InputFilter ALPHA_NUMERIC_FILTER = (arg0, arg1, arg2, arg3, arg4, arg5) -> {
        for (int k = arg1; k < arg2; k++) {
            char ch = arg0.charAt(k);
            if ((ch < 'a' || ch > 'z') && ((ch < 'A' || ch > 'Z') && ((ch < '0' || ch > '9') && ch != '-'))) {
                return DescriptionFactory.emptyText;
            }
        }
        return null;
    };

    public static boolean isEditTextMessageEmpty(@NonNull EditText editText) {
        String msg = String.valueOf(editText.getText());
        return msg != null && !"null".equals(msg) && !DescriptionFactory.emptyText.equals(msg.trim());
    }
}
