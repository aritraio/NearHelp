package com.example.nearhelp.ui.assistant

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

internal data class AssistantAttachment(val name: String, val text: String = "", val jpeg: String? = null)

internal fun cameraAttachment(bitmap: Bitmap): AssistantAttachment {
    val output = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, output)
    return AssistantAttachment("Camera photo", jpeg = Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP))
}

internal suspend fun readAssistantAttachment(context: Context, uri: Uri): AssistantAttachment = withContext(Dispatchers.IO) {
    val resolver = context.contentResolver
    val name = resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) cursor.getString(0) else null
    } ?: "Document"
    val mime = resolver.getType(uri).orEmpty()
    val output = ByteArrayOutputStream()
    resolver.openInputStream(uri)?.use { input ->
        val buffer = ByteArray(8192)
        while (true) {
            val n = input.read(buffer); if (n < 0) break
            check(output.size() + n <= 10 * 1024 * 1024) { "Choose an attachment smaller than 10 MB." }
            output.write(buffer, 0, n)
        }
    } ?: error("Could not open the attachment.")
    val bytes = output.toByteArray()
    if (mime.startsWith("text/")) return@withContext AssistantAttachment(name, text = bytes.toString(Charsets.UTF_8).take(12000))
    if (mime == "application/pdf") {
        val temporary = File.createTempFile("assistant-page-", ".pdf", context.cacheDir)
        try {
            temporary.writeBytes(bytes)
            ParcelFileDescriptor.open(temporary, ParcelFileDescriptor.MODE_READ_ONLY).use { fd ->
                PdfRenderer(fd).use { pdf ->
                    check(pdf.pageCount > 0) { "This PDF has no pages." }
                    pdf.openPage(0).use { page ->
                        val width = 1024
                        val height = (page.height.toFloat() / page.width * width).toInt().coerceIn(1, 2048)
                        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                        try {
                            bitmap.eraseColor(Color.WHITE)
                            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            return@withContext cameraAttachment(bitmap).copy(name = "$name · page 1")
                        } finally { bitmap.recycle() }
                    }
                }
            }
        } finally { temporary.delete() }
    }
    check(mime.startsWith("image/")) { "Choose an image, PDF or text document." }
    val dimensions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, dimensions)
    val options = BitmapFactory.Options().apply {
        inSampleSize = 1
        while (dimensions.outWidth / inSampleSize > 1536 || dimensions.outHeight / inSampleSize > 1536) inSampleSize *= 2
    }
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options) ?: error("This image could not be read.")
    try { cameraAttachment(bitmap).copy(name = name) } finally { bitmap.recycle() }
}
