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

package com.devsebastian.wonderscan.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.devsebastian.wonderscan.DBHelper;
import com.devsebastian.wonderscan.R;
import com.devsebastian.wonderscan.Utils;
import com.devsebastian.wonderscan.activity.ListFramesActivity;
import com.devsebastian.wonderscan.activity.ViewPageActivity;
import com.devsebastian.wonderscan.data.Frame;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class ProgressFramesAdapter extends RecyclerView.Adapter<ProgressFramesAdapter.ViewHolder> {
    Activity activity;
    long docId;
    int maxWidth;
    DBHelper dbHelper;
    private List<Frame> data;

    // RecyclerView recyclerView;
    public ProgressFramesAdapter(Activity activity, long docId, List<Frame> frames) {
        this.activity = activity;
        this.data = frames;
        this.docId = docId;
        dbHelper = new DBHelper(activity);
        maxWidth = Utils.getDeviceWidth(activity) / 2;
    }

    public void swap(int from, int to) {
        Collections.swap(data, from, to);
        notifyItemMoved(from, to);
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.row_frame, parent, false);
        return new ViewHolder(listItem);
    }

    public void setFrames(List<Frame> frames) {
        this.data = frames;
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Frame frame = data.get(position);
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(activity, ViewPageActivity.class);
            intent.putExtra(activity.getString(R.string.intent_document_id), docId);
            intent.putExtra(activity.getString(R.string.intent_frame_position), position);
            activity.startActivityForResult(intent, ListFramesActivity.VIEW_PAGE_ACTIVITY);
        });
        if (frame.getName() == null || frame.getName().isEmpty()) {
            holder.textView.setText(String.valueOf(position + 1));
        } else {
            holder.textView.setText(frame.getName());
        }

        Glide.with(activity).load(dbHelper.getPath(frame.getId())).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(holder.imageView);

        if (dbHelper.getEditedPath(frame.getId()) == null) {
            holder.progressBar.setVisibility(View.VISIBLE);
        } else {
//            holder.imageView.setImageBitmap(matToBitmap(imread(dbHelper.getEditedPath(frame.getId()))));
            holder.progressBar.setVisibility(View.GONE);
        }
    }


    @Override
    public int getItemCount() {
        if (data != null)
            return data.size();
        else return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView textView;
        public View progressBar;

        public ViewHolder(View itemView) {
            super(itemView);
            this.imageView = itemView.findViewById(R.id.iv_frame);
            this.textView = itemView.findViewById(R.id.tv_frame);
            this.progressBar = itemView.findViewById(R.id.progress);

            itemView.setOnLongClickListener(view -> {
                Vibrator v = (Vibrator) itemView.getContext().getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(50);
                return false;
            });
        }
    }
}