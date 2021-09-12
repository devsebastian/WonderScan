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
package com.devsebastian.wonderscan.activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.devsebastian.wonderscan.R
import com.devsebastian.wonderscan.data.BoundingRect
import com.devsebastian.wonderscan.utils.DetectBox
import com.devsebastian.wonderscan.utils.Utils
import com.devsebastian.wonderscan.view.CropView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.io.File
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

class CropActivity : BaseActivity() {
    private var ratio = 0.0
    private var croppedPath: String? = null
    private var editedPath: String? = null
    private var cropped = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crop)

        croppedPath = intent.getStringExtra(getString(R.string.intent_cropped_path))
        editedPath = intent.getStringExtra(getString(R.string.intent_edited_path))
        val path = intent.getStringExtra(getString(R.string.intent_source_path))
        val angle = intent.getIntExtra(getString(R.string.intent_angle), 0)
        val framePos = intent.getIntExtra(getString(R.string.intent_frame_position), 0)

        val cropView = findViewById<CropView>(R.id.cv_crop)
        val confirmBtn = findViewById<TextView>(R.id.tv_confirm)
        val retakeBtn = findViewById<TextView>(R.id.tv_retake)

        val deviceWidth = Utils.getDeviceWidth()

        val bitmap = BitmapFactory.decodeFile(path)
        cropView.setImageBitmap(bitmap)

        val viewHeight = bitmap.height * (deviceWidth / bitmap.width.toFloat())
        val scaleFactor = deviceWidth / viewHeight * 0.9f

        cropView.animate()
            .rotation(angle.toFloat())
            .scaleX(scaleFactor)
            .scaleY(scaleFactor)
            .setDuration(500)
            .start()

        val params = LinearLayout.LayoutParams(
            deviceWidth, (bitmap.height * (deviceWidth / bitmap.width.toFloat())).toInt()
        )
        cropView.layoutParams = params
        ratio = deviceWidth / bitmap.width.toDouble()

        lifecycleScope.launch(Dispatchers.Default) {
            var boundingRect = DetectBox.findCorners(bitmap, 0)
            if (boundingRect == null) {
                val width = bitmap.width
                val height = bitmap.height
                val padding = width * 0.1
                boundingRect = BoundingRect()
                boundingRect.topLeft = Point(padding * ratio, padding * ratio)
                boundingRect.topRight = Point((width - padding) * ratio, padding * ratio)
                boundingRect.bottomLeft = Point(padding * ratio, (height - padding) * ratio)
                boundingRect.bottomRight =
                    Point((width - padding) * ratio, (height - padding) * ratio)
            }
            cropView.setBoundingRect(boundingRect)
        }

        retakeBtn.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        confirmBtn.setOnClickListener {
            findViewById<View>(R.id.spinkit_frame).visibility = View.VISIBLE
            lifecycleScope.launch(Dispatchers.Default) {
                val mat = getPerspectiveTransform(bitmap, cropView.getBoundingRect(), ratio)
                Utils.rotateMat(mat, angle)
                if (croppedPath == null) croppedPath =
                    Utils.createPhotoFile(this@CropActivity).absolutePath
                if (editedPath != null) File(editedPath!!).delete()
                Utils.saveMat(mat, croppedPath)
                bitmap.recycle()
                val i = Intent()
                i.putExtra(getString(R.string.intent_source_path), path)
                i.putExtra(getString(R.string.intent_cropped_path), croppedPath)
                i.putExtra(getString(R.string.intent_frame_position), framePos)
                i.putExtra(getString(R.string.intent_angle), angle)
                setResult(RESULT_OK, i)
                finish()
            }
        }
    }

    companion object {
        fun getPerspectiveTransform(
            bitmap: Bitmap,
            boundingRect: BoundingRect,
            ratio: Double
        ): Mat {
            val mat = Mat()
            org.opencv.android.Utils.bitmapToMat(bitmap, mat)
            val srcMat = Mat(4, 1, CvType.CV_32FC2)
            val dstMat = Mat(4, 1, CvType.CV_32FC2)
            val tl = boundingRect.topLeft
            val tr = boundingRect.topRight
            val bl = boundingRect.bottomLeft
            val br = boundingRect.bottomRight
            srcMat.put(
                0, 0,
                tl.x / ratio, tl.y / ratio,
                bl.x / ratio, bl.y / ratio,
                br.x / ratio, br.y / ratio,
                tr.x / ratio, tr.y / ratio
            )
            val hwRatio = getHWRatio(tl, tr, bl, br, bitmap.width, bitmap.height, ratio)
            val height: Double
            val width: Double
            if (hwRatio != Double.POSITIVE_INFINITY) {
                val widthA = sqrt((br.x - bl.x).pow(2.0) + (br.y - bl.y).pow(2.0))
                val widthB = sqrt((tr.x - tl.x).pow(2.0) + (tr.y - tl.y).pow(2.0))
                width = max(widthA, widthB)
                height = width / hwRatio
            } else {
                height = bl.y - tl.y
                width = tr.x - tl.x
            }
            dstMat.put(
                0, 0,
                0.0, 0.0,
                0.0, height,
                width, height,
                width, 0.0
            )
            val perspectiveTransform = Imgproc.getPerspectiveTransform(srcMat, dstMat)
            val dst = mat.clone()
            Imgproc.warpPerspective(mat, dst, perspectiveTransform, Size(width, height))
            return dst
        }

        fun getPerspectiveTransform(
            sourceMat: Mat,
            boundingRect: BoundingRect,
            ratio: Double
        ): Mat {
            val mat = sourceMat.clone()
            val srcMat = Mat(4, 1, CvType.CV_32FC2)
            val dstMat = Mat(4, 1, CvType.CV_32FC2)
            val tl = boundingRect.topLeft
            val tr = boundingRect.topRight
            val bl = boundingRect.bottomLeft
            val br = boundingRect.bottomRight
            srcMat.put(
                0, 0,
                tl.x / ratio, tl.y / ratio,
                bl.x / ratio, bl.y / ratio,
                br.x / ratio, br.y / ratio,
                tr.x / ratio, tr.y / ratio
            )
            val hwRatio = getHWRatio(tl, tr, bl, br, mat.width(), mat.height(), ratio)
            val widthA = sqrt((br.x - bl.x).pow(2.0) + (br.y - bl.y).pow(2.0))
            val widthB = sqrt((tr.x - tl.x).pow(2.0) + (tr.y - tl.y).pow(2.0))
            val width = max(widthA, widthB)
            val height = width / hwRatio
            dstMat.put(
                0, 0,
                0.0, 0.0,
                0.0, height,
                width, height,
                widthA, 0.0
            )
            val perspectiveTransform = Imgproc.getPerspectiveTransform(srcMat, dstMat)
            val dst = mat.clone()
            Imgproc.warpPerspective(mat, dst, perspectiveTransform, Size(width, height))
            return dst
        }

        private fun sqr(u: Double): Double {
            return u * u
        }

        private fun getHWRatio(
            TL: Point,
            TR: Point,
            BL: Point,
            BR: Point,
            width: Int,
            height: Int,
            ratio: Double
        ): Double {
            var m1x = TL.x / ratio
            var m1y = TL.y / ratio
            var m2x = TR.x / ratio
            var m2y = TR.y / ratio
            var m3x = BL.x / ratio
            var m3y = BL.y / ratio
            var m4x = BR.x / ratio
            var m4y = BR.y / ratio
            val u0 = (width / 2f).toDouble()
            val v0 = (height / 2f).toDouble()

            m1x -= u0
            m1y -= v0
            m2x -= u0
            m2y -= v0
            m3x -= u0
            m3y -= v0
            m4x -= u0
            m4y -= v0


            val k2 = ((m1y - m4y) * m3x - (m1x - m4x) * m3y + m1x * m4y - m1y * m4x) /
                    ((m2y - m4y) * m3x - (m2x - m4x) * m3y + m2x * m4y - m2y * m4x)
            val k3 = ((m1y - m4y) * m2x - (m1x - m4x) * m2y + m1x * m4y - m1y * m4x) /
                    ((m3y - m4y) * m2x - (m3x - m4x) * m2y + m3x * m4y - m3y * m4x)

            val fSquared =
                -((k3 * m3y - m1y) * (k2 * m2y - m1y) + (k3 * m3x - m1x) * (k2 * m2x - m1x)) /
                        ((k3 - 1) * (k2 - 1))

            var hwRatio = sqrt(
                (sqr(k2 - 1) + sqr(k2 * m2y - m1y) / fSquared + sqr(k2 * m2x - m1x) / fSquared) /
                        (sqr(k3 - 1) + sqr(k3 * m3y - m1y) / fSquared + sqr(k3 * m3x - m1x) / fSquared)
            )

            if (k2 == 1.0 && k3 == 1.0) hwRatio = sqrt(
                (sqr(m2y - m1y) + sqr(m2x - m1x)) /
                        (sqr(m3y - m1y) + sqr(m3x - m1x))
            )

            return hwRatio
        }
    }
}