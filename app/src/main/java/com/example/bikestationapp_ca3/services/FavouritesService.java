package com.example.bikestationapp_ca3.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.bikestationapp_ca3.data_classes.Station;
import com.example.bikestationapp_ca3.data_classes.User;
import com.example.bikestationapp_ca3.viewmodels.StationViewModel;

import java.util.ArrayList;
import java.util.List;

public class FavouritesService extends Service {

    List<Station> cachedStation = new ArrayList<>();
    StationViewModel stationViewModel;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    cachedStation.clear();
                    stationViewModel = new StationViewModel(getApplication());
                    User user = (User) intent.getExtras().get("user");
                    stationViewModel.loadStations();

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    cachedStation.addAll(stationViewModel.getStations().getValue());

                    for (String favouriteStation : user.getFavourites()) {
                        for (Station s : cachedStation) {
                            if (favouriteStation.equals(s.getAddress())) {
                                Log.d("Service", s.getName() + " " + s.getStatus() + " " + s.getAvailable_bikes() + " " + s.getAvailable_bike_stands());
                            }
                        }
                    }
                    try {
                        Thread.sleep(500000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }).start();
        return super.onStartCommand(intent, flags, startId);
    }
}
