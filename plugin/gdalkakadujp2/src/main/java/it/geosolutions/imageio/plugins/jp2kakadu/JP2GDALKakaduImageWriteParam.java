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
package it.geosolutions.imageio.plugins.jp2kakadu;

import it.geosolutions.imageio.gdalframework.GDALImageWriteParam;

import java.util.Locale;

import com.sun.media.imageio.plugins.jpeg2000.J2KImageWriteParam;

/**
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public class JP2GDALKakaduImageWriteParam extends GDALImageWriteParam {

    public JP2GDALKakaduImageWriteParam() {
        super(new J2KImageWriteParam(Locale.getDefault()),
                new JP2GDALKakaduCreateOptionsHandler());
    }

    public JP2GDALKakaduImageWriteParam(Locale l) {
        super(new J2KImageWriteParam(l),
                new JP2GDALKakaduCreateOptionsHandler(), l);
    }

    /**
     * This information is based on the documentation available at
     * {@link http://www.gdal.org/frmt_jp2kak.html}
     * 
     * Creation Options:<BR />
     * 
     * QUALITY=n: Set the compressed size ratio as a percentage of the size of
     * the uncompressed image. The default is 20 indicating that the resulting
     * image should be 20% of the size of the uncompressed image. A value of 100
     * will result in use of the lossless compression algorithm . Actual final
     * image size may not exactly match that requested depending on various
     * factors.<BR />
     * 
     * BLOCKXSIZE=n: Set the tile width to use. Defaults to 20000.<BR />
     * 
     * BLOCKYSIZE=n: Set the tile height to use. Defaults to image height.<BR />
     * 
     * GMLJP2=YES/NO: Indicates whether a GML box conforming to the OGC GML in
     * JPEG2000 specification should be included in the file. Defaults to YES.<BR />
     * 
     * GeoJP2=YES/NO: Indicates whether a GML box conforming to the GeoJP2
     * (GeoTIFF in JPEG2000) specification should be included in the file.
     * Defaults to YES.<BR />
     * 
     * LAYERS=n: Control the number of layers produced. These are sort of like
     * resolution layers, but not exactly. The default value is 12 and this
     * works well in most situations.<BR />
     * 
     * ROI=xoff,yoff,xsize,ysize: Selects a region to be a region of interest to
     * process with higher data quality. The various "R" flags below may be used
     * to control the amount better. For example the settings "ROI=0,0,100,100",
     * "Rweight=7" would encode the top left 100x100 area of the image with
     * considerable higher quality compared to the rest of the image.<BR />
     * 
     * COMSEG=YES/NO: Indicates wheter a comment segment should be emitted
     * during the flushing of the written codestream. Defaults to YES.
     * 
     * FLUSH=YES/NO: Indicates wheter multiple flush operations should be
     * enabled during tiles writing. Defaults to YES.
     * 
     */

    /**
     * Setting GeoJP2 Create Option
     */
    public void setGeoJp2(String geoJp2) {
        createOptionsHandler.setCreateOption("GeoJP2", geoJp2.toUpperCase());
    }

    /**
     * Setting ORGgen_plt Create Option
     */
    public void setORGgen_plt(String ORGgen_plt) {
        createOptionsHandler.setCreateOption("ORGgen_plt", ORGgen_plt
                .toLowerCase());
    }

    /**
     * Setting ORGgen_tlm Create Option
     */
    public void setORGgen_tlm(final int ORGgen_tlm) {
        createOptionsHandler.setCreateOption("ORGgen_tlm", ORGgen_tlm);
    }

    /**
     * Setting ORGtparts Create Option
     */
    public void setORGtparts(String orgTparts) {
        createOptionsHandler.setCreateOption("ORGtparts", orgTparts);
    }

    /**
     * Setting GMLJp2 Create Option
     */
    public void setGMLJp2(String gmlJp2) {
        createOptionsHandler.setCreateOption("GMLJP2", gmlJp2.toUpperCase());
    }

    /**
     * Setting Clayers Create Option
     */
    public void setLayers(final int layers) {
        createOptionsHandler.setCreateOption("Clayers", layers);
    }

    /**
     * Setting Clayers Create Option
     */
    public void setClayers(final int layers) {
        setLayers(layers);
    }

    /**
     * Setting Cycc Create Option
     */
    public void setCycc(String cycc) {
        createOptionsHandler.setCreateOption("Cycc", cycc);
    }

    /**
     * Setting Clevels Create Option
     */
    public void setClevels(final int clevels) {
        createOptionsHandler.setCreateOption("Clevels", clevels);
    }

    /**
     * Setting Qguard Create Option
     */
    public void setQguard(final int qguard) {
        createOptionsHandler.setCreateOption("Qguard", qguard);
    }

    /**
     * Setting Qstep Create Option
     */
    public void setQstep(final float qstep) {
        createOptionsHandler.setCreateOption("Qstep", qstep);
    }

    /**
     * Setting ROI Create Option
     */
    public void setRoi(String roi) {
        createOptionsHandler.setCreateOption("ROI", roi);
    }

    /**
     * Setting Quality Create Option
     * 
     * @param quality
     *                the compressed size ratio as a percentage of the size of
     *                the uncompressed image.
     */
    public void setQuality(final float quality) {
        createOptionsHandler.setCreateOption("QUALITY", (quality));
    }

    /**
     * Setting Cprecincts Create Option
     */
    public void setCprecincts(String cPrecincts) {
        createOptionsHandler.setCreateOption("Cprecincts", cPrecincts);
    }

    /**
     * Setting Corder Create Option
     */
    public void setCorder(String cOrder) {
        createOptionsHandler.setCreateOption("Corder", cOrder);
    }

    /**
     * Setting Cblk Create Option
     */
    public void setCblk(String cBlk) {
        createOptionsHandler.setCreateOption("Cblk", cBlk);
    }

    /**
     * Setting SProfile Create Option using an int identifier
     */

    public void setSProfile(final int sProfile) {
        final String profile;
        switch (sProfile) {
        case 0:
            profile = "PROFILE0";
            break;
        case 1:
            profile = "PROFILE1";
            break;
        case 2:
            profile = "PROFILE2";
            break;
        case 3:
            profile = "PART2";
            break;
        case 4:
            profile = "CINEMA2K";
            break;
        case 5:
            profile = "CINEMA4K";
            break;
        default:
            profile = "WRONG!!";
            break;
        }
        createOptionsHandler.setCreateOption("SProfile", profile);
    }

    /**
     * Setting SProfile Create Option using a String identifier
     */
    public void setSProfile(final String sProfile) {
        createOptionsHandler.setCreateOption("SProfile", sProfile);
    }

    /**
     * Setting Cmodes Create Option
     */
    public void setCmodes(String cModes) {
        createOptionsHandler.setCreateOption("Cmodes", cModes);

    }

    /**
     * Setting COMSEG Create Option
     */
    public void setComseg(String comseg) {
        createOptionsHandler.setCreateOption("COMSEG", comseg);
    }

    /**
     * Setting ROI Create Option
     */
    public void setROI(String roi) {
        createOptionsHandler.setCreateOption("ROI", roi);
    }

    /**
     * Setting Rshift Create Option
     */
    public void setRshift(int rShift) {
        createOptionsHandler.setCreateOption("Rshift", rShift);
    }

    /**
     * Setting Rlevels Create Option
     */
    public void setRlevels(int rLevels) {
        createOptionsHandler.setCreateOption("Rlevels", rLevels);
    }

    /**
     * Setting Rweight Create Option
     */
    public void setRweight(float rWeight) {
        createOptionsHandler.setCreateOption("Rweight", rWeight);
    }

    /**
     * Setting FLUSH Create Option
     */
    public void setFlush(String flush) {
        createOptionsHandler.setCreateOption("FLUSH", flush);

    }

    /**
     * Setting tile properites.
     */
    public void setTiling(final int tileWidth, final int tileHeight) {
        super.setTiling(tileWidth, tileHeight);
        createOptionsHandler.setCreateOption("BLOCKXSIZE", Integer
                .toString(tileWidth));
        createOptionsHandler.setCreateOption("BLOCKYSIZE", Integer
                .toString(tileHeight));
    }

    public boolean canWriteTiles() {
        return true;
    }

}
