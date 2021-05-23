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

package com.devsebastian.wonderscan;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.devsebastian.wonderscan.data.Document;
import com.devsebastian.wonderscan.data.Frame;

import java.util.ArrayList;

import static com.devsebastian.wonderscan.Utils.removeImageFromCache;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "FramesDB";
    private static final int VERSION = 1;

    private static final String TABLE_NAME_DOCUMENTS = "DOCUMENTS";
    private static final String TABLE_NAME_FRAMES = "FRAMES";
    private static final String TABLE_NAME_FRAME_PATHS = "FRAME_PATHS";

    private static final String COL_ID = "_ID";
    private static final String COL_DOC_ID = "DOC_ID";
    private static final String COL_FRAME_ID = "FRAME_ID";

    private static final String COL_NAME = "NAME";
    private static final String COL_DATE_TIME = "DATETIME";
    private static final String COL_PATH = "PATH";
    private static final String COL_PATH_TYPE = "PATH_TYPE";
    private static final String COL_NOTE = "NOTE";
    private static final String COL_OCR = "OCR";
    private static final String COL_FRAME_INDEX = "FRAME_INDEX";
    private static final String COL_ANGLE = "ANGLE";

    private static final int PATH_SOURCE = 0;
    private static final int PATH_CROPPED = 1;
    private static final int PATH_EDITED = 2;


    private static final String FIND_PATH_USING_FRAME_ID_AND_TYPE = COL_FRAME_ID + "=? AND " + COL_PATH_TYPE + "=?";
    private static final String FIND_FRAME_USING_DOC_ID_AND_INDEX = COL_DOC_ID + "=? AND " + COL_FRAME_INDEX + "=?";

    private final SQLiteDatabase db;

    private OnDocumentInsertListener onDocumentInsertListener;
    private OnDocumentDeleteListener onDocumentDeleteListener;

    private OnFrameInsertListener onFrameInsertListener;
    private OnFrameDeleteListener onFrameDeleteListener;
    private OnFrameUpdateListener onFrameUpdateListener;

    public DBHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, VERSION);
        db = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String docQuery = "CREATE TABLE " + TABLE_NAME_DOCUMENTS + "(" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME + " TEXT, " +
                COL_DATE_TIME + " LONG);";

        String frameQuery = "CREATE TABLE " + TABLE_NAME_FRAMES + "(" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_DOC_ID + " LONG, " +
                COL_FRAME_INDEX + " LONG, " +
                COL_NAME + " TEXT, " +
                COL_DATE_TIME + " LONG, " +
                COL_NOTE + " TEXT, " +
                COL_OCR + " TEXT, " +
                COL_ANGLE + " INTEGER" + ")";

        String framePathQuery = "CREATE TABLE " + TABLE_NAME_FRAME_PATHS + "(" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_FRAME_ID + " LONG, " +
                COL_PATH + " TEXT, " +
                COL_PATH_TYPE + " INTEGER" + ");";

        sqLiteDatabase.execSQL(docQuery);
        sqLiteDatabase.execSQL(frameQuery);
        sqLiteDatabase.execSQL(framePathQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    private long _createDocument(String docName) {
        ContentValues cv = new ContentValues();
        cv.put(COL_NAME, docName);
        cv.put(COL_DATE_TIME, System.currentTimeMillis());
        return db.insert(TABLE_NAME_DOCUMENTS, null, cv);
    }

    private long _createFrame(long docId, Frame frame) {
        ContentValues cv = new ContentValues();
        cv.put(COL_DOC_ID, docId);
        cv.put(COL_NAME, frame.getName());
        cv.put(COL_DATE_TIME, frame.getTimeInMillis());
        cv.put(COL_NOTE, frame.getNote());
        cv.put(COL_OCR, frame.getOCR());
        cv.put(COL_FRAME_INDEX, frame.getIndex());
        cv.put(COL_ANGLE, frame.getAngle());

        return db.insert(TABLE_NAME_FRAMES, null, cv);
    }

    private void _createPath(long frameId, String path, int type) {
        ContentValues cv = new ContentValues();
        cv.put(COL_FRAME_ID, frameId);
        cv.put(COL_PATH, path);
        cv.put(COL_PATH_TYPE, type);
        db.insert(TABLE_NAME_FRAME_PATHS, null, cv);
    }

    private String _getPath(long frameId, int type) {
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME_FRAME_PATHS + " WHERE " + COL_FRAME_ID + "=? AND " + COL_PATH_TYPE + "=?", new String[]{Long.toString(frameId), Integer.toString(type)});
        if (cursor.moveToFirst()) {
            return cursor.getString(cursor.getColumnIndex(COL_PATH));
        }
        cursor.close();
        return null;
    }

    public void swapFrames(long f1, long index1, long f2, long index2) {
        ContentValues cv1 = new ContentValues();
        cv1.put(COL_FRAME_INDEX, index1);
        db.update(TABLE_NAME_FRAMES, cv1, COL_ID + "=?", _selectionArgs(f1));

        ContentValues cv2 = new ContentValues();
        cv2.put(COL_FRAME_INDEX, index2);
        db.update(TABLE_NAME_FRAMES, cv2, COL_ID + "=?", _selectionArgs(f2));
    }

    public long insertDocument(String name) {
        long docId = _createDocument(name);
        if (onDocumentInsertListener != null)
            onDocumentInsertListener.onDocumentInsert(getDocument(docId));
        return docId;
    }

    public long insertFrame(long id, Frame frame) {
        return _createFrame(id, frame);
    }

    public void deleteFrame(long docId, long frameIndex) {
        long frameId = _getFrameId(docId, frameIndex);
        removeImageFromCache(getSourcePath(frameId));
        removeImageFromCache(getCroppedPath(frameId));
        removeImageFromCache(getEditedPath(frameId));
        db.delete(TABLE_NAME_FRAME_PATHS, COL_FRAME_ID + "=?", new String[]{String.valueOf(frameId)});
        db.delete(TABLE_NAME_FRAMES, COL_ID + "=?", new String[]{String.valueOf(frameId)});
        if (onFrameDeleteListener != null)
            onFrameDeleteListener.onFrameDelete(frameIndex);
    }

    private boolean _updatePath(long frameId, String path, int type) {
        boolean updated = false;
        ContentValues cv = new ContentValues();
        cv.put(COL_PATH, path);
        final String[] SELECTION_ARGS = _selectionArgs(frameId, type);
        Cursor c = db.query(TABLE_NAME_FRAME_PATHS, null, FIND_PATH_USING_FRAME_ID_AND_TYPE, SELECTION_ARGS, null, null, null);
        if (c.moveToFirst()) {
            db.update(TABLE_NAME_FRAME_PATHS, cv, FIND_PATH_USING_FRAME_ID_AND_TYPE, SELECTION_ARGS);
            updated = true;
        }
        c.close();
        return updated;
    }

    private void _insertPath(long frameId, String path, int type) {
        if (!_updatePath(frameId, path, type)) {
            _createPath(frameId, path, type);
        }
        if (onFrameUpdateListener != null)
            onFrameUpdateListener.onFrameUpdate(_getFrameIndex(frameId));
    }

    private void _insertPath(long docId, long frameIndex, String path, int type) {
        if (!_updatePath(_getFrameId(docId, frameIndex), path, type)) {
            _createPath(_getFrameId(docId, frameIndex), path, type);
        }
        if (onFrameUpdateListener != null) {
            onFrameUpdateListener.onFrameUpdate(frameIndex);
        }
    }

    private long _getFrameId(long docId, long frameIndex) {
        long frameId = -1;
        Cursor cursor = db.query(TABLE_NAME_FRAMES, null, FIND_FRAME_USING_DOC_ID_AND_INDEX, _selectionArgs(docId, frameIndex), null, null, null);
        if (cursor.moveToFirst()) {
            frameId = cursor.getLong(cursor.getColumnIndex(COL_ID));
        }
        cursor.close();
        return frameId;
    }

    private long _getFrameIndex(long frameId) {
        long frameIndex = -1;
        Cursor cursor = db.query(TABLE_NAME_FRAME_PATHS, null, COL_FRAME_ID + "=?", _selectionArgs(frameId), null, null, null);
        if (cursor.moveToFirst())
            frameIndex = cursor.getLong(cursor.getColumnIndex(COL_ID));
        cursor.close();
        return frameIndex;
    }

    private Frame _readFrame(Cursor cursor) {
        Frame frame = new Frame();
        frame.setId(cursor.getInt(cursor.getColumnIndex(COL_ID)));
        frame.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(COL_DATE_TIME)));
        frame.setName(cursor.getString(cursor.getColumnIndex(COL_NAME)));
        frame.setIndex(cursor.getLong(cursor.getColumnIndex(COL_FRAME_INDEX)));
        frame.setNote(cursor.getString(cursor.getColumnIndex(COL_NOTE)));
        frame.setOCR(cursor.getString(cursor.getColumnIndex(COL_OCR)));
        frame.setAngle(cursor.getInt(cursor.getColumnIndex(COL_ANGLE)));
        return frame;
    }

    private Document _readDocument(Cursor cursor) {
        Document document = new Document();
        document.setId(cursor.getInt(cursor.getColumnIndex(COL_ID)));
        document.setDateTime(cursor.getLong(cursor.getColumnIndex(COL_DATE_TIME)));
        document.setName(cursor.getString(cursor.getColumnIndex(COL_NAME)));
        return document;
    }

    private String[] _selectionArgs(Number... args) {
        String[] selectionArgs = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            Number n = args[i];
            selectionArgs[i] = String.valueOf(n);
        }
        return selectionArgs;
    }

    public String getSourcePath(long frameId) {
        return _getPath(frameId, PATH_SOURCE);
    }

    public String getCroppedPath(long frameId) {
        return _getPath(frameId, PATH_CROPPED);
    }

    public String getEditedPath(long frameId) {
        return _getPath(frameId, PATH_EDITED);
    }

    public void renameDocument(long id, String name) {
        ContentValues cv = new ContentValues();
        cv.put(COL_NAME, name);
        db.update(TABLE_NAME_DOCUMENTS, cv, COL_ID + "=?", new String[]{Long.toString(id)});
    }


    public void updateSourcePath(long frameId, String path) {
        _insertPath(frameId, path, PATH_SOURCE);
    }

    public void updateCroppedPath(long frameId, String path) {
        _insertPath(frameId, path, PATH_CROPPED);
    }

    public void updateEditedPath(long frameId, String path) {
        _insertPath(frameId, path, PATH_EDITED);
    }

    public void updateSourcePath(long docId, long frameIndex, String path) {
        _insertPath(docId, frameIndex, path, PATH_SOURCE);
    }

    public void updateCroppedPath(long docId, long frameIndex, String path) {
        _insertPath(docId, frameIndex, path, PATH_CROPPED);
    }

    public void updateEditedPath(long docId, long frameIndex, String path) {
        _insertPath(docId, frameIndex, path, PATH_EDITED);
    }

    public long getPageCount(long docId) {
        Cursor cursor = db.query(TABLE_NAME_FRAMES, new String[]{"COUNT(" + COL_ID + ") AS COUNT"}, COL_DOC_ID + "=?", _selectionArgs(docId), null, null, null);
        if (cursor.moveToFirst()) {
            return cursor.getLong(cursor.getColumnIndex("COUNT"));
        }
        return 0;
    }

    public String getPath(long frameId) {
        String path = _getPath(frameId, PATH_EDITED);
        if (path == null) {
            path = _getPath(frameId, PATH_CROPPED);
        }
        if (path == null) {
            path = _getPath(frameId, PATH_SOURCE);
        }
        return path;
    }

    public String getFirstFrameImagePath(long docId) {
        Cursor cursor = db.query(TABLE_NAME_FRAMES, null, FIND_FRAME_USING_DOC_ID_AND_INDEX, _selectionArgs(docId, 0), null, null, null);
        long frameId = -1;
        if (cursor.moveToFirst()) {
            frameId = cursor.getLong(cursor.getColumnIndex(COL_ID));
        }
        cursor.close();
        return getSourcePath(frameId);
    }

    public ArrayList<Document> getAllDocuments() {
        ArrayList<Document> documents = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + TABLE_NAME_DOCUMENTS, null);
        if (res.moveToLast()) {
            do {
                documents.add(_readDocument(res));
            } while (res.moveToPrevious());
        }
        res.close();
        return documents;
    }

    public void deleteFrames(long docId) {
        Cursor cursor = db.query(TABLE_NAME_FRAMES, new String[]{COL_FRAME_INDEX}, COL_DOC_ID + "=?", _selectionArgs(docId), null, null, null);
        if (cursor.moveToFirst()) {
            do {
                long frameIndex = cursor.getLong(cursor.getColumnIndex(COL_FRAME_INDEX));
                deleteFrame(docId, frameIndex);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    public void deleteDocument(long docId) {
        SQLiteDatabase db = this.getWritableDatabase();
        deleteFrames(docId);
        db.delete(TABLE_NAME_DOCUMENTS, COL_ID + " = ? ", new String[]{Long.toString(docId)});
        if (onDocumentDeleteListener != null)
            onDocumentDeleteListener.onDocumentDelete(docId);
    }

    public Document getDocument(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String q = "SELECT * FROM " + TABLE_NAME_DOCUMENTS + " WHERE " + COL_ID + " = " + id;
        Cursor cursor = db.rawQuery(q, null);
        Document document = new Document();
        if (cursor.moveToFirst()) {
            document = _readDocument(cursor);
        }
        return document;
    }

    public String getDocumentName(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String q = "SELECT " + COL_NAME + " FROM " + TABLE_NAME_DOCUMENTS + " WHERE " + COL_ID + " = " + id;
        Cursor cursor = db.rawQuery(q, null);
        if (cursor.moveToFirst()) {
            return cursor.getString(cursor.getColumnIndex(COL_NAME));
        }
        return null;
    }

    public ArrayList<Document> searchDocuments(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        String q = "SELECT * FROM " + TABLE_NAME_DOCUMENTS + " WHERE " + COL_NAME + " LIKE '%" + query + "%'";
        Cursor cursor = db.rawQuery(q, null);
        Document document;
        ArrayList<Document> documents = new ArrayList<>();
        if (cursor.moveToLast()) {
            do {
                document = _readDocument(cursor);
                documents.add(document);
            } while (cursor.moveToPrevious());
        }
        cursor.close();
        return documents;
    }

    public ArrayList<Frame> getAllFrames(long docId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME_FRAMES, null, COL_DOC_ID + "=?", _selectionArgs(docId), null, null, COL_FRAME_INDEX);
        ArrayList<Frame> frames = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                Frame frame = _readFrame(cursor);
                frames.add(frame);
            } while (cursor.moveToNext());
        }
        return frames;
    }

    public void addNote(long docId, long frameIndex, String note) {
        ContentValues cv = new ContentValues();
        cv.put(COL_NOTE, note);
        db.update(TABLE_NAME_FRAMES, cv, COL_DOC_ID + "=? AND " + COL_FRAME_INDEX + "=?", new String[]{Long.toString(docId), Long.toString(frameIndex)});
        if (onFrameUpdateListener != null) onFrameUpdateListener.onFrameUpdate(frameIndex);
    }

    public void renameFrame(long docId, long frameIndex, String name) {
        ContentValues cv = new ContentValues();
        cv.put(COL_NAME, name);
        db.update(TABLE_NAME_FRAMES, cv, COL_DOC_ID + "=? AND " + COL_FRAME_INDEX + "=?", new String[]{Long.toString(docId), Long.toString(frameIndex)});
        if (onFrameUpdateListener != null) onFrameUpdateListener.onFrameUpdate(frameIndex);
    }

    public void setOnDocumentDeleteListener(OnDocumentDeleteListener onDocumentDeleteListener) {
        this.onDocumentDeleteListener = onDocumentDeleteListener;
    }

    public void setOnDocumentInsertListener(OnDocumentInsertListener onDocumentInsertListener) {
        this.onDocumentInsertListener = onDocumentInsertListener;
    }

    public void setOnFrameInsertListener(OnFrameInsertListener onFrameInsertListener) {
        this.onFrameInsertListener = onFrameInsertListener;
    }

    public void setOnFrameDeleteListener(OnFrameDeleteListener onFrameDeleteListener) {
        this.onFrameDeleteListener = onFrameDeleteListener;
    }

    public void setOnFrameUpdateListener(OnFrameUpdateListener onFrameUpdateListener) {
        this.onFrameUpdateListener = onFrameUpdateListener;
    }

    public interface OnDocumentInsertListener {
        void onDocumentInsert(Document document);
    }

    public interface OnDocumentDeleteListener {
        void onDocumentDelete(long index);
    }

    public interface OnFrameUpdateListener {
        void onFrameUpdate(long frameIndex);
    }

    public interface OnFrameDeleteListener {
        void onFrameDelete(long index);
    }

    public interface OnFrameInsertListener {
        void onFrameInsert(Frame frame);
    }

}
