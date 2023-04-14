package com.mcal.droid.rugl.res

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.mcal.droid.rugl.Game
import com.mcal.droid.rugl.GameApp
import com.mcal.droid.rugl.texture.BitmapImage
import java.io.IOException

/**
 * Loads a [BitmapImage]
 */
abstract class BitmapLoader(
    private val imagePath: String,
) : ResourceLoader.Loader<BitmapImage?>() {
    override fun load() {
        try {
            resource = BitmapImage(getBitmapFromAsset(imagePath))
        } catch (e: IOException) {
            exception = e
            Log.e(Game.RUGL_TAG, "Problem loading bitmap $imagePath", e)
        }
    }

    override fun toString(): String {
        return "Image path = " + imagePath + resource?.let { " " + it.width + "x" + it.height }
    }

    companion object {
        /**
         * Retrieve a bitmap from assets.
         *
         * @param path The path to the asset.
         * @return The [Bitmap] or `null` if we failed to decode the file.
         */
        fun getBitmapFromAsset(path: String): Bitmap {
            return BitmapFactory.decodeStream(GameApp.getContext().assets.open(path))
        }
    }
}