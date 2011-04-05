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
package it.geosolutions.imageio.plugins.geotiff;

import it.geosolutions.imageio.gdalframework.GDALCreateOption;
import it.geosolutions.imageio.gdalframework.GDALCreateOptionsHandler;

/**
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public class GeoTiffCreateOptionsHandler extends GDALCreateOptionsHandler {

    /**
     * This information is based on the documentation available at
     * {@link http://www.gdal.org/frmt_gtiff.html}
     * 
     * Creation Options:<BR />
     * 
     * TFW=YES: Force the generation of an associated ESRI world file (.tfw).See
     * a World Files section for details..<BR />
     * 
     * INTERLEAVE=[BAND/PIXEL]: By default TIFF files with band interleaving
     * (PLANARCONFIG_SEPARATE in TIFF terminology) are created. These are
     * slightly more efficient for some purposes, but some applications only
     * support pixel interleaved TIFF files. In these cases pass
     * INTERLEAVE=PIXEL to produce pixel interleaved TIFF files
     * (PLANARCONFIG_CONTIG in TIFF terminology).<BR />
     * 
     * TILED=YES: By default stripped TIFF files are created. This option can be
     * used to force creation of tiled TIFF files.<BR />
     * 
     * BLOCKXSIZE=n: Sets tile width, defaults to 256.<BR />
     * 
     * BLOCKYSIZE=n: Set tile or strip height. Tile height defaults to 256,
     * strip height defaults to a value such that one strip is 8K or less.<BR />
     * 
     * COMPRESS=[JPEG/LZW/PACKBITS/DEFLATE/NONE]: Set the compression to use.
     * JPEG should only be used with Byte data. None is the default..<BR />
     * 
     * PREDICTOR=[1/2/3]: Set the predictor for LZW or DEFLATE compression. The
     * default is 1 (no predictor), 2 is horizontal differencing and 3 is
     * floating point prediction..<BR />
     * 
     * JPEG_QUALITY=[1-100]: Set the JPEG quality when using JPEG compression. A
     * value of 100 is best quality (least compression), and 1 is worst quality
     * (best compression). The default is 75.<BR />
     * 
     * PROFILE=[GDALGeoTIFF/GeoTIFF/BASELINE]: Control what non-baseline tags
     * are emitted by GDAL. With GDALGeoTIFF (the default) various GDAL custom
     * tags may be written. With GeoTIFF only GeoTIFF tags will be added to the
     * baseline. With BASELINE no GDAL or GeoTIFF tags will be written. BASELINE
     * is occasionally useful when writing files to be read by applications
     * intolerant of unrecognized tags.<BR />
     * 
     * PHOTOMETRIC=[MINISBLACK/MINISWHITE/RGB/CMYK/YCBCR/CIELAB/ICCLAB/ITULAB]:
     * Set the photometric interpretation tag. Default is MINISBLACK, but if the
     * input image has 3 or 4 bands of Byte type, then RGB will be selected. You
     * can override default photometric using this option.<BR />
     * 
     * ALPHA=YES: The first "extrasample" is marked as being alpha if there are
     * any extra samples. This is necessary if you want to produce a gray scale
     * TIFF file with an alpha band (for instance).<BR />
     */

    public GeoTiffCreateOptionsHandler() {

        final String tfwVV[] = new String[1];
        tfwVV[0] = "YES";
        addCreateOption(new GDALCreateOption("TFW",
                GDALCreateOption.VALIDITYCHECKTYPE_VALUE, tfwVV,
                GDALCreateOption.TYPE_STRING));

        final String interleaveVV[] = new String[2];
        interleaveVV[0] = "BAND";
        interleaveVV[1] = "PIXEL";
        addCreateOption(new GDALCreateOption("INTERLEAVE",
                GDALCreateOption.VALIDITYCHECKTYPE_ONEOF, interleaveVV,
                GDALCreateOption.TYPE_STRING));

        final String tiledVV[] = new String[1];
        tiledVV[0] = "YES";
        addCreateOption(new GDALCreateOption("TILED",
                GDALCreateOption.VALIDITYCHECKTYPE_VALUE, tiledVV,
                GDALCreateOption.TYPE_STRING));

        final String blockXSizeMinVV[] = new String[1];
        blockXSizeMinVV[0] = "0";
        addCreateOption(new GDALCreateOption("BLOCKXSIZE",
                GDALCreateOption.VALIDITYCHECKTYPE_VALUE_GREATERTHAN,
                blockXSizeMinVV, GDALCreateOption.TYPE_INT));

        final String blockYSizeMinVV[] = new String[1];
        blockYSizeMinVV[0] = "0";
        addCreateOption(new GDALCreateOption("BLOCKYSIZE",
                GDALCreateOption.VALIDITYCHECKTYPE_VALUE_GREATERTHAN,
                blockYSizeMinVV, GDALCreateOption.TYPE_INT));

        final String compressVV[] = new String[5];
        compressVV[0] = "JPEG";
        compressVV[1] = "LZW";
        compressVV[2] = "PACKBITS";
        compressVV[3] = "DEFLATE";
        compressVV[4] = "NONE";

        addCreateOption(new GDALCreateOption("COMPRESS",
                GDALCreateOption.VALIDITYCHECKTYPE_ONEOF, compressVV,
                GDALCreateOption.TYPE_STRING));

        final String predictorVV[] = new String[3];
        predictorVV[0] = "1";
        predictorVV[1] = "2";
        predictorVV[2] = "3";
        addCreateOption(new GDALCreateOption("PREDICTOR",
                GDALCreateOption.VALIDITYCHECKTYPE_ONEOF, predictorVV,
                GDALCreateOption.TYPE_INT));

        final String jpegQualityVV[] = new String[2];
        jpegQualityVV[0] = "1";
        jpegQualityVV[1] = "100";
        addCreateOption(new GDALCreateOption(
                "JPEG_QUALITY",
                GDALCreateOption.VALIDITYCHECKTYPE_VALUE_BELONGINGRANGE_EXTREMESINCLUDED,
                jpegQualityVV, GDALCreateOption.TYPE_INT));

        final String profileVV[] = new String[3];
        profileVV[0] = "GDALGeoTIFF";
        profileVV[1] = "GeoTIFF";
        profileVV[1] = "BASELINE";
        addCreateOption(new GDALCreateOption("PROFILE",
                GDALCreateOption.VALIDITYCHECKTYPE_ONEOF, profileVV,
                GDALCreateOption.TYPE_STRING));

        final String photometricVV[] = new String[8];
        photometricVV[0] = "MINISBLACK";
        photometricVV[1] = "MINISWHITE";
        photometricVV[2] = "RGB";
        photometricVV[3] = "CMYK";
        photometricVV[4] = "YCBCR";
        photometricVV[5] = "CIELAB";
        photometricVV[6] = "ICCLAB";
        photometricVV[7] = "ITULAB";

        addCreateOption(new GDALCreateOption("PHOTOMETRIC",
                GDALCreateOption.VALIDITYCHECKTYPE_ONEOF, photometricVV,
                GDALCreateOption.TYPE_STRING));

        final String alphaVV[] = new String[1];
        alphaVV[0] = "YES";
        addCreateOption(new GDALCreateOption("ALPHA",
                GDALCreateOption.VALIDITYCHECKTYPE_VALUE, alphaVV,
                GDALCreateOption.TYPE_STRING));
    }
}
