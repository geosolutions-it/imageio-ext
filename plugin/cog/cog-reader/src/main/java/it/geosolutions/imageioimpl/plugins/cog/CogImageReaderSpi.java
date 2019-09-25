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
import java.util.Locale;

/**
 * SPI for creating `CogImageReader`
 *
 * @author joshfix
 * Created on 2019-09-18
 */
public class CogImageReaderSpi extends TIFFImageReaderSpi {

    public CogImageReaderSpi() {
        super();
        super.setReaderClassName(CogImageReader.class.getCanonicalName());
    }

    @Override
    public String getDescription(Locale locale) {
        return PackageUtil.getSpecificationTitle() + " COG Image Reader";
    }

    @Override
    public ImageReader createReaderInstance(Object extension) {
        return new CogImageReader(this);
    }

}
