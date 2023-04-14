package com.mcal.worldcraft.activity

import android.media.AudioManager
import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {
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