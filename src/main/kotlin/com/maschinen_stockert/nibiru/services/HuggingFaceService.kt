package com.maschinen_stockert.nibiru.services

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.intellij.openapi.application.ApplicationManager
import com.maschinen_stockert.nibiru.settings.NibiruSettings
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

class HuggingFaceService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val JSON = "application/json; charset=utf-8".toMediaType()

    companion object {
        fun getInstance(): HuggingFaceService {
            return ApplicationManager.getApplication().getService(HuggingFaceService::class.java)
        }

        // Popular open-source models on HuggingFace
        val POPULAR_MODELS = listOf(
            "meta-llama/Llama-2-7b-chat-hf",
            "meta-llama/Llama-2-13b-chat-hf",
            "meta-llama/Llama-2-70b-chat-hf",
            "mistralai/Mistral-7B-Instruct-v0.1",
            "mistralai/Mistral-7B-Instruct-v0.2",
            "mistralai/Mixtral-8x7B-Instruct-v0.1",
            "google/flan-t5-small",
            "google/flan-t5-base",
            "google/flan-t5-large",
            "google/flan-t5-xl",
            "tiiuae/falcon-7b-instruct",
            "tiiuae/falcon-40b-instruct",
            "HuggingFaceH4/zephyr-7b-beta",
            "openchat/openchat-3.5-0106",
            "teknium/OpenHermes-2.5-Mistral-7B"
        )
    }

    fun getBaseUrl(): String {
        val settings = NibiruSettings.getInstance()
        return settings.huggingFaceUrl
    }

    private fun getAuthToken(): String {
        return NibiruSettings.getInstance().huggingFaceToken
    }

    /**
     * Fetches available models (returns predefined list of popular models)
     */
    fun getAvailableModels(): List<HuggingFaceModel> {
        return POPULAR_MODELS.map { modelId ->
            HuggingFaceModel(
                id = modelId,
                name = modelId.split("/").last()
            )
        }
    }

    /**
     * Sends an inference request to HuggingFace
     */
    fun sendInferenceRequest(
        modelId: String,
        inputs: String,
        parameters: Map<String, Any> = emptyMap(),
        callback: (Result<String>) -> Unit
    ) {
        val token = getAuthToken()
        if (token.isBlank()) {
            callback(Result.failure(IllegalStateException("HuggingFace token is not configured")))
            return
        }

        val url = "${getBaseUrl()}/models/$modelId"

        val requestBody = JsonObject().apply {
            addProperty("inputs", inputs)
            if (parameters.isNotEmpty()) {
                val paramsObj = JsonObject()
                parameters.forEach { (key, value) ->
                    when (value) {
                        is String -> paramsObj.addProperty(key, value)
                        is Number -> paramsObj.addProperty(key, value)
                        is Boolean -> paramsObj.addProperty(key, value)
                    }
                }
                add("parameters", paramsObj)
            }
        }

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $token")
            .post(requestBody.toString().toRequestBody(JSON))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(Result.failure(e))
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        val errorBody = response.body?.string() ?: "Unknown error"
                        callback(Result.failure(IOException("HuggingFace API error: $errorBody")))
                        return
                    }

                    try {
                        val responseBody = response.body?.string() ?: ""

                        // HuggingFace API returns different formats depending on the model
                        // Try to parse as array first, then as object
                        val result = try {
                            val jsonArray = gson.fromJson(responseBody, JsonArray::class.java)
                            if (jsonArray.size() > 0) {
                                val firstElement = jsonArray[0]
                                if (firstElement.isJsonObject) {
                                    firstElement.asJsonObject.get("generated_text")?.asString
                                        ?: responseBody
                                } else {
                                    responseBody
                                }
                            } else {
                                responseBody
                            }
                        } catch (e: Exception) {
                            // If not an array, try as object
                            try {
                                val jsonObject = gson.fromJson(responseBody, JsonObject::class.java)
                                jsonObject.get("generated_text")?.asString ?: responseBody
                            } catch (e: Exception) {
                                responseBody
                            }
                        }

                        callback(Result.success(result))
                    } catch (e: Exception) {
                        callback(Result.failure(e))
                    }
                }
            }
        })
    }

    /**
     * Test connection to HuggingFace API
     */
    fun testConnection(callback: (Result<Boolean>) -> Unit) {
        val token = getAuthToken()
        if (token.isBlank()) {
            callback(Result.failure(IllegalStateException("HuggingFace token is not configured")))
            return
        }

        // Test with a simple API endpoint
        val url = "https://huggingface.co/api/whoami-v2"

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $token")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(Result.failure(e))
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    callback(Result.success(response.isSuccessful))
                }
            }
        })
    }
}

data class HuggingFaceModel(
    val id: String,
    val name: String
)
