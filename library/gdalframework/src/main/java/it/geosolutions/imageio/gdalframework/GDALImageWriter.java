/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2007 - 2009, GeoSolutions
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    either version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package it.geosolutions.imageio.gdalframework;

import it.geosolutions.imageio.gdalframework.GDALUtilities.DriverCreateCapabilities;
import it.geosolutions.imageio.stream.output.FileImageOutputStreamExt;
import it.geosolutions.imageio.utilities.Utilities;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.IIOImage;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageWriterSpi;
import javax.media.jai.PlanarImage;

import org.gdal.gdal.Band;
import org.gdal.gdal.ColorTable;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.Driver;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;

/**
 * Main abstract class defining the main framework which needs to be used to
 * extend Image I/O architecture using <a href="http://www.gdal.org/"> GDAL
 * (Geospatial Data Abstraction Library)</a> by means of SWIG (Simplified
 * Wrapper and Interface Generator) bindings in order to perform write
 * operations.
 * 
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public abstract class GDALImageWriter extends ImageWriter {
	
    private static final Logger LOGGER = Logger.getLogger(GDALImageWriter.class.toString());

    /**
     * Utility method which checks if a system property has been specified to
     * set the maximum allowed size to create a GDAL "In Memory Raster" Dataset
     * in case of CreateCopy. In case of the system property has been set,
     * returns this value, otherwise it returns a default value.
     * 
     * @see GDALImageWriter#DEFAULT_GDALMEMORYRASTER_MAXSIZE
     * 
     * @return the maximum allowed size to create a GDAL "In Memory Raster"
     *         Dataset in case of CreateCopy.
     */
    protected final static int getMaxMemorySizeForGDALMemoryDataset() {
        int size = DEFAULT_GDALMEMORYRASTER_MAXSIZE;

        // //
        //
        // Checking for a simple integer value (size in bytes)
        //
        // //
        Integer maxSize = Integer.getInteger(GDALUtilities.GDALMEMORYRASTER_MAXSIZE_KEY);
        if (maxSize != null)
            size = maxSize.intValue();
        else {
            // //
            //
            // Checking for a properly formatted string value.
            // Valid values should end with one of M,m,K,k
            //
            // //
            final String maxSizes = System.getProperty(GDALUtilities.GDALMEMORYRASTER_MAXSIZE_KEY);
            if (maxSizes != null) {
                final int length = maxSizes.length();
                final String value = maxSizes.substring(0, length - 1);
                final String suffix = maxSizes.substring(length - 1, length);

                // //
                //
                // Checking for valid multiplier suffix
                //
                // //
                if (suffix.equalsIgnoreCase("M")
                        || suffix.equalsIgnoreCase("K")) {
                    int val;
                    try {
                        val = Integer.parseInt(value);
                        if (suffix.equalsIgnoreCase("M"))
                            val *= (1024 * 1024); // Size in MegaBytes
                        else
                            val *= 1024; // Size in KiloBytes
                        size = val;
                    } catch (NumberFormatException nfe) {
                        // not a valid value
                    }
                }
            }
        }
        return size;
    }

    /**
     * The maximum amount of memory which should be requested to use an "In
     * Memory" Dataset in case of createcopy
     */
    private static final int DEFAULT_GDALMEMORYRASTER_MAXSIZE = 1024 * 1024 * 32;

    /** Output File */
    protected File outputFile;

    /** Memory driver for creating {@link Dataset}s in memory. */
    private static class ThreadLocalMemoryDriver extends ThreadLocal<Driver> {
        public Driver initialValue() {
            return gdal.GetDriverByName("MEM");
        }
    }

    private static ThreadLocalMemoryDriver memDriver = new ThreadLocalMemoryDriver();

    /**
     * return a "In Memory" Driver which need to be used when using the
     * CreateCopy method.
     */
    protected static Driver getMemoryDriver() {
        return (Driver) memDriver.get();
    }

    /**
     * Constructor for <code>GDALImageWriter</code>
     */
    public GDALImageWriter(ImageWriterSpi originatingProvider) {
        super(originatingProvider);
    }

    public IIOMetadata getDefaultStreamMetadata(ImageWriteParam param) {
        throw new UnsupportedOperationException(
                "getDefaultStreamMetadata not implemented yet.");
    }

    /**
     * Write the input image to the output.
     * <p>
     * The output must have been set beforehand using the <code>setOutput</code>
     * method.
     * 
     * <p>
     * An <code>ImageWriteParam</code> may optionally be supplied to control
     * the writing process. If <code>param</code> is <code>null</code>, a
     * default write param will be used.
     * 
     * <p>
     * If the supplied <code>ImageWriteParam</code> contains optional setting
     * values not supported by this writer (<i>e.g.</i> progressive encoding
     * or any format-specific settings), they will be ignored.
     * 
     * @param streamMetadata
     *                an <code>IIOMetadata</code> object representing stream
     *                metadata, or <code>null</code> to use default values.
     * @param image
     *                an <code>IIOImage</code> object containing an image, and
     *                metadata to be written. Note that metadata is actually
     *                supposed to be an instance of
     *                {@link GDALCommonIIOImageMetadata}.
     *                {@link GDALWritableCommonIIOImageMetadata} may be used to
     *                set properties from other type of ImageMetadata to a
     *                format which is understood by this writer.
     * @param param
     *                an <code>ImageWriteParam</code>, or <code>null</code>
     *                to use a default <code>ImageWriteParam</code>.
     * 
     * @exception IllegalStateException
     *                    if the output has not been set.
     * @exception IllegalArgumentException
     *                    if <code>image</code> is <code>null</code>.
     * @exception IOException
     *                    if an error occurs during writing.
     */
    public void write(IIOMetadata streamMetadata, IIOImage image,
            ImageWriteParam param) throws IOException {

        if (outputFile == null) {
            throw new IllegalStateException("the output is null!");
        }
        if (param == null)
            param = getDefaultWriteParam();

        // /////////////////////////////////////////////////////////////////////
        //
        // Initial check on the capabilities of this writer as well as the
        // provided parameters.
        //
        // /////////////////////////////////////////////////////////////////////
        final String driverName = (String) ((GDALImageWriterSpi) this.originatingProvider)
                .getSupportedFormats().get(0);
        final DriverCreateCapabilities writingCapabilities = GDALUtilities
                .formatWritingCapabilities(driverName);
        if (writingCapabilities == GDALUtilities.DriverCreateCapabilities.READ_ONLY)
            throw new IllegalStateException("This writer seems to not support either create or create copy");
        if (image == null)
            throw new IllegalArgumentException("The provided input image is invalid.");

        // //
        //
        // Getting the source image and its main properties
        //
        // //
        final PlanarImage inputRenderedImage = PlanarImage.wrapRenderedImage(image.getRenderedImage());
        final int sourceWidth = inputRenderedImage.getWidth();
        final int sourceHeight = inputRenderedImage.getHeight();
        final int sourceMinX = inputRenderedImage.getMinX();
        final int sourceMinY = inputRenderedImage.getMinY();
        final int dataType = GDALUtilities.retrieveGDALDataBufferType(
                inputRenderedImage.getSampleModel().getDataType());
        final int nBands = inputRenderedImage.getNumBands();

        // //
        //
        // Setting regions and sizes and retrieving parameters
        //
        // //
        final int xSubsamplingFactor = param.getSourceXSubsampling();
        final int ySubsamplingFactor = param.getSourceYSubsampling();
        final Vector<String> myOptions = (Vector<String>) ((GDALImageWriteParam) param)
                .getCreateOptionsHandler().getCreateOptions();
        Rectangle imageBounds = new Rectangle(sourceMinX, sourceMinY,
                sourceWidth, sourceHeight);
        Dimension destSize = new Dimension();
        computeRegions(imageBounds, destSize, param);

        // Destination sizes, needed for Dataset Creation
        final int destinationWidth = destSize.width;
        final int destinationHeight = destSize.height;

        // getting metadata before deciding if Create or CreateCopy will be used
        final IIOMetadata metadata = image.getMetadata();
        GDALCommonIIOImageMetadata imageMetadata = null;
        if (metadata != null) {
            if (metadata instanceof GDALCommonIIOImageMetadata) {
                imageMetadata = (GDALCommonIIOImageMetadata) metadata;
            } else {
                // TODO: build a metadata conversion to obtain an understandable
                // metadata object. Standard plugin-neutral format does not
                // contain really useful fields to be converted.
                // imageMetadata = new GDALWritableCommonIIOImageMetadata();
                // convertMetadata(IMAGE_METADATA_NAME, metadata,
                // imageMetadata);
            }
        }

        // /////////////////////////////////////////////////////////////////////
        //
        // Some GDAL formats driver support both "Create" and "CreateCopy"
        // methods. Some others simply support "CreateCopy" method which only
        // allows to create a new File from an existing Dataset.
        //
        // /////////////////////////////////////////////////////////////////////
        Dataset writeDataset = null;
        Driver driver = null;
        try{
	        // TODO: send some warning when setting georeferencing or size
	        // properties, if cropping or sourceregion has been defined.
	
	        if (writingCapabilities == GDALUtilities.DriverCreateCapabilities.CREATE) {
	            // /////////////////////////////////////////////////////////////////
	            //
	            // Create is supported
	            // -------------------
	            //
	            // /////////////////////////////////////////////////////////////////
	
	            // Retrieving the file name.
	            final String fileName = outputFile.getAbsolutePath();
	
	            // //
	            //
	            // Dataset creation
	            //
	            // //
	            driver = gdal.GetDriverByName(driverName);
	            writeDataset = driver.Create(fileName, destinationWidth,
	                    destinationHeight, nBands, dataType, myOptions);
	
	            // //
	            //
	            // Data Writing
	            //
	            // //
	            writeDataset = writeData(writeDataset, inputRenderedImage,
	                    imageBounds, nBands, dataType, xSubsamplingFactor,
	                    ySubsamplingFactor);
	
	            // //
	            //
	            // Metadata Setting
	            //
	            // //
	            if (imageMetadata != null) {
	                setMetadata(writeDataset, imageMetadata);
	            }
	        } else {
	
	            // ////////////////////////////////////////////////////////////////
	            //
	            // Only CreateCopy is supported
	            // ----------------------------------------------------------------
	            //
	            // First of all, it is worth to point out that CreateCopy method
	            // allows to create a File from an existing Dataset.
	            // ////////////////////////////////////////////////////////////////
	
	            driver = gdal.GetDriverByName(driverName);
	            // //
	            //
	            // Temporary Dataset creation from the originating image
	            //
	            // //
	            final File tempFile = File.createTempFile("datasetTemp", ".ds", null);
	            Dataset tempDataset = null; 
		        try{
		        	tempDataset = createDatasetFromImage(
		                    inputRenderedImage, tempFile.getAbsolutePath(),
		                    imageBounds, nBands, dataType, destinationWidth,
		                    destinationHeight, xSubsamplingFactor, ySubsamplingFactor);
		            tempDataset.FlushCache();
		
		            // //
		            //
		            // Metadata Setting on the temporary dataset since setting metadata
		            // with createCopy is not supported
		            //
		            // //
		            if (imageMetadata != null) {
		                setMetadata(tempDataset, imageMetadata);
		            }
		
		            // //
		            //
		            // Copy back the temporary dataset to the requested dataset
		            //
		            // //
		            writeDataset = driver.CreateCopy(outputFile.getPath(), tempDataset,
		                    0, myOptions);
		        } finally {
		        	if (tempDataset != null){
		        		try{
		                    // Closing the dataset
		        			GDALUtilities.closeDataSet(tempDataset);
		        		}catch (Throwable e) {
							if(LOGGER.isLoggable(Level.FINEST))
								LOGGER.log(Level.FINEST,e.getLocalizedMessage(),e);
						}
		        	}
		        }
	            tempFile.delete();
	        }
	
	        // //
	        //
	        // Flushing and closing dataset
	        //
	        // //
	        writeDataset.FlushCache();
        } finally{
        	if (writeDataset != null){
        		try{
                    // Closing the dataset
        			GDALUtilities.closeDataSet(writeDataset);
        		}catch (Throwable e) {
					if(LOGGER.isLoggable(Level.FINEST))
						LOGGER.log(Level.FINEST,e.getLocalizedMessage(),e);
				}
        	}
        	
        	if (driver != null){
	    		try{
                    // Closing the driver
	    			driver.delete();
        		}catch (Throwable e) {
					if(LOGGER.isLoggable(Level.FINEST))
						LOGGER.log(Level.FINEST,e.getLocalizedMessage(),e);
				}
	    	}
        }
    }

    /**
     * Set all the metadata available in the imageMetadata
     * <code>IIOMetadata</code> instance
     * 
     * @param dataset
     *                the dataset on which to set metadata and properties
     * @param imageMetadata
     *                an instance of a {@link GDALCommonIIOImageMetadata}
     *                containing metadata
     * 
     */
    private void setMetadata(Dataset dataset,
            GDALCommonIIOImageMetadata imageMetadata) {
        // TODO: which metadata should be copied in the dataset?
        // Should width, height and similar properties to be copied?

        // //
        //
        // Setting GeoTransformation
        //
        // //
        final double[] geoTransformation = imageMetadata.getGeoTransformation();
        if (geoTransformation != null)
            dataset.SetGeoTransform(geoTransformation);

        // //
        //
        // Setting Projection
        //
        // //
        final String projection = imageMetadata.getProjection();
        if (projection != null && projection.trim().length() != 0)
            dataset.SetProjection(projection);

        // //
        //
        // Setting GCPs
        //
        // //
        final int gcpNum = imageMetadata.getGcpNumber();
        if (gcpNum != 0) {
            final String gcpProj = imageMetadata.getGcpProjection();
            List gcps = imageMetadata.getGCPs();

            // TODO: Fix getGCPs access in SWIG's Java Bindings
            // TODO: set GCPs. Not all dataset support GCPs settings
            // dataset.SetGCPs(1, gcps, gcpProj);
        }

        // //
        //
        // Setting bands values
        //
        // //
        final int nBands = imageMetadata.getNumBands();
        for (int i = 0; i < nBands; i++) {
            final Band band = dataset.GetRasterBand(i + 1);
            final int colorInterpretation = imageMetadata
                    .getColorInterpretations(i);
            band.SetRasterColorInterpretation(colorInterpretation);
            if (i == 0 && nBands == 1) {

                // //
                //
                // Setting color table and color interpretations
                //
                // //
                if (colorInterpretation == gdalconstConstants.GCI_PaletteIndex) {
                    ColorModel cm = imageMetadata.getColorModel();
                    if (cm instanceof IndexColorModel) {
                        IndexColorModel icm = (IndexColorModel) cm;

                        // //
                        //
                        // Setting color table
                        //
                        // //
                        final int size = icm.getMapSize();
                        ColorTable ct = new ColorTable(
                                gdalconstConstants.GPI_RGB);
                        int j = 0;
                        for (; j < size; j++)
                            ct.SetColorEntry(j, new Color(icm.getRGB(j)));
                        band.SetRasterColorTable(ct);
                    }
                }
            }
            try {
                final double noData = imageMetadata.getNoDataValue(i);
                if (!Double.isNaN(noData))
                    band.SetNoDataValue(noData);
            } catch (IllegalArgumentException iae) {
                // NoDataValue not found or wrong bandIndex specified. Go on
            }
        }

        // //
        //
        // Setting metadata
        //
        // TODO: Requires SWIG bindings extending since an HashTable as
        // parameter crashes the JVM
        //
        // //
        final List<String> domains = imageMetadata.getGdalMetadataDomainsList();
        final int nDomains = domains.size();
        for (int i = 0; i < nDomains; i++) {
            final String domain = (String) domains.get(i);
            Map metadataMap = imageMetadata.getGdalMetadataDomain(domain);
            if (metadataMap != null) {
                Iterator<String> keysIt = metadataMap.keySet().iterator();
                while (keysIt.hasNext()) {
                    final String key = keysIt.next();
                    final String value = (String) metadataMap.get(key);
                    dataset.SetMetadataItem(key, value, domain);
                }
            }
        }
    }

    /**
     * Given a previously created <code>Dataset</code>, containing no data,
     * provides to store required data coming from an input
     * <code>RenderedImage</code> in compliance with a set of parameter such
     * as subSampling factors, SourceRegion.
     * 
     * @param dataset
     *                the destination dataset
     * @param inputRenderedImage
     *                the input image containing data which need to be written
     * @param sourceRegion
     *                the rectangle used to clip the source image dimensions
     * @param nBands
     *                the number of bands need to be written
     * @param dataType
     *                the datatype
     * @param xSubsamplingFactor
     *                the subsamplingFactor along X
     * @return the <code>Dataset</code> resulting after the write operation
     * 
     * TODO: minimize JNI calls by filling the databuffer before calling
     * writeDirect
     */
    private Dataset writeData(Dataset dataset,
            RenderedImage inputRenderedImage, final Rectangle sourceRegion,
            final int nBands, final int dataType, int xSubsamplingFactor,
            int ySubsamplingFactor) {
        final int typeSizeInBytes = gdal.GetDataTypeSize(dataType) / 8;

        // ////////////////////////////////////////////////////////////////////
        //
        // Variables Initialization
        //
        // ////////////////////////////////////////////////////////////////////

        // //
        //
        // Getting Source Region properties
        //
        // //
        final int srcRegionXOffset = sourceRegion.x;
        final int srcRegionYOffset = sourceRegion.y;
        final int srcRegionWidth = sourceRegion.width;
        final int srcRegionHeight = sourceRegion.height;
        final int srcRegionXEnd = srcRegionWidth + srcRegionXOffset;
        final int srcRegionYEnd = srcRegionHeight + srcRegionYOffset;

        // //
        //
        // Getting original image properties
        //
        // //
        final int minx_ = inputRenderedImage.getMinX();
        final int miny_ = inputRenderedImage.getMinY();
        final int srcW_ = inputRenderedImage.getWidth();
        final int srcH_ = inputRenderedImage.getHeight();
        final int maxx_ = minx_ + srcW_;
        final int maxy_ = miny_ + srcH_;

        // //
        //
        // Getting tiling properties
        //
        // //
        final int minTileX = inputRenderedImage.getMinTileX();
        final int minTileY = inputRenderedImage.getMinTileY();
        final int maxTileX = minTileX + inputRenderedImage.getNumXTiles();
        final int maxTileY = minTileY + inputRenderedImage.getNumYTiles();
        int tileW = inputRenderedImage.getTileWidth();
        int tileH = inputRenderedImage.getTileHeight();
        tileW = tileW < srcW_ ? tileW : srcW_;
        tileH = tileH < srcH_ ? tileH : srcH_;

        // //
        //
        // Auxiliary variables
        //
        // //
        // splitBands = false -> I read n Bands at once.
        // splitBands = true -> I need to read 1 Band at a time.
        boolean splitBands = false;
        final boolean isSubSampled = ySubsamplingFactor > 1
                || xSubsamplingFactor > 1;
        int dstWidth = 0;
        int dstHeight = 0;
        int xOff = 0;
        int yOff = 0;

        // ////////////////////////////////////////////////////////////////////
        //
        // Loop on tiles composing the source image
        // 
        // ////////////////////////////////////////////////////////////////////
        for (int ty = minTileY; ty < maxTileY; ty++) {
            xOff = 0;
            for (int tx = minTileX; tx < maxTileX; tx++) {

                // //
                // 
                // get the source raster for the current tile
                // 
                // //
                final Raster raster = inputRenderedImage.getTile(tx, ty);
                int minx = raster.getMinX();
                int miny = raster.getMinY();

                // //
                //
                // Cropping tiles if they have regions outside the original
                // image
                // 
                // //
                minx = minx < minx_ ? minx_ : minx;
                miny = miny < miny_ ? miny_ : miny;
                int maxx = minx + tileW;
                int maxy = miny + tileH;
                maxx = maxx > maxx_ ? maxx_ : maxx;
                maxy = maxy > maxy_ ? maxy_ : maxy;
                int offsetX = minx;
                int offsetY = miny;
                int newWidth = maxx - minx;
                int newHeight = maxy - miny;

                // //
                //
                // Offsets and sizes tunings.
                // Comparing tile bounds with the specified source region
                // 
                // //
                if (minx < srcRegionXOffset) {
                    if (maxx <= srcRegionXOffset)
                        continue; // Tile is outside the sourceRegion
                    else {
                        // SrcRegion X Offset is contained in the current tile
                        // I need to update the Offset X
                        offsetX = srcRegionXOffset;
                        if (srcRegionXEnd <= maxx)
                            newWidth = srcRegionWidth; // Tile is wider than
                        // the sourceRegion
                        else
                            newWidth = tileW - (offsetX - minx);
                    }
                } else if (minx >= srcRegionXEnd)
                    break; // Tile is outside the sourceRegion
                else if (maxx >= srcRegionXEnd) {
                    newWidth = srcRegionXEnd - minx;
                }

                if (miny < srcRegionYOffset) {
                    if (maxy <= srcRegionYOffset)
                        continue;// Tile is outside the sourceRegion
                    else {
                        // SrcRegion Y Offset is contained in the current tile
                        // I need to update the Offset Y
                        offsetY = srcRegionYOffset;
                        if (srcRegionYEnd <= maxy)
                            newHeight = srcRegionHeight;// Tile is higher than
                        // the sourceRegion
                        else
                            newHeight = tileH - (offsetY - miny);
                    }
                } else if (miny >= srcRegionYEnd)
                    break; // Tile is outside the sourceRegion
                else if (maxy >= srcRegionYEnd) {
                    newHeight = srcRegionYEnd - miny;
                }

                // Updating the first useless pixel along X and Y
                int endX = offsetX + newWidth;
                int endY = offsetY + newHeight;
                dstHeight = dstWidth = 0;

                // TODO: subst with an exact rule which allows to calculate
                // dst Height and Width, given offsets and ends, avoiding scan

                // Setting the destination Width
                if (ySubsamplingFactor > 1) {
                    for (int j = offsetY; j < endY; j++) {
                        if (((j - srcRegionYOffset) % ySubsamplingFactor) != 0)
                            continue;
                        dstHeight++;
                    }
                } else
                    dstHeight = newHeight;

                // Setting the destination Height
                if (xSubsamplingFactor > 1) {
                    for (int i = offsetX; i < endX; i++) {
                        if (((i - srcRegionXOffset) % xSubsamplingFactor) != 0)
                            continue;
                        dstWidth++;
                    }
                } else
                    dstWidth = newWidth;

                // //
                // 
                // Checks on data size
                // 
                // //
                int capacity = dstWidth * dstHeight * typeSizeInBytes * nBands;
                if (capacity < 0) {
                    splitBands = true;
                    capacity = dstWidth * dstHeight * typeSizeInBytes;
                }

                // ////////////////////////////////////////////////////////////////////
                //
                // Loading Data from the source Image
                // 
                // ////////////////////////////////////////////////////////////////////
                ByteBuffer[] bandsBuffer;
                if (!isSubSampled) {
                    bandsBuffer = getDataRegion(raster, offsetX, offsetY,
                            endX - 1, endY - 1, dataType, nBands, splitBands,
                            capacity);
                } else
                    bandsBuffer = getSubSampledDataRegion(raster, offsetX,
                            offsetY, endX - 1, endY - 1, srcRegionXOffset,
                            srcRegionYOffset, xSubsamplingFactor,
                            ySubsamplingFactor, dataType, nBands, splitBands,
                            capacity);

                // ////////////////////////////////////////////////////////////////////
                //
                // Writing Data in the destination dataset
                // 
                // ////////////////////////////////////////////////////////////////////
                if (!splitBands) {
                    final int[] bands = new int[nBands];
                    for (int i = 0; i < nBands; i++)
                        bands[i]=i+1;
                    // I can perform a single Write operation.
                    dataset.WriteRaster_Direct(xOff, yOff, dstWidth, dstHeight,
                            dstWidth, dstHeight, dataType, bandsBuffer[0], bands, 
                            nBands  * typeSizeInBytes, dstWidth * nBands
                                    * typeSizeInBytes, 1);
                } else {
                    // I need to perform a write operation for each band.
                    final int[] bands = new int[nBands];
                    for (int i = 0; i < nBands; i++)
                        dataset.GetRasterBand(i + 1).WriteRaster_Direct(xOff,
                                yOff, dstWidth, dstHeight, dstWidth, dstHeight,
                                dataType, bandsBuffer[i]);
                }
                // Updating the X offset position for writing in the dataset
                xOff += dstWidth;
            }
            // Updating the Y offset position for writing in the dataset
            yOff += dstHeight;
        }
        return dataset;
    }

    /**
     * Returns a proper <code>ByteBuffer</code> array containing data loaded
     * from the input <code>Raster</code>. Requested portion of data is
     * specified by means of a set of index parameters. In case
     * <code>splitBands</code> is <code>true</code> the array will contain
     * <code>nBands</code> <code>ByteBuffer</code>'s, each one containing
     * data element for a single band. In case <code>splitBands</code> is
     * <code>false</code>, the returned array has a single
     * <code>ByteBuffer</code> containing data for different bands stored as
     * PixelInterleaved
     * 
     * @param raster
     *                the input <code>Raster</code> containing data to be
     *                retrieved for the future write operation
     * @param firstX
     *                X index of the first data element to be scanned
     * @param firstY
     *                Y index of the first data element to be scanned
     * @param lastX
     *                X index of the last data element to be scanned
     * @param lastY
     *                Y index of the last data element to be scanned
     * @param srcRegionXOffset
     *                the original sourceRegion X offset value
     * @param srcRegionYOffset
     *                the original sourceRegion Y offset value
     * @param xSubsamplingFactor
     *                the subSampling factor along X
     * @param ySubsamplingFactor
     *                the subSampling factor along Y
     * @param dataType
     *                the datatype
     * @param nBands
     *                the number of bands
     * @param splitBands
     *                when <code>false</code>, a single
     *                <code>ByteBuffer</code> is created. When
     *                <code>true</code>, a number of buffer equals to the
     *                number of bands will be created and each buffer will
     *                contain data elements for a single band
     * @param capacity
     *                the size of each <code>ByteBuffer</code> contained in
     *                the returned array
     * @return a proper <code>ByteBuffer</code> array containing data loaded
     *         from the input <code>Raster</code>.
     * 
     * @throws IllegalArgumentException
     *                 in case the requested region is not valid. This could
     *                 happen in the following cases: <code>
     *             <UL>
     *             <LI> firstX < srcRegionXOffset </LI>
     *             <LI> firstY < srcRegionYOffset </LI>
     *             <LI> firstX > lastX </LI>
     *             <LI> firstY > lastY </LI>
     * </UL>
     * </code>
     */
    private ByteBuffer[] getSubSampledDataRegion(Raster raster,
            final int firstX, final int firstY, final int lastX,
            final int lastY, final int srcRegionXOffset,
            final int srcRegionYOffset, final int xSubsamplingFactor,
            final int ySubsamplingFactor, final int dataType, final int nBands,
            final boolean splitBands, final int capacity) {

        if (firstX < srcRegionXOffset || firstX < srcRegionYOffset
                || firstX > lastX || firstY > lastY)
            throw new IllegalArgumentException(
                    "The requested region is not valid");
        // TODO: provide a more user-friendly error message containing ranges
        // and values set.

        // //
        //
        // ByteBuffer containing data
        //
        // //
        ByteBuffer[] bandsBuffer;
        if (splitBands)
            bandsBuffer = new ByteBuffer[nBands];
        else
            bandsBuffer = new ByteBuffer[1];

        // ////////////////////////////////////////////////////////////////
        //
        // DataType: Byte
        //
        // ////////////////////////////////////////////////////////////////
        if (dataType == gdalconstConstants.GDT_Byte) {
            // //
            //
            // Initializing Data Buffer
            //
            // //
            for (int k = 0; k < nBands; k++) {
                bandsBuffer[k] = ByteBuffer.allocateDirect(capacity);
                if (!splitBands)
                    break;
            }
            byte[] data = new byte[nBands];
            // //
            //
            // Getting Data from the specified region with the requested
            // subsampling factors
            //
            // //
            for (int j = firstY; j <= lastY; j++) {
                if (((j - srcRegionYOffset) % ySubsamplingFactor) != 0) {
                    continue;
                }

                for (int i = firstX; i <= lastX; i++) {
                    if (((i - srcRegionXOffset) % xSubsamplingFactor) != 0) {
                        continue;
                    }
                    data = (byte[]) raster.getDataElements(i, j, data);
                    if (splitBands)
                        for (int k = 0; k < nBands; k++)
                            bandsBuffer[k].put(data, k, 1);
                    else
                        bandsBuffer[0].put(data, 0, nBands);
                }
            }
        }
        // ////////////////////////////////////////////////////////////////
        //
        // DataType: Int16 (short)
        //
        // ////////////////////////////////////////////////////////////////
        else if (dataType == gdalconstConstants.GDT_Int16) {
            // //
            //
            // Initializing Data Buffer
            //
            // //
            final ShortBuffer buf[] = new ShortBuffer[nBands];
            for (int k = 0; k < nBands; k++) {
                bandsBuffer[k] = ByteBuffer.allocateDirect(capacity);
                bandsBuffer[k].order(ByteOrder.nativeOrder());
                buf[k] = bandsBuffer[k].asShortBuffer();
                if (!splitBands)
                    break;
            }
            short[] data = new short[nBands];
            // //
            //
            // Getting Data from the specified region with the requested
            // subsampling factors
            //
            // //
            for (int j = firstY; j <= lastY; j++) {
                if (((j - srcRegionYOffset) % ySubsamplingFactor) != 0) {
                    continue;
                }

                for (int i = firstX; i <= lastX; i++) {
                    if (((i - srcRegionXOffset) % xSubsamplingFactor) != 0) {
                        continue;
                    }
                    data = (short[]) raster.getDataElements(i, j, data);
                    if (splitBands)
                        for (int k = 0; k < nBands; k++)
                            buf[k].put(data, k, 1);
                    else
                        buf[0].put(data, 0, nBands);
                }
            }
        }
        // ////////////////////////////////////////////////////////////////
        //
        // DataType: UInt16 (short)
        //
        // ////////////////////////////////////////////////////////////////
        else if (dataType == gdalconstConstants.GDT_UInt16) {
            // //
            //
            // Initializing Data Buffer
            //
            // //
            final ShortBuffer buf[] = new ShortBuffer[nBands];
            for (int k = 0; k < nBands; k++) {
                bandsBuffer[k] = ByteBuffer.allocateDirect(capacity);
                bandsBuffer[k].order(ByteOrder.nativeOrder());
                buf[k] = bandsBuffer[k].asShortBuffer();
                if (!splitBands)
                    break;
            }
            short[] data = new short[nBands];
            // //
            //
            // Getting Data from the specified region with the requested
            // subsampling factors
            //
            // //
            for (int j = firstY; j <= lastY; j++) {
                if (((j - srcRegionYOffset) % ySubsamplingFactor) != 0) {
                    continue;
                }

                for (int i = firstX; i <= lastX; i++) {
                    if (((i - srcRegionXOffset) % xSubsamplingFactor) != 0) {
                        continue;
                    }
                    data = (short[]) raster.getDataElements(i, j, data);

                    // Unsigned Conversion is needed?
                    // for (int k=0;k<nBands;k++){
                    // data[k]&=0xffff;
                    // }
                    if (splitBands)
                        for (int k = 0; k < nBands; k++)
                            buf[k].put(data, k, 1);
                    else
                        buf[0].put(data, 0, nBands);
                }
            }
        }
        // ////////////////////////////////////////////////////////////////
        //
        // DataType: Int32 (int)
        //
        // ////////////////////////////////////////////////////////////////
        else if (dataType == gdalconstConstants.GDT_Int32) {
            // //
            //
            // Initializing Data Buffer
            //
            // //
            final IntBuffer buf[] = new IntBuffer[nBands];
            for (int k = 0; k < nBands; k++) {
                bandsBuffer[k] = ByteBuffer.allocateDirect(capacity);
                bandsBuffer[k].order(ByteOrder.nativeOrder());
                buf[k] = bandsBuffer[k].asIntBuffer();
                if (!splitBands)
                    break;
            }
            int[] data = new int[nBands];
            // //
            //
            // Getting Data from the specified region with the requested
            // subsampling factors
            //
            // //
            for (int j = firstY; j <= lastY; j++) {
                if (((j - srcRegionYOffset) % ySubsamplingFactor) != 0) {
                    continue;
                }

                for (int i = firstX; i <= lastX; i++) {
                    if (((i - srcRegionXOffset) % xSubsamplingFactor) != 0) {
                        continue;
                    }
                    data = (int[]) raster.getDataElements(i, j, data);
                    if (splitBands)
                        for (int k = 0; k < nBands; k++)
                            buf[k].put(data, k, 1);
                    else
                        buf[0].put(data, 0, nBands);
                }
            }
        }
        // ////////////////////////////////////////////////////////////////
        //
        // DataType: Float32 (float)
        //
        // ////////////////////////////////////////////////////////////////
        else if (dataType == gdalconstConstants.GDT_Float32) {
            // //
            //
            // Initializing Data Buffer
            //
            // //
            final FloatBuffer buf[] = new FloatBuffer[nBands];
            for (int k = 0; k < nBands; k++) {
                bandsBuffer[k] = ByteBuffer.allocateDirect(capacity);
                bandsBuffer[k].order(ByteOrder.nativeOrder());
                buf[k] = bandsBuffer[k].asFloatBuffer();
                if (!splitBands)
                    break;
            }
            float[] data = new float[nBands];
            // //
            //
            // Getting Data from the specified region with the requested
            // subsampling factors
            //
            // //
            for (int j = firstY; j <= lastY; j++) {
                if (((j - srcRegionYOffset) % ySubsamplingFactor) != 0) {
                    continue;
                }
                for (int i = firstX; i <= lastX; i++) {
                    if (((i - srcRegionXOffset) % xSubsamplingFactor) != 0) {
                        continue;
                    }
                    data = (float[]) raster.getDataElements(i, j, data);
                    if (splitBands)
                        for (int k = 0; k < nBands; k++)
                            buf[k].put(data, k, 1);
                    else
                        buf[0].put(data, 0, nBands);
                }
            }
        }
        // ////////////////////////////////////////////////////////////////
        //
        // DataType: Float64 (double)
        //
        // ////////////////////////////////////////////////////////////////
        else if (dataType == gdalconstConstants.GDT_Float64) {
            // //
            //
            // Initializing Data Buffer
            //
            // //
            final DoubleBuffer buf[] = new DoubleBuffer[nBands];
            for (int k = 0; k < nBands; k++) {
                bandsBuffer[k] = ByteBuffer.allocateDirect(capacity);
                bandsBuffer[k].order(ByteOrder.nativeOrder());
                buf[k] = bandsBuffer[k].asDoubleBuffer();
                if (!splitBands)
                    break;
            }
            double[] data = new double[nBands];
            // //
            //
            // Getting Data from the specified region with the requested
            // subsampling factors
            //
            // //
            for (int j = firstY; j <= lastY; j++) {
                if (((j - srcRegionYOffset) % ySubsamplingFactor) != 0) {
                    continue;
                }
                for (int i = firstX; i <= lastX; i++) {
                    if (((i - srcRegionXOffset) % xSubsamplingFactor) != 0) {
                        continue;
                    }
                    data = (double[]) raster.getDataElements(i, j, data);
                    if (splitBands)
                        for (int k = 0; k < nBands; k++)
                            buf[k].put(data, k, 1);
                    else
                        buf[0].put(data, 0, nBands);
                }
            }
        }
        return bandsBuffer;
    }

    /**
     * Returns a proper <code>ByteBuffer</code> array containing data loaded
     * from the input <code>Raster</code>. Requested portion of data is
     * specified by means of a set of index parameters. In case
     * <code>splitBands</code> is <code>true</code> the array will contain
     * <code>nBands</code> <code>ByteBuffer</code>'s, each one containing
     * data element for a single band. In case <code>splitBands</code> is
     * <code>false</code>, the returned array has a single
     * <code>ByteBuffer</code> containing data for different bands stored as
     * PixelInterleaved
     * 
     * @param raster
     *                the input <code>Raster</code> containing data to be
     *                retrieved for the future write operation
     * @param firstX
     *                X index of the first data element to be scanned
     * @param firstY
     *                Y index of the first data element to be scanned
     * @param lastX
     *                X index of the last data element to be scanned
     * @param lastY
     *                Y index of the last data element to be scanned
     * @param dataType
     *                the datatype
     * @param nBands
     *                the number of bands
     * @param splitBands
     *                when <code>false</code>, a single
     *                <code>ByteBuffer</code> is created. When
     *                <code>true</code>, a number of buffer equals to the
     *                number of bands will be created and each buffer will
     *                contain data elements for a single band
     * @param capacity
     *                the size of each <code>ByteBuffer</code> contained in
     *                the returned array
     * @return a proper <code>ByteBuffer</code> array containing data loaded
     *         from the input <code>Raster</code>.
     * 
     * @throws IllegalArgumentException
     *                 in case the requested region is not valid. This could
     *                 happen in the following cases: <code>
     *             <UL>
     *             <LI> firstX > lastX </LI>
     *             <LI> firstY > lastY </LI>
     * </UL>
     * </code>
     */
    private ByteBuffer[] getDataRegion(Raster raster, final int firstX,
            final int firstY, final int lastX, final int lastY,
            final int dataType, final int nBands, final boolean splitBands,
            final int capacity) {

        if (firstX > lastX || firstY > lastY)
            throw new IllegalArgumentException(
                    "The requested region is not valid");
        // TODO: provide a more user-friendly error message containing ranges
        // and values set.

        // //
        //
        // ByteBuffer containing data
        //
        // //
        ByteBuffer[] bandsBuffer;
        if (splitBands)
            bandsBuffer = new ByteBuffer[nBands];
        else
            bandsBuffer = new ByteBuffer[1];

        // ////////////////////////////////////////////////////////////////
        //
        // DataType: Byte
        //
        // ////////////////////////////////////////////////////////////////
        if (dataType == gdalconstConstants.GDT_Byte) {
            // //
            //
            // Initializing Data Buffer
            //
            // //
            for (int k = 0; k < nBands; k++) {
                bandsBuffer[k] = ByteBuffer.allocateDirect(capacity);
                if (!splitBands)
                    break;
            }
            byte[] data = new byte[nBands];
            // //
            //
            // Getting Data from the specified region
            //
            // //
            for (int j = firstY; j <= lastY; j++) {
                for (int i = firstX; i <= lastX; i++) {
                    data = (byte[]) raster.getDataElements(i, j, data);
                    if (splitBands)
                        for (int k = 0; k < nBands; k++)
                            bandsBuffer[k].put(data, k, 1);
                    else
                        bandsBuffer[0].put(data, 0, nBands);
                }
            }
        }
        // ////////////////////////////////////////////////////////////////
        //
        // DataType: Int16 (short)
        //
        // ////////////////////////////////////////////////////////////////
        else if (dataType == gdalconstConstants.GDT_Int16) {
            // //
            //
            // Initializing Data Buffer
            //
            // //
            final ShortBuffer buf[] = new ShortBuffer[nBands];
            for (int k = 0; k < nBands; k++) {
                bandsBuffer[k] = ByteBuffer.allocateDirect(capacity);
                bandsBuffer[k].order(ByteOrder.nativeOrder());
                buf[k] = bandsBuffer[k].asShortBuffer();
                if (!splitBands)
                    break;
            }
            short[] data = new short[nBands];
            // //
            //
            // Getting Data from the specified region
            //
            // //
            for (int j = firstY; j <= lastY; j++) {
                for (int i = firstX; i <= lastX; i++) {
                    data = (short[]) raster.getDataElements(i, j, data);
                    if (splitBands)
                        for (int k = 0; k < nBands; k++)
                            buf[k].put(data, k, 1);
                    else
                        buf[0].put(data, 0, nBands);
                }
            }
        }
        // ////////////////////////////////////////////////////////////////
        //
        // DataType: UInt16 (short)
        //
        // ////////////////////////////////////////////////////////////////
        else if (dataType == gdalconstConstants.GDT_UInt16) {
            // //
            //
            // Initializing Data Buffer
            //
            // //
            final ShortBuffer buf[] = new ShortBuffer[nBands];
            for (int k = 0; k < nBands; k++) {
                bandsBuffer[k] = ByteBuffer.allocateDirect(capacity);
                bandsBuffer[k].order(ByteOrder.nativeOrder());
                buf[k] = bandsBuffer[k].asShortBuffer();
                if (!splitBands)
                    break;
            }
            short[] data = new short[nBands];
            // //
            //
            // Getting Data from the specified region
            //
            // //
            for (int j = firstY; j <= lastY; j++) {
                for (int i = firstX; i <= lastX; i++) {
                    data = (short[]) raster.getDataElements(i, j, data);

                    // Unsigned Conversion is needed?
                    // for (int k=0;k<nBands;k++){
                    // data[k]&=0xffff;
                    // }
                    if (splitBands)
                        for (int k = 0; k < nBands; k++)
                            buf[k].put(data, k, 1);
                    else
                        buf[0].put(data, 0, nBands);
                }
            }
        }
        // ////////////////////////////////////////////////////////////////
        //
        // DataType: Int32 (int)
        //
        // ////////////////////////////////////////////////////////////////
        else if (dataType == gdalconstConstants.GDT_Int32) {
            // //
            //
            // Initializing Data Buffer
            //
            // //
            final IntBuffer buf[] = new IntBuffer[nBands];
            for (int k = 0; k < nBands; k++) {
                bandsBuffer[k] = ByteBuffer.allocateDirect(capacity);
                bandsBuffer[k].order(ByteOrder.nativeOrder());
                buf[k] = bandsBuffer[k].asIntBuffer();
                if (!splitBands)
                    break;
            }
            int[] data = new int[nBands];
            // //
            //
            // Getting Data from the specified region
            //
            // //
            for (int j = firstY; j <= lastY; j++) {
                for (int i = firstX; i <= lastX; i++) {
                    data = (int[]) raster.getDataElements(i, j, data);
                    if (splitBands)
                        for (int k = 0; k < nBands; k++)
                            buf[k].put(data, k, 1);
                    else
                        buf[0].put(data, 0, nBands);
                }
            }
        }
        // ////////////////////////////////////////////////////////////////
        //
        // DataType: Float32 (float)
        //
        // ////////////////////////////////////////////////////////////////
        else if (dataType == gdalconstConstants.GDT_Float32) {
            // //
            //
            // Initializing Data Buffer
            //
            // //
            final FloatBuffer buf[] = new FloatBuffer[nBands];
            for (int k = 0; k < nBands; k++) {
                bandsBuffer[k] = ByteBuffer.allocateDirect(capacity);
                bandsBuffer[k].order(ByteOrder.nativeOrder());
                buf[k] = bandsBuffer[k].asFloatBuffer();
                if (!splitBands)
                    break;
            }
            float[] data = new float[nBands];
            // //
            //
            // Getting Data from the specified region
            //
            // //
            for (int j = firstY; j <= lastY; j++) {
                for (int i = firstX; i <= lastX; i++) {
                    data = (float[]) raster.getDataElements(i, j, data);
                    if (splitBands)
                        for (int k = 0; k < nBands; k++)
                            buf[k].put(data, k, 1);
                    else
                        buf[0].put(data, 0, nBands);
                }
            }
        }
        // ////////////////////////////////////////////////////////////////
        //
        // DataType: Float64 (double)
        //
        // ////////////////////////////////////////////////////////////////
        else if (dataType == gdalconstConstants.GDT_Float64) {
            // //
            //
            // Initializing Data Buffer
            //
            // //
            final DoubleBuffer buf[] = new DoubleBuffer[nBands];
            for (int k = 0; k < nBands; k++) {
                bandsBuffer[k] = ByteBuffer.allocateDirect(capacity);
                bandsBuffer[k].order(ByteOrder.nativeOrder());
                buf[k] = bandsBuffer[k].asDoubleBuffer();
                if (!splitBands)
                    break;
            }
            double[] data = new double[nBands];
            // //
            //
            // Getting Data from the specified region
            //
            // //
            for (int j = firstY; j <= lastY; j++) {
                for (int i = firstX; i <= lastX; i++) {
                    data = (double[]) raster.getDataElements(i, j, data);
                    if (splitBands)
                        for (int k = 0; k < nBands; k++)
                            buf[k].put(data, k, 1);
                    else
                        buf[0].put(data, 0, nBands);
                }
            }
        }
        return bandsBuffer;
    }

    /**
     * Given an input <code>RenderedImage</code> builds a temporary
     * <code>Dataset</code> and fill it with data from the input image. Source
     * region settings are allowed in order to specify the desired portion of
     * input image which need to be used to populate the dataset.
     * 
     * @param inputRenderedImage
     *                the input <code>RenderedImage</code> from which to get
     *                data
     * @param tempFile
     *                a fileName where to store the temporary dataset
     * @param sourceRegion
     *                a <code>Rectangle</code> specifying the desired portion
     *                of the input image which need to be used to populate the
     *                dataset.
     * @param nBands
     *                the number of the bands of the created dataset
     * @param dataType
     *                the dataType of the created dataset.
     * @param width
     *                the width of the created dataset
     * @param height
     *                the height of the created dataset
     * @param xSubsamplingFactor
     *                the X subsampling factor which need to be used when
     *                loading data from the input image
     * @param ySubsamplingFactor
     *                the Y subsampling factor which need to be used when
     *                loading data from the input image
     * @return a <code>Dataset</code> containing data coming from the input
     *         image
     */
    private Dataset createDatasetFromImage(RenderedImage inputRenderedImage,
            final String tempFile, Rectangle sourceRegion, final int nBands,
            final int dataType, final int width, final int height,
            final int xSubsamplingFactor, final int ySubsamplingFactor) {

        // //
        //
        // Attempting to build a "In memory" raster dataset
        //
        // //

        Dataset tempDs = null;
        final int threshold = getMaxMemorySizeForGDALMemoryDataset();
        final int neededMemory = width * height * nBands
                * gdal.GetDataTypeSize(dataType) / 8;

        if (neededMemory <= threshold) {
            // TODO: the real Memory Raster Driver use should create a Memory
            // Dataset from data in memory by specifying the address of the
            // memory containing data.
            tempDs = getMemoryDriver().Create(tempFile, width, height, nBands,
                    dataType, (String[])null);
        }
        if (tempDs == null) {
            // //
            //
            // Unable to allocate memory for In memory raster dataset
            // Using a GTiff driver to create a temp dataset
            //
            // //
            final Driver driver = gdal.GetDriverByName("GTiff");
            tempDs = driver.Create(tempFile, width, height, nBands, dataType,
                    (String[])null);
        }

        // //
        //
        // Writing data in the temp dataset and return it
        //
        // //
        return writeData(tempDs, inputRenderedImage, sourceRegion, nBands,
                dataType, xSubsamplingFactor, ySubsamplingFactor);
    }

    public IIOMetadata getDefaultImageMetadata(ImageTypeSpecifier imageType,ImageWriteParam param) {
    	
        final GDALWritableCommonIIOImageMetadata imageMetadata = new GDALWritableCommonIIOImageMetadata();
        SampleModel sm = imageType.getSampleModel();

        final int sourceWidth = sm.getWidth();
        final int sourceHeight = sm.getHeight();
        final int sourceMinX = 0;
        final int sourceMinY = 0;
        final int dataType = GDALUtilities.retrieveGDALDataBufferType(sm.getDataType());
        final int nBands = sm.getNumBands();

        // //
        //
        // Setting regions and sizes and retrieving parameters
        //
        // //
        Rectangle imageBounds = new Rectangle(sourceMinX, sourceMinY, sourceWidth, sourceHeight);
        Dimension destSize = new Dimension();
        computeRegions(imageBounds, destSize, param);
        imageMetadata.setBasicInfo(destSize.width, destSize.height, nBands);
        // TODO:provides additional settings

        return imageMetadata;
    }

    public IIOMetadata convertStreamMetadata(IIOMetadata inData,
            ImageWriteParam param) {
        throw new UnsupportedOperationException(
                "convertStreamMetadata not supported yet.");
    }

    public IIOMetadata convertImageMetadata(IIOMetadata inData,
            ImageTypeSpecifier imageType, ImageWriteParam param) {

        throw new UnsupportedOperationException(
                "convertImageMetadata not supported yet. Create a new GDALWritableCommonIIOImageMetadata and set required fields");

        // if (inData == null) {
        // throw new IllegalArgumentException("inData == null!");
        // }
        // if (imageType == null) {
        // throw new IllegalArgumentException("imageType == null!");
        // }
        // if (inData instanceof GDALCommonIIOImageMetadata) {
        // return inData;
        // }
        //
        // GDALCommonIIOImageMetadata im = (GDALCommonIIOImageMetadata)
        // getDefaultImageMetadata(
        // imageType, param);
        //
        // convertMetadata(IMAGE_METADATA_NAME, inData, im);
        //
        // return im;
    }

    /**
     * Sets the destination to the given <code>Object</code>, usually a
     * <code>File</code> or a {@link FileImageOutputStreamExt}.
     * 
     * @param output
     *                the <code>Object</code> to use for future writing.
     */
    public void setOutput(Object output) {
        super.setOutput(output); // validates output
        if (output instanceof File)
            outputFile = (File) output;
        else if (output instanceof FileImageOutputStreamExt)
            outputFile = ((FileImageOutputStreamExt) output).getFile();
        else if (output instanceof URL) {
            final URL tempURL = (URL) output;
            if (tempURL.getProtocol().equalsIgnoreCase("file")) {
                    outputFile = Utilities.urlToFile(tempURL);
            }
            else
                throw new IllegalArgumentException("Not a Valid Input");
        }
    }

    /**
     * This method is a shorthand for <code>write(null, image, null)</code>.
     * 
     * @param image
     *                an <code>IIOImage</code> object containing an image,
     *                thumbnails, and metadata to be written to the output.
     */
    public void write(IIOImage image) throws IOException {
        write(null, image, null);
    }

    /**
     * This method is a shorthand for <code>write(null, new IIOImage(image,
     * null, null), null)</code>.
     * 
     * @param image
     *                a <code>RenderedImage</code> to be written.
     */
    public void write(RenderedImage image) throws IOException {
        write(null, new IIOImage(image, null, null), null);
    }

    /**
     * Compute the source region and destination dimensions taking any parameter
     * settings into account.
     */
    private static void computeRegions(Rectangle sourceBounds,
            Dimension destSize, ImageWriteParam p) {
        int periodX = 1;
        int periodY = 1;
        if (p != null) {
            int[] sourceBands = p.getSourceBands();
            if (sourceBands != null
                    && (sourceBands.length != 1 || sourceBands[0] != 0)) {
                throw new IllegalArgumentException("Cannot sub-band image!");
            }

            // ////////////////////////////////////////////////////////////////
            //
            // Get source region and subsampling settings
            //
            // ////////////////////////////////////////////////////////////////
            Rectangle sourceRegion = p.getSourceRegion();
            if (sourceRegion != null) {
                // Clip to actual image bounds
                sourceRegion = sourceRegion.intersection(sourceBounds);
                sourceBounds.setBounds(sourceRegion);
            }

            // Get subsampling factors
            periodX = p.getSourceXSubsampling();
            periodY = p.getSourceYSubsampling();

            // Adjust for subsampling offsets
            int gridX = p.getSubsamplingXOffset();
            int gridY = p.getSubsamplingYOffset();
            sourceBounds.x += gridX;
            sourceBounds.y += gridY;
            sourceBounds.width -= gridX;
            sourceBounds.height -= gridY;
        }

        // ////////////////////////////////////////////////////////////////////
        //
        // Compute output dimensions
        //
        // ////////////////////////////////////////////////////////////////////
        destSize.setSize((sourceBounds.width + periodX - 1) / periodX,
                (sourceBounds.height + periodY - 1) / periodY);
        if (destSize.width <= 0 || destSize.height <= 0) {
            throw new IllegalArgumentException("Empty source region!");
        }
    }
}
