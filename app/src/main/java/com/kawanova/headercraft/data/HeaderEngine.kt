package com.kawanova.headercraft.data

object HeaderEngine {

    fun buildCorsHeaders(state: CorsBuilderState): List<HeaderEntry> {
        val list = mutableListOf<HeaderEntry>()

        val origin = state.allowedOrigins.ifBlank { "*" }
        list.add(HeaderEntry("Access-Control-Allow-Origin", origin, "アクセスを許可するオリジン（呼び出し元ドメイン）"))

        if (state.allowedMethods.isNotEmpty()) {
            val methods = state.allowedMethods.sorted().joinToString(", ")
            list.add(HeaderEntry("Access-Control-Allow-Methods", methods, "許可するHTTPメソッド一覧"))
        }

        if (state.allowedHeaders.isNotBlank()) {
            list.add(HeaderEntry("Access-Control-Allow-Headers", state.allowedHeaders.trim(), "クライアントからの送信を許可するリクエストヘッダー"))
        }

        if (state.allowCredentials) {
            list.add(HeaderEntry("Access-Control-Allow-Credentials", "true", "クッキーや認証情報のクロスオリジン送信を許可"))
        }

        if (state.maxAgeSeconds > 0) {
            list.add(HeaderEntry("Access-Control-Max-Age", state.maxAgeSeconds.toString(), "プリフライト（OPTIONS）結果のブラウザキャッシュ時間（秒）"))
        }

        if (state.exposeHeaders.isNotBlank()) {
            list.add(HeaderEntry("Access-Control-Expose-Headers", state.exposeHeaders.trim(), "ブラウザ側JavaScriptからアクセス可能にするレスポンスヘッダー"))
        }

        return list
    }

    fun buildCacheHeaders(state: CacheBuilderState): List<HeaderEntry> {
        val directives = mutableListOf<String>()

        when (state.visibility.lowercase()) {
            "no-store" -> {
                directives.add("no-store")
                directives.add("no-cache")
                directives.add("must-revalidate")
                directives.add("proxy-revalidate")
            }
            "no-cache" -> {
                directives.add("no-cache")
                directives.add("must-revalidate")
            }
            "private" -> {
                directives.add("private")
                directives.add("max-age=${state.maxAgeSeconds}")
            }
            else -> {
                directives.add("public")
                directives.add("max-age=${state.maxAgeSeconds}")
            }
        }

        if (state.visibility != "no-store" && state.visibility != "no-cache") {
            if (state.sMaxAgeSeconds != null && state.sMaxAgeSeconds > 0) {
                directives.add("s-maxage=${state.sMaxAgeSeconds}")
            }
            if (state.immutable) {
                directives.add("immutable")
            }
            if (state.mustRevalidate) {
                directives.add("must-revalidate")
            }
            if (state.staleWhileRevalidateSeconds != null && state.staleWhileRevalidateSeconds > 0) {
                directives.add("stale-while-revalidate=${state.staleWhileRevalidateSeconds}")
            }
        }

        val list = mutableListOf<HeaderEntry>()
        list.add(HeaderEntry("Cache-Control", directives.joinToString(", "), "ブラウザおよび中間キャッシュプロキシへのキャッシュ指示"))

        if (state.visibility == "no-store") {
            list.add(HeaderEntry("Pragma", "no-cache", "HTTP/1.0 後方互換用キャッシュ無効化ヘッダー"))
            list.add(HeaderEntry("Expires", "0", "コンテンツ有効期限（即座に失効）"))
        }

        return list
    }

