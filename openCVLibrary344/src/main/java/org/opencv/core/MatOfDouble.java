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

public class MatOfDouble extends Mat {
    // 64FC(x)
    private static final int _depth = CvType.CV_64F;
    private static final int _channels = 1;

    public MatOfDouble() {
        super();
    }

    protected MatOfDouble(long addr) {
        super(addr);
        if( !empty() && checkVector(_channels, _depth) < 0 )
            throw new IllegalArgumentException("Incompatible Mat");
        //FIXME: do we need release() here?
    }

    public static MatOfDouble fromNativeAddr(long addr) {
        return new MatOfDouble(addr);
    }

    public MatOfDouble(Mat m) {
        super(m, Range.all());
        if( !empty() && checkVector(_channels, _depth) < 0 )
            throw new IllegalArgumentException("Incompatible Mat");
        //FIXME: do we need release() here?
    }

    public MatOfDouble(double...a) {
        super();
        fromArray(a);
    }

    public void alloc(int elemNumber) {
        if(elemNumber>0)
            super.create(elemNumber, 1, CvType.makeType(_depth, _channels));
    }

    public void fromArray(double...a) {
        if(a==null || a.length==0)
            return;
        int num = a.length / _channels;
        alloc(num);
        put(0, 0, a); //TODO: check ret val!
    }

    public double[] toArray() {
        int num = checkVector(_channels, _depth);
        if(num < 0)
            throw new RuntimeException("Native Mat has unexpected type or size: " + toString());
        double[] a = new double[num * _channels];
        if(num == 0)
            return a;
        get(0, 0, a); //TODO: check ret val!
        return a;
    }

    public void fromList(List<Double> lb) {
        if(lb==null || lb.size()==0)
            return;
        Double ab[] = lb.toArray(new Double[0]);
        double a[] = new double[ab.length];
        for(int i=0; i<ab.length; i++)
            a[i] = ab[i];
        fromArray(a);
    }

    public List<Double> toList() {
        double[] a = toArray();
        Double ab[] = new Double[a.length];
        for(int i=0; i<a.length; i++)
            ab[i] = a[i];
        return Arrays.asList(ab);
    }
}
