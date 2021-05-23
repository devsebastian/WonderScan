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

package com.devsebastian.wonderscan.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.Magnifier;

import androidx.annotation.Nullable;

import com.devsebastian.wonderscan.R;
import com.devsebastian.wonderscan.data.BoundingRect;

import org.opencv.core.Point;

public class CropView extends androidx.appcompat.widget.AppCompatImageView {

    private final Paint line;
    private final Paint circleStroke;
    private final Paint fill;
    private BoundingRect boundingRect;
    private int move;
    private int width, height;

    public CropView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        line = new Paint();
        fill = new Paint();
        circleStroke = new Paint();

        int color = context.getColor(R.color.colorAccent);
        int strokeWidth = 8;
        int circleStrokeWidth = 6;

        boundingRect = new BoundingRect();

        line.setStrokeWidth(strokeWidth);
        line.setAntiAlias(true);
        line.setStyle(Paint.Style.STROKE);
        line.setColor(color);

        circleStroke.setStrokeWidth(circleStrokeWidth);
        circleStroke.setAntiAlias(true);
        circleStroke.setStyle(Paint.Style.STROKE);
        circleStroke.setColor(color);

        fill.setColor(Color.parseColor("#66FFFFFF"));
    }

    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        super.setLayoutParams(params);
        width = params.width;
        height = params.height;
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
    }

    public BoundingRect getBoundingRect() {
        return boundingRect;
    }

    public void setBoundingRect(BoundingRect boundingRect) {
        if (boundingRect != null) {
            this.boundingRect = boundingRect;
            invalidate();
        } else {
            this.setDefaultPositions();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Point TL = boundingRect.getTopLeft();
        Point TR = boundingRect.getTopRight();
        Point BL = boundingRect.getBottomLeft();
        Point BR = boundingRect.getBottomRight();

        canvas.drawLine((float) (TL.x), (float) (TL.y), (float) (TR.x), (float) (TR.y), line);
        canvas.drawLine((float) (TR.x), (float) (TR.y), (float) (BR.x), (float) (BR.y), line);
        canvas.drawLine((float) (BR.x), (float) (BR.y), (float) (BL.x), (float) (BL.y), line);
        canvas.drawLine((float) (BL.x), (float) (BL.y), (float) (TL.x), (float) (TL.y), line);


        drawPin(canvas, boundingRect.getTopLeft());
        drawPin(canvas, boundingRect.getTopRight());
        drawPin(canvas, boundingRect.getBottomLeft());
        drawPin(canvas, boundingRect.getBottomRight());
        drawPin(canvas, boundingRect.getTop());
        drawPin(canvas, boundingRect.getBottom());
        drawPin(canvas, boundingRect.getLeft());
        drawPin(canvas, boundingRect.getRight());
    }

    void drawPin(Canvas canvas, Point point) {
        int inner = 15;
        canvas.drawCircle((float) point.x, (float) point.y, inner * 2, fill);
        canvas.drawCircle((float) point.x, (float) point.y, inner * 2, circleStroke);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                move = getSelectPinNumber(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                float x = event.getX();
                float y = event.getY();
                if (move < 8 && isInside(x, y)) {
                    setPosition(x, y);
                }
                break;
        }
        return true;
    }


    boolean isInside(float x, float y) {
        return x > 0 && x < width && y > 0 && y < height;
    }

    int getSelectPinNumber(float x, float y) {
        int move;
        if (collides(x, y, boundingRect.getTopLeft())) move = 0;
        else if (collides(x, y, boundingRect.getTopRight())) move = 1;
        else if (collides(x, y, boundingRect.getBottomRight())) move = 2;
        else if (collides(x, y, boundingRect.getBottomLeft())) move = 3;
        else if (collides(x, y, boundingRect.getLeft())) move = 4;
        else if (collides(x, y, boundingRect.getTop())) move = 5;
        else if (collides(x, y, boundingRect.getRight())) move = 6;
        else if (collides(x, y, boundingRect.getBottom())) move = 7;
        else move = 8;
        return move;
    }

    public void setDefaultPositions() {
        int inner = 0;
        boundingRect.setTopLeft(new Point(inner, inner));
        boundingRect.setTopRight(new Point(width - inner, inner));
        boundingRect.setBottomLeft(new Point(inner, height - inner));
        boundingRect.setBottomRight(new Point(width - inner, height - inner));
        invalidate();
    }

    void setPosition(float x, float y) {
        switch (move) {
            case 0:
                boundingRect.setTopLeft(new Point(x, y));
                invalidate();
                break;
            case 1:
                boundingRect.setTopRight(new Point(x, y));
                invalidate();
                break;
            case 2:
                boundingRect.setBottomRight(new Point(x, y));
                invalidate();
                break;
            case 3:
                boundingRect.setBottomLeft(new Point(x, y));
                invalidate();
                break;
            case 4:
                boundingRect.setLeft(new Point(x, y));
                invalidate();
                break;
            case 5:
                boundingRect.setTop(new Point(x, y));
                invalidate();
                break;
            case 6:
                boundingRect.setRight(new Point(x, y));
                invalidate();
                break;
            case 7:
                boundingRect.setBottom(new Point(x, y));
                invalidate();
                break;
        }
    }

    boolean collides(float x, float y, Point point) {
        int outer = 50;
        return x < point.x + outer && x > point.x - outer &&
                y < point.y + outer && y > point.y - outer;
    }
}