    fun formatHeaders(headers: List<HeaderEntry>, format: ServerFormat): String {
        val activeHeaders = headers.filter { it.enabled && it.key.isNotBlank() }
        if (activeHeaders.isEmpty()) return "// ヘッダーが設定されていません"

        return when (format) {
            ServerFormat.RAW -> {
                activeHeaders.joinToString("\n") { "${it.key}: ${it.value}" }
            }
            ServerFormat.NGINX -> {
                val sb = StringBuilder()
                sb.append("# Nginx Server Block Configuration\n")
                activeHeaders.forEach {
                    sb.append("add_header ${it.key} \"${escapeQuotes(it.value)}\" always;\n")
                }
                sb.toString()
            }
            ServerFormat.APACHE -> {
                val sb = StringBuilder()
                sb.append("# Apache .htaccess / mod_headers\n<IfModule mod_headers.c>\n")
                activeHeaders.forEach {
                    sb.append("  Header set ${it.key} \"${escapeQuotes(it.value)}\"\n")
                }
                sb.append("</IfModule>\n")
                sb.toString()
            }
            ServerFormat.CLOUDFLARE_VERCEL -> {
                val sb = StringBuilder()
                sb.append("# Cloudflare Pages / Vercel _headers\n/*\n")
                activeHeaders.forEach {
                    sb.append("  ${it.key}: ${it.value}\n")
                }
                sb.toString()
            }
            ServerFormat.NEXT_JS -> {
                val sb = StringBuilder()
                sb.append("// next.config.js\nmodule.exports = {\n")
                sb.append("  async headers() {\n")
                sb.append("    return [\n")
                sb.append("      {\n")
                sb.append("        source: '/:path*',\n")
                sb.append("        headers: [\n")
                activeHeaders.forEach {
                    sb.append("          {\n")
                    sb.append("            key: '${escapeSingleQuotes(it.key)}',\n")
                    sb.append("            value: '${escapeSingleQuotes(it.value)}',\n")
                    sb.append("          },\n")
                }
                sb.append("        ],\n")
                sb.append("      },\n")
                sb.append("    ];\n")
                sb.append("  },\n};\n")
                sb.toString()
            }
            ServerFormat.EXPRESS -> {
                val sb = StringBuilder()
                sb.append("// Express.js Middleware\napp.use((req, res, next) => {\n")
                activeHeaders.forEach {
                    sb.append("  res.setHeader('${escapeSingleQuotes(it.key)}', '${escapeSingleQuotes(it.value)}');\n")
                }
                sb.append("  next();\n});\n")
                sb.toString()
            }
        }
    }

    private fun escapeQuotes(s: String): String = s.replace("\"", "\\\"")
    private fun escapeSingleQuotes(s: String): String = s.replace("'", "\\'")

