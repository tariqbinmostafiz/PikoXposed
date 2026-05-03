@file:Suppress("DEPRECATION") @file:SuppressLint("WorldReadableFiles")

package io.github.pikoxposed.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceCategory
import android.preference.PreferenceFragment
import android.preference.SwitchPreference
import android.text.format.DateUtils
import android.window.OnBackInvokedDispatcher
import app.morphe.extension.shared.Utils
import io.github.pikoxposed.AppPatchInfo
import io.github.pikoxposed.BuildConfig
import io.github.pikoxposed.R
import io.github.pikoxposed.appPatchConfigurations
import io.github.pikoxposed.common.UpdateChecker
import kotlin.system.exitProcess

class SettingsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT
            ) { onBackPressed() }
        }
        setContentView(R.layout.activity_settings)
        actionBar?.setDisplayShowHomeEnabled(true)
        if (savedInstanceState != null) return
        fragmentManager.beginTransaction().replace(R.id.settings_container, SettingsFragment()).commit()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        finishAndRemoveTask()
        exitProcess(0)
    }

    class SettingsFragment : PreferenceFragment() {
        fun AppPatchInfo.getPreference(): Preference {
            val preference = Preference(context)
            preference.title = appName
            preference.key = appName
            preference.intent = Intent(context, AppPatchSettingsActivity::class.java).apply {
                putExtra(AppPatchSettingsActivity.ARGUMENT_APP_NAME, appName)
            }
            return preference
        }

        @Deprecated("Deprecated in Java")
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            val rootScreen = preferenceManager.createPreferenceScreen(context)
            preferenceScreen = rootScreen

            Utils.setContext(context)

            // App info
            Preference(context).apply {
                title = "PikoXposed"
                summary = "Xposed module for X (Twitter) & Instagram — powered by piko patches"
                isEnabled = false
                rootScreen.addPreference(this)
            }

            Preference(context).apply {
                title = "Version"
                summary = "${BuildConfig.VERSION_NAME} (${BuildConfig.COMMIT_HASH}) • " +
                    DateUtils.getRelativeTimeSpanString(BuildConfig.COMMIT_DATE * 1000)
                isEnabled = false
                rootScreen.addPreference(this)
            }

            Preference(context).apply {
                title = "GitHub"
                summary = "github.com/crimera/piko — piko patch source"
                intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/crimera/piko"))
                rootScreen.addPreference(this)
            }

            Preference(context).apply {
                title = "Credits"
                summary = "Based on piko by crimera. Framework by NexAlloy/morphe."
                isEnabled = false
                rootScreen.addPreference(this)
            }


            Preference(context).apply {
                title = "Check for updates"
                setOnPreferenceClickListener {
                    UpdateChecker().apply {
                        setActivity(activity)
                        checkUpdate(silent = false)
                    }
                    true
                }
                rootScreen.addPreference(this)
            }
            UpdateChecker().apply {
                setActivity(activity)
                autoCheckUpdate()
            }

            SwitchPreference(context).apply {
                title = "Hide launcher icon"
                summary = "Hides PikoXposed from the app drawer"
                val aliasName = ComponentName(activity, SettingsActivity::class.java.name + "Alias")
                val packageManager = activity.packageManager
                isChecked = packageManager.getComponentEnabledSetting(aliasName) ==
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                setOnPreferenceChangeListener { _, newValue ->
                    val isShow = newValue as Boolean
                    val status = if (isShow) PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                                 else PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                    if (packageManager.getComponentEnabledSetting(aliasName) != status) {
                        packageManager.setComponentEnabledSetting(aliasName, status, PackageManager.DONT_KILL_APP)
                    }
                    true
                }
                rootScreen.addPreference(this)
            }

            val isModuleActivated: Boolean = try {
                context.getSharedPreferences("prefs", MODE_WORLD_READABLE)
                true
            } catch (_: SecurityException) {
                false
            }

            if (!isModuleActivated) {
                rootScreen.addPreference(Preference(context).apply {
                    summary = "Module not activated. Enable in LSPosed/EdXposed and reboot."
                    isEnabled = false
                })
                return
            }

            val patchSelectionCategory = PreferenceCategory(context).apply {
                title = "Supported apps"
                rootScreen.addPreference(this)
            }
            Preference(context).apply {
                summary = "Force stop the target app to apply changes."
                isEnabled = false
                patchSelectionCategory.addPreference(this)
            }

            for (appPatchInfo in appPatchConfigurations) {
                patchSelectionCategory.addPreference(appPatchInfo.getPreference())
            }
        }
    }
}
