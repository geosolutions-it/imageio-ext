/*
 *    JImageIO-extension - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *	  https://imageio-ext.dev.java.net/
 *    (C) 2007, GeoSolutions
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package it.geosolutions.imageio.gdalframework;

import it.geosolutions.imageio.stream.input.FileImageInputStreamExt;
import it.geosolutions.imageio.stream.input.FileImageInputStreamExtImpl;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ServiceRegistry;
import javax.imageio.stream.ImageInputStream;

import org.gdal.gdal.Dataset;
import org.gdal.gdal.Driver;
import org.gdal.gdalconst.gdalconst;

/**
 * The abstract service provider interface (SPI) for {@link GDALImageReader}s.
 * 
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public abstract class GDALImageReaderSpi extends ImageReaderSpi {
    public void onRegistration(ServiceRegistry registry, Class category) {
        super.onRegistration(registry, category);
        if (!GDALUtilities.isGDALAvailable()) {
            IIORegistry iioRegistry = (IIORegistry) registry;
            Class spiClass = ImageReaderSpi.class;
            final Iterator iter = iioRegistry.getServiceProviders(spiClass,
                    true);
            while (iter.hasNext()) {
                final ImageReaderSpi provider = (ImageReaderSpi) iter.next();
                if (provider instanceof GDALImageReaderSpi) {
                    registry.deregisterServiceProvider(provider);
                }
            }
        }
    }

    private static final Logger LOGGER = Logger
            .getLogger("it.geosolutions.imageio.gdalframework");
    static {
        GDALUtilities.loadGDAL();
    }

    /**
     * <code>List</code> of gdal formats supported by this plugin.
     */
    private List supportedFormats;

    /**
     * Methods returning the formats which are supported by a plugin.
     * 
     * The right value to be returned may be found using the GDAL command:
     * <code> gdalinfo --formats</code> which lists all the supported formats.
     * 
     * As an instance, the result of this command may be:
     * 
     * VRT (rw+): Virtual Raster GTiff (rw+): GeoTIFF NITF (rw+): National
     * Imagery Transmission Format HFA (rw+): Erdas Imagine Images (.img)
     * SAR_CEOS (ro): CEOS SAR Image CEOS (ro): CEOS Image
     * .........................................
     * 
     * You need to set the String returned as the first word (as an instance:
     * "HFA", if you are building a plugin for the Erdas Image Images)
     * 
     * In some circumstances, GDAL provides more than 1 driver to manage a
     * specific format. As an instance, in order to handle HDF4 files, GDAL
     * provides two drivers: HDF4 and HDF4Image (which supports Dataset
     * creation). The HDF4ImageReader will be capable of manage both formats.
     * 
     * To specify different formats, just separate them by the ";" symbol. As an
     * instance: "HDF4;HDF4Image"
     * 
     */
    public List getSupportedFormats() {
        return Collections.unmodifiableList(this.supportedFormats);
    }

    public GDALImageReaderSpi(String vendorName, String version,
            String[] names, String[] suffixes, String[] MIMETypes,
            String readerClassName, Class[] inputTypes,
            String[] writerSpiNames,
            boolean supportsStandardStreamMetadataFormat,
            String nativeStreamMetadataFormatName,
            String nativeStreamMetadataFormatClassName,
            String[] extraStreamMetadataFormatNames,
            String[] extraStreamMetadataFormatClassNames,
            boolean supportsStandardImageMetadataFormat,
            String nativeImageMetadataFormatName,
            String nativeImageMetadataFormatClassName,
            String[] extraImageMetadataFormatNames,
            String[] extraImageMetadataFormatClassNames,
            Collection supportedFormats) {

        super(
                vendorName,
                version,
                names,
                suffixes,
                MIMETypes,
                readerClassName, // readerClassName
                new Class[] { File.class, FileImageInputStreamExt.class },
                writerSpiNames, // writer Spi Names
                supportsStandardStreamMetadataFormat,
                nativeStreamMetadataFormatName,
                nativeStreamMetadataFormatClassName,
                extraStreamMetadataFormatNames,
                extraStreamMetadataFormatClassNames,
                supportsStandardImageMetadataFormat,
                nativeImageMetadataFormatName,
                nativeImageMetadataFormatClassName,
                extraImageMetadataFormatNames,
                extraImageMetadataFormatClassNames);
        this.supportedFormats = new ArrayList(supportedFormats);
    }

    /**
     * Checks if the provided input can be decoded by the specific SPI. When
     * building a new plugin, remember to implement the
     * <code>getSupportedFormat</coded> abstract method.
     * 
     * @return 
     * 		<code>true</code> if the input can be successfully decoded.
     */
    public boolean canDecodeInput(Object input) throws IOException {
        if (LOGGER.isLoggable(Level.FINE))
            LOGGER.fine("Can Decode Input");

        if (input instanceof ImageInputStream)
            ((ImageInputStream) input).mark();

        // if input source is a string,
        // convert input from String to File
        if (input instanceof String)
            input = new File((String) input);

        // if input source is an URL, open an InputStream
        if (input instanceof URL) {
            final URL tempURL = (URL) input;
            if (tempURL.getProtocol().equalsIgnoreCase("file"))
                input = new File(URLDecoder.decode(tempURL.getFile(), "UTF8"));
            else
                input = ((URL) input).openStream();
        }

        // if input source is a File,
        // convert input from File to FileInputStream
        if (input instanceof File)
            input = new FileImageInputStreamExtImpl((File) input);

        boolean isInputDecodable = false;
        // Checking if this specific SPI can decode the provided input
        try {
            final String s = ((FileImageInputStreamExtImpl) input).getFile()
                    .getAbsolutePath();

            final Dataset ds = GDALUtilities.acquireDataSet(s,
                    gdalconst.GA_ReadOnly);
            isInputDecodable = isDecodable(ds);

            // Closing the dataset
            GDALUtilities.closeDataSet(ds);
        } catch (Exception e) {
        } catch (NoClassDefFoundError missingDLLs) {
        }
        return isInputDecodable;
    }

    /**
     * Checks if the provided Dataset was opened by a Driver supporting the same
     * formats which are supported by the specific ImageReaderSpi.
     * 
     * There is a trivial example: Suppose we are implementing a plugin for HDF4
     * format and suppose we are testing the <code>canDecodeInput</code> with
     * a NITF file as input. GDAL will successfully open the NITF file. However,
     * it will use the NITF driver instead of the HDF4 driver. Since NITF is not
     * supported by the HDF4ImageReaderSpi, this method will return return
     * <code>false</code>.
     * 
     * @param dataset
     *                The input dataset
     * 
     * @return <code>true</code> if the format is supported.
     *         <code>false</code> otherwise.
     */
    protected boolean isDecodable(Dataset dataset) {
        if (dataset != null) {
            final Driver driver = dataset.GetDriver();

            // retrieving the format of the provided input.
            // We use the "Description" of the driver which has opened the
            // input.
            final String sDriver = driver.getShortName();

            // ////////////////////////////////////////////////////////////////
            // checking if this format is supported by the specific SPI */
            // ////////////////////////////////////////////////////////////////
            return getSupportedFormats().contains(sDriver);
        }
        return false;
    }

    /**
     * This method tells us if this driver is available or not.
     * 
     * @return <code>true</code> if the driver is available,
     *         <code>false</code> otherwise.
     */
    public boolean isAvailable() {
        // check if gdal is available
        if (!GDALUtilities.isGDALAvailable())
            return false;
        // now check that all the drivers are available
        final List supportedFormats = getSupportedFormats();
        final Iterator it = supportedFormats.iterator();
        if (!it.hasNext())
            return false;
        while (it.hasNext()) {
            final String formatName = (String) it.next();
            if (!GDALUtilities.isDriverAvailable(formatName))
                return false;
        }
        return true;
    }
}
