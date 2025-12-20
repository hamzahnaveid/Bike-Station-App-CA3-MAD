package com.example.bikestationapp_ca3.fragments;

import static com.example.bikestationapp_ca3.BuildConfig.MAPS_API_KEY;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bikestationapp_ca3.R;
import com.example.bikestationapp_ca3.adapters.StationsRecyclerViewAdapter;
import com.example.bikestationapp_ca3.data_classes.Station;
import com.example.bikestationapp_ca3.viewmodels.LocationViewModel;
import com.example.bikestationapp_ca3.viewmodels.StationViewModel;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class StationsFragment extends Fragment {

    List<Station> cachedStation = new ArrayList<>();
    int pendingDistances = 0;
    StationsRecyclerViewAdapter adapter;
    StationViewModel stationViewModel;
    LocationViewModel locationViewModel;
    Location currentUserLocation;
    GeoApiContext geoApiContext;

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;

    public StationsFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static StationsFragment newInstance(int columnCount) {
        StationsFragment fragment = new StationsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stations_list, container, false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(geoApiContext == null) {
            geoApiContext = new GeoApiContext.Builder()
                    .apiKey(MAPS_API_KEY)
                    .build();
        }

        adapter = new StationsRecyclerViewAdapter(new ArrayList<>());

        locationViewModel = new ViewModelProvider(requireActivity()).get(LocationViewModel.class);
        locationViewModel.loadLocation();

        stationViewModel = new ViewModelProvider(requireActivity()).get(StationViewModel.class);
        stationViewModel.loadStations();

        observeLocation();
        observeStations();

        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recyclerView.setAdapter(adapter);
        }
    }

    public void observeStations() {
        stationViewModel.getStations().observe(getViewLifecycleOwner(), stations ->
        {
            cachedStation.clear();
            pendingDistances = stations.size();

            for (Station s : stations) {
                calculateDistance(s);
            }
        });
    }

    public void observeLocation() {
        locationViewModel.getCurrentLocation().observe(getViewLifecycleOwner(), location ->
        {
            currentUserLocation = location;
        });
    }

    public void calculateDistance(Station station) {
        DirectionsApiRequest directions = new DirectionsApiRequest(geoApiContext);

        directions.origin(new com.google.maps.model.LatLng(
                currentUserLocation.getLatitude(),
                currentUserLocation.getLongitude()
        ));

        directions.mode(TravelMode.WALKING);
        directions.destination(new com.google.maps.model.LatLng(
                station.getPosition().get("lat"),
                station.getPosition().get("lng")
        )).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                station.setDistance((double) (result.routes[0].legs[0].distance.inMeters) / 1000);
                cachedStation.add(station);

                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pendingDistances--;
                        if (pendingDistances == 0) {
                            Collections.sort(cachedStation, new Comparator<Station>() {
                                @Override
                                public int compare(Station o1, Station o2) {
                                    return Double.compare(o1.getDistance(), o2.getDistance());
                                }
                            });
                            adapter.updateStations(cachedStation);
                        }
                    }
                });
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e("calculateDirections", "Failed to get directions: " + e.getMessage());
            }
        });
    }
}