package com.solverlabs.worldcraft.activity

import com.solverlabs.droid.rugl.util.WorldUtils.WorldInfo

interface RemoveMapListener {
    fun removeWorld(position: Int, worldInfo: WorldInfo)
}