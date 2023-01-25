package com.mcal.worldcraft;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

import androidx.annotation.NonNull;

import com.mcal.worldcraft.material.Material;
import com.mcal.worldcraft.util.RandomUtil;

import java.util.HashMap;

public class SoundManager {
    private static final float HIT_BLOCK_SPEED = 0.65f;
    private static final float HIT_BLOCK_VOLUME = 0.2f;
    private static final float MAX_HEARABLE_DISTANCE = 25.0f;
    private static final float NORMAL_SPEED = 1.0f;
    private static final float NORMAL_VOLUME = 1.0f;
    private static final float STEP_BLOCK_SPEED = 1.0f;
    private static final float STEP_BLOCK_VOLUME = 0.1f;
    private static final HashMap<Material, int[]> materialSounds = new HashMap<>();
    private static AudioManager mAudioManager;
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;
    private static SoundPool mSoundPool;
    private static HashMap<Integer, Integer> mSoundPoolMap;
    private static boolean isSoundEnabled = true;

    private SoundManager() {
    }

    public static void initSounds(Context theContext) {
        mContext = theContext;
        mSoundPool = new SoundPool(30, 3, 0);
        mSoundPoolMap = new HashMap<>();
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        setSoundEnabled(Persistence.getInstance().isSoundEnabled());
    }

    public static void loadSounds() {
        load(Sounds.DOOR_OPEN);
        load(Sounds.DOOR_CLOSE);
        load(Sounds.CHEST_OPEN);
        load(Sounds.CHEST_CLOSE);
        load(Sounds.FUSE);
        load(Sounds.BURP);
        load(Sounds.ZOMBIE_HIT);
        load(Sounds.ZOMBIE_DEATH);
        load(Sounds.GLASS);
        load(Sounds.POP);
        loadMaterialSound(Material.COW, Sounds.COW);
        loadMaterialSound(Material.SHEEP, Sounds.SHEEP);
        loadMaterialSound(Material.PIG, Sounds.PIG);
        loadMaterialSound(Material.GRASS, Sounds.GRASS);
        loadMaterialSound(Material.GRAVEL, Sounds.GRAVEL);
        loadMaterialSound(Material.WOOD, Sounds.WOOD);
        loadMaterialSound(Material.STONE, Sounds.STONE);
        loadMaterialSound(Material.ZOMBIE, Sounds.ZOMBIE);
        loadMaterialSound(Material.EXPLOSIVE, Sounds.EXPLOSIVE);
        loadMaterialSound(Material.FOOD, Sounds.FOOD);
        loadMaterialSound(Material.HUMAN, Sounds.HUMAN_DAMAGE);
    }

    private static void loadMaterialSound(Material material, int sound) {
        loadMaterialSound(material, new int[]{sound});
    }

    private static void loadMaterialSound(Material material, int[] sounds) {
        load(sounds);
        materialSounds.put(material, sounds);
    }

    private static void load(@NonNull int[] sounds) {
        for (int i : sounds) {
            load(i);
        }
    }

    private static void load(int sound) {
        mSoundPoolMap.put(sound, mSoundPool.load(mContext, sound, 1));
    }

    public static void playSound(int sound) {
        playSound(sound, 1.0f, 1.0f);
    }

    public static void playSound(int sound, float volume, float speed) {
        if (isSoundEnabled()) {
            Integer soundID = null;
            try {
                soundID = mSoundPoolMap.get(sound);
                if (soundID != null && volume > 0.0f) {
                    volume = Math.min(volume, 1.0f);
                    mSoundPool.play(soundID, volume, volume, 1, 0, speed);
                }
            } catch (Exception e) {
                Log.w(SoundManager.class.getSimpleName(), "playSound(" + sound + ", " + volume + ", " + speed + ") failed (soundID = " + soundID + "): " + e.getMessage(), e);
            }
        }
    }

    public static void stopSound(int sound) {
        mSoundPool.stop(mSoundPoolMap.get(sound));
    }

    public static void stopAllSounds() {
        for (Integer num : mSoundPoolMap.values()) {
            int soundID = num;
            mSoundPool.stop(soundID);
        }
    }

    public static boolean isSoundEnabled() {
        return isSoundEnabled;
    }

    public static void setSoundEnabled(boolean isSoundEnabled2) {
        isSoundEnabled = isSoundEnabled2;
        Persistence.getInstance().setSoundEnabled(isSoundEnabled2);
    }

    public static void cleanup() {
        mSoundPool.release();
        mSoundPool = null;
        mSoundPoolMap.clear();
        mAudioManager.unloadSoundEffects();
    }

    public static void playHit(Material material, float distance) {
        playMaterialRandomSound(material, 0.2f, HIT_BLOCK_SPEED, distance);
    }

    public static void playStep(Material material, float distance) {
        playMaterialRandomSound(material, STEP_BLOCK_VOLUME, 1.0f, distance);
    }

    public static void playBroke(Material material, float distance) {
        playMaterialSound(material, distance);
    }

    public static void playAppeared(Material material, float distance) {
        playMaterialSound(material, distance);
    }

    public static void playMaterialSound(Material material, float distance) {
        playMaterialRandomSound(material, 1.0f, 1.0f, distance);
    }

    private static void playMaterialRandomSound(Material material, float volume, float speed, float distance) {
        int[] sounds = materialSounds.get(material);
        if (sounds != null) {
            playDistancedSound(getRandomSound(sounds), volume, speed, distance);
        }
    }

    private static int getRandomSound(@NonNull int[] sounds) {
        int randomIndex = RandomUtil.getRandomInRangeInclusive(0, sounds.length - 1);
        return sounds[randomIndex];
    }

    public static void playDistancedSound(int sound, float distance) {
        playDistancedSound(sound, 1.0f, 1.0f, distance);
    }

    public static void playDistancedSound(int[] sound, float distance) {
        playDistancedSound(getRandomSound(sound), 1.0f, 1.0f, distance);
    }

    private static void playDistancedSound(int sound, float volume, float speed, float distance) {
        playSound(sound, volume * ((MAX_HEARABLE_DISTANCE - distance) / MAX_HEARABLE_DISTANCE), speed);
    }

    public static void playDoorChangeType(boolean isOpen, float distance) {
        int sound = isOpen ? Sounds.DOOR_OPEN : Sounds.DOOR_CLOSE;
        playDistancedSound(sound, distance);
    }
}
