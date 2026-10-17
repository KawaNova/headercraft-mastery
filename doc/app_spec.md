# アプリ仕様書: 033_jwt-inspector (JWTインスペクター / JWT Inspector)

## 1. 基本情報
- **アプリ名 (日本語)**: JWTインスペクター - トークンデコーダ ＆ クレーム検証
- **アプリ名 (英語)**: JWT Inspector - Token Decoder & Claims Analyzer
- **パッケージ名**: `com.kawanova.jwtinspector`
- **対象プラットフォーム**: Android (minSdk 24, targetSdk 36)
- **開発言語・基盤**: Kotlin 2.0.21 / Jetpack Compose / Material 3
- **アーキテクチャ**: MVVM + Jetpack DataStore + 完全オンデバイス処理（外部通信ゼロ）

---

## 2. 目的・コンセプト
- **課題**:
  - JWTトークンをオンライン上のデコーダサービス（jwt.ioなど）に貼り付けると、本番環境の認証トークンや顧客データが漏洩するセキュリティリスクがある。
  - トークンの有効期限（`exp`）が切れているのか、あと何分有効なのかの確認に手間がかかる。
- **解決策**:
  - 100%端末内オンデバイスで安全にBase64UrlデコードとJSON整形を行う。
  - Header（赤）、Payload（紫）、Signature（青）の視覚的3色ハイライト。
  - `exp`, `iat`, `nbf` のリアルタイム検証とカウントダウン表示。
  - 開発テスト用モックJWT（HS256）の即時生成機能。
  - 実務頻出6大テンプレート（Auth0, Firebase, AWS Cognito, OAuth2, GitHub, 期限切れ）を標準装備。

---

## 3. 主要機能一覧
1. **JWTトークンデコーダ**:
   - Header（アルゴリズム、タイプ、鍵ID）のJSON整形表示
   - Payload（全クレーム）のインデントJSON整形表示
   - 署名部（Signature）の抽出表示
   - ワンタップクリップボードコピー
2. **トークン有効性・期限ステータス**:
   - `ACTIVE` (有効・残り時間カウントダウン)
   - `EXPIRED` (期限切れ・失効からの経過時間)
   - `NOT_YET_VALID` (nbf前・有効開始までの時間)
   - `INVALID_FORMAT` (不正構文エラー)
3. **クレーム詳細解説 (Claims Glossary)**:
   - 登録済みクレーム（`iss`, `sub`, `aud`, `exp`, `iat`, `nbf`, `jti`）
   - プロファイルクレーム（`name`, `email`, `roles`, `scope`, `auth_time`）
   - カスタムクレームの自動分類と日本語/英語解説。
4. **モックJWTビルダー**:
   - カスタム `sub`, `name`, `email`, `role`, 有効期限（1h/24h/7d/30d）、秘密鍵によるHS256署名付きモックトークン生成。
5. **実務テンプレート (6選)**:
   - Auth0 / OIDC IDトークン
   - Firebase Auth IDトークン
   - AWS Cognito ユーザートークン
   - 標準 OAuth 2.0 アクセストークン
   - GitHub App サーバー間トークン
   - 期限切れトークン (401検証用)
6. **マイ トークン保存・管理**:
   - 端末内DataStoreによるローカル暗号化・安全保存。
