package com.example.weatherapp

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

class FavoritesManager {
    private val favoriteCities: SnapshotStateList<String> = mutableStateListOf()

    fun addCity(city: String) {
        if (!favoriteCities.contains(city)) {
            favoriteCities.add(city)
        }
    }

    fun removeCity(city: String) {
        favoriteCities.remove(city)
    }

    fun getFavoriteCities(): List<String> {
        return favoriteCities
    }
}