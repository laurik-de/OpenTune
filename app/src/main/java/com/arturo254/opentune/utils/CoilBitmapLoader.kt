package com.arturo254.opentune.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.media3.common.util.BitmapLoader
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ErrorResult
import coil.request.ImageRequest
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.guava.future
import timber.log.Timber
import java.util.concurrent.ExecutionException

class CoilBitmapLoader(
    private val context: Context,
    private val scope: CoroutineScope,
) : BitmapLoader {
    override fun supportsMimeType(mimeType: String): Boolean = mimeType.startsWith("image/")

    override fun decodeBitmap(data: ByteArray): ListenableFuture<Bitmap> =
        scope.future(Dispatchers.IO) {
            BitmapFactory.decodeByteArray(data, 0, data.size)
                ?: error("Could not decode image data")
        }

    override fun loadBitmap(uri: Uri): ListenableFuture<Bitmap> =
        scope.future(Dispatchers.IO) {
            val result =
                context.imageLoader.execute(
                    ImageRequest
                        .Builder(context)
                        .data(uri)
                        .allowHardware(false)
                        .build(),
                )
            if (result is ErrorResult) {
                Timber.tag("CoilBitmapLoader").e(result.throwable, "Failed to load bitmap from uri: $uri")
                throw ExecutionException(result.throwable)
            }
            try {
                result.drawable?.toBitmap() ?: throw ExecutionException(Exception("Drawable is null"))
            } catch (e: Exception) {
                Timber.tag("CoilBitmapLoader").e(e, "Error converting drawable to bitmap for uri: $uri")
                throw ExecutionException(e)
            }
        }
}
