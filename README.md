# WonderScan
WonderScan is a fully open-source, free, and secure on-device document scanner. All data is stored in your device and no data is sent to any online server around the world! Many different effects are available ranging from simple Black and White, Grayscale, to even Magic color! Various other parameters like the brightness and contrast can also be controlled. Scanned documents could then be easilly exported in PDF format!

You can download the app from: https://play.google.com/store/apps/details?id=com.wonderscan.android

<p float="left">
<img width="24%" src="https://user-images.githubusercontent.com/19506171/134734966-43aff8a5-5d6b-4880-a39d-276c2be34e7a.png"/>
<img width="24%" src="https://user-images.githubusercontent.com/19506171/134734973-bcbcf9cd-6e92-4b94-9e95-53de50c07a24.jpg"/>
<img width="24%" src="https://user-images.githubusercontent.com/19506171/134734983-dd722396-8bbe-4868-bf40-3649e223ac12.jpg"/>
<img width="24%" src="https://user-images.githubusercontent.com/19506171/134734979-bb001a70-4492-4a05-95db-088cc3b5837a.jpg"/>
</p>

## Features of WonderScan:
1. Import Documents from camera or gallery
2. Crop Images and apply perspective-wrap to give a 2D effect
3. Apply many different filters to each document
4. Save as collection of frames as a document. 
5. Add any number of pages to existing documents.
6. Re-arrange pages of existing document.
6. Create PDFs
7. perform OCR and read text of scanned documents
8. Save a note for each frame or document
9. Control brightness and contrast of each document


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
