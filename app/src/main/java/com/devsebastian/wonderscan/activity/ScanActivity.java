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
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.media.Image;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.devsebastian.wonderscan.AsyncTask.DetectBoxTask;
import com.devsebastian.wonderscan.DBHelper;
import com.devsebastian.wonderscan.R;
import com.devsebastian.wonderscan.data.Frame;
import com.devsebastian.wonderscan.view.ScanView;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.devsebastian.wonderscan.Utils.createPhotoFile;

public class ScanActivity extends BaseActivity {

    public static final String TAG = ScanActivity.class.getSimpleName();

    private static final int CROP_ACTIVITY = 101;
    private final int REQUEST_CODE_PERMISSIONS = 1001;

    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};

    private long docId;
    private int angle;

    private ScanView scanView;
    private PreviewView viewFinder;
    private ProgressBar captureProgress;
    private ImageView finalImage;
    private TextView pageCount;

    private List<Pair<String, String>> paths;
    private Executor executor = Executors.newSingleThreadExecutor();

    private DBHelper dbHelper;

    @Override

    @androidx.camera.core.ExperimentalGetImage
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_scan);

        captureProgress = findViewById(R.id.pb_scan);
        finalImage = findViewById(R.id.iv_recent_capture);
        FrameLayout frameLayout = findViewById(R.id.camera_frame);
        viewFinder = findViewById(R.id.viewFinder);
        scanView = findViewById(R.id.scan_view);
        pageCount = findViewById(R.id.page_count);

        paths = new ArrayList<>();

        Intent intent = getIntent();
        if (intent != null) {
            docId = intent.getLongExtra(getString(R.string.intent_document_id), -1);
        }


        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int deviceWidth = displayMetrics.widthPixels;
        frameLayout.setLayoutParams(new LinearLayout.LayoutParams(deviceWidth, (int) (deviceWidth * (4 / 3f))));

        dbHelper = new DBHelper(this);
        finalImage.setOnClickListener(view -> {
            long count = 0;
            if (docId == -1) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss", Locale.getDefault());
                docId = dbHelper.insertDocument(getString(R.string.app_name) + " " + simpleDateFormat.format(new Date()));
            } else {
                count = dbHelper.getPageCount(docId);
            }
            for (int i = 0; i < paths.size(); i++) {
                Pair<String, String> path = paths.get(i);
                Frame frame = new Frame();
                frame.setTimeInMillis(System.currentTimeMillis());
                frame.setIndex(count + i);
                frame.setAngle(angle);
                long frameId = dbHelper.insertFrame(docId, frame);
                dbHelper.updateSourcePath(frameId, path.first);
                dbHelper.updateCroppedPath(frameId, path.second);
            }

            Intent i = new Intent(ScanActivity.this, ListFramesActivity.class);
            i.putExtra(getString(R.string.intent_document_id), docId);
            startActivity(i);
            finish();
        });

        if (allPermissionsGranted()) {
            startCamera(); //start camera if permission has been granted by user
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

    }

    private boolean allPermissionsGranted() {

        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    @androidx.camera.core.ExperimentalGetImage
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                this.finish();
            }
        }
    }

    @androidx.camera.core.ExperimentalGetImage
    private void startCamera() {

        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {

                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);

            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CROP_ACTIVITY && resultCode == RESULT_OK && data != null) {
            String croppedPath = data.getStringExtra(getString(R.string.intent_cropped_path));
            String sourcePath = data.getStringExtra(getString(R.string.intent_source_path));
            paths.add(new Pair<>(sourcePath, croppedPath));
            finalImage.setImageBitmap(BitmapFactory.decodeFile(croppedPath));
            if (pageCount.getVisibility() != View.VISIBLE) pageCount.setVisibility(View.VISIBLE);
            pageCount.setText(String.valueOf(paths.size()));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @androidx.camera.core.ExperimentalGetImage
    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        ImageCapture.Builder builder = new ImageCapture.Builder();

        final ImageCapture imageCapture = builder
                .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .build();

        preview.setSurfaceProvider(viewFinder.getSurfaceProvider());


        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, imageAnalysis);
        View captureImageBtn = findViewById(R.id.btn_capture);

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), imageProxy -> {
            angle = imageProxy.getImageInfo().getRotationDegrees();
            Image image = imageProxy.getImage();
            if (image != null && image.getFormat() == ImageFormat.YUV_420_888) {
                new DetectBoxTask(this, imageProxy, angle, boundingRect -> {
                    scanView.setBoundingRect(boundingRect);
                    imageProxy.close();
                }).execute();
            }
        });

        captureImageBtn.setOnClickListener(v -> {
            captureProgress.setVisibility(View.VISIBLE);
            File file = createPhotoFile(this);

            executor = ContextCompat.getMainExecutor(this);

            ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();
            imageCapture.takePicture(outputFileOptions, executor, new ImageCapture.OnImageSavedCallback() {
                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                    Intent intent = new Intent(ScanActivity.this, CropActivity.class);
                    intent.putExtra(getString(R.string.intent_source_path), file.getAbsolutePath());
                    intent.putExtra(getString(R.string.intent_angle), angle);
                    startActivityForResult(intent, CROP_ACTIVITY);
                    captureProgress.setVisibility(View.GONE);
                }

                @Override
                public void onError(@NonNull ImageCaptureException error) {
                    Log.d(TAG, Log.getStackTraceString(error));
                }
            });
        });

    }

}