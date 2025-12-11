package com.example.bikestationapp_ca3;

import android.content.Context;
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

import com.example.bikestationapp_ca3.fragments.ItemFragment;
import com.example.bikestationapp_ca3.fragments.MapFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.Map;

public class MapActivity extends AppCompatActivity {
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_map);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        getSupportFragmentManager().beginTransaction().replace(
                R.id.main, new MapFragment()
        ).commit();

        sp = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        Log.d("UserID", "User ID:" + sp.getString("USER", "null"));

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment selectedFragment = null;
                if (menuItem.getItemId() == R.id.nav_map) {
                    selectedFragment = new MapFragment();
                }
                else if (menuItem.getItemId() == R.id.nav_favourites) {
                    selectedFragment = new ItemFragment();
                }

                getSupportFragmentManager().beginTransaction().replace(
                        R.id.fragment_container, selectedFragment
                ).commit();

                return true;
            }
        });
    }
}