/*
 * Copyright (C) 2026 piko <https://github.com/crimera/piko>
 * Ported to Xposed module by PikoXposed
 *
 * Any modifications, derivatives, or substantial rewrites of this file
 * must retain this copyright notice and the piko attribution
 * in the source code and version control history.
 */

package io.github.pikoxposed.piko.twitter.featureswitch

import io.github.pikoxposed.morphe.Fingerprint

// Verified on Twitter (X) 11.69.0-release.0
// Source: piko FeatureFlagPatch.kt - FeatureFlagFingerprint

/**
 * Matches the class containing feature flag evaluation methods via the unique string
 * "feature_switches_configs_crashlytics_enabled".
 * Within that class, we target the boolean method with signature:
 *   boolean method(String flagName, boolean defaultValue)
 * This is done at runtime inside FeatureSwitchPatch.kt after resolving the class.
 */
internal val FeatureFlagClassFingerprint = Fingerprint(
    strings = listOf("feature_switches_configs_crashlytics_enabled"),
)
