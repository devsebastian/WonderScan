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

package com.wonderscan.android.viewmodel

import androidx.lifecycle.*
import com.wonderscan.android.WonderScanApp
import com.wonderscan.android.dao.FrameDao
import com.wonderscan.android.data.BoundingRect
import com.wonderscan.android.data.Frame


class CropActivityViewModel(
    application: WonderScanApp,
    frameDao: FrameDao,
    frameId: Long
) : AndroidViewModel(application) {
    var frame: LiveData<Frame> = frameDao.getFrame(frameId)
    private var boundingRect: MutableLiveData<BoundingRect> = MutableLiveData()

    // TODO
}

class CropActivityViewModelFactory(
    private val application: WonderScanApp,
    private val frameDao: FrameDao,
    private val frameId: Long
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return CropActivityViewModel(application, frameDao, frameId) as T
    }
}