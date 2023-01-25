package com.mcal.worldcraft.activity

import com.mcal.droid.rugl.util.WorldUtils.WorldInfo

interface RemoveMapListener {
    fun removeWorld(position: Int, worldInfo: WorldInfo)
}