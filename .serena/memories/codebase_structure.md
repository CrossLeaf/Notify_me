# 程式碼結構

## 目錄架構
```
app/src/main/java/com/eton/notification_me/
├── viewmodel/           # ViewModel 類別
│   ├── MainViewModel.kt
│   └── SoundViewModel.kt
├── ui/theme/           # UI 主題系統
│   ├── Color.kt        # 色彩定義
│   ├── Theme.kt        # 主題配置
│   └── Type.kt         # 字型定義
├── util/               # 工具類別
│   └── LogManager.kt
├── MainActivity.kt     # 主要活動
├── NotificationMonitorService.kt  # 通知監控服務
├── NotificationUtils.kt           # 通知工具
├── NotificationSoundActivity.kt   # 音效設定
├── NotificationVolumeActivity.kt  # 音量控制
├── AppListActivity.kt            # 應用程式選擇
├── LogActivity.kt               # 日誌顯示
├── WifiVolumeService.kt         # WiFi 音量服務
└── SP.kt                        # SharedPreferences 管理
```

## 核心組件
1. **MainActivity**: 主要介面和設定管理 (Compose UI)
2. **NotificationMonitorService**: 通知攔截服務
3. **NotificationUtils**: 通知處理工具
4. **NotificationSoundActivity**: 音效設定介面 (Compose UI)
5. **AppListActivity**: 應用程式選擇介面
6. **LogActivity**: 日誌顯示介面 (Compose UI)
7. **SpUtil**: SharedPreferences 封裝

## 資源目錄
- `res/layout/`: XML 佈局檔案
- `res/values/`: 字串、色彩、主題資源
- `res/drawable/`: 圖像資源
- `res/raw/`: 音效檔案
- `res/menu/`: 選單資源