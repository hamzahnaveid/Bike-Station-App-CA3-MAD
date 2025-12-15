package com.example.bikestationapp_ca3.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.bikestationapp_ca3.R;
import com.example.bikestationapp_ca3.adapters.StationInfoWindowAdapter;
import com.example.bikestationapp_ca3.classes.Station;
import com.example.bikestationapp_ca3.viewmodels.StationViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class MapFragment extends Fragment {

    GoogleMap googleMap;
    StationViewModel stationViewModel;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap map) {
            googleMap = map;

            googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            LatLng dublin = new LatLng(53.34550694182451, -6.269892450557356);
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(dublin).zoom(12).build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            googleMap.setTrafficEnabled(true);
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            googleMap.setMyLocationEnabled(true);

            Log.d("GoogleMap", "Map ready");

            List<Station> stations = stationViewModel.getStations().getValue();
            if (stations != null) {
                populateMapWithStations(stations);
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        stationViewModel = new ViewModelProvider(requireActivity()).get(StationViewModel.class);
        stationViewModel.loadStations();

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
        Log.d("MapFragment", "View created");

        observeStations();
    }

    public void observeStations() {
        stationViewModel.getStations().observe(getViewLifecycleOwner(), stations ->
        {
            if (googleMap != null) {
                populateMapWithStations(stations);
            }
        });
    }

    public void populateMapWithStations(List<Station> stations) {
        googleMap.setInfoWindowAdapter(new StationInfoWindowAdapter(getActivity()));

        for (Station s : stations) {
            String snippet = "Status: " + s.getStatus() + "\n" +
                    "Total Bike Stands: " + s.getBike_stands() + "\n" +
                    "Available Bike Stands: " + s.getAvailable_bike_stands() + "\n" +
                    "Available Bikes: " + s.getAvailable_bikes();
            double lat = s.getPosition().get("lat");
            double lng = s.getPosition().get("lng");

            LatLng stationLatLng = new LatLng(lat, lng);
            MarkerOptions options = new MarkerOptions()
                    .position(stationLatLng)
                    .title(s.getAddress())
                    .snippet(snippet);
            googleMap.addMarker(options);

            Log.d("StationMarker", "Marker added: " + s.getName());
        }
    }
}