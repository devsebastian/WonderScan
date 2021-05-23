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
 * Installation callback interface.
 */
public interface InstallCallbackInterface
{
    /**
     * New package installation is required.
     */
    static final int NEW_INSTALLATION = 0;
    /**
     * Current package installation is in progress.
     */
    static final int INSTALLATION_PROGRESS = 1;

    /**
     * Target package name.
     * @return Return target package name.
     */
    public String getPackageName();
    /**
     * Installation is approved.
     */
    public void install();
    /**
     * Installation is canceled.
     */
    public void cancel();
    /**
     * Wait for package installation.
     */
    public void wait_install();
};
