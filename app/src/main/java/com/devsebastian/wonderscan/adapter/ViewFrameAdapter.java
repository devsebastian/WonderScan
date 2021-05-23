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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.devsebastian.wonderscan.DBHelper;
import com.devsebastian.wonderscan.R;
import com.devsebastian.wonderscan.Utils;
import com.devsebastian.wonderscan.data.Frame;
import com.jsibbold.zoomage.ZoomageView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.devsebastian.wonderscan.Utils.getBitmapFromMat;
import static com.devsebastian.wonderscan.Utils.readMat;
import static org.opencv.android.Utils.matToBitmap;
import static org.opencv.imgcodecs.Imgcodecs.imread;

public class ViewFrameAdapter extends PagerAdapter {

    private final List<Frame> frames;
    private final Activity activity;
    private final DBHelper dbHelper;

    public ViewFrameAdapter(Activity activity, List<Frame> frames) {
        this.activity = activity;
        this.frames = frames;
        dbHelper = new DBHelper(activity);
    }

    @Override
    public int getCount() {
        if (frames != null)
            return frames.size();
        else return 0;
    }

    @Override
    public boolean isViewFromObject(View view, @NotNull Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Frame frame = frames.get(position);
        @SuppressLint("InflateParams") View v = activity.getLayoutInflater().inflate(R.layout.row_page, null);
        ZoomageView imageView = v.findViewById(R.id.ssiv_page);
//        Glide.with(activity).load(dbHelper.getPath(frame.getId())).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(imageView); TODO check if it works better
        imageView.setImageBitmap(getBitmapFromMat(readMat(dbHelper.getPath(frame.getId()))));
        container.addView(v);
        return v;
    }


    @Override
    public void destroyItem(ViewGroup container, int position, @NotNull Object object) {
        container.removeView((View) object);
    }
}
