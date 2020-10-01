/*
 * $RCSfile: TIFFImageReaderSpi.java,v $
 *
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All  Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistribution of source code must retain the above copyright
 *   notice, this  list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN MIDROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL
 * NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR
 * ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed or intended for
 * use in the design, construction, operation or maintenance of any
 * nuclear facility.
 *
 * $Revision: 1.2 $
 * $Date: 2006/03/31 19:43:41 $
 * $State: Exp $
 */
/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2007 - 2009, GeoSolutions
 *    All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of GeoSolutions nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY GeoSolutions ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL GeoSolutions BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package it.geosolutions.imageioimpl.plugins.tiff;

import com.sun.media.imageioimpl.common.PackageUtil;
import it.geosolutions.imageio.utilities.ImageIOUtilities;

import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageReaderWriterSpi;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.spi.ServiceRegistry;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;

public class TIFFImageReaderSpi extends ImageReaderSpi {

    private static final String[] names = {"tif", "TIF", "tiff", "TIFF", "btiff", "BTIFF"};

    private static final String[] suffixes = {"tif", "tiff", "tf8", "btf", "TIF", "TIFF", "TF8", "BTF"};

    private static final String[] MIMETypes = {"image/tiff", "image/tiff; application=geotiff"};

    protected String readerClassName;

    protected String[] writerSpiNames;

    protected boolean registered = false;

    public TIFFImageReaderSpi() {
        this("it.geosolutions.imageioimpl.plugins.tiff.TIFFImageReader",
                new String[] {"it.geosolutions.imageioimpl.plugins.tiff.TIFFImageWriterSpi"});
    }

    public TIFFImageReaderSpi(String readerClassName) {
        this(readerClassName, new String[] {"it.geosolutions.imageioimpl.plugins.tiff.TIFFImageWriterSpi"});
    }

    public TIFFImageReaderSpi(String readerClassName, String[] writerSpiNames) {
        super("ImageIO-Ext",
                "1.0",
                names,
                suffixes,
                MIMETypes,
                readerClassName,
                STANDARD_INPUT_TYPE,
                writerSpiNames,
                false,
                TIFFStreamMetadata.nativeMetadataFormatName,
                "it.geosolutions.imageioimpl.plugins.tiff.TIFFStreamMetadataFormat",
                null, null,
                true,
                TIFFImageMetadata.nativeMetadataFormatName,
                "it.geosolutions.imageioimpl.plugins.tiff.TIFFImageMetadataFormat",
                new String[]{""}, new String[]{""}
        );

        this.readerClassName = readerClassName;
        this.writerSpiNames = writerSpiNames;
    }

    public String getDescription(Locale locale) {
        String desc = PackageUtil.getSpecificationTitle() +
                " TIFF Image Reader";
        return desc;
    }

    public boolean canDecodeInput(Object input) throws IOException {
        if (!(input instanceof ImageInputStream)) {
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

    public ImageReader createReaderInstance(Object extension) {
        return new TIFFImageReader(this);
    }

    @Override
    public void onRegistration(ServiceRegistry registry, Class category) {
        super.onRegistration(registry, category);
        if (registered) {
            return;
        }
        registered = true;
        Iterator<ImageReaderWriterSpi> readers = ImageIOUtilities
                .getImageReaderWriterSPI(registry, new TIFFFilter(true), "TIFF", true).iterator();
        while (readers.hasNext()) {
            final ImageReaderSpi spi = (ImageReaderSpi) readers.next();
            if (spi == this) {
                continue;
            }
            registry.deregisterServiceProvider(spi);
            registry.setOrdering(category, this, spi);
        }
    }

    /**
     * Filter which returns <code>true</code> if and only if the provider is an ImageReader/WriterSpi which supports the TIFF format.
     */
    static class TIFFFilter implements ServiceRegistry.Filter {
        boolean isReader;

        TIFFFilter(boolean isReader) {
            this.isReader = isReader;
        }

        public boolean filter(Object provider) {
            boolean isSupportedSpi = isReader ? provider instanceof ImageReaderSpi
                    : provider instanceof ImageWriterSpi;
            if (!isSupportedSpi) {
                return false;
            }

            ImageReaderWriterSpi spi = (ImageReaderWriterSpi) provider;
            String[] formatNames = spi.getFormatNames();
            for (int i = 0; i < formatNames.length; i++) {
                if (formatNames[i].equalsIgnoreCase("TIFF")) {
                    return true;
                }
            }

            return false;
        }
    }
}
