package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import java.io.InputStream

object BitmapUtils {
    
    fun loadFromUri(context: Context, uri: Uri, targetSize: Int = 800): Bitmap? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            // Calculate ideal sample size
            var inSampleSize = 1
            if (options.outHeight > targetSize || options.outWidth > targetSize) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while (halfHeight / inSampleSize >= targetSize && halfWidth / inSampleSize >= targetSize) {
                    inSampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            
            val finalStream = context.contentResolver.openInputStream(uri)
            val decoded = BitmapFactory.decodeStream(finalStream, null, decodeOptions)
            finalStream?.close()
            
            decoded?.let {
                // Keep orientation corrections in mind, but standard cropping is essential
                val squared = makeSquare(it)
                if (squared.width != targetSize) {
                    Bitmap.createScaledBitmap(squared, targetSize, targetSize, true)
                } else {
                    squared
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun makeSquare(bitmap: Bitmap): Bitmap {
        val size = minOf(bitmap.width, bitmap.height)
        val x = (bitmap.width - size) / 2
        val y = (bitmap.height - size) / 2
        return Bitmap.createBitmap(bitmap, x, y, size, size)
    }
}
