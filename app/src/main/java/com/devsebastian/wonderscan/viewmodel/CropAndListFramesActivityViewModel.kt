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

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.net.Uri
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.lifecycle.*
import com.devsebastian.wonderscan.WonderScanApp
import com.devsebastian.wonderscan.R
import com.devsebastian.wonderscan.dao.DocumentDao
import com.devsebastian.wonderscan.dao.FrameDao
import com.devsebastian.wonderscan.data.Document
import com.devsebastian.wonderscan.data.Frame
import com.devsebastian.wonderscan.utils.ExportPdf
import com.devsebastian.wonderscan.utils.Utils.cropAndFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class CropAndListFramesActivityViewModel(
    private val application: WonderScanApp,
    private val documentDao: DocumentDao,
    private val frameDao: FrameDao
) : AndroidViewModel(application) {
    var count: LiveData<Int> = MutableLiveData(0)
    var document: Document = Document()
    var frames: LiveData<MutableList<Frame>> = frameDao.getFrames(document.id)

    private fun getName(): String {
        val simpleDateFormat =
            SimpleDateFormat("dd-MMM-yyyy hh:mm:ss", Locale.getDefault())
        return application.getString(R.string.app_name) + " " + simpleDateFormat.format(Date())
    }

    fun setup(paths: MutableList<String>) {
        val docName: String = getName()
        document.name = docName
        document.dateTime = System.currentTimeMillis()
        viewModelScope.launch(Dispatchers.IO) {
            documentDao.insert(document)
            getFramesFromImagePaths(paths).let { frames ->
                for (frame in frames) {
                    viewModelScope.launch(Dispatchers.Default) {
                        cropAndFormat(frame, application, frameDao)
                    }
                }
            }
        }
    }

    fun exportPdf(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val pdf = ExportPdf.exportPdf(frameDao.getFramesSync(document.id))
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

    fun showConfirmDeleteDialog(activity: Activity) {
        AlertDialog.Builder(application).apply {
            setTitle("Confirm Delete")
            setMessage("Are you sure you want to delete this document. You won't be able to recover the document later!")
            setNegativeButton("Cancel", null)
            setPositiveButton("Delete") { _, _ ->
                documentDao.delete(document)
                if (!activity.isFinishing) activity.finish()
            }
            create().show()
        }
    }


    fun showRenameDialog() {
        viewModelScope.launch(Dispatchers.Main) {
            val frameLayout = FrameLayout(application)
            val editText = EditText(application).apply {
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(50, 12, 50, 12)
                }
                setText(document.name)
            }
            frameLayout.addView(editText)
            AlertDialog.Builder(application).apply {
                setTitle("Rename")
                setView(frameLayout)
                setNegativeButton("Cancel", null)
                setPositiveButton("Save") { _: DialogInterface?, _: Int ->
                    document.name = editText.text.toString()
                    documentDao.update(document)
                }
                create().show()
            }
        }
    }

    private fun getFramesFromImagePaths(
        paths: MutableList<String>,
    ): MutableList<Frame> {
        val frames = ArrayList<Frame>()
        for (i in paths.indices) {
            val sourcePath = paths[i]
            val frame = Frame(
                timeInMillis = System.currentTimeMillis(),
                index = i,
                docId = document.id,
                uri = sourcePath,
                angle = 0
            )
            frame.id = frameDao.insert(frame)
            frames.add(frame)
        }
        return frames
    }

}

class CropAndListFramesActivityViewModelFactory(
    private val application: WonderScanApp,
    private val documentDao: DocumentDao,
    private val frameDao: FrameDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return CropAndListFramesActivityViewModel(application, documentDao, frameDao) as T
    }
}