/*
 *    JImageIO-extension - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    (C) 2007, GeoSolutions
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
package it.geosolutions.imageio.plugins.grib1;

import it.geosolutions.imageio.core.CoreCommonImageMetadata;
import it.geosolutions.imageio.ndplugin.BaseImageMetadata;
import it.geosolutions.imageio.ndplugin.BaseImageReader;
import it.geosolutions.imageio.plugins.grib1.GRIB1ImageReader.GribVariableWrapper;
import it.geosolutions.imageio.plugins.grib1.GRIB1ImageReader.VerticalLevel;
import it.geosolutions.imageio.plugins.netcdf.NetCDFUtilities.KeyValuePair;
import java.io.IOException;
import java.util.List;
import javax.imageio.metadata.IIOMetadataNode;
import org.w3c.dom.Node;

/** @author Daniele Romagnoli, GeoSolutions */
public class GRIB1ImageMetadata extends BaseImageMetadata {

    // final Calendar baseTime = PDS.getGMTBaseTime();

    public static final String nativeMetadataFormatName =
            "it_geosolutions_imageio_plugins_grib1_grib1ImageMetadata_1.0";

    private static final String driverName = "GRIB1";

    private static final String driverDescription = "Gridded Binary";

    public static final String GDS = "GridDefinitionSection";

    public static final String PDS = "ProductDefinitionSection";

    public static final String GRID_DELTA_X = "Dx";

    public static final String GRID_DELTA_Y = "Dy";

    public static final String GRID_N_X = "GridNX";

    public static final String GRID_N_Y = "GridNY";

    public static final String GRID_LAT_1 = "La1";

    public static final String GRID_LAT_2 = "La2";

    //  public final static String GRID_LAT_1 = "GridLat1";
    //  public final static String GRID_LAT_2 = "GridLat2";
    //  public final static String GRID_LON_1 = "GridLon1";
    //  public final static String GRID_LON_2 = "GridLon2";
    //  public final static String GRID_DELTA_X = "GridDeltaX";
    //  public final static String GRID_DELTA_Y = "GridDeltaY";
    //  public final static String GRID_TYPE = "GridType";

    public static final String GRID_TYPE = "grid_type";

    public static final String GRID_LON_1 = "Lo1";

    public static final String GRID_LON_2 = "Lo2";

    public static final String GRID_LATIN_1 = "GridLatin1";

    public static final String GRID_LATIN_2 = "GridLatin2";

    public static final String GRID_STARTX = "GridStartX";

    public static final String GRID_STARTY = "GridStartY";

    public static final String GRID_LOV = "GridLov";

    public static final String GRID_ROTATION_ANGLE = "GridRotationAngle";

    public static final String GRID_LAT_SP = "GridLatSP";

    public static final String GRID_LON_SP = "GridLonSP";

    public static final String GRID_LAT_SPST = "GridLatSPST";

    public static final String GRID_LON_SPST = "GridLonSPST";

    public static final String PROD_TIME_RANGE_INDICATOR = "ProductTimeRangeIndicator";

    public static final String PROD_TIME = "ProductTime";

    public static final String PROD_TIME_NAME = "Time";

    public static final String PROD_TIME_UNITS = "TimeUnits";

    public static final String PROD_TIME_VALUES = "TimeValues";

    public static final String PROD_DEFINITION_TYPE = "DefinitionType";

    public static final String PROD_PARAMETER_DESCRIPTOR = "ParameterDescriptor";

    public static final String PROD_PARAMETER_NAME = "ParameterName";

    public static final String PROD_PARAMTABLE_CENTERID = "CenterID";

    public static final String PROD_PARAMTABLE_SUBCENTERID = "SubcenterID";

    public static final String PROD_PARAMTABLE_TABLEVERSION = "TableVersion";

    public static final String PROD_PARAMTABLE_PARAMETERNUMBER = "ParameterNumber";

    public static final String PROD_PARAMETER_UNIT = "Unit";

    public static final String PDS_LEVEL = "PDS_Level";

    public static final String PDSL_NAME = "name";

    public static final String PDSL_ID = "id";

    public static final String PDSL_ISNUMERIC = "isNumeric";

