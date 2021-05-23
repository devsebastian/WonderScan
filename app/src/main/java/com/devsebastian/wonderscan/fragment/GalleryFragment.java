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

package com.devsebastian.wonderscan.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devsebastian.wonderscan.R;
import com.devsebastian.wonderscan.activity.CropAndListFramesActivity;
import com.devsebastian.wonderscan.adapter.GalleryAdapter;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class GalleryFragment extends Fragment {


    private GalleryAdapter adapter;
    private Activity activity;

    @Override
    public void onResume() {
        super.onResume();
        adapter.reset();
    }

    // get all images from external storage
    public List<String> getAllImages() {
        List<String> uris = new ArrayList<>();
        String[] projection = {MediaStore.MediaColumns.DATA};
        if (getActivity() != null) {
            Cursor cursor = getActivity().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC");
            while (cursor.moveToNext()) {
                String absolutePathOfImage = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
                uris.add(absolutePathOfImage);
            }
            cursor.close();
        }
        return uris;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_gallery, container, false);
        activity = getActivity();

        RecyclerView recyclerView = v.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 5));
        adapter = new GalleryAdapter(getActivity(), new ArrayList<>());
        recyclerView.setAdapter(adapter);

        new Thread(() -> {
            List<String> uris = getAllImages();
            adapter.setImagePaths(uris);
            activity.runOnUiThread(() -> adapter.notifyDataSetChanged());
        }).start();

        View fab = v.findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            v.findViewById(R.id.gallery_progress).setVisibility(View.VISIBLE);
            new Thread(() -> {
                ArrayList<String> uris = adapter.getSelectedUris();
                Intent intent = new Intent(getActivity(), CropAndListFramesActivity.class);
                intent.putExtra(getString(R.string.intent_uris), uris);
                startActivity(intent);
                adapter.clearSelection();
                activity.runOnUiThread(() -> v.findViewById(R.id.gallery_progress).setVisibility(View.GONE));
            }).start();
        });

        if (getActivity().getPreferences(MODE_PRIVATE).getBoolean("closed_message", false)) {
            v.findViewById(R.id.gallery_message).setVisibility(View.GONE);
            v.findViewById(R.id.close).setVisibility(View.GONE);
        }

        v.findViewById(R.id.close).setOnClickListener(view -> {
            view.setVisibility(View.GONE);
            v.findViewById(R.id.gallery_message).setVisibility(View.GONE);
            getActivity().getPreferences(MODE_PRIVATE).edit().putBoolean("closed_message", true).apply();
        });
        return v;
    }


}