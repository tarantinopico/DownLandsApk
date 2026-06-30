# DownLands

DownLands is a beautiful, modern Android app that acts as a smart dashboard for the device's Downloads folder (and any other folder the user grants access to). The app allows you to browse, search, categorize, and open downloaded files.

## Features

*   **Smart Dashboard:** Single clean interface to manage your downloads.
*   **Categories:** Create custom categories with smart rules (e.g., name contains "apk", extension is "pdf") to automatically group files.
*   **Sorting & Filtering:** Sort by newest, oldest, name, size, or type. Filter by file types and date ranges.
*   **Search:** Real-time search by file name or extension.
*   **View Modes:** Switch between List, Grid, and Gallery view modes.
*   **Group By Category:** Easily group files by their matched categories.
*   **Customization:** Full dark/light mode support, dynamic colors (Android 12+), and a sleek Material 3 design.
*   **File Actions:** Open files using their native applications or share multiple files simultaneously.
*   **Backup & Restore:** Export your custom categories and settings to a JSON file and restore them anytime.

## Tech Stack

*   **Language:** Kotlin
*   **UI Toolkit:** Jetpack Compose (Material 3)
*   **Architecture:** MVVM (Model-View-ViewModel) + Clean Architecture principles
*   **Persistence:** Room (Local DB for categories & rules) & Jetpack DataStore (Preferences)
*   **File Access:** Storage Access Framework (SAF) & `DocumentFile`
*   **Dependency Injection:** Manual/AppContainer based
*   **Image Loading:** Coil

## Setup & Running

This project uses Gradle with the Kotlin DSL. You can open the project in Android Studio (Ladybug or newer recommended) and run it directly on an emulator or physical device.

To build the APK from the command line:

```bash
gradle assembleDebug
```

The APK will be generated in `app/build/outputs/apk/debug/`.
