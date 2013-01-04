/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2007 - 2011, GeoSolutions
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
package it.geosolutions.imageio.plugins.turbojpeg;

import java.util.Locale;

import it.geosolutions.imageio.plugins.exif.EXIFMetadata;

import javax.imageio.ImageWriteParam;
import org.libjpegturbo.turbojpeg.TJ;

/**
 * Class holding Write parameters to customize the write operations
 * 
 * @author Daniele Romagnoli, GeoSolutions SaS
 * @author Emanuele Tajariol, GeoSolutions SaS
 */
public class TurboJpegImageWriteParam extends ImageWriteParam {
	
    public TurboJpegImageWriteParam() {
        this(Locale.getDefault());
    }

    public TurboJpegImageWriteParam(Locale locale) {
        super(locale);
        // fix compression type
        this.compressionTypes = new String[] { DEFAULT_COMPRESSION_SCHEME };

    }

    public final static String DEFAULT_COMPRESSION_SCHEME = "JPEG";

    public final static float DEFAULT_COMPRESSION_QUALITY = 0.75f;
    
    public final static int DEFAULT_RGB_COMPONENT_SUBSAMPLING = TJ.SAMP_420;
        
    private int componentSubsampling = -1;
    
    private EXIFMetadata exif;
	
    @Override
    public boolean canWriteCompressed() {
        return true;
    }

    @Override
    public boolean canWriteTiles() {
        return false;
    }
    
    public EXIFMetadata getExif() {
        return exif;
    }

    public void setExif(EXIFMetadata exif) {
        this.exif = exif;
    }

    /**
     * @param componentSubsampling the componentSubsampling to set.
     * It represents the Chrominance subsampling factor applied by the turbojpeg library. Supported values are:
     * 
     * <ul>
     * <li> {@linkplain TurboJpegLibrary#TJ_444} : 4:4:4 chrominance subsampling (no chrominance subsampling).<BR>
     *  The JPEG or YUV image will contain one chrominance component for every pixel in the source image.</li>
     *  <li> {@linkplain TurboJpegLibrary#TJ_422} : 4:2:2 chrominance subsampling. <BR>
     *  The JPEG or YUV image will contain one chrominance component for every 2x1 block of pixels in the source image.</li>
     *  <li> {@linkplain TurboJpegLibrary#TJ_420} : 4:2:0 chrominance subsampling. <BR>
     *  The JPEG or YUV image will contain one chrominance component for every 2x2 block of pixels in the source image..</li>
     *  <li> {@linkplain TurboJpegLibrary#TJ_GRAYSCALE} : Grayscale. <BR>
     *  The JPEG or YUV image will contain no chrominance components</li>
     * </ul> 
     * 
     */
    public void setComponentSubsampling(int componentSubsampling) {
        this.componentSubsampling = componentSubsampling;
    }

    /**
     * @return the componentSubsampling
     */
    public int getComponentSubsampling() {
        return componentSubsampling;
    }
   
}
