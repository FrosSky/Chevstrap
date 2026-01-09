package com.chevstrap.rbx.Models.APIs.Roblox;

import org.json.JSONObject;

public class GameCreator {
    private final long id;
    private final String name;
    private final String type;
    private final boolean isRNVAccount;
    private final boolean hasVerifiedBadge;

    public GameCreator(JSONObject obj) {
        this.id = obj.optLong("id");
        this.name = obj.optString("name");
        this.type = obj.optString("type");
        this.isRNVAccount = obj.optBoolean("isRNVAccount");
        this.hasVerifiedBadge = obj.optBoolean("hasVerifiedBadge");
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isRNVAccount() {
        return isRNVAccount;
    }

    public boolean hasVerifiedBadge() {
        return hasVerifiedBadge;
    }
}
