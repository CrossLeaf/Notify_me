# 專案概覽

## 專案目的
這是一個 Android 通知監控應用程式 ("Notification_me")，可以攔截和處理來自其他應用程式的通知。應用程式需要特殊權限來存取 Android 通知系統，並可以根據可配置的條件觸發自定義通知。

## 主要功能
- 通知攔截與監控
- 條件篩選系統
- 應用程式選擇
- 音效控制與音量管理
- 支援中文介面

## 技術架構
- **Target SDK**: 34 (Android 14)
- **Min SDK**: 24 (Android 7.0)
- **Language**: Kotlin with Java 8 compatibility
- **UI Framework**: Jetpack Compose with Material 3 design system
- **Architecture**: ComponentActivity with Compose UI, ViewModel for state management
- **Build Tools**: Android Gradle Plugin 8.2.0, Kotlin 1.9.22

## 版本資訊
- 目前版本: 3.0.0 (versionCode 8)
- 應用程式 ID: com.eton.notification_me