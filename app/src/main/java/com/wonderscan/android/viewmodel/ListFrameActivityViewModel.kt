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

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.*
import com.wonderscan.android.WonderScanApp
import com.wonderscan.android.dao.DocumentDao
import com.wonderscan.android.dao.FrameDao
import com.wonderscan.android.data.Document
import com.wonderscan.android.data.Frame
import com.wonderscan.android.utils.ExportPdf
import com.wonderscan.android.utils.Filter
import com.wonderscan.android.utils.Utils
import com.wonderscan.android.utils.Utils.cropAndFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.opencv.imgcodecs.Imgcodecs
import java.io.IOException


class ListFrameActivityViewModel(
    private val application: WonderScanApp,
    private val documentDao: DocumentDao,
    private val frameDao: FrameDao,
    private val docId: String
) : AndroidViewModel(application) {
    var count: LiveData<Int> = MutableLiveData(0)
    var document: LiveData<Document> = documentDao.getDocument(docId)
    var frames: LiveData<MutableList<Frame>> = frameDao.getFrames(docId)

    fun processUnprocessedFrames(docId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            frameDao.getFramesSync(docId).let { frames ->
                for (i in frames.indices) {
                    if (frames[i].editedUri == null) {
                        val frame = frames[i]
                        viewModelScope.launch(Dispatchers.Default) {
                            if (frame.croppedUri == null)
                                cropAndFormat(frame, application, frameDao)
                            else processFrame(application, frame)
                        }
                    }
                }
            }
        }
    }

    private fun processFrame(context: Context, frame: Frame) {
        val file = Utils.createPhotoFile(context)
        val mat = Imgcodecs.imread(frame.croppedUri)
        val editedMat = Filter.magicColor(mat)
        Imgcodecs.imwrite(file.absolutePath, editedMat)
        frame.editedUri = file.absolutePath
        frameDao.update(frame)
        mat.release()
        editedMat.release()
    }

    fun update(frames: List<Frame>) {
        viewModelScope.launch(Dispatchers.IO) {
            for (i in frames.indices) {
                frameDao.update(frames[i])
            }
        }
    }

    fun delete() {
        viewModelScope.launch(Dispatchers.IO) {
            documentDao.delete(docId)
        }
    }

    suspend fun getDocument(): Document {
        return documentDao.getDocumentSync(docId)
    }

    fun updateDocument(document: Document) {
        viewModelScope.launch(Dispatchers.IO) {
            documentDao.update(document)
        }
    }

    fun sendCreateFileIntent(type: String?, resultLauncher: ActivityResultLauncher<Intent>) {
        viewModelScope.launch(Dispatchers.IO) {
            val name = documentDao.getDocumentName(docId)
            Intent(Intent.ACTION_CREATE_DOCUMENT).let {
                it.addCategory(Intent.CATEGORY_OPENABLE)
                it.type = type
                it.putExtra(Intent.EXTRA_TITLE, name)
                resultLauncher.launch(it)
            }
        }
    }

    fun exportPdf(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val pdf = ExportPdf.exportPdf(frameDao.getFramesSync(docId))
            try {
                pdf.writeTo(application.contentResolver.openOutputStream(uri))
                viewModelScope.launch(Dispatchers.Main) {
                    Toast.makeText(
                        application,
                        "PDF document saved in " + uri.path,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}

class ListFrameActivityViewModelFactory(
    private val application: WonderScanApp,
    private val documentDao: DocumentDao,
    private val frameDao: FrameDao,
    private val docId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ListFrameActivityViewModel(application, documentDao, frameDao, docId) as T
    }
}