# PikoXposed

An **Xposed module** for **X (Twitter)** and **Instagram**, powered by [piko](https://github.com/crimera/piko) patches.

Inspired by [NexAlloy](https://github.com/NexAlloy/NexAlloy), which ports morphe patches as an Xposed module.
This project does the same for piko's Twitter and Instagram patches.

## Features

### X (Twitter / `com.twitter.android`)
- **Hide ads** — removes promoted posts, trends, RTB ads, superhero cards
- **Hide sensitive media warnings**
- **Feature flags** — overrides Twitter's internal feature switches (disable chirp font, hide FAB menu, hide Google ads, etc.)
- **Force HD video** — always plays the highest bitrate MP4 variant
- **Download media** — native download support for tweets/images/videos

### Instagram (`com.instagram.android`)
- **Hide ads** — blocks the sponsored content injector
- **Sanitize share links** — strips `igshid` and `utm_*` tracking params from shared URLs

## Requirements
- Android 8.0+ (API 27+)
- LSPosed / EdXposed / Xposed Framework
- Target app installed (X or Instagram)

## Setup
1. Install PikoXposed APK
2. Enable module in LSPosed → select scope: **X (Twitter)** and/or **Instagram**
3. Force stop the target app
4. Open the app — patches apply at runtime

## Build

```bash
git clone --recurse-submodules https://github.com/YOUR_USERNAME/PikoXposed.git
cd PikoXposed
./gradlew assembleDebug
```

> **Note:** Requires the custom DexKit AAR in `libs/`. Copy `dexkit-android.aar` from [NexAlloy](https://github.com/NexAlloy/NexAlloy/tree/main/libs).

## Credits
- **[piko](https://github.com/crimera/piko)** by crimera — patch logic and extension classes (Apache-2.0)
- **[NexAlloy](https://github.com/NexAlloy/NexAlloy)** — Xposed framework architecture
- **[morphe](https://morphe.software)** — shared extension utilities

## License
Apache-2.0 (following piko's license)
