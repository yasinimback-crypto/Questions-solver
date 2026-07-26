package com.example.data.repository

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.data.remote.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import java.io.ByteArrayOutputStream

class GeminiRepository {

  private val jsonParser = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
  }

  suspend fun analyzeHomeworkImage(
    bitmap: Bitmap,
    selectedSubject: String,
    isBangla: Boolean
  ): HomeworkSolutionJson = withContext(Dispatchers.IO) {
    val apiKey = BuildConfig.GEMINI_API_KEY
    if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
      throw IllegalStateException("Gemini API Key is missing! Please configure GEMINI_API_KEY in the AI Studio Secrets panel or .env file.")
    }

    // Convert Bitmap to Base64 JPEG
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
    val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

    val languagePrompt = if (isBangla) {
      "The user requested the explanation in Bengali (Bangla). Respond strictly in Bengali language (except for standard mathematical symbols/equations)."
    } else {
      "The user requested the explanation in English. Respond strictly in English language."
    }

    val subjectHint = if (selectedSubject != "All Subjects" && selectedSubject != "General") {
      "The user selected the subject filter: '$selectedSubject'. Focus your analysis on this domain."
    } else {
      ""
    }

    val userPrompt = """
      Please analyze this cropped homework or exam question image.
      $subjectHint
      $languagePrompt
      Extract the question precisely, solve it step-by-step with clear logic, and provide a quick summary answer.
      If there are math formulas or equations, format them cleanly using LaTeX syntax (e.g. ${'$'}E = mc^2${'$'} or ${'$'}${'$'}f(x) = x^2${'$'}${'$'}).
      If this is Islamic Studies, provide authentic Quranic verses or Hadith references.
    """.trimIndent()

    val systemInstructionText = """
      You are an expert AI Tutor and Problem Solver designed to help students. Your task is to analyze images uploaded by users—which may contain handwritten or printed questions in Bangla or English—and provide accurate, structured, and easy-to-understand solutions.

      Supported Subjects:
      1. Mathematics (equations, word problems, geometry, algebra, calculus, etc.)
      2. Science (Physics, Chemistry, Biology)
      3. Bengali & English (Grammar, translation, comprehension, essay writing)
      4. Islamic Studies (Quranic verses, Hadith context, Fiqh, Islamic history)

      Rules:
      - Respond in the SAME language as the question (Bangla or English).
      - For Math & Science, provide step-by-step logic, not just the final answer.
      - Use LaTeX syntax for math equations (e.g., ${'$'}E = mc^2${'$'}).
      - For Islamic Studies, provide authentic Quran/Hadith references.
    """.trimIndent()

    // Build JSON Schema for structured output
    val jsonSchema = buildJsonObject {
      put("type", "OBJECT")
      putJsonObject("properties") {
        putJsonObject("extractedQuestion") {
          put("type", "STRING")
          put("description", "The exact question text or equations found in the image.")
        }
        putJsonObject("subjectDetected") {
          put("type", "STRING")
          put("description", "The detected subject: Mathematics, Science, English, Bangla, or Islamic Studies.")
        }
        putJsonObject("quickAnswer") {
          put("type", "STRING")
          put("description", "A concise 1-3 sentence summary of the final answer or core concept.")
        }
        putJsonObject("stepByStepSolution") {
          put("type", "STRING")
          put("description", "Detailed step-by-step logic, derivation, explanation, or proofs formatted cleanly with markdown and LaTeX.")
        }
        putJsonObject("keyFormulas") {
          put("type", "STRING")
          put("description", "Key mathematical formulas, scientific rules, grammar rules, or Quran/Hadith references used.")
        }
      }
      putJsonArray("required") {
        add("extractedQuestion")
        add("subjectDetected")
        add("quickAnswer")
        add("stepByStepSolution")
        add("keyFormulas")
      }
    }

    val request = GenerateContentRequest(
      contents = listOf(
        Content(
          parts = listOf(
            Part(text = userPrompt),
            Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64Image))
          )
        )
      ),
      generationConfig = GenerationConfig(
        temperature = 0.3f,
        responseFormat = ResponseFormat(
          text = ResponseFormatText(
            mimeType = "application/json",
            schema = jsonSchema
          )
        )
      ),
      systemInstruction = Content(
        parts = listOf(Part(text = systemInstructionText))
      )
    )

    try {
      // Use modern preview model gemini-2.5-flash which natively supports vision + structured JSON output
      val response = GeminiClient.service.generateContent("gemini-2.5-flash", apiKey, request)
      val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
      if (responseText.isNullOrBlank()) {
        throw IllegalStateException("Empty response from AI Tutor")
      }

      Log.d("GeminiRepo", "Raw AI Response: $responseText")
      jsonParser.decodeFromString<HomeworkSolutionJson>(responseText)
    } catch (e: Exception) {
      Log.e("GeminiRepo", "Error generating solution: ${e.message}", e)
      throw e
    }
  }
}
