# 程式碼風格與慣例

## 命名慣例
- **類別名稱**: PascalCase (e.g., `MainActivity`, `NotificationUtils`)
- **函式名稱**: camelCase (e.g., `onCreate`, `onNotificationPosted`)
- **變數名稱**: camelCase (e.g., `isEnabled`, `notificationText`)
- **常數**: UPPER_SNAKE_CASE (e.g., `NOTIFICATION_CHANNEL_ID`)

## 檔案組織
- Kotlin 檔案使用 `.kt` 副檔名
- 每個類別通常放在獨立的檔案中
- 檔案名稱與主要類別名稱相同

## Compose UI 慣例
- Composable 函式使用 PascalCase 命名
- 使用 `@Composable` 註解
- 狀態管理使用 ViewModel 架構
- 主題使用 Material 3 設計系統

## 程式碼結構
- 使用 Kotlin 語法特性
- 支援中文註解和字串
- 使用 Coroutines 進行非同步操作
- 遵循 Android 架構指南

## 權限處理
- 遵循 Android 13+ 權限模型
- 使用現代的權限請求方式
- 適當處理權限被拒絕的情況

## 資源管理
- 字串資源放在 `strings.xml`
- 色彩資源支援深色模式
- 圖像資源使用向量圖形