    public static final String PDSL_LEVEL = "level";

    public static final String PDSL_DESCRIPTION = "description";

    public static final String PDSL_UNITS = "units";

    public static final String PDSL_POSITIVE = "positive";

    public static final String PDSL_VALUES = "values";

    public static final String PDSL_AXISTYPE = "axisType";

    public GRIB1ImageMetadata(final BaseImageReader reader, final int imageIndex) {
        super(checkReaderType(reader), imageIndex);
    }

    private static BaseImageReader checkReaderType(final BaseImageReader reader) {
        if (reader instanceof GRIB1ImageReader) return reader;
        throw new IllegalArgumentException(
                "Provided reader is of type " + reader.getClass().getCanonicalName());
    }

    @Override
    protected void setMembers(BaseImageReader imageReader) throws IOException {
        super.setMembers(imageReader);
        if (imageReader instanceof GRIB1ImageReader) {
            final int imageIndex = getImageIndex();
            GRIB1ImageReader reader = (GRIB1ImageReader) imageReader;
            setDatasetName(reader.getInnerReader().getVariableName(imageIndex));
            setDriverDescription(driverDescription);
            setDriverName(driverName);
        }
    }

    /**
     * Returns an XML DOM <code>Node</code> object that represents the root of a tree of common stream metadata
     * contained within this object according to the conventions defined by a given metadata format name.
     *
     * @param formatName the name of the requested metadata format.
     */
    public Node getAsTree(String formatName) {
        if (GRIB1ImageMetadata.nativeMetadataFormatName.equalsIgnoreCase(formatName)) return createNativeTree();
        else if (CoreCommonImageMetadata.nativeMetadataFormatName.equalsIgnoreCase(formatName))
            return super.createCommonNativeTree();
        throw new IllegalArgumentException(formatName + " is not a supported format name");
    }

