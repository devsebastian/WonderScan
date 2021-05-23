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


public class MatOfRect extends Mat {
    // 32SC4
    private static final int _depth = CvType.CV_32S;
    private static final int _channels = 4;

    public MatOfRect() {
        super();
    }

    protected MatOfRect(long addr) {
        super(addr);
        if( !empty() && checkVector(_channels, _depth) < 0 )
            throw new IllegalArgumentException("Incompatible Mat");
        //FIXME: do we need release() here?
    }

    public static MatOfRect fromNativeAddr(long addr) {
        return new MatOfRect(addr);
    }

    public MatOfRect(Mat m) {
        super(m, Range.all());
        if( !empty() && checkVector(_channels, _depth) < 0 )
            throw new IllegalArgumentException("Incompatible Mat");
        //FIXME: do we need release() here?
    }

    public MatOfRect(Rect...a) {
        super();
        fromArray(a);
    }

    public void alloc(int elemNumber) {
        if(elemNumber>0)
            super.create(elemNumber, 1, CvType.makeType(_depth, _channels));
    }

    public void fromArray(Rect...a) {
        if(a==null || a.length==0)
            return;
        int num = a.length;
        alloc(num);
        int buff[] = new int[num * _channels];
        for(int i=0; i<num; i++) {
            Rect r = a[i];
            buff[_channels*i+0] = (int) r.x;
            buff[_channels*i+1] = (int) r.y;
            buff[_channels*i+2] = (int) r.width;
            buff[_channels*i+3] = (int) r.height;
        }
        put(0, 0, buff); //TODO: check ret val!
    }


    public Rect[] toArray() {
        int num = (int) total();
        Rect[] a = new Rect[num];
        if(num == 0)
            return a;
        int buff[] = new int[num * _channels];
        get(0, 0, buff); //TODO: check ret val!
        for(int i=0; i<num; i++)
            a[i] = new Rect(buff[i*_channels], buff[i*_channels+1], buff[i*_channels+2], buff[i*_channels+3]);
        return a;
    }
    public void fromList(List<Rect> lr) {
        Rect ap[] = lr.toArray(new Rect[0]);
        fromArray(ap);
    }

    public List<Rect> toList() {
        Rect[] ar = toArray();
        return Arrays.asList(ar);
    }
}
