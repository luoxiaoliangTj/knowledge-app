
package com.luoxiaoliangtj.knowledgeapp.data.repository

import com.luoxiaoliangtj.knowledgeapp.data.ai.*

class AiRepository {
    
    private val systemPrompt = """你是一个知识库助手。帮助用户整理、搜索、总结他们的知识资料。
回答要简洁、有条理。如果用户问的是关于他们资料的问题，先搜索再回答。"""
    
    suspend fun chat(
        baseUrl: String,
        apiKey: String,
        model: String,
        prompt: String,
        context: String? = null
    ): Result<String> {
        return try {
            val service = AiClient.getService(baseUrl)
            val messages = mutableListOf(
                Message("system", systemPrompt)
            )
            if (!context.isNullOrBlank()) {
                messages.add(Message("user", "相关资料：${context.take(2000)}"))
            }
            messages.add(Message("user", prompt))
            
            val request = ChatRequest(model = model, messages = messages)
            val response = service.chat("chat/completions", request)
            
            if (response.error != null) {
                Result.failure(Exception(response.error.message))
            } else {
                val reply = response.choices?.firstOrNull()?.message?.content
                if (reply != null) {
                    Result.success(reply)
                } else {
                    Result.failure(Exception("Empty response"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun summarize(baseUrl: String, apiKey: String, model: String, content: String): Result<String> {
        val prompt = "请用中文总结以下内容的核心要点（3-5条），语言简洁：\n\n${content.take(5000)}"
        return chat(baseUrl, apiKey, model, prompt, null)
    }
    
    suspend fun classify(baseUrl: String, apiKey: String, model: String, fileName: String): Result<String> {
        val prompt = "根据以下文件名，给出1-3个分类标签（中文，简短），用逗号分隔：\n\n$fileName"
        return chat(baseUrl, apiKey, model, prompt, null)
    }
}
