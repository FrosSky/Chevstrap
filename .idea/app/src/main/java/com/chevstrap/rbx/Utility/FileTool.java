package com.chevstrap.rbx.Utility;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.AtomicFile;

import androidx.annotation.Nullable;

import com.chevstrap.rbx.App;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class FileTool {

    public static boolean isExist(String filePath) {
        return new File(filePath).exists();
    }

    public static boolean deleteDir(File dir, Boolean deleteDirectoriesToo) {
        if (dir == null) return false;
        if (dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    boolean success = deleteDir(new File(dir, child), deleteDirectoriesToo);
                    if (!success) return false;
                }
            }
            return !deleteDirectoriesToo || dir.delete();
        } else if (dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    public static void deleteFile(File file) {
        if (file != null && file.exists()) {
            if (file.isDirectory()) {
                deleteDir(file, true);
            } else {
                if (file.delete()) {
                    App.getLogger().writeLine("FileTool::DeleteFile", file.getAbsolutePath());
                }
            }
        }
    }

    public static void ensureDirectoryExists(File dir) {
        if (dir != null && !dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException("Failed to create directory: " + dir.getAbsolutePath());
        }
    }

    public static void safeWrite(File file, String content) throws IOException {
        File parent = file.getParentFile();
        ensureDirectoryExists(parent);

        AtomicFile atomicFile = new AtomicFile(file);
        FileOutputStream fos = null;

        try {
            fos = atomicFile.startWrite();
            fos.write(content.getBytes(StandardCharsets.UTF_8));
            atomicFile.finishWrite(fos);
            fos = null;
        } catch (IOException e) {
            if (fos != null) {
                atomicFile.failWrite(fos);
            }
            throw e;
        }
    }

    public static void copy(File sourceFile, File destFile, boolean overwrite) throws IOException {
        if (!sourceFile.exists()) {
            throw new IOException("Source file does not exist: " + sourceFile.getAbsolutePath());
        }

        if (destFile.exists()) {
            if (overwrite) {
                if (!destFile.delete()) {
                    throw new IOException("Failed to overwrite existing file: " + destFile.getAbsolutePath());
                }
            } else {
                throw new IOException("Destination file already exists: " + destFile.getAbsolutePath());
            }
        } else {
            File parentDir = destFile.getParentFile();
            assert parentDir != null;
            if (!parentDir.exists() && !parentDir.mkdirs()) {
                throw new IOException("Failed to create parent directories for: " + destFile.getAbsolutePath());
            }
        }

        try (FileInputStream in = new FileInputStream(sourceFile);
             FileOutputStream out = new FileOutputStream(destFile)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    public static File[] listFiles(File dir) throws IOException {
        if (dir == null) throw new IOException("Directory is null.");
        if (!dir.exists()) throw new IOException("Directory does not exist: " + dir.getAbsolutePath());
        if (!dir.isDirectory()) throw new IOException("Not a directory: " + dir.getAbsolutePath());
        File[] files = dir.listFiles();
        if (files == null) throw new IOException("Unable to access files in directory: " + dir.getAbsolutePath());
        return files;
    }

    public static String read(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (FileReader fr = new FileReader(file)) {
            char[] buffer = new char[1024];
            int length;
            while ((length = fr.read(buffer)) != -1) {
                sb.append(buffer, 0, length);
            }
        }
        return sb.toString();
    }

    public static void write(File file, String data) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(data);
        }
    }

    public static String convertUriToFilePath(final Context context, final Uri uri) throws IOException {
        String path = null;

        if (DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                if ("primary".equalsIgnoreCase(split[0])) {
                    path = Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                if (docId != null && docId.startsWith("raw:")) {
                    return docId.substring(4);
                }
                if (docId != null) {
                    try {
                        Uri contentUri = ContentUris.withAppendedId(
                                Uri.parse("content://downloads/public_downloads"),
                                Long.parseLong(docId)
                        );
                        path = getDataColumn(context, contentUri, null, null);
                    } catch (NumberFormatException ignored) {}
                }
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                Uri contentUri = getUri(split);
                path = getDataColumn(context, contentUri, "_id=?", new String[]{split[1]});
            }
        } else if (ContentResolver.SCHEME_CONTENT.equalsIgnoreCase(uri.getScheme())) {
            path = getDataColumn(context, uri, null, null);
        } else if (ContentResolver.SCHEME_FILE.equalsIgnoreCase(uri.getScheme())) {
            path = uri.getPath();
        }

        if (path == null) {
            path = copyUriToCache(context, uri);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return URLDecoder.decode(path, StandardCharsets.UTF_8);
        }
        return path;
    }

    @Nullable
    private static Uri getUri(String[] split) {
        final String type = split[0];
        if ("image".equals(type)) {
            return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else if ("video".equals(type)) {
            return MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        } else if ("audio".equals(type)) {
            return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }
        return null;
    }

    private static String copyUriToCache(Context context, Uri uri) throws IOException {
        File cacheDir = new File(context.getCacheDir(), "imports");
        if (!cacheDir.exists() && !cacheDir.mkdirs()) {
            throw new IOException("Failed to create cache directory");
        }
        String fileName = "import_" + System.currentTimeMillis();
        File file = new File(cacheDir, fileName);

        try (InputStream input = context.getContentResolver().openInputStream(uri);
             FileOutputStream output = new FileOutputStream(file)) {
            if (input == null) throw new IOException("Unable to open input stream for URI: " + uri);
            byte[] buffer = new byte[8192];
            int len;
            while ((len = input.read(buffer)) != -1) {
                output.write(buffer, 0, len);
            }
        }
        return file.getAbsolutePath();
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int columnIndex = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(columnIndex);
            }
        } catch (Exception ignored) {
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}
