/*
 *    JImageIO-extension - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
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
package it.geosolutions.imageio.plugins.grib1;

import it.geosolutions.imageio.core.CoreCommonImageMetadata;
import it.geosolutions.imageio.ndplugin.BaseImageMetadata;
import it.geosolutions.imageio.ndplugin.BaseImageReader;

import java.io.IOException;

import javax.imageio.metadata.IIOMetadataNode;

import org.w3c.dom.Node;

/**
 * @author Daniele Romagnoli, GeoSolutions
 */
public class GRIB1ImageMetadata extends BaseImageMetadata {

    // final Calendar baseTime = PDS.getGMTBaseTime();

    public static final String nativeMetadataFormatName = "it_geosolutions_imageio_plugins_grib1_grib1ImageMetadata_1.0";

    private final static String driverName = "GRIB1";

    private final static String driverDescription = "Gridded Binary";

    public final static String GDS = "GridDefinitionSection";

    public final static String PDS = "ProductDefinitionSection";
    
    public final static String UNIT = "Unit";

    public final static String GRID_DELTA_X = "GridDeltaX";

    public final static String GRID_DELTA_Y = "GridDeltaY";
    
    public final static String GRID_N_X = "GridNX";

    public final static String GRID_N_Y = "GridNY";

    public final static String GRID_LAT_1 = "GridLat1";

    public final static String GRID_LAT_2 = "GridLat2";

    public final static String GRID_LON_1 = "GridLon1";

    public final static String GRID_LON_2 = "GridLon2";

    public final static String GRID_LATIN_1 = "GridLatin1";

    public final static String GRID_LATIN_2 = "GridLatin2";

    public final static String GRID_STARTX = "GridStartX";

    public final static String GRID_STARTY = "GridStartY";

    public final static String GRID_LOV = "GridLov";

    public final static String GRID_ROTATION_ANGLE = "GridRotationAngle";

    public final static String GRID_LAT_SP = "GridLatSP";

    public final static String GRID_LON_SP = "GridLonSP";

    public final static String GRID_LAT_SPST = "GridLatSPST";

    public final static String GRID_LON_SPST = "GridLonSPST";

    public final static String GRID_TYPE = "GridType";

    public final static String PROD_TIME_RANGE_INDICATOR = "ProductTimeRangeIndicator";
    
    public final static String PROD_TIME = "ProductTime";

    public final static String PROD_PARAMETER_DESCRIPTOR = "ParameterDescriptor";

    public final static String PROD_PARAMTABLE_CENTERID = "CenterID";

    public final static String PROD_PARAMTABLE_SUBCENTERID = "SubcenterID";

    public final static String PROD_PARAMTABLE_TABLEVERSION = "TableVersion";

    public final static String PROD_PARAMTABLE_PARAMETERNUMBER = "ParameterNumber";

    public final static String PDS_LEVEL = "PDS_Level";

    public final static String PDSL_NAME = "name";
    
    public final static String PDSL_ID = "id";

    public final static String PDSL_ISNUMERIC = "isNumeric";

    public final static String PDSL_LEVEL = "level";

    public final static String PDSL_DESCRIPTION = "description";

    public final static String PDSL_UNITS = "units";

    public final static String PDSL_VALUES = "values";

    public GRIB1ImageMetadata(final BaseImageReader reader, final int imageIndex) {
        super(checkReaderType(reader), imageIndex);
    }

    private static BaseImageReader checkReaderType(final BaseImageReader reader) {
    	if(reader instanceof GRIB1ImageReader)
    		return reader;
    	throw new IllegalArgumentException("Provided reader is of type "+reader.getClass().getCanonicalName());
	}

	@Override
    protected void setMembers(BaseImageReader imageReader) throws IOException {
        super.setMembers(imageReader);
        if (imageReader instanceof GRIB1ImageReader) {
            final int imageIndex = getImageIndex();
            GRIB1ImageReader reader = (GRIB1ImageReader) imageReader;
            setDatasetName(reader.getRecordName(imageIndex));
            setDatasetDescription(reader.getRecordDescription(imageIndex));
            setDriverDescription(driverDescription);
            setDriverName(driverName);
        }
    }

    /**
     * Returns an XML DOM <code>Node</code> object that represents the root of
     * a tree of common stream metadata contained within this object according
     * to the conventions defined by a given metadata format name.
     * 
     * @param formatName
     *                the name of the requested metadata format.
     */
    public Node getAsTree(String formatName) {
        if (GRIB1ImageMetadata.nativeMetadataFormatName
                .equalsIgnoreCase(formatName))
            return createNativeTree();
        else if (CoreCommonImageMetadata.nativeMetadataFormatName
                .equalsIgnoreCase(formatName))
            return super.createCommonNativeTree();
        throw new IllegalArgumentException(formatName
                + " is not a supported format name");
    }

