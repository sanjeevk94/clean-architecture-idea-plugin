# Publishing Guide

This repo ships **two separate artefacts** published to different registries:

| Component | Registry | Task |
|-----------|----------|------|
| `clean-architecture-idea-plugin` | [JetBrains Marketplace](https://plugins.jetbrains.com) | `publishPlugin` |
| `clean-architecture-gradle-plugin` | [Gradle Plugin Portal](https://plugins.gradle.org) | `publishPlugin` |

> **Recommended:** Publish the IDE plugin first — it is fully self-contained and requires
> no Gradle plugin in the user's project. The Gradle plugin is optional for CI/headless use.

---

## Part 1 — IDE Plugin → JetBrains Marketplace

### Step 1 — Create a JetBrains Account

Go to https://account.jetbrains.com and sign up (or log in).

---

### Step 2 — Generate a Publish Token

1. Go to https://plugins.jetbrains.com/author/me/tokens
2. Click **Generate Token**, give it a name (e.g. `CleanArchPublish`)
3. Copy the token — **you will only see it once**

---

### Step 3 — Generate a Signing Key Pair

JetBrains requires all plugins to be signed since 2021.

```bash
# 1. Download the JetBrains signing CLI
curl -L https://github.com/JetBrains/marketplace-zip-signer/releases/latest/download/marketplace-zip-signer-cli.jar \
  -o marketplace-zip-signer-cli.jar

# 2. Generate a self-signed certificate + private key (valid for 10 years)
java -jar marketplace-zip-signer-cli.jar \
  generateCertificate \
  --key-algorithm RSA \
  --key-size 4096 \
  --days-valid 3650 \
  --cert-file clean-architecture-idea-plugin/chain.crt \
  --private-key-file clean-architecture-idea-plugin/private.pem
```

> The password you set here becomes your `PRIVATE_KEY_PASSWORD`.

---

### Step 4 — Add Secrets to `.gitignore`

Make sure private keys are never committed:

```bash
echo "clean-architecture-idea-plugin/chain.crt"  >> .gitignore
echo "clean-architecture-idea-plugin/private.pem" >> .gitignore
```

---

### Step 5 — Verify the `signing` and `publishing` blocks are active

In `clean-architecture-idea-plugin/build.gradle.kts`, ensure these blocks are **uncommented**:

```kotlin
signing {
    certificateChainFile.set(file("chain.crt"))
    privateKeyFile.set(file("private.pem"))
    password.set(providers.environmentVariable("PRIVATE_KEY_PASSWORD"))
}

publishing {
    token.set(providers.environmentVariable("PUBLISH_TOKEN"))
    // channels.set(listOf("beta"))   // uncomment to publish to beta channel first
}
```

---

### Step 6 — Set Environment Variables

```bash
export PRIVATE_KEY_PASSWORD=your_key_password_here
export PUBLISH_TOKEN=your_marketplace_token_here
```

---

### Step 7 — Verify, Sign & Publish

```bash
cd clean-architecture-idea-plugin

# Check for API compatibility issues — fix any ERROR findings before publishing
./gradlew verifyPlugin

# Build + sign the plugin zip
./gradlew buildPlugin signPlugin

# Upload to JetBrains Marketplace
./gradlew publishPlugin
```

The signed zip is at `build/distributions/clean-architecture-idea-plugin-1.0.0.zip`.

---

### Step 8 — JetBrains Review

- JetBrains reviews **new plugins** within 1–3 business days
- You will receive an email when it is approved and live
- Subsequent **updates** (same plugin ID) go live much faster once you have a track record

---

### Step 9 — How Users Install It

After approval, users install it directly from within any JetBrains IDE:

```
Settings → Plugins → Marketplace → search "Clean Architecture Generator" → Install
```

Or via the zip (before approval / for testing):

```
Settings → Plugins → ⚙ → Install Plugin from Disk → select the .zip
```

---

## Part 2 — Gradle Plugin → Gradle Plugin Portal

> **Note:** The `clean-architecture-gradle-plugin` module has been removed from this repo.
> The IDE plugin is fully self-contained and covers all use cases via the GUI.
> If you want to re-add CLI/CI generation in the future, the Gradle plugin can be
> rebuilt as a separate project.

---

## Part 3 — GitHub Repository Setup

Do this **before** publishing either artefact.

### 1. Add a LICENSE file

MIT or Apache 2.0 are standard for open-source developer tools:

```bash
# MIT example — replace YOUR_NAME
curl https://opensource.org/licenses/MIT | sed "s/\[year\]/$(date +%Y)/; s/\[fullname\]/YOUR_NAME/" > LICENSE
```

### 2. Add a `.gitignore`

```gitignore
# Plugin signing — never commit these
clean-architecture-idea-plugin/chain.crt
clean-architecture-idea-plugin/private.pem
clean-architecture-idea-plugin/keyStore.p12

# Gradle credentials — managed globally in ~/.gradle/gradle.properties
local.properties
*.keystore
```

### 3. Tag the Release

```bash
git add .
git commit -m "chore: prepare v1.0.0 release"
git tag -a v1.0.0 -m "Initial public release"
git push origin main --tags
```

### 4. GitHub Actions — Auto-publish on Tag

Create `.github/workflows/publish-idea-plugin.yml`:

```yaml
name: Publish IDE Plugin

on:
  push:
    tags: [ "v*" ]

jobs:
  publish:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Write signing keys from secrets
        working-directory: clean-architecture-idea-plugin
        env:
          CERT_CHAIN:  ${{ secrets.CERTIFICATE_CHAIN }}
          PRIVATE_KEY: ${{ secrets.PRIVATE_KEY }}
        run: |
          echo "$CERT_CHAIN"  | base64 --decode > chain.crt
          echo "$PRIVATE_KEY" | base64 --decode > private.pem

      - name: Verify plugin
        working-directory: clean-architecture-idea-plugin
        run: ./gradlew verifyPlugin

      - name: Publish to JetBrains Marketplace
        working-directory: clean-architecture-idea-plugin
        env:
          PUBLISH_TOKEN:        ${{ secrets.PUBLISH_TOKEN }}
          PRIVATE_KEY_PASSWORD: ${{ secrets.PRIVATE_KEY_PASSWORD }}
        run: ./gradlew publishPlugin
```

Add these **Repository Secrets** in GitHub → Settings → Secrets and variables → Actions:

| Secret name | Value |
|-------------|-------|
| `PUBLISH_TOKEN` | JetBrains Marketplace token from Step 2 |
| `PRIVATE_KEY_PASSWORD` | Password used when generating the key pair |
| `CERTIFICATE_CHAIN` | `base64 clean-architecture-idea-plugin/chain.crt` |
| `PRIVATE_KEY` | `base64 clean-architecture-idea-plugin/private.pem` |

---

## Version Bump Checklist

When releasing a new version, update **all** of these in one commit before tagging:

| File | Property to update |
|------|--------------------|
| `clean-architecture-idea-plugin/build.gradle.kts` | `version = "X.Y.Z"` and `pluginConfiguration { version = "X.Y.Z" }` |
| `clean-architecture-gradle-plugin/build.gradle.kts` | `version = "X.Y.Z"` |
| `README.md` | Version badge and installation snippet |
| `TECHNICAL_SPEC.md` | Version references |

Then tag and push:

```bash
git tag -a vX.Y.Z -m "Release X.Y.Z: <brief description>"
git push origin vX.Y.Z
```

---

## Quick Reference — Key URLs

| Resource | URL |
|----------|-----|
| JetBrains Marketplace | https://plugins.jetbrains.com |
| Your published plugins | https://plugins.jetbrains.com/author/me |
| Publish tokens | https://plugins.jetbrains.com/author/me/tokens |
| Plugin signing docs | https://plugins.jetbrains.com/docs/intellij/plugin-signing.html |
| Marketplace ZIP Signer | https://github.com/JetBrains/marketplace-zip-signer |
| Gradle Plugin Portal | https://plugins.gradle.org |
