package com.chevstrap.rbx.UI.ViewModels;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class GlobalViewModel {
    public static void openWebpage(Context context, String getLink) {
        if (getLink == null || getLink.isEmpty())
            return;
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getLink));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception ignored) {
        }
    }
}