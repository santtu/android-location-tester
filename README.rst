Android Location Tester application
===================================

Simple Android app to demonstrate use of LocationManager API. It targets
Android API level 10 (Gingerbread MR1) but as far as I know it should work
with older devices too, but I don't have access to pre-10 emulator images
or devices for testing. If you can check it out on an older device, I'll be
happy to accept a pull request for lower API target level.

Compiling
---------

Either import the project into your favourite IDE, or run ant::

  ANDROID_HOME=path/to/android/sdk ant debug

If you don't yet know how to progress from there to running the app
in an emulator or in actual Android device, please go and see
http://developer.android.com/training/basics/firstapp/index.html first.