    fun auditHeaders(headers: List<HeaderEntry>): AuditResult {
        val activeMap = headers.filter { it.enabled }.associate { it.key.trim().lowercase() to it.value.trim() }
        val issues = mutableListOf<SecurityAuditItem>()
        var score = 100

        // 1. CORS Credential + Wildcard Origin Check (CRITICAL)
        val corsOrigin = activeMap["access-control-allow-origin"]
        val corsCreds = activeMap["access-control-allow-credentials"]
        if (corsOrigin == "*" && corsCreds?.lowercase() == "true") {
            score -= 40
            issues.add(
                SecurityAuditItem(
                    AuditSeverity.CRITICAL,
                    "CORS仕様違反: ワイルドカードとCookieの併用",
                    "Access-Control-Allow-Origin に '*' を指定した状態で Access-Control-Allow-Credentials: true を有効にすると、主要ブラウザはリクエストをブロックします。",
                    "特定のオリジン（例: https://example.com）を明示するか、リクエストの Origin ヘッダーを動的に検証して反映してください。"
                )
            )
        } else if (corsOrigin == "*") {
            issues.add(
                SecurityAuditItem(
                    AuditSeverity.INFO,
                    "CORS: 全オリジン開放 (*)",
                    "すべての外部ドメインからのリクエストを許可しています。公開APIやフォント配信には適していますが、プライベートAPIでは制限を推奨します。",
                    "認証が必要なAPIの場合は、許可する信頼できるオリジンを指定してください。"
                )
            )
        }

        // 2. Strict-Transport-Security (HSTS)
        val hsts = activeMap["strict-transport-security"]
        if (hsts == null) {
            score -= 15
            issues.add(
                SecurityAuditItem(
                    AuditSeverity.WARNING,
                    "HSTS (HTTP Strict Transport Security) 未設定",
                    "ブラウザに対して常にHTTPSで通信するよう強制するヘッダーがありません。SSL Strippingや中間者攻撃のリスクが高まります。",
                    "add_header Strict-Transport-Security \"max-age=31536000; includeSubDomains; preload\" always; の追加を推奨します。"
                )
            )
        } else {
            if (!hsts.contains("preload", ignoreCase = true)) {
                issues.add(
                    SecurityAuditItem(
                        AuditSeverity.INFO,
                        "HSTS: Preload 未指定",
                        "HSTSヘッダーは有効ですが、ブラウザのPreloadリストへの登録ディレクティブ (preload) が含まれていません。",
                        "初回アクセス時から完全な保護を得るには preload の付与を検討してください。"
                    )
                )
            }
        }

        // 3. X-Content-Type-Options
        val nosniff = activeMap["x-content-type-options"]
        if (nosniff == null || !nosniff.equals("nosniff", ignoreCase = true)) {
            score -= 10
            issues.add(
                SecurityAuditItem(
                    AuditSeverity.WARNING,
                    "X-Content-Type-Options 未設定",
                    "MIMEタイプスニッフィング攻撃を防ぐヘッダーがありません。悪意あるファイルがスクリプトとして誤認実行される恐れがあります。",
                    "X-Content-Type-Options: nosniff を設定してください。"
                )
            )
        }

        // 4. Content-Security-Policy (CSP)
        val csp = activeMap["content-security-policy"]
        if (csp == null) {
            score -= 20
            issues.add(
                SecurityAuditItem(
                    AuditSeverity.WARNING,
                    "Content-Security-Policy (CSP) 未設定",
                    "XSS（クロスサイトスクリプティング）やデータインジェクション攻撃を遮断する最も重要なヘッダーが設定されていません。",
                    "Content-Security-Policy: default-src 'self' などのポリシーを策定して設定してください。"
                )
            )
        } else if (csp.contains("'unsafe-inline'", ignoreCase = true) || csp.contains("'unsafe-eval'", ignoreCase = true)) {
            score -= 10
            issues.add(
                SecurityAuditItem(
                    AuditSeverity.WARNING,
                    "CSP: unsafe ディレクティブの検出",
                    "CSPに 'unsafe-inline' や 'unsafe-eval' が含まれているため、インラインスクリプト経由のXSS攻撃を完全には防げません。",
                    "可能であれば Nonce (暗黙乱数) または ハッシュ (sha256) ベースの安全なポリシーへの移行を推奨します。"
                )
            )
        }

        // 5. X-Frame-Options or CSP frame-ancestors
        val xfo = activeMap["x-frame-options"]
        val hasFrameAncestors = csp?.contains("frame-ancestors", ignoreCase = true) == true
        if (xfo == null && !hasFrameAncestors) {
            score -= 10
            issues.add(
                SecurityAuditItem(
                    AuditSeverity.WARNING,
                    "クリックジャッキング対策 未設定",
                    "サイトを <iframe> 等に埋め込むことを制限する X-Frame-Options または CSP frame-ancestors がありません。",
                    "X-Frame-Options: DENY または SAMEORIGIN を設定してください。"
                )
            )
        }

        // 6. Referrer-Policy
        val referrer = activeMap["referrer-policy"]
        if (referrer == null) {
            score -= 5
            issues.add(
                SecurityAuditItem(
                    AuditSeverity.INFO,
                    "Referrer-Policy 未設定",
                    "別ドメインへの遷移時に送信されるRefererヘッダー（URLパスやクエリ文字列）の漏洩防止設定がありません。",
                    "Referrer-Policy: strict-origin-when-cross-origin を推奨します。"
                )
            )
        }

        // 7. Permissions-Policy
        val permissions = activeMap["permissions-policy"]
        if (permissions == null) {
            issues.add(
                SecurityAuditItem(
                    AuditSeverity.INFO,
                    "Permissions-Policy 未設定",
                    "カメラ、マイク、位置情報などのブラウザ機能へのアクセス権限を明示的に制限するヘッダーがありません。",
                    "Permissions-Policy: camera=(), microphone=(), geolocation=() の設定を検討してください。"
                )
            )
        }

        val finalScore = score.coerceIn(0, 100)
        val grade = when {
            finalScore >= 95 -> "A+"
            finalScore >= 85 -> "A"
            finalScore >= 70 -> "B"
            finalScore >= 50 -> "C"
            else -> "F"
        }

        if (issues.none { it.severity == AuditSeverity.CRITICAL || it.severity == AuditSeverity.WARNING }) {
            issues.add(
                0,
                SecurityAuditItem(
                    AuditSeverity.SUCCESS,
                    "高水準のセキュリティ設定",
                    "主要なHTTPセキュリティヘッダー（HSTS, CSP, nosniff, クリックジャッキング防止）が適切に構成されています。",
                    "現状の設定を維持し、運用中の脆弱性診断を定期的に実施してください。"
                )
            )
        }

        return AuditResult(finalScore, grade, issues)
    }

