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

package com.devsebastian.wonderscan.AsyncTask;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.media.Image;
import android.os.AsyncTask;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageProxy;

import com.devsebastian.wonderscan.data.BoundingRect;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.devsebastian.wonderscan.Utils.getDeviceWidth;
import static com.devsebastian.wonderscan.Utils.rotateMat;
import static org.opencv.android.Utils.matToBitmap;
import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.core.CvType.CV_8UC2;
import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.COLOR_YUV2RGBA_I420;
import static org.opencv.imgproc.Imgproc.COLOR_YUV2RGBA_NV12;
import static org.opencv.imgproc.Imgproc.COLOR_YUV2RGBA_NV21;
import static org.opencv.imgproc.Imgproc.RETR_LIST;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.cvtColorTwoPlane;
import static org.opencv.imgproc.Imgproc.findContours;

public class DetectBoxTask extends AsyncTask<Object, Object, Boolean> {

    public static final int MAX_WIDTH = 500;
    private static final String TAG = DetectBoxTask.class.getSimpleName();
    Mat sourceMat;
    BoundingRect bRect;
    double ratio;
    OnSuccessListener onSuccessListener;

    @ExperimentalGetImage
    public DetectBoxTask(Activity activity, ImageProxy image, int angle, OnSuccessListener onSuccessListener) {
        this.onSuccessListener = onSuccessListener;
        this.sourceMat = yuvToRgba(image, angle);
        Bitmap bmp = Bitmap.createBitmap(sourceMat.width(), sourceMat.height(), Bitmap.Config.ARGB_8888);
        matToBitmap(sourceMat, bmp);
        ratio = getDeviceWidth(activity) / (float) sourceMat.width();
    }

    public DetectBoxTask(Activity activity, Bitmap image, int angle, OnSuccessListener onSuccessListener) {
        this.onSuccessListener = onSuccessListener;
        this.sourceMat = bitmapToMat(image, angle);
        ratio = getDeviceWidth(activity) / (float) sourceMat.width();
    }

