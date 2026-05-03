/*
 * Copyright (C) 2026 piko <https://github.com/crimera/piko>
 * Ported to Xposed module by PikoXposed
 *
 * Any modifications, derivatives, or substantial rewrites of this file
 * must retain this copyright notice and the piko attribution
 * in the source code and version control history.
 */

package io.github.pikoxposed.piko.twitter.featureswitch

import app.morphe.extension.twitter.patches.FeatureSwitchPatch
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import io.github.pikoxposed.patch

/**
 * Hooks Twitter's internal feature flag resolution method to override flag values.
 *
 * How it works in piko (bytecode):
 *   1. Finds class via string "feature_switches_configs_crashlytics_enabled"
 *   2. Within that class, targets: boolean method(String flagName, boolean defaultValue)
 *   3. After MOVE_RESULT_OBJECT, injects FeatureSwitchPatch.flagInfo(String, Object)Object
 *
 * We replicate:
 *   - FeatureFlagClassFingerprint.run() finds any method in the target class
 *   - We then use declaredClass to get the class, find the boolean(String,boolean) method
 *   - Hook afterHookedMethod to call FeatureSwitchPatch.flagInfo()
 *
 * Extension class: app.morphe.extension.twitter.patches.FeatureSwitchPatch
 * Verified on Twitter (X) 11.69.0-release.0
 */
val FeatureSwitch = patch(
    name = "Feature flags",
    description = "Overrides Twitter feature flags: disables chirp font, hides FAB menu, Google ads, etc.",
) {
    // Get the declaring class of the matched method (the feature flag class)
    val featureFlagClass = FeatureFlagClassFingerprint.declaredClass

    // Find the boolean method(String, boolean) within that class
    // piko: booleanMethod = methods.first { it.returnType == "Z" && it.parameters == listOf("String;","Z") }
    val booleanMethod = featureFlagClass.declaredMethods.firstOrNull { method ->
        method.returnType == Boolean::class.javaPrimitiveType &&
        method.parameterTypes.size == 2 &&
        method.parameterTypes[0] == String::class.java &&
        method.parameterTypes[1] == Boolean::class.javaPrimitiveType
    } ?: run {
        android.util.Log.e("PikoXposed", "[FeatureSwitch] Could not find boolean(String,boolean) method in $featureFlagClass")
        return@patch
    }

    XposedBridge.hookMethod(booleanMethod, object : XC_MethodHook() {
        override fun afterHookedMethod(param: MethodHookParam) {
            val flagName = param.args[0] as? String ?: return
            // Mirrors: FeatureSwitchPatch;->flagInfo(String, Object)Object
            param.result = FeatureSwitchPatch.flagInfo(flagName, param.result)
        }
    })
}
