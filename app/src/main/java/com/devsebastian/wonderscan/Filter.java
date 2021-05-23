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

package com.devsebastian.wonderscan;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.Core.NORM_MINMAX;
import static org.opencv.core.Core.absdiff;
import static org.opencv.core.Core.merge;
import static org.opencv.core.Core.normalize;
import static org.opencv.core.Core.split;
import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.core.Mat.ones;
import static org.opencv.imgproc.Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C;
import static org.opencv.imgproc.Imgproc.ADAPTIVE_THRESH_MEAN_C;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2YCrCb;
import static org.opencv.imgproc.Imgproc.COLOR_YCrCb2BGR;
import static org.opencv.imgproc.Imgproc.GaussianBlur;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.THRESH_OTSU;
import static org.opencv.imgproc.Imgproc.THRESH_TRUNC;
import static org.opencv.imgproc.Imgproc.adaptiveThreshold;
import static org.opencv.imgproc.Imgproc.bilateralFilter;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.dilate;
import static org.opencv.imgproc.Imgproc.equalizeHist;
import static org.opencv.imgproc.Imgproc.getStructuringElement;
import static org.opencv.imgproc.Imgproc.medianBlur;
import static org.opencv.imgproc.Imgproc.threshold;
import static org.opencv.photo.Photo.fastNlMeansDenoising;

public class Filter {

    public static Mat sharpen(Mat mat) {
        Mat blurred = new Mat();
        mat.copyTo(blurred);
        double sigma = 6, threshold = 1, amount = 0.5;
        GaussianBlur(mat, blurred, new Size(0, 0), sigma, sigma);
        Mat lowContrastMask = new Mat();
        mat.copyTo(lowContrastMask);

        absdiff(mat, blurred, lowContrastMask);
        Core.compare(lowContrastMask, new Scalar(threshold), lowContrastMask, Core.CMP_LT);
        Mat sharpened = new Mat();
        mat.copyTo(sharpened);
        Core.addWeighted(mat, 1 + amount, blurred, -1 * amount, 0, sharpened);

        lowContrastMask.release();
        blurred.release();

        return sharpened;
    }

    public static Mat auto(Mat sourceMat) {
        Mat mat = new Mat();
        sourceMat.copyTo(mat);

        Mat dilated_img = new Mat();
        dilate(mat, dilated_img, ones(new Size(7, 7), CV_8UC1));

        Mat bg_img = new Mat();
        medianBlur(dilated_img, bg_img, 21);

        Mat diff_img = new Mat();
        absdiff(bg_img, mat, diff_img);
        Core.subtract(new MatOfDouble(255), diff_img, diff_img); // absdiff(diff_img, new Scalar(255), diff_img);

        Mat norm_img = new Mat();
        diff_img.copyTo(norm_img);
        normalize(diff_img, norm_img, 0, 255, NORM_MINMAX, CV_8UC1);
        threshold(norm_img, norm_img, 235, 0, THRESH_TRUNC);
        normalize(norm_img, norm_img, 0, 255, Core.NORM_MINMAX, CV_8UC1);

        Mat resultMat = sharpen(norm_img);

        dilated_img.release();
        bg_img.release();
        diff_img.release();
        norm_img.release();

        return resultMat;
    }


    public static Mat equalizeIntensity(Mat sourceMat) {
        Mat ycrcb = new Mat();
        cvtColor(sourceMat, ycrcb, COLOR_BGR2YCrCb);

        List<Mat> channels = new ArrayList<>();
        split(ycrcb, channels);

        equalizeHist(channels.get(0), channels.get(0));

        Mat result = new Mat();
        merge(channels, ycrcb);

        cvtColor(ycrcb, result, COLOR_YCrCb2BGR);
        return result;
    }

    public static Mat method(Mat sourceMat) {
        Mat mat = new Mat();
        bilateralFilter(sourceMat, mat, 15, 75, 75);
        return mat;
    }

