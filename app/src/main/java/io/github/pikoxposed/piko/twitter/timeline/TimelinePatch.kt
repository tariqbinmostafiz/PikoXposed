/*
 * Copyright (C) 2026 piko <https://github.com/crimera/piko>
 * Ported to Xposed module by PikoXposed
 *
 * Any modifications, derivatives, or substantial rewrites of this file
 * must retain this copyright notice and the piko attribution
 * in the source code and version control history.
 */

package io.github.pikoxposed.piko.twitter.timeline

import app.morphe.extension.twitter.patches.TimelineEntry
import app.morphe.extension.twitter.patches.TweetInfo
import de.robv.android.xposed.XC_MethodHook
import io.github.pikoxposed.patch

/**
 * Forces highest quality video playback.
 * Mirrors piko's ForceHDPatch (verified on Twitter 11.69.0-release.0):
 *   PlayerSupportFingerprint → static method in /av/player/support/ with 2 params
 *   → TimelineEntry.timelineVideos(List)List
 */
val ForceHD = patch(
    name = "Force HD video",
    description = "Always plays videos in highest available quality.",
) {
    ::PlayerSupportFingerprint.hookMethod(object : XC_MethodHook() {
        override fun afterHookedMethod(param: MethodHookParam) {
            @Suppress("UNCHECKED_CAST")
            val list = param.result as? java.util.List<Any> ?: return
            param.result = TimelineEntry.timelineVideos(list)
        }
    })
}

/**
 * Hooks tweet object parsing for TweetInfo feature patches.
 * Mirrors piko's TweetInfoHook (verified on Twitter 11.69.0-release.0):
 *   JsonApiTweet$$JsonObjectMapper.parse() → TweetInfo.checkEntry()
 */
val TweetInfoHook = patch(
    name = "Tweet info hook",
    description = "Enables hiding community notes, promote button, and other per-tweet modifications.",
) {
    ::TweetInfoParseFingerprint.hookMethod(object : XC_MethodHook() {
        override fun afterHookedMethod(param: MethodHookParam) {
            param.result = TweetInfo.checkEntry(param.result)
        }
    })
}
