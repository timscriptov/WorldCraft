package com.mcal.worldcraft.fragments

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.mcal.worldcraft.R
import com.mcal.worldcraft.SoundManager


class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey);
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            "user_name" -> {
//                Persistence.getInstance().playerName = "Steve"
            }
            "user_skin" -> {
//                Persistence.getInstance().playerSkin = 0
            }
            "invert_y" -> {
//                Persistence.getInstance().isInvertY = false
            }
            "fog_distance" -> {
//                Persistence.getInstance().fogDistance = 30.0f
            }
            "enable_sound" -> {
                SoundManager.setSoundEnabled(true)
            }
        }
    }
}