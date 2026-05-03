/*
 * Copyright (C) 2026 piko <https://github.com/crimera/piko>
 * Ported to Xposed module by PikoXposed
 *
 * Any modifications, derivatives, or substantial rewrites of this file
 * must retain this copyright notice and the piko attribution
 * in the source code and version control history.
 */

package io.github.pikoxposed.piko.twitter

import android.app.Activity
import app.morphe.extension.shared.Utils
import io.github.pikoxposed.addModuleAssets
import io.github.pikoxposed.injectHostClassLoaderToSelf
import io.github.pikoxposed.injectSelfClassLoaderToHost
import io.github.pikoxposed.patch
import io.github.pikoxposed.piko.twitter.ads.HideTwitterAds
import io.github.pikoxposed.piko.twitter.ads.ShowSensitiveMedia
import io.github.pikoxposed.piko.twitter.featureswitch.FeatureSwitch
import io.github.pikoxposed.piko.twitter.timeline.ForceHD
import io.github.pikoxposed.piko.twitter.timeline.TweetInfoHook
import io.github.pikoxposed.piko.twitter.tracking.ClearTrackingParams
import org.luckypray.dexkit.wrap.DexMethod

// Twitter (X) main activity — entry point for extension initialization
private const val TWITTER_MAIN_ACTIVITY =
    "com.twitter.app.main.MainActivity"

/**
 * Initializes piko extension classes (Utils.setContext, addModuleAssets)
 * by hooking MainActivity.onCreate — mirrors piko's extension initHook pattern.
 */
internal val TwitterExtensionHook = patch(name = "<TwitterExtensionHook>") {
    injectHostClassLoaderToSelf(this::class.java.classLoader!!, classLoader)
    injectSelfClassLoaderToHost(this::class.java.classLoader!!, classLoader)

    DexMethod("$TWITTER_MAIN_ACTIVITY->onCreate(Landroid/os/Bundle;)V").hookMethod {
        before {
            val mainActivity = it.thisObject as Activity
            mainActivity.addModuleAssets()
            Utils.setContext(mainActivity)
        }
    }
}

/**
 * All Twitter/X patches verified on version 11.69.0-release.0
 */
val TwitterPatches = arrayOf(
    TwitterExtensionHook,
    HideTwitterAds,        // Remove ads (timeline entries, module items, promoted trends)
    ShowSensitiveMedia,    // Bypass sensitive media warning overlay
    FeatureSwitch,         // Override feature flags (chirp font, FAB, Google ads, etc.)
    ForceHD,               // Force highest quality video playback
    TweetInfoHook,         // Hook tweet info (community notes, promote button, etc.)
    ClearTrackingParams,   // Strip sessionToken from shared links
)
