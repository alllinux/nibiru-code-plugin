# Nibiru Coding Agent

A JetBrains IDE plugin for the Nibiru Coding Agent that enables AI-powered coding assistance with support for Ollama, HuggingFace, and Model Context Protocol (MCP).

## Features

- **Multi-Provider AI Support**: Connect to Ollama and HuggingFace AI models
- **Model Pipeline**: Create visual pipelines connecting multiple AI models
- **MCP Integration**: Full support for Model Context Protocol with handshake and command querying
- **Tool Window**: Easy-to-use interface on the right side of your IDE
- **Comprehensive Configuration**: Manage endpoints, ports, authentication, and model selections
- **Compatible**: Works with PHPStorm and IntelliJ IDEA Ultimate (2024.1+)

## Installation

### Option 1: Download Pre-built Plugin (Recommended for Network Issues)

If you have network restrictions or build issues, use GitHub Actions to build the plugin:

1. Go to the [GitHub Actions page](https://github.com/alllinux/nibiru-code-plugin/actions)
2. Click the latest successful workflow run
3. Download the `nibiru-code-plugin` artifact
4. Extract and install in your IDE

See [BUILD_WITH_GITHUB_ACTIONS.md](BUILD_WITH_GITHUB_ACTIONS.md) for detailed instructions.

### Option 2: Build from Source

**Prerequisites**:
- Internet access to download dependencies from Maven Central, Gradle Plugin Portal, and JetBrains repositories
- **Gradle 8.0 or higher** (run `gradle --version` to check)
  - If you have Gradle 4.x or older, run `./upgrade-gradle.sh` after cloning

**Diagnostic**: Run `./diagnose-network.sh` to test if your network allows Gradle to download dependencies

1. Clone the repository:
   ```bash
   git clone https://github.com/alllinux/nibiru-code-plugin.git
   cd nibiru-code-plugin
   ```

2. Build the plugin:
   ```bash
   # Option A: Use Gradle wrapper (downloads Gradle 8.5)
   ./gradlew buildPlugin

   # Option B: Use system Gradle (if you have Gradle 8+ installed)
   gradle buildPlugin

   # Option C: Use the build script
   ./build.sh
   ```

3. The plugin will be generated in `build/distributions/`

4. Install in your IDE:
   - Go to `Settings/Preferences` → `Plugins` → `⚙️` → `Install Plugin from Disk...`
   - Select the generated `.zip` file

**Troubleshooting**: If you encounter build errors, see:
- [QUICK_FIX.md](QUICK_FIX.md) - For Java/Gradle version issues
- [NETWORK_TROUBLESHOOTING.md](NETWORK_TROUBLESHOOTING.md) - For network/proxy issues
- [BUILD_WITH_GITHUB_ACTIONS.md](BUILD_WITH_GITHUB_ACTIONS.md) - Build remotely without local network issues

## Configuration

1. Open `Settings/Preferences` → `Tools` → `Nibiru Coding Agent`

2. Configure your AI providers:

### Ollama Configuration
- **URL**: Default `http://localhost`
- **Port**: Default `11434`
- Click "Test Connection" to verify

### HuggingFace Configuration
- **URL**: Default `https://api-inference.huggingface.co`
- **Auth Token**: Your HuggingFace API token
- Click "Test Connection" to verify

### MCP Server Configuration
- **Endpoint URL**: Your MCP server URL
- **Port**: MCP server port
- Click "Test Connection" to verify handshake

### Model Selection
1. Select provider (Ollama or HuggingFace)
2. Click "Load Models" to fetch available models
3. Select a model from the dropdown
4. Click "Add Model" to add it to your pipeline

## Usage

1. Open the Nibiru Coding Agent tool window from the right sidebar
2. View and manage your model pipeline
3. Enter text in the input field
4. Click "Execute Pipeline" to process with your configured models
5. View results in the output panel

### Model Pipelines

Create connections between models for sequential processing:
1. Right-click in the pipeline panel
2. Select "Add Connection Between Models"
3. Choose source and target models
4. The pipeline will visually display the connections

### MCP Commands

Query available MCP commands:
1. Click "Query MCP Commands" button
2. View available commands and server information in the output panel

## Development

### Prerequisites
- JDK 17 or higher
- Gradle 8.0+

### Building
```bash
./gradlew build
```

### Running in Development
```bash
./gradlew runIde
```

### Testing
```bash
./gradlew test
```

## Technical Details

- **Language**: Kotlin
- **Platform**: IntelliJ Platform 2024.1+
- **Dependencies**: OkHttp, Gson, Kotlin Coroutines
- **Supported IDEs**: IntelliJ IDEA Ultimate, PHPStorm

## Project Structure

```
src/main/kotlin/com/maschinen_stockert/nibiru/
├── services/          # API clients (Ollama, HuggingFace, MCP)
├── settings/          # Configuration and state management
└── ui/                # User interface components
```

## License

See [LICENSE](LICENSE) file for details.

## Support

For issues and feature requests, please use the GitHub issue tracker.
