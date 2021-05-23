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

package com.devsebastian.wonderscan.AsyncTask;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.AsyncTask;

import com.devsebastian.wonderscan.DBHelper;
import com.devsebastian.wonderscan.data.Frame;

import java.util.List;

public class ExportPdfTask extends AsyncTask<Object, Object, Object> {

    private final List<Frame> frames;
    private final OnCompleteListener onCompleteListener;
    private final DBHelper dbHelper;
    private PdfDocument pdf;

    public ExportPdfTask(Activity activity, List<Frame> frames, OnCompleteListener onCompleteListener) {
        this.frames = frames;
        dbHelper = new DBHelper(activity);
        this.onCompleteListener = onCompleteListener;
    }


    private void createPage(PdfDocument pdfDocument, Bitmap bitmap, int width, int height) {
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(width, height, 0).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        canvas.drawPaint(paint);

        canvas.drawBitmap(bitmap, (width - bitmap.getWidth()) / 2f, (height - bitmap.getHeight()) / 2f, null);
        pdfDocument.finishPage(page);
    }

    @Override
    protected Object doInBackground(Object... objects) {
        Bitmap bitmap;
        pdf = new PdfDocument();

        int width = 0, height = 0;
        for (Frame frame : frames) {
            bitmap = BitmapFactory.decodeFile(dbHelper.getEditedPath(frame.getId()));
            if (bitmap.getWidth() > width) width = bitmap.getWidth();
            if (bitmap.getHeight() > height) height = bitmap.getHeight();
            createPage(pdf, bitmap, width, height);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        onCompleteListener.onComplete(pdf);
    }


    public interface OnCompleteListener {
        void onComplete(PdfDocument document);
    }
}