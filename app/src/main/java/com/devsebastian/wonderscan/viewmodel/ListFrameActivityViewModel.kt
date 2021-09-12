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

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.*
import com.devsebastian.wonderscan.MyApplication
import com.devsebastian.wonderscan.dao.DocumentDao
import com.devsebastian.wonderscan.dao.FrameDao
import com.devsebastian.wonderscan.data.Document
import com.devsebastian.wonderscan.data.Frame
import com.devsebastian.wonderscan.utils.ExportPdf
import com.devsebastian.wonderscan.utils.Filter
import com.devsebastian.wonderscan.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.opencv.imgcodecs.Imgcodecs
import java.io.IOException


class ListFrameActivityViewModel(
    private val application: MyApplication,
    private val documentDao: DocumentDao,
    private val frameDao: FrameDao,
    private val docId: String
) : AndroidViewModel(application) {
    var count: LiveData<Int> = MutableLiveData(0)
    var document: LiveData<Document> = documentDao.getDocument(docId)
    var frames: LiveData<MutableList<Frame>> = frameDao.getFrames(docId)

    fun processUnprocessedFrames(docId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val frames = frameDao.getFramesSync(docId)
            for (i in frames.indices) {
                if (frames[i].editedUri == null) {
                    val frame = frames[i]
                    processFrame(application, frame)
                }
            }
        }
    }

    fun swap(from: Frame, to: Frame) {
        val i = from.index
        from.index = to.index
        to.index = i
        viewModelScope.launch(Dispatchers.IO) {
            frameDao.update(from)
            frameDao.update(to)
        }
    }

    fun update(frames: List<Frame>) {
        viewModelScope.launch(Dispatchers.IO) {
            for(i in frames.indices) {
                frames[i].index = i
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
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = type
            intent.putExtra(Intent.EXTRA_TITLE, name)
            resultLauncher.launch(intent)
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
                    )
                        .show()
                }
            } catch (e: IOException) {
                e.printStackTrace()
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
}

class ListFrameActivityViewModelFactory(
    private val application: MyApplication,
    private val documentDao: DocumentDao,
    private val frameDao: FrameDao,
    private val docId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ListFrameActivityViewModel(application, documentDao, frameDao, docId) as T
    }
}