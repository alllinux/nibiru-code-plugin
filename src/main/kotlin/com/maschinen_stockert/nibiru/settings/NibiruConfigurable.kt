package com.maschinen_stockert.nibiru.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import com.maschinen_stockert.nibiru.services.HuggingFaceService
import com.maschinen_stockert.nibiru.services.OllamaService
import java.awt.BorderLayout
import java.awt.Component
import java.util.Locale
import javax.swing.*

class NibiruConfigurable : Configurable {
    private var mainPanel: JPanel? = null

    // Ollama fields
    private val ollamaUrlField = JBTextField()
    private val ollamaPortField = JBTextField()
    private val ollamaTestButton = JButton("Test Connection")

    // HuggingFace fields
    private val huggingFaceUrlField = JBTextField()
    private val huggingFaceTokenField = JBPasswordField()
    private val hfTestButton = JButton("Test Connection")

    // MCP fields
    private val mcpEndpointField = JBTextField()
    private val mcpPortField = JBTextField()
    private val mcpTestButton = JButton("Test Connection")

    // Model selection
    private val modelProviderCombo = ComboBox(arrayOf("Ollama", "HuggingFace"))
    private val availableModelsCombo = ComboBox<ModelListEntry>()
    private val loadModelsButton = JButton("Load Models")
    private val addModelButton = JButton("Add Model")

    // Selected models list
    private val selectedModelsListModel = DefaultListModel<ModelListEntry>()
    private val selectedModelsList = JList(selectedModelsListModel)
    private val removeModelButton = JButton("Remove")

    override fun getDisplayName(): String = "Nibiru Coding Agent"

    override fun createComponent(): JComponent {
        reset()

        // Create Ollama section
        val ollamaPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent("URL:", ollamaUrlField, 1, false)
            .addLabeledComponent("Port:", ollamaPortField, 1, false)
            .addComponent(ollamaTestButton)
            .panel

        val ollamaTitledPanel = JPanel(BorderLayout()).apply {
            border = BorderFactory.createTitledBorder("Ollama Configuration")
            add(ollamaPanel, BorderLayout.CENTER)
        }

        // Create HuggingFace section
        val huggingFacePanel = FormBuilder.createFormBuilder()
            .addLabeledComponent("URL:", huggingFaceUrlField, 1, false)
            .addLabeledComponent("Auth Token:", huggingFaceTokenField, 1, false)
            .addComponent(hfTestButton)
            .panel

        val hfTitledPanel = JPanel(BorderLayout()).apply {
            border = BorderFactory.createTitledBorder("HuggingFace Configuration")
            add(huggingFacePanel, BorderLayout.CENTER)
        }

        // Create MCP section
        val mcpPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent("Endpoint URL:", mcpEndpointField, 1, false)
            .addLabeledComponent("Port:", mcpPortField, 1, false)
            .addComponent(mcpTestButton)
            .panel

        val mcpTitledPanel = JPanel(BorderLayout()).apply {
            border = BorderFactory.createTitledBorder("MCP Server Configuration")
            add(mcpPanel, BorderLayout.CENTER)
        }

        // Create model selection section
        val modelSelectionTopPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            add(JBLabel("Provider: "))
            add(Box.createHorizontalStrut(5))
            add(modelProviderCombo)
            add(Box.createHorizontalStrut(10))
            add(loadModelsButton)
        }

