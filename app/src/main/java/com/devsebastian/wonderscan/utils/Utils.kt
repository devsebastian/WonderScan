/*
 * Copyright (C) 2021 Dev Sebastian
 * This file is part of WonderScan <https://github.com/devsebastian/WonderScan>.
 *
 * WonderScan is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * WonderScan is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with WonderScan.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.devsebastian.wonderscan.utils

import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import androidx.activity.result.ActivityResultLauncher
import com.devsebastian.wonderscan.R
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.io.File
import java.util.*

object Utils {

    private const val folderName: String = "WonderScan"

    fun getDeviceWidth(): Int {
        return Resources.getSystem().displayMetrics.widthPixels

    }

    fun shareAppLink(context: Context) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.setType("text/plain")
            .putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_message))
        context.startActivity(intent)
    }

    fun createPhotoFile(context: Context): File {
        val filename = System.currentTimeMillis().toString() + ".png"
        val folder = File(context.filesDir, folderName)
        if (!folder.exists()) folder.mkdir()
        return File(folder, filename)
    }



    fun rotateMat(mat: Mat, angle: Int) {
        when (angle) {
            90 -> Core.rotate(mat, mat, Core.ROTATE_90_CLOCKWISE)
            180 -> Core.rotate(mat, mat, Core.ROTATE_180)
            270, -90 -> Core.rotate(mat, mat, Core.ROTATE_90_COUNTERCLOCKWISE)
        }
    }

    fun getBitmapFromMat(mat: Mat): Bitmap? {
        val bitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(mat, bitmap)
        return bitmap
    }

    fun saveMat(mat: Mat?, path: String?) {
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2RGB)
        Imgcodecs.imwrite(path, mat)
    }

    fun readMat(path: String?): Mat {
        val mat = Imgcodecs.imread(path)
        if (!mat.empty()) Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2BGR)
        return mat
    }

    fun sendCreateFileIntent(activity: Activity, name: String, type: String?, code: Int) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = type
        intent.putExtra(Intent.EXTRA_TITLE, name)
        activity.startActivityForResult(intent, code)
    }

    fun sendCreateFileIntent(name: String, type: String?, resultLauncher: ActivityResultLauncher<Intent>) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = type
        intent.putExtra(Intent.EXTRA_TITLE, name)
        resultLauncher.launch(intent)
    }

    fun removeImageFromCache(path: String?) {
        path?.let {
            val file = File(it)
            if (file.exists()) file.delete()
        }
    }
}