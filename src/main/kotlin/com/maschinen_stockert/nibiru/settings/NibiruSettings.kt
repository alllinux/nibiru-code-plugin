package com.maschinen_stockert.nibiru.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "com.maschinen_stockert.nibiru.settings.NibiruSettings",
    storages = [Storage("NibiruSettings.xml")]
)
class NibiruSettings : PersistentStateComponent<NibiruSettings> {

    // Ollama Configuration
    var ollamaUrl: String = "http://localhost"
    var ollamaPort: String = "11434"

    // HuggingFace Configuration
    var huggingFaceUrl: String = "https://api-inference.huggingface.co"
    var huggingFaceToken: String = ""

    // MCP Configuration
    var mcpEndpointUrl: String = ""
    var mcpPort: String = ""

    // Selected models
    var selectedModels: MutableList<ModelConfig> = mutableListOf()

    // Model pipelines (connections between models)
    var modelPipelines: MutableList<ModelPipeline> = mutableListOf()

    override fun getState(): NibiruSettings = this

    override fun loadState(state: NibiruSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        fun getInstance(): NibiruSettings {
            return ApplicationManager.getApplication().getService(NibiruSettings::class.java)
        }
    }
}

data class ModelConfig(
    var id: String = "",
    var name: String = "",
    var provider: String = "", // "ollama" or "huggingface"
    var parameters: MutableMap<String, String> = mutableMapOf()
)

data class ModelPipeline(
    var id: String = "",
    var sourceModelId: String = "",
    var targetModelId: String = "",
    var description: String = ""
)