        val modelSelectionMiddlePanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            add(JBLabel("Available Models: "))
            add(Box.createHorizontalStrut(5))
            add(availableModelsCombo)
            add(Box.createHorizontalStrut(10))
            add(addModelButton)
        }

        selectedModelsList.cellRenderer = object : DefaultListCellRenderer() {
            override fun getListCellRendererComponent(
                list: JList<*>,
                value: Any?,
                index: Int,
                isSelected: Boolean,
                cellHasFocus: Boolean
            ): Component {
                val component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
                if (value is ModelListEntry) {
                    val providerLabel = value.provider.replaceFirstChar { char ->
                        if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
                    }
                    text = "${value.name} ($providerLabel)"
                }
                return component
            }
        }

        val selectedModelsScrollPane = JScrollPane(selectedModelsList).apply {
            preferredSize = JBUI.size(400, 150)
        }

        val modelSelectionBottomPanel = JPanel(BorderLayout()).apply {
            add(JBLabel("Selected Models:"), BorderLayout.NORTH)
            add(selectedModelsScrollPane, BorderLayout.CENTER)
            add(removeModelButton, BorderLayout.SOUTH)
        }

        val modelSelectionPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            add(modelSelectionTopPanel)
            add(Box.createVerticalStrut(10))
            add(modelSelectionMiddlePanel)
            add(Box.createVerticalStrut(10))
            add(modelSelectionBottomPanel)
        }

        val modelTitledPanel = JPanel(BorderLayout()).apply {
            border = BorderFactory.createTitledBorder("AI Model Configuration")
            add(modelSelectionPanel, BorderLayout.CENTER)
        }

        // Setup button listeners
        setupButtonListeners()

        // Main panel
        mainPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            add(ollamaTitledPanel)
            add(Box.createVerticalStrut(10))
            add(hfTitledPanel)
            add(Box.createVerticalStrut(10))
            add(mcpTitledPanel)
            add(Box.createVerticalStrut(10))
            add(modelTitledPanel)
            add(Box.createVerticalGlue())
        }

        return JScrollPane(mainPanel).apply {
            border = BorderFactory.createEmptyBorder()
        }
    }

    private fun setupButtonListeners() {
        // Ollama test connection
        ollamaTestButton.addActionListener {
            ollamaTestButton.isEnabled = false
            OllamaService.getInstance().testConnection { result ->
                SwingUtilities.invokeLater {
                    ollamaTestButton.isEnabled = true
                    result.onSuccess {
                        JOptionPane.showMessageDialog(
                            mainPanel,
                            "Connection successful!",
                            "Ollama Connection",
                            JOptionPane.INFORMATION_MESSAGE
                        )
                    }.onFailure { error ->
                        JOptionPane.showMessageDialog(
                            mainPanel,
                            "Connection failed: ${error.message}",
                            "Ollama Connection",
                            JOptionPane.ERROR_MESSAGE
                        )
                    }
                }
            }
        }

        // HuggingFace test connection
        hfTestButton.addActionListener {
            hfTestButton.isEnabled = false
            HuggingFaceService.getInstance().testConnection { result ->
                SwingUtilities.invokeLater {
                    hfTestButton.isEnabled = true
                    result.onSuccess {
                        JOptionPane.showMessageDialog(
                            mainPanel,
                            "Connection successful!",
                            "HuggingFace Connection",
                            JOptionPane.INFORMATION_MESSAGE
                        )
                    }.onFailure { error ->
                        JOptionPane.showMessageDialog(
                            mainPanel,
                            "Connection failed: ${error.message}",
                            "HuggingFace Connection",
                            JOptionPane.ERROR_MESSAGE
                        )
                    }
                }
            }
        }

        // MCP test connection
        mcpTestButton.addActionListener {
            mcpTestButton.isEnabled = false
            com.maschinen_stockert.nibiru.services.MCPService.getInstance().testConnection { result ->
                SwingUtilities.invokeLater {
                    mcpTestButton.isEnabled = true
                    result.onSuccess {
                        JOptionPane.showMessageDialog(
                            mainPanel,
                            "Handshake successful!",
                            "MCP Connection",
                            JOptionPane.INFORMATION_MESSAGE
                        )
                    }.onFailure { error ->
                        JOptionPane.showMessageDialog(
                            mainPanel,
                            "Connection failed: ${error.message}",
                            "MCP Connection",
                            JOptionPane.ERROR_MESSAGE
                        )
                    }
                }
            }
        }

        // Load models button
        loadModelsButton.addActionListener {
            val provider = modelProviderCombo.selectedItem as String
            availableModelsCombo.removeAllItems()

            when (provider) {
                "Ollama" -> {
                    loadModelsButton.isEnabled = false
                    OllamaService.getInstance().fetchAvailableModels { result ->
                        SwingUtilities.invokeLater {
                            loadModelsButton.isEnabled = true
                            result.onSuccess { models ->
                                models.forEach { model ->
                                    availableModelsCombo.addItem(ModelListEntry(model.name, "ollama"))
                                }
                            }.onFailure { error ->
                                JOptionPane.showMessageDialog(
                                    mainPanel,
                                    "Failed to load models: ${error.message}",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE
                                )
                            }
                        }
                    }
                }
                "HuggingFace" -> {
                    val models = HuggingFaceService.getInstance().getAvailableModels()
                    models.forEach { model ->
                        availableModelsCombo.addItem(ModelListEntry(model.id, "huggingface"))
                    }
                }
            }
        }

        // Add model button
        addModelButton.addActionListener {
            val selectedModel = availableModelsCombo.selectedItem as? ModelListEntry
            if (selectedModel != null && !containsModel(selectedModel.name, selectedModel.provider)) {
                selectedModelsListModel.addElement(selectedModel.copy(configId = null))
            }
        }

        // Remove model button
        removeModelButton.addActionListener {
            val selectedIndex = selectedModelsList.selectedIndex
            if (selectedIndex != -1) {
                selectedModelsListModel.remove(selectedIndex)
            }
        }
    }

    override fun isModified(): Boolean {
        val settings = NibiruSettings.getInstance()
        if (ollamaUrlField.text != settings.ollamaUrl ||
            ollamaPortField.text != settings.ollamaPort ||
            huggingFaceUrlField.text != settings.huggingFaceUrl ||
            String(huggingFaceTokenField.password) != settings.huggingFaceToken ||
            mcpEndpointField.text != settings.mcpEndpointUrl ||
            mcpPortField.text != settings.mcpPort
        ) {
            return true
        }

        return isModelSelectionModified(settings)
    }

    private fun isModelSelectionModified(settings: NibiruSettings): Boolean {
        if (selectedModelsListModel.size() != settings.selectedModels.size) {
            return true
        }

        val currentEntries = (0 until selectedModelsListModel.size()).map { index ->
            selectedModelsListModel.getElementAt(index)
        }

        val savedEntries = settings.selectedModels.map { model ->
            ModelListEntry(model.name, model.provider, model.id)
        }

        return currentEntries.zip(savedEntries).any { (current, saved) -> current != saved }
    }

    private fun containsModel(name: String, provider: String): Boolean {
        for (i in 0 until selectedModelsListModel.size()) {
            val entry = selectedModelsListModel.getElementAt(i)
            if (entry.name == name && entry.provider == provider) {
                return true
            }
        }
        return false
    }

    override fun apply() {
        val settings = NibiruSettings.getInstance()
        settings.ollamaUrl = ollamaUrlField.text
        settings.ollamaPort = ollamaPortField.text
        settings.huggingFaceUrl = huggingFaceUrlField.text
        settings.huggingFaceToken = String(huggingFaceTokenField.password)
        settings.mcpEndpointUrl = mcpEndpointField.text
        settings.mcpPort = mcpPortField.text

        // Save selected models
        val existingConfigs = settings.selectedModels.associateBy { it.id }
        settings.selectedModels.clear()
        for (i in 0 until selectedModelsListModel.size()) {
            val entry = selectedModelsListModel.getElementAt(i)
            settings.selectedModels.add(
                existingConfigs[entry.configId]?.copy(
                    name = entry.name,
                    provider = entry.provider
                ) ?: ModelConfig(
                    id = entry.configId ?: java.util.UUID.randomUUID().toString(),
                    name = entry.name,
                    provider = entry.provider
                )
            )
        }
    }

    override fun reset() {
        val settings = NibiruSettings.getInstance()
        ollamaUrlField.text = settings.ollamaUrl
        ollamaPortField.text = settings.ollamaPort
        huggingFaceUrlField.text = settings.huggingFaceUrl
        huggingFaceTokenField.text = settings.huggingFaceToken
        mcpEndpointField.text = settings.mcpEndpointUrl
        mcpPortField.text = settings.mcpPort

        // Load selected models
        selectedModelsListModel.clear()
        settings.selectedModels.forEach { model ->
            selectedModelsListModel.addElement(
                ModelListEntry(
                    name = model.name,
                    provider = model.provider,
                    configId = model.id
                )
            )
        }
    }
}

private data class ModelListEntry(
    val name: String,
    val provider: String,
    val configId: String? = null
) {
    override fun toString(): String = name
}
