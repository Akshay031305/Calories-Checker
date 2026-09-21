package com.example.data.remote

import android.os.SystemClock
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

typealias HealthEntryType = String

data class ParsedHealthItem(
    val name: String,
    val type: HealthEntryType, // "intake" | "burn"
    val calories: Double,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double
)

sealed class ParseResult {
    data class Success(val items: List<ParsedHealthItem>) : ParseResult()
    data class Empty(val message: String) : ParseResult()
    data class Error(val userFriendlyMessage: String) : ParseResult()
}

class GeminiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val TAG = "GeminiService"

        // Prefer gemini-3.8-flash; if experiencing high demand (503) or unavailable, fallback gracefully
        private const val PRIMARY_MODEL = "gemini-3.8-flash"
        private val FALLBACK_MODELS = listOf(
            "gemini-3.6-flash",
            "gemini-3.5-flash",
            "gemini-3.5-flash-lite",
            "gemini-3.1-flash-lite",
            "gemini-flash-latest"
        )
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

        @Volatile
        private var lastSuccessfulModel: String? = null

        @Volatile
        private var primary503Timestamp: Long = 0L
        private const val BACKOFF_DURATION_MS = 60_000L // 1 minute cooldown before re-testing primary after a 503 spike

        val SYSTEM_INSTRUCTION = """
            You are the health and nutrition parsing engine for Healthify AI.

            Convert natural-language descriptions of food, drinks, meals, exercise, and physical activity into structured health-log entries.

            For every mentioned item:
            1. Identify the item.
            2. Classify it as "intake" or "burn".
            3. Determine quantity when available.
            4. Estimate calories.
            5. For food/drink estimate protein, carbohydrates, and fat.
            6. For exercise use zero for protein, carbohydrates, and fat.
            7. Separate multiple foods and activities into separate entries.
            8. Preserve quantities in the name.
            9. Use reasonable standard nutritional/metabolic estimates when exact information is unavailable.
            10. Do not make medical diagnoses or medical claims.

            Use these baseline values when applicable:
            * 1 large egg: 72 kcal, 6.2g protein, 0.4g carbs, 5g fat
            * 100ml toned milk: 51 kcal, 3.3g protein, 4.7g carbs, 2g fat
            * 250ml toned milk: approximately 127 kcal
            * 1 small idli: 32 kcal, 0.8g protein, 6.9g carbs, 0.1g fat
            * Jogging/running: approximately 10.5 kcal/min for a standard 70kg adult at average pace.
            * A 5km jog taking approximately 35 minutes can therefore be estimated around 368 kcal

            Adjust exercise estimates when the user provides duration, distance, weight, or intensity.

            Return only structured output matching the schema.
        """.trimIndent()
    }

    private fun getPrioritizedModels(): List<String> {
        val now = SystemClock.elapsedRealtime()
        val inPrimaryCooldown = (now - primary503Timestamp) < BACKOFF_DURATION_MS

        val list = mutableListOf<String>()
        val working = lastSuccessfulModel

        if (inPrimaryCooldown && working != null && working != PRIMARY_MODEL) {
            // Primary recently had a 503 spike; try our last working model first to ensure instant user response
            list.add(working)
            list.addAll(FALLBACK_MODELS.filter { it != working })
            list.add(PRIMARY_MODEL)
        } else {
            // Default: prefer primary model gemini-3.8-flash first
            list.add(PRIMARY_MODEL)
            if (working != null && working != PRIMARY_MODEL) {
                list.add(working)
            }
            FALLBACK_MODELS.forEach { m ->
                if (!list.contains(m)) list.add(m)
            }
        }
        return list
    }

    suspend fun parseHealthInput(
        text: String,
        userWeightKg: Double = 70.0
    ): ParseResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY.trim()

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API configuration is missing in BuildConfig")
            return@withContext ParseResult.Error("Gemini API configuration is missing.")
        }

        val userPromptWithContext = "User Weight: ${userWeightKg}kg.\nUser Log: $text"

        // Construct structured output schema
        val schema = JSONObject().apply {
            put("type", "OBJECT")
            put("required", JSONArray().apply { put("entries") })
            put("properties", JSONObject().apply {
                put("entries", JSONObject().apply {
                    put("type", "ARRAY")
                    put("items", JSONObject().apply {
                        put("type", "OBJECT")
                        put("required", JSONArray().apply {
                            put("name")
                            put("type")
                            put("calories")
                            put("protein_g")
                            put("carbs_g")
                            put("fat_g")
                        })
                        put("properties", JSONObject().apply {
                            put("name", JSONObject().apply {
                                put("type", "STRING")
                            })
                            put("type", JSONObject().apply {
                                put("type", "STRING")
                                put("enum", JSONArray().apply {
                                    put("intake")
                                    put("burn")
                                })
                            })
                            put("calories", JSONObject().apply {
                                put("type", "NUMBER")
                            })
                            put("protein_g", JSONObject().apply {
                                put("type", "NUMBER")
                            })
                            put("carbs_g", JSONObject().apply {
                                put("type", "NUMBER")
                            })
                            put("fat_g", JSONObject().apply {
                                put("type", "NUMBER")
                            })
                        })
                    })
                })
            })
        }

        val requestJson = JSONObject().apply {
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply {
                        put("text", SYSTEM_INSTRUCTION)
                    })
                })
            })

            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", userPromptWithContext)
                        })
                    })
                })
            })

            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.2)
                put("responseSchema", schema)
            })
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = requestJson.toString().toRequestBody(mediaType)

        val candidateModels = getPrioritizedModels()
        var lastErrorCode: Int? = null
        var lastErrorMessage: String = ""

        for (model in candidateModels) {
            try {
                val url = "$BASE_URL/$model:generateContent?key=$apiKey"
                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                Log.d(TAG, "Sending parse request to Gemini model: $model")
                val response = client.newCall(request).execute()
                val responseString = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    lastErrorCode = response.code
                    lastErrorMessage = responseString

                    // If model is experiencing temporary demand spike (503/429) or unavailable (404/500/502/504),
                    // record warning and smoothly continue to next model candidate without throwing an error
                    if (response.code in listOf(404, 429, 500, 502, 503, 504)) {
                        Log.w(TAG, "Model $model returned HTTP ${response.code}. Switching to next candidate...")
                        if (model == PRIMARY_MODEL && response.code == 503) {
                            primary503Timestamp = SystemClock.elapsedRealtime()
                        }
                        continue
                    }

                    // For client authentication errors (400, 403), stop and report to user
                    return@withContext when (response.code) {
                        400, 403 -> ParseResult.Error("Invalid Gemini API key. Please check your AI Studio configuration.")
                        else -> ParseResult.Error("Couldn't connect to Gemini. Please try again.")
                    }
                }

                // Process successful response
                val root = JSONObject(responseString)
                val candidates = root.optJSONArray("candidates")
                if (candidates == null || candidates.length() == 0) {
                    Log.w(TAG, "Empty candidates array received from Gemini")
                    return@withContext ParseResult.Empty("I couldn't identify a food or activity in that entry.")
                }

                val content = candidates.getJSONObject(0).optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val textOutput = parts?.optJSONObject(0)?.optString("text") ?: ""

                if (textOutput.isBlank()) {
                    Log.w(TAG, "Empty text parts output from Gemini")
                    return@withContext ParseResult.Empty("I couldn't identify a food or activity in that entry.")
                }

                // Parse and validate structured JSON
                val outputJson = JSONObject(textOutput)
                if (!outputJson.has("entries")) {
                    Log.w(TAG, "Schema validation: 'entries' key missing in output from $model")
                    return@withContext ParseResult.Error("Invalid response format from Gemini.")
                }

                val entriesArray = outputJson.optJSONArray("entries")
                if (entriesArray == null || entriesArray.length() == 0) {
                    return@withContext ParseResult.Empty("I couldn't identify a food or activity in that entry.")
                }

                val validatedItems = mutableListOf<ParsedHealthItem>()
                for (i in 0 until entriesArray.length()) {
                    val itemObj = entriesArray.optJSONObject(i) ?: continue

                    val rawName = itemObj.optString("name", "").trim()
                    if (rawName.isBlank()) continue

                    val rawType = itemObj.optString("type", "").lowercase().trim()
                    val type = if (rawType == "burn") "burn" else if (rawType == "intake") "intake" else continue

                    val rawCalories = itemObj.optDouble("calories", Double.NaN)
                    val rawProtein = itemObj.optDouble("protein_g", Double.NaN)
                    val rawCarbs = itemObj.optDouble("carbs_g", Double.NaN)
                    val rawFat = itemObj.optDouble("fat_g", Double.NaN)

                    if (rawCalories.isNaN() || rawCalories.isInfinite() || rawCalories < 0) continue

                    val calories = rawCalories
                    val proteinG = if (type == "burn") 0.0 else (if (rawProtein.isNaN() || rawProtein.isInfinite() || rawProtein < 0) 0.0 else rawProtein)
                    val carbsG = if (type == "burn") 0.0 else (if (rawCarbs.isNaN() || rawCarbs.isInfinite() || rawCarbs < 0) 0.0 else rawCarbs)
                    val fatG = if (type == "burn") 0.0 else (if (rawFat.isNaN() || rawFat.isInfinite() || rawFat < 0) 0.0 else rawFat)

                    validatedItems.add(
                        ParsedHealthItem(
                            name = rawName,
                            type = type,
                            calories = calories,
                            proteinG = proteinG,
                            carbsG = carbsG,
                            fatG = fatG
                        )
                    )
                }

                if (validatedItems.isEmpty()) {
                    return@withContext ParseResult.Empty("I couldn't identify a food or activity in that entry.")
                }

                // Remember working model for instant performance
                lastSuccessfulModel = model
                Log.d(TAG, "Successfully parsed ${validatedItems.size} items using $model")
                return@withContext ParseResult.Success(validatedItems)

            } catch (e: IOException) {
                Log.w(TAG, "Network connection error while calling Gemini $model: ${e.message}. Trying next candidate...")
                if (model == candidateModels.last()) {
                    return@withContext ParseResult.Error("Couldn't connect to Gemini. Please try again.")
                }
                continue
            } catch (e: JSONException) {
                Log.e(TAG, "JSON parsing failure from Gemini $model: ${e.message}", e)
                return@withContext ParseResult.Error("Gemini returned an unexpected response.")
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error processing Gemini response: ${e.message}", e)
                return@withContext ParseResult.Error("Gemini returned an unexpected response.")
            }
        }

        // If all candidate models failed
        Log.e(TAG, "All candidate Gemini models failed. Last error: $lastErrorCode - $lastErrorMessage")
        if (lastErrorCode == 503 || lastErrorCode == 429) {
            return@withContext ParseResult.Error("Gemini is currently experiencing high demand. Please try again in a moment.")
        }
        return@withContext ParseResult.Error("Selected Gemini model is currently unavailable.")
    }
}