    private Node createNativeTree() {
        final IIOMetadataNode root = new IIOMetadataNode(GRIB1ImageMetadata.nativeMetadataFormatName);

        // ////////////////////////////////////////////////////////////////////
        //
        // GridDefinitionSection
        //
        // ////////////////////////////////////////////////////////////////////
        IIOMetadataNode gdsNode = new IIOMetadataNode(GDS);
        GRIB1ImageReader directReader = (GRIB1ImageReader) imageReader;
        final int imageIndex = getImageIndex();
        final GribVariableWrapper wrapper =
                (GribVariableWrapper) directReader.getInnerReader().getVariableWrapper(imageIndex);
        List<KeyValuePair> coordsAttribute = directReader.getCoordinateAttributes();
        for (KeyValuePair pair : coordsAttribute) {
            gdsNode.setAttribute(pair.getKey(), pair.getValue());
        }
        //                final int gridType = directReader.getGridType(imageIndex);
        //                gdsNode.setAttribute(GRID_TYPE, Integer.toString(gridType));
        //
        //                gdsNode.setAttribute(GRID_DELTA_X, Double.toString(directReader.getGridDeltaX(imageIndex)));
        //                gdsNode.setAttribute(GRID_DELTA_Y, Double.toString(directReader.getGridDeltaY(imageIndex)));
        //                gdsNode.setAttribute(GRID_N_X, Double.toString(directReader.getWidth(imageIndex)));
        //                gdsNode.setAttribute(GRID_N_Y, Double.toString(directReader.getHeight(imageIndex)));
        //                gdsNode.setAttribute(GRID_LON_1, Double.toString(directReader.getGridLon1(imageIndex)));
        //                gdsNode.setAttribute(GRID_LAT_1, Double.toString(directReader.getGridLat1(imageIndex)));
        //                gdsNode.setAttribute(GRID_LON_2, Double.toString(directReader.getGridLon2(imageIndex)));
        //                gdsNode.setAttribute(GRID_LAT_2, Double.toString(directReader.getGridLat2(imageIndex)));
        //                gdsNode.setAttribute(GRID_LON_SP, Double.toString(directReader.getGridLonSP(imageIndex)));
        //                gdsNode.setAttribute(GRID_LAT_SP, Double.toString(directReader.getGridLatSP(imageIndex)));
        //                gdsNode.setAttribute(GRID_ROTATION_ANGLE,
        // Double.toString(directReader.getGridRotationAngle(imageIndex)));
        //
        //                switch (gridType) {
        //                case 3:
        //                    gdsNode.setAttribute(GRID_STARTX,
        // Double.toString(directReader.getGridStartX(imageIndex)));
        //                    gdsNode.setAttribute(GRID_STARTY,
        // Double.toString(directReader.getGridStartY(imageIndex)));
        //                    gdsNode.setAttribute(GRID_LATIN_1,
        // Double.toString(directReader.getGridLatin1(imageIndex)));
        //                    gdsNode.setAttribute(GRID_LATIN_2,
        // Double.toString(directReader.getGridLatin2(imageIndex)));
        //                    gdsNode.setAttribute(GRID_LOV, Double.toString(directReader.getGridLov(imageIndex)));
        //
        //                    break;
        //                case 10:
        //                    gdsNode.setAttribute(GRID_LON_SPST,
        // Double.toString(directReader.getGridLonSPST(imageIndex)));
        //                    gdsNode.setAttribute(GRID_LAT_SPST,
        // Double.toString(directReader.getGridLatSPST(imageIndex)));
        //                }
        //
        root.appendChild(gdsNode);
        // ////////////////////////////////////////////////////////////////////
        //
        // ProductDefinitionSection
        //
        // ////////////////////////////////////////////////////////////////////
        IIOMetadataNode pdsNode = new IIOMetadataNode(PDS);
        //
        //                pdsNode.setAttribute(PROD_TIME, wrapper.getTime(imageIndex));
        //                pdsNode.setAttribute(PROD_TIME_RANGE_INDICATOR,
        // Integer.toString(directReader.getTimeRangeIndicator(imageIndex)));

        pdsNode.setAttribute(PROD_DEFINITION_TYPE, wrapper.getProductDefinitionType());
        pdsNode.setAttribute(PROD_TIME_NAME, wrapper.getTimeName());
        pdsNode.setAttribute(PROD_TIME_UNITS, wrapper.getTimeUnits());
        pdsNode.setAttribute(PROD_TIME_VALUES, wrapper.getTimeValues(imageIndex));

        pdsNode.setAttribute(PROD_PARAMETER_NAME, wrapper.getParameterName());
        pdsNode.setAttribute(PROD_PARAMTABLE_CENTERID, Integer.toString(wrapper.getParameterCenterID()));
        //                    pdsNode.setAttribute(PROD_PARAMTABLE_SUBCENTERID, Integer.toString(params[1]));
        pdsNode.setAttribute(PROD_PARAMTABLE_TABLEVERSION, Integer.toString(wrapper.getParameterTableVersion()));
        pdsNode.setAttribute(PROD_PARAMTABLE_PARAMETERNUMBER, Integer.toString(wrapper.getParameterNumber()));
        pdsNode.setAttribute(PROD_PARAMETER_UNIT, wrapper.getParameterUnit());

        root.appendChild(pdsNode);
        IIOMetadataNode pdsLevelNode = new IIOMetadataNode(PDS_LEVEL);
        VerticalLevel level = wrapper.getVerticalLevel();
        pdsLevelNode.setAttribute(PDSL_ID, Integer.toString(level.getLevelType()));
        pdsLevelNode.setAttribute(PDSL_NAME, level.getLevelName());
        pdsLevelNode.setAttribute(PDSL_DESCRIPTION, level.getLevelDescription());
        pdsLevelNode.setAttribute(PDSL_UNITS, level.getLevelUnits());
        pdsLevelNode.setAttribute(PDSL_ISNUMERIC, Boolean.toString(level.isHasExplicitVerticalAxis()));
        pdsLevelNode.setAttribute(PDSL_VALUES, wrapper.getLevelValues(imageIndex));
        pdsLevelNode.setAttribute(PDSL_POSITIVE, level.getPositive());
        pdsLevelNode.setAttribute(PDSL_AXISTYPE, level.getAxisType());
        root.appendChild(pdsLevelNode);

        return root;
    }
}
