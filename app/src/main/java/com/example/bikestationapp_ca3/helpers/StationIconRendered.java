package com.example.bikestationapp_ca3.helpers;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

public class StationIconRendered extends DefaultClusterRenderer<StationMarker> {

    public StationIconRendered(Context context, GoogleMap map, ClusterManager<StationMarker> clusterManager) {
        super(context, map, clusterManager);
    }

    public void onBeforeClusterItemRendered(StationMarker marker, MarkerOptions markerOptions) {
        markerOptions.icon(marker.getIcon());
        super.onBeforeClusterItemRendered(marker, markerOptions);
    }
}
