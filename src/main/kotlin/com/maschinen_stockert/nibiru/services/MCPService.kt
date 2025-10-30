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

class MCPService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val JSON = "application/json; charset=utf-8".toMediaType()

    private var sessionId: String? = null
    private var isConnected: Boolean = false

    companion object {
        fun getInstance(): MCPService {
            return ApplicationManager.getApplication().getService(MCPService::class.java)
        }
    }

    fun getBaseUrl(): String {
        val settings = NibiruSettings.getInstance()
        val url = settings.mcpEndpointUrl
        val port = settings.mcpPort
        return if (port.isNotBlank()) "$url:$port" else url
    }

    /**
     * Performs MCP handshake to establish connection
     */
    fun performHandshake(callback: (Result<MCPHandshakeResponse>) -> Unit) {
        val url = "${getBaseUrl()}/mcp/handshake"

        val requestBody = JsonObject().apply {
            addProperty("protocol_version", "1.0")
            addProperty("client_name", "Nibiru Coding Agent")
            addProperty("client_version", "1.0.0")
        }

        val request = Request.Builder()
            .url(url)
            .post(requestBody.toString().toRequestBody(JSON))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                isConnected = false
                callback(Result.failure(e))
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        isConnected = false
                        callback(Result.failure(IOException("Handshake failed: $response")))
                        return
                    }

                    try {
                        val responseBody = response.body?.string() ?: ""
                        val jsonObject = gson.fromJson(responseBody, JsonObject::class.java)

                        val handshakeResponse = MCPHandshakeResponse(
                            sessionId = jsonObject.get("session_id")?.asString ?: "",
                            protocolVersion = jsonObject.get("protocol_version")?.asString ?: "1.0",
                            serverName = jsonObject.get("server_name")?.asString ?: "Unknown",
                            capabilities = parseCapabilities(jsonObject.get("capabilities")?.asJsonObject)
                        )

                        sessionId = handshakeResponse.sessionId
                        isConnected = true

                        callback(Result.success(handshakeResponse))
                    } catch (e: Exception) {
                        isConnected = false
                        callback(Result.failure(e))
                    }
                }
            }
        })
    }

    /**
     * Queries available commands from the MCP server
     */
    fun queryAvailableCommands(callback: (Result<List<MCPCommand>>) -> Unit) {
        if (!isConnected || sessionId == null) {
            callback(Result.failure(IllegalStateException("Not connected. Please perform handshake first.")))
            return
        }

        val url = "${getBaseUrl()}/mcp/commands"

        val request = Request.Builder()
            .url(url)
            .header("X-Session-Id", sessionId!!)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(Result.failure(e))
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        callback(Result.failure(IOException("Failed to query commands: $response")))
                        return
                    }

                    try {
                        val responseBody = response.body?.string() ?: ""
                        val jsonObject = gson.fromJson(responseBody, JsonObject::class.java)
                        val commandsArray = jsonObject.getAsJsonArray("commands")

                        val commands = commandsArray.map { element ->
                            val cmdObj = element.asJsonObject
                            MCPCommand(
                                name = cmdObj.get("name").asString,
                                description = cmdObj.get("description")?.asString ?: "",
                                parameters = parseParameters(cmdObj.get("parameters")?.asJsonObject)
                            )
                        }

                        callback(Result.success(commands))
                    } catch (e: Exception) {
                        callback(Result.failure(e))
                    }
                }
            }
        })
    }

    /**
     * Executes an MCP command
     */
    fun executeCommand(
        commandName: String,
        parameters: Map<String, Any>,
        callback: (Result<JsonObject>) -> Unit
    ) {
        if (!isConnected || sessionId == null) {
            callback(Result.failure(IllegalStateException("Not connected. Please perform handshake first.")))
            return
        }

        val url = "${getBaseUrl()}/mcp/execute"

        val requestBody = JsonObject().apply {
            addProperty("command", commandName)
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

        val request = Request.Builder()
            .url(url)
            .header("X-Session-Id", sessionId!!)
            .post(requestBody.toString().toRequestBody(JSON))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(Result.failure(e))
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        callback(Result.failure(IOException("Command execution failed: $response")))
                        return
                    }

                    try {
                        val responseBody = response.body?.string() ?: ""
                        val jsonObject = gson.fromJson(responseBody, JsonObject::class.java)
                        callback(Result.success(jsonObject))
                    } catch (e: Exception) {
                        callback(Result.failure(e))
                    }
                }
            }
        })
    }

    /**
     * Test connection to MCP server
     */
    fun testConnection(callback: (Result<Boolean>) -> Unit) {
        performHandshake { result ->
            result.onSuccess {
                callback(Result.success(true))
            }.onFailure {
                callback(Result.failure(it))
            }
        }
    }

    /**
     * Disconnects from the MCP server
     */
    fun disconnect() {
        sessionId = null
        isConnected = false
    }

    private fun parseCapabilities(capabilitiesObj: JsonObject?): List<String> {
        if (capabilitiesObj == null) return emptyList()

        return try {
            val capArray = capabilitiesObj.getAsJsonArray("supported")
            capArray.map { it.asString }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseParameters(parametersObj: JsonObject?): Map<String, String> {
        if (parametersObj == null) return emptyMap()

        return try {
            parametersObj.keySet().associateWith { key ->
                parametersObj.get(key).asString
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }
}

data class MCPHandshakeResponse(
    val sessionId: String,
    val protocolVersion: String,
    val serverName: String,
    val capabilities: List<String>
)

data class MCPCommand(
    val name: String,
    val description: String,
    val parameters: Map<String, String>
)
