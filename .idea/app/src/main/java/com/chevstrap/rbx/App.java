package com.chevstrap.rbx;

import android.app.Activity;
import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.Context;
import android.os.Build;

import androidx.fragment.app.FragmentActivity;

import com.chevstrap.rbx.Models.Persistable.ChevstrapSettings;
import com.chevstrap.rbx.Utility.HTTPFetcher;

import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class App extends Application {
    private static ConfigManager manager;
    private static ChevstrapSettings getChevstrapSettings;
    public static final String ProjectName = "Chevstrap";
    public static final String ProjectOwner = "FrosSky";
    public static final String ProjectRepository = "FrosSky/Chevstrap";
    public static final String ProjectDownloadLink = "https://github.com/FrosSky/Chevstrap/releases/latest";
    public static final String ProjectHelpLink = "https://github.com/frossky/chevstrap/wiki";
    public static final String ProjectSupportLink = "https://github.com/frossky/chevstrap/issues/new";
    private static Context appContext;
    private static FragmentActivity savedFragmentActivity;
    private static WeakReference<Activity> savedSettingsActivityRef;
    private static boolean IsAlreadyStartup = false;
    private static boolean isLastLogFoundOrMaybeNot = false;
    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
        manager = ConfigManager.getInstance();
        getChevstrapSettings = ChevstrapSettings.getInstance();

        if (!getIsAlreadyStartup()) {
            onStartup();
        }
    }

    public static ConfigManager getConfig() {
        return manager;
    }
    public static ChevstrapSettings getChevstrapSettings() {
        return getChevstrapSettings;
    }

    public static Context getAppContext() {
        return appContext;
    }

    public static FragmentActivity getSavedFragmentActivity() {
        return savedFragmentActivity;
    }

    public static void setSavedFragmentActivity(FragmentActivity getFragmentActivityIs) {
        savedFragmentActivity = getFragmentActivityIs;
    }

    public static Activity getSavedSettingsActivity() {
        return savedSettingsActivityRef != null ? savedSettingsActivityRef.get() : null;
    }

    public static void setSavedSettingsActivity(Activity activity) {
        if (activity == null) {
            savedSettingsActivityRef = null;
        } else {
            savedSettingsActivityRef = new WeakReference<>(activity);
        }
    }

    public static boolean getIsAlreadyStartup() {
        return IsAlreadyStartup;
    }

    public static void setIsAlreadyStartup(Boolean valueSet) {
        IsAlreadyStartup = valueSet;
    }

    public static boolean getIsLastLogFoundOrMaybeNot() {
        return isLastLogFoundOrMaybeNot;
    }

    public static void setIsLastLogFoundOrMaybeNot(Boolean valueSet) {
        isLastLogFoundOrMaybeNot = valueSet;
    }

    private static void initializeLogger() {
        Logger logger = Logger.getInstance();
        logger.initializePersistent();
    }

//    public static boolean isNetworkAvailable(Context context) {
//        ConnectivityManager connectivityManager =
//                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//
//        if (connectivityManager == null) {
//            return false;
//        }
//
//        Network network = connectivityManager.getActiveNetwork();
//        if (network == null) {
//            return false;
//        }
//
//        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
//        if (capabilities == null) {
//            return false;
//        }
//
//        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
//                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
//                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET);
//    }

//    public static boolean isEmulator() {
//        String brand = Build.BRAND;
//        String device = Build.DEVICE;
//        String product = Build.PRODUCT;
//        String model = Build.MODEL;
//        String hardware = Build.HARDWARE;
//        String manufacturer = Build.MANUFACTURER;
//        String fingerprint = Build.FINGERPRINT;
//
//        if (brand != null && brand.startsWith("generic")) return true;
//        if (device != null && device.startsWith("generic")) return true;
//        if (product != null && (product.contains("sdk") || product.contains("emulator") || product.contains("genymotion"))) return true;
//        if (model != null && (model.contains("google_sdk") || model.contains("Emulator") || model.contains("Android SDK built for x86"))) return true;
//        if (hardware != null && (hardware.contains("goldfish") || hardware.contains("ranchu") || hardware.contains("qemu"))) return true;
//        if (manufacturer != null && manufacturer.contains("Genymotion")) return true;
//        return fingerprint != null && fingerprint.startsWith("generic");
//    }

//    public String getAppVersion() {
//        try {
//            return context.getPackageManager()
//                    .getPackageInfo(context.getPackageName(), 0).versionName;
//        } catch (Exception e) {
//            return null;
//        }
//    }

    public interface ReleaseCallback {
        void onSuccess(String version);
        void onError(Exception e);
    }

    public static void getLatestReleaseAsync(ReleaseCallback callback) {
        new Thread(() -> {
            try {
                Object response = HTTPFetcher.getJson("https://api.github.com/repos/" + App.ProjectRepository + "/releases/latest");
                JSONObject thejson = new JSONObject(response.toString());

                if (!thejson.has("tag_name") || thejson.getString("tag_name").isEmpty()) {
                    throw new Exception("Invalid response");
                }

                String version = thejson.getString("tag_name").replaceFirst("^v", "");
                callback.onSuccess(version);

            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }


//    public static int getDeviceRefreshRate(Context context) {
//        if (context == null) return 0;
//
//        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//        if (wm == null) return 0;
//
//        Display display = wm.getDefaultDisplay();
//        if (display == null) return 0;
//
//        return (int) display.getRefreshRate();
//    }

    public static void onStartup() {
        final String LOG_IDENT = "App::onStartup";
        initializeLogger();

        String versionName = Build.VERSION.RELEASE;
        int sdkInt = Build.VERSION.SDK_INT;
        String codename = Build.VERSION.CODENAME;

        getLogger().writeLine(LOG_IDENT, "Starting " + ProjectName + " 2.0");

        String osInfo = "OSVersion: Android " + versionName +
                " (SDK " + sdkInt + ", Codename: " + codename + ")";
        getLogger().writeLine(LOG_IDENT, osInfo);

        manager.load(false);
        setIsAlreadyStartup(true);
    }

    public static String getCurrentVersion(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pInfo = pm.getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public static String getTextLocale(Context context, int resId) {
        return context.getString(resId);
    }

    public static Logger getLogger() {
        return Logger.getInstance();
    }
}
