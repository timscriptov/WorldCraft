package com.solverlabs.worldcraft.multiplayer.dialogs;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class PopupDialog {
    public static void showInUiThread(final int titleId, final String messageText, @NonNull final Activity activity) {
        activity.runOnUiThread(() -> PopupDialog.show(titleId, messageText, activity));
    }

    public static void show(int titleId, int messageId, Context context) {
        if (context != null) {
            show(titleId, context.getString(messageId), context);
        }
    }

    public static void show(int titleId, String messageText, Context context) {
        final MaterialAlertDialogBuilder alertDialogBuilder = createDialog(titleId, messageText, context);
        alertDialogBuilder.setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss());
        alertDialogBuilder.show();
    }

    public static MaterialAlertDialogBuilder createDialog(int titleId, int messageId, Context context) {
        if (context != null) {
            return createDialog(titleId, context.getString(messageId), context);
        }
        return null;
    }

    @NonNull
    public static MaterialAlertDialogBuilder createDialog(int titleId, String messageText, Context context) {
        final MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle(titleId);
        builder.setMessage(messageText);
        return builder;
    }
}
