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

package org.opencv.core;

import java.util.Arrays;
import java.util.List;

import org.opencv.core.KeyPoint;

public class MatOfKeyPoint extends Mat {
    // 32FC7
    private static final int _depth = CvType.CV_32F;
    private static final int _channels = 7;

    public MatOfKeyPoint() {
        super();
    }

    protected MatOfKeyPoint(long addr) {
        super(addr);
        if( !empty() && checkVector(_channels, _depth) < 0 )
            throw new IllegalArgumentException("Incompatible Mat");
        //FIXME: do we need release() here?
    }

    public static MatOfKeyPoint fromNativeAddr(long addr) {
        return new MatOfKeyPoint(addr);
    }

    public MatOfKeyPoint(Mat m) {
        super(m, Range.all());
        if( !empty() && checkVector(_channels, _depth) < 0 )
            throw new IllegalArgumentException("Incompatible Mat");
        //FIXME: do we need release() here?
    }

    public MatOfKeyPoint(KeyPoint...a) {
        super();
        fromArray(a);
    }

    public void alloc(int elemNumber) {
        if(elemNumber>0)
            super.create(elemNumber, 1, CvType.makeType(_depth, _channels));
    }

    public void fromArray(KeyPoint...a) {
        if(a==null || a.length==0)
            return;
        int num = a.length;
        alloc(num);
        float buff[] = new float[num * _channels];
        for(int i=0; i<num; i++) {
            KeyPoint kp = a[i];
            buff[_channels*i+0] = (float) kp.pt.x;
            buff[_channels*i+1] = (float) kp.pt.y;
            buff[_channels*i+2] = kp.size;
            buff[_channels*i+3] = kp.angle;
            buff[_channels*i+4] = kp.response;
            buff[_channels*i+5] = kp.octave;
            buff[_channels*i+6] = kp.class_id;
        }
        put(0, 0, buff); //TODO: check ret val!
    }

    public KeyPoint[] toArray() {
        int num = (int) total();
        KeyPoint[] a = new KeyPoint[num];
        if(num == 0)
            return a;
        float buff[] = new float[num * _channels];
        get(0, 0, buff); //TODO: check ret val!
        for(int i=0; i<num; i++)
            a[i] = new KeyPoint( buff[_channels*i+0], buff[_channels*i+1], buff[_channels*i+2], buff[_channels*i+3],
                                 buff[_channels*i+4], (int) buff[_channels*i+5], (int) buff[_channels*i+6] );
        return a;
    }

    public void fromList(List<KeyPoint> lkp) {
        KeyPoint akp[] = lkp.toArray(new KeyPoint[0]);
        fromArray(akp);
    }

    public List<KeyPoint> toList() {
        KeyPoint[] akp = toArray();
        return Arrays.asList(akp);
    }
}
