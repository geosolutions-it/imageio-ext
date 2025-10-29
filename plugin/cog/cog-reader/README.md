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

This module provides five build profiles: `cog-http`, `cog-s3`, `cog-azure`, `cog-gs`, and `cog-all`.  The selected profile
will include the ability to read COGs using either the `com.squareup.okhttp3:okhttp`, `software.amazon.awssdk:s3`,
`com.azure:azure-storage-blob`, or `com.google.cloud:google-cloud-storage` client libraries.  The `cog-all` profile is
the default and will include all of the dependencies.

## Integration Tests

Integration tests are disabled by default since they require a Docker environment (using Testcontainers). Each profile
has corresponding integration tests organized by package:

- `cog-http`: tests in `it.geosolutions.imageioimpl.plugins.cog.http`
- `cog-s3`: tests in `it.geosolutions.imageioimpl.plugins.cog.s3`
- `cog-azure`: tests in `it.geosolutions.imageioimpl.plugins.cog.azure`
- `cog-gs`: tests in `it.geosolutions.imageioimpl.plugins.cog.gs`

**Important**: The build profiles only include dependencies for their specific storage backend, so you cannot compile with
a single profile enabled. Instead, use this two-step process:

```bash
# Step 1: Build and compile all code (uses cog-all profile by default)
mvn clean install -DskipTests -pl :imageio-ext-cog-reader -am

# Step 2: Run integration tests for a specific profile (doesn't recompile)
mvn verify -P cog-azure -DskipITs=false -pl :imageio-ext-cog-reader
```

To run all integration tests (requires Docker):

```bash
mvn clean install -DskipTests -pl :imageio-ext-cog-reader -am
mvn verify -P cog-all -DskipITs=false -pl :imageio-ext-cog-reader
```

