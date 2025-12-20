package com.example.bikestationapp_ca3.fragments;

import static com.example.bikestationapp_ca3.BuildConfig.MAPS_API_KEY;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;

import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 */
public class StationsFragment extends Fragment {

    StationsRecyclerViewAdapter adapter;
    StationViewModel stationViewModel;
    LocationViewModel locationViewModel;
    Location currentUserLocation;
    GeoApiContext geoApiContext;
    String distanceToStation;

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
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
            for (Station s : stations) {
                s.setDistance(calculateDistance(new LatLng(s.getPosition().get("lat"), s.getPosition().get("lng"))));
            }
            adapter.updateStations(stations);
        });
    }

    public void observeLocation() {
        locationViewModel.getCurrentLocation().observe(getViewLifecycleOwner(), location ->
        {
            currentUserLocation = location;
        });
    }

    public String calculateDistance(LatLng stationPos) {
        DirectionsApiRequest directions = new DirectionsApiRequest(geoApiContext);

        directions.origin(new com.google.maps.model.LatLng(
                currentUserLocation.getLatitude(),
                currentUserLocation.getLongitude()
        ));

        directions.mode(TravelMode.WALKING);
        directions.destination(new com.google.maps.model.LatLng(
                stationPos.latitude,
                stationPos.longitude
        )).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                distanceToStation = result.routes[0].legs[0].distance.humanReadable;
            }

            @Override
            public void onFailure(Throwable e) {

            }
        });

        return distanceToStation;
    }
}