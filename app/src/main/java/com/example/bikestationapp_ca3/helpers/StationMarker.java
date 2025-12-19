package com.example.bikestationapp_ca3.helpers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class StationMarker implements ClusterItem {
    private LatLng position;
    private String name;
    private String snippet;
    private BitmapDescriptor icon;

    public StationMarker(LatLng latlng, String name, String snippet) {
        position = latlng;
        this.name = name;
        this.snippet = snippet;
    }

    @NonNull
    @Override
    public LatLng getPosition() {
        return position;
    }

    @Nullable
    @Override
    public String getTitle() {
        return name;
    }

    @Nullable
    @Override
    public String getSnippet() {
        return snippet;
    }

    @Nullable
    @Override
    public Float getZIndex() {
        return 0f;
    }

    public BitmapDescriptor getIcon() {
        return icon;
    }



    public void setIcon(BitmapDescriptor icon) {
        this.icon = icon;
    }
}
