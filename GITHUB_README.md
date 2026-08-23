# DeepNight SDK for Android TV 🚀

A collection of high-performance modules for Android TV development, extracted from the **DeepNight Launcher** ecosystem.

[![Platform](https://img.shields.io/badge/platform-Android%20TV-green.svg)](https://developer.android.com/tv)
[![Language](https://img.shields.io/badge/language-Kotlin%20%2F%20C%2B%2B-blue.svg)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/license-Dual--License-orange.svg)](#licensing)

## Key Features

### 🎮 TV Input & Navigation
- **FocusEngine**: Robust focus management for complex Compose grids. Prevents focus loss and "jumping".
- **RemoteHandler**: Easy handling of specialized TV keys, including **Long Press Back** for Recents/Menu.

### 🎙 DAP (DeepNight Audio Pipeline)
- **High-Performance FFT**: Real-time frequency analysis in C++/NDK.
- **VAD (Voice Activity Detection)**: Low-latency voice detection to save CPU/Battery.
- **Unity Bridge**: Ready-to-use C# wrapper for game developers.

### 📝 Text & Search Tools
- **Native Russian Stemming**: Fast ending removal for accurate movie search.
- **Phonetic Matching**: Find "Мстители" even if the user typed "Мститити".
- **Metadata Extraction**: Parse quality (4K, UHD) and year from release titles automatically.

### 💎 Premium UI Kit
- **NeonGlowSurface**: Beautiful surfaces with customizable neon glow effects.
- **DeepNightCard**: High-quality TV cards with smooth focus animations and Z-indexing.

---

## Getting Started

### 1. Download AARs
Grab the latest `.aar` files from the [Releases](https://github.com/...) page.

### 2. Add to your project
Copy `.aar` files to your `libs/` folder and add to `build.gradle.kts`:

```kotlin
dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar"))))
}
```

### 3. Usage Example (Kotlin)
```kotlin
// Robust focus management
val focusRegistry = FocusEngine.rememberFocusRegistry()
val fr = focusRegistry.get("my_button")

DeepNightCard(onClick = { ... }, focusRequester = fr) {
    Text("Premium TV UI")
}
```

---

## Licensing
This project uses a **Dual License** model:
1. **Community License**: Free for open-source and non-commercial projects (GPLv3).
2. **Enterprise License**: For commercial apps, TV Box manufacturers, and custom integrations. Includes priority support and closed-source C++ core.

Contact: **@Igor1974** (4PDA/GitHub)

---

Developed with ❤️ for the Android TV community.
