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

import org.opencv.features2d.BFMatcher;
import org.opencv.features2d.DescriptorMatcher;

// C++: class BFMatcher
//javadoc: BFMatcher

public class BFMatcher extends DescriptorMatcher {

    protected BFMatcher(long addr) { super(addr); }

    // internal usage only
    public static BFMatcher __fromPtr__(long addr) { return new BFMatcher(addr); }

    //
    // C++:   cv::BFMatcher::BFMatcher(int normType = NORM_L2, bool crossCheck = false)
    //

    //javadoc: BFMatcher::BFMatcher(normType, crossCheck)
    public   BFMatcher(int normType, boolean crossCheck)
    {
        
        super( BFMatcher_0(normType, crossCheck) );
        
        return;
    }

    //javadoc: BFMatcher::BFMatcher(normType)
    public   BFMatcher(int normType)
    {
        
        super( BFMatcher_1(normType) );
        
        return;
    }

    //javadoc: BFMatcher::BFMatcher()
    public   BFMatcher()
    {
        
        super( BFMatcher_2() );
        
        return;
    }


    //
    // C++: static Ptr_BFMatcher cv::BFMatcher::create(int normType = NORM_L2, bool crossCheck = false)
    //

    //javadoc: BFMatcher::create(normType, crossCheck)
    public static BFMatcher create(int normType, boolean crossCheck)
    {
        
        BFMatcher retVal = BFMatcher.__fromPtr__(create_0(normType, crossCheck));
        
        return retVal;
    }

    //javadoc: BFMatcher::create(normType)
    public static BFMatcher create(int normType)
    {
        
        BFMatcher retVal = BFMatcher.__fromPtr__(create_1(normType));
        
        return retVal;
    }

    //javadoc: BFMatcher::create()
    public static BFMatcher create()
    {
        
        BFMatcher retVal = BFMatcher.__fromPtr__(create_2());
        
        return retVal;
    }


    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }



    // C++:   cv::BFMatcher::BFMatcher(int normType = NORM_L2, bool crossCheck = false)
    private static native long BFMatcher_0(int normType, boolean crossCheck);
    private static native long BFMatcher_1(int normType);
    private static native long BFMatcher_2();

    // C++: static Ptr_BFMatcher cv::BFMatcher::create(int normType = NORM_L2, bool crossCheck = false)
    private static native long create_0(int normType, boolean crossCheck);
    private static native long create_1(int normType);
    private static native long create_2();

    // native support for java finalize()
    private static native void delete(long nativeObj);

}
