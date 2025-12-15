package com.example.bikestationapp_ca3;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.bikestationapp_ca3.fragments.FavouritesFragment;
import com.example.bikestationapp_ca3.fragments.MapFragment;
import com.example.bikestationapp_ca3.fragments.StationsFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {
    SharedPreferences sp;
    FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        sp = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        Log.d("UserID", "User ID:" + sp.getString("USER", "null"));

//        getSupportFragmentManager().beginTransaction().replace(
//                R.id.main, new MapFragment()
//        ).commit();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getCurrentLocation();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(menuItem -> {
            getSelectedFragmentAndReplace(menuItem);
            return true;
        });
    }

    public void getSelectedFragmentAndReplace(MenuItem menuItem) {
        Fragment selectedFragment = null;
        if (menuItem.getItemId() == R.id.nav_map) {
            selectedFragment = new MapFragment();
        }
        else if (menuItem.getItemId() == R.id.nav_stations) {
            selectedFragment = new StationsFragment();
        }
        else if (menuItem.getItemId() == R.id.nav_favourites) {
            selectedFragment = new FavouritesFragment();
        }

        getSupportFragmentManager().beginTransaction().replace(
                R.id.fragment_container, selectedFragment
        ).commit();
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, new CancellationToken() {
            @NonNull
            @Override
            public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                return null;
            }

            @Override
            public boolean isCancellationRequested() {
                return false;
            }
        }).addOnSuccessListener(location -> {
            if (location == null) {
                Toast.makeText(HomeActivity.this,
                        "Cannot retrieve location!",
                        Toast.LENGTH_SHORT
                ).show();
            }
            else {
                Log.d("GPS", String.valueOf(location.getLatitude()));
                Log.d("GPS", String.valueOf(location.getLongitude()));
            }
        });
    }
}