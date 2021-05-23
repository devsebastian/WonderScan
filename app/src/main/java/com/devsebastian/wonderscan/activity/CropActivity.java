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

package com.devsebastian.wonderscan.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.devsebastian.wonderscan.AsyncTask.DetectBoxTask;
import com.devsebastian.wonderscan.R;
import com.devsebastian.wonderscan.data.BoundingRect;
import com.devsebastian.wonderscan.view.CropView;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import static com.devsebastian.wonderscan.Utils.createPhotoFile;
import static com.devsebastian.wonderscan.Utils.getDeviceWidth;
import static com.devsebastian.wonderscan.Utils.rotateMat;
import static com.devsebastian.wonderscan.Utils.saveMat;
import static java.lang.Math.max;
import static java.lang.Math.sqrt;
import static org.opencv.android.Utils.bitmapToMat;
import static org.opencv.imgcodecs.Imgcodecs.imread;

public class CropActivity extends BaseActivity {

    private double ratio;
    private String croppedPath;

    public static Mat getPerspectiveTransform(Bitmap bitmap, BoundingRect boundingRect, double ratio) {
        Mat mat = new Mat();
        bitmapToMat(bitmap, mat);
        Mat src_mat = new Mat(4, 1, CvType.CV_32FC2);
        Mat dst_mat = new Mat(4, 1, CvType.CV_32FC2);

        Point TL = boundingRect.getTopLeft();
        Point TR = boundingRect.getTopRight();
        Point BL = boundingRect.getBottomLeft();
        Point BR = boundingRect.getBottomRight();
        src_mat.put(0, 0,
                TL.x / ratio, TL.y / ratio,
                BL.x / ratio, BL.y / ratio,
                BR.x / ratio, BR.y / ratio,
                TR.x / ratio, TR.y / ratio);


        double hwRatio = getHWRatio(TL, TR, BL, BR, bitmap.getWidth(), bitmap.getHeight(), ratio);

        double height, width;
        if (hwRatio != Double.POSITIVE_INFINITY) {
            double widthA = sqrt((Math.pow(BR.x - BL.x, 2)) + Math.pow((BR.y - BL.y), 2));
            double widthB = sqrt((Math.pow(TR.x - TL.x, 2)) + Math.pow((TR.y - TL.y), 2));
            width = max(widthA, widthB);
            height = width / hwRatio;
        } else {
            height = BL.y - TL.y;
            width = TR.x - TL.x;
        }
        dst_mat.put(0, 0,
                0.0, 0.0,
                0.0, height,
                width, height,
                width, 0.0);
        Mat perspectiveTransform = Imgproc.getPerspectiveTransform(src_mat, dst_mat);

        Mat dst = mat.clone();

        Imgproc.warpPerspective(mat, dst, perspectiveTransform, new Size(width, height));
        return dst;
    }

    public static Mat getPerspectiveTransform(Mat sourceMat, BoundingRect boundingRect, double ratio) {
        Mat mat = sourceMat.clone();
        Mat src_mat = new Mat(4, 1, CvType.CV_32FC2);
        Mat dst_mat = new Mat(4, 1, CvType.CV_32FC2);

        Point TL = boundingRect.getTopLeft();
        Point TR = boundingRect.getTopRight();
        Point BL = boundingRect.getBottomLeft();
        Point BR = boundingRect.getBottomRight();

        src_mat.put(0, 0,
                TL.x / ratio, TL.y / ratio,
                BL.x / ratio, BL.y / ratio,
                BR.x / ratio, BR.y / ratio,
                TR.x / ratio, TR.y / ratio);


        double hwRatio = getHWRatio(TL, TR, BL, BR, mat.width(), mat.height(), ratio);

        double widthA = sqrt((Math.pow(BR.x - BL.x, 2)) + Math.pow((BR.y - BL.y), 2));
        double widthB = sqrt((Math.pow(TR.x - TL.x, 2)) + Math.pow((TR.y - TL.y), 2));

        double width = max(widthA, widthB);

        double height = width / hwRatio;


        dst_mat.put(0, 0,
                0.0, 0.0,
                0.0, height,
                width, height,
                widthA, 0.0);
        Mat perspectiveTransform = Imgproc.getPerspectiveTransform(src_mat, dst_mat);

        Mat dst = mat.clone();

        Imgproc.warpPerspective(mat, dst, perspectiveTransform, new Size(width, height));
        return dst;
    }

    static double sqr(double u) {
        return u * u;
    }

