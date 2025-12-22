package com.example.bikestationapp_ca3.fragments;

import static com.example.bikestationapp_ca3.BuildConfig.MAPS_API_KEY;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.example.bikestationapp_ca3.viewmodels.StationViewModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.GeoApiContext;

import java.util.ArrayList;
import java.util.List;

public class FavouritesFragment extends Fragment {

    ValueEventListener listener;
    String uid;
    User user;
    DatabaseReference ref;
    List<Station> cachedStation = new ArrayList<>();
    StationsRecyclerViewAdapter adapter;
    StationViewModel stationViewModel;
    GeoApiContext geoApiContext;

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;

    public FavouritesFragment() {
    }

    @SuppressWarnings("unused")
    public static FavouritesFragment newInstance(int columnCount) {
        FavouritesFragment fragment = new FavouritesFragment();
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

        SharedPreferences sp = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        uid = sp.getString("USER", "");

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        ref = db.getReference("users").child(uid);

        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);

                stationViewModel = new ViewModelProvider(requireActivity()).get(StationViewModel.class);
                stationViewModel.loadStations();

                observeStations();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        ref.addValueEventListener(listener);

        adapter = new StationsRecyclerViewAdapter(new ArrayList<>());

        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));

            ItemTouchHelper favouriteItemTouchHelper = new ItemTouchHelper(deleteSimpleCallback);
            favouriteItemTouchHelper.attachToRecyclerView(recyclerView);
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ref.removeEventListener(listener);
    }

    ItemTouchHelper.SimpleCallback deleteSimpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            Station station = cachedStation.get(viewHolder.getBindingAdapterPosition());
            showRemoveFavouriteDialog(station);
            adapter.notifyItemChanged(viewHolder.getBindingAdapterPosition());
        }
    };

    public void observeStations() {
        stationViewModel.getStations().observe(getViewLifecycleOwner(), stations -> {
            cachedStation.clear();
            for (String favouriteStation : user.getFavourites()) {
                for (Station s : stations) {
                    if (favouriteStation.equals(s.getAddress())) {
                        cachedStation.add(s);
                    }
                }
            }
            adapter.updateStations(cachedStation);
         });
    }

    public void showRemoveFavouriteDialog(Station station) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

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

    public void removeStationFromFavourites(Station station, DatabaseReference ref) {
        user.removeFromFavourites(station.getAddress());
        cachedStation.remove(station);

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
        adapter.updateStations(cachedStation);
    }
}