package com.mcal.worldcraft.utils

object GameTime {
    private var currentGameTime: Long = 0
    private var lastPlayedTime: Long = 0
    private var systemStartSessionTime: Long = 0
    private var timeOffset: Long = 0

    @JvmStatic
    val time: Long
        get() {
            currentGameTime =
                System.currentTimeMillis() - systemStartSessionTime + lastPlayedTime + timeOffset
            return currentGameTime
        }

    @JvmStatic
    fun initTime(lastPlayedTime: Long) {
        this.lastPlayedTime = lastPlayedTime
        systemStartSessionTime = System.currentTimeMillis()
    }

    @JvmStatic
    fun incTime(time: Long) {
        timeOffset += time
    }
}