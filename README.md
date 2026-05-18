# Karoo Location Tagger

A Hammerhead Karoo 3 bicycle computer extension for tagging and navigating to points of interest (POIs) during rides. Fully offline — no network required.

## Features

- **One-tap POI creation** — Select type (MTB/Road) and potential (1–3), then save your current GPS position
- **Saved POIs list** — View all tagged locations sorted by distance, with direction arrows
- **Navigate to POI** — Drops a pin on the Karoo map and starts turn-by-turn navigation
- **Offline-first** — All data stored locally using DataStore, no internet needed
- **WCAG-accessible** — 48dp touch targets, icon+text indicators (never color alone), contrast-compliant palette

## Installation

### Prerequisites

- Android SDK with platform 34 and build-tools 34.0.0
- Java 17+
- GitHub Packages credentials for karoo-ext dependency (or use local maven repo)

### Build

```bash
# Set SDK path
echo "sdk.dir=/path/to/android-sdk" > local.properties

# Build debug APK
./gradlew assembleDebug
```

The APK is output to `app/build/outputs/apk/debug/app-debug.apk`.

### Install on Karoo

1. Build the debug APK
2. Host the APK on a URL (e.g., GitHub Releases)
3. On your phone, long-press the download URL → Share → Hammerhead Companion App
4. The install prompt appears on the Karoo

## Setup on Karoo

1. After installing, go to **Ride Profile → Edit Pages**
2. Select a page and add a field
3. Find **"Location Tagger"** in the extension list
4. Add the **"Tag Location"** action button to your ride page
5. During a ride, tap the button to open the POI tagging app

## Architecture

### Project Structure

```
app/src/main/
├── AndroidManifest.xml
├── kotlin/com/karoo/locationtagger/
│   ├── MainActivity.kt              # Entry point, dispatches pin drops
│   ├── data/
│   │   ├── Poi.kt                    # Data model (id, lat, lng, type, potential)
│   │   ├── PoiRepository.kt          # DataStore persistence
│   │   └── GeoUtils.kt               # Haversine distance, bearing, direction
│   ├── extension/
│   │   ├── LocationTaggerExtension.kt # KarooExtension service, BonusAction handler
│   │   ├── OpenAppReceiver.kt        # BroadcastReceiver for launching app
│   │   ├── PoiTagDataType.kt         # DataType for ride-page field
│   │   └── KarooExtensions.kt        # consumerFlow() extension function
│   ├── theme/
│   │   └── Theme.kt                  # MaterialTheme wrapper
│   └── ui/
│       ├── MainTabLayout.kt          # Browser-style tab container
│       ├── MainViewModel.kt          # ViewModel with GPS + POI state
│       ├── NewEntryTab.kt            # Tag a new POI
│       └── SavedPoisTab.kt           # List of saved POIs with navigation
└── res/
    ├── drawable/                      # Launcher icon (green crosshair)
    ├── layout/poi_tag_field.xml       # RemoteViews for ride-page field
    ├── mipmap-*/                      # App icons
    ├── values/strings.xml
    └── xml/extension_info.xml         # DataType + BonusAction declaration
```

### Key Components

#### Extension Service (`LocationTaggerExtension`)

- Extends `KarooExtension("karoo-location-tagger", "1.0")`
- Registers `PoiTagDataType` (shows "📍 Tag" on ride page)
- Handles `onBonusAction("open-location-tagger")` by sending a broadcast
- **Broadcast pattern**: Android 10+ blocks `startActivity()` from background services, so `onBonusAction` sends a broadcast to `OpenAppReceiver` which launches `MainActivity`

#### Data Layer

- **`Poi`**: Kotlin data class with `@Serializable`, stored as JSON in DataStore
- **`PoiRepository`**: CRUD operations over DataStore Preferences with auto-incrementing IDs
- **`GeoUtils`**: Pure Kotlin Haversine formula for distance/bearing — no external map SDK needed

#### UI Layer

- **`MainTabLayout`**: Browser-style tabs ("Tag" / "Points") with GPS status indicator
- **`NewEntryTab`**: Type selector (MTB/Road), potential selector (1/2/3), save button
- **`SavedPoisTab`**: Compact list with direction arrow, distance, delete confirmation, navigate button
- **`MainViewModel`**: Combines GPS location flow with POI list to compute distance/direction

#### Pin Drop Navigation

```kotlin
// In MainActivity.dispatchPinDrop()
karooSystem.dispatch(
    LaunchPinDrop(
        Symbol.POI(id, lat, lng, type = "generic", name = displayName)
    )
)
```

This tells the Karoo to drop a pin on the map and offer turn-by-turn navigation to the target coordinates.

## Dependencies

| Dependency | Version | Purpose |
|-----------|---------|---------|
| `io.hammerhead:karoo-ext` | 1.1.8 | Karoo extension SDK (local maven repo) |
| `androidx.compose.*` | BOM 2024.x | Jetpack Compose UI |
| `androidx.datastore` | 1.1.1 | Local persistent storage |
| `kotlinx-serialization-json` | 1.7.3 | JSON serialization for POI data |
| `androidx.lifecycle` | 2.8.6 | ViewModel + lifecycle |

> **Note**: The karoo-ext dependency is served from a local maven repo (`local-maven-repo/`) because GitHub Packages requires authentication. The AAR was cached from `~/.gradle/caches/`.

## Karoo Extension API Notes

- `Symbol.POI` requires an `id` parameter (first positional argument)
- `OnLocationChanged` has fields: `lat`, `lng`, `orientation` (nullable)
- `consumerFlow<T>()` is an extension function, not part of the library — defined in `KarooExtensions.kt`
- `ViewEmitter.updateView()` is rate-limited to ~1Hz (900ms minimum between updates)
- RemoteViews click handlers (`setOnClickPendingIntent`) do not work on Karoo ride fields — the system intercepts taps as `SINGLE_TAP_DATA_FIELD`

## Build Configuration

| Property | Value |
|----------|-------|
| `applicationId` | `com.karoo.locationtagger` |
| `minSdk` | 23 |
| `targetSdk` / `compileSdk` | 34 |
| `kotlin` | 2.0.0 |
| `AGP` | 8.2.2 |

## License

Private project — all rights reserved.