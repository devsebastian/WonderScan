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

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.devsebastian.wonderscan.DBHelper;
import com.devsebastian.wonderscan.R;
import com.devsebastian.wonderscan.adapter.ViewFrameAdapter;
import com.devsebastian.wonderscan.data.Frame;
import com.devsebastian.wonderscan.view.CustomViewPager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import java.util.List;

public class ViewPageActivity extends BaseActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    public static final int MODIFY_FRAME = 101;
    public static final int CROP_ACTIVITY = 102;

    private long docId;
    private List<Frame> frames;

    private CustomViewPager viewPager;

    private ViewFrameAdapter viewFrameAdapter;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("");
        setContentView(R.layout.activity_view_frames);
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

            frames = dbHelper.getAllFrames(docId);
            viewPager = findViewById(R.id.view_pager);
            viewFrameAdapter = new ViewFrameAdapter(this, frames);
            viewPager.setAdapter(viewFrameAdapter);
            viewPager.setCurrentItem(intent.getIntExtra(getString(R.string.intent_frame_position), 0));
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Frame frame = frames.get(viewPager.getCurrentItem());
        int itemId = item.getItemId();
        if (itemId == R.id.menu_modify) {
            Intent intent = new Intent(this, EditActivity.class);
            intent.putExtra(getString(R.string.intent_document_id), docId);
            intent.putExtra(getString(R.string.intent_frame_id), frames.get(viewPager.getCurrentItem()).getId());
            startActivityForResult(intent, MODIFY_FRAME);
        } else if (itemId == R.id.menu_note) {
            showNoteDialog("Note", "Write something beautiful here! This note will be saved alongside the scanned copy", null);
        } else if (itemId == R.id.menu_crop) {
            Intent cropIntent = new Intent(this, CropActivity.class);
            cropIntent.putExtra(getString(R.string.intent_source_path), dbHelper.getSourcePath(frame.getId()));
            cropIntent.putExtra(getString(R.string.intent_cropped_path), dbHelper.getCroppedPath(frame.getId()));
            cropIntent.putExtra(getString(R.string.intent_frame_position), viewPager.getCurrentItem());
            cropIntent.putExtra(getString(R.string.intent_angle), frame.getAngle());

            startActivityForResult(cropIntent, CROP_ACTIVITY);
        } else if (itemId == R.id.menu_ocr) {
            Toast.makeText(this, "Detecting Text. Please wait", Toast.LENGTH_SHORT).show();
            TextRecognizer recognizer = TextRecognition.getClient();
            Bitmap bitmap = BitmapFactory.decodeFile(dbHelper.getEditedPath(frame.getId()));
            recognizer.process(InputImage.fromBitmap(bitmap, 0))
                    .addOnSuccessListener(text -> showNoteDialog("Detected Text", "", text.getText()))
                    .addOnFailureListener(e -> Toast.makeText(this, "ERROR: Could not detect text", Toast.LENGTH_SHORT).show());
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_frames, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
        } else if (itemId == R.id.menu_rename) {
            showFrameRenameDialog(this, docId, viewPager.getCurrentItem());
        } else if (itemId == R.id.menu_delete) {
            showFrameDeleteDialog(this, docId, viewPager.getCurrentItem());
        }
        return super.onOptionsItemSelected(item);
    }

    public void showFrameRenameDialog(Activity activity, long docId, int framePos) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(activity);
        builder.setTitle("Rename");

        Frame frame = frames.get(framePos);

        FrameLayout frameLayout = new FrameLayout(activity);
        EditText editText = new EditText(activity);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(50, 12, 50, 12);
        editText.setLayoutParams(layoutParams);
        editText.setText(frame.getName());
        editText.setHint("Frame Name");
        frameLayout.addView(editText);

        builder.setView(frameLayout);
        builder.setNegativeButton("Cancel", null);
        builder.setPositiveButton("Save", (dialogInterface, i) -> dbHelper.renameFrame(docId, framePos, editText.getText().toString()));

        builder.create().show();
    }


    public void showFrameDeleteDialog(Activity activity, long docId, long frameIndex) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(activity);
        builder.setTitle("Confirm Delete");
        builder.setMessage("Are you sure you want to delete this frame? You won't be able to recover this frame later");
        builder.setNegativeButton("Cancel", null);
        builder.setPositiveButton("Delete", (dialogInterface, i) -> dbHelper.deleteFrame(docId, frameIndex));
        builder.create().show();
    }

    void showNoteDialog(String name, String hint, String note) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_note, null);
        builder.setView(view);
        EditText et_note;
        TextView cancelBtn, saveBtn, title;

        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        alertDialog.show();

        int framePos = viewPager.getCurrentItem();
        et_note = view.findViewById(R.id.et_note);
        et_note.setHint(hint);
        cancelBtn = view.findViewById(R.id.tv_cancel);
        saveBtn = view.findViewById(R.id.tv_save);
        title = view.findViewById(R.id.title);

        title.setText(name);

        if (note == null)
            note = frames.get(framePos).getNote();

        et_note.setText(note);

        saveBtn.setOnClickListener(v -> {
            DBHelper dbHelper = new DBHelper(ViewPageActivity.this);
            dbHelper.addNote(docId, framePos, et_note.getText().toString());
            alertDialog.dismiss();
        });

        cancelBtn.setOnClickListener(v -> alertDialog.dismiss());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            int index = 0;
            if (data != null)
                index = data.getIntExtra(getString(R.string.intent_frame_position), 0);
            if (requestCode == CROP_ACTIVITY) {
                dbHelper.updateEditedPath(docId, index, null);
            }
            viewPager.setAdapter(viewFrameAdapter);
            viewPager.setCurrentItem(index);
        }
    }
}