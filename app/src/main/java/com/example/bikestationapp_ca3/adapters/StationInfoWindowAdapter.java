package com.example.bikestationapp_ca3.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.bikestationapp_ca3.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class StationInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final View window;
    private Context context;

    public StationInfoWindowAdapter(Context context) {
        this.context = context;
        window = LayoutInflater.from(context).inflate(R.layout.station_info_window, null);
    }

    public void renderWindowText(Marker marker, View view) {
        String name = marker.getTitle();
        TextView tvName = (TextView) view.findViewById(R.id.iw_name);
        tvName.setText(name);

        String snippet = marker.getSnippet();
        TextView tvSnippet = view.findViewById(R.id.iw_snippet);
        tvSnippet.setText(snippet);

    }

    @Nullable
    @Override
    public View getInfoContents(@NonNull Marker marker) {
        renderWindowText(marker, window);
        return window;
    }

    @Nullable
    @Override
    public View getInfoWindow(@NonNull Marker marker) {
        renderWindowText(marker, window);
        return window;
    }
}
