package com.chevstrap.rbx.Models.APIs.Roblox;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GameDetailResponse {
    private final long id;
    private final long rootPlaceId;
    private final String name;
    private final String description;
    private final String sourceName;
    private final String sourceDescription;
    private GameCreator creator;
    private Long price;
    private final List<String> allowedGearGenres;
    private final List<String> allowedGearCategories;
    private final boolean isGenreEnforced;
    private final boolean copyingAllowed;
    private final long playing;
    private final long visits;
    private final int maxPlayers;
    private final String created;
    private final String updated;
    private final boolean studioAccessToApisAllowed;
    private final boolean createVipServersAllowed;
    private final String universeAvatarType;
    private final String genre;
    private final boolean isAllGenre;
    private final boolean isFavoritedByUser;
    private final int favoritedCount;

    public GameDetailResponse(JSONObject obj) {
        this.id = obj.optLong("id");
        this.rootPlaceId = obj.optLong("rootPlaceId");
        this.name = obj.optString("name");
        this.description = obj.optString("description");
        this.sourceName = obj.optString("sourceName");
        this.sourceDescription = obj.optString("sourceDescription");

        JSONObject creatorObj = obj.optJSONObject("creator");
        if (creatorObj != null) {
            this.creator = new GameCreator(creatorObj);
        }

        if (obj.has("price") && !obj.isNull("price"))
            this.price = obj.optLong("price");

        this.allowedGearGenres = toStringList(obj.optJSONArray("allowedGearGenres"));
        this.allowedGearCategories = toStringList(obj.optJSONArray("allowedGearCategories"));

        this.isGenreEnforced = obj.optBoolean("isGenreEnforced");
        this.copyingAllowed = obj.optBoolean("copyingAllowed");
        this.playing = obj.optLong("playing");
        this.visits = obj.optLong("visits");
        this.maxPlayers = obj.optInt("maxPlayers");
        this.created = obj.optString("created");
        this.updated = obj.optString("updated");
        this.studioAccessToApisAllowed = obj.optBoolean("studioAccessToApisAllowed");
        this.createVipServersAllowed = obj.optBoolean("createVipServersAllowed");
        this.universeAvatarType = obj.optString("universeAvatarType");
        this.genre = obj.optString("genre");
        this.isAllGenre = obj.optBoolean("isAllGenre");
        this.isFavoritedByUser = obj.optBoolean("isFavoritedByUser");
        this.favoritedCount = obj.optInt("favoritedCount");
    }

    private static List<String> toStringList(JSONArray array) {
        List<String> list = new ArrayList<>();
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                list.add(array.optString(i));
            }
        }
        return list;
    }

    public long getId() {
        return id;
    }

    public long getRootPlaceId() {
        return rootPlaceId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getSourceName() {
        return sourceName;
    }

    public String getSourceDescription() {
        return sourceDescription;
    }

    public GameCreator getCreator() {
        return creator;
    }

    public Long getPrice() {
        return price;
    }

    public List<String> getAllowedGearGenres() {
        return allowedGearGenres;
    }

    public List<String> getAllowedGearCategories() {
        return allowedGearCategories;
    }

    public boolean isGenreEnforced() {
        return isGenreEnforced;
    }

    public boolean isCopyingAllowed() {
        return copyingAllowed;
    }

    public long getPlaying() {
        return playing;
    }

    public long getVisits() {
        return visits;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public String getCreated() {
        return created;
    }

    public String getUpdated() {
        return updated;
    }

    public boolean isStudioAccessToApisAllowed() {
        return studioAccessToApisAllowed;
    }

    public boolean isCreateVipServersAllowed() {
        return createVipServersAllowed;
    }

    public String getUniverseAvatarType() {
        return universeAvatarType;
    }

    public String getGenre() {
        return genre;
    }

    public boolean isAllGenre() {
        return isAllGenre;
    }

    public boolean isFavoritedByUser() {
        return isFavoritedByUser;
    }

    public int getFavoritedCount() {
        return favoritedCount;
    }
}
