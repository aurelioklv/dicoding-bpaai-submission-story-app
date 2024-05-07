package com.aurelioklv.dicodingstoryapp.presentation.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import com.aurelioklv.dicodingstoryapp.BuildConfig
import com.aurelioklv.dicodingstoryapp.R
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

private const val FILENAME_FORMAT = "yyyyMMdd_HHmmss"
private val timestamp: String = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(Date())
private const val MAXIMAL_SIZE = 1000_000
private const val TIMESTAMP_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
private const val WPM = 240

fun getTimeMillisFromString(dateTimeString: String): Long {
    val sdf = SimpleDateFormat(TIMESTAMP_PATTERN)
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    val date = sdf.parse(dateTimeString) as Date
    return date.time
}

fun getTimeAgo(context: Context, timeMillis: Long): String {
    val now = System.currentTimeMillis()
    val diffMillis = now - timeMillis

    val seconds = TimeUnit.MILLISECONDS.toSeconds(diffMillis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis)
    val hours = TimeUnit.MILLISECONDS.toHours(diffMillis)
    val days = TimeUnit.MILLISECONDS.toDays(diffMillis)
    val weeks = days / 7
    val months = days / 30
    val years = days / 365

    return when {
        seconds < 60 -> context.getString(R.string.seconds_ago, seconds)
        minutes < 60 -> context.getString(R.string.minutes_ago, minutes)
        hours < 24 -> context.getString(R.string.hours_ago, hours)
        days < 7 -> context.getString(R.string.days_ago, days)
        weeks < 4 -> context.getString(R.string.weeks_ago, weeks)
        months < 12 -> context.getString(R.string.months_ago, months)
        else -> context.getString(R.string.years_ago, years)
    }
}

fun getReadingTimeMinute(text: String): Int {
    val words = text.split(Regex("\\s+")).size
    val minute = words / WPM
    return if (minute == 0) 1 else minute
}

fun getFrontName(name: String): String {
    return name.substringBefore(" ")
}

fun getImageUri(context: Context): Uri {
    var uri: Uri? = null
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "$timestamp.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/Dicoding Story/")
        }
        uri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
    }
    return uri ?: getImageUriForPreQ(context)
}

private fun getImageUriForPreQ(context: Context): Uri {
    val filesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val imageFile = File(filesDir, "/Dicoding Story/$timestamp.jpg")
    if (imageFile.parentFile?.exists() == false) {
        imageFile.parentFile?.mkdir()
    }

    return FileProvider.getUriForFile(
        context,
        "${BuildConfig.APPLICATION_ID}.fileprovider",
        imageFile
    )
}

fun createCustomTempFile(context: Context): File {
    val filesDir = context.externalCacheDir
    return File.createTempFile(timestamp, ".jpg", filesDir)
}


fun uriToFile(context: Context, imageUri: Uri): File {
    val myFile = createCustomTempFile(context)
    val inputStream = context.contentResolver.openInputStream(imageUri) as InputStream
    val fileOutputStream = FileOutputStream(myFile)
    val buffer = ByteArray(1024)
    var length: Int
    while (inputStream.read(buffer).also { length = it } > 0) fileOutputStream.write(
        buffer,
        0,
        length
    )
    fileOutputStream.close()
    inputStream.close()
    return myFile
}

fun File.reduceFileSize(): File {
    val file = this
    val bitmap = BitmapFactory.decodeFile(file.path).getRotatedBitmap(file)

    var compressQuality = 100
    var streamLength: Int

    do {
        val bmpStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
        val bmpPicByteArray = bmpStream.toByteArray()
        streamLength = bmpPicByteArray.size
        compressQuality -= 5
    } while (streamLength > MAXIMAL_SIZE)
    bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, FileOutputStream(file))

    return file
}

fun Bitmap.getRotatedBitmap(file: File): Bitmap {
    val orientation = ExifInterface(file).getAttributeInt(
        ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED
    )
    return when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(this, 90F)
        ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(this, 180F)
        ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(this, 270F)
        ExifInterface.ORIENTATION_NORMAL -> this
        else -> this
    }
}

fun rotateImage(source: Bitmap, angle: Float): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(angle)
    return Bitmap.createBitmap(
        source, 0, 0, source.width, source.height, matrix, true
    )
}

