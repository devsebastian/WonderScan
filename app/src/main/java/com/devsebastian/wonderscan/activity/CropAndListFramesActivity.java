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
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devsebastian.wonderscan.AsyncTask.ExportPdfTask;
import com.devsebastian.wonderscan.DBHelper;
import com.devsebastian.wonderscan.R;
import com.devsebastian.wonderscan.Utils;
import com.devsebastian.wonderscan.adapter.ProgressFramesAdapter;
import com.devsebastian.wonderscan.data.BoundingRect;
import com.devsebastian.wonderscan.data.Frame;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.devsebastian.wonderscan.Filter.auto;
import static com.devsebastian.wonderscan.Utils.createPhotoFile;
import static com.devsebastian.wonderscan.activity.CropActivity.getPerspectiveTransform;
import static java.lang.Math.min;
import static java.lang.System.currentTimeMillis;
import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;
import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.RETR_LIST;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.findContours;

public class CropAndListFramesActivity extends BaseActivity {


    private static final int SAVE_PDF_INTENT_CODE = 101;
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss", Locale.getDefault());
    PdfDocument pdfDocument;
    private long docId;
    private String docName;
    private DBHelper dbHelper;
    private ProgressFramesAdapter framesAdapter;

    private ArrayList<String> sourcePaths = new ArrayList<>();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list_frames, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_export_pdf) {
            new ExportPdfTask(this, dbHelper.getAllFrames(docId), (pdf) -> {
                pdfDocument = pdf;
                Utils.sendCreateFileIntent(this, docName, "application/pdf", SAVE_PDF_INTENT_CODE);
            }).execute();
        } else if (itemId == R.id.menu_delete) {
            Utils.showConfirmDeleteDialog(this, docId);
        } else if (itemId == R.id.menu_rename) {
            Utils.showDocumentRenameDialog(this, docId, docName);
        } else if (itemId == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SAVE_PDF_INTENT_CODE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            try {
                pdfDocument.writeTo(getContentResolver().openOutputStream(uri));
                Toast.makeText(this, "PDF document saved in " + uri.getPath(), Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("");
        setContentView(R.layout.activity_list_frames);
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        RecyclerView recyclerView = findViewById(R.id.rv_frames);

        dbHelper = new DBHelper(this);

        Intent intent = getIntent();
        if (intent != null) {
            sourcePaths = intent.getStringArrayListExtra(getString(R.string.intent_uris));
        }

        View fab = findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setHasFixedSize(true);


        docName = getString(R.string.app_name) + " " + simpleDateFormat.format(new Date());
        docId = dbHelper.insertDocument(docName);

        framesAdapter = new ProgressFramesAdapter(this, docId, new ArrayList<>());
        recyclerView.setAdapter(framesAdapter);

        ExecutorService executorService = Executors.newFixedThreadPool(min(9, sourcePaths.size()));
        executorService.submit(() -> {
            List<Frame> frames = getFramesFromImagePaths(sourcePaths);
            framesAdapter.setFrames(frames);

            runOnUiThread(() -> framesAdapter.notifyDataSetChanged());

            for (int i = 0; i < sourcePaths.size(); i++) {
                int finalI = i;
                executorService.submit(() -> CropAndFormat(sourcePaths.get(finalI), frames.get(finalI).getId(), finalI));
            }

            if (!executorService.isShutdown())
                executorService.shutdown();
        });
    }

    private List<Frame> getFramesFromImagePaths(List<String> paths) {
        ArrayList<Frame> frames = new ArrayList<>();
        for (int i = 0; i < paths.size(); i++) {
            String sourcePath = paths.get(i);
            Frame frame = new Frame();
            frame.setTimeInMillis(currentTimeMillis());
            frame.setIndex(i);
            long frameId = dbHelper.insertFrame(docId, frame);
            frame.setId(frameId);
            dbHelper.updateSourcePath(frameId, sourcePath);
            frames.add(frame);
        }
        return frames;
    }


    void CropAndFormat(String path, long frameId, int index) {
        Mat originalMat = imread(path);

        double ratio = Utils.getDeviceWidth(this) / (double) originalMat.width();
        BoundingRect bRect = findCorners(originalMat, ratio);
        Mat croppedMat;
        if (bRect != null) {
            croppedMat = getPerspectiveTransform(originalMat, bRect, ratio);
        } else {
            croppedMat = new Mat();
            originalMat.copyTo(croppedMat);
        }

        String croppedPath = createPhotoFile(this).getAbsolutePath();
        imwrite(croppedPath, croppedMat);
        dbHelper.updateCroppedPath(frameId, croppedPath);

        Mat editedMat = auto(croppedMat);

        String editedPath = createPhotoFile(this).getAbsolutePath();
        imwrite(editedPath, editedMat);
        dbHelper.updateEditedPath(frameId, editedPath);

        runOnUiThread(() -> framesAdapter.notifyItemChanged(index));

        originalMat.release();
        croppedMat.release();
        editedMat.release();
    }


    BoundingRect findCorners(Mat sourceMat, double ratio) {
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
            return null;
        }

        for (Pair<MatOfPoint, Double> area : areas) {
            MatOfPoint2f matOfPoint2f = new MatOfPoint2f(area.first.toArray());
            Imgproc.approxPolyDP(matOfPoint2f, matOfPoint2f, 0.02 * Imgproc.arcLength(matOfPoint2f, true), true);
            if (matOfPoint2f.height() == 4) {
                if (area.second > maxArea) {
                    BoundingRect bRect = new BoundingRect();
                    bRect.fromPoints(matOfPoint2f.toList(), ratio, ratio);
                    mat.release();
                    return bRect;
                }
            }
        }
        mat.release();
        return null;
    }
}