    private Mat bitmapToMat(Bitmap bitmap, int angle) {
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap, mat);
        resizeMat(mat);
        rotateMat(mat, angle);
        return mat;
    }


    Mat yuvToRgba(ImageProxy imageProxy, int angle) {
        Mat rgbaMat = new Mat();
        ImageProxy.PlaneProxy[] planes = imageProxy.getPlanes();
        int height = imageProxy.getHeight();
        int width = imageProxy.getWidth();

        if (imageProxy.getFormat() == ImageFormat.YUV_420_888 && planes.length == 3) {

            int chromaPixelStride = planes[1].getPixelStride();

            if (chromaPixelStride == 2) { // Chroma channels are interleaved
                ByteBuffer yPlane = planes[0].getBuffer();
                ByteBuffer uvPlane1 = planes[1].getBuffer();
                ByteBuffer uvPlane2 = planes[2].getBuffer();
                Mat yMat = new Mat(height, width, CV_8UC1, yPlane);
                Mat uvMat1 = new Mat(height / 2, width / 2, CV_8UC2, uvPlane1);
                Mat uvMat2 = new Mat(height / 2, width / 2, CV_8UC2, uvPlane2);


                long addrDiff = uvMat2.dataAddr() - uvMat1.dataAddr();
                if (addrDiff > 0) {
                    cvtColorTwoPlane(yMat, uvMat1, rgbaMat, COLOR_YUV2RGBA_NV12);
                } else {
                    cvtColorTwoPlane(yMat, uvMat2, rgbaMat, COLOR_YUV2RGBA_NV21);
                }
            } else { // Chroma channels are not interleaved
                byte[] yuvBytes = new byte[width * (height + height / 2)];
                ByteBuffer yPlane = planes[0].getBuffer();
                ByteBuffer uPlane = planes[1].getBuffer();
                ByteBuffer vPlane = planes[2].getBuffer();

                yPlane.get(yuvBytes, 0, width * height);

                int chromaRowStride = planes[1].getRowStride();
                int chromaRowPadding = chromaRowStride - width / 2;

                int offset = width * height;
                if (chromaRowPadding == 0) {
                    // When the row stride of the chroma channels equals their width, we can copy
                    // the entire channels in one go
                    uPlane.get(yuvBytes, offset, width * height / 4);
                    offset += width * height / 4;
                    vPlane.get(yuvBytes, offset, width * height / 4);
                } else {
                    // When not equal, we need to copy the channels row by row
                    for (int i = 0; i < height / 2; i++) {
                        uPlane.get(yuvBytes, offset, width / 2);
                        offset += width / 2;
                        if (i < height / 2 - 1) {
                            uPlane.position(uPlane.position() + chromaRowPadding);
                        }
                    }
                    for (int i = 0; i < height / 2; i++) {
                        vPlane.get(yuvBytes, offset, width / 2);
                        offset += width / 2;
                        if (i < height / 2 - 1) {
                            vPlane.position(vPlane.position() + chromaRowPadding);
                        }
                    }
                }

                Mat yuvMat = new Mat(height + height / 2, width, CV_8UC1);
                yuvMat.put(0, 0, yuvBytes);
                cvtColor(yuvMat, rgbaMat, COLOR_YUV2RGBA_I420, 4);
            }
        }

        Bitmap bmp = Bitmap.createBitmap(rgbaMat.width(), rgbaMat.height(), Bitmap.Config.ARGB_8888);
        matToBitmap(rgbaMat, bmp);
        resizeMat(rgbaMat);
        rotateMat(rgbaMat, angle);

        return rgbaMat;
    }

    public Mat convertYuv420888ToMat(Image image, boolean isGreyOnly) {
        int width = image.getWidth();
        int height = image.getHeight();

        Image.Plane yPlane = image.getPlanes()[0];
        int ySize = yPlane.getBuffer().remaining();

        if (isGreyOnly) {
            byte[] data = new byte[ySize];
            yPlane.getBuffer().get(data, 0, ySize);

            Mat greyMat = new Mat(height, width, CvType.CV_8UC1);
            greyMat.put(0, 0, data);

            return greyMat;
        }

        Image.Plane uPlane = image.getPlanes()[1];
        Image.Plane vPlane = image.getPlanes()[2];

        // be aware that this size does not include the padding at the end, if there is any
        // (e.g. if pixel stride is 2 the size is ySize / 2 - 1)
        int uSize = uPlane.getBuffer().remaining();
        int vSize = vPlane.getBuffer().remaining();

        byte[] data = new byte[ySize + (ySize / 2)];

        yPlane.getBuffer().get(data, 0, ySize);

        ByteBuffer ub = uPlane.getBuffer();
        ByteBuffer vb = vPlane.getBuffer();

        int uvPixelStride = uPlane.getPixelStride(); //stride guaranteed to be the same for u and v planes
        if (uvPixelStride == 1) {
            uPlane.getBuffer().get(data, ySize, uSize);
            vPlane.getBuffer().get(data, ySize + uSize, vSize);

            Mat yuvMat = new Mat(height + (height / 2), width, CvType.CV_8UC1);
            yuvMat.put(0, 0, data);
            Mat rgbMat = new Mat(height, width, CvType.CV_8UC3);
            Imgproc.cvtColor(yuvMat, rgbMat, Imgproc.COLOR_YUV2RGB_I420, 3);
            yuvMat.release();
            return rgbMat;
        }

        // if pixel stride is 2 there is padding between each pixel
        // converting it to NV21 by filling the gaps of the v plane with the u values
        vb.get(data, ySize, vSize);
        for (int i = 0; i < uSize; i += 2) {
            data[ySize + i + 1] = ub.get(i);
        }

        Mat yuvMat = new Mat(height + (height / 2), width, CvType.CV_8UC1);
        yuvMat.put(0, 0, data);
        Mat rgbMat = new Mat(height, width, CvType.CV_8UC3);
        Imgproc.cvtColor(yuvMat, rgbMat, Imgproc.COLOR_YUV2RGB_NV12, 3);
        yuvMat.release();
        return rgbMat;
    }

    private Mat imageToMat(@NonNull Image img, int angle) {
        byte[] nv21;

        ByteBuffer yBuffer = img.getPlanes()[0].getBuffer();
        ByteBuffer uBuffer = img.getPlanes()[1].getBuffer();
        ByteBuffer vBuffer = img.getPlanes()[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        nv21 = new byte[ySize + uSize + vSize];

        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        Mat yuv = new Mat(img.getHeight() + img.getHeight() / 2, img.getWidth(), CV_8UC1);
        yuv.put(0, 0, nv21);


        Mat rgb = new Mat();
        cvtColor(yuv, rgb, Imgproc.COLOR_YUV2BGR_NV21, 3);

        Bitmap bmp = Bitmap.createBitmap(rgb.width(), rgb.height(), Bitmap.Config.ARGB_8888);
        matToBitmap(rgb, bmp);
        resizeMat(rgb);
        rotateMat(rgb, angle);
        return rgb;
    }

    void resizeMat(Mat mat) {
        Imgproc.resize(mat, mat, new Size(MAX_WIDTH, mat.height() * MAX_WIDTH / (float) mat.width()));
    }

    boolean findCorners() {
        Mat mat = sourceMat.clone();
        cvtColor(mat, mat, COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(mat, mat, new Size(5, 5), 0);
        Imgproc.Canny(mat, mat, 75, 200);

        List<MatOfPoint> points = new ArrayList<>();
        findContours(mat, points, new Mat(), RETR_LIST, CHAIN_APPROX_SIMPLE);

        List<Pair<MatOfPoint, Double>> areas = new ArrayList<>();

        for (MatOfPoint point : points) {
            areas.add(new Pair<>(point, Imgproc.contourArea(point)));
        }

        areas.sort((t1, t2) -> Double.compare(t2.second, t1.second));

        double maxArea = mat.width() * (mat.height() / 8f);
        if (areas.size() == 0 || areas.get(0).second < maxArea) {
            return false;
        }

        for (Pair<MatOfPoint, Double> area : areas) {
            MatOfPoint2f matOfPoint2f = new MatOfPoint2f(area.first.toArray());
            Imgproc.approxPolyDP(matOfPoint2f, matOfPoint2f, 0.02 * Imgproc.arcLength(matOfPoint2f, true), true);
            if (matOfPoint2f.height() == 4) {
                if (area.second > maxArea) {
                    bRect = new BoundingRect();
                    bRect.fromPoints(matOfPoint2f.toList(), ratio, ratio);
                    mat.release();
                    return true;
                }
            }
        }
        mat.release();
        return false;
    }


    @Override
    protected Boolean doInBackground(Object... objects) {
        return findCorners();
    }

    @Override
    protected void onPostExecute(Boolean result) {
        sourceMat.release();
        onSuccessListener.onSuccess(bRect);
    }

    public interface OnSuccessListener {
        void onSuccess(BoundingRect boundingRect);
    }
}
