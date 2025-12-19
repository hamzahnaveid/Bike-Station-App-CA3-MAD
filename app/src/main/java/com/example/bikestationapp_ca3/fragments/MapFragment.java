package com.example.bikestationapp_ca3.fragments;

import static com.example.bikestationapp_ca3.BuildConfig.MAPS_API_KEY;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.bikestationapp_ca3.R;
import com.example.bikestationapp_ca3.adapters.StationInfoWindowAdapter;
import com.example.bikestationapp_ca3.data_classes.Station;
import com.example.bikestationapp_ca3.helpers.StationIconRendered;
import com.example.bikestationapp_ca3.helpers.StationMarker;
import com.example.bikestationapp_ca3.viewmodels.LocationViewModel;
import com.example.bikestationapp_ca3.viewmodels.StationViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;

import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment {

    GoogleMap googleMap;
    GeoApiContext geoApiContext;
    StationViewModel stationViewModel;
    LocationViewModel locationViewModel;
    ClusterManager<StationMarker> clusterManager;
    Location userPosition;
    Polyline currentPolyline;

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
            setUpClusterer();
            Log.d("GoogleMap", "Map ready");
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

        locationViewModel = new ViewModelProvider(requireActivity()).get(LocationViewModel.class);
        locationViewModel.loadLocation();

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }

        if(geoApiContext == null) {
            geoApiContext = new GeoApiContext.Builder()
                    .apiKey(MAPS_API_KEY)
                    .build();
        }

        Log.d("MapFragment", "View created");

        observeStations();
        observeLocation();
    }

    public void observeStations() {
        stationViewModel.getStations().observe(getViewLifecycleOwner(), stations ->
        {
            if (googleMap != null) {
                populateMapWithStations(stations);
            }
        });
    }

    public void observeLocation() {
        locationViewModel.getCurrentLocation().observe(getViewLifecycleOwner(), location ->
        {
            userPosition = location;
        });
    }

    public void populateMapWithStations(List<Station> stations) {
        googleMap.setInfoWindowAdapter(new StationInfoWindowAdapter(getActivity()));

        for (Station s : stations) {
            String snippet = "Status: " + s.getStatus() + "\n" +
                    "Available Bikes: " + s.getAvailable_bikes() + "\n" +
                    "Available Stands: " + s.getAvailable_bike_stands() + "\n" +
                    "Total Stands: " + s.getBike_stands();

            double lat = s.getPosition().get("lat");
            double lng = s.getPosition().get("lng");
            LatLng stationLatLng = new LatLng(lat, lng);
            String address = s.getAddress();

            StationMarker marker = new StationMarker(stationLatLng, address, snippet);

            if (s.getStatus().equals("CLOSED")) {
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(getBitmapFromDrawable(R.drawable.closed_station_icon)));
            }
            else if (s.getAvailable_bikes() == 0 && s.getAvailable_bike_stands() > 0) {
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(getBitmapFromDrawable(R.drawable.no_bikes_station_icon)));

            }
            else {
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(getBitmapFromDrawable(R.drawable.station_icon)));
            }
            clusterManager.addItem(marker);

            Log.d("StationMarker", "Marker added: " + s.getName());
        }
    }

    public void calculateDirections(StationMarker marker) {
        DirectionsApiRequest directions = new DirectionsApiRequest(geoApiContext);

        directions.origin(new com.google.maps.model.LatLng(
                userPosition.getLatitude(),
                userPosition.getLongitude()
        ));

        directions.mode(TravelMode.WALKING);
        directions.destination(new com.google.maps.model.LatLng(
                marker.getPosition().latitude,
                marker.getPosition().longitude
        )).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                addPolylineToMap(result);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(),
                                        "Duration: " + result.routes[0].legs[0].duration + "\n" +
                                             "Distance: " + result.routes[0].legs[0].distance,
                                        Toast.LENGTH_LONG)
                                .show();
                    }
                });
                Log.d("calculateDirections", "Routes: " + result.routes[0].toString());
                Log.d("calculateDirections", "Distance: " + result.routes[0].legs[0].distance);
                Log.d("calculateDirections", "Duration: " + result.routes[0].legs[0].duration);
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e("calculateDirections", "Failed to get directions: " + e.getMessage());
            }
        });
    }

    public void addPolylineToMap(DirectionsResult result) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (currentPolyline != null) {
                    currentPolyline.remove();
                }
                List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(result.routes[0].overviewPolyline.getEncodedPath());
                List<LatLng> newDecodedPath = new ArrayList<>();

                for(com.google.maps.model.LatLng latLng : decodedPath) {
                    newDecodedPath.add(new LatLng(
                            latLng.lat,
                            latLng.lng
                    ));
                }
                Polyline polyline = googleMap.addPolyline(new PolylineOptions().color(Color.CYAN).geodesic(true).addAll(newDecodedPath));
                currentPolyline = polyline;
            }
        });
    }

    public Bitmap getBitmapFromDrawable(int resId) {
        Bitmap bitmap = null;
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), resId, null);

        if (drawable != null) {
            bitmap = Bitmap.createBitmap(120, 120, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        }

        return bitmap;
    }

    public void setUpClusterer() {
        clusterManager = new ClusterManager<StationMarker>(getActivity(), googleMap);
        clusterManager.setRenderer(new StationIconRendered(getActivity(), googleMap, clusterManager));
        googleMap.setOnCameraIdleListener(clusterManager);
        googleMap.setOnMarkerClickListener(clusterManager);

        clusterManager.setOnClusterItemInfoWindowClickListener(new ClusterManager.OnClusterItemInfoWindowClickListener<StationMarker>() {
            @Override
            public void onClusterItemInfoWindowClick(StationMarker item) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setMessage("Determine walking route to " + item.getTitle() + "?")
                                .setCancelable(true)
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                calculateDirections(item);
                                                dialog.dismiss();
                                            }
                                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .show();
            }
        });
    }
}