    private Node createNativeTree() {
        final IIOMetadataNode root = new IIOMetadataNode(GRIB1ImageMetadata.nativeMetadataFormatName);

        
            // ////////////////////////////////////////////////////////////////////
            //
            // GridDefinitionSection
            //
            // ////////////////////////////////////////////////////////////////////
            IIOMetadataNode gdsNode = new IIOMetadataNode(GDS);
            GRIB1ImageReader flatReader = (GRIB1ImageReader) imageReader;
            final int imageIndex = getImageIndex();
            try {
                final int gridType = flatReader.getGridType(imageIndex);
                gdsNode.setAttribute(GRID_TYPE, Integer.toString(gridType));

                gdsNode.setAttribute(GRID_DELTA_X, Double.toString(flatReader.getGridDeltaX(imageIndex)));
                gdsNode.setAttribute(GRID_DELTA_Y, Double.toString(flatReader.getGridDeltaY(imageIndex)));
                gdsNode.setAttribute(GRID_N_X, Double.toString(flatReader.getWidth(imageIndex)));
                gdsNode.setAttribute(GRID_N_Y, Double.toString(flatReader.getHeight(imageIndex)));
                gdsNode.setAttribute(GRID_LON_1, Double.toString(flatReader.getGridLon1(imageIndex)));
                gdsNode.setAttribute(GRID_LAT_1, Double.toString(flatReader.getGridLat1(imageIndex)));
                gdsNode.setAttribute(GRID_LON_2, Double.toString(flatReader.getGridLon2(imageIndex)));
                gdsNode.setAttribute(GRID_LAT_2, Double.toString(flatReader.getGridLat2(imageIndex)));
                gdsNode.setAttribute(GRID_LON_SP, Double.toString(flatReader.getGridLonSP(imageIndex)));
                gdsNode.setAttribute(GRID_LAT_SP, Double.toString(flatReader.getGridLatSP(imageIndex)));
                gdsNode.setAttribute(GRID_ROTATION_ANGLE, Double.toString(flatReader.getGridRotationAngle(imageIndex)));

                switch (gridType) {
                case 3:
                    gdsNode.setAttribute(GRID_STARTX, Double.toString(flatReader.getGridStartX(imageIndex)));
                    gdsNode.setAttribute(GRID_STARTY, Double.toString(flatReader.getGridStartY(imageIndex)));
                    gdsNode.setAttribute(GRID_LATIN_1, Double.toString(flatReader.getGridLatin1(imageIndex)));
                    gdsNode.setAttribute(GRID_LATIN_2, Double.toString(flatReader.getGridLatin2(imageIndex)));
                    gdsNode.setAttribute(GRID_LOV, Double.toString(flatReader.getGridLov(imageIndex)));

                    break;
                case 10:
                    gdsNode.setAttribute(GRID_LON_SPST, Double.toString(flatReader.getGridLonSPST(imageIndex)));
                    gdsNode.setAttribute(GRID_LAT_SPST, Double.toString(flatReader.getGridLatSPST(imageIndex)));
                }

                root.appendChild(gdsNode);
                // ////////////////////////////////////////////////////////////////////
                //
                // ProductDefinitionSection
                //
                // ////////////////////////////////////////////////////////////////////
                IIOMetadataNode pdsNode = new IIOMetadataNode(PDS);

                pdsNode.setAttribute(PROD_TIME, flatReader.getTime(imageIndex));
                pdsNode.setAttribute(PROD_TIME_RANGE_INDICATOR, Integer.toString(flatReader.getTimeRangeIndicator(imageIndex)));
                pdsNode.setAttribute(UNIT, flatReader.getRecordUnit(imageIndex));
                
                final int[] params = flatReader
                        .getParameterDescriptor(imageIndex);
                if (params != null) {
                    pdsNode.setAttribute(PROD_PARAMTABLE_CENTERID, Integer.toString(params[0]));
                    pdsNode.setAttribute(PROD_PARAMTABLE_SUBCENTERID, Integer.toString(params[1]));
                    pdsNode.setAttribute(PROD_PARAMTABLE_TABLEVERSION, Integer.toString(params[2]));
                    pdsNode.setAttribute(PROD_PARAMTABLE_PARAMETERNUMBER,Integer.toString(params[3]));
                }
                root.appendChild(pdsNode);
                IIOMetadataNode pdsLevelNode = new IIOMetadataNode(PDS_LEVEL);
                pdsLevelNode.setAttribute(PDSL_ID, flatReader.getPDSLevelIndex(imageIndex));
                pdsLevelNode.setAttribute(PDSL_NAME, flatReader.getPDSLevelName(imageIndex));
                pdsLevelNode.setAttribute(PDSL_DESCRIPTION, flatReader.getPDSLevelDescription(imageIndex));
                pdsLevelNode.setAttribute(PDSL_UNITS, flatReader.getPDSLevelUnits(imageIndex));
                pdsLevelNode.setAttribute(PDSL_LEVEL, flatReader.getPDSLevelShortName(imageIndex));
                pdsLevelNode.setAttribute(PDSL_ISNUMERIC, flatReader.getPDSLevelIsNumeric(imageIndex));
                pdsLevelNode.setAttribute(PDSL_VALUES, flatReader.getPDSLevelValues(imageIndex));
                root.appendChild(pdsLevelNode);

            } catch (IOException e) {
                throw new IllegalArgumentException("Unable to parse attribute", e);
            }
        return root;
    }
}
