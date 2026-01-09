package com.chevstrap.rbx;

public class Utilities {
    public enum VersionComparison {
        LESS,
        EQUAL,
        GREATER
    }

    public static int[] getVersionFromString(String version) {
        if (version == null || version.isEmpty())
            return new int[]{0};

        if (version.startsWith("v"))
            version = version.substring(1);

        int idx = version.indexOf("+");
        if (idx != -1)
            version = version.substring(0, idx);

        String[] parts = version.split("\\.");
        int[] numbers = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                numbers[i] = Integer.parseInt(parts[i]);
            } catch (NumberFormatException e) {
                numbers[i] = 0;
            }
        }
        return numbers;
    }

    public static VersionComparison compareVersions(String versionStr1, String versionStr2) {
        try {
            int[] v1 = getVersionFromString(versionStr1);
            int[] v2 = getVersionFromString(versionStr2);

            int length = Math.max(v1.length, v2.length);
            for (int i = 0; i < length; i++) {
                int num1 = (i < v1.length) ? v1[i] : 0;
                int num2 = (i < v2.length) ? v2[i] : 0;

                if (num1 < num2) return VersionComparison.LESS;
                if (num1 > num2) return VersionComparison.GREATER;
            }

            return VersionComparison.EQUAL;
        } catch (Exception e) {
            App.getLogger().writeLine("Utilities::CompareVersions", "An exception occurred when comparing versions");
            App.getLogger().writeLine("Utilities::CompareVersions", "versionStr1=" + versionStr1 + " versionStr2=" + versionStr2);
            throw e;
        }
    }
}
