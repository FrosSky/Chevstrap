package com.chevstrap.rbx.Models.Entities;

import com.chevstrap.rbx.App;
import com.chevstrap.rbx.Models.APIs.Roblox.GameDetailResponse;
import com.chevstrap.rbx.Models.APIs.Roblox.ThumbnailResponse;
import com.chevstrap.rbx.Utility.HTTPFetcher;

import org.json.JSONArray;
import org.json.JSONObject;

public class UniverseDetails {
    private GameDetailResponse data;
    private ThumbnailResponse thumbnail;

    public GameDetailResponse getData() {
        return data;
    }

    public ThumbnailResponse getThumbnail() {
        return thumbnail;
    }

    public interface FetchCallback {
        void onFetched(UniverseDetails universeDetails);
        void onFailed(Exception e);
    }

    public static void fetchSingle(long universeId, FetchCallback callback) {
        new Thread(() -> {
            try {
                String gameResponse = HTTPFetcher.getJson(
                        "https://games.roblox.com/v1/games?universeIds=" + universeId
                ).toString();
                JSONObject gameJson = new JSONObject(gameResponse);
                JSONArray gameDataArray = gameJson.getJSONArray("data");

                if (gameDataArray.length() == 0)
                    throw new Exception("Roblox API returned invalid game data");

                JSONObject gameObj = gameDataArray.optJSONObject(0);
                if (gameObj == null)
                    throw new Exception("Game object missing in response");

                String thumbResponse = HTTPFetcher.getJson(
                        "https://thumbnails.roblox.com/v1/games/icons?universeIds=" + universeId +
                                "&returnPolicy=PlaceHolder&size=128x128&format=Png&isCircular=false"
                ).toString();
                JSONObject thumbJson = new JSONObject(thumbResponse);
                JSONArray thumbDataArray = thumbJson.getJSONArray("data");
                JSONObject thumbObj = thumbDataArray.optJSONObject(0);
                if (thumbObj == null)
                    throw new Exception("Thumbnail object missing in response");

                UniverseDetails u = new UniverseDetails();
                u.data = new GameDetailResponse(gameObj);
                u.thumbnail = new ThumbnailResponse(thumbObj);

                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onFetched(u));

            } catch (Exception e) {
                App.getLogger().writeException("UniverseDetails", e);
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onFailed(e));
            }
        }).start();
    }
}
