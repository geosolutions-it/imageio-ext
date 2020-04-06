# Cloud Optimized GeoTIFF Reader Plugin 

### Overview
Cloud Optimized GeoTIFF (COG) reader for imageio-ext.   

The [TIFFImageReader](https://github.com/geosolutions-it/imageio-ext/blob/master/plugin/tiff/src/main/java/it/geosolutions/imageioimpl/plugins/tiff/TIFFImageReader.java) 
handles reading GeoTIFF files using a provided ImageInputStream.  The TIFFImageReader will determine which tiles 
or strips fall within the requested pixel coordinates and synchronously loop through each tile to read and then decode 
each image.  This equates to remotely fetching each tile, one by one. For COG, we wish to read contiguous byte ranges of 
consecutive tiles to reduce the number of reads required to obtain the requested data.

This project seeks to support Cloud Optimized GeoTIFFs without rewriting any of the low level GeoTIFF code that is 
already provided with imageio-ext.  The strategy is:
 
 * Determine which tiles need to be read for the given request
 * Build a collection of tile metadata objects to include the start offset and byte length for each requested tile
 * Compare all tile ranges and combine all continguous ranges into a single range
 * Asynchronously read all contiguous ranges, blocking until all ranges have been read
 * Store the byte data in memory inside the image input stream object
 * Allow TIFFImageReader to continue decoding single tiles at a time, using the in-memory byte data that has already 
 been fetched.
 
A code sample may look like this:

```java
CogUri cogUri = new CogUri("https://server.com/cog.tif", true);
ImageInputStream cogStream = new CogImageInputStreamSpi().createInputStreamInstance(cogUri);

CogImageReader reader = new CogImageReader(new CogImageReaderSpi());
reader.setInput(cogStream);

CogImageReadParam param = new CogImageReadParam();
param.setSourceRegion(new Rectangle(500, 500, 1000, 1000));
param.setRangeReaderClass(HttpRangeReader.class);

BufferedImage cogImage = reader.read(0, param);
```
 
### Building
The [cog-reader](./cog-reader/) module provides four build profiles, `cog-http`, `cog-s3`, `cog-azure`, and `cog-all`.  
The selected  profile will include the ability to read COGs using either the `com.squareup.okhttp3:okhttp`, 
`software.amazon.awssdk:s3`, or `com.azure:azure-storage-blob` client libraries.  The `cog-all` profile is the default 
and will include all of the dependencies.
 
### Notable Classes
[CogImageReader](./cog-reader/src/main/java/it/geosolutions/imageioimpl/plugins/cog/CogImageReader.java) extends 
TIFFImageReader and overrides the `read` method.  It provides the logic to determine which byte ranges need to be read 
before passing the request on to TIFFImageReader's `read` method. 

[CogTileInfo](./cog-commons/src/main/java/it/geosolutions/imageioimpl/plugins/cog/CogTileInfo.java) is a simple utility 
class to store all of the metadata about each tile requested, including start byte position, end byte position, byte 
length, and tile index.  It includes methods to fetch the corresponding TileRange metadata object given a position or 
tile index.

[CogImageInputStream](./cog-commons/src/main/java/it/geosolutions/imageioimpl/plugins/cog/CogImageInputStream.java) is 
an interface that defines several methods, notably `readRanges`.  It accepts a 2D long array as a method parameter 
containing all of the start and end byte positions that need to be read.  The CogImageReader checks to see if the 
ImageInputStream being used is an instance of this class to determine if it should attempt to build and fetch the byte 
ranges.  If the ImageInputStream does not implement CogImageInputStream, CogImageReader will simply pass the request on 
to TIFFImageReader. 
 
[DefaultCogImageInputStream](./cog-streams/src/main/java/it/geosolutions/imageioimpl/plugins/cog/DefaultCogImageInputStream.java) 
is an ImageInputStream implementation that stores all requeted tile bytes a delegate MemoryCacheImageInputStream.  This 
is sufficient for libraries that do not expect to make multiple requests for the same tiles.
 
[CachingCogImageInputStream](./cog-streams/src/main/java/it/geosolutions/imageioimpl/plugins/cog/CachingCogImageInputStream.java)
is an ImageInputStream implementation that will cache GeoTIFF tiles using Ehcache to prevent additional requests 
for data that is expected to be read multiple times.  Subsequent reads of the same tiles using this input stream 
provides a significant performance increase.

[CogUri](./cog-commons/src/main/java/it/geosolutions/imageioimpl/plugins/cog/CogUri.java) is a simple Java bean that 
is used as the CogImageReader's input source.  Using a custom class helps ImageIOExt.getImageInputStreamSPI() 
automatically select the properly ImageInputStream implementation.  CogUri contains the URI of the image along with a 
boolean `useCache` to specify whether the Caching or Default ImageInputStream implementation should be used.  The 
value defaults to true and if not modified, the SPI will return the `CachingCogImageInputStream`.  

[RangeBuilder](./cog-commons/src/main/java/it/geosolutions/imageioimpl/plugins/cog/RangeBuilder.java)) is responsible 
for comparing individual tile start/end information and group all contiguous ranges into a single range.  These are 
the ranges that will be used by the RangeReader.

