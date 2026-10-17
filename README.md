# HeaderCraft - HTTP Headers, CORS & Security Configurator
## (034_http-headers)

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-Ready-brightgreen.svg)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-Proprietary-red.svg)]()

CORS設定、セキュリティヘッダー（HSTS / CSP / XFO / nosniff）、Cache-Control設定をGUIで直感構築し、Nginx / Apache / Next.js / Express などマルチサーバー向けに瞬時出力するエンジニア必携ツールです。

---

### ✨ 主な機能

- **🌐 直感 CORS ビルダー**: Allowed Origins, Methods, Headers, Allow-Credentials, Max-Age を視覚的に設定。
- **🛡️ セキュリティ脆弱性診断 (Grade A+ 判定)**: ワイルドカード Origin と Credentials の仕様違反競合や HSTS/CSP 欠落を未然検知。
- **⚡ Cache-Control 制御**: no-store / immutable / must-revalidate などの組み合わせを簡単生成。
- **💻 マルチプラットフォーム設定出力**:
  - Raw HTTP
  - Nginx (`add_header`)
  - Apache (`Header set`)
  - Cloudflare / Vercel (`_headers`)
  - Next.js (`next.config.js`)
  - Node.js Express (`res.setHeader`)
- **📚 実務プリセット (6選)**: RESTful API、最高セキュリティ A+ サイト、静的アセットCDN、SPA、Webフォント、Webhook。
- **📖 HTTPヘッダー辞典**: 40種以上のHTTPヘッダー詳細解説と設定例を収録。
- **💾 マイ設定保存**: ローカルDataStoreによるセキュア保存＆復元。

---

### 🛠️ 開発環境
- **OS**: Windows 11
- **JDK**: Java 17
- **AGP**: 8.8.x
- **minSdk**: 24 (Android 7.0+)
- **targetSdk**: 36 (Android 16 Ready)

---

### 📦 ビルド手順

```powershell
# ユニットテスト実行
.\gradlew testDebugUnitTest

# AAB (Android App Bundle) ビルド
.\gradlew bundleRelease

# リリース APK ビルド
.\gradlew assembleRelease
```
