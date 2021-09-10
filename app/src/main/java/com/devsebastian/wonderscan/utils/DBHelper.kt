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
package com.devsebastian.wonderscan.utils

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.devsebastian.wonderscan.data.Document
import com.devsebastian.wonderscan.data.Frame
import java.util.*

class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, VERSION) {
    private val db: SQLiteDatabase = writableDatabase
    private lateinit var onDocumentInsertListener: OnDocumentInsertListener
    private lateinit var onDocumentDeleteListener: OnDocumentDeleteListener
    private lateinit var onFrameDeleteListener: OnFrameDeleteListener
    private lateinit var onFrameUpdateListener: OnFrameUpdateListener

    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {
        val docQuery = "CREATE TABLE " + TABLE_NAME_DOCUMENTS + "(" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME + " TEXT, " +
                COL_DATE_TIME + " LONG);"
        val frameQuery = "CREATE TABLE " + TABLE_NAME_FRAMES + "(" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_DOC_ID + " LONG, " +
                COL_FRAME_INDEX + " LONG, " +
                COL_NAME + " TEXT, " +
                COL_DATE_TIME + " LONG, " +
                COL_NOTE + " TEXT, " +
                COL_OCR + " TEXT, " +
                COL_ANGLE + " INTEGER" + ")"
        val framePathQuery = "CREATE TABLE " + TABLE_NAME_FRAME_PATHS + "(" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_FRAME_ID + " LONG, " +
                COL_PATH + " TEXT, " +
                COL_PATH_TYPE + " INTEGER" + ");"
        sqLiteDatabase.execSQL(docQuery)
        sqLiteDatabase.execSQL(frameQuery)
        sqLiteDatabase.execSQL(framePathQuery)
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {}

    private fun createDocument(docName: String): Long {
        val cv = ContentValues()
        cv.put(COL_NAME, docName)
        cv.put(COL_DATE_TIME, System.currentTimeMillis())
        return db.insert(TABLE_NAME_DOCUMENTS, null, cv)
    }

    private fun createFrame(docId: Long, frame: Frame): Long {
        val cv = ContentValues()
        cv.put(COL_DOC_ID, docId)
        cv.put(COL_NAME, frame.name)
        cv.put(COL_DATE_TIME, frame.timeInMillis)
        cv.put(COL_NOTE, frame.note)
        cv.put(COL_OCR, frame.ocr)
        cv.put(COL_FRAME_INDEX, frame.index)
        cv.put(COL_ANGLE, frame.angle)
        return db.insert(TABLE_NAME_FRAMES, null, cv)
    }

    private fun createPath(frameId: Long, path: String?, type: Int) {
        val cv = ContentValues()
        cv.put(COL_FRAME_ID, frameId)
        cv.put(COL_PATH, path)
        cv.put(COL_PATH_TYPE, type)
        db.insert(TABLE_NAME_FRAME_PATHS, null, cv)
    }

    private fun getPath(frameId: Long, type: Int): String? {
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_NAME_FRAME_PATHS WHERE $COL_FRAME_ID=? AND $COL_PATH_TYPE=?",
            arrayOf(frameId.toString(), type.toString())
        )
        var path: String? = null
        if (cursor.moveToFirst()) {
            path = cursor.getString(cursor.getColumnIndex(COL_PATH))
        }
        cursor.close()
        return path
    }

    fun swapFrames(f1: Long, index1: Long, f2: Long, index2: Long) {
        val cv1 = ContentValues()
        cv1.put(COL_FRAME_INDEX, index1)
        db.update(TABLE_NAME_FRAMES, cv1, "$COL_ID=?", selectionArgs(f1))
        val cv2 = ContentValues()
        cv2.put(COL_FRAME_INDEX, index2)
        db.update(TABLE_NAME_FRAMES, cv2, "$COL_ID=?", selectionArgs(f2))
    }

    fun insertDocument(name: String): Long {
        val docId = createDocument(name)
        onDocumentInsertListener.onDocumentInsert(getDocument(docId))
        return docId
    }

    fun insertFrame(id: Long, frame: Frame): Long {
        return createFrame(id, frame)
    }

