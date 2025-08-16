# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an Android notification monitoring application ("Notification_me") that intercepts and processes notifications from other apps. The app requires special permissions to access the Android notification system and can trigger custom notifications based on configurable conditions.

## Build and Development Commands

### Building the Project
```bash
# Build the project
./gradlew build

# Build release APK
./gradlew assembleRelease

# Build debug APK
./gradlew assembleDebug

# Build Android App Bundle (AAB)
./gradlew bundleRelease
```

### Testing
```bash
# Run unit tests
./gradlew test

# Run instrumented tests on connected device/emulator
./gradlew connectedAndroidTest

# Run specific test class
./gradlew testDebugUnitTest --tests="com.eton.notification_me.ExampleUnitTest"
```

### Cleaning and Dependencies
```bash
# Clean build artifacts
./gradlew clean

# Check for dependency updates
./gradlew dependencyUpdates
```

### Linting and Code Quality
```bash
# Run lint checks
./gradlew lint

# Generate lint report
./gradlew lintDebug

# Build without lint (when fixing lint issues)
./gradlew assembleDebug -x lintDebug
```

## Architecture and Structure

### Core Components

1. **MainActivity** (`app/src/main/java/com/eton/notification_me/MainActivity.kt`)
   - Main entry point and configuration UI built with Jetpack Compose
   - Handles notification permission requests (Android 13+)
   - Manages condition list for notification filtering
   - Uses Material 3 design system with LazyColumn for dynamic condition management
   - Implemented as ComponentActivity with MainViewModel for state management

2. **NotificationMonitorService** (`app/src/main/java/com/eton/notification_me/NotificationMonitorService.kt`)
   - Extends `NotificationListenerService`
   - Intercepts system notifications from other apps
   - Extracts notification data (title, text, icons, package name)
   - Processes notifications through `NotificationUtils`

3. **NotificationUtils** (`app/src/main/java/com/eton/notification_me/NotificationUtils.kt`)
   - Handles notification channel creation and management
   - Sends custom notifications based on intercepted data

4. **WifiVolumeService** (`app/src/main/java/com/eton/notification_me/WifiVolumeService.kt`)
   - Background service for volume-related functionality
   - Configured as `specialUse` foreground service type

5. **SharedPreferences Management** (`app/src/main/java/com/eton/notification_me/SP.kt`)
   - Handles persistent storage of app conditions and settings
   - Wrapper class `SpUtil` for cleaner SP operations

### Key Features

- **Notification Interception**: Requires `BIND_NOTIFICATION_LISTENER_SERVICE` permission
- **Android 13+ Compatibility**: Handles `POST_NOTIFICATIONS` permission
- **Condition-based Filtering**: Users can configure text-based conditions for notification processing
- **App Selection**: `AppListActivity` allows users to select specific apps to monitor
- **Volume Control**: `NotificationVolumeActivity` provides notification volume management

### Permissions and Manifest

The app requires several critical permissions:
- `ACCESS_NETWORK_STATE`: Network state monitoring
- `FOREGROUND_SERVICE`: Background service operation
- `FOREGROUND_SERVICE_REMOTE_MESSAGING`: For notification service
- `FOREGROUND_SERVICE_SPECIAL_USE`: For volume service
- `POST_NOTIFICATIONS`: Android 13+ notification posting

### Development Notes

- **Target SDK**: 34 (Android 14)
- **Min SDK**: 24 (Android 7.0)
- **Language**: Kotlin with Java 8 compatibility
- **Build Tools**: Android Gradle Plugin 8.2.0, Kotlin 1.9.22
- **UI**: Jetpack Compose with Material 3 design system
- **Architecture**: ComponentActivity with Compose UI, ViewModel for state management

### Data Flow

1. User configures notification conditions in MainActivity
2. NotificationMonitorService intercepts system notifications
3. Service extracts relevant data and checks against user conditions
4. NotificationUtils processes and sends custom notifications
5. Settings persist via SharedPreferences through SpUtil

### Testing Strategy

- Unit tests for utility classes and data processing logic
- Instrumented tests for UI interactions and permission handling
- Focus on notification interception and condition matching logic

## UI Framework and Theming

The app has been migrated to use **Jetpack Compose** with **Material 3** design system:

### Compose Structure
- **Theme System**: `app/src/main/java/com/eton/notification_me/ui/theme/`
  - `Color.kt`: Material 3 color scheme definitions
  - `Theme.kt`: Main theme composable with light/dark mode support
  - `Type.kt`: Typography definitions
- **MainActivity**: Converted to ComponentActivity with Compose UI
- **NotificationSoundActivity**: Converted to ComponentActivity with Compose UI

### Material 3 Features
- Dynamic color support (Android 12+)
- Light and dark theme support
- Adaptive UI components
- Material 3 color tokens and typography scale

### Build Configuration
- Compose BOM: 2024.02.00
- Compose Compiler: 1.5.8
- Material 3 library integration
- Navigation Compose support

## Important Implementation Details

- **UI Framework**: Jetpack Compose with Material 3 design system
- **State Management**: ViewModel architecture with Compose state
- **Navigation**: Navigation Compose for screen transitions
- **Theming**: Material 3 with dynamic color and day/night support
- Notification listener service auto-starts when permission is granted
- Chinese language support in UI strings and comments
- Custom notification icons stored in drawable resources