    public static Mat method2(Mat sourceMat) {
        bilateralFilter(sourceMat, sourceMat, 15, 75, 75);
        List<Mat> rgb_planes = new ArrayList<>();
        split(sourceMat, rgb_planes);

        List<Mat> result_planes = new ArrayList<>();
        List<Mat> result_norm_planes = new ArrayList<>();
//        result_planes = []
//        result_norm_planes = []
        for (Mat plane : rgb_planes) {
            Mat dilated_img = new Mat();
            dilate(plane, dilated_img, ones(new Size(7, 7), CV_8UC1));
            Mat bg_img = new Mat();
            medianBlur(dilated_img, bg_img, 21);
            Mat diff_img = new Mat();
            absdiff(plane, bg_img, diff_img);
            absdiff(diff_img, new Scalar(255), diff_img);
//            invert(diff_img, diff_img);
//            threshold(diff_img, diff_img, 0, 255, THRESH_OTSU);
            Mat norm_img = new Mat();
            normalize(diff_img, norm_img, 0, 255, NORM_MINMAX, CV_8UC1);
            result_planes.add(diff_img);
            result_norm_planes.add(norm_img);
        }

        Mat result = new Mat();
        merge(result_planes, result);

        Mat result_norm = new Mat();
        merge(result_norm_planes, result_norm);

        return (result_norm);
    }

    public static Mat adaptiveMorph(Mat croppedMat) {
        final Mat image = new Mat();
        croppedMat.copyTo(image);

        // Divide the image by its morphologically closed counterpart
        Mat kernel = getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(19, 19));
        Mat closed = new Mat();
        Imgproc.morphologyEx(image, closed, Imgproc.MORPH_CLOSE, kernel);

        image.convertTo(image, CvType.CV_32F); // divide requires floating-point
        Core.divide(image, closed, image, 1, CvType.CV_32F);
        normalize(image, image, 0, 255, NORM_MINMAX);
        image.convertTo(image, CvType.CV_8UC1); // convert back to unsigned int

        threshold(image, image, 235, 0, THRESH_TRUNC);
        normalize(image, image, 0, 255, NORM_MINMAX, CV_8UC1);

        closed.release();
        kernel.release();
        return sharpen(image);
    }


    public static Mat magicColor(Mat croppedMat) {
        // sharpen image using "unsharp mask" algorithm
//        GaussianBlur(sourceMat, mat, new Size(0, 0), 3);
//        Core.addWeighted(sourceMat, 1.5, mat, -0.5, 0, mat);

// sharpen image using "unsharp mask" algorithm
        Mat blurred = new Mat();
        croppedMat.copyTo(blurred);
        double sigma = 6, threshold = 1, amount = 0.5;
        GaussianBlur(croppedMat, blurred, new Size(0, 0), sigma, sigma);
        Mat lowContrastMask = new Mat();
        croppedMat.copyTo(lowContrastMask);

        absdiff(croppedMat, blurred, lowContrastMask);
        Core.compare(lowContrastMask, new Scalar(threshold), lowContrastMask, Core.CMP_LT);
        Mat sharpened = new Mat();
        croppedMat.copyTo(sharpened);
        Core.addWeighted(croppedMat, 1 + amount, blurred, -1 * amount, 0, sharpened);
        sharpened.convertTo(sharpened, -1, 1.4, 0);

        blurred.release();
        lowContrastMask.release();
        return sharpened;
    }

    public static Mat grayscaleMagic(Mat sourceMat) {
        final Mat mat = new Mat();
        sourceMat.copyTo(mat);
        cvtColor(mat, mat, COLOR_BGR2GRAY);
        threshold(mat, mat, 127, 255, THRESH_BINARY);
        adaptiveThreshold(mat, mat, 255, ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY, 11, 2);
        return mat;
    }


    public static Mat grayscale(Mat croppedMat) {
        final Mat mat = new Mat();
        croppedMat.copyTo(mat);
        cvtColor(mat, mat, COLOR_BGR2GRAY);
        return mat;
    }

    public static Mat thresholdOTSU(Mat croppedMat) {
        final Mat mat = new Mat();
        croppedMat.copyTo(mat);
        cvtColor(mat, mat, COLOR_BGR2GRAY);
        threshold(mat, mat, 0, 255, THRESH_OTSU);
        return mat;
    }

    public static Mat adaptiveThresholdMean(Mat sourceMat) {
        final Mat mat = new Mat();
        sourceMat.copyTo(mat);
        cvtColor(mat, mat, COLOR_BGR2GRAY);
        adaptiveThreshold(mat, mat, 255, ADAPTIVE_THRESH_MEAN_C, THRESH_BINARY, 11, 2);
        return mat;
    }

    public static Mat adaptiveThresholdGaussian(Mat sourceMat, int threshold) {
        final Mat mat = new Mat();
        sourceMat.copyTo(mat);
        cvtColor(mat, mat, COLOR_BGR2GRAY);
        adaptiveThreshold(mat, mat, 255, ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY, 11, 2);
        fastNlMeansDenoising(mat, mat);
        return mat;
    }
}
