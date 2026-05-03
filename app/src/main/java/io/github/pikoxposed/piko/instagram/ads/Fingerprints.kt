/*
 * Copyright (C) 2026 piko <https://github.com/crimera/piko>
 * Ported to Xposed module by PikoXposed
 *
 * Any modifications, derivatives, or substantial rewrites of this file
 * must retain this copyright notice and the piko attribution
 * in the source code and version control history.
 */

package io.github.pikoxposed.piko.instagram.ads

import io.github.pikoxposed.morphe.Fingerprint
import io.github.pikoxposed.morphe.fingerprint
import org.luckypray.dexkit.query.enums.StringMatchType

// Verified on Instagram 423.0.0.47.66
// Source: piko DisableAdsPatch.kt

/**
 * Matches the sponsored content insertion method via the unique string
 * "SponsoredContentController.insertItem".
 */
internal val DisableAdsFingerprint = Fingerprint(
    strings = listOf("SponsoredContentController.insertItem"),
)

// Source: piko HideSuggestedContentPatch.kt - FeedItemParseFromJsonFingerprint

/**
 * Matches the feed item JSON parser containing suggested content type strings,
 * with method name containing "parseFromJson".
 */
internal val FeedItemParseFromJsonFingerprint = fingerprint {
    strings(
        "suggested_businesses",
        "clips_netego",
        "stories_netego",
        "in_feed_survey",
        "bloks_netego",
        "suggested_igd_channels",
        "suggested_top_accounts",
        "suggested_users",
    )
    methodMatcher { name("parseFromJson", StringMatchType.Contains) }
}
