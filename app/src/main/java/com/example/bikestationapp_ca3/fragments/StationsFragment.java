package com.example.bikestationapp_ca3.fragments;

import static com.example.bikestationapp_ca3.BuildConfig.MAPS_API_KEY;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bikestationapp_ca3.R;
import com.example.bikestationapp_ca3.adapters.StationsRecyclerViewAdapter;
import com.example.bikestationapp_ca3.data_classes.Station;
import com.example.bikestationapp_ca3.data_classes.User;
import com.example.bikestationapp_ca3.viewmodels.LocationViewModel;
import com.example.bikestationapp_ca3.viewmodels.StationViewModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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

    ValueEventListener listener;
    String uid;
    User user;
    DatabaseReference ref;
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

        Toast.makeText(getActivity(), "Loading stations and calculating distances...", Toast.LENGTH_LONG).show();

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }

        SharedPreferences sp = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        uid = sp.getString("USER", "");

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        ref = db.getReference("users").child(uid);

        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        ref.addValueEventListener(listener);
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
            recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));

            ItemTouchHelper favouriteItemTouchHelper = new ItemTouchHelper(favouriteSimpleCallback);
            favouriteItemTouchHelper.attachToRecyclerView(recyclerView);
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ref.removeEventListener(listener);
    }

    ItemTouchHelper.SimpleCallback favouriteSimpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            Station station = cachedStation.get(viewHolder.getBindingAdapterPosition());
            List<String> favouriteStationNames = user.getFavourites();
            boolean isFavourite = false;

            for (String s : favouriteStationNames) {
                if (s.equals(station.getAddress())) {
                    isFavourite = true;
                    break;
                }
            }
            showFavouriteDialog(station, isFavourite);
            adapter.notifyItemChanged(viewHolder.getBindingAdapterPosition());
        }
    };

    public void observeStations() {
        stationViewModel.getStations().observe(getViewLifecycleOwner(), stations ->
        {
            cachedStation.clear();
            pendingDistances = stations.size();

            //Uncomment these two lines for replacing calculateDistance
            cachedStation = stations;
            adapter.updateStations(cachedStation);

//            for (Station s : stations) {
//                calculateDistance(s);
//            }
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

    public void showFavouriteDialog(Station station, boolean isFavourite) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if (!isFavourite) {
            builder.setMessage("Add " + station.getAddress() + " Bike Station to Favourites?")
                    .setCancelable(true)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            addStationToFavourites(station, ref);
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
        else {
            builder.setMessage("Remove " + station.getAddress() + " Bike Station from Favourites?")
                    .setCancelable(true)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            removeStationFromFavourites(station, ref);
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

    }

    public void addStationToFavourites(Station station, DatabaseReference ref) {
        user.addToFavourites(station.getAddress());

        ref.setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(
                        getActivity(),
                        station.getAddress() + " added to Favourites",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    public void removeStationFromFavourites(Station station, DatabaseReference ref) {
        user.removeFromFavourites(station.getAddress());

        ref.setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(
                        getActivity(),
                        station.getAddress() + " removed from Favourites",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }
}