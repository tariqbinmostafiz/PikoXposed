/*
 * Copyright (C) 2026 piko <https://github.com/crimera/piko>
 * Ported to Xposed module by PikoXposed
 *
 * Any modifications, derivatives, or substantial rewrites of this file
 * must retain this copyright notice and the piko attribution
 * in the source code and version control history.
 */

package io.github.pikoxposed.piko.twitter.tracking

import io.github.pikoxposed.morphe.Fingerprint

// Verified on Twitter (X) 11.69.0-release.0
// Source: piko ClearTrackingParamsPatch.kt - AddSessionTokenFingerprint

/**
 * Matches the method that appends session tokens to shared URLs.
 * Criteria (from piko):
 *   - parameters: [String, L (object), String]
 *   - returnType: String
 *   - strings: ["<this>", "shareParam", "sessionToken"]
 *
 * piko replaces this method body with "return-object p0" (return the URL unchanged).
 * We do the same via XC_MethodReplacement returning the first argument directly.
 */
internal val AddSessionTokenFingerprint = Fingerprint(
    parameters = listOf("Ljava/lang/String;", "L", "Ljava/lang/String;"),
    returnType = "Ljava/lang/String;",
    strings = listOf("<this>", "shareParam", "sessionToken"),
)
