package com.karoo.locationtagger.network

import android.util.Log
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object MapUploader {

    private const val TAG = "MapUploader"
    private const val SPATIX_API_URL = "https://api.spatix.io/api/map"

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun uploadMap(title: String, geoJson: String): Result<String> {
        return try {
            val geoJsonElement = json.parseToJsonElement(geoJson)
            val requestBody = JsonObject(
                mapOf(
                    "title" to JsonPrimitive(title),
                    "data" to geoJsonElement
                )
            )
            val requestBodyString = json.encodeToString(JsonObject.serializer(), requestBody)

            Log.d(TAG, "Uploading map: $requestBodyString")

            val connection = URL(SPATIX_API_URL).openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.setRequestProperty("Accept", "application/json")
            connection.doOutput = true
            connection.connectTimeout = 15_000
            connection.readTimeout = 15_000

            OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                writer.write(requestBodyString)
                writer.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_CREATED) {
                val errorStream = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "Unknown error"
                Log.e(TAG, "Upload failed: HTTP $responseCode — $errorStream")
                return Result.failure(Exception("Upload failed: HTTP $responseCode"))
            }

            val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
            val jsonResponse = json.parseToJsonElement(responseBody).jsonObject

            val mapUrl = jsonResponse["url"]?.jsonPrimitive?.content
                ?: return Result.failure(Exception("No URL in response"))

            Log.d(TAG, "Map uploaded successfully: $mapUrl")
            Result.success(mapUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Upload failed", e)
            Result.failure(e)
        }
    }
}