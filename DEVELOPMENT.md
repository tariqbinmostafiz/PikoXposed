# Development Guide

## Project Structure

```
PikoXposed/
├── app/
│   └── src/main/java/io/github/pikoxposed/
│       ├── MainHook.kt          # Xposed entry point
│       ├── AppPatchInfo.kt      # Package → patch list mapping
│       ├── PatchExecutor.kt     # DexKit-based patch runner
│       ├── morphe/              # DexKit fingerprint compat layer
│       ├── piko/
│       │   ├── twitter/         # Twitter/X patches
│       │   │   ├── ads/         # Hide ads, sensitive media
│       │   │   ├── featureswitch/ # Feature flag override
│       │   │   ├── timeline/    # Force HD, timeline hooks
│       │   │   └── misc/        # Download, etc.
│       │   └── instagram/       # Instagram patches
│       │       ├── ads/         # Hide sponsored content
│       │       └── misc/        # Link sanitizer
├── piko/                        # git submodule (crimera/piko)
│   └── extensions/
│       ├── shared/              # Shared utils (Logger, Utils, etc.)
│       ├── twitter/             # Twitter extension Java classes
│       └── instagram/           # Instagram extension Java classes
├── libs/
│   └── dexkit-android.aar       # Custom DexKit (copy from NexAlloy)
└── stub/                        # Android SDK stubs
```

## How it works

1. **MainHook** is invoked by LSPosed when Twitter/Instagram loads
2. **PatchExecutor** uses DexKit to scan the APK's DEX at runtime
3. Each **Patch** defines fingerprints (string/opcode/class patterns) to locate target methods
4. Xposed hooks are applied to those methods
5. **piko extension classes** (Java, from `piko/extensions/`) contain the actual patch logic
   - `TimelineEntry.checkEntry()` filters ads from timeline
   - `FeatureSwitchPatch.flagInfo()` overrides feature flags
   - `DownloadPatch.download()` handles media downloads

## Adding a new patch

### 1. Create Fingerprints.kt
```kotlin
// app/src/main/java/io/github/pikoxposed/piko/twitter/myfeature/Fingerprints.kt
package io.github.pikoxposed.piko.twitter.myfeature

import io.github.pikoxposed.morphe.findMethodDirect
import io.github.pikoxposed.morphe.strings

val myMethodFingerprint = findMethodDirect {
    findMethod {
        matcher {
            strings("some_unique_string_from_target_apk")
            returnType = "void"
        }
    }.single()
}
```

### 2. Create the Patch
```kotlin
// app/src/main/java/io/github/pikoxposed/piko/twitter/myfeature/MyPatch.kt
package io.github.pikoxposed.piko.twitter.myfeature

import de.robv.android.xposed.XC_MethodReplacement
import io.github.pikoxposed.patch

val MyPatch = patch(name = "My patch") {
    ::myMethodFingerprint.hookMethod(XC_MethodReplacement.DO_NOTHING)
}
```

### 3. Register in TwitterHook.kt
```kotlin
val TwitterPatches = arrayOf(
    TwitterExtensionHook,
    // ... existing patches
    MyPatch,
)
```

## Getting DexKit AAR
Copy `dexkit-android.aar` and `dexkit-android-sources.jar` from:
https://github.com/NexAlloy/NexAlloy/tree/main/libs

## Testing fingerprints
Run unit tests against a real APK:
```bash
./gradlew test --tests "io.github.pikoxposed.FingerprintsKtTest"
```
(Requires placing the APK in `app/src/test/`)
