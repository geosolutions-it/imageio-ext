                                =======================
                                ImageI/O-Ext Deployment
                                =======================

1. Make sure all the imageio-ext jars you need are on the classpath of your application


2. Download the proper GDAL Native libraries (depending on your System) 
   and extract them on your disk.

  2a. If you are on Windows, make sure that the GDAL DLL files are on your PATH (by adding
  an entry to the PATH environment variable referring to the folder where the native libs
  have been deployed). Note that multiple DLLs versions available on different location on your 
  PATH's machine may cause loading issues. Usually, you can extract DLLs on your JDK/bin folder.
  When done, you could run "gdalinfo --version" to check the native libs installation.
  
  2b. If you are on Linux, make sure to set the LD_LIBRARY_PATH environment variable 
  to refer to the folder where the SOs have been extracted. 
  (Typical uses are extracting SOs on your java JDK in the /jre/lib/i386 (Linux32) 
  or /jre/lib/amd64 (linux64) subfolders).
  When done, you could "cd" to the folder where they have been extracted and run 
  "./gdalinfo --version" to check the native libs installation.
  
  
3. Download the GDAL CRS Definitions zip archive and extract them on your disk. 
   Make sure to set a GDAL_DATA environment variable to the folder 
   where you have extracted the files.
