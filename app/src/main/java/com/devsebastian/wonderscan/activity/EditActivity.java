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
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.devsebastian.wonderscan.BrightnessAndContrastController;
import com.devsebastian.wonderscan.DBHelper;
import com.devsebastian.wonderscan.Filter;
import com.devsebastian.wonderscan.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jsibbold.zoomage.ZoomageView;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.devsebastian.wonderscan.Utils.readMat;
import static com.devsebastian.wonderscan.Utils.saveMat;
import static org.opencv.android.Utils.matToBitmap;

public class EditActivity extends BaseActivity implements BottomNavigationView.OnNavigationItemSelectedListener, View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    Handler processImageHandler;
    Runnable processImageRunnable;
    private Mat croppedMat;
    private Mat editedMat;
    private ZoomageView mainImageView;
    private LinearLayout modifyToolsLayout;
    private TextView tv_brightness, tv_contrast;
    private boolean modifyToolsIsVisible = false;
    private int currentActiveId = R.id.iv_original_image;
    private String editedPath, croppedPath;
    private ExecutorService executorService;
    private BrightnessAndContrastController brightnessAndContrastController;

    void setupPreview() {
        croppedMat = readMat(croppedPath);
        if (editedPath != null) {
            editedMat = readMat(editedPath);
        } else {
            editedMat = new Mat();
            croppedMat.copyTo(editedMat);
        }

        previewMat(editedMat);
        findViewById(R.id.pb_edit).setVisibility(View.GONE);
    }

    private void filterImageButton(int resourceId, ProcessImage processImage) {
        executorService.submit(() -> {
            long l = System.currentTimeMillis();
            Mat mat = new Mat();
            double height, width;
//            if (croppedMat.width() > 100) {
//                width = 200;
//                height = 200 * croppedMat.height() / (double) croppedMat.width();
//            } else {
            height = croppedMat.height();
            width = croppedMat.width();
//            }
            Imgproc.resize(croppedMat, mat, new Size(width, height));
            mat = processImage.process(mat);
            Bitmap bmp = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
            matToBitmap(mat, bmp);
            Log.d("devdevdev", getResources().getResourceEntryName(resourceId) + ": " + (System.currentTimeMillis() - l));
            runOnUiThread(() -> ((ImageView) findViewById(resourceId)).setImageBitmap(bmp));
        });
    }

    private void filterImage(ProcessImage processImage) {
        findViewById(R.id.pb_edit).setVisibility(View.VISIBLE);
        if (processImageHandler != null && processImageRunnable != null) {
            processImageHandler.removeCallbacks(processImageRunnable);
            processImageHandler = null;
        }
        processImageHandler = new Handler();
        processImageRunnable = () -> {
            editedMat = processImage.process(croppedMat);
            previewMat(editedMat);
            runOnUiThread(() -> findViewById(R.id.pb_edit).setVisibility(View.GONE));
        };
        processImageHandler.post(processImageRunnable);
    }

    void setupFilterButtons() {
        filterImageButton(R.id.iv_original_image, m -> croppedMat);
        filterImageButton(R.id.iv_black_and_white, Filter::thresholdOTSU);
        filterImageButton(R.id.iv_auto, Filter::auto);
        filterImageButton(R.id.iv_grayscale, Filter::grayscale);
        filterImageButton(R.id.iv_magic, Filter::adaptiveMorph);
    }

    void setActive(int activeId) {
        findViewById(currentActiveId).setAlpha(0.6f);
        findViewById(currentActiveId).setPadding(12, 12, 12, 12);
        currentActiveId = activeId;
        findViewById(currentActiveId).setAlpha(1f);
        findViewById(currentActiveId).setPadding(0, 0, 0, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);


        executorService = Executors.newFixedThreadPool(5);
        DBHelper dbHelper = new DBHelper(this);

        setActive(R.id.iv_auto);

        long frameId = getIntent().getLongExtra(getString(R.string.intent_frame_id), -1);
        if (frameId == -1) {
            Toast.makeText(this, getString(R.string.toast_error_message), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        editedPath = dbHelper.getEditedPath(frameId);
        croppedPath = dbHelper.getCroppedPath(frameId);

        mainImageView = findViewById(R.id.iv_edit);
        modifyToolsLayout = findViewById(R.id.ll_modify_tools);
        tv_brightness = findViewById(R.id.tv_brightness);
        tv_contrast = findViewById(R.id.tv_contrast);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);

        modifyToolsLayout.setVisibility(View.GONE);

        setupPreview();
        setupFilterButtons();
        setupBrightnessAndContrast();

        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        findViewById(R.id.iv_black_and_white).setOnClickListener(this);
        findViewById(R.id.iv_auto).setOnClickListener(this);
        findViewById(R.id.iv_grayscale).setOnClickListener(this);
        findViewById(R.id.iv_magic).setOnClickListener(this);
        findViewById(R.id.iv_original_image).setOnClickListener(this);
    }

    private void setupBrightnessAndContrast() {
        brightnessAndContrastController = new BrightnessAndContrastController(0, 1);
        SeekBar sb_contrast = findViewById(R.id.sb_contrast);
        SeekBar sb_brightness = findViewById(R.id.sb_brightness);
        sb_brightness.setMax(200);
        sb_brightness.setProgress(100);
        sb_brightness.setOnSeekBarChangeListener(this);

        sb_contrast.setMax(200);
        sb_contrast.setProgress(100);
        sb_contrast.setOnSeekBarChangeListener(this);
    }

    public void previewMat(Mat mat) {
        Bitmap bitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bitmap);
//        runOnUiThread(() -> Glide.with(this).load(bitmap).into(mainImageView));
        runOnUiThread(() -> mainImageView.setImageBitmap(bitmap));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_save) {
            findViewById(R.id.pb_edit).setVisibility(View.VISIBLE);
            executorService.submit(() -> {
                saveMat(editedMat, editedPath);
                saveMat(croppedMat, croppedPath);
                Intent resultIntent = new Intent();
                resultIntent.putExtra(getString(R.string.intent_frame_position), getIntent().getIntExtra(getString(R.string.intent_frame_position), 0));
                setResult(RESULT_OK, resultIntent);
                editedMat.release();
                croppedMat.release();
                runOnUiThread(this::finish);
            });
        } else if (itemId == R.id.menu_rotate_left) {
            rotateLeft();
        } else if (itemId == R.id.menu_retake) {
            setResult(RESULT_CANCELED);
            finish();
        } else if (itemId == R.id.menu_modify) {
            modifyToolsIsVisible = !modifyToolsIsVisible;
            modifyToolsLayout.setVisibility(modifyToolsIsVisible ? View.VISIBLE : View.GONE);
        }
        return false;
    }

    public void rotateLeft() {
        Core.rotate(croppedMat, croppedMat, Core.ROTATE_90_COUNTERCLOCKWISE);
        Core.rotate(editedMat, editedMat, Core.ROTATE_90_COUNTERCLOCKWISE);
        previewMat(editedMat);
    }

    @Override
    public void onClick(View view) {
        setActive(view.getId());
        int id = view.getId();
        if (id == R.id.iv_black_and_white) {
            filterImage(Filter::thresholdOTSU);
        } else if (id == R.id.iv_auto) {
            filterImage(Filter::auto);
        } else if (id == R.id.iv_grayscale) {
            filterImage(Filter::grayscale);
        } else if (id == R.id.iv_magic) {
            filterImage(Filter::adaptiveMorph);
        } else if (id == R.id.iv_original_image) {
            editedMat = croppedMat.clone();
            previewMat(editedMat);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        Mat mat;
        int id = seekBar.getId();
        if (id == R.id.sb_contrast) {
            tv_contrast.setText(String.format(Locale.getDefault(), "Contrast • %d%%", i - 100));
            mat = brightnessAndContrastController.setContrast(editedMat.clone(), i / 100d);
        } else if (id == R.id.sb_brightness) {
            tv_brightness.setText(String.format(Locale.getDefault(), "Brightness • %d%%", i - 100));
            mat = brightnessAndContrastController.setBrightness(editedMat.clone(), i - 100);
        } else {
            mat = new Mat();
        }
        previewMat(mat);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    interface ProcessImage {
        Mat process(Mat mat);
    }
}