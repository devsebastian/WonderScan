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
package com.wonderscan.android.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.wonderscan.android.R
import com.wonderscan.android.databinding.ActivityMainBinding
import com.wonderscan.android.fragment.GalleryFragment
import com.wonderscan.android.fragment.MainFragment
import com.wonderscan.android.fragment.SettingsFragment
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader

class MainActivity : BaseActivity() {
    private val mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            if (status == SUCCESS) {
                Log.i("OpenCV", "OpenCV loaded successfully")
            } else {
                super.onManagerConnected(status)
            }
        }
    }
    private val requiredPermissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private var mainFragment = MainFragment()
    private var galleryFragment = GalleryFragment()
    private var settingsFragment = SettingsFragment()
    private lateinit var binding: ActivityMainBinding

    public override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback)
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }

    private fun allPermissionsGranted(): Boolean {
        for (permission in requiredPermissions) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(findViewById(R.id.toolbar))
        title = ""
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, requiredPermissions, REQUEST_CODE_PERMISSIONS)
        }
        supportFragmentManager.beginTransaction().replace(R.id.fragment_holder, mainFragment)
            .commitNow()
        binding.bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            val transaction = supportFragmentManager.beginTransaction()
            when (item.itemId) {
                R.id.menu_home -> {
                    transaction.replace(R.id.fragment_holder, mainFragment)
                }
                R.id.menu_gallery -> {
                    transaction.replace(R.id.fragment_holder, galleryFragment)
                }
                R.id.menu_settings -> {
                    transaction.replace(R.id.fragment_holder, settingsFragment)
                }
            }
            transaction.commit()
            invalidateOptionsMenu()
            true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_search) {
            startActivity(Intent(this, SearchActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (mainFragment.isVisible) {
            menuInflater.inflate(R.menu.menu_fragment_main, menu)
        } else {
            menuInflater.inflate(R.menu.menu_fragment_gallery, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onBackPressed() {
        val selectedItemId = binding.bottomNavigationView.selectedItemId
        if (R.id.menu_home != selectedItemId) {
            binding.bottomNavigationView.selectedItemId = R.id.menu_home
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 1001
    }
}