package com.kawanova.headercraft.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kawanova.headercraft.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID

class HeaderViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = HeaderRepository(application)

    // Current working headers
    private val _headers = MutableStateFlow<List<HeaderEntry>>(emptyList())
    val headers: StateFlow<List<HeaderEntry>> = _headers.asStateFlow()

    // Output Server Format
    private val _selectedFormat = MutableStateFlow(ServerFormat.RAW)
    val selectedFormat: StateFlow<ServerFormat> = _selectedFormat.asStateFlow()

    // CORS builder state
    private val _corsState = MutableStateFlow(CorsBuilderState())
    val corsState: StateFlow<CorsBuilderState> = _corsState.asStateFlow()

    // Cache builder state
    private val _cacheState = MutableStateFlow(CacheBuilderState())
    val cacheState: StateFlow<CacheBuilderState> = _cacheState.asStateFlow()

    // Audit result
    private val _auditResult = MutableStateFlow(HeaderEngine.auditHeaders(emptyList()))
    val auditResult: StateFlow<AuditResult> = _auditResult.asStateFlow()

    // Presets and Glossary
    val presets: List<HeaderPreset> = HeaderEngine.getPresets()
    val glossary: List<GlossaryItem> = HeaderEngine.getGlossary()

    // Glossary filtering
    private val _glossaryQuery = MutableStateFlow("")
    val glossaryQuery: StateFlow<String> = _glossaryQuery.asStateFlow()

    private val _selectedGlossaryType = MutableStateFlow<HeaderType?>(null)
    val selectedGlossaryType: StateFlow<HeaderType?> = _selectedGlossaryType.asStateFlow()

    // Saved Configs
    private val _savedConfigs = MutableStateFlow<List<SavedHeaderConfig>>(emptyList())
    val savedConfigs: StateFlow<List<SavedHeaderConfig>> = _savedConfigs.asStateFlow()

    init {
        // Load default preset (REST API) initially
        val defaultPreset = presets.first()
        _headers.value = defaultPreset.headers
        recalculateAudit()

        viewModelScope.launch {
            repository.savedConfigs.collectLatest { list ->
                _savedConfigs.value = list
            }
        }
    }

    private fun recalculateAudit() {
        _auditResult.value = HeaderEngine.auditHeaders(_headers.value)
    }

    fun setFormat(format: ServerFormat) {
        _selectedFormat.value = format
    }

    fun getFormattedOutput(): String {
        return HeaderEngine.formatHeaders(_headers.value, _selectedFormat.value)
    }

    fun addHeader(key: String, value: String, description: String = "") {
        if (key.isBlank()) return
        val current = _headers.value.toMutableList()
        current.add(HeaderEntry(key.trim(), value.trim(), description.trim(), enabled = true))
        _headers.value = current
        recalculateAudit()
    }

    fun updateHeader(index: Int, key: String, value: String, description: String = "") {
        val current = _headers.value.toMutableList()
        if (index in current.indices) {
            val old = current[index]
            current[index] = old.copy(key = key.trim(), value = value.trim(), description = description.ifBlank { old.description })
            _headers.value = current
            recalculateAudit()
        }
    }

    fun toggleHeader(index: Int) {
        val current = _headers.value.toMutableList()
        if (index in current.indices) {
            val old = current[index]
            current[index] = old.copy(enabled = !old.enabled)
            _headers.value = current
            recalculateAudit()
        }
    }

    fun removeHeader(index: Int) {
        val current = _headers.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _headers.value = current
            recalculateAudit()
        }
    }

    fun clearHeaders() {
        _headers.value = emptyList()
        recalculateAudit()
    }

    fun loadPreset(preset: HeaderPreset) {
        _headers.value = preset.headers
        recalculateAudit()
    }

    // CORS builder operations
    fun updateCorsState(state: CorsBuilderState) {
        _corsState.value = state
    }

    fun applyCorsHeaders() {
        val corsHeaders = HeaderEngine.buildCorsHeaders(_corsState.value)
        mergeHeaders(corsHeaders)
    }

    // Cache builder operations
    fun updateCacheState(state: CacheBuilderState) {
        _cacheState.value = state
    }

    fun applyCacheHeaders() {
        val cacheHeaders = HeaderEngine.buildCacheHeaders(_cacheState.value)
        mergeHeaders(cacheHeaders)
    }

    private fun mergeHeaders(newEntries: List<HeaderEntry>) {
        val current = _headers.value.toMutableList()
        val newKeyMap = newEntries.associateBy { it.key.lowercase() }

        // Remove old entries that match the incoming keys
        current.removeAll { it.key.lowercase() in newKeyMap }
        // Add new entries
        current.addAll(newEntries)
        _headers.value = current
        recalculateAudit()
    }

    // Glossary filtering
    fun setGlossaryQuery(query: String) {
        _glossaryQuery.value = query
    }

    fun setGlossaryTypeFilter(type: HeaderType?) {
        _selectedGlossaryType.value = type
    }

    // Saved Configs
    fun saveCurrentConfiguration(title: String, note: String = "") {
        if (title.isBlank()) return
        viewModelScope.launch {
            val config = SavedHeaderConfig(
                id = UUID.randomUUID().toString(),
                title = title.trim(),
                note = note.trim(),
                headers = _headers.value
            )
            repository.saveConfig(config)
        }
    }

    fun loadSavedConfiguration(config: SavedHeaderConfig) {
        _headers.value = config.headers
        recalculateAudit()
    }

    fun deleteSavedConfiguration(id: String) {
        viewModelScope.launch {
            repository.deleteConfig(id)
        }
    }
}
