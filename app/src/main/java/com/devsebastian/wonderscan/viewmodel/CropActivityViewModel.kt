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

package com.devsebastian.wonderscan.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.*
import com.devsebastian.wonderscan.MyApplication
import com.devsebastian.wonderscan.dao.FrameDao
import com.devsebastian.wonderscan.data.BoundingRect
import com.devsebastian.wonderscan.data.Document
import com.devsebastian.wonderscan.data.Frame
import com.devsebastian.wonderscan.utils.DetectBox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.opencv.core.Point


class CropActivityViewModel(
    application: MyApplication,
    frameDao: FrameDao,
    frameId: Long
) : AndroidViewModel(application) {
    var frame: LiveData<Frame> = frameDao.getFrame(frameId)
    private var boundingRect: MutableLiveData<BoundingRect> = MutableLiveData()

    fun getBoundingRect(bitmap: Bitmap, ratio: Double): LiveData<BoundingRect> {
        viewModelScope.launch(Dispatchers.Default) {
            var boundingRect = DetectBox.findCorners(bitmap, 0)
            if (boundingRect == null) {
                val width = bitmap.width
                val height = bitmap.height
                val padding = width * 0.1
                boundingRect = BoundingRect()
                boundingRect.topLeft = Point(padding * ratio, padding * ratio)
                boundingRect.topRight = Point((width - padding) * ratio, padding * ratio)
                boundingRect.bottomLeft = Point(padding * ratio, (height - padding) * ratio)
                boundingRect.bottomRight =
                    Point((width - padding) * ratio, (height - padding) * ratio)
            }
        }
        return boundingRect
    }

}

class CropActivityViewModelFactory(
    private val application: MyApplication,
    private val frameDao: FrameDao,
    private val frameId: Long
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return CropActivityViewModel(application, frameDao, frameId) as T
    }
}