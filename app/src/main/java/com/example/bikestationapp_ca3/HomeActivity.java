package com.example.bikestationapp_ca3;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.bikestationapp_ca3.data_classes.User;
import com.example.bikestationapp_ca3.fragments.FavouritesFragment;
import com.example.bikestationapp_ca3.fragments.MapFragment;
import com.example.bikestationapp_ca3.fragments.ProfileFragment;
import com.example.bikestationapp_ca3.fragments.StationsFragment;
import com.example.bikestationapp_ca3.services.FavouritesService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeActivity extends AppCompatActivity {

    ValueEventListener listener;
    SharedPreferences sp;
    User user;
    DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });
        sp = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        Log.d("UserID", "User ID:" + sp.getString("USER", "null"));
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        String uid = sp.getString("USER", "null");
        ref = db.getReference("users").child(uid);
        Log.d("HomeActivity", "Activity created");

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

        getSupportFragmentManager().beginTransaction().replace(
                R.id.fragment_container, new MapFragment()
        ).commit();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(menuItem -> {
            getSelectedFragmentAndReplace(menuItem);
            return true;
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        Intent intent = new Intent(getApplication(), FavouritesService.class);
        intent.putExtra("user", user);
        startService(intent);
        Log.d("HomeActivity", "Activity stopped");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        stopService(new Intent(getApplication(), FavouritesService.class));
        Log.d("HomeActivity", "Activity resumed");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(getApplication(), FavouritesService.class));
        Log.d("HomeActivity", "Activity destroyed");
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
        else if (menuItem.getItemId() == R.id.nav_user) {
            selectedFragment = new ProfileFragment();
        }

        getSupportFragmentManager().beginTransaction().replace(
                R.id.fragment_container, selectedFragment
        ).commit();
    }
}