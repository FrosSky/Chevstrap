package com.chevstrap.rbx;

import java.io.File;

public class Paths {
    private static String base = "";
    private static String backups = "";
    private static String logs = "";
    private static String modifications = "";

    public static String getTemp() {
        return new File(App.getAppContext().getCacheDir(), "").getAbsolutePath();
    }

    public static String getLocalAppData() {
        return App.getAppContext().getFilesDir().getAbsolutePath();
    }

    public static String getBackups() {
        ensureInitialized();
        return backups;
    }

    public static String getLogs() {
        ensureInitialized();
        return logs;
    }

    public static String getModifications() {
        ensureInitialized();
        return modifications;
    }

    public static boolean isInitialized() {
        return !base.isEmpty();
    }

    public static void initialize(String baseDirectory) {
        base = baseDirectory;
        backups = new File(base, "Backups").getAbsolutePath();
        logs = new File(base, "Logs").getAbsolutePath();
        modifications = new File(base, "Modifications").getAbsolutePath();
    }

    private static void ensureInitialized() {
        if (!isInitialized()) {
            initialize(getLocalAppData());
        }
    }
}