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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.devsebastian.wonderscan.R;
import com.devsebastian.wonderscan.data.BoundingRect;

import org.opencv.core.Point;

public class ScanView extends androidx.appcompat.widget.AppCompatImageView {

    private final Paint line;
    private final Paint circleStroke;
    private final Paint fill;
    private BoundingRect dynamicBoundingRect;
    private Handler delayedHandler;
    private Runnable delayedRunnable;

    public ScanView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        line = new Paint();
        fill = new Paint();
        circleStroke = new Paint();
        int color = context.getColor(R.color.colorAccent);

        dynamicBoundingRect = new BoundingRect();

        int strokeWidth = 8;
        line.setStrokeWidth(strokeWidth);
        line.setAntiAlias(true);
        line.setStyle(Paint.Style.STROKE);
        line.setColor(color);

        int circleStrokeWidth = 6;
        circleStroke.setStrokeWidth(circleStrokeWidth);
        circleStroke.setAntiAlias(true);
        circleStroke.setStyle(Paint.Style.STROKE);
        circleStroke.setColor(Color.WHITE);

        fill.setColor(context.getColor(R.color.colorAccent));
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
    }

    private void _setBoundingRect(BoundingRect boundingRect) {
        dynamicBoundingRect.TOP_LEFT = new Point((dynamicBoundingRect.TOP_LEFT.x + boundingRect.TOP_LEFT.x) / 2,
                (dynamicBoundingRect.TOP_LEFT.y + boundingRect.TOP_LEFT.y) / 2);

        dynamicBoundingRect.TOP_RIGHT = new Point((dynamicBoundingRect.TOP_RIGHT.x + boundingRect.TOP_RIGHT.x) / 2,
                (dynamicBoundingRect.TOP_RIGHT.y + boundingRect.TOP_RIGHT.y) / 2);

        dynamicBoundingRect.BOTTOM_LEFT = new Point((dynamicBoundingRect.BOTTOM_LEFT.x + boundingRect.BOTTOM_LEFT.x) / 2,
                (dynamicBoundingRect.BOTTOM_LEFT.y + boundingRect.BOTTOM_LEFT.y) / 2);

        dynamicBoundingRect.BOTTOM_RIGHT = new Point((dynamicBoundingRect.BOTTOM_RIGHT.x + boundingRect.BOTTOM_RIGHT.x) / 2,
                (dynamicBoundingRect.BOTTOM_RIGHT.y + boundingRect.BOTTOM_RIGHT.y) / 2);
    }

    public void setBoundingRect(BoundingRect boundingRect) {

        if (boundingRect != null) {
            if (delayedHandler != null) {
                removeDelayedHandler();
            }
            dynamicBoundingRect = boundingRect;
//            _setBoundingRect(boundingRect);
            invalidate();
        } else {
            if (delayedHandler == null) {
                startDelayedHandler();
            }
        }
    }

    private void startDelayedHandler() {
        delayedHandler = new Handler();
        if (delayedRunnable == null)
            delayedRunnable = () -> {
                this.dynamicBoundingRect = new BoundingRect(-100);
                invalidate();
                removeDelayedHandler();
            };
        delayedHandler.postDelayed(delayedRunnable, 100);
    }

    private void removeDelayedHandler() {
        delayedHandler.removeCallbacks(delayedRunnable);
        delayedHandler = null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Point TL = dynamicBoundingRect.getTopLeft();
        Point TR = dynamicBoundingRect.getTopRight();
        Point BL = dynamicBoundingRect.getBottomLeft();
        Point BR = dynamicBoundingRect.getBottomRight();
        canvas.drawLine((float) (TL.x), (float) (TL.y), (float) (TR.x), (float) (TR.y), line);
        canvas.drawLine((float) (TR.x), (float) (TR.y), (float) (BR.x), (float) (BR.y), line);
        canvas.drawLine((float) (BR.x), (float) (BR.y), (float) (BL.x), (float) (BL.y), line);
        canvas.drawLine((float) (BL.x), (float) (BL.y), (float) (TL.x), (float) (TL.y), line);

        drawPin(canvas, dynamicBoundingRect.getTopLeft());
        drawPin(canvas, dynamicBoundingRect.getTopRight());
        drawPin(canvas, dynamicBoundingRect.getBottomLeft());
        drawPin(canvas, dynamicBoundingRect.getBottomRight());
        drawPin(canvas, dynamicBoundingRect.getTop());
        drawPin(canvas, dynamicBoundingRect.getBottom());
        drawPin(canvas, dynamicBoundingRect.getLeft());
        drawPin(canvas, dynamicBoundingRect.getRight());
    }

    void drawPin(Canvas canvas, Point point) {
        int inner = 12;
        canvas.drawCircle((float) point.x, (float) point.y, inner * 2, fill);
        canvas.drawCircle((float) point.x, (float) point.y, inner * 2, circleStroke);
    }
}
