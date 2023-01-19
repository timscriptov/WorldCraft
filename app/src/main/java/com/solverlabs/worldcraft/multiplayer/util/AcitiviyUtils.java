package com.solverlabs.worldcraft.multiplayer.util;

import android.app.Activity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;


public class AcitiviyUtils {
    public static void hideKeyBoard(View v, Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService("input_method");
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }
}
