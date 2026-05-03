/*
 * Copyright (C) 2026 piko <https://github.com/crimera/piko>
 * Ported to Xposed module by PikoXposed
 *
 * Any modifications, derivatives, or substantial rewrites of this file
 * must retain this copyright notice and the piko attribution
 * in the source code and version control history.
 */

package io.github.pikoxposed.piko.instagram

import android.app.Activity
import app.morphe.extension.shared.Utils
import io.github.pikoxposed.addModuleAssets
import io.github.pikoxposed.injectHostClassLoaderToSelf
import io.github.pikoxposed.injectSelfClassLoaderToHost
import io.github.pikoxposed.patch
import io.github.pikoxposed.piko.instagram.ads.DisableAds
import io.github.pikoxposed.piko.instagram.ads.HideSuggestedContent
import io.github.pikoxposed.piko.instagram.links.SanitizeShareLinks
import org.luckypray.dexkit.wrap.DexMethod

// Instagram main activity entry point
private const val INSTAGRAM_MAIN_ACTIVITY =
    "com.instagram.mainactivity.MainActivity"

/**
 * Initializes piko extension classes for Instagram
 * by hooking MainActivity.onCreate.
 */
internal val InstagramExtensionHook = patch(name = "<InstagramExtensionHook>") {
    injectHostClassLoaderToSelf(this::class.java.classLoader!!, classLoader)
    injectSelfClassLoaderToHost(this::class.java.classLoader!!, classLoader)

    DexMethod("$INSTAGRAM_MAIN_ACTIVITY->onCreate(Landroid/os/Bundle;)V").hookMethod {
        before {
            val mainActivity = it.thisObject as Activity
            mainActivity.addModuleAssets()
            Utils.setContext(mainActivity)
        }
    }
}

/**
 * All Instagram patches verified on version 423.0.0.47.66
 */
val InstagramPatches = arrayOf(
    InstagramExtensionHook,
    DisableAds,            // Block SponsoredContentController.insertItem()
    HideSuggestedContent,  // Hide suggested stories/reels/channels/users from feed
    SanitizeShareLinks,    // Strip igshid/utm_* from all share URL types
)
