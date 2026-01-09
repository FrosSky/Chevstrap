package com.chevstrap.rbx.Models.APIs.Roblox;

import org.json.JSONObject;

public class ThumbnailResponse {
    private final String requestId;
    private final String targetId;
    private final String state;
    private final String imageUrl;
    private final int errorCode;
    private final String errorMessage;

    public ThumbnailResponse(JSONObject json) {
        requestId = json.optString("requestId", "");
        targetId = json.optString("targetId", "");
        state = json.optString("state", "");
        imageUrl = json.optString("imageUrl", "");
        errorCode = json.optInt("errorCode", 0);
        errorMessage = json.optString("errorMessage", "");
    }

    public String getRequestId() { return requestId; }
    public String getTargetId() { return targetId; }
    public String getState() { return state; }
    public String getImageUrl() { return imageUrl; }
    public int getErrorCode() { return errorCode; }
    public String getErrorMessage() { return errorMessage; }
}