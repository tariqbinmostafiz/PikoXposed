/*
 * Copyright (C) 2026 piko <https://github.com/crimera/piko>
 * Ported to Xposed module by PikoXposed
 *
 * Any modifications, derivatives, or substantial rewrites of this file
 * must retain this copyright notice and the piko attribution
 * in the source code and version control history.
 */

package io.github.pikoxposed.piko.twitter.ads

import app.morphe.extension.twitter.patches.TimelineEntry
import de.robv.android.xposed.XC_MethodHook
import io.github.pikoxposed.patch

/**
 * Removes promoted posts, promoted trends, and Google ad entries from the Twitter timeline.
 *
 * Mirrors piko's TimelineEntryHookPatch + HideAds (verified on Twitter 11.69.0-release.0):
 *   - JsonTimelineEntry$$JsonObjectMapper.parse() → TimelineEntry.checkEntry()
 *   - JsonTimelineModuleItem$$JsonObjectMapper.parse() → TimelineEntry.checkEntry()
 *   - JsonTimelineTrend.parse() → TimelineEntry.hidePromotedTrend()
 */
val HideTwitterAds = patch(
    name = "Hide Twitter/X ads",
    description = "Removes promoted posts, promoted trends and Google ads from timeline.",
) {
    ::TimelineEntryParseFingerprint.hookMethod(object : XC_MethodHook() {
        override fun afterHookedMethod(param: MethodHookParam) {
            param.result = TimelineEntry.checkEntry(param.result)
        }
    })

    ::TimelineModuleItemParseFingerprint.hookMethod(object : XC_MethodHook() {
        override fun afterHookedMethod(param: MethodHookParam) {
            param.result = TimelineEntry.checkEntry(param.result)
        }
    })

    ::PromotedTrendFingerprint.hookMethod(object : XC_MethodHook() {
        override fun afterHookedMethod(param: MethodHookParam) {
            if (TimelineEntry.hidePromotedTrend(param.result)) {
                param.result = null
            }
        }
    })
}

/**
 * Bypasses sensitive media warning overlay.
 * Mirrors piko's ShowSensitiveMediaPatch (verified on Twitter 11.69.0-release.0):
 *   - JsonSensitiveMediaWarning$$JsonObjectMapper.parse() → TimelineEntry.sensitiveMedia()
 */
val ShowSensitiveMedia = patch(
    name = "Show sensitive media",
    description = "Bypasses sensitive content warnings on Twitter media.",
) {
    ::SensitiveMediaWarningFingerprint.hookMethod(object : XC_MethodHook() {
        override fun afterHookedMethod(param: MethodHookParam) {
            TimelineEntry.sensitiveMedia(param.result)
        }
    })
}
