# Notification_me

一個基於 Jetpack Compose 和 Material 3 設計的 Android 通知監控應用程式。

## 功能特色

- 🔔 **通知攔截**: 監控和攔截其他應用程式的通知
- 🎨 **Material 3 設計**: 採用最新的 Material 3 設計系統
- 🌙 **深色模式**: 支援系統深色模式和動態色彩
- ⚙️ **條件篩選**: 可配置的通知篩選條件
- 📱 **應用程式選擇**: 選擇要監控的特定應用程式
- 🔊 **音效控制**: 自訂通知音效設定
- 🎵 **音效管理**: 支援通知音量管理

## 技術架構

### UI 框架
- **Jetpack Compose**: 現代化的 UI 工具包
- **Material 3**: 最新的 Material Design 設計系統
- **ComponentActivity**: 支援 Compose 的 Activity 基類
- **ViewModel**: 狀態管理和資料處理

### 核心依賴
- Compose BOM: 2024.02.00
- Material 3 庫
- Navigation Compose
- ViewModel Compose
- Kotlin Coroutines

### 開發環境
- Android Gradle Plugin: 8.2.0
- Kotlin: 1.9.22
- Target SDK: 34 (Android 14)
- Min SDK: 24 (Android 7.0)

## 安裝需求

### 權限要求
- `BIND_NOTIFICATION_LISTENER_SERVICE`: 通知監聽服務
- `POST_NOTIFICATIONS`: Android 13+ 通知發布
- `ACCESS_NETWORK_STATE`: 網路狀態監控
- `FOREGROUND_SERVICE`: 前台服務運行
- `FOREGROUND_SERVICE_REMOTE_MESSAGING`: 遠程訊息服務
- `FOREGROUND_SERVICE_SPECIAL_USE`: 特殊用途服務

### 系統需求
- Android 7.0 (API 24) 或更高版本
- 支援動態色彩的 Android 12+ 裝置可獲得最佳體驗

## 快速開始

### 構建項目
```bash
# 構建調試版本
./gradlew assembleDebug

# 構建發布版本
./gradlew assembleRelease

# 生成 Android App Bundle
./gradlew bundleRelease
```

### 運行測試
```bash
# 執行單元測試
./gradlew test

# 執行儀器測試
./gradlew connectedAndroidTest
```

### 代碼品質
```bash
# 執行 lint 檢查
./gradlew lint

# 清理建置檔案
./gradlew clean
```

## 項目結構

### 核心組件
- `MainActivity`: 主要介面和設定管理
- `NotificationMonitorService`: 通知監聽服務
- `NotificationUtils`: 通知處理工具
- `NotificationSoundActivity`: 音效設定介面
- `AppListActivity`: 應用程式選擇介面
- `NotificationVolumeActivity`: 音量控制介面

### 主題系統
- `ui/theme/Color.kt`: Material 3 色彩定義
- `ui/theme/Theme.kt`: 主題配置
- `ui/theme/Type.kt`: 字型定義

### 狀態管理
- `viewmodel/MainViewModel`: 主介面狀態管理
- `SP.kt` 和 `SpUtil.kt`: 持久化儲存

## 使用說明

1. **授予權限**: 首次啟動時需要授予通知存取權限
2. **設定條件**: 在主介面中新增和編輯通知篩選條件
3. **選擇應用**: 選擇要監控的應用程式
4. **設定音效**: 自訂通知音效和音量
5. **開始監控**: 啟動通知監控服務

## 貢獻

歡迎提交 Pull Request 和 Issues 來改進這個項目。

## 授權

此項目採用 MIT 授權條款。
