package com.solverlabs.worldcraft.dialog.tools.util;

import android.view.View;

import androidx.annotation.NonNull;

public class OverScrollDisabler {
    public static void disableOverScroll(@NonNull View view) {
        view.setOverScrollMode(2);
    }
}
