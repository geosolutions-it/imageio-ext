# COG Image Reader

This module simply provides two classes, [CogImageReader](./src/main/java/it/geosolutions/imageioimpl/plugins/cog/CogImageReader.java)
and [CogImageReaderSpi](./src/main/java/it/geosolutions/imageioimpl/plugins/cog/CogImageReaderSpi.java).  The
`CogImageReader` extends from `TIFFImageReader` and is responsible for:

* Compiling information about which tiles need to be read for the current request given the input pixel space
* Requesting that the CogImageInputStream implementation read the ranges for the requested tiles and thus pre-warming 
the input stream's cache with the tile data
* Passing the request on to `TIFFImageReader`, where it will take advantage of the data cached in the input stream.

If the `ImageInputStream` implementation passed to the reader as a source is not an instance of `CogImageInputStream`, 
the request will simply be passed on to `TIFFImageReader` to be read without taking advantage of COG optimizations.

This module provides four build profiles, `cog-http`, `cog-s3`, `cog-azure`, and `cog-all`.  The selected profile will 
include the ability to read COGs using either the `com.squareup.okhttp3:okhttp`, `software.amazon.awssdk:s3`, or 
`com.azure:azure-storage-blob` client libraries.  The `cog-all` profile is the default and will include all of the 
dependencies. 