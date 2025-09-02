# The ImageIO-Ext Project
![GeoSolutions Rocks!](http://3.bp.blogspot.com/_0_xIiXP5xuY/TUGnIbDpcgI/AAAAAAAAAOY/gKhkBdKZcfs/s1600/imageio_jpg_580x320_crop_q85.jpg)

The **ImageIO-Ext** is an Open Source project that provides extensions, fixes and improvements for the standard Oracle Java Image I/O project such as:

1. Support for the [GDAL](http://www.gdal.org/) I/O library
2. Support for reading/writing JPEG2000 files with [Kakadu](http://www.kakadusoftware.com/)
3. Improved support for reading/writing tiff files 
4. A Reader/Writer for JPEG images based on the [libjpeg-turbo](http://libjpeg-turbo.virtualgl.org/) open source high performance library. More info can be found [here](https://github.com/geosolutions-it/imageio-ext/wiki/TurboJPEG-plugin)
5. A NITF plugin based on [NITRO](http://nitro-nitf.sourceforge.net/wikka.php?wakka=HomePage). More info can be found [here](https://github.com/geosolutions-it/imageio-ext/wiki/NITF-plugin)
6. A new PNG Writer with improved performances. More informations can be found [here](https://github.com/geosolutions-it/imageio-ext/wiki/PNG-plugin).

The ImageIO library provides support for encoding/decoding raster formats in Java. Some useful documentation on ImageIO can be found [here](http://docs.oracle.com/javase/1.4.2/docs/guide/imageio/spec/imageio_guideTOC.fm.html).

See the documentation below for more information on the ImageIO-Ext project.

# Releases and Downloads
Current stable release is **[1.3.2](http://demo.geo-solutions.it/share/github/imageio-ext/releases/1.3.X/1.3.2/)**. Check [this page](https://github.com/geosolutions-it/imageio-ext/wiki/Releases) for additional information on how to download artifacts and binaries.


# Getting Support
## Mailing List
We have created two public mailing lists for ImageIO-Ext one for Developers and one for Users, you can find their homepages here below:

* [**ImageIO-Ext Users Group**](https://groups.google.com/d/forum/imageio-ext-users)
* [**ImageIO-Ext Developers Group**](https://groups.google.com/d/forum/imageio-ext-developers)

# Contributing
We welcome contributions in any form:

* pull requests for new features
* pull requests for bug fixes
* pull requests for documentation
* funding for any combination of the above

# Working with ImageIO-Ext
Here below you can find links with useful information for working with ImageIO-Ext.

* [Documentation](https://github.com/geosolutions-it/imageio-ext/wiki/Documentation)
* [Releases](https://github.com/geosolutions-it/imageio-ext/wiki/Releases)
* [Working with Maven](https://github.com/geosolutions-it/imageio-ext/wiki/Working-with-Maven)
* [Continuous Build](https://github.com/geosolutions-it/imageio-ext/wiki/ContinuosIntegration)

## Java 17 Compatibility

When using ImageIO-Ext with Java 17, you need to add the following JVM arguments to access internal Java modules that the library requires:

```bash
--add-opens java.base/java.lang=ALL-UNNAMED
--add-opens java.base/java.util=ALL-UNNAMED
--add-opens java.desktop/java.awt.image=ALL-UNNAMED
--add-opens java.desktop/javax.imageio.stream=ALL-UNNAMED
--add-opens java.desktop/javax.imageio=ALL-UNNAMED
--add-exports java.desktop/com.sun.imageio.plugins.jpeg=ALL-UNNAMED
--add-exports java.desktop/com.sun.imageio.plugins.png=ALL-UNNAMED
```

### Example Usage

**Running your application:**

```bash
java --add-opens java.base/java.lang=ALL-UNNAMED \
     --add-opens java.base/java.util=ALL-UNNAMED \
     --add-opens java.desktop/java.awt.image=ALL-UNNAMED \
     --add-opens java.desktop/javax.imageio.stream=ALL-UNNAMED \
     --add-opens java.desktop/javax.imageio=ALL-UNNAMED \
     --add-exports java.desktop/com.sun.imageio.plugins.jpeg=ALL-UNNAMED \
     --add-exports java.desktop/com.sun.imageio.plugins.png=ALL-UNNAMED \
     -cp your-classpath YourMainClass
```

**Running tests with Maven:**

The project automatically handles these JVM arguments when building with Java 9+ through the `jdk-9-plus` profile in the root POM, so you can simply run:

```bash
mvn test
```

If you need to override or add additional JVM arguments for tests, you can use:

```bash
mvn test -Dsurefire.jvm.args="your-additional-args"
```

These arguments are required because ImageIO-Ext accesses internal Java APIs that are encapsulated by the module system introduced in Java 9+.

# Important Notice
**In case you want to enable ECW Decode support, it is mandatory you agree with the ECW Eula. Moreover if you want to support ECW Decode in a Server application you need to BUY a license from ERDAS.**

# License
**ImageIO-Ext** is released partly under [LGPL](https://github.com/geosolutions-it/imageio-ext/blob/master/LICENSE.txt) license partly under the [BSD](https://github.com/geosolutions-it/imageio-ext/blob/master/plugin/tiff/LICENSE.txt) license (namely, derivative work from imageio source code). Refer to the code tree for more information.

## Professional Support
ImageIO-EXT has been developed by [**GeoSolutions**](http://www.geo-solutions.it) as an internal effort to provide extensions, fixes and improvements for the standard Oracle Java Image I/O project 
