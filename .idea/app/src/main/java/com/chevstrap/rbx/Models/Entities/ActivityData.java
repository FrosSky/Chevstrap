package com.chevstrap.rbx.Models.Entities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.chevstrap.rbx.App;
import com.chevstrap.rbx.AppDirectories.RobloxClientData;
import com.chevstrap.rbx.CustomWatcher;
import com.chevstrap.rbx.Enums.ServerType;
import com.chevstrap.rbx.Models.APIs.IPInfoResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Semaphore;

public class ActivityData {
    private static final String LOG_IDENT = "ActivityData";

    private long universeId = 0;
//    public ActivityData rootActivity;

    private long rootPlaceId = 0;
    private long lastUniverseId = 0;
    private long placeId = 0;
    private String jobId = "";
//    private String accessCode = "";
//    private long userId = 0;
    private String machineAddress = "";
    private boolean isTeleport = false;
//    private ServerType serverType = ServerType.PUBLIC;
    private Date timeJoined;
    private Date timeLeft;
    public UniverseDetails universeDetails;
    private final Semaphore serverQuerySemaphore = new Semaphore(1);

    public ActivityData(String machineAddress) {
        this.machineAddress = machineAddress;
    }

    public long getUniverseId() { return universeId; }

    public void setUniverseId(long universeId) {
        this.universeId = universeId;
        App.getLogger().writeLine(LOG_IDENT, "Universe ID set to " + universeId);

        if (universeDetails == null) {
            UniverseDetails.fetchSingle(universeId, new UniverseDetails.FetchCallback() {
                @Override
                public void onFetched(UniverseDetails u) {
                    universeDetails = u;

                    if (lastUniverseId == universeDetails.getData().getId()) {
                        isTeleport = true;
                    }

                    setRootPlaceId(universeDetails.getData().getRootPlaceId());
                }
                @Override
                public void onFailed(Exception e) {
                    App.getLogger().writeLine(LOG_IDENT, "Failed to fetch universe details");
                    App.getLogger().writeException(LOG_IDENT, e);
                }
            });
        }
    }

    public long getRootPlaceId() {
        return rootPlaceId;
    }
    public void setRootPlaceId(long val) {
        this.rootPlaceId = val;
        App.getLogger().writeLine(LOG_IDENT, "Root place ID set to " + val);
    }

    public long getPlaceId() { return placeId; }
    public void setPlaceId(long placeId) {
        this.placeId = placeId;
        App.getLogger().writeLine(LOG_IDENT, "Place ID set to " + placeId);
    }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) {
        this.jobId = jobId;
        App.getLogger().writeLine(LOG_IDENT, "Job ID set to " + jobId);
    }

//    public String getAccessCode() { return accessCode; }
//    public void setAccessCode(String accessCode) {
//        this.accessCode = accessCode;
//        App.getLogger().writeLine(LOG_IDENT, "Access code updated");
//    }

//    public long getUserId() { return userId; }
//    public void setUserId(long userId) {
//        this.userId = userId;
//        App.getLogger().writeLine(LOG_IDENT, "User ID set to " + userId);
//    }

    public String getMachineAddress() { return machineAddress; }
    public void setMachineAddress(String machineAddress) {
        this.machineAddress = machineAddress;
        App.getLogger().writeLine(LOG_IDENT, "Machine address set to " + machineAddress);
    }

    public boolean isMachineAddressValid() {
        boolean valid = machineAddress != null && !machineAddress.isEmpty() && !machineAddress.startsWith("10.");
        App.getLogger().writeLine(LOG_IDENT, "Machine address valid = " + valid);
        return valid;
    }

    public boolean isTeleport() { return isTeleport; }
    public void setTeleport(boolean teleport) {
        this.isTeleport = teleport;
        App.getLogger().writeLine(LOG_IDENT, "Teleport flag = " + teleport);
    }

//    public ServerType getServerType() { return serverType; }
//    public void setServerType(ServerType serverType) {
//        this.serverType = serverType;
//        App.getLogger().writeLine(LOG_IDENT, "Server type = " + serverType);
//    }

//    public Date getTimeJoined() { return timeJoined; }
    public void setTimeJoined(Date timeJoined) {
        this.timeJoined = timeJoined;
        App.getLogger().writeLine(LOG_IDENT, "Time joined = " + timeJoined);
    }