[RangeReader](./cog-commons/src/main/java/it/geosolutions/imageioimpl/plugins/cog/RangeReader.java) is a newly introduced 
abstract class.  It can be implemented by any library to execute asynchronous range reads.  Currently 3 implementations
are provided:

* HTTP: [HttpRangeReader](./cog-rangereader-http/src/main/java/it/geosolutions/imageioimpl/plugins/cog/HttpRangeReader.java)
* AWS-S3: [S3RangeReader](./cog-rangereader-s3/src/main/java/it/geosolutions/imageioimpl/plugins/cog/S3RangeReader.java)
* Azure Blob Storage: [AzureRangeReader](./cog-rangereader-azure/src/main/java/it/geosolutions/imageioimpl/plugins/cog/AzureRangeReader.java)

The RangeReader implementations are also responsible for eagerly fetching the COG's header upon instantiation and default 
to a 16KB header size to ensure all header information is read.  This value can be modified before the `init()` method 
is called to initialize the header.  The class also contains logic to prevent re-reading data from supplied byte ranges 
if the byte ranges fall inside of the header range that has already been read.

Although all the S3 and Azure client libraries fetch all ranges asynchronously using HTTP under the hood, their 
demonstrated performance seems to be far below that of the HttpRangeReader.
 
When using the CachingCogImageInputStream, each tile is individually stored in cache.  Each cache entry is not a fixed
size; instead it is simply the size of the tile.  This way, when the TIFFImageReader begins it's process of looping 
through each individual tile to decode and requests to read a tile, only one call is needed to be made to fetch that 
tile from cache. 

[CogImageReadParam](./cog-commons/src/main/java/it/geosolutions/imageio/plugins/cog/CogImageReadParam) extends 
`TIFFImageReadParam` and provides an additional field `Class<? extends RangeReader> rangeReaderClass;`.  This provides 
the `CogImageInputStream` implementation the information necessary to know which `RangeReader` to construct.  This
object is passed to the `read` method of `CogImageReader`.

[CogTileCacheProvider](./cog-commons/src/main/java/it/geosolutions/imageioimpl/plugins/cog/CogTileCacheProvider.java) 
is a simple interface to define the methods that need to be implemented for `CachingCogImageInputStream` to cache 
and retrieve information.  Currently the only implementation provided uses EhCache 

