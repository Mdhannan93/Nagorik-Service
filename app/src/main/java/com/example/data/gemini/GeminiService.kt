package com.example.data.gemini

import android.util.Log
import com.example.BuildConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.putJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Gemini Request / Response Models ---

@Serializable
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val tools: List<JsonObject>? = null,
    val systemInstruction: Content? = null
)

@Serializable
data class Content(
    val role: String? = null, // "user" or "model"
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String? = null
)

@Serializable
data class GenerationConfig(
    val temperature: Float? = null,
    val topP: Float? = null,
    val topK: Int? = null
)

@Serializable
data class GenerateContentResponse(
    val candidates: List<Candidate>? = null
)

@Serializable
data class Candidate(
    val content: Content,
    val groundingMetadata: GroundingMetadata? = null
)

@Serializable
data class GroundingMetadata(
    val groundingChunks: List<GroundingChunk>? = null,
    val webSearchQueries: List<String>? = null
)

@Serializable
data class GroundingChunk(
    val web: WebSource? = null
)

@Serializable
data class WebSource(
    val uri: String? = null,
    val title: String? = null
)

// --- Domain Representative Response ---
data class GeminiChatResponse(
    val text: String,
    val searchSources: List<Pair<String, String>> = emptyList(), // Pair of Title and URL
    val searchQueries: List<String> = emptyList()
)

// --- Retrofit Interface ---
interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiRetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val json = Json { 
            ignoreUnknownKeys = true 
            coerceInputValues = true
        }
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}

// --- High-Level Accessor with Search Grounding Enabled ---
object GeminiChatRepository {
    private const val TAG = "GeminiChatRepository"

    suspend fun getAIResponseOfUDC(prompt: String): GeminiChatResponse {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            Log.e(TAG, "Gemini API key is missing or system failed: ${e.message}")
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return GeminiChatResponse(
                text = "My local AI module is awaiting your configuration. Please set up the GEMINI_API_KEY in the Secrets panel in AI Studio.\n\n" +
                        "**Quick Guide on UDC services in Bangladesh:**\n" +
                        "- **Birth Registration:** Needs applicant DOB info, parents' NIDs, and EP1 card. Standard fee: 50 BDT.\n" +
                        "- **Porcha (Land Record):** Needs Khatian No, Mouza Name, Upazila, and District. Standard fee: 100 BDT.\n" +
                        "- **Utility Bill:** Pay DESCO, WASA, or Titas Gas bills. Surcharge: 10 BDT.",
                searchSources = listOf("Online Birth Registration" to "https://bdris.gov.bd"),
                searchQueries = listOf("Birth registration fees Bangladesh")
            )
        }

        // Setup System Instructions to guide the AI for Union Digital Centres
        val systemInstruction = Content(
            parts = listOf(
                Part(
                    text = "You are the official digital services assistant for the Union Digital Centre (UDC) Service in Bangladesh. " +
                            "Use Google Search results to provide accurate, up-to-date, hyper-relevant guidelines to citizens and portal entrepreneurs. " +
                            "Explain registration rules, fees (in Bangladeshi Taka BDT), necessary documents, and steps clearly. " +
                            "Format your responses nicely with markdown. Answer in a professional, welcoming tone, supporting both English and Bengali."
                )
            )
        )

        // Structure a Google Search Grounding tool configuration
        val toolsConfig = listOf(
            buildJsonObject {
                putJsonObject("googleSearch") {}
            }
        )

        val request = GenerateContentRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = prompt)))
            ),
            generationConfig = GenerationConfig(temperature = 0.5f),
            tools = toolsConfig,
            systemInstruction = systemInstruction
        )

        return try {
            val response = GeminiRetrofitClient.service.generateContent(apiKey, request)
            val candidate = response.candidates?.firstOrNull()
            val text = candidate?.content?.parts?.firstOrNull()?.text ?: "No text returned."
            
            // Extract grounding details
            val sources = mutableListOf<Pair<String, String>>()
            val queries = mutableListOf<String>()

            val metadata = candidate?.groundingMetadata
            metadata?.groundingChunks?.forEach { chunk ->
                val title = chunk.web?.title
                val uri = chunk.web?.uri
                if (title != null && uri != null) {
                    sources.add(title to uri)
                }
            }
            metadata?.webSearchQueries?.forEach { query ->
                queries.add(query)
            }

            GeminiChatResponse(
                text = text,
                searchSources = sources.distinctBy { it.second },
                searchQueries = queries
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error generating content: ${e.message}", e)
            GeminiChatResponse(
                text = "I encountered a networking anomaly while consulting the search systems: ${e.localizedMessage}. " +
                        "Let me provide a local summary:\n\n" +
                        "For authenticating or applying, use the Citizen Panel. Ensure required documents are ready for local verification."
            )
        }
    }
}