//    public Date getTimeLeft() { return timeLeft; }
    public void setTimeLeft(Date timeLeft) {
        this.timeLeft = timeLeft;
        App.getLogger().writeLine(LOG_IDENT, "Time left = " + timeLeft);
    }

    public String getGameHistoryDescription() {
        Locale locale = Locale.getDefault();
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", locale);

        String creatorName = "";
        if (universeDetails != null && universeDetails.getData() != null && universeDetails.getData().getCreator() != null) {
            creatorName = universeDetails.getData().getCreator().getName();

        }

        String joinedText = (timeJoined != null) ? timeFormat.format(timeJoined) : "N/A";
        String leftText = (timeLeft != null) ? timeFormat.format(timeLeft) : "N/A";

        String separator = locale.getLanguage().startsWith("ja") ? "~" : "-";
        String desc = creatorName + " • " + joinedText + " " + separator + " " + leftText;

        App.getLogger().writeLine(LOG_IDENT, "Game history description generated: " + desc);
        App.getLogger().writeLine(LOG_IDENT, "Creator name: " + creatorName);

        if (universeDetails != null && universeDetails.getData() != null && universeDetails.getData().getCreator() != null) {
            App.getLogger().writeLine(LOG_IDENT, "Creator name: " + creatorName);
        }

        return desc;
    }

    public String getInviteDeeplink(String PrivateServerAccessCode) {
        StringBuilder deeplink = new StringBuilder("roblox://experiences/start?placeId=" + placeId);
        if (!PrivateServerAccessCode.isEmpty()) {
//            deeplink.append("&accessCode=").append(accessCode);
//        } else {
            deeplink.append("&gameInstanceId=").append(jobId);
        }
        String link = deeplink.toString();
        App.getLogger().writeLine(LOG_IDENT, "Generated deeplink: " + link);
        return link;
    }

    private void fetchIPInfoIo(String ip, IPInfoCallback callback) {
        App.getLogger().writeLine(LOG_IDENT, "Fetching IP info for " + ip);
        new Thread(() -> {
            try {
                URL url = new URL("https://ipinfo.io/" + ip + "/json");
                JSONObject json = getJsonObject(url);
                new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(json));
                App.getLogger().writeLine(LOG_IDENT, "IP info fetch success for " + ip);
            } catch (Exception e) {
                App.getLogger().writeException(LOG_IDENT, e);
                new Handler(Looper.getMainLooper()).post(() -> callback.onError(e));
            }
        }).start();
    }

    @NonNull
    private static JSONObject getJsonObject(URL url) throws IOException, JSONException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) result.append(line);
        reader.close();

        return new JSONObject(result.toString());
    }

    public interface IPInfoCallback {
        void onSuccess(JSONObject json);
        void onError(Exception e);
    }

    public interface LocationCallback {
        void onLocationResolved(String location);
        void onFailure();
    }

    public void queryServerLocation(LocationCallback callback) {
        if (!isMachineAddressValid()) {
            App.getLogger().writeLine(LOG_IDENT, "Invalid machine address, cannot resolve server location");
            callback.onFailure();
            return;
        }

        new Thread(() -> {
            try {
                serverQuerySemaphore.acquire();
                fetchIPInfoIo(machineAddress, new IPInfoCallback() {
                    @Override
                    public void onSuccess(JSONObject json) {
                        try {
                            IPInfoResponse ipInfo = IPInfoResponse.parse(json.toString());
                            if (ipInfo == null) {
                                callback.onFailure();
                                App.getLogger().writeLine(LOG_IDENT, "Failed to parse IP info response");
                                return;
                            }

                            String city = ipInfo.getCity();
                            String region = ipInfo.getRegion();
                            String country = ipInfo.getCountry();

                            if (city == null || city.isEmpty()) {
                                callback.onFailure();
                                App.getLogger().writeLine(LOG_IDENT, "Invalid IP info: city missing");
                                return;
                            }

                            String location = city.equals(region)
                                    ? region + ", " + country
                                    : city + ", " + region + ", " + country;

                            App.getLogger().writeLine(LOG_IDENT, "Resolved server location: " + location);
                            callback.onLocationResolved(location);
                        } catch (Exception e) {
                            App.getLogger().writeException(LOG_IDENT, e);
                            callback.onFailure();
                        } finally {
                            serverQuerySemaphore.release();
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        App.getLogger().writeException(LOG_IDENT, e);
                        serverQuerySemaphore.release();
                        callback.onFailure();
                    }
                });
            } catch (InterruptedException e) {
                App.getLogger().writeException(LOG_IDENT, e);
                callback.onFailure();
            }
        }).start();
    }

    public void rejoinServer(Context context) {
        App.getLogger().writeLine(LOG_IDENT, "Rejoining server via deeplink: " + getInviteDeeplink(""));
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getInviteDeeplink("")));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

//    public Runnable getRejoinServerCommand(Context context) {
//        return () -> rejoinServer(context);
//    }
}
