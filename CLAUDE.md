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
```

## Architecture and Structure

### Core Components

1. **MainActivity** (`app/src/main/java/com/eton/notification_me/MainActivity.kt`)
   - Main entry point and configuration UI
   - Handles notification permission requests (Android 13+)
   - Manages condition list for notification filtering
   - Uses RecyclerView with custom adapter for dynamic condition management

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
- **Min SDK**: 23 (Android 6.0)
- **Language**: Kotlin with Java 8 compatibility
- **Build Tools**: Android Gradle Plugin 7.1.2, Kotlin 1.6.21
- **UI**: Material Design components with RecyclerView patterns

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

## Important Implementation Details

- The app uses traditional View system (not Compose)
- Notification listener service auto-starts when permission is granted
- Chinese language support in UI strings and comments
- Custom notification icons stored in drawable resources
- RecyclerView with dynamic item management for condition editing