    fun getPresets(): List<HeaderPreset> {
        return listOf(
            HeaderPreset(
                id = "rest_api",
                nameJa = "RESTful JSON API (セキュア & no-cache)",
                nameEn = "RESTful JSON API (Secure & No-Cache)",
                descriptionJa = "APIレスポンスのキャッシュ防止、CORS許可、MIMEスニッフィング防止を備えた標準API構成",
                descriptionEn = "Standard API setup with no-store cache, JSON content type, nosniff, and secure CORS",
                category = "API",
                headers = listOf(
                    HeaderEntry("Content-Type", "application/json; charset=utf-8", "レスポンスのMIMEタイプ"),
                    HeaderEntry("Access-Control-Allow-Origin", "*", "すべてのオリジンからのAPI呼び出しを許可"),
                    HeaderEntry("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH, OPTIONS", "許可するHTTPメソッド"),
                    HeaderEntry("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With", "許可するヘッダー"),
                    HeaderEntry("Cache-Control", "no-store, no-cache, must-revalidate, proxy-revalidate", "機密データのキャッシュ防止"),
                    HeaderEntry("X-Content-Type-Options", "nosniff", "MIMEタイプのスニッフィング防止"),
                    HeaderEntry("Strict-Transport-Security", "max-age=31536000; includeSubDomains", "HTTPS通信強制")
                )
            ),
            HeaderPreset(
                id = "a_plus_security",
                nameJa = "最高セキュリティ A+ ランク Webサイト",
                nameEn = "Maximum Security A+ Rating Website",
                descriptionJa = "Mozilla Observatory や SecurityHeaders.com で最高評価 A+ を獲得するための厳格なセキュリティ設定",
                descriptionEn = "Hardened security configuration aiming for A+ score on SecurityHeaders & Mozilla Observatory",
                category = "Security",
                headers = listOf(
                    HeaderEntry("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload", "1年間のHTTPS強制とPreload登録"),
                    HeaderEntry("Content-Security-Policy", "default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self' data:; font-src 'self'; frame-ancestors 'none'; object-src 'none'; base-uri 'self';", "厳格なコンテンツセキュリティポリシー"),
                    HeaderEntry("X-Content-Type-Options", "nosniff", "MIMEスニッフィング防止"),
                    HeaderEntry("X-Frame-Options", "DENY", "クリックジャッキング完全遮断"),
                    HeaderEntry("Referrer-Policy", "strict-origin-when-cross-origin", "安全なリファラー送信"),
                    HeaderEntry("Permissions-Policy", "camera=(), microphone=(), geolocation=(), payment=()", "危険なブラウザハードウェア機能無効化"),
                    HeaderEntry("Cross-Origin-Opener-Policy", "same-origin", "クロスオリジンウィンドウの分離"),
                    HeaderEntry("Cross-Origin-Resource-Policy", "same-origin", "別オリジンからのリソース読込制限")
                )
            ),
            HeaderPreset(
                id = "static_assets",
                nameJa = "静的アセット & CDN長期キャッシュ (1年 immutable)",
                nameEn = "Static Assets & CDN Long-term Cache (1 Year)",
                descriptionJa = "CSS, JS, 画像, フォント等ハッシュ値が付与されたビルドアセットを最大効率でキャッシュ",
                descriptionEn = "Optimized for hashed assets (JS/CSS/Images). Cached for 1 year with immutable directive",
                category = "Performance",
                headers = listOf(
                    HeaderEntry("Cache-Control", "public, max-age=31536000, immutable", "1年間の不変キャッシュ（ブラウザ再検証不要）"),
                    HeaderEntry("Access-Control-Allow-Origin", "*", "CDNや他ドメインからのアセット読み込み許可"),
                    HeaderEntry("Timing-Allow-Origin", "*", "ブラウザ開発者ツールでのパフォーマンスタイミング測定許可"),
                    HeaderEntry("X-Content-Type-Options", "nosniff", "スクリプト等への不正改ざん防止")
                )
            ),
            HeaderPreset(
                id = "spa_frontend",
                nameJa = "SPA / フロントエンドホスティング (React, Vue, Next)",
                nameEn = "SPA & Modern Web Hosting (React, Vue, Next)",
                descriptionJa = "index.html の即座反映 (no-cache) と基本セキュリティを両立させたシングルページアプリ用設定",
                descriptionEn = "Balanced configuration for single-page apps ensuring HTML updates immediately with core security",
                category = "Web",
                headers = listOf(
                    HeaderEntry("Cache-Control", "no-cache, must-revalidate", "HTMLは毎回更新を検証"),
                    HeaderEntry("X-Frame-Options", "SAMEORIGIN", "自ドメイン内でのみ埋め込み許可"),
                    HeaderEntry("X-Content-Type-Options", "nosniff", "MIMEスニッフィング防止"),
                    HeaderEntry("Referrer-Policy", "strict-origin-when-cross-origin", "リファラー漏洩防止"),
                    HeaderEntry("Strict-Transport-Security", "max-age=31536000; includeSubDomains", "HTTPS通信強制")
                )
            ),
            HeaderPreset(
                id = "web_fonts",
                nameJa = "Webフォント配信 (WOFF2 / TTF)",
                nameEn = "Web Fonts Hosting (WOFF2 / TTF)",
                descriptionJa = "クロスドメインでのフォント読み込みを保証するCORSヘッダーと長期キャッシュ",
                descriptionEn = "Required CORS origin headers and long caching for custom web fonts loaded across domains",
                category = "Assets",
                headers = listOf(
                    HeaderEntry("Access-Control-Allow-Origin", "*", "クロスドメインでのフォント描画許可"),
                    HeaderEntry("Cache-Control", "public, max-age=31536000, immutable", "フォントファイルの長期キャッシュ"),
                    HeaderEntry("Timing-Allow-Origin", "*", "フォント読み込み遅延測定許可")
                )
            ),
            HeaderPreset(
                id = "webhook_secure",
                nameJa = "Webhook 受信 & マイクロサービス間通信",
                nameEn = "Webhook & Inter-service Communication",
                descriptionJa = "キャッシュを完全に禁止し、署名検証やペイロード完全性を重視した通信設定",
                descriptionEn = "Strict zero-cache, signature-ready headers for microservice webhooks",
                category = "API",
                headers = listOf(
                    HeaderEntry("Content-Type", "application/json; charset=utf-8", "JSONペイロード"),
                    HeaderEntry("Cache-Control", "no-store, no-cache, must-revalidate", "Webhookイベントのキャッシュ完全無効化"),
                    HeaderEntry("Pragma", "no-cache", "HTTP/1.0 キャッシュ無効化"),
                    HeaderEntry("X-Content-Type-Options", "nosniff", "MIMEスニッフィング防止"),
                    HeaderEntry("X-Robots-Tag", "noindex, nofollow", "検索エンジンのインデックス防止")
                )
            )
        )
    }

