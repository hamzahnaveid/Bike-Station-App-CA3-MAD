package com.example.bikestationapp_ca3.fragments;

import static com.example.bikestationapp_ca3.BuildConfig.BIKES_API_KEY;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.bikestationapp_ca3.R;
import com.example.bikestationapp_ca3.classes.Station;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment {

    List<Station> stations = new ArrayList<>();

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
        public void onMapReady(GoogleMap googleMap) {
            googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            LatLng dublin = new LatLng(53.34550694182451, -6.269892450557356);
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(dublin).zoom(10).build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            googleMap.setTrafficEnabled(true);
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            googleMap.setMyLocationEnabled(true);

            populateMapWithStations(googleMap);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        getStations();
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    public void getStations() {
        String url = "https://api.jcdecaux.com/vls/v1/stations?contract=dublin&apiKey=" + BIKES_API_KEY;

        StringRequest stringRequest = new StringRequest(StringRequest.Method.GET, url,
                response -> {
                    Log.d("JSON", response);
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        onPostExecute(jsonArray);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                },
                volleyError -> Toast.makeText(getActivity(),
                        "Failed to retrieve station locations",
                        Toast.LENGTH_SHORT)
                        .show());
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        requestQueue.add(stringRequest);
    }

    public void onPostExecute(JSONArray results) throws JSONException {
        for (int i = 0; i < results.length(); i++) {
            JSONObject station = results.getJSONObject(i);
            Station s = new Gson().fromJson(station.toString(), Station.class);
            stations.add(s);
        }
    }

    public void populateMapWithStations(GoogleMap googleMap) {
        for (Station s : stations) {
//            String snippet = "Status: " + s.getStatus() + "\n" +
//                    "Available Bike Stands: " + s.getAvailable_bike_stands() + "\n" +
//                    "Available Bikes: " + s.getAvailable_bikes();
            double lat = s.getPosition().get("lat");
            double lng = s.getPosition().get("lng");

            LatLng stationLatLng = new LatLng(lat, lng);
            MarkerOptions options = new MarkerOptions()
                    .position(stationLatLng)
                    .title(s.getAddress());
            googleMap.addMarker(options);
        }
    }
}