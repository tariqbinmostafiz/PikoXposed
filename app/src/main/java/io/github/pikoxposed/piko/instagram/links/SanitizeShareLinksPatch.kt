/*
 * Copyright (C) 2026 piko <https://github.com/crimera/piko>
 * Ported to Xposed module by PikoXposed
 *
 * Any modifications, derivatives, or substantial rewrites of this file
 * must retain this copyright notice and the piko attribution
 * in the source code and version control history.
 */

package io.github.pikoxposed.piko.instagram.links

import app.morphe.extension.instagram.patches.Links
import de.robv.android.xposed.XC_MethodHook
import io.github.pikoxposed.patch

/**
 * Strips tracking parameters from Instagram shared URLs.
 * Mirrors piko's SanitizeShareLinksPatch (verified on Instagram 423.0.0.47.66):
 *   Four fingerprints targeting parsefromjson / getter methods that return share URLs.
 *   Links.sanitizeUrl(String) strips igshid, utm_* params.
 */
val SanitizeShareLinks = patch(
    name = "Sanitize share links",
    description = "Removes igshid and utm_* tracking parameters from Instagram shared URLs.",
) {
    val urlSanitizerHook = object : XC_MethodHook() {
        override fun afterHookedMethod(param: MethodHookParam) {
            val url = param.result as? String ?: return
            param.result = Links.sanitizeUrl(url)
        }
    }

    // Permalink share URLs (XDTPermalinkResponse.parseFromJson)
    ::PermalinkResponseFingerprint.hookMethod(urlSanitizerHook)

    // Profile share URLs (profile_to_share_url parseFromJson)
    ::ProfileUrlResponseFingerprint.hookMethod(urlSanitizerHook)

    // Story share URLs (StoryItemUrlResponseImpl getter)
    ::StoryUrlResponseFingerprint.hookMethod(urlSanitizerHook)

    // Live stream share URLs (LiveItemLinkUrlResponseImpl getter)
    ::LiveUrlResponseFingerprint.hookMethod(urlSanitizerHook)
}
