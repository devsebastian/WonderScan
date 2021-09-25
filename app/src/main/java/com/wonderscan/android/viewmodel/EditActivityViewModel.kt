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

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.wonderscan.android.WonderScanApp
import com.wonderscan.android.dao.FrameDao
import com.wonderscan.android.data.Frame

class EditActivityViewModel(
    application: WonderScanApp,
    private val frameDao: FrameDao
) : AndroidViewModel(application) {
    var frame: LiveData<Frame>? = null

    fun getFrame(frameId: Long): LiveData<Frame>? {
        frame = frameDao.getFrame(frameId)
        return frame
    }
}

class EditActivityViewModelFactory(
    private val application: WonderScanApp,
    private val frameDao: FrameDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return EditActivityViewModel(application, frameDao) as T
    }
}