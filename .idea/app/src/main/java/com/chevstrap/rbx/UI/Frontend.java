package com.chevstrap.rbx.UI;

import androidx.fragment.app.FragmentActivity;

import com.chevstrap.rbx.App;
import com.chevstrap.rbx.R;
import com.chevstrap.rbx.UI.Elements.CustomDialogs.MessageboxFragment;

public class Frontend {
    public static void ShowExceptionDialog(FragmentActivity activity, String message, Exception exception) {
        MessageboxFragment dialog = new MessageboxFragment();

        dialog.setMessageText(message);
        dialog.disableCancel();
        dialog.setMoreInformation(exception.getMessage());

        dialog.setMessageboxListener(new MessageboxFragment.MessageboxListener() {
            @Override
            public void onOkClicked() {
                dialog.dismiss();
            }
            @Override
            public void onCancelClicked() {}
        });
        dialog.show(activity.getSupportFragmentManager(), "messagebox");
    }

    public static void ShowPlayerErrorDialog(FragmentActivity activity, Exception ex) {
        ShowMessageBox(activity, App.getTextLocale(App.getAppContext(), R.string.dialog_player_error_help_information) + "https://github.com/" + App.ProjectRepository + "/wiki/Roblox-Crash-&-Launch-Issues-(Android))");
        ShowExceptionDialog(activity, ex.getMessage(), ex);
    }

    public static void ShowMessageBox(FragmentActivity activity, String message) {
        MessageboxFragment dialog = new MessageboxFragment();

        dialog.setMessageText(message);
        dialog.disableCancel();

        dialog.setMessageboxListener(new MessageboxFragment.MessageboxListener() {
            @Override
            public void onOkClicked() {
                dialog.dismiss();
            }
            @Override
            public void onCancelClicked() {}
        });

        dialog.show(activity.getSupportFragmentManager(), "messagebox");
    }

    public static void ShowMessageBoxWithRunnable(FragmentActivity activity, String message, Boolean useYes,
                                      Runnable onOk, Runnable onCancel) {
        MessageboxFragment dialog = new MessageboxFragment();
        dialog.setMessageText(message);

        if (useYes) {
            dialog.replaceOKWithYes();
        }

//        if (onCancel == null) {
//            dialog.disableCancel();
//        }

        dialog.setMessageboxListener(new MessageboxFragment.MessageboxListener() {
            @Override
            public void onOkClicked() {
                dialog.dismiss();
                if (onOk != null) onOk.run();
            }

            @Override
            public void onCancelClicked() {
                dialog.dismiss();
                if (onCancel != null) onCancel.run();
            }
        });

        dialog.show(activity.getSupportFragmentManager(), "messagebox");
    }
}

