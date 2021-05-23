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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.io.File;

import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2RGB;
import static org.opencv.imgproc.Imgproc.COLOR_RGB2BGR;
import static org.opencv.imgproc.Imgproc.cvtColor;

public class Utils {
    public static final String FOLDER_NAME = "WonderScan";

    public static int getDeviceWidth(Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    public static File createPhotoFile(Context context) {
        String filename = System.currentTimeMillis() + ".png";
        File folder = new File(context.getFilesDir(), Utils.FOLDER_NAME);
        if (!folder.exists()) folder.mkdir();
        return new File(folder, filename);
    }

    public static void showConfirmDeleteDialog(Activity activity, long docId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Confirm Delete");
        builder.setMessage("Are you sure you want to delete this document. You won't be able to recover the document later!");
        builder.setNegativeButton("Cancel", null);
        builder.setPositiveButton("Delete", (dialogInterface, i) -> {
            DBHelper dbHelper = new DBHelper(activity);
            dbHelper.deleteDocument(docId);
            activity.finish();
        });

        builder.create().show();
    }

    public static void showDocumentRenameDialog(Activity activity, long docId, String name) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Rename");

        FrameLayout frameLayout = new FrameLayout(activity);
        EditText editText = new EditText(activity);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(50, 12, 50, 12);
        editText.setLayoutParams(layoutParams);
        editText.setText(name);
        frameLayout.addView(editText);

        builder.setView(frameLayout);
        builder.setNegativeButton("Cancel", null);
        builder.setPositiveButton("Save", (dialogInterface, i) -> {
            DBHelper dbHelper = new DBHelper(activity);
            dbHelper.renameDocument(docId, editText.getText().toString());
            ((TextView) activity.findViewById(R.id.toolbar_title)).setText(name);
        });
        builder.create().show();
    }

    public static void rotateMat(Mat mat, int angle) {
        switch (angle) {
            case 90:
                Core.rotate(mat, mat, Core.ROTATE_90_CLOCKWISE);
                break;
            case 180:
                Core.rotate(mat, mat, Core.ROTATE_180);
                break;
            case 270:
            case -90:
                Core.rotate(mat, mat, Core.ROTATE_90_COUNTERCLOCKWISE);
                break;
        }
    }

    public static Bitmap getBitmapFromMat(Mat mat) {
        Bitmap bitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
        org.opencv.android.Utils.matToBitmap(mat, bitmap);
        return bitmap;
    }

    public static void saveMat(Mat mat, String path) {
        cvtColor(mat, mat, COLOR_BGR2RGB);
        imwrite(path, mat);
    }

    public static Mat readMat(String path) {
        Mat mat = imread(path);
        if (!mat.empty())
            cvtColor(mat, mat, COLOR_RGB2BGR);
        return mat;
    }

    public static void sendCreateFileIntent(Activity activity, String name, String type, int code) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(type);
        intent.putExtra(Intent.EXTRA_TITLE, name);
        activity.startActivityForResult(intent, code);
    }

    public static void removeImageFromCache(String path) {
        File file = new File(path);
        if (file.exists()) file.delete();
    }
}
