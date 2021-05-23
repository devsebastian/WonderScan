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

// C++: class DenseOpticalFlow
//javadoc: DenseOpticalFlow

public class DenseOpticalFlow extends Algorithm {

    protected DenseOpticalFlow(long addr) { super(addr); }

    // internal usage only
    public static DenseOpticalFlow __fromPtr__(long addr) { return new DenseOpticalFlow(addr); }

    //
    // C++:  void cv::DenseOpticalFlow::calc(Mat I0, Mat I1, Mat& flow)
    //

    //javadoc: DenseOpticalFlow::calc(I0, I1, flow)
    public  void calc(Mat I0, Mat I1, Mat flow)
    {
        
        calc_0(nativeObj, I0.nativeObj, I1.nativeObj, flow.nativeObj);
        
        return;
    }


    //
    // C++:  void cv::DenseOpticalFlow::collectGarbage()
    //

    //javadoc: DenseOpticalFlow::collectGarbage()
    public  void collectGarbage()
    {
        
        collectGarbage_0(nativeObj);
        
        return;
    }


    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }



    // C++:  void cv::DenseOpticalFlow::calc(Mat I0, Mat I1, Mat& flow)
    private static native void calc_0(long nativeObj, long I0_nativeObj, long I1_nativeObj, long flow_nativeObj);

    // C++:  void cv::DenseOpticalFlow::collectGarbage()
    private static native void collectGarbage_0(long nativeObj);

    // native support for java finalize()
    private static native void delete(long nativeObj);

}
