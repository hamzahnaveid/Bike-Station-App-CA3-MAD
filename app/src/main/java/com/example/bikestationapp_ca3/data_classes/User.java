package com.example.bikestationapp_ca3.data_classes;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String email;
    private String password;
    private String name;
    private List<String> favourites;

    public User() {

    }

    public User(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.favourites = new ArrayList<>();
        this.favourites.add("");
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getFavourites() {
        return favourites;
    }

    public void setFavourites(List<String> favourites) {
        this.favourites = favourites;
    }

    public void addToFavourites(String stationName) {
        favourites.add(stationName);
    }

    public void removeFromFavourites(String stationName) {
        favourites.remove(stationName);
    }
}
