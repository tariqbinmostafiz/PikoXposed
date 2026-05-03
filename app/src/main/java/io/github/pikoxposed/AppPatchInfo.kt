package io.github.pikoxposed

import io.github.pikoxposed.piko.instagram.InstagramPatches
import io.github.pikoxposed.piko.twitter.TwitterPatches

data class AppPatchInfo(
    val appName: String,
    val packageName: String,
    val patches: Array<Patch>,
)

/**
 * Package → patch list mapping.
 * Twitter verified: 11.69.0-release.0
 * Instagram verified: 423.0.0.47.66
 */
val appPatchConfigurations = listOf(
    AppPatchInfo(
        appName = "X (Twitter)",
        packageName = "com.twitter.android",
        patches = TwitterPatches,
    ),
    AppPatchInfo(
        appName = "Instagram",
        packageName = "com.instagram.android",
        patches = InstagramPatches,
    ),
    AppPatchInfo(
        appName = "Instagram Lite",
        packageName = "com.instagram.android.lite",
        patches = InstagramPatches,
    ),
)

val patchesByPackage: Map<String, Array<Patch>> =
    appPatchConfigurations.associate { it.packageName to it.patches }
