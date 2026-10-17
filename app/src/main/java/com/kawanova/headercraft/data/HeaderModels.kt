package com.kawanova.headercraft.data

import kotlinx.serialization.Serializable

@Serializable
data class HeaderEntry(
    val key: String,
    val value: String,
    val description: String = "",
    val enabled: Boolean = true
)

enum class ServerFormat(val displayName: String, val extension: String) {
    RAW("Raw HTTP", "txt"),
    NGINX("Nginx", "conf"),
    APACHE("Apache (.htaccess)", "conf"),
    CLOUDFLARE_VERCEL("Cloudflare / Vercel", "_headers"),
    NEXT_JS("Next.js (next.config.js)", "js"),
    EXPRESS("Node.js (Express)", "js")
}

enum class AuditSeverity {
    CRITICAL,
    WARNING,
    INFO,
    SUCCESS
}

@Serializable
data class SecurityAuditItem(
    val severity: AuditSeverity,
    val title: String,
    val message: String,
    val recommendation: String
)

@Serializable
data class AuditResult(
    val score: Int,
    val grade: String,
    val issues: List<SecurityAuditItem>
)

@Serializable
data class HeaderPreset(
    val id: String,
    val nameJa: String,
    val nameEn: String,
    val descriptionJa: String,
    val descriptionEn: String,
    val category: String,
    val headers: List<HeaderEntry>
)

enum class HeaderType(val labelJa: String, val labelEn: String) {
    SECURITY("セキュリティ", "Security"),
    CORS("CORS", "CORS"),
    CACHE("キャッシュ", "Caching"),
    REQUEST("リクエスト", "Request"),
    RESPONSE("レスポンス", "Response"),
    AUTH("認証・クッキー", "Auth / Cookie")
}

@Serializable
data class GlossaryItem(
    val name: String,
    val type: HeaderType,
    val isStandard: Boolean = true,
    val summaryJa: String,
    val summaryEn: String,
    val detailJa: String,
    val detailEn: String,
    val example: String,
    val mdnUrl: String = ""
)

@Serializable
data class SavedHeaderConfig(
    val id: String,
    val title: String,
    val note: String = "",
    val headers: List<HeaderEntry>,
    val createdAt: Long = System.currentTimeMillis()
)

data class CorsBuilderState(
    val allowedOrigins: String = "*",
    val allowedMethods: Set<String> = setOf("GET", "POST", "OPTIONS"),
    val allowedHeaders: String = "Content-Type, Authorization, X-Requested-With",
    val allowCredentials: Boolean = false,
    val maxAgeSeconds: Int = 86400,
    val exposeHeaders: String = ""
)

data class CacheBuilderState(
    val visibility: String = "public", // public, private, no-store, no-cache
    val maxAgeSeconds: Int = 3600,
    val sMaxAgeSeconds: Int? = null,
    val immutable: Boolean = false,
    val mustRevalidate: Boolean = false,
    val staleWhileRevalidateSeconds: Int? = null,
    val etagEnabled: Boolean = true
)
