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

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.util.Pair
import androidx.activity.result.ActivityResultLauncher
import com.devsebastian.wonderscan.R
import com.devsebastian.wonderscan.activity.CropActivity
import com.devsebastian.wonderscan.dao.FrameDao
import com.devsebastian.wonderscan.data.BoundingRect
import com.devsebastian.wonderscan.data.Frame
import org.opencv.core.*
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

    fun saveMat(mat: Mat?, path: String?) {
        when (mat?.channels()) {
            2 -> {
                Imgproc.cvtColor(mat, mat, Imgproc.COLOR_GRAY2RGB)
            }
            3 -> {
                Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2RGB)
            }
        }
        Imgcodecs.imwrite(path, mat)
    }

    fun readMat(path: String?): Mat {
        val mat = Imgcodecs.imread(path)
        if (!mat.empty()) Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2BGR)
        return mat
    }

    fun sendCreateFileIntent(
        name: String,
        type: String?,
        resultLauncher: ActivityResultLauncher<Intent>
    ) {
        Intent(Intent.ACTION_CREATE_DOCUMENT).let {
            it.addCategory(Intent.CATEGORY_OPENABLE)
            it.type = type
            it.putExtra(Intent.EXTRA_TITLE, name)
            resultLauncher.launch(it)
        }
    }

    fun cropAndFormat(frame: Frame, application: Application, frameDao: FrameDao) {
        val originalMat = Imgcodecs.imread(frame.uri)
        val ratio = getDeviceWidth() / originalMat.width().toDouble()
        val bRect = findCorners(originalMat, ratio)
        val croppedMat: Mat
        if (bRect != null) {
            croppedMat = CropActivity.getPerspectiveTransform(originalMat, bRect, ratio)
        } else {
            croppedMat = Mat()
            originalMat.copyTo(croppedMat)
        }
        val croppedPath = createPhotoFile(application).absolutePath
        Imgcodecs.imwrite(croppedPath, croppedMat)
        val editedMat = Filter.auto(croppedMat)
        val editedPath = createPhotoFile(application).absolutePath
        Imgcodecs.imwrite(editedPath, editedMat)

        frame.croppedUri = croppedPath
        frame.editedUri = editedPath
        frameDao.update(frame)

        originalMat.release()
        croppedMat.release()
        editedMat.release()
    }

    private fun findCorners(sourceMat: Mat, ratio: Double): BoundingRect? {
        val mat = sourceMat.clone()
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY)
        Imgproc.GaussianBlur(mat, mat, Size(5.0, 5.0), 0.0)
        Imgproc.Canny(mat, mat, 75.0, 200.0)
        val points: MutableList<MatOfPoint?> = ArrayList()
        Imgproc.findContours(mat, points, Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE)
        val areas: MutableList<Pair<MatOfPoint, Double>> = ArrayList()
        for (point in points) {
            areas.add(Pair(point, Imgproc.contourArea(point)))
        }
        areas.sortWith { t1: Pair<MatOfPoint, Double>, t2: Pair<MatOfPoint, Double> ->
            java.lang.Double.compare(
                t2.second,
                t1.second
            )
        }
        val maxArea = (mat.width() * (mat.height() / 8f)).toDouble()
        if (areas.size == 0 || areas[0].second < maxArea) {
            return null
        }
        for (area in areas) {
            val matOfPoint2f = MatOfPoint2f(*area.first.toArray())
            Imgproc.approxPolyDP(
                matOfPoint2f,
                matOfPoint2f,
                0.02 * Imgproc.arcLength(matOfPoint2f, true),
                true
            )
            if (matOfPoint2f.height() == 4) {
                if (area.second > maxArea) {
                    val bRect = BoundingRect()
                    bRect.fromPoints(matOfPoint2f.toList(), ratio, ratio)
                    mat.release()
                    return bRect
                }
            }
        }
        mat.release()
        return null
    }
}