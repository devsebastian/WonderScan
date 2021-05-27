# WonderScan
WonderScan is a Document Scanning Mobile application developed in India. All data is stored in your device and no data is sent to any server! 
Many different filters are available ranging from simple Black and White to Magic color. 
various other parameters like the brightness and contrast can also be controlled quite easilly.

You can download the app from: https://play.google.com/store/apps/details?id=com.devsebastian.wonderscan

<p float="left">
<img width="16%" src="https://play-lh.googleusercontent.com/-RfQx1yEJbR1bPaXqdt1v7ad_8mmlWHF0aRM7i0IsMEe4c480n4VMyrdHM7sL9msOO05=w720-h310-rw"/>
<img width="16%" src="https://play-lh.googleusercontent.com/lWEzOTpEj4IngZ-CZP3USUPmSrYKIILb9DgYKZIQBni8qbz1Bi1AyIbh4_G5WHoqlWU=w720-h310-rw"/>
<img width="16%" src="https://play-lh.googleusercontent.com/p3Yyl02Rq68YlzgW-eWzKEgepOIyk60ho9RCjZr-gfH9AokRzTJ_P7AgcPmBagIcW0nL=w720-h310-rw"/>
<img width="16%" src="https://play-lh.googleusercontent.com/ecrLgCAbafcRD1CwvDc_GZMgnaEKxhJTlYcmz0s4FzOOO4qCjMs4tghmMNBCsSaoEdeT=w720-h310-rw"/>
<img width="16%" src="https://play-lh.googleusercontent.com/GppLzH6eYZ4i6bMJzbbZh-S2gnpFqU64Dr1rvAgkyF_duX6f07fF5iRjQqp5_TDSqA=w720-h310-rw"/>
<img width="16%" src="https://play-lh.googleusercontent.com/_zYDDmcuD-JB3c-D1Bl71vhbEt9-vZa1SLuHc9w1SlrSoWaqEUuD0HxpX0uTY4jD6Qk=w720-h310-rw"/>
</p>

## Features of WonderScan:
1. Import Documents from Camera or Gallery
2. Crop Images and apply perspective-wrap to give a 2D effect
3. Apply many Filters.
4. Save as Documents.
5. Add any number of pages to existing Documents.
6. Re-arrange pages of existing document.
6. Create PDFs
7. perform OCR and read text of scanned documents
8. Save a not for each frame or document
9. Control Brightness and Contrast of each document


## Contributing 

If you want to contribute we suggest that you start with [forking](https://help.github.com/articles/fork-a-repo/) this repository and browse the code. Then you can look at our [Issue-Tracker](https://github.com/devsebastian/WonderScan/issues) and start with fixing one issue. 
After you've created a pull request we will review your code and merge it to the repo! don't worry all kinds of contributions are welcome:)

If you want to implement a new feature or fix a bug, be sure to create an issue first and then get it approved before starting to work on it!

## Setting up the Android Project

1. Download the *WonderScan* project source. You can do this either by forking and cloning the repository (recommended if you plan on pushing changes) or by downloading it as a ZIP file and extracting it.

2. Install the NDK in Android Studio.

3. Open Android Studio, you will see a **Welcome to Android** window. Under Quick Start, select *Import Project (Eclipse ADT, Gradle, etc.)*

4. Navigate to the directory where you saved the WonderScan project, select the root folder of the project (the folder named "WonderScan"), and hit OK. Android Studio should now begin building the project with Gradle.

5. Once this process is complete and Android Studio opens, check the Console for any build errors.

    - *Note:* If you receive a Gradle sync error titled, "failed to find ...", you should click on the link below the error message (if available) that says *Install missing platform(s) and sync project* and allow Android studio to fetch you what is missing.

6. Download this [OpenCV-android-sdk](https://github.com/opencv/opencv/releases/download/4.0.1/opencv-4.0.1-android-sdk.zip) zip file and extract it.

     - Copy all the files from *"OpenCV-android-sdk/sdk/native/libs"* to *"WonderScan/app/src/main/jniLibs"* (create directory if it doesn't exist)
     - Copy all the files from *"OpenCV-android-sdk/sdk/native/jni/include"* to *"WonderScan/app/src/main/jni/include"* (create directory if it doesn't exist)
     - Now build your project. If your build fails then try deleting these build directories *"WonderScan/app/.externalNativeBuild"* and *"WonderScan/app/build"*, if they exist and run the build again.

7. If build error still persist, try replacing ndk with ndk version 18b from [ndk-archives](https://developer.android.com/ndk/downloads/older_releases) .  Once all build errors have been resolved, you should be all set to build the app and test it.

8. To Build the app, go to *Build > Make Project* (or alternatively press the Make Project icon in the toolbar).

9. If the app was built successfully, you can test it by running it on either a real device or an emulated one by going to *Run > Run 'app'* or pressing the Run icon in the toolbar.

## Feedback
For any kind of feedback or query you can contact me directly via developer.devsebastian@gmail.com :)

## License
Copyright (c) Dev Sebastian. All rights reserved.

Licensed under the MIT license.
