package com.karoo.locationtagger.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pois")

class PoiRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    private companion object {
        val POIS_KEY = stringPreferencesKey("stored_pois")
        val NEXT_ID_KEY = longPreferencesKey("next_poi_id")
    }

    val pois: Flow<List<Poi>> = context.dataStore.data.map { prefs ->
        val encoded = prefs[POIS_KEY] ?: "[]"
        json.decodeFromString<List<Poi>>(encoded)
    }

    private suspend fun readPois(): List<Poi> {
        val prefs = context.dataStore.data.first()
        val encoded = prefs[POIS_KEY] ?: "[]"
        return json.decodeFromString<List<Poi>>(encoded)
    }

    private suspend fun writePois(pois: List<Poi>) {
        context.dataStore.edit { prefs ->
            prefs[POIS_KEY] = json.encodeToString(pois)
        }
    }

    suspend fun addPoi(poi: Poi) {
        val current = readPois()
        writePois(current + poi)
    }

    suspend fun removePoi(id: Long) {
        val current = readPois()
        writePois(current.filter { it.id != id })
    }

    suspend fun nextId(): Long {
        val prefs = context.dataStore.data.first()
        val id = prefs[NEXT_ID_KEY] ?: 1L
        context.dataStore.edit { it[NEXT_ID_KEY] = id + 1 }
        return id
    }
}