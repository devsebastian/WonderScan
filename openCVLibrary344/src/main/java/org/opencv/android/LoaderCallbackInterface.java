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

package org.opencv.android;

/**
 * Interface for callback object in case of asynchronous initialization of OpenCV.
 */
public interface LoaderCallbackInterface
{
    /**
     * OpenCV initialization finished successfully.
     */
    static final int SUCCESS = 0;
    /**
     * Google Play Market cannot be invoked.
     */
    static final int MARKET_ERROR = 2;
    /**
     * OpenCV library installation has been canceled by the user.
     */
    static final int INSTALL_CANCELED = 3;
    /**
     * This version of OpenCV Manager Service is incompatible with the app. Possibly, a service update is required.
     */
    static final int INCOMPATIBLE_MANAGER_VERSION = 4;
    /**
     * OpenCV library initialization has failed.
     */
    static final int INIT_FAILED = 0xff;

    /**
     * Callback method, called after OpenCV library initialization.
     * @param status status of initialization (see initialization status constants).
     */
    public void onManagerConnected(int status);

    /**
     * Callback method, called in case the package installation is needed.
     * @param callback answer object with approve and cancel methods and the package description.
     */
    public void onPackageInstall(final int operation, InstallCallbackInterface callback);
};
