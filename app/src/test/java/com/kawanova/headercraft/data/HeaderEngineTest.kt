package com.kawanova.headercraft.data

import org.junit.Assert.*
import org.junit.Test

class HeaderEngineTest {

    @Test
    fun testBuildCorsHeaders_wildcard() {
        val state = CorsBuilderState(
            allowedOrigins = "*",
            allowedMethods = setOf("GET", "POST"),
            allowedHeaders = "Content-Type, Authorization",
            allowCredentials = false,
            maxAgeSeconds = 86400
        )
        val headers = HeaderEngine.buildCorsHeaders(state)
        val map = headers.associate { it.key to it.value }

        assertEquals("*", map["Access-Control-Allow-Origin"])
        assertEquals("GET, POST", map["Access-Control-Allow-Methods"])
        assertEquals("Content-Type, Authorization", map["Access-Control-Allow-Headers"])
        assertEquals("86400", map["Access-Control-Max-Age"])
        assertFalse(map.containsKey("Access-Control-Allow-Credentials"))
    }

    @Test
    fun testBuildCorsHeaders_withCredentials() {
        val state = CorsBuilderState(
            allowedOrigins = "https://app.example.com",
            allowedMethods = setOf("GET", "POST", "OPTIONS"),
            allowedHeaders = "Content-Type",
            allowCredentials = true,
            maxAgeSeconds = 3600
        )
        val headers = HeaderEngine.buildCorsHeaders(state)
        val map = headers.associate { it.key to it.value }

        assertEquals("https://app.example.com", map["Access-Control-Allow-Origin"])
        assertEquals("true", map["Access-Control-Allow-Credentials"])
        assertEquals("3600", map["Access-Control-Max-Age"])
    }

    @Test
    fun testBuildCacheHeaders_noStore() {
        val state = CacheBuilderState(visibility = "no-store")
        val headers = HeaderEngine.buildCacheHeaders(state)
        val map = headers.associate { it.key to it.value }

        assertTrue(map["Cache-Control"]!!.contains("no-store"))
        assertTrue(map["Cache-Control"]!!.contains("no-cache"))
        assertEquals("no-cache", map["Pragma"])
        assertEquals("0", map["Expires"])
    }

    @Test
    fun testBuildCacheHeaders_immutable() {
        val state = CacheBuilderState(
            visibility = "public",
            maxAgeSeconds = 31536000,
            immutable = true
        )
        val headers = HeaderEngine.buildCacheHeaders(state)
        val map = headers.associate { it.key to it.value }

        val cacheControl = map["Cache-Control"]!!
        assertTrue(cacheControl.contains("public"))
        assertTrue(cacheControl.contains("max-age=31536000"))
        assertTrue(cacheControl.contains("immutable"))
    }

    @Test
    fun testFormatHeaders_allFormats() {
        val headers = listOf(
            HeaderEntry("Content-Type", "application/json"),
            HeaderEntry("X-Frame-Options", "DENY")
        )

        val raw = HeaderEngine.formatHeaders(headers, ServerFormat.RAW)
        assertTrue(raw.contains("Content-Type: application/json"))
        assertTrue(raw.contains("X-Frame-Options: DENY"))

        val nginx = HeaderEngine.formatHeaders(headers, ServerFormat.NGINX)
        assertTrue(nginx.contains("add_header Content-Type \"application/json\" always;"))
        assertTrue(nginx.contains("add_header X-Frame-Options \"DENY\" always;"))

        val apache = HeaderEngine.formatHeaders(headers, ServerFormat.APACHE)
        assertTrue(apache.contains("Header set Content-Type \"application/json\""))

        val nextJs = HeaderEngine.formatHeaders(headers, ServerFormat.NEXT_JS)
        assertTrue(nextJs.contains("key: 'Content-Type'"))
        assertTrue(nextJs.contains("value: 'application/json'"))

        val express = HeaderEngine.formatHeaders(headers, ServerFormat.EXPRESS)
        assertTrue(express.contains("res.setHeader('Content-Type', 'application/json');"))
    }

    @Test
    fun testAuditHeaders_corsCredentialConflict() {
        // Critical conflict: * and credentials=true
        val headers = listOf(
            HeaderEntry("Access-Control-Allow-Origin", "*"),
            HeaderEntry("Access-Control-Allow-Credentials", "true")
        )
        val audit = HeaderEngine.auditHeaders(headers)
        val criticalIssue = audit.issues.find { it.severity == AuditSeverity.CRITICAL }

        assertNotNull("Should find critical CORS violation", criticalIssue)
        assertTrue(audit.score <= 60)
    }

    @Test
    fun testAuditHeaders_aPlusPreset() {
        val presets = HeaderEngine.getPresets()
        val aPlus = presets.find { it.id == "a_plus_security" }!!
        val audit = HeaderEngine.auditHeaders(aPlus.headers)

        assertEquals("A+", audit.grade)
        assertTrue("Score should be 95 or above", audit.score >= 95)
    }

    @Test
    fun testGlossaryItemsNotEmpty() {
        val glossary = HeaderEngine.getGlossary()
        assertTrue("Glossary should contain at least 15 items", glossary.size >= 15)
        val corsHeader = glossary.find { it.name == "Access-Control-Allow-Origin" }
        assertNotNull(corsHeader)
    }
}
