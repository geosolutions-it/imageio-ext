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

import it.geosolutions.imageio.gdalframework.GDALCreateOption;
import it.geosolutions.imageio.gdalframework.GDALCreateOptionsHandler;

/**
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public class JP2GDALKakaduCreateOptionsHandler extends GDALCreateOptionsHandler {

    /**
     * This information is based on the documentation available at
     * {@link http://www.gdal.org/frmt_jp2kak.html}
     * 
     * Creation Options:<BR>
     * ========================================================================
     * QUALITY=n: Set the compressed size ratio as a percentage of the size of
     * the uncompressed image. The default is 20 indicating that the resulting
     * image should be 20% of the size of the uncompressed image. A value of 100
     * will result in use of the lossless compression algorithm . Actual final
     * image size may not exactly match that requested depending on various
     * factors.<BR>
     * ------------------------------------------------------------------------
     * BLOCKXSIZE=n: Set the tile width to use. Defaults to 20000.<BR>
     * ------------------------------------------------------------------------
     * BLOCKYSIZE=n: Set the tile height to use. Defaults to image height.<BR>
     * ------------------------------------------------------------------------
     * GMLJP2=YES/NO: Indicates whether a GML box conforming to the OGC GML in
     * JPEG2000 specification should be included in the file. Defaults to YES.<BR>
     * ------------------------------------------------------------------------
     * GeoJP2=YES/NO: Indicates whether a GML box conforming to the GeoJP2
     * (GeoTIFF in JPEG2000) specification should be included in the file.
     * Defaults to YES.<BR>
     * ------------------------------------------------------------------------
     * LAYERS=n: Control the number of layers produced. These are sort of like
     * resolution layers, but not exactly. The default value is 12 and this
     * works well in most situations.<BR>
     * ------------------------------------------------------------------------
     * ROI=xoff,yoff,xsize,ysize: Selects a region to be a region of interest to
     * process with higher data quality. The various "R" flags below may be used
     * to control the amount better. For example the settings "ROI=0,0,100,100",
     * "Rweight=7" would encode the top left 100x100 area of the image with
     * considerable higher quality compared to the rest of the image.<BR>
     */
    public JP2GDALKakaduCreateOptionsHandler() {

        final String qualityVV[] = new String[2];
        qualityVV[0] = "1";
        qualityVV[1] = "100";

        addCreateOption(new GDALCreateOption(
                "QUALITY",
                GDALCreateOption.VALIDITYCHECKTYPE_VALUE_BELONGINGRANGE_EXTREMESINCLUDED,
                qualityVV, GDALCreateOption.TYPE_FLOAT));
        // createOptions[0].setDefaultValue("20");

        final String blockXSizeMinVV[] = new String[1];
        blockXSizeMinVV[0] = "0";
        addCreateOption(new GDALCreateOption("BLOCKXSIZE",
                GDALCreateOption.VALIDITYCHECKTYPE_VALUE_GREATERTHAN,
                blockXSizeMinVV, GDALCreateOption.TYPE_INT));
        // createOptions[1].setDefaultValue("20000");

        final String blockYSizeMinVV[] = new String[1];
        blockYSizeMinVV[0] = "0";
        addCreateOption(new GDALCreateOption("BLOCKYSIZE",
                GDALCreateOption.VALIDITYCHECKTYPE_VALUE_GREATERTHAN,
                blockYSizeMinVV, GDALCreateOption.TYPE_INT));

        final String gmljp2VV[] = new String[2];
        gmljp2VV[0] = "YES";
        gmljp2VV[1] = "NO";
        addCreateOption(new GDALCreateOption("GMLJP2",
                GDALCreateOption.VALIDITYCHECKTYPE_ONEOF, gmljp2VV,
                GDALCreateOption.TYPE_STRING));

        final String geojp2VV[] = new String[2];
        geojp2VV[0] = "YES";
        geojp2VV[1] = "NO";
        addCreateOption(new GDALCreateOption("GeoJP2",
                GDALCreateOption.VALIDITYCHECKTYPE_ONEOF, geojp2VV,
                GDALCreateOption.TYPE_STRING));

        final String layersVV[] = new String[2];
        layersVV[0] = "1";
        layersVV[1] = "65535";
        addCreateOption(new GDALCreateOption(
                "Clayers",
                GDALCreateOption.VALIDITYCHECKTYPE_VALUE_BELONGINGRANGE_EXTREMESINCLUDED,
                layersVV, GDALCreateOption.TYPE_INT));

        final String orgGen_pltVV[] = new String[2];
        orgGen_pltVV[0] = "yes";
        orgGen_pltVV[1] = "no";
        addCreateOption(new GDALCreateOption("ORGgen_plt",
                GDALCreateOption.VALIDITYCHECKTYPE_ONEOF, orgGen_pltVV,
                GDALCreateOption.TYPE_STRING));

        // TODO: Check the validity value. 0 ?? "GREATER THAN" OR "GREATER OR
        // EQUAL THAN"?
        final String orgGen_tlmVV[] = new String[1];
        orgGen_tlmVV[0] = "0";
        addCreateOption(new GDALCreateOption("ORGgen_tlm",
                GDALCreateOption.VALIDITYCHECKTYPE_VALUE_GREATERTHANOREQUALTO,
                orgGen_tlmVV, GDALCreateOption.TYPE_INT));

        final String cLevelsVV[] = new String[1];
        cLevelsVV[0] = "1";
        addCreateOption(new GDALCreateOption("Clevels",
                GDALCreateOption.VALIDITYCHECKTYPE_VALUE_GREATERTHANOREQUALTO,
                cLevelsVV, GDALCreateOption.TYPE_INT));

        addCreateOption(new GDALCreateOption("Cprecincts",
                GDALCreateOption.VALIDITYCHECKTYPE_NONE, null,
                GDALCreateOption.TYPE_STRING));

        final String cOrderVV[] = new String[5];
        cOrderVV[0] = "LRCP";
        cOrderVV[1] = "RLCP";
        cOrderVV[2] = "RPCL";
        cOrderVV[3] = "PCRL";
        cOrderVV[4] = "CPRL";

        addCreateOption(new GDALCreateOption("Corder",
                GDALCreateOption.VALIDITYCHECKTYPE_ONEOF, cOrderVV,
                GDALCreateOption.TYPE_STRING));

        final String cModesVV[] = new String[6];
        cModesVV[0] = "BYPASS";
        cModesVV[1] = "RESET";
        cModesVV[2] = "RESTART";
        cModesVV[3] = "CAUSAL";
        cModesVV[4] = "ERTERM";
        cModesVV[5] = "SEGMARK";

        addCreateOption(new GDALCreateOption("Cmodes",
                GDALCreateOption.VALIDITYCHECKTYPE_COMBINATIONOF, cModesVV,
                GDALCreateOption.TYPE_STRING));

        final String cBlkVV[] = new String[1];
        cBlkVV[0] = "{,}";
        addCreateOption(new GDALCreateOption("Cblk",
                GDALCreateOption.VALIDITYCHECKTYPE_STRING_SYNTAX, cBlkVV,
                GDALCreateOption.TYPE_STRING));

        // Add Combined values
        final String orgTpartsVV[] = new String[15];
        orgTpartsVV[0] = "R";
        orgTpartsVV[1] = "C";
        orgTpartsVV[2] = "L";
        addCreateOption(new GDALCreateOption("ORGtparts",
                GDALCreateOption.VALIDITYCHECKTYPE_COMBINATIONOF, orgTpartsVV,
                GDALCreateOption.TYPE_STRING));

        final String comsegVV[] = new String[2];
        comsegVV[0] = "YES";
        comsegVV[1] = "NO";
        addCreateOption(new GDALCreateOption("COMSEG",
                GDALCreateOption.VALIDITYCHECKTYPE_ONEOF, comsegVV,
                GDALCreateOption.TYPE_STRING));

        // The Kakadu Documentation says that typical values for Qguard
        // parameter are 1 or 2. Should eliminate constraints?
        final String qGuardVV[] = new String[2];
        qGuardVV[0] = "1";
        qGuardVV[1] = "3";
        addCreateOption(new GDALCreateOption(
                "Qguard",
                GDALCreateOption.VALIDITYCHECKTYPE_VALUE_BELONGINGRANGE_EXTREMESINCLUDED,
                qGuardVV, GDALCreateOption.TYPE_INT));

        final String qStepVV[] = new String[2];
        qStepVV[0] = "0";
        qStepVV[1] = "2";

        addCreateOption(new GDALCreateOption(
                "Qstep",
                GDALCreateOption.VALIDITYCHECKTYPE_VALUE_BELONGINGRANGE_EXTREMESINCLUDED,
                qStepVV, GDALCreateOption.TYPE_FLOAT));

        final String flushVV[] = new String[2];
        flushVV[0] = "YES";
        flushVV[1] = "NO";
        addCreateOption(new GDALCreateOption("FLUSH",
                GDALCreateOption.VALIDITYCHECKTYPE_ONEOF, flushVV,
                GDALCreateOption.TYPE_STRING));

        final String cYccVV[] = new String[2];
        cYccVV[0] = "yes";
        cYccVV[1] = "no";
        addCreateOption(new GDALCreateOption("Cycc",
                GDALCreateOption.VALIDITYCHECKTYPE_ONEOF, cYccVV,
                GDALCreateOption.TYPE_STRING));

        final String sProfileVV[] = new String[6];
        sProfileVV[0] = "PROFILE0";
        sProfileVV[1] = "PROFILE1";
        sProfileVV[2] = "PROFILE2";
        sProfileVV[3] = "PART2";
        sProfileVV[4] = "CINEMA2K";
        sProfileVV[5] = "CINEMA4K";
        addCreateOption(new GDALCreateOption("SProfile",
                GDALCreateOption.VALIDITYCHECKTYPE_ONEOF, sProfileVV,
                GDALCreateOption.TYPE_STRING));

        final String roiVV[] = new String[1];
        roiVV[0] = ",,,";
        addCreateOption(new GDALCreateOption("ROI",
                GDALCreateOption.VALIDITYCHECKTYPE_STRING_SYNTAX, roiVV,
                GDALCreateOption.TYPE_STRING));

        // TODO: Check the validity values!!!
        final String rShiftVV[] = new String[1];
        rShiftVV[0] = "0";
        addCreateOption(new GDALCreateOption("Rshift",
                GDALCreateOption.VALIDITYCHECKTYPE_VALUE_GREATERTHANOREQUALTO,
                rShiftVV, GDALCreateOption.TYPE_INT));

        // TODO: Check the validity values!!!
        final String rLevelsVV[] = new String[2];
        rLevelsVV[0] = "0";
        rLevelsVV[1] = "32";
        addCreateOption(new GDALCreateOption(
                "Rlevels",
                GDALCreateOption.VALIDITYCHECKTYPE_VALUE_BELONGINGRANGE_EXTREMESINCLUDED,
                rLevelsVV, GDALCreateOption.TYPE_INT));

        // TODO: Check the validity values!!!
        final String rWeightVV[] = new String[1];
        rWeightVV[0] = "0";
        addCreateOption(new GDALCreateOption("Rweight",
                GDALCreateOption.VALIDITYCHECKTYPE_VALUE_GREATERTHAN,
                rWeightVV, GDALCreateOption.TYPE_FLOAT));

    }
}
