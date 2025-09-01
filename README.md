# TripTracker

Lightweight Android app for tracking trips using a foreground service and Google’s Fused Location
Provider. Trips are stored locally with Room and can be viewed/exported from a history screen. UI
built with Jetpack Compose.

## Features

- Start / Pause / Stop background GPS tracking (foreground service)
- Live stats: distance, speed, elapsed time
- Map preview (Google Maps Compose)
- Trip history + CSV export
- Settings stored in DataStore
- Dark mode

## Tech

Kotlin · Jetpack Compose · Hilt · Room · DataStore · Google Play Services (Fused Location, Maps)

## Setup

1. Add Google Maps API key in strings.xml.

2. Grant runtime permissions:
    - `ACCESS_FINE_LOCATION`
    - `ACCESS_BACKGROUND_LOCATION`
    - `FOREGROUND_SERVICE`
    - `POST_NOTIFICATIONS`
3. Build & run on a device or emulator with Google Play services.

## Testing

- Use Android Studio's Location controls to simulate movement.

## Notes

- All data is stored locally (Room, DataStore).
- I spent around 25 hours completing this task.

---