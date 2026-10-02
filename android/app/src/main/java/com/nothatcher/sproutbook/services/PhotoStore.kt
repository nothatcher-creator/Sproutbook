package com.nothatcher.sproutbook.services

import android.content.Context
import android.graphics.*
import android.media.ExifInterface
import android.net.Uri
import com.nothatcher.sproutbook.core.BoundedIo
import java.io.File
import java.util.UUID
import kotlinx.coroutines.*

object PhotoStore {
    fun file(context: Context, name: String): File {
        require(name.matches(Regex("[A-Za-z0-9_-]+\\.jpg")))
        return File(File(context.filesDir, "photos"), name)
    }

    suspend fun copy(context: Context, uri: Uri): String =
        withContext(Dispatchers.IO) {
            val bytes =
                context.contentResolver.openInputStream(uri)?.use {
                    BoundedIo.read(it, 15 * 1024 * 1024)
                } ?: error("The photo could not be opened.")
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
            require(bounds.outWidth > 0 && bounds.outHeight > 0) { "Choose a supported image." }
            val options = BitmapFactory.Options().apply { inSampleSize = 1 }
            while (maxOf(bounds.outWidth, bounds.outHeight) / options.inSampleSize > 1600) options
                .inSampleSize *= 2
            val bitmap =
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
                    ?: error("The photo could not be decoded.")
            val orientation = runCatching {
                ExifInterface(bytes.inputStream()).getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)
            }.getOrDefault(1)
            val matrix =
                Matrix().apply {
                    when (orientation) {
                        2 -> postScale(-1f, 1f)
                        3 -> postRotate(180f)
                        4 -> postScale(1f, -1f)
                        5 -> {
                            postRotate(90f)
                            postScale(-1f, 1f)
                        }
                        6 -> postRotate(90f)
                        7 -> {
                            postRotate(270f)
                            postScale(-1f, 1f)
                        }
                        8 -> postRotate(270f)
                    }
                }
            val rotated =
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            val name = "${UUID.randomUUID()}.jpg"
            val target = file(context, name)
            target.parentFile!!.mkdirs()
            try {
                target.outputStream().use {
                    check(rotated.compress(Bitmap.CompressFormat.JPEG, 88, it)) {
                        "Photo could not be stored."
                    }
                }
            } finally {
                if (rotated !== bitmap) rotated.recycle()
                bitmap.recycle()
            }
            name
        }

    suspend fun load(context: Context, name: String): Bitmap? =
        withContext(Dispatchers.IO) {
            runCatching { BitmapFactory.decodeFile(file(context, name).path) }.getOrNull()
        }
}
