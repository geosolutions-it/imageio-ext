/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2019, GeoSolutions
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

import com.sun.media.imageioimpl.common.PackageUtil;
import it.geosolutions.imageioimpl.plugins.tiff.TIFFImageReaderSpi;

import javax.imageio.ImageReader;
import javax.imageio.spi.ServiceRegistry;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.util.Locale;

/**
 * SPI for creating `CogImageReader`
 *
 * @author joshfix
 * Created on 2019-09-18
 */
public class CogImageReaderSpi extends TIFFImageReaderSpi {

    public CogImageReaderSpi() {
        super(CogImageReader.class.getCanonicalName());
    }

    @Override
    public String getDescription(Locale locale) {
        return PackageUtil.getSpecificationTitle() + " COG Image Reader";
    }

    @Override
    public ImageReader createReaderInstance(Object extension) {
        return new CogImageReader(this);
    }

    @Override
    public boolean canDecodeInput(Object input) throws IOException {
        // the input stream for a cog must be an instance of CogImageInputStream
        if (!(input instanceof CogImageInputStream)) {
            return false;
        }

        // cog input streams must have the header initialized before they can be used in any capacity
        if (!((CogImageInputStream)input).isInitialized()) {
            return false;
        }

        ImageInputStream stream = (ImageInputStream) input;
        byte[] b = new byte[4];
        stream.mark();
        stream.readFully(b);
        stream.reset();

        return (
                ((b[0] == (byte) 0x49 && b[1] == (byte) 0x49 &&
                        b[2] == (byte) 0x2a && b[3] == (byte) 0x00) ||
                        (b[0] == (byte) 0x4d && b[1] == (byte) 0x4d &&
                                b[2] == (byte) 0x00 && b[3] == (byte) 0x2a)) ||

                        ((b[0] == (byte) 0x49 && b[1] == (byte) 0x49 &&
                                b[2] == (byte) 0x2b && b[3] == (byte) 0x00) ||
                                (b[0] == (byte) 0x4d && b[1] == (byte) 0x4d &&
                                        b[2] == (byte) 0x00 && b[3] == (byte) 0x2b))
        );
    }

    @Override
    public void onRegistration(ServiceRegistry registry, Class category) {
        // Override the onRegistration so that we can have COG reader and
        // TIFF ImageReader coexist
        if (registered) {
            return;
        }
        registered = true;
    }

}
