package com.luoxiaoliangtj.knowledgeapp.data.ai

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

data class ChatRequest(
    val model: String,
    val messages: List<Message>,
    val temperature: Float = 0.7f,
    val max_tokens: Int = 2048
)

data class Message(
    val role: String,
    val content: String
)

data class ChatResponse(
    val choices: List<Choice>? = null,
    val error: ApiError? = null
)

data class Choice(
    val message: Message
)

data class ApiError(
    val message: String,
    val type: String? = null
)

interface AiService {
    @POST
    suspend fun chat(@Url url: String, @Body request: ChatRequest): ChatResponse
}