    fun deleteFrame(docId: Long, frameIndex: Long) {
        val frameId = getFrameId(docId, frameIndex)
        Utils.removeImageFromCache(getSourcePath(frameId))
        Utils.removeImageFromCache(getCroppedPath(frameId))
        Utils.removeImageFromCache(getEditedPath(frameId))
        db.delete(TABLE_NAME_FRAME_PATHS, "$COL_FRAME_ID=?", arrayOf(frameId.toString()))
        db.delete(TABLE_NAME_FRAMES, "$COL_ID=?", arrayOf(frameId.toString()))
        onFrameDeleteListener.onFrameDelete(frameIndex)
    }

    private fun updatePath(frameId: Long, path: String?, type: Int): Boolean {
        var updated = false
        val cv = ContentValues()
        cv.put(COL_PATH, path)
        val selectionArgs = selectionArgs(frameId, type)
        val c = db.query(
            TABLE_NAME_FRAME_PATHS,
            null,
            FIND_PATH_USING_FRAME_ID_AND_TYPE,
            selectionArgs,
            null,
            null,
            null
        )
        if (c.moveToFirst()) {
            db.update(TABLE_NAME_FRAME_PATHS, cv, FIND_PATH_USING_FRAME_ID_AND_TYPE, selectionArgs)
            updated = true
        }
        c.close()
        return updated
    }

    private fun insertPath(frameId: Long, path: String, type: Int) {
        if (!updatePath(frameId, path, type)) {
            createPath(frameId, path, type)
        }
        onFrameUpdateListener.onFrameUpdate(
            getFrameIndex(
                frameId
            )
        )
    }

    private fun insertPath(docId: Long, frameIndex: Long, path: String?, type: Int) {
        if (!updatePath(getFrameId(docId, frameIndex), path, type)) {
            createPath(getFrameId(docId, frameIndex), path, type)
        }
        onFrameUpdateListener.onFrameUpdate(frameIndex)
    }

    private fun getFrameId(docId: Long, frameIndex: Long): Long {
        var frameId: Long = -1
        val cursor = db.query(
            TABLE_NAME_FRAMES,
            null,
            FIND_FRAME_USING_DOC_ID_AND_INDEX,
            selectionArgs(docId, frameIndex),
            null,
            null,
            null
        )
        if (cursor.moveToFirst()) {
            frameId = cursor.getLong(cursor.getColumnIndex(COL_ID))
        }
        cursor.close()
        return frameId
    }

    private fun getFrameIndex(frameId: Long): Long {
        var frameIndex: Long = -1
        val cursor = db.query(
            TABLE_NAME_FRAME_PATHS,
            null,
            "$COL_FRAME_ID=?",
            selectionArgs(frameId),
            null,
            null,
            null
        )
        if (cursor.moveToFirst()) frameIndex = cursor.getLong(cursor.getColumnIndex(COL_ID))
        cursor.close()
        return frameIndex
    }

    private fun readFrame(cursor: Cursor): Frame {
        val frame = Frame()
        frame.id = cursor.getInt(cursor.getColumnIndex(COL_ID)).toLong()
        frame.timeInMillis = cursor.getLong(cursor.getColumnIndex(COL_DATE_TIME))
        frame.name = cursor.getString(cursor.getColumnIndex(COL_NAME))
        frame.index = cursor.getLong(cursor.getColumnIndex(COL_FRAME_INDEX))
        frame.note = cursor.getString(cursor.getColumnIndex(COL_NOTE))
        frame.ocr = cursor.getString(cursor.getColumnIndex(COL_OCR))
        frame.angle = cursor.getInt(cursor.getColumnIndex(COL_ANGLE))
        return frame
    }

    private fun readDocument(cursor: Cursor): Document {
        val document = Document()
        document.id = cursor.getInt(cursor.getColumnIndex(COL_ID)).toLong()
        document.dateTime = cursor.getLong(cursor.getColumnIndex(COL_DATE_TIME))
        document.name = cursor.getString(cursor.getColumnIndex(COL_NAME))
        return document
    }

    private fun selectionArgs(vararg args: Number): Array<String> {
        val selectionArgs = Array(args.size) { "" }
        for (i in args.indices) {
            val n = args[i]
            selectionArgs[i] = n.toString()
        }
        return selectionArgs
    }

