package com.example.bikestationapp_ca3.viewmodels;

import android.Manifest;
import android.app.Application;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnTokenCanceledListener;

public class LocationViewModel extends AndroidViewModel {
    private final MutableLiveData<Location> locationLiveData = new MutableLiveData<>();
    private FusedLocationProviderClient mFusedLocationClient;

    public LocationViewModel(Application application) {
        super(application);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(application);
    }

    public LiveData<Location> getCurrentLocation() {
        return locationLiveData;
    }

    public void loadLocation() {
        if (ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                Toast.makeText(getApplication(),
                        "Failed to retrieve location!",
                        Toast.LENGTH_SHORT
                ).show();
            }
            else {
                locationLiveData.setValue(location);
                Log.d("GPS", String.valueOf(location.getLatitude()));
                Log.d("GPS", String.valueOf(location.getLongitude()));
            }
        });
    }
}
