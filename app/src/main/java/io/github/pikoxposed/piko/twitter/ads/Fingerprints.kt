/*
 * Copyright (C) 2026 piko <https://github.com/crimera/piko>
 * Ported to Xposed module by PikoXposed
 *
 * Any modifications, derivatives, or substantial rewrites of this file
 * must retain this copyright notice and the piko attribution
 * in the source code and version control history.
 */

package io.github.pikoxposed.piko.twitter.ads

import io.github.pikoxposed.morphe.Fingerprint

// Verified on Twitter (X) 11.69.0-release.0
// Source: piko TimelineEntryHookPatch.kt

/**
 * Matches: com.twitter.model.json.timeline.urt.JsonTimelineEntry$$JsonObjectMapper.parse()
 * Used to intercept timeline entry parsing and filter promoted/ad entries.
 */
internal val TimelineEntryParseFingerprint = Fingerprint(
    definingClass = "Lcom/twitter/model/json/timeline/urt/JsonTimelineEntry\$\$JsonObjectMapper;",
    name = "parse",
    returnType = "Ljava/lang/Object;",
)

/**
 * Matches: com.twitter.model.json.timeline.urt.JsonTimelineModuleItem$$JsonObjectMapper.parse()
 * Used to filter module items (Who to follow, Today's news, etc.)
 */
internal val TimelineModuleItemParseFingerprint = Fingerprint(
    definingClass = "Lcom/twitter/model/json/timeline/urt/JsonTimelineModuleItem\$\$JsonObjectMapper;",
    name = "parse",
    returnType = "Ljava/lang/Object;",
)

/**
 * Matches: com.twitter.model.json.timeline.urt.JsonTimelineTrend
 * Used to hide promoted trends.
 * Source: piko HideAds.kt - HidePromotedTrendFingerprint
 */
internal val PromotedTrendFingerprint = Fingerprint(
    definingClass = "Lcom/twitter/model/json/timeline/urt/JsonTimelineTrend;",
    returnType = "Ljava/lang/Object;",
)

/**
 * Matches: com.twitter.model.json.core.JsonSensitiveMediaWarning$$JsonObjectMapper.parse()
 * Used to bypass sensitive media warnings.
 * Source: piko ShowSensitiveMediaPatch.kt - sensitiveMediaSettingsPatchFingerprint
 */
internal val SensitiveMediaWarningFingerprint = Fingerprint(
    definingClass = "Lcom/twitter/model/json/core/JsonSensitiveMediaWarning\$\$JsonObjectMapper;",
    name = "parse",
    returnType = "Ljava/lang/Object;",
)
