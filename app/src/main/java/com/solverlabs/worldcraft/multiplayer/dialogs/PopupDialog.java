package com.solverlabs.worldcraft.multiplayer.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;

import androidx.annotation.NonNull;

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
        AlertDialog.Builder alertDialogBuilder = createDialog(titleId, messageText, context);
        alertDialogBuilder.setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss());
        alertDialogBuilder.show();
    }

    public static AlertDialog.Builder createDialog(int titleId, int messageId, Context context) {
        if (context != null) {
            return createDialog(titleId, context.getString(messageId), context);
        }
        return null;
    }

    @NonNull
    public static AlertDialog.Builder createDialog(int titleId, String messageText, Context context) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle(titleId);
        alertDialogBuilder.setMessage(messageText);
        return alertDialogBuilder;
    }
}
