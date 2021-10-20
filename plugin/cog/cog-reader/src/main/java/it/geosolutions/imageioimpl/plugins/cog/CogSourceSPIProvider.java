/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://github.com/geosolutions-it/imageio-ext
 *    (C) 2020, GeoSolutions
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
package it.geosolutions.imageioimpl.plugins.cog;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.spi.ImageInputStreamSpi;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import it.geosolutions.imageio.core.BasicAuthURI;
import it.geosolutions.imageio.core.SourceSPIProvider;
import it.geosolutions.imageio.plugins.cog.CogImageReadParam;

/**
 * A @{@link SourceSPIProvider}  subclass containing additional
 * elements for the COG Implementation
 */
public class CogSourceSPIProvider extends SourceSPIProvider {

    private final static Logger LOGGER = Logger.getLogger(CogSourceSPIProvider.class.getName());

    /** The CogUri version of the source */
    private BasicAuthURI cogUri;

    /** The full classname of the RangeReader implementation */
    private String rangeReaderClassname;
    
    private volatile URL cogURL; 

    public CogSourceSPIProvider(
            BasicAuthURI cogUri,
            ImageReaderSpi readerSpi,
            ImageInputStreamSpi streamSpi,
            String rangeReader) {
        super(cogUri, readerSpi, streamSpi);
        this.cogUri = cogUri;
        this.rangeReaderClassname = rangeReader;
    }

    public String getRangeReaderClassname() {
        return rangeReaderClassname;
    }

    public BasicAuthURI getCogUri() {
        return cogUri;
    }

    @Override
    public URL getSourceUrl() throws MalformedURLException {
        if (cogURL == null) {
            synchronized (this) {
                if (cogURL == null) {
                    RangeReader reader = createRangeReaderInstance(rangeReaderClassname, cogUri,
                            CogImageReadParam.DEFAULT_HEADER_LENGTH);
                    if (reader == null) return super.getSourceUrl();
                    cogURL = reader.getURL();            
                }
            }
        }
        return cogURL;
        
    }

    /**
     * Get an initialized COG stream: The Header will be read before
     * returning the stream back to the caller.
     */
    @Override
    public ImageInputStream getStream() throws IOException {
        BasicAuthURI uri = getCogUri();
        CogImageInputStream inStream =
                (CogImageInputStream)
                        ((CogImageInputStreamSpi) getStreamSpi())
                                .createInputStreamInstance(uri, uri.isUseCache(), null);
        RangeReader rangeReader = createRangeReaderInstance(rangeReaderClassname, uri, CogImageReadParam.DEFAULT_HEADER_LENGTH);
        if (rangeReader == null) return null;
        inStream.init(rangeReader);
        return inStream;
    }

    /**
     * Instantiate a new RangeReader based on the specified className implementation,
     * on top of the given URI, using the specified headerLength
     *
     * @param className the complete className of the required RangeReader implementation
     * @param uri the source URI
     * @param headerLength the headerLength
     * @return a RangeReader instance
     */
    public static RangeReader createRangeReaderInstance(String className, BasicAuthURI uri, int headerLength) {
        RangeReader rangeReader = null;
        if (className != null) {
            try {
                final Class<?> clazz = Class.forName(className);
                Constructor constructor = clazz.getConstructor(new Class[] {BasicAuthURI.class, int.class});
                rangeReader =
                        (RangeReader)
                                constructor.newInstance(uri, headerLength);
            } catch (Exception e) {
                if (LOGGER.isLoggable(Level.WARNING)) {
                    LOGGER.warning("Unable to create a RangeReader of type " + className + " on uri: " +
                            uri.getUri().getPath() + " due to " + e.toString());
                }
                rangeReader = null;
            }
        }
        return rangeReader;
    }



    /**
     * Return a compatible SourceProvider (same readerSPI, same streamSPI, same rangeReader,
     * same credentials) for a different URL
     */
    @Override
    public CogSourceSPIProvider getCompatibleSourceProvider (URL url) {
        BasicAuthURI sourceURI = getCogUri();
        BasicAuthURI newSourceUri = new BasicAuthURI(url, sourceURI.isUseCache());
        newSourceUri.setPassword(sourceURI.getPassword());
        newSourceUri.setUser(sourceURI.getUser());
        return new CogSourceSPIProvider(newSourceUri, getReaderSpi(), getStreamSpi(), rangeReaderClassname);
    }
}
