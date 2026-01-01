package com.example.bikestationapp_ca3.services;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.bikestationapp_ca3.R;
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
                    int id = 0;
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
                            int drawable;
                            String contentText;
                            if (favouriteStation.equals(s.getAddress())) {
                                if (s.getStatus().equals("CLOSED")) {
                                    drawable = R.drawable.closed_station_icon;
                                    contentText = "CLOSED - no bikes or parking available";
                                }
                                else if (s.getAvailable_bikes() == 0 && s.getAvailable_bike_stands() > 0) {
                                    drawable = R.drawable.no_bikes_station_icon;
                                    contentText = "OPEN - no bikes available";
                                }
                                else if (s.getAvailable_bike_stands() == 0 && s.getAvailable_bikes() > 0) {
                                    drawable = R.drawable.no_stands_station_icon;
                                    contentText = "OPEN - no parking spaces available";
                                }
                                else {
                                    drawable = R.drawable.station_icon;
                                    contentText = "OPEN - bikes and parking spaces available";
                                }

                                NotificationChannel channel = new NotificationChannel("LiveBike",
                                        "LiveBike",
                                        NotificationManager.IMPORTANCE_DEFAULT);
                                NotificationManager manager = getSystemService(NotificationManager.class);
                                manager.createNotificationChannel(channel);


                                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplication(), "LiveBike")
                                        .setSmallIcon(drawable)
                                        .setBadgeIconType(drawable)
                                        .setContentTitle(s.getAddress())
                                        .setContentText(contentText)
                                        .setStyle(new NotificationCompat.BigTextStyle()
                                                .bigText("Available Bikes: " + s.getAvailable_bikes() + "\n" +
                                                        "Available Stands: " + s.getAvailable_bike_stands()))
                                        .setAutoCancel(true)
                                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                                if (ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                    return;
                                }
                                NotificationManagerCompat.from(getApplication()).notify(++id, builder.build());
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
