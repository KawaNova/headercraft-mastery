# アプリ仕様書: 034_http-headers (HeaderCraft / HTTPヘッダー設定ガイド)

## 1. 基本情報
- **アプリ名 (日本語)**: HTTPヘッダー設定ガイド - CORS・セキュリティ・キャッシュ設定支援
- **アプリ名 (英語)**: HeaderCraft - HTTP Headers, CORS & Security Configurator
- **パッケージ名**: `com.kawanova.headercraft`
- **対象プラットフォーム**: Android (minSdk 24, targetSdk 36)
- **開発言語・基盤**: Kotlin 2.0.21 / Jetpack Compose / Material 3
- **アーキテクチャ**: MVVM + Jetpack DataStore + 完全オンデバイス処理（外部通信ゼロ）

---

## 2. 目的・コンセプト
- **課題**:
  - CORS（Cross-Origin Resource Sharing）の設定ミスによるブラウザブロックや、Cookie送信フラグとワイルドカードOriginの併用による仕様違反障害が頻発する。
  - セキュリティヘッダー（HSTS, CSP, X-Content-Type-Options, X-Frame-Options, Permissions-Policy）の正しい書き方や、Nginx / Apache / Next.js / Express 等のサーバー別構文を調べるのに時間がかかる。
  - Cache-Control の複雑なディレクティブ（no-store vs no-cache、immutable、must-revalidate）の組み合わせ設定にミスが発生しやすい。
- **解決策**:
  - 100%端末内オンデバイスで安全・直感的にヘッダーを構築できるGUIツールを提供。
  - CORS 設定ビルダーにより、Origin、Method、Header、Credentials、Max-Age を視覚的に構築。
  - セキュリティ脆弱性診断機能により、ブラウザブロックの原因となる設定ミスをリアルタイム検知し Grade A+ 獲得を支援。
  - Nginx、Apache、Cloudflare、Next.js、Express 形式へのワンタップ設定コード出力。
  - プロダクション実務プリセット（6選）と40種以上のHTTPヘッダー詳細辞典を搭載。

---

## 3. 主要機能一覧
1. **HTTPヘッダービルダー**:
   - ヘッダーの追加・編集・トグル（ON/OFF）・削除
   - クイックCORSジェネレータ
   - クイックCache-Controlジェネレータ
   - 総合セキュリティスコア判定（Grade A+, A, B, C, F）
2. **マルチプラットフォーム出力**:
   - Raw HTTP
   - Nginx (`add_header ... always;`)
   - Apache (.htaccess `Header set ...`)
   - Cloudflare Pages / Vercel (`_headers`)
   - Next.js (`next.config.js` headers 配列)
   - Node.js Express (`res.setHeader` ミドルウェア)
   - ワンタップクリップボードコピー ＆ 共有
3. **セキュリティ診断エンジン (Audit)**:
   - ワイルドカード Origin と Credentials=true の併用違反検知 (CRITICAL)
   - HSTS 未指定・Preload 欠落検知
   - X-Content-Type-Options (nosniff) 欠落検知
   - CSP 未指定・unsafe インラインスクリプト検知
   - クリックジャッキング対策（X-Frame-Options / frame-ancestors）欠落検知
   - Referrer-Policy, Permissions-Policy 提案
4. **実務プリセット (6選)**:
   - RESTful JSON API (セキュア & no-cache)
   - 最高セキュリティ A+ ランク Webサイト
   - 静的アセット & CDN長期キャッシュ (1年 immutable)
   - SPA / フロントエンドホスティング (React, Vue, Next)
   - Webフォント配信 (WOFF2/TTF)
   - Webhook / マイクロサービス間通信
5. **HTTPヘッダー辞典 (Glossary)**:
   - 40種以上の主要ヘッダーをカテゴリ別（セキュリティ、CORS、キャッシュ、認証、リクエスト、レスポンス）に収録
   - 日本語詳細解説、設定例、ビルダーへのワンタップ追加機能
6. **マイ設定保存・管理**:
   - 端末内DataStoreによるローカル安全保存＆いつでもビルダー復元。
