package com.solverlabs.worldcraft.activity;

import com.solverlabs.droid.rugl.util.WorldUtils;

public interface RemoveMapListener {
    void removeWorld(int position, WorldUtils.WorldInfo worldInfo);
}
