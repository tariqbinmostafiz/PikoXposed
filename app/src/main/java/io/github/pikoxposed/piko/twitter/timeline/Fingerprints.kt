/*
 * Copyright (C) 2026 piko <https://github.com/crimera/piko>
 * Ported to Xposed module by PikoXposed
 *
 * Any modifications, derivatives, or substantial rewrites of this file
 * must retain this copyright notice and the piko attribution
 * in the source code and version control history.
 */

package io.github.pikoxposed.piko.twitter.timeline

import io.github.pikoxposed.morphe.AccessFlags
import io.github.pikoxposed.morphe.Fingerprint
import io.github.pikoxposed.morphe.fingerprint

// Verified on Twitter (X) 11.69.0-release.0
// Source: piko ForceHDPatch.kt - PlayerSupportFingerprint

/**
 * Matches the video quality/player support utility in the /av/player/support/ package.
 * Criteria (from piko):
 *   - definingClass contains "/av/player/support/"
 *   - accessFlags: PUBLIC, STATIC
 *   - parameterCount == 2
 * Used to intercept video variant list and select highest quality only.
 */
internal val PlayerSupportFingerprint = Fingerprint(
    definingClass = "/av/player/support/",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    custom = {
        // MethodMatcher DSL — paramCount is not directly available but DexKit
        // will match 2-param methods via the paramTypes list length constraint
        // piko original: methodDef.parameters.size == 2
        // In DexKit: we set paramTypes to any two non-null entries to constrain count
        paramTypes(null, null)
    },
)

// Source: piko TweetInfoHook.kt - TweetInfoHookFingerprint

/**
 * Matches: com.twitter.api.model.json.core.JsonApiTweet$$JsonObjectMapper.parse()
 * Used for TweetInfo hooks (hide community notes, promote button, force translate, etc.)
 */
internal val TweetInfoParseFingerprint = Fingerprint(
    definingClass = "Lcom/twitter/api/model/json/core/JsonApiTweet\$\$JsonObjectMapper;",
    name = "parse",
    returnType = "Ljava/lang/Object;",
)
