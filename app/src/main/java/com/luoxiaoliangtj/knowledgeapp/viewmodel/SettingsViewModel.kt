
package com.luoxiaoliangtj.knowledgeapp.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val apiKey: String = "",
    val baseUrl: String = "https://apihub.agnes-ai.com/v1",
    val model: String = "agnes-2.0-flash",
    val provider: String = "remote",
    val isSaved: Boolean = false
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("knowledge_prefs", Context.MODE_PRIVATE)
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        _uiState.value = SettingsUiState(
            apiKey = prefs.getString("api_key", "") ?: "",
            baseUrl = prefs.getString("base_url", "https://apihub.agnes-ai.com/v1") ?: "https://apihub.agnes-ai.com/v1",
            model = prefs.getString("model", "agnes-2.0-flash") ?: "agnes-2.0-flash",
            provider = prefs.getString("provider", "remote") ?: "remote"
        )
    }
    
    fun updateApiKey(key: String) {
        _uiState.update { it.copy(apiKey = key) }
    }
    
    fun updateBaseUrl(url: String) {
        _uiState.update { it.copy(baseUrl = url) }
    }
    
    fun updateModel(model: String) {
        _uiState.update { it.copy(model = model) }
    }
    
    fun updateProvider(provider: String) {
        _uiState.update { it.copy(provider = provider) }
    }
    
    fun saveSettings() {
        viewModelScope.launch {
            prefs.edit()
                .putString("api_key", _uiState.value.apiKey)
                .putString("base_url", _uiState.value.baseUrl)
                .putString("model", _uiState.value.model)
                .putString("provider", _uiState.value.provider)
                .apply()
            _uiState.update { it.copy(isSaved = true) }
        }
    }
    
    fun getApiKey(): String = _uiState.value.apiKey
    fun getBaseUrl(): String = _uiState.value.baseUrl
    fun getModel(): String = _uiState.value.model
}
