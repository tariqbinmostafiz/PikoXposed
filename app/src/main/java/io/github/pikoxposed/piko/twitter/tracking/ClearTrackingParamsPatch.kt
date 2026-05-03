/*
 * Copyright (C) 2026 piko <https://github.com/crimera/piko>
 * Ported to Xposed module by PikoXposed
 *
 * Any modifications, derivatives, or substantial rewrites of this file
 * must retain this copyright notice and the piko attribution
 * in the source code and version control history.
 */

package io.github.pikoxposed.piko.twitter.tracking

import de.robv.android.xposed.XC_MethodHook
import io.github.pikoxposed.patch

/**
 * Removes tracking session tokens when sharing Twitter links.
 * Mirrors piko's ClearTrackingParamsPatch (verified on Twitter 11.69.0-release.0):
 *   AddSessionTokenFingerprint → method(String, Object, String): String
 *   piko injects "return-object p0" at index 0 → returns first arg (URL) unchanged
 */
val ClearTrackingParams = patch(
    name = "Clear tracking params",
    description = "Removes sessionToken tracking parameters when sharing Twitter/X links.",
) {
    ::AddSessionTokenFingerprint.hookMethod(object : XC_MethodHook() {
        override fun beforeHookedMethod(param: MethodHookParam) {
            // Mirrors piko's "return-object p0" — return URL (first arg) unchanged
            param.result = param.args[0] as? String
        }
    })
}
