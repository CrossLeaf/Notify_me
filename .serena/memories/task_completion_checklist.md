# 任務完成檢查清單

## 程式碼品質檢查
```bash
# 執行 lint 檢查
./gradlew lint

# 確認沒有 lint 錯誤或警告
./gradlew lintDebug
```

## 測試執行
```bash
# 執行單元測試
./gradlew test

# 執行儀器測試 (如果可用)
./gradlew connectedAndroidTest
```

## 構建驗證
```bash
# 確保專案可以成功構建
./gradlew build

# 構建 debug APK 進行測試
./gradlew assembleDebug
```

## 功能驗證
1. **權限檢查**: 確認通知存取權限正常運作
2. **UI 測試**: 確認 Compose UI 正常顯示
3. **通知功能**: 測試通知攔截和處理
4. **音效功能**: 確認音效選擇和播放正常
5. **條件篩選**: 測試條件設定和篩選功能

## 程式碼審查
- 確認遵循專案的程式碼風格
- 檢查是否有硬編碼的字串 (應使用 strings.xml)
- 確認適當的錯誤處理
- 檢查記憶體洩漏和資源管理

## 提交前檢查
- 確認所有修改都已儲存
- 執行完整的構建測試
- 確認沒有未完成的 TODO 或 FIXME 註解
- 檢查 Git 狀態和變更