package com.example.pixman.helper

import android.graphics.Bitmap
import android.graphics.Matrix

/**
 * Created by SURYA N on 3/2/20.
 */

fun Bitmap?.flip(x: Float, y: Float, cx: Float, cy: Float): Bitmap? {
    if(this == null)
        return null
    val matrix = Matrix().apply { postScale(x, y, cx, cy) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}