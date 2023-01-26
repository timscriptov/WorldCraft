package com.mcal.worldcraft.utils

import com.mcal.worldcraft.math.MathUtils

object RandomUtil {
    @JvmStatic
    fun getRandomInRangeInclusive(min: Int, max: Int): Int {
        return (Math.random() * (max - min + 1)).toInt() + min
    }

    @JvmStatic
    fun getRandomSignedInRangeInclusive(min: Int, max: Int): Int {
        return getRandomInRangeInclusive(min, max) * MathUtils.getRandomSign()
    }

    @JvmStatic
    fun getRandomInRangeExclusive(min: Float, max: Float): Float {
        return (Math.random() * (max - min)).toFloat() + min
    }

    @JvmStatic
    fun getRandomSignedInRangeExclusive(min: Float, max: Float): Float {
        return getRandomInRangeExclusive(min, max) * MathUtils.getRandomSign()
    }

    @JvmStatic
    fun getChance(value: Float): Boolean {
        return Math.random() <= value.toDouble()
    }
}