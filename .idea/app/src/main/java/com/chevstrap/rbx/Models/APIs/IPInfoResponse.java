package com.chevstrap.rbx.Models.APIs;

import org.json.JSONObject;

public class IPInfoResponse {
    private final String city;
    private final String region;
    private final String country;

    public IPInfoResponse(String city, String region, String country) {
        this.city = city;
        this.region = region;
        this.country = country;
    }

    public String getCity() { return city; }
    public String getRegion() { return region; }
    public String getCountry() { return country; }

    public static IPInfoResponse parse(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            String city = jsonObject.optString("city", "");
            String region = jsonObject.optString("region", "");
            String country = jsonObject.optString("country", "");
            return new IPInfoResponse(city, region, country);
        } catch (Exception e) {
            return null;
        }
    }
}
