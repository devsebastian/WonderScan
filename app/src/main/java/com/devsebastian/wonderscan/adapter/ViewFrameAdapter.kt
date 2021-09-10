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
package com.devsebastian.wonderscan.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.devsebastian.wonderscan.utils.DBHelper
import com.devsebastian.wonderscan.R
import com.devsebastian.wonderscan.utils.Utils
import com.devsebastian.wonderscan.data.Frame
import com.jsibbold.zoomage.ZoomageView

class ViewFrameAdapter(private val activity: Activity, private val frames: MutableList<Frame>) :
    PagerAdapter() {
    private var dbHelper: DBHelper = DBHelper(activity)

    override fun getCount(): Int {
        return frames.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val frame = frames[position]
        @SuppressLint("InflateParams") val v =
            activity.layoutInflater.inflate(R.layout.row_page, null)
        val imageView: ZoomageView = v.findViewById(R.id.ssiv_page)
        //        Glide.with(activity).load(dbHelper.getPath(frame.getId())).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(imageView); TODO check if it works better
        imageView.setImageBitmap(Utils.getBitmapFromMat(Utils.readMat(dbHelper.getPath(frame.id))))
        container.addView(v)
        return v
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

}