package com.chevstrap.rbx.Integrations;

import com.chevstrap.rbx.App;
import com.chevstrap.rbx.AppDirectories.RobloxClientData;
import com.chevstrap.rbx.CustomWatcher;
import com.chevstrap.rbx.Models.Entities.ActivityData;
import com.chevstrap.rbx.UI.NotifyIconWrapper;
import com.chevstrap.rbx.Utility.FileTool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ActivityWatcher {
    public final List<ActivityData> history = new ArrayList<>();
    private ActivityData lastTeleportActivityData = null;
    private volatile boolean stopMonitoring = false;
    private volatile boolean isInExperience = false;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    public ActivityWatcher() {}

    public void runWatcher() {
        final String LOG_IDENT = "ActivityWatcher::runWatcher";
        App.getLogger().writeLine(LOG_IDENT, "Starting activity watcher session");
        executor.submit(this::Start);
    }

    private void Start() {
        final String LOG_IDENT = "ActivityWatcher::Start";
        boolean isFoundANewLog = false;

        CustomWatcher.getInstance().setActivityData(new ActivityData(""));
        RobloxClientData robloxData = new RobloxClientData();
        File logLocation = new File(Objects.requireNonNull(robloxData.getExecutablePath()), "logs");
        if (!logLocation.exists()) {
            boolean created = logLocation.mkdirs();
            App.getLogger().writeLine(LOG_IDENT, "Log directory created=" + created + " at " + logLocation.getAbsolutePath());
        }
        App.getLogger().writeLine(LOG_IDENT, "Watching logs from " + logLocation.getAbsolutePath());
        BufferedReader reader = null;
        File ignoredFirstLog;
        File currentLog = null;
        try {
            File[] logsList = FileTool.listFiles(logLocation);
            if (logsList.length > 0) {
                Arrays.sort(logsList, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
                ignoredFirstLog = logsList[0];

                App.setIsLastLogFoundOrMaybeNot(true);
                App.getLogger().writeLine(LOG_IDENT, "Ignoring first log: " + ignoredFirstLog.getName());
            } else {
                App.setIsLastLogFoundOrMaybeNot(true);
                App.getLogger().writeLine(LOG_IDENT, "No logs found");
                return;
            }
            int lastLogCount = -1;
            while (!stopMonitoring) {
                logsList = FileTool.listFiles(logLocation);
                if (logsList.length == 0) {
                    synchronized (this) {
                        wait(1000);
                    }
                    continue;
                }
                Arrays.sort(logsList, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
                File latestLog = logsList[0];
                if (latestLog.equals(ignoredFirstLog)) {
                    synchronized (this) {
                        wait(1000);
                    }
                    continue;
                }
                if (!latestLog.equals(currentLog)) {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException ignored) {
                        }
                    }
                    reader = new BufferedReader(new InputStreamReader(new FileInputStream(latestLog)));
                    currentLog = latestLog;
                    lastLogCount = logsList.length;
                    if (!isFoundANewLog) {
                        isFoundANewLog = true;
                        App.getLogger().writeLine(LOG_IDENT, "Now watching new log: " + currentLog.getName());
                    }
                }
                String line = reader.readLine();
                if (line != null) {
                    HandleLogEntry(line);
                } else {
                    File[] currentLogs = FileTool.listFiles(logLocation);
                    if (currentLogs.length > lastLogCount) {
                        Arrays.sort(currentLogs, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
                        File newestLog = currentLogs[0];
                        if (!newestLog.getName().equals(currentLog.getName())) {
                            App.getLogger().writeLine(LOG_IDENT, "Detected new log while monitoring → disposing watcher");
                            CustomWatcher.getInstance().Dispose();
                            return;
                        }
                    }
                    synchronized (this) {
                        wait(1000);
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            App.getLogger().writeLine(LOG_IDENT, "Failed to run activity watcher!");
            App.getLogger().writeException(LOG_IDENT, e);
            Thread.currentThread().interrupt();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {}
            }
            NotifyIconWrapper.hideConnectionNotification(App.getAppContext());
            executor.shutdownNow();
        }
    }

    private void HandleLogEntry(String line) {
        final String LOG_IDENT = "ActivityWatcher::HandleLogEntry";

        final String joinAServerStr = "[FLog::Output] ! Joining game";
        final String leaveAServerStr = "[FLog::Network] Time to disconnect replication data";
        final String connectionStr = "Info [DFLog::NetworkClient] Connection accepted from";
        final String universeStr = "[FLog::GameJoinLoadTime] Report game_join_loadtime:";

        try {
            ActivityData activityData = CustomWatcher.getInstance().getActivityData();
            if (isInExperience && activityData.getPlaceId() != 0 && line.contains(leaveAServerStr)) {
                activityData.setTimeLeft(new Date());
                if (activityData.getRootPlaceId() == activityData.getPlaceId()) {
                    if (lastTeleportActivityData != null) {
                        lastTeleportActivityData.setTimeLeft(new Date());
                    }
                    history.add(activityData);
                } else {
                    lastTeleportActivityData = activityData;
                    App.getLogger().writeLine(LOG_IDENT, "Added activity to history → left and joined another game");
                }
                isInExperience = false;
                CustomWatcher.getInstance().setActivityData(new ActivityData(""));
                NotifyIconWrapper.hideConnectionNotification(App.getAppContext());
            } else if (!isInExperience && activityData.getPlaceId() == 0 && line.contains(joinAServerStr)) {
                try {
                    int jobStart = line.indexOf('\'', line.indexOf(joinAServerStr)) + 1;
                    int jobEnd = line.indexOf('\'', jobStart);
                    String jobId = (jobStart > 0 && jobEnd > jobStart) ? line.substring(jobStart, jobEnd).trim() : "";
                    int placeStart = line.indexOf("place ", jobEnd) + 6;
                    int placeEnd = line.indexOf(" at", placeStart);
                    String placeIdStr = (placeStart >= 6 && placeEnd > placeStart) ? line.substring(placeStart, placeEnd).trim() : "";
                    int ipStart = line.indexOf(" at ", placeEnd) + 4;
                    String serverIP = (ipStart >= 4 && ipStart < line.length()) ? line.substring(ipStart).trim() : "";
                    if (!placeIdStr.isEmpty() && !jobId.isEmpty()) {
                        App.getLogger().writeLine(LOG_IDENT, "Joining game → placeId=" + placeIdStr + ", jobId=" + jobId + ", serverIP=" + serverIP);
                        isInExperience = true;
                        activityData.setTimeJoined(new Date());
                        activityData.setPlaceId(Long.parseLong(placeIdStr));
                        activityData.setJobId(jobId);
                    } else {
                        App.getLogger().writeLine(LOG_IDENT, "Failed to parse join experience info or game info: " + line);
                    }
                } catch (Exception e) {
                    App.getLogger().writeLine(LOG_IDENT, "Failed to parse join experience info or game info");
                    App.getLogger().writeException(LOG_IDENT, e);
                }
            } else if (isInExperience && activityData.getPlaceId() != 0 && line.contains(connectionStr)) {
                String[] parts = line.trim().split(" ");
                if (parts.length > 0) {
                    String lastPart = parts[parts.length - 1];
                    String[] ipSplit = lastPart.split("\\|");
                    if (ipSplit.length >= 1) {
                        String location = ipSplit[0].trim();
                        App.getLogger().writeLine(LOG_IDENT, "Connection string: " + line);
                        activityData.setMachineAddress(location);
                        if (!activityData.getMachineAddress().isEmpty() && activityData.getUniverseId() != 0) {
                            NotifyIconWrapper.showConnectionNotification(App.getAppContext(), activityData.getMachineAddress(), String.valueOf(activityData.getUniverseId()));
                        } else {
                            App.getLogger().writeLine(LOG_IDENT, "Failed to parse connection string: " + line);
                            App.getLogger().writeLine(LOG_IDENT, "Machine address: " + activityData.getMachineAddress());
                            App.getLogger().writeLine(LOG_IDENT, "Universe ID: " + activityData.getUniverseId());
                        }
                    } else {
                        App.getLogger().writeLine(LOG_IDENT, "Unexpected connection format: " + lastPart);
                    }
                }
            } else if (isInExperience && activityData.getPlaceId() != 0 && line.contains(universeStr)) {
                try {
                    int uniIndex = line.indexOf("universeid:");
                    if (uniIndex == -1) return;
                    int universeEnd = line.indexOf(",", uniIndex);
                    if (universeEnd == -1) universeEnd = line.length();
                    String universeIdStr = line.substring(uniIndex + 11, universeEnd).trim();
                    long currentUniverseId = Long.parseLong(universeIdStr);

                    new Thread(() -> {
                        try {
                            activityData.setUniverseId(currentUniverseId);
                        } catch (Exception e) {
                            App.getLogger().writeLine(LOG_IDENT, "Failed to process universe ID");
                            App.getLogger().writeException(LOG_IDENT, e);
                        }
                    }).start();
                } catch (Exception e) {
                    App.getLogger().writeLine(LOG_IDENT, "Failed to parse universe id info");
                    App.getLogger().writeException(LOG_IDENT, e);
                }
            }
        } catch (Exception e) {
            App.getLogger().writeLine(LOG_IDENT, "Failed to handle log entry");
            App.getLogger().writeException(LOG_IDENT, e);
        }
    }

    public boolean isStopMonitoring() {
        return stopMonitoring;
    }

    public void Dispose() {
        final String LOG_IDENT = "ActivityWatcher::Dispose";
        stopMonitoring = true;
        App.getLogger().writeLine(LOG_IDENT, "Disposed activity watcher session");
        NotifyIconWrapper.hideConnectionNotification(App.getAppContext());
        executor.shutdownNow();
    }
}