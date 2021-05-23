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

//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.video;

import org.opencv.core.Algorithm;
import org.opencv.core.Mat;

// C++: class SparseOpticalFlow
//javadoc: SparseOpticalFlow

public class SparseOpticalFlow extends Algorithm {

    protected SparseOpticalFlow(long addr) { super(addr); }

    // internal usage only
    public static SparseOpticalFlow __fromPtr__(long addr) { return new SparseOpticalFlow(addr); }

    //
    // C++:  void cv::SparseOpticalFlow::calc(Mat prevImg, Mat nextImg, Mat prevPts, Mat& nextPts, Mat& status, Mat& err = cv::Mat())
    //

    //javadoc: SparseOpticalFlow::calc(prevImg, nextImg, prevPts, nextPts, status, err)
    public  void calc(Mat prevImg, Mat nextImg, Mat prevPts, Mat nextPts, Mat status, Mat err)
    {
        
        calc_0(nativeObj, prevImg.nativeObj, nextImg.nativeObj, prevPts.nativeObj, nextPts.nativeObj, status.nativeObj, err.nativeObj);
        
        return;
    }

    //javadoc: SparseOpticalFlow::calc(prevImg, nextImg, prevPts, nextPts, status)
    public  void calc(Mat prevImg, Mat nextImg, Mat prevPts, Mat nextPts, Mat status)
    {
        
        calc_1(nativeObj, prevImg.nativeObj, nextImg.nativeObj, prevPts.nativeObj, nextPts.nativeObj, status.nativeObj);
        
        return;
    }


    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }



    // C++:  void cv::SparseOpticalFlow::calc(Mat prevImg, Mat nextImg, Mat prevPts, Mat& nextPts, Mat& status, Mat& err = cv::Mat())
    private static native void calc_0(long nativeObj, long prevImg_nativeObj, long nextImg_nativeObj, long prevPts_nativeObj, long nextPts_nativeObj, long status_nativeObj, long err_nativeObj);
    private static native void calc_1(long nativeObj, long prevImg_nativeObj, long nextImg_nativeObj, long prevPts_nativeObj, long nextPts_nativeObj, long status_nativeObj);

    // native support for java finalize()
    private static native void delete(long nativeObj);

}
