/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://imageio-ext.dev.java.net/
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
package it.geosolutions.imageio.matfile5;

import it.geosolutions.imageio.stream.input.FileImageInputStreamExtImpl;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import com.jmatio.io.MatlabIOException;

/**
 * The abstract service provider interface (SPI) for {@link MatFileImageReader}s.
 * 
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public abstract class MatFileImageReaderSpi extends ImageReaderSpi {

    static final String[] suffixes = { "mat" };

    static final String[] formatNames = { "Mat", "Mat5" }; // TODO: Improve this

    static final String[] MIMETypes = { "image/mat" };

    static final String version = "1.0";

    static final String readerCN = "it.geosolutions.imageio.matfile5.MatFileImageReader";

    static final String vendorName = "GeoSolutions";

    // writerSpiNames
    static final String[] wSN = { null };

    // StreamMetadataFormatNames and StreamMetadataFormatClassNames
    static final boolean supportsStandardStreamMetadataFormat = false;

    static final String nativeStreamMetadataFormatName = null;

    static final String nativeStreamMetadataFormatClassName = null;

    static final String[] extraStreamMetadataFormatNames = { null };

    static final String[] extraStreamMetadataFormatClassNames = { null };

    // ImageMetadataFormatNames and ImageMetadataFormatClassNames
    static final boolean supportsStandardImageMetadataFormat = false;

    static final String nativeImageMetadataFormatName = "";

    static final String nativeImageMetadataFormatClassName = "";

    static final String[] extraImageMetadataFormatNames = { null };

    static final String[] extraImageMetadataFormatClassNames = { null };

    private static final String MAT_FILE_5 = "MATLAB 5.0 MAT-file.*";

    private static final Logger LOGGER = Logger
            .getLogger("it.geosolutions.imageio.matfile5");

    
    
    public MatFileImageReaderSpi() {
        super(
                vendorName,
                version,
                formatNames,
                suffixes,
                MIMETypes,
                readerCN, // readerClassName
                new Class[] { File.class, FileImageInputStreamExtImpl.class },
                wSN, // writer Spi Names
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
    }

    public MatFileImageReaderSpi(String vendorName, String version,
			String[] formatNames, String[] suffixes, String[] types,
			String readerCN, Class[] classes, String[] wsn,
			boolean supportsStandardStreamMetadataFormat,
			String nativeStreamMetadataFormatName,
			String nativeStreamMetadataFormatClassName,
			String[] extraStreamMetadataFormatNames,
			String[] extraStreamMetadataFormatClassNames,
			boolean supportsStandardImageMetadataFormat,
			String nativeImageMetadataFormatName,
			String nativeImageMetadataFormatClassName,
			String[] extraImageMetadataFormatNames,
			String[] extraImageMetadataFormatClassNames) {
    	 super(
                 vendorName,
                 version,
                 formatNames,
                 suffixes,
                 types,
                 readerCN, // readerClassName
                 classes,
                 wsn, // writer Spi Names
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
	}

    /**
     * Checks if the provided input can be decoded by the specific SPI. 
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
            final String file = ((FileImageInputStreamExtImpl) input).getFile()
                    .getAbsolutePath();

            isInputDecodable = isDecodable(file);

        } catch (Exception e) {
        }
        return isInputDecodable;
    }

    protected boolean isDecodable(final String file)
            throws IOException {
        if (file == null || file.trim().length() < 1)
            return false;
        RandomAccessFile raFile = new RandomAccessFile(file, "r");
        FileChannel roChannel = raFile.getChannel();
        ByteBuffer buf = ByteBuffer.allocateDirect(128);
        roChannel.read(buf, 0);
        buf.rewind();
        try {
            checkHeader(buf);
        } catch (MatlabIOException mlio) {
            if (LOGGER.isLoggable(Level.FINE))
                LOGGER.fine("This is not a valid MATLAB 5.0 MAT-file.");
            return false;
        } finally {
            if (roChannel != null) {
                roChannel.close();
            }
            if (raFile != null) {
                raFile.close();
            }
        }
        return true;
    }

    private void checkHeader(ByteBuffer buf) throws IOException {
        // header values
        String description;
        // descriptive text 116 bytes
        byte[] descriptionBuffer = new byte[116];
        buf.get(descriptionBuffer);
        description = new String(descriptionBuffer);

        if (!description.matches(MAT_FILE_5)) {
            throw new MatlabIOException(
                    "This is not a valid MATLAB 5.0 MAT-file.");
        }
    }

    @Override
    public String getDescription(Locale locale) {
        return new StringBuffer("MATLAB 5.0 MAT-Files Image Reader, version ")
                .append(version).toString();
    }

}