    fun getSourcePath(frameId: Long): String? {
        return getPath(frameId, PATH_SOURCE)
    }

    fun getCroppedPath(frameId: Long): String? {
        return getPath(frameId, PATH_CROPPED)
    }

    fun getEditedPath(frameId: Long): String? {
        return getPath(frameId, PATH_EDITED)
    }

    fun renameDocument(id: Long, name: String) {
        val cv = ContentValues()
        cv.put(COL_NAME, name)
        db.update(TABLE_NAME_DOCUMENTS, cv, "$COL_ID=?", arrayOf(id.toString()))
    }

    fun updateSourcePath(frameId: Long, path: String) {
        insertPath(frameId, path, PATH_SOURCE)
    }

    fun updateCroppedPath(frameId: Long, path: String) {
        insertPath(frameId, path, PATH_CROPPED)
    }

    fun updateEditedPath(frameId: Long, path: String) {
        insertPath(frameId, path, PATH_EDITED)
    }

    fun updateEditedPath(docId: Long, frameIndex: Long, path: String?) {
        insertPath(docId, frameIndex, path, PATH_EDITED)
    }

    fun getPageCount(docId: Long): Long {
        val cursor = db.query(
            TABLE_NAME_FRAMES,
            arrayOf("COUNT($COL_ID) AS COUNT"),
            "$COL_DOC_ID=?",
            selectionArgs(docId),
            null,
            null,
            null
        )
        var count = 0L
        if (cursor.moveToFirst()) {
           count = cursor.getLong(cursor.getColumnIndex("COUNT"))
        }
        cursor.close()
        return count
    }

    fun getPath(frameId: Long): String? {
        var path = getPath(frameId, PATH_EDITED)
        if (path == null) {
            path = getPath(frameId, PATH_CROPPED)
        }
        if (path == null) {
            path = getPath(frameId, PATH_SOURCE)
        }
        return path
    }

    fun getFirstFrameImagePath(docId: Long): String? {
        val cursor = db.query(
            TABLE_NAME_FRAMES,
            null,
            FIND_FRAME_USING_DOC_ID_AND_INDEX,
            selectionArgs(docId, 0),
            null,
            null,
            null
        )
        var frameId: Long = -1
        if (cursor.moveToFirst()) {
            frameId = cursor.getLong(cursor.getColumnIndex(COL_ID))
        }
        cursor.close()
        return getSourcePath(frameId)
    }

    fun getAllDocuments(): ArrayList<Document> {
        val documents = ArrayList<Document>()
        val db = this.readableDatabase
        val res = db.rawQuery("SELECT * FROM $TABLE_NAME_DOCUMENTS", null)
        if (res.moveToLast()) {
            do {
                documents.add(readDocument(res))
            } while (res.moveToPrevious())
        }
        res.close()
        return documents
    }

    private fun deleteFrames(docId: Long) {
        val cursor = db.query(
            TABLE_NAME_FRAMES,
            arrayOf(COL_FRAME_INDEX),
            "$COL_DOC_ID=?",
            selectionArgs(docId),
            null,
            null,
            null
        )
        if (cursor.moveToFirst()) {
            do {
                val frameIndex = cursor.getLong(cursor.getColumnIndex(COL_FRAME_INDEX))
                deleteFrame(docId, frameIndex)
            } while (cursor.moveToNext())
        }
        cursor.close()
    }

    fun deleteDocument(docId: Long) {
        val db = this.writableDatabase
        deleteFrames(docId)
        db.delete(TABLE_NAME_DOCUMENTS, "$COL_ID = ? ", arrayOf(docId.toString()))
        onDocumentDeleteListener.onDocumentDelete(docId)
    }

    fun getDocument(id: Long): Document {
        val db = this.readableDatabase
        val q = "SELECT * FROM $TABLE_NAME_DOCUMENTS WHERE $COL_ID = $id"
        val cursor = db.rawQuery(q, null)
        var document = Document()
        if (cursor.moveToFirst()) {
            document = readDocument(cursor)
        }
        return document
    }

