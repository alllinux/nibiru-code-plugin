package com.maschinen_stockert.nibiru.ui

import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.util.ui.JBUI
import com.maschinen_stockert.nibiru.services.HuggingFaceService
import com.maschinen_stockert.nibiru.services.MCPService
import com.maschinen_stockert.nibiru.services.OllamaService
import com.maschinen_stockert.nibiru.settings.ModelPipeline
import com.maschinen_stockert.nibiru.settings.NibiruSettings
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

class NibiruToolWindowContent(private val project: Project) {
    private val mainPanel: JPanel
    private val modelPipelinePanel: ModelPipelinePanel
    private val inputTextArea: JBTextArea
    private val outputTextArea: JBTextArea
    private val executeButton: JButton
    private val statusLabel: JBLabel

    init {
        mainPanel = JPanel(BorderLayout())

        // Create header
        val headerPanel = JPanel(BorderLayout()).apply {
            border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
            add(JBLabel("Nibiru Coding Agent").apply {
                font = font.deriveFont(Font.BOLD, 16f)
            }, BorderLayout.WEST)
        }

        // Create model pipeline visualization panel
        modelPipelinePanel = ModelPipelinePanel()
        val pipelineScrollPane = JBScrollPane(modelPipelinePanel).apply {
            preferredSize = JBUI.size(400, 200)
            border = BorderFactory.createTitledBorder("Model Pipeline")
        }

        // Create input panel
        inputTextArea = JBTextArea(5, 40).apply {
            lineWrap = true
            wrapStyleWord = true
        }
        val inputScrollPane = JBScrollPane(inputTextArea).apply {
            preferredSize = JBUI.size(400, 150)
            border = BorderFactory.createTitledBorder("Input")
        }

        // Create output panel
        outputTextArea = JBTextArea(10, 40).apply {
            lineWrap = true
            wrapStyleWord = true
            isEditable = false
        }
        val outputScrollPane = JBScrollPane(outputTextArea).apply {
            preferredSize = JBUI.size(400, 250)
            border = BorderFactory.createTitledBorder("Output")
        }

        // Create control panel
        executeButton = JButton("Execute Pipeline").apply {
            addActionListener { executePipeline() }
        }

        val refreshPipelineButton = JButton("Refresh Pipeline").apply {
            addActionListener { modelPipelinePanel.refresh() }
        }

        val mcpCommandsButton = JButton("Query MCP Commands").apply {
            addActionListener { queryMCPCommands() }
        }

        statusLabel = JBLabel("Ready")

        val controlPanel = JPanel(FlowLayout(FlowLayout.LEFT)).apply {
            add(executeButton)
            add(refreshPipelineButton)
            add(mcpCommandsButton)
            add(Box.createHorizontalStrut(20))
            add(JBLabel("Status: "))
            add(statusLabel)
        }

        // Assemble main panel
        val centerPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            add(pipelineScrollPane)
            add(Box.createVerticalStrut(10))
            add(inputScrollPane)
            add(Box.createVerticalStrut(10))
            add(outputScrollPane)
            add(Box.createVerticalStrut(10))
            add(controlPanel)
        }

        mainPanel.add(headerPanel, BorderLayout.NORTH)
        mainPanel.add(centerPanel, BorderLayout.CENTER)
    }

    fun getContent(): JComponent = mainPanel

    private fun executePipeline() {
        val input = inputTextArea.text.trim()
        if (input.isEmpty()) {
            JOptionPane.showMessageDialog(
                mainPanel,
                "Please enter some input text",
                "Input Required",
                JOptionPane.WARNING_MESSAGE
            )
            return
        }

        val settings = NibiruSettings.getInstance()
        if (settings.selectedModels.isEmpty()) {
            JOptionPane.showMessageDialog(
                mainPanel,
                "No models selected. Please configure models in Settings.",
                "Configuration Required",
                JOptionPane.WARNING_MESSAGE
            )
            return
        }

        executeButton.isEnabled = false
        statusLabel.text = "Executing..."
        outputTextArea.text = "Processing...\n"

        // Execute first model in the pipeline
        val firstModel = settings.selectedModels[0]
        when (firstModel.provider) {
            "ollama" -> {
                OllamaService.getInstance().sendCompletionRequest(firstModel.name, input) { result ->
                    SwingUtilities.invokeLater {
                        result.onSuccess { response ->
                            outputTextArea.text = "Model: ${firstModel.name}\n\n$response"
                            statusLabel.text = "Completed"
                        }.onFailure { error ->
                            outputTextArea.text = "Error: ${error.message}"
                            statusLabel.text = "Failed"
                        }
                        executeButton.isEnabled = true
                    }
                }
            }
            "huggingface" -> {
                HuggingFaceService.getInstance().sendInferenceRequest(firstModel.name, input) { result ->
                    SwingUtilities.invokeLater {
                        result.onSuccess { response ->
                            outputTextArea.text = "Model: ${firstModel.name}\n\n$response"
                            statusLabel.text = "Completed"
                        }.onFailure { error ->
                            outputTextArea.text = "Error: ${error.message}"
                            statusLabel.text = "Failed"
                        }
                        executeButton.isEnabled = true
                    }
                }
            }
            else -> {
                SwingUtilities.invokeLater {
                    outputTextArea.text = "Unknown provider: ${firstModel.provider}"
                    statusLabel.text = "Failed"
                    executeButton.isEnabled = true
                }
            }
        }
    }

    private fun queryMCPCommands() {
        statusLabel.text = "Connecting to MCP..."
        MCPService.getInstance().performHandshake { handshakeResult ->
            handshakeResult.onSuccess { handshake ->
                MCPService.getInstance().queryAvailableCommands { commandsResult ->
                    SwingUtilities.invokeLater {
                        commandsResult.onSuccess { commands ->
                            val commandsList = commands.joinToString("\n") { cmd ->
                                "- ${cmd.name}: ${cmd.description}"
                            }
                            outputTextArea.text = "MCP Session: ${handshake.sessionId}\n" +
                                    "Server: ${handshake.serverName}\n" +
                                    "Protocol: ${handshake.protocolVersion}\n\n" +
                                    "Available Commands:\n$commandsList"
                            statusLabel.text = "MCP Connected"
                        }.onFailure { error ->
                            outputTextArea.text = "Error querying commands: ${error.message}"
                            statusLabel.text = "MCP Error"
                        }
                    }
                }
            }.onFailure { error ->
                SwingUtilities.invokeLater {
                    outputTextArea.text = "Handshake failed: ${error.message}"
                    statusLabel.text = "MCP Connection Failed"
                }
            }
        }
    }
}

