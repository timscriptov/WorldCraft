package com.mcal.worldcraft.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.mcal.worldcraft.databinding.ActivitySettingsBinding
import com.mcal.worldcraft.fragments.SettingsFragment


class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settingsBinding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(settingsBinding.root)
        setSupportActionBar(settingsBinding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (supportFragmentManager.findFragmentById(settingsBinding.frameContainer.id) == null) {
            supportFragmentManager
                .beginTransaction()
                .add(settingsBinding.frameContainer.id, SettingsFragment())
                .commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}