    fun getGlossary(): List<GlossaryItem> {
        return listOf(
            GlossaryItem(
                name = "Access-Control-Allow-Origin",
                type = HeaderType.CORS,
                summaryJa = "クロスオリジンアクセスを許可するドメイン（または *）を指定",
                summaryEn = "Indicates whether the response can be shared with requesting code from the given origin",
                detailJa = "ブラウザが別のドメイン（オリジン）からリソースを取得する際、アクセスを許可するかをサーバー側が宣言します。'*'を指定すると全ドメインに公開され、特定のURL（例: https://app.example.com）を指定するとそのドメインのみに制限されます。",
                detailEn = "Specifies which origin(s) are allowed to access the resource. A wildcard '*' allows any domain, while a specific URL restricts access to that origin.",
                example = "Access-Control-Allow-Origin: https://app.example.com"
            ),
            GlossaryItem(
                name = "Access-Control-Allow-Credentials",
                type = HeaderType.CORS,
                summaryJa = "Cookieや認証情報を含むクロスオリジンリクエストの可否を制御",
                summaryEn = "Indicates whether the response to the request can be exposed when the credentials flag is true",
                detailJa = "withCredentials または credentials: 'include' を指定したFetch/XHRリクエストに対し、レスポンスの取得を許可するかを指示します。注意: このヘッダーが 'true' の場合、Access-Control-Allow-Origin にワイルドカード '*' を指定することはブラウザ仕様上禁止されています。",
                detailEn = "Tells browsers whether to expose the response to frontend JavaScript when credentials (cookies, auth headers) are sent. Wildcard '*' is prohibited when this is true.",
                example = "Access-Control-Allow-Credentials: true"
            ),
            GlossaryItem(
                name = "Access-Control-Allow-Methods",
                type = HeaderType.CORS,
                summaryJa = "プリフライト要求時にリソースへのアクセスを許可するHTTPメソッド一覧",
                summaryEn = "Specifies the methods allowed when accessing the resource in response to a preflight request",
                detailJa = "ブラウザが送信するOPTIONSプリフライトリクエストに対するレスポンスとして、実際に利用可能なHTTPメソッド（GET, POST, PUT, DELETEなど）をカンマ区切りで返します。",
                detailEn = "Used in response to a preflight OPTIONS request to list the HTTP methods allowed for subsequent requests.",
                example = "Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS"
            ),
            GlossaryItem(
                name = "Access-Control-Max-Age",
                type = HeaderType.CORS,
                summaryJa = "CORSプリフライト（OPTIONS）結果をブラウザがキャッシュする時間（秒）",
                summaryEn = "Indicates how long the results of a preflight request can be cached",
                detailJa = "プリフライトリクエストの結果をキャッシュさせることで、毎回OPTIONSリクエストが発生するネットワークオーバーヘッドを劇的に軽減できます。Chromium系ブラウザの上限は通常7200秒（2時間）または86400秒（24時間）です。",
                detailEn = "Reduces round-trips by allowing browsers to cache preflight OPTIONS responses for the specified number of seconds.",
                example = "Access-Control-Max-Age: 86400"
            ),
            GlossaryItem(
                name = "Strict-Transport-Security (HSTS)",
                type = HeaderType.SECURITY,
                summaryJa = "ブラウザにHTTPS通信のみを強制し、SSL Stripping攻撃を防御",
                summaryEn = "Enforces secure (HTTPS) connections to the server, protecting against SSL stripping",
                detailJa = "ブラウザに対し、指定した期間（max-age）の間、HTTPではなく常にHTTPS経由でのみアクセスするよう記憶させます。includeSubDomainsで全サブドメインに適用し、preloadでブラウザ組み込みのHSTSリストへの登録を申請できます。",
                detailEn = "Forces browsers to connect via HTTPS only. The preload directive requests inclusion in browser-level HSTS preload lists.",
                example = "Strict-Transport-Security: max-age=31536000; includeSubDomains; preload"
            ),
            GlossaryItem(
                name = "Content-Security-Policy (CSP)",
                type = HeaderType.SECURITY,
                summaryJa = "スクリプトやスタイル、画像などの読み込み元を厳格に制限しXSSを防御",
                summaryEn = "Restricts the resources (such as scripts, images, styles) that the browser is allowed to load",
                detailJa = "Webページが読み込み可能なコンテンツの出所（オリジン）をディレクティブごとに指定します。悪意あるインラインスクリプトや第三者サイトへの不正通信（XSSやデータ窃取）を根本から遮断する最も強力な防御ヘッダーです。",
                detailEn = "Controls where scripts, styles, frames, and images can be fetched from, serving as the strongest defense against Cross-Site Scripting (XSS).",
                example = "Content-Security-Policy: default-src 'self'; script-src 'self' https://trusted.cdn.com;"
            ),
            GlossaryItem(
                name = "X-Content-Type-Options",
                type = HeaderType.SECURITY,
                summaryJa = "ブラウザによるMIMEタイプの推測（スニッフィング）を禁止",
                summaryEn = "Prevents the browser from MIME-sniffing a response away from the declared content-type",
                detailJa = "値に 'nosniff' を設定することで、サーバーが宣言した Content-Type ヘッダーをブラウザが勝手に変更・推測して実行するのを防ぎます。画像としてアップロードされた不正なスクリプトファイルが実行される危険を防止します。",
                detailEn = "Setting 'nosniff' prevents browsers from interpreting files as a different MIME type (e.g., executing an uploaded image file as JavaScript).",
                example = "X-Content-Type-Options: nosniff"
            ),
            GlossaryItem(
                name = "X-Frame-Options",
                type = HeaderType.SECURITY,
                summaryJa = "ページが <iframe> や <frame> に埋め込まれることを制御しクリックジャッキングを防止",
                summaryEn = "Controls whether a browser should be allowed to render a page in a <frame>, <iframe>, or <object>",
                detailJa = "ユーザーに見えない透明なフレームでサイトを包み、不正なクリックを誘発するクリックジャッキング攻撃を防ぎます。'DENY'（全埋め込み禁止）または 'SAMEORIGIN'（同一ドメインのみ許可）を指定します。",
                detailEn = "Guards against clickjacking attacks by forbidding iframe embedding ('DENY') or only permitting embedding on the same origin ('SAMEORIGIN').",
                example = "X-Frame-Options: DENY"
            ),
            GlossaryItem(
                name = "Referrer-Policy",
                type = HeaderType.SECURITY,
                summaryJa = "リンク遷移時やリソース要求時に送信されるRefererヘッダーの情報を制御",
                summaryEn = "Governs which referrer information should be included with requests made",
                detailJa = "URLのパスやパラメータに含まれる機密情報が外部サイトへ送信されるのを防ぎます。'strict-origin-when-cross-origin' はHTTPS間のクロスオリジン遷移時にオリジン部分のみを送信する最もバランスの良い設定です。",
                detailEn = "Controls how much referrer info (URL path, query params) is shared when navigating to other sites. 'strict-origin-when-cross-origin' is standard best practice.",
                example = "Referrer-Policy: strict-origin-when-cross-origin"
            ),
            GlossaryItem(
                name = "Permissions-Policy",
                type = HeaderType.SECURITY,
                summaryJa = "カメラ、マイク、位置情報などのブラウザ機能へのアクセス許可を宣言",
                summaryEn = "Allows site administrators to restrict browser features and APIs like camera, mic, and geolocation",
                detailJa = "旧Feature-Policyの後継ヘッダー。サードパーティスクリプトや埋め込みiframeが勝手にカメラ、マイク、GPS位置情報、自動再生、決済APIなどを起動できないよう制限できます。",
                detailEn = "The successor to Feature-Policy. Disables sensitive device features such as camera, microphone, or geolocation for embedded frames and scripts.",
                example = "Permissions-Policy: camera=(), microphone=(), geolocation=()"
            ),
            GlossaryItem(
                name = "Cache-Control",
                type = HeaderType.CACHE,
                summaryJa = "リクエストとレスポンス双方のキャッシュ動作を制御する最重要ヘッダー",
                summaryEn = "Primary header for holding cache instructions (directives) in both requests and responses",
                detailJa = "max-age（ブラウザ保持秒数）、s-maxage（CDN/共有プロキシ保持秒数）、no-store（キャッシュ保存禁止）、no-cache（毎回サーバー検証）、immutable（更新されない不変ファイル）などのディレクティブを組み合わせて指定します。",
                detailEn = "Configures caching behavior across client browsers, CDNs, and proxies using directives such as max-age, no-store, no-cache, and immutable.",
                example = "Cache-Control: public, max-age=31536000, immutable"
            ),
            GlossaryItem(
                name = "ETag",
                type = HeaderType.CACHE,
                summaryJa = "リソースの特定バージョンを識別するハッシュ値（条件付きGET用）",
                summaryEn = "An identifier for a specific version of a resource, used for conditional cache validation",
                detailJa = "ファイル内容に基づいたハッシュ値を付与します。次回リクエスト時にブラウザが 'If-None-Match' にこのETagを送信し、変更がなければサーバーは 304 Not Modified を返して通信量を最小化します。",
                detailEn = "Contains a digest of the resource content. The browser passes this back in 'If-None-Match' to check if a 304 Not Modified response can be returned.",
                example = "ETag: \"33a64df551425fcc55e4d42a148795d9f25f89d4\""
            ),
            GlossaryItem(
                name = "Vary",
                type = HeaderType.CACHE,
                summaryJa = "キャッシュサーバーが同一URLで異なるキャッシュを保持するための基準ヘッダーを指定",
                summaryEn = "Determines how to match request headers to decide whether a cached response can be used",
                detailJa = "例えば 'Vary: Accept-Encoding' を指定すると、gzip対応クライアントとbr対応クライアントで別々の圧縮キャッシュを保存・返却します。'Vary: Origin' はCORSレスポンスのキャッシュ分離に必須です。",
                detailEn = "Informs caches that responses vary based on specific request headers (e.g., Accept-Encoding for gzip/brotli, or Origin for CORS).",
                example = "Vary: Accept-Encoding, Origin"
            ),
            GlossaryItem(
                name = "Authorization",
                type = HeaderType.AUTH,
                summaryJa = "サーバーへの認証情報（Bearer トークンや Basic 認証）を送信",
                summaryEn = "Contains credentials for authenticating a user agent with a server",
                detailJa = "クライアントがサーバーに対して本人確認情報を伝達するリクエストヘッダーです。通常 JWT や OAuth 2.0 アクセストークンを 'Bearer <token>' の形式で送信します。",
                detailEn = "Carries authentication tokens or credentials to the server, most commonly formatted as 'Bearer <jwt_token>' or 'Basic <base64>'.",
                example = "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            ),
            GlossaryItem(
                name = "Set-Cookie",
                type = HeaderType.AUTH,
                summaryJa = "サーバーからクライアントブラウザへCookieを送信・保存させる",
                summaryEn = "Sends a cookie from the server to the user agent",
                detailJa = "セッションIDやトークンをブラウザに保存します。セキュリティ属性として 'HttpOnly'（JSからのアクセス禁止）、'Secure'（HTTPSのみ送信）、'SameSite=Strict/Lax'（CSRF攻撃防止）を必ず付与するのが推奨されます。",
                detailEn = "Stores state on the client. Critical security flags include 'HttpOnly' (prevents XSS cookie theft), 'Secure' (HTTPS only), and 'SameSite=Lax/Strict' (CSRF defense).",
                example = "Set-Cookie: session_id=xyz123; Secure; HttpOnly; SameSite=Lax; Path=/"
            ),
            GlossaryItem(
                name = "Cross-Origin-Opener-Policy (COOP)",
                type = HeaderType.SECURITY,
                summaryJa = "異なるオリジンの別タブ/ウィンドウとの window.opener 参照を遮断",
                summaryEn = "Ensures a top-level document does not share a browsing context group with cross-origin documents",
                detailJa = "Spectre等のサイドチャネル攻撃やクロスオリジン情報漏洩を防ぐため、リンク先ページとのDOM接続を切り離します。値に 'same-origin' を設定するのが標準です。",
                detailEn = "Isolates top-level windows from cross-origin documents, neutralizing cross-origin window manipulation and Spectre-like side-channel leaks.",
                example = "Cross-Origin-Opener-Policy: same-origin"
            ),
            GlossaryItem(
                name = "Cross-Origin-Resource-Policy (CORP)",
                type = HeaderType.SECURITY,
                summaryJa = "画像や動画、スクリプト等のリソースが他オリジンから読み込まれるのを防御",
                summaryEn = "Allows servers to declare that their resources should not be loaded by cross-origin sites",
                detailJa = "機密データ（ユーザーのアバター画像やPDF等）が、悪意ある他サイトの <img> タグや <script> タグ経由でサイドチャネル攻撃（XS-Leaks）等により盗み見られるのを防ぎます。",
                detailEn = "Prevents other origins from embedding or reading static resources like user images or PDFs via HTML tags.",
                example = "Cross-Origin-Resource-Policy: same-origin"
            )
        )
    }
}
