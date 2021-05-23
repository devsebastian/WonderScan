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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy;
import com.devsebastian.wonderscan.R;

import java.util.ArrayList;
import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {
	Context context;
	private List<String> data;
	private final List<Integer> positions;

	public void reset() {
		positions.clear();
		notifyDataSetChanged();
	}

	// RecyclerView recyclerView;
	public GalleryAdapter(Context context, List<String> frames) {
		this.context = context;
		this.data = frames;
		positions = new ArrayList<>();
	}

	public void setImagePaths(List<String> data) {
		this.data = data;
	}

	public void clearSelection() {
		positions.clear();
	}

	public ArrayList<String> getSelectedUris() {
		ArrayList<String> uris = new ArrayList<>();
		for (int pos : positions) {
			uris.add(data.get(pos));
		}
		return uris;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
		View listItem = layoutInflater.inflate(R.layout.row_image, parent, false);
		ViewHolder viewHolder = new ViewHolder(listItem);
		return viewHolder;
	}

	public void updateFrames(ArrayList<String> frames) {
		this.data = frames;
		notifyDataSetChanged();
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		final String uri = data.get(position);
		Glide.with(context).load(uri).centerCrop().downsample(DownsampleStrategy.AT_MOST).into(holder.imageView);
		if (positions.contains(position)) {
			holder.imageView.setAlpha(0.5f);
			holder.bubble.setText(String.valueOf(positions.indexOf(position) + 1));
			holder.bubble.setVisibility(View.VISIBLE);
		} else {
			holder.imageView.setAlpha(1f);
			holder.bubble.setVisibility(View.GONE);
		}
		holder.imageView.setOnClickListener(v -> {
			if (positions.contains(holder.getAdapterPosition())) {
				positions.remove(Integer.valueOf(holder.getAdapterPosition()));
				notifyDataSetChanged();
			} else {
				positions.add(holder.getAdapterPosition());
				notifyItemChanged(holder.getAdapterPosition());
			}
		});
	}


	@Override
	public int getItemCount() {
		return data.size();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		public ImageView imageView;
		TextView bubble;

		public ViewHolder(View itemView) {
			super(itemView);
			this.imageView = itemView.findViewById(R.id.imageview);
			this.bubble = itemView.findViewById(R.id.iv_bubble);
		}
	}
}