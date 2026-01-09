package com.chevstrap.rbx.Utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

public class MD5Hash {

    public static String fromBytes(byte[] data) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] hash = md5.digest(data);
            return stringify(hash);
        } catch (Exception e) {
            return null;
        }
    }

    public static String fromStream(InputStream stream) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = stream.read(buffer)) != -1) {
                md5.update(buffer, 0, bytesRead);
            }
            stream.close();
            return stringify(md5.digest());
        } catch (Exception e) {
            return null;
        }
    }

    public static String fromFile(String filename) {
        try (FileInputStream stream = new FileInputStream(new File(filename))) {
            return fromStream(stream);
        } catch (Exception e) {
            return null;
        }
    }

    public static String fromString(String str) {
        return fromBytes(str.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    public static String stringify(byte[] hash) {
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) {
            String h = Integer.toHexString(0xFF & b);
            if (h.length() == 1)
                hex.append('0');
            hex.append(h);
        }
        return hex.toString();
    }
}
