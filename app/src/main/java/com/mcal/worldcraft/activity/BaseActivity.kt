package com.mcal.worldcraft.activity

import android.app.Activity
import android.media.AudioManager
import android.os.Bundle
import android.view.KeyEvent

open class BaseActivity : Activity() {
    private lateinit var audio: AudioManager

    public override fun onCreate(savedInstanceState: Bundle?) {
        audio = getSystemService(AUDIO_SERVICE) as AudioManager
        super.onCreate(savedInstanceState)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return when (keyCode) {
            24 -> {
                audio.adjustStreamVolume(3, 1, 1)
                true
            }
            25 -> {
                audio.adjustStreamVolume(3, -1, 1)
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }
}