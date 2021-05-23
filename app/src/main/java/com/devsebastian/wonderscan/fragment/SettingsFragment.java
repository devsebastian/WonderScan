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

package com.devsebastian.wonderscan.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.devsebastian.wonderscan.R;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceChangeListener {

    SharedPreferences preferences;


    public static void sendFeedback(Context context, String topic) {
        String body = null;
        try {
            String versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            body = "\n\n-----------------------------\nPlease don't remove this information\n Device OS: Android \n Device OS version: " +
                    Build.VERSION.RELEASE + "\n App Version: " + versionName + "\n Device Brand: " + Build.BRAND +
                    "\n Device Model: " + Build.MODEL + "\n Device Manufacturer: " + Build.MANUFACTURER;
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822")
                .putExtra(Intent.EXTRA_EMAIL, new String[]{"developer.devsebastian@gmail.com"})
                .putExtra(Intent.EXTRA_SUBJECT, topic + ": " + context.getString(R.string.app_name))
                .putExtra(Intent.EXTRA_TEXT, body);
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.choose_email_client)));
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        preferences = getPreferenceManager().getSharedPreferences();

        Preference privacyPolicyPreference = findPreference(getString(R.string.preference_privacy_policy));
        Preference rateUsPreference = findPreference(getString(R.string.preference_rate_us));
        SwitchPreference systemPreference = findPreference(getString(R.string.key_system_theme));
        ListPreference defaultThemePreference = findPreference(getString(R.string.key_default_theme));
        Preference developerPreference = findPreference(getString(R.string.key_developer));
        Preference feedbackPreference = findPreference(getString(R.string.preference_feedback));

        if (feedbackPreference != null) {
            feedbackPreference.setOnPreferenceClickListener(preference -> {
                sendFeedback(getContext(), "Feedback");
                return false;
            });
        }


        if (privacyPolicyPreference != null) {
            privacyPolicyPreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://sites.google.com/view/bharatscanprivacypolicy/home"));
                startActivity(intent);
                return false;
            });
        }

        if (rateUsPreference != null) {
            rateUsPreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.devsebastian.wonderscan"));
                startActivity(intent);
                return false;
            });
        }

        if (developerPreference != null) {
            developerPreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://devsebastian.me/"));
                startActivity(intent);
                return false;
            });
        }


        if (defaultThemePreference != null) {
            defaultThemePreference.setEnabled(!preferences.getBoolean(getString(R.string.key_system_theme), false));
            defaultThemePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                if (newValue.equals(getString(R.string.dark_theme))) {
                    preferences.edit().putBoolean(getString(R.string.preference_mode_night), true).apply();
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    preferences.edit().putBoolean(getString(R.string.preference_mode_night), false).apply();
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }

                return false;
            });
        }

        if (systemPreference != null) {
            systemPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                if (preference.getKey().equals(getString(R.string.key_system_theme))) {
                    boolean useSystemTheme = (boolean) newValue;
                    preferences.edit().putBoolean(getString(R.string.preference_use_system_theme), useSystemTheme).apply();
                    if (useSystemTheme) {
                        if (defaultThemePreference != null)
                            defaultThemePreference.setEnabled(false);
                    } else {
                        if (defaultThemePreference != null)
                            defaultThemePreference.setEnabled(true);
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                        } else {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        }
                    }
                }
                return true;
            });
        }
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

    }
}