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
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy;
import com.devsebastian.wonderscan.DBHelper;
import com.devsebastian.wonderscan.R;
import com.devsebastian.wonderscan.activity.ListFramesActivity;
import com.devsebastian.wonderscan.data.Document;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DocumentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_NORMAL = 0;
    private static final int TYPE_FOOTER = 1;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM, yyyy hh:mm", Locale.getDefault());
    Context context;
    int maxWidth;
    DBHelper dbHelper;
    private ArrayList<Document> data;

    // RecyclerView recyclerView;
    public DocumentsAdapter(Activity activity, ArrayList<Document> frames) {
        this.data = frames;
        this.context = activity;
        dbHelper = new DBHelper(context);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int deviceWidth = displayMetrics.widthPixels;
        maxWidth = deviceWidth / 4;

    }

    public void updateDocuments(ArrayList<Document> documents) {
        this.data = documents;
        data.add(new Document());
        notifyDataSetChanged();
    }

    public void insertDocument(Document document) {
        this.data.add(0, document);
        notifyItemInserted(0);
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem;
        if (viewType == TYPE_NORMAL) {
            listItem = layoutInflater.inflate(R.layout.row_document, parent, false);
            return new NormalViewHolder(listItem);
        } else {
            listItem = layoutInflater.inflate(R.layout.row_documents_footer, parent, false);
            return new FooterViewholder(listItem);
        }
    }

    @Override
    public void onBindViewHolder(@NotNull RecyclerView.ViewHolder holder, int position) {
        if (position < data.size()) {
            final Document document = data.get(position);
            NormalViewHolder h = (NormalViewHolder) holder;
            h.title.setText(document.getName());
            h.subtitle.setText(simpleDateFormat.format(new Date(document.getDateTime())));
            h.sheetNumber.setText(String.format(Locale.getDefault(), "%d pages", dbHelper.getPageCount(document.getId())));
            holder.itemView.setOnClickListener((view) -> {
                Intent intent = new Intent(context, ListFramesActivity.class);
                intent.putExtra(context.getString(R.string.intent_document_id), document.getId());
                context.startActivity(intent);
            });
            Glide.with(context).load(dbHelper.getFirstFrameImagePath(document.getId())).downsample(DownsampleStrategy.AT_MOST).into(h.imageView);
        } else {
            holder.itemView.setOnClickListener(v -> {
                shareAppLink(context);
            });
        }
    }

    public void shareAppLink(Context context) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain").
                putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_message));
        context.startActivity(intent);
    }


    @Override
    public int getItemViewType(int position) {
        if (position == data.size()) {
            return TYPE_FOOTER;
        } else {
            return TYPE_NORMAL;
        }
    }

    @Override
    public int getItemCount() {
        return data.size() + 1;
    }

    public static class NormalViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView title, subtitle;
        TextView sheetNumber;

        public NormalViewHolder(View itemView) {
            super(itemView);
            this.imageView = itemView.findViewById(R.id.iv_frame);
            this.title = itemView.findViewById(R.id.tv_title);
            this.subtitle = itemView.findViewById(R.id.tv_sub_title);
            this.sheetNumber = itemView.findViewById(R.id.tv_number);
        }
    }

    public static class FooterViewholder extends RecyclerView.ViewHolder {

        public FooterViewholder(View itemView) {
            super(itemView);
        }
    }
}