    fun getDocumentName(id: Long): String? {
        val db = this.readableDatabase
        val q =
            "SELECT $COL_NAME FROM $TABLE_NAME_DOCUMENTS WHERE $COL_ID = $id"
        val cursor = db.rawQuery(q, null)
        var name: String? = null
        if (cursor.moveToFirst()) {
            name = cursor.getString(cursor.getColumnIndex(COL_NAME))
        }
        cursor.close()
        return name
    }

    fun searchDocuments(query: String): ArrayList<Document> {
        val db = this.readableDatabase
        val q =
            "SELECT * FROM $TABLE_NAME_DOCUMENTS WHERE $COL_NAME LIKE '%$query%'"
        val cursor = db.rawQuery(q, null)
        var document: Document
        val documents = ArrayList<Document>()
        if (cursor.moveToLast()) {
            do {
                document = readDocument(cursor)
                documents.add(document)
            } while (cursor.moveToPrevious())
        }
        cursor.close()
        return documents
    }

    fun getAllFrames(docId: Long): ArrayList<Frame> {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_NAME_FRAMES,
            null,
            "$COL_DOC_ID=?",
            selectionArgs(docId),
            null,
            null,
            COL_FRAME_INDEX
        )
        val frames = ArrayList<Frame>()
        if (cursor.moveToFirst()) {
            do {
                val frame = readFrame(cursor)
                frames.add(frame)
            } while (cursor.moveToNext())
        }
        return frames
    }

    fun addNote(docId: Long, frameIndex: Long, note: String) {
        val cv = ContentValues()
        cv.put(COL_NOTE, note)
        db.update(
            TABLE_NAME_FRAMES,
            cv,
            "$COL_DOC_ID=? AND $COL_FRAME_INDEX=?",
            arrayOf(docId.toString(), frameIndex.toString())
        )
        onFrameUpdateListener.onFrameUpdate(frameIndex)
    }

    fun renameFrame(docId: Long, frameIndex: Long, name: String) {
        val cv = ContentValues()
        cv.put(COL_NAME, name)
        db.update(
            TABLE_NAME_FRAMES,
            cv,
            "$COL_DOC_ID=? AND $COL_FRAME_INDEX=?",
            arrayOf(docId.toString(), frameIndex.toString())
        )
        onFrameUpdateListener.onFrameUpdate(frameIndex)
    }

    fun setOnDocumentInsertListener(onDocumentInsertListener: OnDocumentInsertListener) {
        this.onDocumentInsertListener = onDocumentInsertListener
    }

    interface OnDocumentInsertListener {
        fun onDocumentInsert(document: Document)
    }

    interface OnDocumentDeleteListener {
        fun onDocumentDelete(index: Long)
    }

    interface OnFrameUpdateListener {
        fun onFrameUpdate(frameIndex: Long)
    }

    interface OnFrameDeleteListener {
        fun onFrameDelete(index: Long)
    }

    companion object {
        private const val DATABASE_NAME: String = "FramesDB"
        private const val VERSION = 1
        private const val TABLE_NAME_DOCUMENTS: String = "DOCUMENTS"
        private const val TABLE_NAME_FRAMES: String = "FRAMES"
        private const val TABLE_NAME_FRAME_PATHS: String = "FRAME_PATHS"
        private const val COL_ID: String = "_ID"
        private const val COL_DOC_ID: String = "DOC_ID"
        private const val COL_FRAME_ID: String = "FRAME_ID"
        private const val COL_NAME: String = "NAME"
        private const val COL_DATE_TIME: String = "DATETIME"
        private const val COL_PATH: String = "PATH"
        private const val COL_PATH_TYPE: String = "PATH_TYPE"
        private const val COL_NOTE: String = "NOTE"
        private const val COL_OCR: String = "OCR"
        private const val COL_FRAME_INDEX: String = "FRAME_INDEX"
        private const val COL_ANGLE: String = "ANGLE"
        private const val PATH_SOURCE = 0
        private const val PATH_CROPPED = 1
        private const val PATH_EDITED = 2
        private const val FIND_PATH_USING_FRAME_ID_AND_TYPE: String =
            "$COL_FRAME_ID=? AND $COL_PATH_TYPE=?"
        private const val FIND_FRAME_USING_DOC_ID_AND_INDEX: String =
            "$COL_DOC_ID=? AND $COL_FRAME_INDEX=?"
    }
}