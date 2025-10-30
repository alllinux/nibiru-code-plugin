# Building with GitHub Actions (Network Restrictions Workaround)

If you cannot build the plugin locally due to network restrictions (firewall, proxy, etc.), you can use GitHub Actions to build it with full internet access.

## Why Use GitHub Actions?

- ✅ GitHub servers have unrestricted internet access
- ✅ No local network/firewall issues
- ✅ Automatic builds on every push
- ✅ Free for public repositories
- ✅ Download pre-built plugin artifacts

## Prerequisites

- GitHub account
- Push access to the repository

## Method 1: Automatic Build on Push (Easiest)

Every time you push to the repository, GitHub Actions automatically builds the plugin:

```bash
# Make any change (or just push as-is)
git push origin main

# Or push your branch
git push origin your-branch-name
```

Then:

1. Go to your repository on GitHub
2. Click the **"Actions"** tab
3. Click on the latest workflow run
4. Wait for the build to complete (usually 2-5 minutes)
5. Download the **"nibiru-code-plugin"** artifact
6. Extract the downloaded zip to get the plugin file

## Method 2: Manual Trigger

You can manually trigger a build without making any changes:

1. Go to your repository on GitHub
2. Click **"Actions"** tab
3. Click **"Build Nibiru Plugin"** workflow on the left
4. Click **"Run workflow"** button (top right)
5. Select the branch to build
6. Click the green **"Run workflow"** button
7. Wait for completion and download the artifact

## Method 3: Build on Pull Request

When you create a pull request, the plugin is automatically built and tested:

```bash
# Create a new branch
git checkout -b feature/my-changes

# Make changes, commit
git add .
git commit -m "My changes"

# Push and create PR
git push origin feature/my-changes
```

Then create a PR on GitHub. The build will run automatically, and you can download the artifact from the PR's "Checks" tab.

## Installing the Downloaded Plugin

After downloading the artifact from GitHub Actions:

1. **Extract the artifact:**
   ```bash
   unzip nibiru-code-plugin.zip
   # This gives you: nibiru-code-plugin-1.0.0.zip
   ```

2. **Install in IDE:**
   - Open IntelliJ IDEA or PHPStorm
   - Go to `Settings/Preferences` → `Plugins`
   - Click ⚙️ (gear icon) → `Install Plugin from Disk...`
   - Select `nibiru-code-plugin-1.0.0.zip`
   - Click OK and restart IDE

## Viewing Build Logs

If the build fails:

1. Go to Actions tab
2. Click the failed workflow run
3. Click "build" job
4. Expand the failed step to see error details
5. Copy error logs for troubleshooting

## Configuring the Workflow

The workflow file is at: `.github/workflows/build-plugin.yml`

It automatically:
- Uses JDK 17
- Runs on Ubuntu with full internet access
- Caches Gradle dependencies for faster builds
- Uploads the built plugin as an artifact
- Keeps artifacts for 30 days

## Troubleshooting

### "Actions are disabled for this repository"

Enable GitHub Actions:
1. Go to repository Settings
2. Click "Actions" in left sidebar
3. Select "Allow all actions and reusable workflows"
4. Save

### "Workflow not found"

Make sure the workflow file exists:
```bash
ls -la .github/workflows/build-plugin.yml
```

If missing, pull the latest changes:
```bash
git pull origin main
```

### "Artifact not available"

Artifacts expire after 30 days. Run the workflow again to generate a new build.

### "Build failed in GitHub Actions"

Check the error logs in the Actions tab. Common issues:
- Kotlin/Java code compilation errors
- Plugin configuration issues
- Dependency resolution problems (rare in GitHub Actions)

## Cost

- ✅ **FREE** for public repositories
- ✅ **FREE** for private repositories (2,000 minutes/month for free tier)
- Each build takes ~2-5 minutes

## Alternative: Release Builds

You can also create releases with pre-built plugins:

1. Tag a release:
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```

2. Go to GitHub → Releases → Create Release
3. The workflow artifact can be attached to releases
4. Users can download directly from the Releases page

## Local Build Still Recommended

While GitHub Actions works perfectly, you should still try to fix your local build environment:

1. **Run diagnostics:**
   ```bash
   ./diagnose-network.sh
   ```

2. **Configure proxy if needed** (see NETWORK_TROUBLESHOOTING.md)

3. **Contact IT** to whitelist:
   - `*.gradle.org`
   - `*.maven.org`
   - `*.jetbrains.com`

Having a working local build is more convenient for development and testing.

## Summary

For immediate plugin builds despite network restrictions:

```bash
# Just push to GitHub
git push origin main

# Go to GitHub → Actions → Download artifact
# Extract and install in IDE
```

This is a reliable workaround that bypasses all local network restrictions!
