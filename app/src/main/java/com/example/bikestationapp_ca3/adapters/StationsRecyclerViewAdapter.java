package com.example.bikestationapp_ca3.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bikestationapp_ca3.R;
import com.example.bikestationapp_ca3.data_classes.Station;

import java.util.List;

public class StationsRecyclerViewAdapter extends RecyclerView.Adapter<StationsRecyclerViewAdapter.ViewHolder> {

    private final List<Station> mValues;

    public StationsRecyclerViewAdapter(List<Station> items) {
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.fragment_stations, parent, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Station s = mValues.get(position);

        if (s.getStatus().equals("CLOSED")) {
            holder.imgStationIcon.setImageResource(R.drawable.closed_station_icon);
        }
        else if (s.getAvailable_bikes() == 0 && s.getAvailable_bike_stands() > 0) {
            holder.imgStationIcon.setImageResource(R.drawable.no_bikes_station_icon);
        }
        else if (s.getAvailable_bike_stands() == 0 && s.getAvailable_bikes() > 0) {
            holder.imgStationIcon.setImageResource(R.drawable.no_stands_station_icon);
        }
        else {
            holder.imgStationIcon.setImageResource(R.drawable.station_icon);
        }

        holder.tvAddress.setText(s.getAddress());
        holder.tvAvailableBikes.setText("Available Bikes: " + String.valueOf(s.getAvailable_bikes()));
        holder.tvStands.setText("Available Stands: " + String.valueOf(s.getAvailable_bike_stands()));

        if (s.getDistance() == 0.0) {
            holder.tvDistance.setText("");
        }
        else {
            holder.tvDistance.setText("Distance: " + s.getDistance() + "km");
        }
        holder.tvStatus.setText(s.getStatus());

        if (holder.tvStatus.getText().toString().equals("OPEN")) {
            holder.tvStatus.setTextColor(Color.GREEN);
        }
        else {
            holder.tvStatus.setTextColor(Color.RED);
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public void updateStations(List<Station> stations) {
        mValues.clear();
        mValues.addAll(stations);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout constraintLayout;
        ImageView imgStationIcon;
        TextView tvAddress, tvAvailableBikes, tvStands, tvDistance, tvStatus;
        LinearLayout mainLayout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            constraintLayout = itemView.findViewById(R.id.rv_constraint_layout);
            imgStationIcon = itemView.findViewById(R.id.rv_station_icon);
            tvAddress = itemView.findViewById(R.id.rv_address);
            tvAvailableBikes = itemView.findViewById(R.id.rv_available_bikes);
            tvStands = itemView.findViewById(R.id.rv_available_stands);
            tvDistance = itemView.findViewById(R.id.rv_distance);
            tvStatus = itemView.findViewById(R.id.rv_status);
            mainLayout = itemView.findViewById(R.id.mainLayout);
            }
    }
}