package com.mcal.droid.rugl.texture

import android.graphics.Bitmap
import android.opengl.GLES10
import android.opengl.GLUtils
import com.mcal.droid.rugl.gl.GLUtil

/**
 * An image based on a [Bitmap]
 */
class BitmapImage(
    @JvmField
    val bitmap: Bitmap
) : Image(bitmap.width, bitmap.height, Format.RGBA) {
    override fun writeToTexture(x: Int, y: Int) {
        GLES10.glPixelStorei(GLES10.GL_UNPACK_ALIGNMENT, format.bytes)
        GLUtils.texSubImage2D(GLES10.GL_TEXTURE_2D, 0, x, y, bitmap)
        GLUtil.checkGLError()
    }
}