package com.dicoding.submission.storyapp.helper

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

private const val MAX_IMAGE_SIZE = 1_000_000

fun reduceFileImage(file: File): File {
    var bitmap = BitmapFactory.decodeFile(file.path)
    var streamLength: Int
    var compressQuality = 100
    val bmpStream = ByteArrayOutputStream()

    do {
        bmpStream.reset()
        bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
        val bmpPicByteArray = bmpStream.toByteArray()
        streamLength = bmpPicByteArray.size
        compressQuality -= 5
    } while (streamLength > MAX_IMAGE_SIZE)

    try {
        FileOutputStream(file).use { fos ->
            fos.write(bmpStream.toByteArray())
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }

    return file
}
