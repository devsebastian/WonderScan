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

import android.content.Context;
import android.content.Intent;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.devsebastian.wonderscan.AsyncTask.ExportPdfTask;
import com.devsebastian.wonderscan.DBHelper;
import com.devsebastian.wonderscan.R;
import com.devsebastian.wonderscan.Utils;
import com.devsebastian.wonderscan.adapter.ProgressFramesAdapter;
import com.devsebastian.wonderscan.data.Document;
import com.devsebastian.wonderscan.data.Frame;

import org.jetbrains.annotations.NotNull;
import org.opencv.core.Mat;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.devsebastian.wonderscan.Filter.adaptiveMorph;
import static com.devsebastian.wonderscan.Utils.createPhotoFile;
import static java.lang.Math.min;
import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;

public class ListFramesActivity extends BaseActivity {
    public static final int VIEW_PAGE_ACTIVITY = 101;
    public static final int SAVE_PDF_INTENT_CODE = 102;

    private long docId;

    private PdfDocument pdfDocument;
    private DBHelper dbHelper;
    private Document document;
    private List<Frame> frames;
    private ProgressFramesAdapter framesAdapter;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list_frames, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_export_pdf) {
            new ExportPdfTask(this, frames, (pdf) -> {
                pdfDocument = pdf;
                Utils.sendCreateFileIntent(this, document.getName(), "application/pdf", SAVE_PDF_INTENT_CODE);
            }).execute();
        } else if (itemId == R.id.menu_delete) {
            Utils.showConfirmDeleteDialog(this, docId);
        } else if (itemId == R.id.menu_rename) {
            Utils.showDocumentRenameDialog(this, docId, document.getName());
        } else if (itemId == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void processFrame(Context context, long frameId) {
        File file = createPhotoFile(context);
        Mat mat = imread(dbHelper.getCroppedPath(frameId));
        Mat editedMat = adaptiveMorph(mat);
        imwrite(file.getAbsolutePath(), editedMat);
        dbHelper.updateEditedPath(frameId, file.getAbsolutePath());
        mat.release();
        editedMat.release();
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

        dbHelper = new DBHelper(this);

        Intent intent = getIntent();
        if (intent != null) {
            docId = intent.getLongExtra(getString(R.string.intent_document_id), -1);
            if (docId == -1) {
                Toast.makeText(this, getString(R.string.toast_error_message), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            document = dbHelper.getDocument(docId);
            frames = dbHelper.getAllFrames(docId);
            ((TextView) findViewById(R.id.toolbar_title)).setText(dbHelper.getDocumentName(docId));
        }

        framesAdapter = new ProgressFramesAdapter(this, docId, frames);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(getItemTouchHelperCallback());

        processUnprocessedFrames();

        View fab = findViewById(R.id.fab);
        RecyclerView recyclerView = findViewById(R.id.rv_frames);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(framesAdapter);

        fab.setOnClickListener(view -> {
            Intent i = new Intent(ListFramesActivity.this, ScanActivity.class);
            i.putExtra(getString(R.string.intent_document_id), docId);
            startActivity(i);
            finish();
        });

        itemTouchHelper.attachToRecyclerView(recyclerView);

        Toast.makeText(this, "Hint: Re-order pages by long pressing a page and dragging it to the appropriate position", Toast.LENGTH_SHORT).show();
    }

    ItemTouchHelper.Callback getItemTouchHelperCallback() {
        return new ItemTouchHelper.Callback() {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int from = viewHolder.getAdapterPosition();
                int to = target.getAdapterPosition();
                framesAdapter.swap(from, to);
                dbHelper.swapFrames(frames.get(from).getId(), from, frames.get(to).getId(), to);
                return false;
            }

            @Override
            public void onSwiped(@NotNull RecyclerView.ViewHolder viewHolder, int direction) {
            }

            //defines the enabled move directions in each state (idle, swiping, dragging).
            @Override
            public int getMovementFlags(@NotNull RecyclerView recyclerView, @NotNull RecyclerView.ViewHolder viewHolder) {
                return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG,
                        ItemTouchHelper.DOWN | ItemTouchHelper.UP | ItemTouchHelper.START | ItemTouchHelper.END);
            }
        };
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
        } else if (requestCode == VIEW_PAGE_ACTIVITY) {
            processUnprocessedFrames();
        }
    }

    private void processUnprocessedFrames() {
        ExecutorService executorService = Executors.newFixedThreadPool(min(9, frames.size()));
        for (int i = 0; i < frames.size(); i++) {
            long frameId = frames.get(i).getId();
            if (dbHelper.getEditedPath(frameId) == null) {
                int finalIndex = i;
                executorService.submit(() -> {
                    Frame frame = frames.get(finalIndex);
                    processFrame(this, frame.getId());
                    runOnUiThread(() -> framesAdapter.notifyItemChanged(finalIndex));
                });
            }
        }
        if (!executorService.isShutdown()) {
            executorService.shutdown();
        }
    }


}