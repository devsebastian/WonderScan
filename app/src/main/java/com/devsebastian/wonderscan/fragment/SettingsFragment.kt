/*
 * Copyright (C) 2021 Dev Sebastian
 * This file is part of WonderScan <https://github.com/devsebastian/WonderScan>.
 *
 * WonderScan is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * WonderScan is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with WonderScan.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.devsebastian.wonderscan.fragment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.devsebastian.wonderscan.R

class SettingsFragment : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener,
    Preference.OnPreferenceChangeListener {
    private lateinit var preferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences)
        preferences = preferenceManager.sharedPreferences
        val privacyPolicyPreference =
            findPreference<Preference?>(getString(R.string.preference_privacy_policy))
        val rateUsPreference = findPreference<Preference?>(getString(R.string.preference_rate_us))
        val systemPreference =
            findPreference<SwitchPreference?>(getString(R.string.key_system_theme))
        val defaultThemePreference =
            findPreference<ListPreference?>(getString(R.string.key_default_theme))
        val developerPreference = findPreference<Preference?>(getString(R.string.key_developer))
        val feedbackPreference =
            findPreference<Preference?>(getString(R.string.preference_feedback))
        if (feedbackPreference != null) {
            feedbackPreference.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    sendFeedback(
                        requireContext(), "Feedback"
                    )
                    false
                }
        }
        if (privacyPolicyPreference != null) {
            privacyPolicyPreference.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://sites.google.com/view/bharatscanprivacypolicy/home")
                    )
                    startActivity(intent)
                    false
                }
        }
        if (rateUsPreference != null) {
            rateUsPreference.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=com.devsebastian.wonderscan")
                    )
                    startActivity(intent)
                    false
                }
        }
        if (developerPreference != null) {
            developerPreference.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://devsebastian.me/"))
                    startActivity(intent)
                    false
                }
        }
        if (defaultThemePreference != null) {
            defaultThemePreference.isEnabled =
                !preferences.getBoolean(getString(R.string.key_system_theme), false)
            defaultThemePreference.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any? ->
                    if (newValue == getString(R.string.dark_theme)) {
                        preferences.edit()
                            .putBoolean(getString(R.string.preference_mode_night), true).apply()
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    } else {
                        preferences.edit()
                            .putBoolean(getString(R.string.preference_mode_night), false).apply()
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    }
                    false
                }
        }
        if (systemPreference != null) {
            systemPreference.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { preference: Preference, newValue: Any ->
                    if (preference.key == getString(R.string.key_system_theme)) {
                        val useSystemTheme = newValue as Boolean
                        preferences.edit().putBoolean(
                            getString(R.string.preference_use_system_theme),
                            useSystemTheme
                        ).apply()
                        if (useSystemTheme) {
                            if (defaultThemePreference != null) defaultThemePreference.isEnabled =
                                false
                        } else {
                            if (defaultThemePreference != null) defaultThemePreference.isEnabled =
                                true
                            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                            } else {
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                            }
                        }
                    }
                    true
                }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {}
    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
        return false
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, s: String?) {}

    companion object {
        fun sendFeedback(context: Context, topic: String?) {
            var body: String? = null
            try {
                val versionName = context.packageManager
                    .getPackageInfo(context.packageName, 0).versionName
                body = """

-----------------------------
Please don't remove this information
 Device OS: Android 
 Device OS version: ${Build.VERSION.RELEASE}
 App Version: $versionName
 Device Brand: ${Build.BRAND}
 Device Model: ${Build.MODEL}
 Device Manufacturer: ${Build.MANUFACTURER}"""
            } catch (e: PackageManager.NameNotFoundException) {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
            val intent = Intent(Intent.ACTION_SEND)
            intent.setType("message/rfc822")
                .putExtra(Intent.EXTRA_EMAIL, arrayOf<String?>("developer.devsebastian@gmail.com"))
                .putExtra(Intent.EXTRA_SUBJECT, topic + ": " + context.getString(R.string.app_name))
                .putExtra(Intent.EXTRA_TEXT, body)
            context.startActivity(
                Intent.createChooser(
                    intent,
                    context.getString(R.string.choose_email_client)
                )
            )
        }
    }
}