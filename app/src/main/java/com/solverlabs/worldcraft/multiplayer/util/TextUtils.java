package com.solverlabs.worldcraft.multiplayer.util;

import android.text.InputFilter;
import android.text.Spanned;
import android.widget.EditText;

import com.solverlabs.worldcraft.factories.DescriptionFactory;


public class TextUtils {
    public static final InputFilter ALPHA_NUMERIC_FILTER = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence arg0, int arg1, int arg2, Spanned arg3, int arg4, int arg5) {
            for (int k = arg1; k < arg2; k++) {
                char ch = arg0.charAt(k);
                if ((ch < 'a' || ch > 'z') && ((ch < 'A' || ch > 'Z') && ((ch < '0' || ch > '9') && ch != '-'))) {
                    return DescriptionFactory.emptyText;
                }
            }
            return null;
        }
    };

    public static boolean isEditTextMessageEmpty(EditText editText) {
        String msg = String.valueOf(editText.getText());
        return msg != null && !"null".equals(msg) && !DescriptionFactory.emptyText.equals(msg.trim());
    }
}
