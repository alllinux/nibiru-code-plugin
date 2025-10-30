package com.maschinen_stockert.nibiru.services

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.intellij.openapi.application.ApplicationManager
import com.maschinen_stockert.nibiru.settings.NibiruSettings
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

class OllamaService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val JSON = "application/json; charset=utf-8".toMediaType()

    companion object {
        fun getInstance(): OllamaService {
            return ApplicationManager.getApplication().getService(OllamaService::class.java)
        }
    }

    fun getBaseUrl(): String {
        val settings = NibiruSettings.getInstance()
        return "${settings.ollamaUrl}:${settings.ollamaPort}"
    }

    /**
     * Fetches available models from Ollama
     */
    fun fetchAvailableModels(callback: (Result<List<OllamaModel>>) -> Unit) {
        val url = "${getBaseUrl()}/api/tags"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(Result.failure(e))
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        callback(Result.failure(IOException("Unexpected code $response")))
                        return
                    }

                    try {
                        val responseBody = response.body?.string() ?: ""
                        val jsonObject = gson.fromJson(responseBody, JsonObject::class.java)
                        val modelsArray = jsonObject.getAsJsonArray("models")

                        val models = modelsArray.map { element ->
                            val modelObj = element.asJsonObject
                            OllamaModel(
                                name = modelObj.get("name").asString,
                                modifiedAt = modelObj.get("modified_at")?.asString ?: "",
                                size = modelObj.get("size")?.asLong ?: 0L
                            )
                        }

                        callback(Result.success(models))
                    } catch (e: Exception) {
                        callback(Result.failure(e))
                    }
                }
            }
        })
    }

    /**
     * Sends a completion request to Ollama
     */
    fun sendCompletionRequest(
        model: String,
        prompt: String,
        callback: (Result<String>) -> Unit
    ) {
        val url = "${getBaseUrl()}/api/generate"

        val requestBody = JsonObject().apply {
            addProperty("model", model)
            addProperty("prompt", prompt)
            addProperty("stream", false)
        }

        val request = Request.Builder()
            .url(url)
            .post(requestBody.toString().toRequestBody(JSON))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(Result.failure(e))
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        callback(Result.failure(IOException("Unexpected code $response")))
                        return
                    }

                    try {
                        val responseBody = response.body?.string() ?: ""
                        val jsonObject = gson.fromJson(responseBody, JsonObject::class.java)
                        val responseText = jsonObject.get("response").asString
                        callback(Result.success(responseText))
                    } catch (e: Exception) {
                        callback(Result.failure(e))
                    }
                }
            }
        })
    }

    /**
     * Test connection to Ollama server
     */
    fun testConnection(callback: (Result<Boolean>) -> Unit) {
        val url = "${getBaseUrl()}/api/tags"

        val request = Request.Builder()
            .url(url)
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

data class OllamaModel(
    val name: String,
    val modifiedAt: String,
    val size: Long
)
