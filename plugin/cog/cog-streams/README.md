# COG Streams

This module provides two `ImageInputStream`/`CogImageInputStream` implementations:

* [DefaultCogImageInputStream](./cog-streams/src/main/java/it/geosolutions/imageioimpl/plugins/cog/DefaultCogImageInputStream.java) 
* [CachingCogImageInputStream](./cog-streams/src/main/java/it/geosolutions/imageioimpl/plugins/cog/CachingCogImageInputStream.java)

The [CogImageInputStreamSpi](./cog-streams/src/main/java/it/geosolutions/imageioimpl/plugins/cog/CogImageInputStreamSpi.java)
requires an instance of [CogUri](../cog-commons/src/main/java/it/geosolutions/imageioimpl/plugins/cog/CogUri) which 
holds a reference to the URI of the image, along with a boolean to determine if the caching input stream should be used 
or not.  The default value is `true`, so without modification, the SPI will return `CachingCogImageInputStream`. 
    