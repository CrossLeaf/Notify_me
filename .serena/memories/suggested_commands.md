# 建議命令

## 構建命令
```bash
# 構建專案
./gradlew build

# 構建調試版本 APK
./gradlew assembleDebug

# 構建發布版本 APK
./gradlew assembleRelease

# 構建 Android App Bundle (AAB)
./gradlew bundleRelease
```

## 測試命令
```bash
# 執行單元測試
./gradlew test

# 執行儀器測試 (需要連接裝置或模擬器)
./gradlew connectedAndroidTest

# 執行特定測試類別
./gradlew testDebugUnitTest --tests="com.eton.notification_me.ExampleUnitTest"
```

## 程式碼品質
```bash
# 執行 lint 檢查
./gradlew lint

# 生成 lint 報告
./gradlew lintDebug

# 在修復 lint 問題時忽略 lint 檢查的構建
./gradlew assembleDebug -x lintDebug
```

## 清理和依賴
```bash
# 清理建置檔案
./gradlew clean

# 檢查依賴更新
./gradlew dependencyUpdates
```

## 系統工具 (macOS)
```bash
# 檔案列表
ls -la

# 目錄切換
cd

# 檔案搜尋
find . -name "*.kt"

# 文字搜尋
grep -r "text" .

# Git 操作
git status
git add .
git commit -m "message"
git push
```