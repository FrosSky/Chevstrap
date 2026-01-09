package com.chevstrap.rbx;

import static com.chevstrap.rbx.App.getLatestReleaseAsync;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import com.chevstrap.rbx.AppDirectories.RobloxClientData;
import com.chevstrap.rbx.UI.Elements.CustomDialogs.LoadingFragment;
import com.chevstrap.rbx.UI.Frontend;
import com.chevstrap.rbx.UI.ViewModels.GlobalViewModel;
import com.chevstrap.rbx.Utility.FileTool;

import org.json.JSONObject;

import java.io.File;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ChevstrapLauncher {
    private FragmentManager fragmentManager;
    private volatile boolean isCancelled = false;
    private String packageName;

    private static String getTextLocale(Context context, int resId) {
        return context.getString(resId);
    }

    public void setFragmentManager(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }

    public void Run() {
        String LOG_IDENT = "Launcher::Run";
        Logger logger = App.getLogger();
        logger.writeLine(LOG_IDENT, "Running launcher");
        App.getConfig().load(false);
        StartRoblox();
    }

    public Boolean ApplyChanges() throws Exception {
        String LOG_IDENT = "Launcher::ApplyChanges";
        Logger logger = App.getLogger();
        logger.writeLine(LOG_IDENT, "Checking file changes");

        RobloxClientData robloxData = new RobloxClientData();
        String fallbackDirPath = new File(new File(robloxData.getRobloxDirectory(), "exe"), "ClientSettings").getAbsolutePath();
        File fallbackDir = new File(fallbackDirPath);
        File fallbackFile = new File(fallbackDir, "ClientAppSettings.json");

        boolean value_usefflagmanager = App.getChevstrapSettings().isUseFFlagsManager();
        if (!value_usefflagmanager) {
            logger.writeLine(LOG_IDENT, "FFlag manager disabled or not set, cancelling ApplyChanges");
            return true;
        }
        if (!fallbackDir.exists() && !fallbackDir.mkdirs()) {
            String reason = "Failed to create directory: " + fallbackDir.getAbsolutePath();
            logger.writeLine(LOG_IDENT, reason);
            throw new Exception(reason);
        }

        File file = new File(String.valueOf(App.getConfig().getFileLocation()));
        String jsonStr;
        try {
            jsonStr = FileTool.read(file);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

        JSONObject json = new JSONObject(jsonStr);
        if (json.has("fflags")) {
            JSONObject FFlagsObject = json.getJSONObject("fflags");
            String contents = FFlagsObject.toString(4);
            logger.writeLine(LOG_IDENT, "Applying FFlags");
            try {
                FileTool.write(fallbackFile, contents);
            } catch (Exception e) {
                throw new Exception(e.getMessage());
            }
        }

        RobloxLocalSettings.setASetting(
                "HapticStrength",
                App.getChevstrapSettings().isSetInGameHaptics() ? 1 : 0
        );
        RobloxLocalSettings.setASetting("ReducedMotion",  App.getChevstrapSettings().isSetInGameReducedMotion());
        return true;
    }

    public void StartRoblox() {
        Context context = App.getAppContext();
        if (context == null || fragmentManager == null) {
            throw new IllegalStateException("Context or FragmentManager not set.");
        }
        this.packageName = new RobloxClientData().getExecutablePackage();
        isCancelled = false;
        LoadingFragment fragment = createLoadingDialog();
        fragment.setCancelable(false);
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            fragment.setMessageText(getTextLocale(App.getAppContext(), R.string.bootstrapper_status_connecting));
            fragment.setMessageStatus("0%");
            try {
                fragment.show(fragmentManager, "Messagebox");
            } catch (IllegalStateException ignored) {
            }
        });
        animateProgress(fragment, 0, 20, () -> {
            if (isCancelled) return;
            if (App.getChevstrapSettings().isManualUpdateBootstrapper()) {
                handler.post(() -> {
                    try {
                        String currentLauncherVersion = App.getCurrentVersion(context);
                        getLatestReleaseAsync(new App.ReleaseCallback() {
                            @Override
                            public void onSuccess(String version) {
                                if (isCancelled) return;
                                if (version == null || currentLauncherVersion == null) {
                                    App.getLogger().writeLine("Launcher::StartRoblox", "Version string is null");
                                    return;
                                }
                                if (!version.equals(currentLauncherVersion)) {
                                    handler.post(() -> {
                                        GlobalViewModel.openWebpage(context, "https://github.com/" + App.ProjectRepository + "/releases/latest");
                                        fragment.setMessageText(getTextLocale(App.getAppContext(), R.string.bootstrapper_status_upgrading_chevstrap));
                                        fragment.setMessageStatus("20%");
                                        isCancelled = true;
                                    });
                                }
                            }

                            @Override
                            public void onError(Exception e) {
                                App.getLogger().writeLine("Launcher::StartRoblox", "Failed to get latest launcher release");
                            }
                        });
                    } catch (Exception ex) {
                        App.getLogger().writeLine("Launcher::StartRoblox", "Failed to get current launcher version");
                        App.getLogger().writeException("Launcher::StartRoblox", ex);
                    }
                });
            }
            if (IsRobloxModified(context)) {
                Frontend.ShowMessageBox(App.getSavedFragmentActivity(), getTextLocale(App.getAppContext(), R.string.dialog_cheater_warning));
                isCancelled = true;
                return;
            }
            animateProgress(fragment, 20, 50, () -> {
                if (isCancelled) return;
                handler.post(() -> fragment.setMessageText(getTextLocale(App.getAppContext(), R.string.bootstrapper_status_applying_modifications)));
                animateProgress(fragment, 50, 70, () -> {
                    if (isCancelled) return;
                    boolean dontcancel;
                    Exception tempException = null;
                    try {
                        dontcancel = ApplyChanges();
                        App.getLogger().writeLine("Launcher::ApplyChanges", "ApplyChanges returned: " + dontcancel);
                    } catch (Exception e) {
                        String LOG_IDENT = "Launcher::ApplyChanges";
                        App.getLogger().writeLine(LOG_IDENT, "Exception while applying FFlags");
                        App.getLogger().writeException(LOG_IDENT, e);
                        dontcancel = false;
                        tempException = e;
                    }
                    if (!dontcancel) {
                        isCancelled = true;
                        Exception finalTempException = tempException;
                        handler.post(() -> {
                            if (fragment.isAdded()) fragment.dismissAllowingStateLoss();
                            assert finalTempException != null;
                            Frontend.ShowExceptionDialog(App.getSavedFragmentActivity(), getTextLocale(App.getAppContext(), R.string.dialog_failed_to_apply_changes), finalTempException);
                        });
                        return;
                    }
                    animateProgress(fragment, 70, 80, () -> {
                        if (isCancelled) return;
                        animateProgress(fragment, 80, 100, () -> {
                            if (isCancelled) return;
                            handler.post(() -> fragment.setMessageText(getTextLocale(App.getAppContext(), R.string.bootstrapper_status_starting_roblox)));
                            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
                            if (launchIntent == null) {
                                handler.post(() -> {
                                    if (fragment.isAdded()) fragment.dismissAllowingStateLoss();
                                    Frontend.ShowMessageBox(App.getSavedFragmentActivity(), getTextLocale(App.getAppContext(), R.string.dialog_not_installed));
                                });
                                return;
                            }
                            handler.post(() -> {
                                try {
                                    if (fragment.isAdded()) fragment.dismissAllowingStateLoss();
                                    if (!(context instanceof Activity))
                                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    if ((Boolean) App.getChevstrapSettings().isServerLocationIndicatorEnabled() && (Boolean) App.getChevstrapSettings().isActivityTrackerEnabled()) {
                                    App.setIsLastLogFoundOrMaybeNot(false);
                                        CustomWatcher.getInstance().Dispose();
                                        CustomWatcher.getInstance().run();
                                        new Thread(() -> {
                                            while (true) {
                                                try {
                                                    synchronized (this) {
                                                        wait(1000);
                                                    }
                                                    if (App.getIsLastLogFoundOrMaybeNot()) {
                                                        LaunchRoblox(context, launchIntent);
                                                        break;
                                                    }
                                                } catch (InterruptedException ignored) {
                                                }
                                            }
                                        }).start();
                                    } else {
                                        LaunchRoblox(context, launchIntent);
                                    }
                                } catch (Exception e) {
                                    Frontend.ShowExceptionDialog(App.getSavedFragmentActivity(), getTextLocale(App.getAppContext(), R.string.dialog_failed_to_launch), e);
                                }
                            });
                        });
                    });
                });
            });
        });
    }

    private void LaunchRoblox(Context context, Intent launchIntent) {
        try {
            context.startActivity(launchIntent);
        } catch (Exception e) {
            Frontend.ShowPlayerErrorDialog(App.getSavedFragmentActivity(), e);
            App.getLogger().writeLine("Launcher::LaunchRoblox", "Failed to launch Roblox");
            App.getLogger().writeException("Launcher::LaunchRoblox", e);
        }
    }

    private void animateProgress(LoadingFragment fragment, int start, int end, Runnable onComplete) {
        Handler handler = new Handler(Looper.getMainLooper());
        int steps = 20;
        int delay = 3500 / steps;
        float increment = (float) (end - start) / steps;
        final float[] progress = {start};
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (isCancelled) return;
                if (progress[0] < end) {
                    fragment.setMessageStatus(((int) progress[0]) + "%");
                    progress[0] += increment;
                    handler.postDelayed(this, delay);
                } else {
                    fragment.setMessageStatus(end + "%");
                    if (onComplete != null) onComplete.run();
                }
            }
        };
        handler.post(runnable);
    }

    private boolean IsRobloxModified(Context context) {
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(packageName, 0);
            try (ZipFile zipFile = new ZipFile(ai.sourceDir)) {
                for (ZipEntry entry : Collections.list(zipFile.entries())) {
                    if (entry.getName().startsWith("lib/")) {
                        App.getLogger().writeLine("Launcher::IsRobloxModified", "Roblox is modified detected");
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            App.getLogger().writeLine("Launcher::IsRobloxModified", "Failed to check if Roblox is modified");
            App.getLogger().writeException("Launcher::IsRobloxModified", e);
        }
        return false;
    }

    @NonNull
    private LoadingFragment createLoadingDialog() {
        LoadingFragment fragment = new LoadingFragment();
        fragment.setMessageboxListener(() -> {
            isCancelled = true;
            if (fragment.isAdded() && fragmentManager != null) {
                fragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss();
            }
        });
        return fragment;
    }
}