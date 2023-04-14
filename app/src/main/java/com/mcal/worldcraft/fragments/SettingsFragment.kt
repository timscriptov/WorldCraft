package com.mcal.worldcraft.fragments

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.*
import com.mcal.worldcraft.Persistence
import com.mcal.worldcraft.R
import com.mcal.worldcraft.SoundManager


class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {
    private val manager by lazy { preferenceManager }
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
        manager.apply {
            findPreference<EditTextPreference>("user_name")?.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _, newValue ->
                    if (newValue is String && newValue.isNotEmpty()) {
                        Persistence.getInstance().playerName = newValue
                    }
                    true
                }
            findPreference<ListPreference>("user_skin")?.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _, newValue ->
                    if (newValue is Short) {
                        Persistence.getInstance().playerSkin = newValue
                    }
                    true
                }
            findPreference<SwitchPreference>("invert_y")?.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _, newValue ->
                    if (newValue is Boolean) {
                        Persistence.getInstance().isInvertY = newValue
                    }
                    true
                }
            findPreference<EditTextPreference>("fog_distance")?.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _, newValue ->
                    if (newValue is Float) {
                        Persistence.getInstance().fogDistance = newValue
                    }
                    true
                }
            findPreference<SwitchPreference>("enable_sound")?.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _, newValue ->
                    if (newValue is Boolean) {
                        SoundManager.setSoundEnabled(newValue)
                    }
                    true
                }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
    }
}