### Performance
To quickly benchmark performance between the CogImageReader and the TIFFImageReader, I averaged the amount of time it 
took to to produce the final image with 10 and 50 consecutive requests. All tests used [this Landsat 8 image](
https://s3-us-west-2.amazonaws.com/landsat-pds/c1/L8/153/075/LC08_L1TP_153075_20190515_20190515_01_RT/LC08_L1TP_153075_20190515_20190515_01_RT_B2.TIF) 
as the target image.

Reading 1000x1000 pixels from an offset of 2000, 2000:

| Number of Requests | CogImageReader (ms) | TIFFImageReader (ms) | Delta (ms) |
| ------------------ | -------------- | --------------- | ------ |
|        10          |      1689      |       4961      |  3272  |
|        50          |      1586      |       5305      |  3719  |

Reading 2000x2000 pixels from an offset of 0, 0:

| Number of Requests | CogImageReader (ms) | TIFFImageReader (ms) | Delta (ms) |
| ------------------ | -------------- | --------------- | ------ |
|        10          |      1029      |       2927      |  1898  |
|        50          |      1287      |       3418      |  2131  |

### Sample Debug Outputs
* Reading an entire image.  Because all tiles are contiguous, only 1 range request is made.  The byte locations, as 
calculated by CogImageReader, are modified by HttpCogImageInputStream as to not re-request data that was read in the 
header:
```
File size: 52468640
Reading header with size 16384
Building request for range 0-16384 to https://s3-us-west-2.amazonaws.com/landsat-pds/c1/L8/153/075/LC08_L1TP_153075_20190515_20190515_01_RT/LC08_L1TP_153075_20190515_20190515_01_RT_B2.TIF
Reading pixels at offset (0.0, 0.0) with a width of 8000.0px and height of 8000.0px
Reading tiles (0,0) - (14,15)
Modified range 2304-52468639 to 16385-52468639 as it overlaps with data previously read in the header request
Submitting 1 range request(s)
Building request for range 16385-52468639 to https://s3-us-west-2.amazonaws.com/landsat-pds/c1/L8/153/075/LC08_L1TP_153075_20190515_20190515_01_RT/LC08_L1TP_153075_20190515_20190515_01_RT_B2.TIF
Time to read all ranges: PT8.227302S
Time for COG: 9271ms
```
---
* Reading only the first tile.  No additional range requests are necessary as the first tile was actually fully read in 
the header request:
```
File size: 52468640
Reading header with size 16384
Building request for range 0-16384 to https://s3-us-west-2.amazonaws.com/landsat-pds/c1/L8/153/075/LC08_L1TP_153075_20190515_20190515_01_RT/LC08_L1TP_153075_20190515_20190515_01_RT_B2.TIF
Reading pixels at offset (0.0, 0.0) with a width of 500.0px and height of 500.0px
Reading tiles (0,0) - (0,0)
Removed range 2304-2834 as it lies fully within the data already read in the header request
Submitting 0 range request(s)
Time to read all ranges: PT0.000036S
Time for COG: 164ms
```
---
* Reading an arbitrary portion of the image.  Multiple range requests are required where the tiles are not contiguous:
```
File size: 52468640
Reading header with size 16384
Building request for range 0-16384 to https://s3-us-west-2.amazonaws.com/landsat-pds/c1/L8/153/075/LC08_L1TP_153075_20190515_20190515_01_RT/LC08_L1TP_153075_20190515_20190515_01_RT_B2.TIF
Reading pixels at offset (1208.0, 2684.0) with a width of 4893.0px and height of 3879.0px
Reading tiles (2,5) - (11,12)
No ranges modified.
Submitting 8 range request(s)
Building request for range 16540037-19858156 to https://s3-us-west-2.amazonaws.com/landsat-pds/c1/L8/153/075/LC08_L1TP_153075_20190515_20190515_01_RT/LC08_L1TP_153075_20190515_20190515_01_RT_B2.TIF
Building request for range 20916677-24108115 to https://s3-us-west-2.amazonaws.com/landsat-pds/c1/L8/153/075/LC08_L1TP_153075_20190515_20190515_01_RT/LC08_L1TP_153075_20190515_20190515_01_RT_B2.TIF
Building request for range 25035438-28262317 to https://s3-us-west-2.amazonaws.com/landsat-pds/c1/L8/153/075/LC08_L1TP_153075_20190515_20190515_01_RT/LC08_L1TP_153075_20190515_20190515_01_RT_B2.TIF
Building request for range 29320282-32580105 to https://s3-us-west-2.amazonaws.com/landsat-pds/c1/L8/153/075/LC08_L1TP_153075_20190515_20190515_01_RT/LC08_L1TP_153075_20190515_20190515_01_RT_B2.TIF
Building request for range 33603270-36705283 to https://s3-us-west-2.amazonaws.com/landsat-pds/c1/L8/153/075/LC08_L1TP_153075_20190515_20190515_01_RT/LC08_L1TP_153075_20190515_20190515_01_RT_B2.TIF
Building request for range 37723107-40946150 to https://s3-us-west-2.amazonaws.com/landsat-pds/c1/L8/153/075/LC08_L1TP_153075_20190515_20190515_01_RT/LC08_L1TP_153075_20190515_20190515_01_RT_B2.TIF
Building request for range 41935185-45115808 to https://s3-us-west-2.amazonaws.com/landsat-pds/c1/L8/153/075/LC08_L1TP_153075_20190515_20190515_01_RT/LC08_L1TP_153075_20190515_20190515_01_RT_B2.TIF
Building request for range 45872899-48895646 to https://s3-us-west-2.amazonaws.com/landsat-pds/c1/L8/153/075/LC08_L1TP_153075_20190515_20190515_01_RT/LC08_L1TP_153075_20190515_20190515_01_RT_B2.TIF
Time to read all ranges: PT3.836557S
Time for COG: 4463ms
```
---
![COG](./images/sample.png "COG")