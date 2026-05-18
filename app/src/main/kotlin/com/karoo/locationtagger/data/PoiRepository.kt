package com.karoo.locationtagger.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pois")

class PoiRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    private companion object {
        val TAG = "PoiRepository"
        val POIS_KEY = stringPreferencesKey("stored_pois")
        val NEXT_ID_KEY = longPreferencesKey("next_poi_id")
    }

    /** In-memory cache — avoids re-reading and re-deserializing from DataStore on every mutation. */
    private val cachedPois = MutableStateFlow(emptyList<Poi>())

    /** Exposed as a Flow; reflects the in-memory cache which is kept in sync with DataStore. */
    val pois: Flow<List<Poi>> = cachedPois

    /**
     * Must be called once (e.g. in a ViewModel init block) to seed the cache from persistent storage.
     * Subsequent mutations go through [addPoi]/[removePoi] which update both cache and DataStore.
     */
    suspend fun initialize() {
        try {
            val encoded = context.dataStore.data.first()[POIS_KEY] ?: "[]"
            cachedPois.value = json.decodeFromString<List<Poi>>(encoded)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load POIs, starting with empty list", e)
            cachedPois.value = emptyList()
        }
    }

    private suspend fun writePois(pois: List<Poi>) {
        try {
            context.dataStore.edit { prefs ->
                prefs[POIS_KEY] = json.encodeToString(pois)
            }
            cachedPois.value = pois
        } catch (e: Exception) {
            Log.e(TAG, "Failed to persist POIs", e)
        }
    }

    suspend fun addPoi(poi: Poi) {
        val updated = cachedPois.value + poi
        writePois(updated)
    }

    suspend fun removePoi(id: Long) {
        val updated = cachedPois.value.filter { it.id != id }
        writePois(updated)
    }

    suspend fun nextId(): Long {
        var id = 1L
        try {
            context.dataStore.edit { prefs ->
                id = prefs[NEXT_ID_KEY] ?: 1L
                prefs[NEXT_ID_KEY] = id + 1
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate next ID, falling back to cache", e)
            id = (cachedPois.value.maxOfOrNull { it.id } ?: 0L) + 1
        }
        return id
    }
}