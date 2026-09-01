
package com.luoxiaoliangtj.knowledgeapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.luoxiaoliangtj.knowledgeapp.data.repository.AiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatMessage(
    val role: String, // "user" or "assistant"
    val content: String
)

data class AiUiState(
    val messages: List<ChatMessage> = listOf(ChatMessage("assistant", "你好！我是你的知识库助手。你可以问我关于你资料的任何问题，或者让我帮你整理、总结知识。")),
    val isLoading: Boolean = false
)

class AiViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AiRepository()
    private val _uiState = MutableStateFlow(AiUiState())
    val uiState: StateFlow<AiUiState> = _uiState.asStateFlow()
    
    fun sendMessage(baseUrl: String, apiKey: String, model: String, prompt: String, context: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(messages = it.messages + ChatMessage("user", prompt), isLoading = true) }
            
            val result = repository.chat(baseUrl, apiKey, model, prompt, context)
            
            val reply = if (result.isSuccess) {
                result.getOrDefault("未知错误")
            } else {
                "错误: ${result.exceptionOrNull()?.message}"
            }
            
            _uiState.update { 
                it.copy(
                    messages = it.messages + ChatMessage("assistant", reply),
                    isLoading = false
                )
            }
        }
    }
    
    fun summarize(baseUrl: String, apiKey: String, model: String, content: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val result = repository.summarize(baseUrl, apiKey, model, content)
            
            val reply = if (result.isSuccess) {
                result.getOrDefault("总结失败")
            } else {
                "总结失败: ${result.exceptionOrNull()?.message}"
            }
            
            _uiState.update { 
                it.copy(
                    messages = it.messages + ChatMessage("user", "📝 请总结这个文件"),
                    isLoading = false
                )
            }
            _uiState.update { 
                it.copy(messages = it.messages + ChatMessage("assistant", reply))
            }
        }
    }
}
