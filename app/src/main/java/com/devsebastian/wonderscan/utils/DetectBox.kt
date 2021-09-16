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

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.util.Pair
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.devsebastian.wonderscan.data.BoundingRect
import com.devsebastian.wonderscan.utils.Utils.getDeviceWidth
import org.opencv.android.Utils
import org.opencv.core.*
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

        private fun yuvToRgba(imageProxy: ImageProxy, angle: Int): Mat {
            val rgbaMat = Mat()
            val planes = imageProxy.planes
            val height = imageProxy.height
            val width = imageProxy.width
            if (imageProxy.format == ImageFormat.YUV_420_888 && planes.size == 3) {
                val chromaPixelStride = planes[1].pixelStride
                if (chromaPixelStride == 2) { // Chroma channels are interleaved
                    val yPlane = planes[0].buffer
                    val uvPlane1 = planes[1].buffer
                    val uvPlane2 = planes[2].buffer
                    val yMat = Mat(height, width, CvType.CV_8UC1, yPlane)
                    val uvMat1 = Mat(height / 2, width / 2, CvType.CV_8UC2, uvPlane1)
                    val uvMat2 = Mat(height / 2, width / 2, CvType.CV_8UC2, uvPlane2)
                    val addrDiff = uvMat2.dataAddr() - uvMat1.dataAddr()
                    if (addrDiff > 0) {
                        Imgproc.cvtColorTwoPlane(yMat, uvMat1, rgbaMat, Imgproc.COLOR_YUV2RGBA_NV12)
                    } else {
                        Imgproc.cvtColorTwoPlane(yMat, uvMat2, rgbaMat, Imgproc.COLOR_YUV2RGBA_NV21)
                    }
                } else { // Chroma channels are not interleaved
                    val yuvBytes = ByteArray(width * (height + height / 2))
                    val yPlane = planes[0].buffer
                    val uPlane = planes[1].buffer
                    val vPlane = planes[2].buffer
                    yPlane[yuvBytes, 0, width * height]
                    val chromaRowStride = planes[1].rowStride
                    val chromaRowPadding = chromaRowStride - width / 2
                    var offset = width * height
                    if (chromaRowPadding == 0) {
                        // When the row stride of the chroma channels equals their width, we can copy
                        // the entire channels in one go
                        uPlane[yuvBytes, offset, width * height / 4]
                        offset += width * height / 4
                        vPlane[yuvBytes, offset, width * height / 4]
                    } else {
                        // When not equal, we need to copy the channels row by row
                        for (i in 0 until height / 2) {
                            uPlane[yuvBytes, offset, width / 2]
                            offset += width / 2
                            if (i < height / 2 - 1) {
                                uPlane.position(uPlane.position() + chromaRowPadding)
                            }
                        }
                        for (i in 0 until height / 2) {
                            vPlane[yuvBytes, offset, width / 2]
                            offset += width / 2
                            if (i < height / 2 - 1) {
                                vPlane.position(vPlane.position() + chromaRowPadding)
                            }
                        }
                    }
                    val yuvMat = Mat(height + height / 2, width, CvType.CV_8UC1)
                    yuvMat.put(0, 0, yuvBytes)
                    Imgproc.cvtColor(yuvMat, rgbaMat, Imgproc.COLOR_YUV2RGBA_I420, 4)
                }
            }
            val bmp =
                Bitmap.createBitmap(rgbaMat.width(), rgbaMat.height(), Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(rgbaMat, bmp)
            resizeMat(rgbaMat)
            com.devsebastian.wonderscan.utils.Utils.rotateMat(rgbaMat, angle)
            return rgbaMat
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