class ModelPipelinePanel : JPanel() {
    private val settings = NibiruSettings.getInstance()
    private val modelBoxes = mutableListOf<ModelBox>()

    init {
        layout = null // Use absolute positioning for custom layout
        background = JBColor.WHITE
        preferredSize = JBUI.size(800, 400)

        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (SwingUtilities.isRightButton(e)) {
                    showContextMenu(e.x, e.y)
                }
            }
        })

        refresh()
    }

    fun refresh() {
        removeAll()
        modelBoxes.clear()

        val models = settings.selectedModels
        val spacing = 150
        var x = 50

        models.forEachIndexed { index, model ->
            val box = ModelBox(model.name, model.provider, x, 100)
            modelBoxes.add(box)
            add(box)
            x += spacing
        }

        revalidate()
        repaint()
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2d = g as Graphics2D
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        // Draw connections between models
        val pipelines = settings.modelPipelines
        pipelines.forEach { pipeline ->
            val sourceBox = modelBoxes.find { it.modelName == findModelName(pipeline.sourceModelId) }
            val targetBox = modelBoxes.find { it.modelName == findModelName(pipeline.targetModelId) }

            if (sourceBox != null && targetBox != null) {
                g2d.color = JBColor.GRAY
                g2d.stroke = BasicStroke(2f)
                g2d.drawLine(
                    sourceBox.x + sourceBox.width,
                    sourceBox.y + sourceBox.height / 2,
                    targetBox.x,
                    targetBox.y + targetBox.height / 2
                )

                // Draw arrow
                val arrowSize = 10
                val arrowX = targetBox.x
                val arrowY = targetBox.y + targetBox.height / 2
                val xPoints = intArrayOf(arrowX, arrowX - arrowSize, arrowX - arrowSize)
                val yPoints = intArrayOf(arrowY, arrowY - arrowSize / 2, arrowY + arrowSize / 2)
                g2d.fillPolygon(xPoints, yPoints, 3)
            }
        }
    }

    private fun findModelName(modelId: String): String? {
        return settings.selectedModels.find { it.id == modelId }?.name
    }

    private fun showContextMenu(x: Int, y: Int) {
        val popup = JPopupMenu()
        val addConnectionItem = JMenuItem("Add Connection Between Models")
        addConnectionItem.addActionListener {
            showAddConnectionDialog()
        }
        popup.add(addConnectionItem)
        popup.show(this, x, y)
    }

    private fun showAddConnectionDialog() {
        val models = settings.selectedModels
        if (models.size < 2) {
            JOptionPane.showMessageDialog(
                this,
                "You need at least 2 models to create a connection",
                "Insufficient Models",
                JOptionPane.WARNING_MESSAGE
            )
            return
        }

        val modelNames = models.map { it.name }.toTypedArray()
        val sourceModel = JOptionPane.showInputDialog(
            this,
            "Select source model:",
            "Add Connection",
            JOptionPane.QUESTION_MESSAGE,
            null,
            modelNames,
            modelNames[0]
        ) as? String

        if (sourceModel != null) {
            val targetModel = JOptionPane.showInputDialog(
                this,
                "Select target model:",
                "Add Connection",
                JOptionPane.QUESTION_MESSAGE,
                null,
                modelNames,
                modelNames[0]
            ) as? String

            if (targetModel != null && sourceModel != targetModel) {
                val sourceId = models.find { it.name == sourceModel }?.id ?: return
                val targetId = models.find { it.name == targetModel }?.id ?: return

                settings.modelPipelines.add(
                    ModelPipeline(
                        id = java.util.UUID.randomUUID().toString(),
                        sourceModelId = sourceId,
                        targetModelId = targetId,
                        description = "$sourceModel -> $targetModel"
                    )
                )

                refresh()
            }
        }
    }
}

class ModelBox(val modelName: String, private val provider: String, x: Int, y: Int) : JPanel() {
    init {
        layout = BorderLayout()
        setBounds(x, y, 120, 60)
        border = BorderFactory.createLineBorder(JBColor.DARK_GRAY, 2)
        background = when (provider) {
            "ollama" -> JBColor(Color(230, 240, 255), Color(40, 60, 100))
            "huggingface" -> JBColor(Color(255, 245, 230), Color(100, 80, 40))
            else -> JBColor.WHITE
        }

        val nameLabel = JBLabel(modelName).apply {
            horizontalAlignment = SwingConstants.CENTER
            font = font.deriveFont(Font.BOLD, 11f)
        }

        val providerLabel = JBLabel(provider).apply {
            horizontalAlignment = SwingConstants.CENTER
            font = font.deriveFont(Font.PLAIN, 9f)
        }

        val labelPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            add(nameLabel)
            add(providerLabel)
            isOpaque = false
        }

        add(labelPanel, BorderLayout.CENTER)
    }
}
