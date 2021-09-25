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
package com.wonderscan.android.fragment

import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.wonderscan.android.R
import com.wonderscan.android.utils.Utils

open class SettingsFragment : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener,
    Preference.OnPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences)

        findPreference<Preference?>(getString(R.string.key_share))?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                Utils.shareAppLink(requireContext())
                false
            }

        findPreference<Preference?>(getString(R.string.key_feedback))?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                sendFeedback()
                false
            }

        findPreference<Preference?>(getString(R.string.key_privacy_policy))?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://sites.google.com/view/bharatscanprivacypolicy/home")
                )
                startActivity(intent)
                false
            }

        findPreference<Preference?>(getString(R.string.key_rate_us))?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=com.devsebastian.wonderscan")
                )
                startActivity(intent)
                false
            }

        findPreference<Preference?>(getString(R.string.key_developer))?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://devsebastian.me/"))
                startActivity(intent)
                false
            }

        findPreference<Preference?>(getString(R.string.key_source_code_license))?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/devsebastian/WonderScan/blob/main/LICENSE.txt"))
                startActivity(intent)
                false
            }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {}
    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
        return false
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, s: String?) {}

    private fun sendFeedback() {
        requireContext().let {
            var body: String? = null
            try {
                val versionName = it.packageManager
                    .getPackageInfo(it.packageName, 0).versionName
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
                Toast.makeText(it, e.message, Toast.LENGTH_SHORT).show()
            }
            val intent = Intent(Intent.ACTION_SEND)
            intent.setType("message/rfc822")
                .putExtra(Intent.EXTRA_EMAIL, arrayOf<String?>("developer.devsebastian@gmail.com"))
                .putExtra(Intent.EXTRA_SUBJECT, "Feedback" + ": " + it.getString(R.string.app_name))
                .putExtra(Intent.EXTRA_TEXT, body)
            it.startActivity(
                Intent.createChooser(
                    intent,
                    it.getString(R.string.choose_email_client)
                )
            )
        }
    }
}