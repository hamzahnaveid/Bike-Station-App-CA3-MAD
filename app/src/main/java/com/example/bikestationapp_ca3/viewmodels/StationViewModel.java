package com.example.bikestationapp_ca3.viewmodels;

import static com.example.bikestationapp_ca3.BuildConfig.BIKES_API_KEY;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.bikestationapp_ca3.classes.Station;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class StationViewModel extends AndroidViewModel {

    private final MutableLiveData<List<Station>> stationsLiveData = new MutableLiveData<>();
    private final RequestQueue requestQueue;

    public StationViewModel(Application application) {
        super(application);
        requestQueue = Volley.newRequestQueue(application);
    }

    public LiveData<List<Station>> getStations() {
        return stationsLiveData;
    }

    public void loadStations() {
        String url = "https://api.jcdecaux.com/vls/v1/stations?contract=dublin&apiKey=" + BIKES_API_KEY;

        StringRequest stringRequest = new StringRequest(StringRequest.Method.GET, url,
                response -> {
                    Log.d("JSON", response);
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        List<Station> stations = new ArrayList<>();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject station = jsonArray.getJSONObject(i);
                            Station s = new Gson().fromJson(station.toString(), Station.class);
                            stations.add(s);
                        }

                        stationsLiveData.setValue(stations);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                },
                volleyError -> Log.e("VolleyError", "Failed to retrieve locations"));
        requestQueue.add(stringRequest);
        Log.d("RequestQueue", "Request added to queue");
    }
}
