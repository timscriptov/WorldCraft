package com.mcal.droid.rugl.res

import android.util.Log
import com.mcal.droid.rugl.Game
import com.mcal.droid.rugl.text.Font
import java.io.IOException

/**
 * Loads a font
 */
abstract class FontLoader
/**
 * @param resourceID
 * @param mipmap
 */(private val resourceID: Int, private val mipmap: Boolean) : ResourceLoader.Loader<Font?>() {
    override fun load() {
        try {
            resource = Font(ResourceLoader.resources.openRawResource(resourceID))
        } catch (e: IOException) {
            exception = e
            Log.e(Game.RUGL_TAG, "Problem loading font $resourceID", e)
        }
    }

    override fun complete() {
        resource?.let {
            it.init(mipmap)
            fontLoaded()
        }
    }

    /**
     * Called once the font has been constructed from the file and
     * loaded into opengl
     */
    abstract fun fontLoaded()
    override fun toString(): String {
        return "Font loader $resourceID mipmap = $mipmap"
    }
}