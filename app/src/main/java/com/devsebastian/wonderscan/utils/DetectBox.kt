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

import android.graphics.Bitmap
import android.util.Pair
import com.devsebastian.wonderscan.data.BoundingRect
import com.devsebastian.wonderscan.utils.Utils.getDeviceWidth
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.util.*

class DetectBox {

    companion object {
        private const val MAX_WIDTH = 500.0

        fun findCorners(
            image: Bitmap,
            angle: Int
        ): BoundingRect? {
            val sourceMat = bitmapToMat(image, angle)
            return detect(
                sourceMat,
                (getDeviceWidth() / sourceMat.width().toFloat()).toDouble()
            )
        }

        private fun bitmapToMat(bitmap: Bitmap, angle: Int): Mat {
            val mat = Mat()
            Utils.bitmapToMat(bitmap, mat)
            resizeMat(mat)
            com.devsebastian.wonderscan.utils.Utils.rotateMat(mat, angle)
            return mat
        }

        private fun resizeMat(mat: Mat) {
            Imgproc.resize(
                mat,
                mat,
                Size(MAX_WIDTH, mat.height() * MAX_WIDTH / mat.width().toFloat())
            )
        }

        private fun detect(sourceMat: Mat, ratio: Double): BoundingRect? {
            val mat = sourceMat.clone()
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY)
            Imgproc.GaussianBlur(mat, mat, Size(5.0, 5.0), 0.0)
            Imgproc.Canny(mat, mat, 75.0, 200.0)
            val points: MutableList<MatOfPoint> = ArrayList()
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
                        sourceMat.release()
                        return bRect
                    }
                }
            }
            mat.release()
            sourceMat.release()
            return null
        }
    }

}