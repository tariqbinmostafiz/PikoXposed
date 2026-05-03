/*
 * Copyright (C) 2026 piko <https://github.com/crimera/piko>
 * Ported to Xposed module by PikoXposed
 *
 * Any modifications, derivatives, or substantial rewrites of this file
 * must retain this copyright notice and the piko attribution
 * in the source code and version control history.
 */

package io.github.pikoxposed.piko.instagram.ads

import app.morphe.extension.instagram.patches.Block
import app.morphe.extension.instagram.utils.Pref
import de.robv.android.xposed.XC_MethodHook
import io.github.pikoxposed.patch

/**
 * Blocks Instagram's sponsored content injector.
 * Mirrors piko's DisableAdsPatch (verified on Instagram 423.0.0.47.66):
 *   DisableAdsFingerprint → "SponsoredContentController.insertItem" method
 *   piko injects at index 0: Pref.disableAds()Z → if true, return false
 */
val DisableAds = patch(
    name = "Disable ads",
    description = "Blocks Instagram's sponsored content injector.",
) {
    ::DisableAdsFingerprint.hookMethod(object : XC_MethodHook() {
        override fun beforeHookedMethod(param: MethodHookParam) {
            // Mirrors: if (Pref.disableAds()) return false
            if (Pref.disableAds()) {
                param.result = false
            }
        }
    })
}

/**
 * Hides suggested content (stories, reels, channels, users) from Instagram feed.
 * Mirrors piko's HideSuggestedContentPatch (verified on Instagram 423.0.0.47.66):
 *   FeedItemParseFromJsonFingerprint → parsefromjson method with suggested content strings
 *   Block.replaceJsonParserKey(String) → nullifies unwanted suggested type keys
 */
val HideSuggestedContent = patch(
    name = "Hide suggested content",
    description = "Hides suggested stories, reels, channels and users from Instagram feed.",
) {
    ::FeedItemParseFromJsonFingerprint.hookMethod(object : XC_MethodHook() {
        override fun afterHookedMethod(param: MethodHookParam) {
            val result = param.result as? String ?: return
            param.result = Block.replaceJsonParserKey(result)
        }
    })
}
