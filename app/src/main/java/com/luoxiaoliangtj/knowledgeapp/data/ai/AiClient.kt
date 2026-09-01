
package com.luoxiaoliangtj.knowledgeapp.data.ai

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object AiClient {
    private var retrofit: Retrofit? = null
    private var currentBaseUrl: String = ""
    
    fun getService(baseUrl: String): AiService {
        if (retrofit == null || baseUrl != currentBaseUrl) {
            val client = OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build()
            
            val url = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
            retrofit = Retrofit.Builder()
                .baseUrl(url)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            currentBaseUrl = baseUrl
        }
        return retrofit!!.create(AiService::class.java)
    }
}
