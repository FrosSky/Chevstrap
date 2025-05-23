package com.chevstrap.rbx;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.app.Activity;

public class StorageFullDialog {
    public static void show(final Context context) {
        AlertDialog.Builder ask = new AlertDialog.Builder(context);
        ask.setTitle(context.getString(R.string.Warning1));
        ask.setMessage(context.getString(R.string.StorageFullMessage));
        ask.setCancelable(false); // Prevent dismissing by tapping outside or pressing back

        ask.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (context instanceof Activity) {
                    ((Activity) context).finishAffinity();
                }
            }
        });

        AlertDialog dialog = ask.create();
        dialog.show();
    }
}
