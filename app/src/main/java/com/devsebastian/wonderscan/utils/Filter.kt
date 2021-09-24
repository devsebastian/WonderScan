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

import org.opencv.core.*
import org.opencv.imgproc.Imgproc

object Filter {
    private fun sharpen(mat: Mat): Mat {
        val blurred = Mat()
        mat.copyTo(blurred)
        val sigma = 6.0
        val threshold = 1.0
        val amount = 0.5
        Imgproc.GaussianBlur(mat, blurred, Size(0.0, 0.0), sigma, sigma)
        val lowContrastMask = Mat()
        mat.copyTo(lowContrastMask)
        Core.absdiff(mat, blurred, lowContrastMask)
        Core.compare(lowContrastMask, Scalar(threshold), lowContrastMask, Core.CMP_LT)
        val sharpened = Mat()
        mat.copyTo(sharpened)
        Core.addWeighted(mat, 1 + amount, blurred, -1 * amount, 0.0, sharpened)
        lowContrastMask.release()
        blurred.release()
        return sharpened
    }

    fun auto(sourceMat: Mat): Mat {
        val mat = Mat()
        sourceMat.copyTo(mat)
        val dilatedImg = Mat()
        Imgproc.dilate(mat, dilatedImg, Mat.ones(Size(7.0, 7.0), CvType.CV_8UC1))
        val bgImg = Mat()
        Imgproc.medianBlur(dilatedImg, bgImg, 21)
        val diffImg = Mat()
        Core.absdiff(bgImg, mat, diffImg)
        Core.subtract(
            MatOfDouble(255.0),
            diffImg,
            diffImg
        )
        val normImg = Mat()
        diffImg.copyTo(normImg)
        Core.normalize(diffImg, normImg, 0.0, 255.0, Core.NORM_MINMAX, CvType.CV_8UC1)
        Imgproc.threshold(normImg, normImg, 235.0, 0.0, Imgproc.THRESH_TRUNC)
        Core.normalize(normImg, normImg, 0.0, 255.0, Core.NORM_MINMAX, CvType.CV_8UC1)
        val resultMat = sharpen(normImg)
        dilatedImg.release()
        bgImg.release()
        diffImg.release()
        normImg.release()
        return resultMat
    }

    fun magicColor(croppedMat: Mat): Mat {
        val image = Mat()
        croppedMat.copyTo(image)

        // Divide the image by its morphologically closed counterpart
        val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, Size(19.0, 19.0))
        val closed = Mat()
        Imgproc.morphologyEx(image, closed, Imgproc.MORPH_CLOSE, kernel)
        image.convertTo(image, CvType.CV_32F) // divide requires floating-point
        Core.divide(image, closed, image, 1.0, CvType.CV_32F)
        Core.normalize(image, image, 0.0, 255.0, Core.NORM_MINMAX)
        image.convertTo(image, CvType.CV_8UC1) // convert back to unsigned int
        Imgproc.threshold(image, image, 235.0, 0.0, Imgproc.THRESH_TRUNC)
        Core.normalize(image, image, 0.0, 255.0, Core.NORM_MINMAX, CvType.CV_8UC1)
        closed.release()
        kernel.release()
        return sharpen(image)
    }

    fun magicColor2(croppedMat: Mat): Mat {
        val blurred = Mat()
        croppedMat.copyTo(blurred)
        val sigma = 6.0
        val threshold = 1.0
        val amount = 0.5
        Imgproc.GaussianBlur(croppedMat, blurred, Size(0.0, 0.0), sigma, sigma)
        val lowContrastMask = Mat()
        croppedMat.copyTo(lowContrastMask)
        Core.absdiff(croppedMat, blurred, lowContrastMask)
        Core.compare(lowContrastMask, Scalar(threshold), lowContrastMask, Core.CMP_LT)
        val sharpened = Mat()
        croppedMat.copyTo(sharpened)
        Core.addWeighted(croppedMat, 1 + amount, blurred, -1 * amount, 0.0, sharpened)
        sharpened.convertTo(sharpened, -1, 1.4, 0.0)
        blurred.release()
        lowContrastMask.release()
        return sharpened
    }

    fun grayscale(croppedMat: Mat): Mat {
        val mat = Mat()
        croppedMat.copyTo(mat)
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY)
        return mat
    }

    fun thresholdOTSU(croppedMat: Mat): Mat {
        val mat = Mat()
        croppedMat.copyTo(mat)
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY)
        Imgproc.threshold(mat, mat, 0.0, 255.0, Imgproc.THRESH_OTSU)
        return mat
    }
}