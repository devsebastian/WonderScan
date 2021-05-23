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
package org.opencv.features2d;

import java.util.ArrayList;
import java.util.List;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.utils.Converters;

// C++: class BOWImgDescriptorExtractor
//javadoc: BOWImgDescriptorExtractor

public class BOWImgDescriptorExtractor {

    protected final long nativeObj;
    protected BOWImgDescriptorExtractor(long addr) { nativeObj = addr; }

    public long getNativeObjAddr() { return nativeObj; }

    // internal usage only
    public static BOWImgDescriptorExtractor __fromPtr__(long addr) { return new BOWImgDescriptorExtractor(addr); }

    //
    // C++:   cv::BOWImgDescriptorExtractor::BOWImgDescriptorExtractor(Ptr_DescriptorExtractor dextractor, Ptr_DescriptorMatcher dmatcher)
    //

    // Unknown type 'Ptr_DescriptorExtractor' (I), skipping the function


    //
    // C++:  Mat cv::BOWImgDescriptorExtractor::getVocabulary()
    //

    //javadoc: BOWImgDescriptorExtractor::getVocabulary()
    public  Mat getVocabulary()
    {
        
        Mat retVal = new Mat(getVocabulary_0(nativeObj));
        
        return retVal;
    }


    //
    // C++:  int cv::BOWImgDescriptorExtractor::descriptorSize()
    //

    //javadoc: BOWImgDescriptorExtractor::descriptorSize()
    public  int descriptorSize()
    {
        
        int retVal = descriptorSize_0(nativeObj);
        
        return retVal;
    }


    //
    // C++:  int cv::BOWImgDescriptorExtractor::descriptorType()
    //

    //javadoc: BOWImgDescriptorExtractor::descriptorType()
    public  int descriptorType()
    {
        
        int retVal = descriptorType_0(nativeObj);
        
        return retVal;
    }


    //
    // C++:  void cv::BOWImgDescriptorExtractor::compute2(Mat image, vector_KeyPoint keypoints, Mat& imgDescriptor)
    //

    //javadoc: BOWImgDescriptorExtractor::compute(image, keypoints, imgDescriptor)
    public  void compute(Mat image, MatOfKeyPoint keypoints, Mat imgDescriptor)
    {
        Mat keypoints_mat = keypoints;
        compute_0(nativeObj, image.nativeObj, keypoints_mat.nativeObj, imgDescriptor.nativeObj);
        
        return;
    }


    //
    // C++:  void cv::BOWImgDescriptorExtractor::setVocabulary(Mat vocabulary)
    //

    //javadoc: BOWImgDescriptorExtractor::setVocabulary(vocabulary)
    public  void setVocabulary(Mat vocabulary)
    {
        
        setVocabulary_0(nativeObj, vocabulary.nativeObj);
        
        return;
    }


    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }



    // C++:  Mat cv::BOWImgDescriptorExtractor::getVocabulary()
    private static native long getVocabulary_0(long nativeObj);

    // C++:  int cv::BOWImgDescriptorExtractor::descriptorSize()
    private static native int descriptorSize_0(long nativeObj);

    // C++:  int cv::BOWImgDescriptorExtractor::descriptorType()
    private static native int descriptorType_0(long nativeObj);

    // C++:  void cv::BOWImgDescriptorExtractor::compute2(Mat image, vector_KeyPoint keypoints, Mat& imgDescriptor)
    private static native void compute_0(long nativeObj, long image_nativeObj, long keypoints_mat_nativeObj, long imgDescriptor_nativeObj);

    // C++:  void cv::BOWImgDescriptorExtractor::setVocabulary(Mat vocabulary)
    private static native void setVocabulary_0(long nativeObj, long vocabulary_nativeObj);

    // native support for java finalize()
    private static native void delete(long nativeObj);

}
