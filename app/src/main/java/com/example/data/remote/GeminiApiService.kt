package com.example.data.remote

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

// --- Common Data Classes ---

@Serializable
data class GenerateContentRequest(
  val contents: List<Content>,
  val generationConfig: GenerationConfig? = null,
  val systemInstruction: Content? = null
)

@Serializable
data class Content(
  val parts: List<Part>
)

@Serializable
data class Part(
  val text: String? = null,
  val inlineData: InlineData? = null
)

@Serializable
data class InlineData(
  val mimeType: String,
  val data: String
)

@Serializable
data class ResponseFormat(
  val text: ResponseFormatText? = null
)

@Serializable
data class ResponseFormatText(
  val mimeType: String,
  val schema: JsonObject? = null
)

@Serializable
data class GenerationConfig(
  val responseFormat: ResponseFormat? = null,
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
  val content: Content? = null
)

@Serializable
data class HomeworkSolutionJson(
  val extractedQuestion: String = "No question detected",
  val subjectDetected: String = "General",
  val quickAnswer: String = "No answer generated",
  val stepByStepSolution: String = "No detailed step-by-step solution available.",
  val keyFormulas: String = ""
)

interface GeminiApiService {
  @POST("v1beta/models/{model}:generateContent")
  suspend fun generateContent(
    @Path("model") model: String,
    @Query("key") apiKey: String,
    @Body request: GenerateContentRequest
  ): GenerateContentResponse
}
