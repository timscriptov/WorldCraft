package com.solverlabs.worldcraft.activity;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.KeyEvent;

import androidx.appcompat.app.AppCompatActivity;


public class CommonActivity extends AppCompatActivity {
    private AudioManager audio;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        this.audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case 24:
                this.audio.adjustStreamVolume(3, 1, 1);
                return true;
            case 25:
                this.audio.adjustStreamVolume(3, -1, 1);
                return true;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }
}
