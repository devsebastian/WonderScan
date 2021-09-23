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
import android.view.View.VISIBLE
import android.widget.LinearLayout
import androidx.lifecycle.lifecycleScope
import com.devsebastian.wonderscan.R
import com.devsebastian.wonderscan.data.BoundingRect
import com.devsebastian.wonderscan.databinding.ActivityCropBinding
import com.devsebastian.wonderscan.utils.DetectBox
import com.devsebastian.wonderscan.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.opencv.android.Utils.bitmapToMat
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
    private var croppedUri: String? = null
    private var editedUri: String? = null
    private lateinit var binding: ActivityCropBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCropBinding.inflate(layoutInflater)
        setContentView(binding.root)

        croppedUri = intent.getStringExtra(getString(R.string.intent_cropped_path))
        editedUri = intent.getStringExtra(getString(R.string.intent_edited_path))
        val uri = intent.getStringExtra(getString(R.string.intent_source_path))
        val angle = intent.getIntExtra(getString(R.string.intent_angle), 0)
        val framePos = intent.getIntExtra(getString(R.string.intent_frame_position), 0)
        val bitmap = BitmapFactory.decodeFile(uri)
        val width = Utils.getDeviceWidth()
        val height = (bitmap.height * (width / bitmap.width.toFloat())).toInt()
        val viewHeight = bitmap.height * (width / bitmap.width.toFloat())
        val scaleFactor = width / viewHeight * 0.9f
        val params = LinearLayout.LayoutParams(width, height)
        ratio = width / bitmap.width.toDouble()

        binding.cvCrop.let {
            it.setImageBitmap(bitmap)
            it.animate()
                .rotation(angle.toFloat())
                .scaleX(scaleFactor)
                .scaleY(scaleFactor)
                .setDuration(500)
                .start()
            it.layoutParams = params
        }


        lifecycleScope.launch(Dispatchers.Default) {
            var boundingRect = DetectBox.findCorners(bitmap, 0)
            if (boundingRect == null) {
                val w = bitmap.width
                val h = bitmap.height
                val padding = w * 0.1
                boundingRect = BoundingRect().apply {
                    topLeft = Point(padding * ratio, padding * ratio)
                    topRight = Point((w - padding) * ratio, padding * ratio)
                    bottomLeft = Point(padding * ratio, (h - padding) * ratio)
                    bottomRight = Point((w - padding) * ratio, (h - padding) * ratio)
                }
            }
            binding.cvCrop.setBoundingRect(boundingRect)
        }

        binding.tvRetake.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        binding.tvConfirm.setOnClickListener {
            binding.progressFrame.visibility = VISIBLE
            lifecycleScope.launch(Dispatchers.Default) {
                editedUri?.let { File(it).delete() }
                croppedUri = croppedUri ?: Utils.createPhotoFile(this@CropActivity).absolutePath
                getPerspectiveTransform(
                    bitmap,
                    binding.cvCrop.getBoundingRect(),
                    ratio
                ).run {
                    Utils.rotateMat(this, angle)
                    Utils.saveMat(this, croppedUri)
                }
                bitmap.recycle()
                Intent().let {
                    it.putExtra(getString(R.string.intent_source_path), uri)
                    it.putExtra(getString(R.string.intent_cropped_path), croppedUri)
                    it.putExtra(getString(R.string.intent_frame_position), framePos)
                    it.putExtra(getString(R.string.intent_angle), angle)
                    setResult(RESULT_OK, it)
                }
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
            val tl = boundingRect.topLeft
            val tr = boundingRect.topRight
            val bl = boundingRect.bottomLeft
            val br = boundingRect.bottomRight
            val srcMat = Mat(4, 1, CvType.CV_32FC2).apply {
                put(
                    0, 0,
                    tl.x / ratio, tl.y / ratio,
                    bl.x / ratio, bl.y / ratio,
                    br.x / ratio, br.y / ratio,
                    tr.x / ratio, tr.y / ratio
                )
            }
            val dstMat = Mat(4, 1, CvType.CV_32FC2)
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

            val mat = Mat()
            bitmapToMat(bitmap, mat)
            return mat.clone().apply {
                val perspectiveTransform = Imgproc.getPerspectiveTransform(srcMat, dstMat)
                Imgproc.warpPerspective(mat, this, perspectiveTransform, Size(width, height))
            }
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
            return mat.clone().apply {
                val perspectiveTransform = Imgproc.getPerspectiveTransform(srcMat, dstMat)
                Imgproc.warpPerspective(mat, this, perspectiveTransform, Size(width, height))
            }
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