    public static double getHWRatio(Point TL, Point TR, Point BL, Point BR, int width, int height, double ratio) {
        double m1x = TL.x / ratio;
        double m1y = TL.y / ratio;
        double m2x = TR.x / ratio;
        double m2y = TR.y / ratio;
        double m3x = BL.x / ratio;
        double m3y = BL.y / ratio;
        double m4x = BR.x / ratio;
        double m4y = BR.y / ratio;

        double u0 = width / 2f;
        double v0 = height / 2f;
        // in case it matters: licensed under GPLv2 or later
        // legend:
        // sqr(x)  = x*x
        // sqrt(x) = square root of x

        // let m1x,m1y ... m4x,m4y be the (x,y) pixel coordinates
        // of the 4 corners of the detected quadrangle
        // i.e. (m1x, m1y) are the cordinates of the first corner,
        // (m2x, m2y) of the second corner and so on.
        // let u0, v0 be the pixel coordinates of the principal point of the image
        // for a normal camera this will be the center of the image,
        // i.e. u0=IMAGEWIDTH/2; v0 =IMAGEHEIGHT/2
        // This assumption does not hold if the image has been cropped asymmetrically

        // first, transform the image so the principal point is at (0,0)
        // this makes the following equations much easier
        m1x = m1x - u0;
        m1y = m1y - v0;
        m2x = m2x - u0;
        m2y = m2y - v0;
        m3x = m3x - u0;
        m3y = m3y - v0;
        m4x = m4x - u0;
        m4y = m4y - v0;


        // temporary variables k2, k3
        double k2 = ((m1y - m4y) * m3x - (m1x - m4x) * m3y + m1x * m4y - m1y * m4x) /
                ((m2y - m4y) * m3x - (m2x - m4x) * m3y + m2x * m4y - m2y * m4x);

        double k3 = ((m1y - m4y) * m2x - (m1x - m4x) * m2y + m1x * m4y - m1y * m4x) /
                ((m3y - m4y) * m2x - (m3x - m4x) * m2y + m3x * m4y - m3y * m4x);

        // f_squared is the focal length of the camera, squared
        // if k2==1 OR k3==1 then this equation is not solvable
        // if the focal length is known, then this equation is not needed
        // in that case assign f_squared= sqr(focal_length)
        double f_squared =
                -((k3 * m3y - m1y) * (k2 * m2y - m1y) + (k3 * m3x - m1x) * (k2 * m2x - m1x)) /
                        ((k3 - 1) * (k2 - 1));

        //The width/height ratio of the original rectangle
        double hwRatio = sqrt(
                (sqr(k2 - 1) + sqr(k2 * m2y - m1y) / f_squared + sqr(k2 * m2x - m1x) / f_squared) /
                        (sqr(k3 - 1) + sqr(k3 * m3y - m1y) / f_squared + sqr(k3 * m3x - m1x) / f_squared)
        );

        // if k2==1 AND k3==1, then the focal length equation is not solvable
        // but the focal length is not needed to calculate the ratio.
        // I am still trying to figure out under which circumstances k2 and k3 become 1
        // but it seems to be when the rectangle is not distorted by perspective,
        // i.e. viewed straight on. Then the equation is obvious:
        if (k2 == 1 && k3 == 1) hwRatio = sqrt(
                (sqr(m2y - m1y) + sqr(m2x - m1x)) /
                        (sqr(m3y - m1y) + sqr(m3x - m1x)));


        // After testing, I found that the above equations
        // actually give the height/width ratio of the rectangle,
        // not the width/height ratio.
        // If someone can find the error that caused this,
        // I would be most grateful.
        // until then:
        return hwRatio;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);

        Intent intent = getIntent();
        String path = intent.getStringExtra(getString(R.string.intent_source_path));
        int angle = intent.getIntExtra(getString(R.string.intent_angle), 0);
        croppedPath = intent.getStringExtra(getString(R.string.intent_cropped_path));
        int framePos = intent.getIntExtra(getString(R.string.intent_frame_position), 0);

        CropView cropView = findViewById(R.id.cv_crop);
        TextView confirmBtn = findViewById(R.id.tv_confirm);
        TextView retakeBtn = findViewById(R.id.tv_retake);

        retakeBtn.setOnClickListener(v -> finish());

        int deviceWidth = getDeviceWidth(this);

        Bitmap bitmap = BitmapFactory.decodeFile(path);
        cropView.setImageBitmap(bitmap);

        float viewHeight = (bitmap.getHeight() * (deviceWidth / (float) bitmap.getWidth()));
        float scaleFactor = (deviceWidth / viewHeight) * 0.9f;

        cropView.animate()
                .rotation(angle)
                .scaleX(scaleFactor)
                .scaleY(scaleFactor)
                .setDuration(500)
                .start();

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(deviceWidth, (int) (bitmap.getHeight() * (deviceWidth / (float) bitmap.getWidth())));
        cropView.setLayoutParams(params);

        ratio = deviceWidth / (double) bitmap.getWidth();

        new DetectBoxTask(this, bitmap, 0, boundingRect -> {
            if (boundingRect == null) {
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                double padding = width * 0.1;
                boundingRect = new BoundingRect();
                boundingRect.setTopLeft(new Point(padding * ratio, padding * ratio));
                boundingRect.setTopRight(new Point((width - padding) * ratio, padding * ratio));
                boundingRect.setBottomLeft(new Point(padding * ratio, (height - padding) * ratio));
                boundingRect.setBottomRight(new Point((width - padding) * ratio, (height - padding) * ratio));
            }
            cropView.setBoundingRect(boundingRect);
        }).execute();


        confirmBtn.setOnClickListener(view -> {
            findViewById(R.id.spinkit_frame).setVisibility(View.VISIBLE);
            new Thread(() -> {
                Mat mat = getPerspectiveTransform(bitmap, cropView.getBoundingRect(), ratio);
                rotateMat(mat, angle);

                if (croppedPath == null)
                    croppedPath = createPhotoFile(this).getAbsolutePath();

                saveMat(mat, croppedPath);
                bitmap.recycle();

                Intent i = new Intent();
                i.putExtra(getString(R.string.intent_source_path), path);
                i.putExtra(getString(R.string.intent_cropped_path), croppedPath);
                i.putExtra(getString(R.string.intent_frame_position), framePos);
                i.putExtra(getString(R.string.intent_angle), angle);
                setResult(RESULT_OK, i);
                finish();
            }).start();

        });
    }
}