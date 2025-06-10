package com.example.spotly.model;

import com.google.gson.annotations.SerializedName;

public class PlaceResult {

    @SerializedName("place_id")
    private long placeId;

    @SerializedName("display_name")
    private String displayName;

    @SerializedName("lat")
    private String latitude;

    @SerializedName("lon")
    private String longitude;

    @SerializedName("type")
    private String type;

    @SerializedName("icon")
    private String iconUrl;

    public long getPlaceId() {
        return placeId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getType() {
        return type;
    }

    public String getIconUrl() {
        return iconUrl;
    }
}