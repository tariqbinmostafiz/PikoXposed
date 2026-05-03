/*
 * Copyright (C) 2026 piko <https://github.com/crimera/piko>
 * Ported to Xposed module by PikoXposed
 *
 * Any modifications, derivatives, or substantial rewrites of this file
 * must retain this copyright notice and the piko attribution
 * in the source code and version control history.
 */

package io.github.pikoxposed.piko.instagram.links

import io.github.pikoxposed.morphe.Fingerprint
import io.github.pikoxposed.morphe.fingerprint
import org.luckypray.dexkit.query.enums.StringMatchType

// Verified on Instagram 423.0.0.47.66
// Source: piko SanitizeShareLinksPatch.kt

/**
 * Matches the XDTPermalinkResponse JSON parser (parseFromJson method).
 * Uses DexKit name matcher with Contains to match parsefromjson (case-insensitive not supported
 * directly, so we match lowercase "parsefromjson" as DexKit is case-sensitive —
 * Instagram uses "parseFromJson" so we target that exact casing).
 */
internal val PermalinkResponseFingerprint = fingerprint {
    strings("XDTPermalinkResponse")
    methodMatcher { name("parseFromJson", StringMatchType.Contains) }
}

/**
 * Matches the profile_to_share_url JSON parser (parseFromJson method).
 */
internal val ProfileUrlResponseFingerprint = fingerprint {
    strings("profile_to_share_url")
    methodMatcher { name("parseFromJson", StringMatchType.Contains) }
}

/**
 * Matches: com.instagram.request.StoryItemUrlResponseImpl → method returning String.
 * Used to strip tracking params from story shared URLs.
 */
internal val StoryUrlResponseFingerprint = Fingerprint(
    definingClass = "Lcom/instagram/request/StoryItemUrlResponseImpl;",
    returnType = "Ljava/lang/String;",
)

/**
 * Matches: com.instagram.request.LiveItemLinkUrlResponseImpl → method returning String.
 * Used to strip tracking params from live shared URLs.
 */
internal val LiveUrlResponseFingerprint = Fingerprint(
    definingClass = "Lcom/instagram/request/LiveItemLinkUrlResponseImpl;",
    returnType = "Ljava/lang/String;",
)
