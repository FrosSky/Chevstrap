package com.chevstrap.rbx;

import com.chevstrap.rbx.Utility.FileTool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Semaphore;

public class Logger {

    private static volatile Logger instance;

    private final Semaphore semaphore = new Semaphore(1);
    private FileOutputStream filestream;

    public final List<String> history = new ArrayList<>();

    public boolean initialized = false;
    public boolean noWriteMode = false;
    public String fileLocation;
    private boolean cleanupDone = false;

    private Logger() {
    }

    public static Logger getInstance() {
        if (instance == null) {
            synchronized (Logger.class) {
                if (instance == null) {
                    instance = new Logger();
                }
            }
        }
        return instance;
    }

    public void initializePersistent() {
        if (initialized || noWriteMode) return;

        File directory = new File(Paths.getLogs());
        if (!directory.exists() && !directory.mkdirs()) {
            noWriteMode = true;
            return;
        }

        if (!cleanupDone) {
            cleanupOldLogs(directory);
            cleanupDone = true;
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US).format(new Date());
        File file = new File(directory, "Chevstrap_" + timestamp + ".log");

        try {
            filestream = new FileOutputStream(file, true);
            initialized = true;
            fileLocation = file.getAbsolutePath();

            writeLine("App::OnStartup", "Starting Chevstrap");
            writeLine("Logger::Initialize", "Logger initialized at " + fileLocation);

            if (!history.isEmpty()) {
                writeToLog(String.join("\r\n", history));
            }
        } catch (IOException e) {
            noWriteMode = true;
        }
    }

    private void cleanupOldLogs(File directory) {
        long cutoff = System.currentTimeMillis() - (3L * 24L * 60L * 60L * 1000L);

        try {
            File[] files = FileTool.listFiles(directory);
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".log")) {
                    if (file.lastModified() < cutoff) {
                        FileTool.deleteFile(file);
                    }
                }
            }
        } catch (IOException e) {
            writeException("Logger::Cleanup", e);
        }
    }

    public void writeLine(String identifier, String message) {
        writeLineInternal("[" + identifier + "] " + message);
    }

    public void writeException(String identifier, Exception ex) {
        String hResult = "0x" + Integer.toHexString(ex.hashCode()).toUpperCase(Locale.US);
        writeLineInternal("[" + identifier + "] (" + hResult + ") " + ex);
    }

    private void writeLineInternal(String message) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(new Date()) + "Z";
        String output = timestamp + " " + message;

        System.out.println(output);
        history.add(output);
        writeToLog(output);
    }

    private void writeToLog(String message) {
        if (!initialized || filestream == null || noWriteMode) return;

        try {
            semaphore.acquire();
            filestream.write((message + "\r\n").getBytes(StandardCharsets.UTF_8));
            filestream.flush();
        } catch (IOException | InterruptedException ignored) {
        } finally {
            semaphore.release();
        }
    }
}
